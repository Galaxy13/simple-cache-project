package com.galaxy13.network.netty.decoder;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.CacheResponse;
import io.netty.buffer.ByteBuf;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.handler.codec.ByteToMessageDecoder;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class CacheMessageDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(CacheMessageDecoder.class);

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out){
        if (in.readableBytes() > 4) {
            String message = in.toString(in.readerIndex(), in.readableBytes(), StandardCharsets.UTF_8);
            in.readerIndex(in.writerIndex());
            Map<String, String> parameters = getFields(message);
            try {
                Operation operation = Operation.valueOf(parameters.get("op"));
                CacheMessage cacheMessage = new CacheMessage() {
                    private final Operation op = operation;
                    private final Map<String, String> params = parameters;

                    @Override
                    public Operation getOperation() {
                        return op;
                    }

                    @Override
                    public String getParameter(String key) {
                        return params.get(key);
                    }
                };
                out.add(cacheMessage);
            } catch (IllegalArgumentException e){
                logger.info("Invalid operation", e);
                ctx.writeAndFlush(CacheResponse.create(MessageCode.FORMAT_EXCEPTION));
                throw new CorruptedFrameException("Invalid operation");
            }
        }
    }

    private Map<String, String> getFields(String message) {
        return Arrays.stream(message.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error(cause.getMessage(), cause);
        ctx.close();
    }
}
