package com.galaxy13.network.server;

import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.netty.decoder.CacheMessageDecoder;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

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
}
