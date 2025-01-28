package com.galaxy13.network.netty.handler;

import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.processor.ProcessorController;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

@ChannelHandler.Sharable
public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<CacheMessage> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTCPChannelHandler.class);

    private final ProcessorController controller;

    public SimpleTCPChannelHandler(ProcessorController controller) {
        this.controller = controller;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CacheMessage msg){
        logger.info("{}: {}", ctx.channel().remoteAddress(), msg.getOperation());
        controller.processMessage(msg, ctx.channel());
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx){
        logger.info("{}: connected", ctx.channel().remoteAddress());
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx){
        logger.info("{}: channel inactive", ctx.channel().remoteAddress());
    }
}
