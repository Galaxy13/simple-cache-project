package com.galaxy13.network.netty.encoder;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.response.Response;
import com.galaxy13.storage.Value;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.MessageToByteEncoder;

import java.nio.charset.StandardCharsets;

public class ResponseEncoder extends MessageToByteEncoder<Response<Value>> {

    private final MessageCreator creator;

    public ResponseEncoder(MessageCreator creator) {
        this.creator = creator;
    }

    @Override
    protected void encode(ChannelHandlerContext ctx, Response<Value> msg, ByteBuf out){
        MessageCode code = msg.messageCode();
        Value value = msg.value();
        String key = msg.key();
        MessageCreator.MessageBuilder builder = creator.builder(code);
        if (value != null) {
            builder.setValue(value);
        }
        if (key != null) {
            builder.setKey(key);
        }
        out.writeBytes(builder.build().getBytes(StandardCharsets.UTF_8));
    }
}
