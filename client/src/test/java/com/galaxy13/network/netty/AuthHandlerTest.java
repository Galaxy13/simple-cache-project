package com.galaxy13.network.netty;

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

import static org.assertj.core.api.Assertions.*;

class AuthHandlerTest {
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        Credentials creds = new Credentials("test", "123");
        MessageCreator creator = new MessageCreatorImpl(";", ":");
        AuthHandler authHandler = new AuthHandler(creds, creator);
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(authHandler);
    }

    @Test
    void wrongMessageFormatHandling() {
        ByteBuf buf = Unpooled.copiedBuffer("wrong", CharsetUtil.UTF_8);
        assertThatThrownBy(() -> channel.writeInbound(buf)).isInstanceOf(CorruptedFrameException.class);
    }
}
