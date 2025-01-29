package com.galaxy13.network.netty.handler;

import com.galaxy13.network.message.Response;
import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;
import com.galaxy13.network.message.code.MessageCode;
import io.netty.channel.ChannelHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

@ChannelHandler.Sharable
public class ResponseHandler extends SimpleChannelInboundHandler<Response> {
    private static final Logger logger = LoggerFactory.getLogger(ResponseHandler.class);

    private final ResponseAction responseAction;
    private final ErrorAction errorAction;
    private final Phaser pendingRequests;
    private final List<Future<?>> pendingFutures;
    private final ExecutorService executor;

    public ResponseHandler(final ResponseAction responseAction, final ErrorAction errorAction,
                           final Phaser pendingRequests,
                           final List<Future<?>> pendingFutures,
                           final ExecutorService executor) {
        this.pendingRequests = pendingRequests;
        this.pendingFutures = pendingFutures;
        this.executor = executor;
        this.responseAction = responseAction;
        this.errorAction = errorAction;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, Response response) {
        logger.trace("Handler accepted response: {}", response);
        Future<?> future = executor.submit(() -> responseAction.action(response));
        pendingFutures.add(future);
        pendingRequests.arriveAndDeregister();
        if (response.getCode() != MessageCode.SUBSCRIPTION_RESPONSE || response.getCode() != MessageCode.SUBSCRIPTION_SUCCESS){
            ctx.channel().close().addListener(f -> {
                if (f.isSuccess()) {
                    logger.trace("Channel {} closed", ctx.channel().id().asLongText());
                } else {
                    throw new RuntimeException(f.cause());
                }
            });
        }
    }

    @Override
    public void channelReadComplete(ChannelHandlerContext ctx){
        logger.trace("Response received. Closing channel...");

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
        logger.error("Exception caught", cause);
        errorAction.execute((Exception) cause);
        pendingRequests.arriveAndDeregister();
    }
}
