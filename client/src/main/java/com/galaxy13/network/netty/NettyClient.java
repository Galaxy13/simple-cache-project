package com.galaxy13.network.netty;

import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;
import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.decoder.ResponseDecoder;
import com.galaxy13.network.netty.handler.ResponseHandler;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.concurrent.Phaser;

public class NettyClient implements NetworkStorageClient{
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final NioEventLoopGroup group;
    private final int port;
    private final String host;
    private final Phaser pendingRequests;
    private final List<Future<?>> pendingFutures;
    private final ExecutorService executor;
    private final Credentials credentials;
    private final MessageCreator messageCreator;

    public NettyClient(int port,
                       String host,
                       ExecutorService executor,
                       Credentials credentials,
                       MessageCreator creator) {
        this.port = port;
        this.host = host;
        this.group = new NioEventLoopGroup();
        this.pendingRequests = new Phaser(1);
        this.pendingFutures = new ArrayList<>();
        this.executor = executor;
        this.credentials = credentials;
        this.messageCreator = creator;
    }

    @Override
    public void sendMessage(String message, ResponseAction respAction, ErrorAction errorAction) throws InterruptedException {
        pendingRequests.register();
        var bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ResponseDecoder(";", ":"));
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new AuthHandler(credentials, messageCreator));
                        ch.pipeline().addLast(new ResponseHandler(respAction,
                                errorAction,
                                pendingRequests,
                                pendingFutures, executor));
                    }
                });
        ChannelFuture future = bootstrap.connect().sync();
        var channel = future.channel();
        channel.writeAndFlush(message).addListener(fut -> {
            if(!fut.isSuccess()){
                logger.error("Error sending data to server", future.cause());
                pendingRequests.arriveAndDeregister();
            } else {
                logger.info("Successfully sent data to server: {}", channel.remoteAddress());
            }
        });
    }

    public void shutdown(){
        try {
            pendingRequests.arriveAndAwaitAdvance();
            for (Future<?> future : pendingFutures) {
                future.get();
            }
            executor.shutdown();
        } catch (IllegalArgumentException | InterruptedException | ExecutionException e) {
            logger.error("Error closing pending requests", e);
            Thread.currentThread().interrupt();
        }

        group.shutdownGracefully().syncUninterruptibly().addListener(future -> {
            if (future.isSuccess()) {
                logger.info("Netty event group shutdown successfully. Executor shutdown awaiting...");
            }
        });
    }
}
