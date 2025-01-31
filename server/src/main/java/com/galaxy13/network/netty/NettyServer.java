package com.galaxy13.network.netty;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.decoder.CacheMessageDecoder;
import com.galaxy13.network.netty.encoder.ResponseEncoder;
import com.galaxy13.network.netty.handler.SimpleTCPChannelHandler;
import com.galaxy13.processor.ProcessorController;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;

public class NettyServer implements StorageServer {
    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);
    private final int port;

    private final ProcessorController processorController;
    private final MessageCreator messageCreator;
    private final Credentials credentials;
    private EventLoopGroup bossGroup;
    private EventLoopGroup workerGroup;

    public NettyServer(int port, ProcessorController controller, MessageCreator creator, Credentials credentials) {
        this.port = port;
        this.processorController = controller;
        this.messageCreator = creator;
        this.credentials = credentials;
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
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) {
                            socketChannel.pipeline().addLast(new CacheMessageDecoder());
                            socketChannel.pipeline().addLast(new ResponseEncoder(messageCreator));
                            socketChannel.pipeline().addLast(new AuthHandler(credentials));
                            socketChannel.pipeline().addLast(new SimpleTCPChannelHandler(processorController));
                        }
                    })
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
    public void stop() {
        logger.info("Cache server stopped by user");
        workerGroup.shutdownGracefully();
        bossGroup.shutdownGracefully();
    }
}
