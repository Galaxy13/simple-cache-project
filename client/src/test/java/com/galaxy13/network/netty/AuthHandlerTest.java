package com.galaxy13.network.netty;

import com.galaxy13.network.exception.CredentialException;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class AuthHandlerTest {
    private EmbeddedChannel channel;
    private Credentials credentials;

    @BeforeEach
    void setUp() {
        credentials = new Credentials("test", "123");
        MessageCreator creator = new MessageCreatorImpl(";", ":");
        AuthHandler authHandler = new AuthHandler(credentials, creator);
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(authHandler);
    }

    @Test
    void wrongMessageFormatHandling() {
        ByteBuf buf = Unpooled.copiedBuffer("wrong", CharsetUtil.UTF_8);
        assertThatThrownBy(() -> channel.writeInbound(buf)).isInstanceOf(CorruptedFrameException.class);
    }

    @Test
    void wrongCredentialsHandling() {
        Response response = Response.readFromMsg(Map.of("code", MessageCode.AUTHENTICATION_FAILURE.code()));
        assertThatThrownBy(() -> channel.writeInbound(response)).isInstanceOf(CredentialException.class);
    }

    @Test
    void wrongTokenHandling() {
        credentials.setToken("test");
        Response response = Response.readFromMsg(Map.of("code", MessageCode.INVALID_TOKEN.code()));
        channel.writeInbound(response);

        assertThat(credentials.getToken()).isNull();
    }
}
