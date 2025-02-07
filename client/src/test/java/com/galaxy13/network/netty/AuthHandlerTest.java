package com.galaxy13.network.netty;

import com.galaxy13.network.exception.CredentialException;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.auth.QueuedMessage;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Queue;
import java.util.concurrent.ConcurrentLinkedQueue;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.locks.ReentrantLock;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

class AuthHandlerTest {
    private EmbeddedChannel channel;
    private Credentials credentials;
    private ReentrantLock lock;
    private AtomicBoolean atomicBoolean;
    private Queue<QueuedMessage> queue;

    @BeforeEach
    void setUp() {
        credentials = Mockito.mock(Credentials.class);
        lock = new ReentrantLock();
        atomicBoolean = new AtomicBoolean(false);
        queue = new ConcurrentLinkedQueue<>();
        MessageCreator creator = new MessageCreatorImpl(";", ":");
        AuthHandler authHandler = new AuthHandler(credentials, creator, lock, atomicBoolean, queue);
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(authHandler);
    }

    @Test
    void wrongMessageFormatHandling() {
        // assert wrong message format
        ByteBuf buf = Unpooled.copiedBuffer("wrong", CharsetUtil.UTF_8);
        assertThatThrownBy(() -> channel.writeInbound(buf)).isInstanceOf(CorruptedFrameException.class);
    }

    @Test
    void wrongCredentialsHandling() {
        // assert auth failure response
        Response response = Response.readFromMsg(Map.of("code", MessageCode.AUTHENTICATION_FAILURE.code()));
        assertThatThrownBy(() -> channel.writeInbound(response)).isInstanceOf(CredentialException.class);
    }

    @Test
    void wrongTokenHandling() {
        // credentials mocking
        when(credentials.getLogin()).thenReturn("login");
        when(credentials.getPassword()).thenReturn("password");

        // accepting INVALID_TOKEN response from server
        Response response = Response.readFromMsg(Map.of("code", MessageCode.INVALID_TOKEN.code()));
        channel.writeInbound(response);

        // checking that token is set to null, if invalid
        assertThat(credentials.getToken()).isNull();
    }

    @Test
    void authenticationRequestHandlingTest() {
        // credentials mocking
        String token = "token";
        String login = "login";
        String password = "pass";
        when(credentials.containsToken()).thenReturn(false).thenReturn(true);
        when(credentials.getToken()).thenReturn(token);
        when(credentials.getLogin()).thenReturn(login);
        when(credentials.getPassword()).thenReturn(password);

        // sending PUT request through handler
        String message = "op:PUT;key:testKey;value:12345;";
        channel.writeOutbound(message);

        // checking outbound request and performed actions
        String authRequest = channel.readOutbound();
        verify(credentials, times(1)).getLogin();
        verify(credentials, times(1)).getPassword();
        verify(credentials, times(1)).containsToken();
        assertThat(authRequest).isNotNull()
                .startsWith("op:AUTHENTICATE;")
                .contains("login:" + login + ";")
                .contains("password:" + password + ";");

        // accepting AUTH_SUCCESS response from server
        Response response = Response.readFromMsg(Map.of("code", MessageCode.AUTHENTICATION_SUCCESS.code(), "token", token));
        channel.writeInbound(response);

        assertThat(channel.finish()).isTrue();

        // check that token is accepted and set into credentials
        verify(credentials).setToken(token);
        String queuedPutRequest = channel.readOutbound();
        assertThat(queuedPutRequest).isNotNull().isEqualTo(message + "token:" + token + ";");
    }

    @Test
    void invalidTokenHandlingTest() {
        // credentials mocking
        when(credentials.getLogin()).thenReturn("login");
        when(credentials.getPassword()).thenReturn("pass");
        when(credentials.containsToken()).thenReturn(false);

        // accepting invalid token response
        Response response = Response.readFromMsg(Map.of("code", MessageCode.INVALID_TOKEN.code()));
        channel.writeInbound(response);

        // check that invalid token is set to null
        verify(credentials).setToken(null);
        assertThat(channel.finish()).isTrue();

        // check that handler executes new authentication request
        String newAuthRequest = channel.readOutbound();
        assertThat(newAuthRequest).isNotNull().startsWith("op:AUTHENTICATE;")
                .contains("login:" + credentials.getLogin() + ";")
                .contains("password:" + credentials.getPassword() + ";");
    }
}
