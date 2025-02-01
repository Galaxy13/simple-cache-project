package com.galaxy13.network.netty.encoder;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.response.Response;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class ResponseEncoder<K, V> extends MessageToByteEncoder<Response<K, V>> {

    private final MessageCreator<K, V> creator;

    public ResponseEncoder(MessageCreator<K, V> creator) {
        this.creator = creator;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Response<K, V> msg, ByteBuf out) {
        String message = creator.fromResponse(msg);
        out.writeBytes(message.getBytes(StandardCharsets.UTF_8));
    }
}
