package com.galaxy13.network.netty;

import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.auth.QueuedMessage;
import com.galaxy13.network.netty.decoder.ResponseDecoder;
import com.galaxy13.network.netty.handler.ResponseHandler;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.string.StringEncoder;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.RepeatedTest;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;
import org.mockito.stubbing.Answer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FullPipelineTest {
    private EmbeddedChannel channel;
    private Credentials credentials;
    private MessageCreator messageCreator;
    private ResponseAction action;
    private ErrorAction errorAction;
    private final List<Future<?>> futures = new ArrayList<>();
    private final Phaser phaser = new Phaser(1);
    private Queue<QueuedMessage> queue;

    @BeforeEach
    void setUp() {
        credentials = Mockito.mock(Credentials.class);
        action = Mockito.mock(ResponseAction.class);
        errorAction = Mockito.mock(ErrorAction.class);
        queue = new ConcurrentLinkedQueue<>();
        ExecutorService executor = Mockito.mock(ExecutorService.class);

        mockAsSynchronizedExecutor(executor);

        String parameterDelimiter = ";";
        String equalDelimiter = ":";

        messageCreator = new MessageCreatorImpl(parameterDelimiter, equalDelimiter);
        var decoder = new ResponseDecoder(parameterDelimiter, equalDelimiter);
        var authHandler = new AuthHandler(credentials, messageCreator, new ReentrantLock(), new AtomicBoolean(false), queue);
        var clientHandler = new ResponseHandler(action, errorAction, phaser, futures, executor);

        channel = new EmbeddedChannel();
        channel.pipeline().addLast(decoder);
        channel.pipeline().addLast(new StringEncoder());
        channel.pipeline().addLast(authHandler);
        channel.pipeline().addLast(clientHandler);
    }

    @RepeatedTest(5)
    void wrongCredentialsTest(){
        // setting "wrong" credentials
        String login = "wrongLogin";
        String password = "wrongPassword";

        when(credentials.containsToken()).thenReturn(false);
        when(credentials.getLogin()).thenReturn(login);
        when(credentials.getPassword()).thenReturn(password);

        // sending GET request to server
        String message = messageCreator.createRequest(Operation.GET, Map.of("key", "testKey"));
        channel.writeOutbound(message);

        // accepting AUTHENTICATION_ERROR message from server
        channel.writeInbound(Unpooled.copiedBuffer("code:305;", CharsetUtil.UTF_8));
        assertThat(channel.finish()).isTrue();

        // testing auth request creation
        ByteBuf authRequest = channel.readOutbound();
        assertThat(authRequest).isNotNull();
        String strRequest = authRequest.toString(CharsetUtil.UTF_8);
        assertThat(strRequest).startsWith("op:AUTHENTICATE")
                .contains("login:" + login + ";")
                .contains("password:" + password + ';');

        // testing reaction to invalid credentials
        verify(credentials, times(1)).containsToken();
        verify(action, times(0)).action(ArgumentMatchers.any());
        verify(errorAction, times(1)).execute(ArgumentMatchers.any());
    }

    @RepeatedTest(5)
    void tokenAcquireTest(){
        // mocking credentials
        String login = "testLogin";
        String password = "testPassword";
        String token = "testToken";
        when(credentials.containsToken()).thenReturn(false).thenReturn(true);
        when(credentials.getLogin()).thenReturn(login);
        when(credentials.getPassword()).thenReturn(password);

        // sending message to server
        String message = messageCreator.createRequest(Operation.GET, Map.of("key", "testKey"));
        channel.writeOutbound(message);

        // receiving response from server
        channel.writeInbound(Unpooled.copiedBuffer("code:203;token:" + token + ";", CharsetUtil.UTF_8));
        assertThat(channel.finish()).isTrue();

        // check auth request
        ByteBuf authRequest = channel.readOutbound();
        assertThat(authRequest).isNotNull();
        String strRequest = authRequest.toString(CharsetUtil.UTF_8);
        assertThat(strRequest).startsWith("op:AUTHENTICATE")
                .contains("login:" + login + ";")
                .contains("password:" + password + ';');

        verify(credentials, times(2)).containsToken();
        verify(credentials, times(1)).getLogin();
        verify(credentials, times(1)).getPassword();
        verify(errorAction, times(0)).execute(ArgumentMatchers.any());

        // check that token is set from response from the server
        verify(credentials).setToken(token);
    }

    @RepeatedTest(5)
    void performPutOperationTest(){
        // simulating token check
        String token = "token12345";
        when(credentials.containsToken()).thenReturn(true);
        when(credentials.getToken()).thenReturn(token);

        // sending put request to server
        String key = "strg456";
        String value = "deckard";

        String message = messageCreator.createRequest(Operation.PUT, Map.of("key", key, "value", value));
        channel.writeOutbound(message);

        // sending answer response to put message
        String strPutResponse = "code:" + MessageCode.OK.code() + ";key:" + key + ";value:" + value + ";";
        ByteBuf putResponse = Unpooled.copiedBuffer(strPutResponse, CharsetUtil.UTF_8);
        channel.writeInbound(putResponse);
        assertThat(channel.finish()).isTrue();

        // check outbound request
        ByteBuf authRequest = channel.readOutbound();
        assertThat(authRequest).isNotNull();
        String strRequest = authRequest.toString(CharsetUtil.UTF_8);
        assertThat(strRequest).startsWith("op:PUT;")
                .contains("token:" + token + ";")
                .contains("key:" + key + ";")
                .contains("value:" + value + ";");
        verify(credentials, times(1)).containsToken();
        verify(credentials, times(1)).getToken();

        // check actions, performed after receiving response
        verify(action, times(1)).action(Response.readFromMsg(
                Map.of("key", key, "value", value, "code", MessageCode.OK.code())));
        verify(errorAction, times(0)).execute(ArgumentMatchers.any());
    }

    @RepeatedTest(5)
    void getOperationTest(){
        // simulating token check
        String token = "token12345";
        when(credentials.containsToken()).thenReturn(true);
        when(credentials.getToken()).thenReturn(token);

        // sending get request to server
        String key = "lowKey";

        String message = messageCreator.createRequest(Operation.GET, Map.of("key", key));
        channel.writeOutbound(message);

        // accepting NOT_PRESENT response from server
        String strGetResponse = "code:" + MessageCode.NOT_PRESENT.code() + ";";
        ByteBuf authRequest = Unpooled.copiedBuffer(strGetResponse, CharsetUtil.UTF_8);
        channel.writeInbound(authRequest);
        assertThat(channel.finish()).isTrue();

        // check outbound message
        ByteBuf authResponse = channel.readOutbound();
        assertThat(authResponse).isNotNull();
        String strRequest = authResponse.toString(CharsetUtil.UTF_8);
        assertThat(strRequest).isEqualTo(
                "op:" + Operation.GET + ";key:" + key + ";token:" + token + ";");
        verify(credentials, times(1)).containsToken();
        verify(credentials, times(1)).getToken();

        // check response to server response
        verify(action, times(1)).action(Response.readFromMsg(
                Map.of("code", MessageCode.NOT_PRESENT.code())
        ));
        verify(errorAction, times(0)).execute(ArgumentMatchers.any());
    }

    @RepeatedTest(5)
    void subscribingTest(){
        // simulating token check
        String token = "token12345";
        when(credentials.containsToken()).thenReturn(true);
        when(credentials.getToken()).thenReturn(token);

        // sending subscribing request to server
        String key = "subKey";
        String message = messageCreator.createRequest(Operation.SUBSCRIBE, Map.of("key", key));
        channel.writeOutbound(message);

        // accepting response from server
        String strSubscribeResponse = "code:" + MessageCode.SUBSCRIPTION_SUCCESS.code() + ";key:" + key + ";";
        ByteBuf subscribeResponse = Unpooled.copiedBuffer(strSubscribeResponse, CharsetUtil.UTF_8);
        channel.writeInbound(subscribeResponse);

        // check that client not closing connection when subscribed
        assertThat(channel.isOpen()).isTrue();

        // check outbound request
        assertThat(channel.finish()).isTrue();
        ByteBuf subscribeRequest = channel.readOutbound();
        assertThat(subscribeRequest).isNotNull();
        String strRequest = subscribeRequest.toString(CharsetUtil.UTF_8);
        assertThat(strRequest).contains("op:SUBSCRIBE;")
                .contains("key:" + key + ";")
                .contains("token:" + token + ";");
        verify(credentials, times(1)).containsToken();
        verify(credentials, times(1)).getToken();

        // check actions on response
        verify(action, times(1)).action(Response.readFromMsg(
                Map.of("code", MessageCode.SUBSCRIPTION_SUCCESS.code(),
                        "key", key)
        ));
        verify(errorAction, times(0)).execute(ArgumentMatchers.any());
    }

    // this mocking executes Runnable in same thread, so the verify() can be correctly checked
    private void mockAsSynchronizedExecutor(ExecutorService executor) {
        doAnswer((Answer<Object>) invocation -> {
            ((Runnable) invocation.getArguments()[0]).run();
            return null;
        }).when(executor).submit(any(Runnable.class));
    }
}
