package com.galaxy13.network;

import com.galaxy13.storage.ResponseAction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class StorageClientHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(StorageClientHandler.class);

    private final ResponseAction action;

    public StorageClientHandler(ResponseAction action) {
        this.action = action;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        logger.info("Received message: {}", msg);
        action.action(msg);
        ctx.channel().close();
    }
}
