package com.galaxy13.network.netty.auth;

import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;

public record QueuedMessage(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {
    public void sendMessage() {
        ctx.channel().writeAndFlush(msg, promise);
    }
}
