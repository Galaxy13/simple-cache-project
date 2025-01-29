package com.galaxy13.network.netty.decoder;

import com.galaxy13.network.message.Response;
import io.netty.buffer.ByteBuf;
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

public class ResponseDecoder extends ByteToMessageDecoder {
    private static final Logger logger = LoggerFactory.getLogger(ResponseDecoder.class);

    private final String parameterDelimiter;
    private final String equalSign;

    public ResponseDecoder(String parameterDelimiter, String equalSign) {
        this.parameterDelimiter = parameterDelimiter;
        this.equalSign = equalSign;
    }

    @Override
    protected void decode(ChannelHandlerContext ctx, ByteBuf in, List<Object> out) {
        logger.trace("Received msg from cache");
        if (in.readableBytes() > 4) {
            String msg = in.toString(in.readerIndex(), in.readableBytes(), StandardCharsets.UTF_8);
            in.readerIndex(in.writerIndex());
            Map<String, String> messageValues = getValuesFromMsg(msg);
            try {
                Response response = Response.readFromMsg(messageValues);
                out.add(response);
                logger.info("Response read successfully: {}", response);
            } catch (IllegalArgumentException e) {
                ctx.close();
                throw new CorruptedFrameException("Unknown response format received: " + msg, e);
            }
        }
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(parameterDelimiter))
                .map(s -> s.split(equalSign))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
