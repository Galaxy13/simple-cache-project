package com.galaxy13.network.message.handler.netty;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class TCPChannelHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(TCPChannelHandler.class);

    private final MessageAsyncHandler clientMessageAsyncHandler;

    public TCPChannelHandler(final MessageAsyncHandler clientMessageAsyncHandler) {
        this.clientMessageAsyncHandler = clientMessageAsyncHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        logger.info("Received message: {}", msg);
        clientMessageAsyncHandler.handleMessage(msg);
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught", cause);
        clientMessageAsyncHandler.exceptionCaught(cause);
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
