package com.galaxy13.network;

import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.util.concurrent.Future;

import java.net.InetSocketAddress;

public class NettyClient implements NetworkStorageClient{

    private final Bootstrap bootstrap;

    public NettyClient(int port, String host) {
        var group = new NioEventLoopGroup(1);
        try {
            this.bootstrap = new Bootstrap();
            this.bootstrap.group(group)
                    .channel(NioSocketChannel.class)
                    .remoteAddress(new InetSocketAddress(host, port))
                    .handler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        public void initChannel(SocketChannel ch) {
                            ch.pipeline().addLast(new StorageClientHandler());
                        }
                    });
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    @Override
    public ChannelFuture sendMessage(String message) throws InterruptedException {
        return bootstrap.connect().sync();
    }
}
