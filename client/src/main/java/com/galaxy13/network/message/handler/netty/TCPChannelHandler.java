package com.galaxy13.network.message.handler.netty;

import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Phaser;

@ChannelHandler.Sharable
public class TCPChannelHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(TCPChannelHandler.class);

    private final MessageAsyncHandler clientMessageAsyncHandler;
    private final Phaser pendingRequests;
    private final ExecutorService executor;

    public TCPChannelHandler(final MessageAsyncHandler clientMessageAsyncHandler,
                             final Phaser pendingRequests,
                             final ExecutorService executor) {
        this.clientMessageAsyncHandler = clientMessageAsyncHandler;
        this.pendingRequests = pendingRequests;
        this.executor = executor;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        logger.info("Received message: {}", msg);
        executor.execute(() -> clientMessageAsyncHandler.handleMessage(msg));
        pendingRequests.arriveAndDeregister();
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        logger.info("Response received. Closing channel...");
        ctx.channel().close().addListener(future -> {
            if (future.isSuccess()) {
                logger.info("Channel closed");
            } else {
                throw new RuntimeException(future.cause());
            }
        });
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        clientMessageAsyncHandler.exceptionCaught(cause);
        pendingRequests.arriveAndDeregister();
    }
}
