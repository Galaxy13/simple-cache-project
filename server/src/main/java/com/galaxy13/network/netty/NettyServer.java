package com.galaxy13.network.netty;

import com.galaxy13.network.StorageServer;
import com.galaxy13.network.message.MessageHandler;
import com.galaxy13.network.message.MessageHandlerImpl;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.StorageImpl;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelOption;
import io.netty.channel.EventLoopGroup;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer implements StorageServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private int port;

    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    private final Storage<String, String> storage;
    private final MessageHandler handler;

    public NettyServer(int port) {
        this.port = port;
        this.storage = new StorageImpl<>(100);
        this.handler = new MessageHandlerImpl(storage);
    }

    @Override
    public void start() throws InterruptedException {
        logger.info("Cache server starting at port {}", port);
        this.bossGroup = new NioEventLoopGroup();
        this.workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap bootstrap = new ServerBootstrap();
            bootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .localAddress(new InetSocketAddress(port))
                    .childHandler(new SimpleTCPChanelInitializer(handler))
                    .childOption(ChannelOption.SO_KEEPALIVE, true);
            ChannelFuture future = bootstrap.bind(port).sync();
            if (future.isSuccess()) {
                logger.info("Cache server started at port {}", port);
                future.channel().closeFuture().sync();
            }
        } finally {
            logger.info("Cache server stopped");
            workerGroup.shutdownGracefully();
            bossGroup.shutdownGracefully();
        }
    }

    @Override
    public void stop() throws InterruptedException {

    }
}
