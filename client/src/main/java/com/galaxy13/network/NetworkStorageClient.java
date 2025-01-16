package com.galaxy13.network;

import io.netty.channel.ChannelFuture;

public interface NetworkStorageClient {
    ChannelFuture sendMessage(String message) throws InterruptedException;
}
