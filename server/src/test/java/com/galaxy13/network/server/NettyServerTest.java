package com.galaxy13.network.server;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.Response;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.decoder.CacheMessageDecoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class NettyServerTest {
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        channel = new EmbeddedChannel();
    }

    @Test
    void testDecoderGET() {
        ByteToMessageDecoder decoder = new CacheMessageDecoder();
        channel.pipeline().addLast(decoder);

        channel.writeInbound(Unpooled.copiedBuffer("op:GET;key:testKey", CharsetUtil.UTF_8));
        assertThat(channel.finish()).isTrue();

        CacheMessage msg = channel.readInbound();
        assertThat(msg).isNotNull();
        assertThat(msg.getOperation()).isEqualTo(Operation.GET);
        assertThat(msg.getParameter("key")).isEqualTo("testKey");
    }

    @Test
    void testDecoderPUT() {
        ByteToMessageDecoder decoder = new CacheMessageDecoder();
        channel.pipeline().addLast(decoder);

        channel.writeInbound(Unpooled.copiedBuffer("op:PUT;key:testKey;value:1234", CharsetUtil.UTF_8));
        assertThat(channel.finish()).isTrue();

        CacheMessage msg = channel.readInbound();
        assertThat(msg).isNotNull();
        assertThat(msg.getOperation()).isEqualTo(Operation.PUT);
        assertThat(msg.getParameter("key")).isEqualTo("testKey");
        assertThat(msg.getParameter("value")).isEqualTo("1234");
    }

    @Test
    void testAuthHandlerPass() {
        Credentials credentials = new Credentials("testLogin", "testPassword");
        channel.pipeline().addLast(new AuthHandler(credentials));

        channel.writeInbound(new TestCacheMessage(Operation.AUTHENTICATE, Map.of(
                "login", "testLogin",
                "password", "testPassword"
        )));

        Response<String, String> msg = channel.readOutbound();
        assertThat(msg).isNotNull();
        assertThat(msg.messageCode()).isEqualTo(MessageCode.AUTHENTICATION_SUCCESS);
        assertThat(msg.getParameter("token")).isNotNull();

        String token = msg.getParameter("token");

        channel.writeInbound(new TestCacheMessage(Operation.PUT, Map.of(
                "token", token,
                "key", "testKey",
                "value", "1234"
        )));
        assertThat(channel.finish()).isTrue();

        CacheMessage tokenResponse = channel.readInbound();
        assertThat(tokenResponse).isNotNull();
        assertThat(tokenResponse.getOperation()).isEqualTo(Operation.PUT);
        assertThat(tokenResponse.getParameter("key")).isEqualTo("testKey");
        assertThat(tokenResponse.getParameter("value")).isEqualTo("1234");
    }

    @Test
    void testAuthHandlerFail() {
        String login = "testLogin";
        String password = "testPassword";
        Credentials credentials = new Credentials(login, password);

        testWrongCredentials(credentials, "wrongLogin", password);
        testWrongCredentials(credentials, login, "wrongPassword");
        testWrongCredentials(credentials, "", "");

        channel = new EmbeddedChannel(new AuthHandler(credentials));

        channel.writeInbound(new TestCacheMessage(Operation.AUTHENTICATE, Map.of(
                "login", "testLogin",
                "password", "testPassword"
        )));
        Response<String, String> msg3 = channel.readOutbound();
        assertThat(msg3).isNotNull();
        assertThat(msg3.messageCode()).isEqualTo(MessageCode.AUTHENTICATION_SUCCESS);
        assertThat(msg3.getParameter("token")).isNotNull();

        channel.writeInbound(new TestCacheMessage(Operation.GET, Map.of(
                "key", "testKey"
        )));
        Response<String, String> msg4 = channel.readOutbound();
        assertThat(msg4).isNotNull();
        assertThat(msg4.messageCode()).isEqualTo(MessageCode.INVALID_TOKEN);
        assertThat(msg4.getParameter("value")).isNull();

        channel = new EmbeddedChannel(new AuthHandler(credentials));

        channel.writeInbound(new TestCacheMessage(Operation.GET, Map.of(
                "key", "testKey",
                "token", "wrongToken"
        )));
        Response<String, String> msg5 = channel.readOutbound();
        assertThat(msg5).isNotNull();
        assertThat(msg5.messageCode()).isEqualTo(MessageCode.INVALID_TOKEN);
        assertThat(msg5.getParameter("value")).isNull();
    }

    private void testWrongCredentials(Credentials credentials, String login, String password) {
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(new AuthHandler(credentials));
        channel.writeInbound(new TestCacheMessage(Operation.AUTHENTICATE, Map.of(
                "login", login,
                "password", password
        )));

        Response<String, String> msg = channel.readOutbound();
        assertThat(msg).isNotNull();
        assertThat(msg.messageCode()).isEqualTo(MessageCode.AUTHENTICATION_FAILURE);
        assertThat(msg.getParameter("token")).isNull();
    }

    private static class TestCacheMessage implements CacheMessage {
        private final Operation operation;
        private final Map<String, String> map;

        public TestCacheMessage(Operation operation, Map<String, String> map) {
            this.map = map;
            this.operation = operation;
        }

        @Override
        public Operation getOperation() {
            return operation;
        }

        @Override
        public String getParameter(String key) {
            return map.get(key);
        }
    }
}
