package com.galaxy13.network.netty;

import com.galaxy13.network.message.MessageHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.LoggerFactory;
import org.slf4j.Logger;

public class SimpleTCPChannelHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(SimpleTCPChannelHandler.class);

    private final MessageHandler messageHandler;

    public SimpleTCPChannelHandler(MessageHandler messageHandler) {
        this.messageHandler = messageHandler;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg){
        logger.info("{}: {}", ctx.channel().remoteAddress(), msg);
        String responseMsg = messageHandler.handleMessage(msg);
        ctx.writeAndFlush(responseMsg);
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
