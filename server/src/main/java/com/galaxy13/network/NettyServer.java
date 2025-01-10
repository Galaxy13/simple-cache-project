package com.galaxy13.network;

import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer implements StorageServer{
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private int port;

    private EventLoopGroup eventLoopGroup;

    public NettyServer(int port) {
        this.port = port;
    }

    @Override
    public void start() throws InterruptedException {
        this.eventLoopGroup = new NioEventLoopGroup();
            var serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(eventLoopGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port));
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
