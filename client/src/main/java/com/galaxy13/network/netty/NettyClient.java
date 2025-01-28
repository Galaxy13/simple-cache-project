package com.galaxy13.network.netty;

import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.message.handler.netty.ClientMessageAsyncHandler;
import com.galaxy13.network.message.handler.netty.TCPChannelHandler;
import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResponseAction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class NettyClient implements NetworkStorageClient{
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final NioEventLoopGroup group = new NioEventLoopGroup();
    private final int port;
    private final String host;
    private final AtomicInteger pendingRequests = new AtomicInteger(0);
    private final ExecutorService executor = Executors.newCachedThreadPool();


    public NettyClient(int port, String host) {
        this.port = port;
        this.host = host;
    }

    @Override
    public void sendMessage(String message, ResponseAction respAction, ErrorAction errorAction) throws InterruptedException {
        pendingRequests.incrementAndGet();
        var bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new TCPChannelHandler(
                                new ClientMessageAsyncHandler(respAction, errorAction), pendingRequests, executor)
                        );
                    }
                });
        ChannelFuture future = bootstrap.connect().sync();
        var channel = future.channel();
        channel.writeAndFlush(message).addListener((ChannelFutureListener) fut -> {
            if(!fut.isSuccess()){
                logger.error("Error sending data to server, retrying...", future.cause());
                pendingRequests.decrementAndGet();
                sendMessage(message, respAction, errorAction);
            } else {
                logger.info("Successfully sent data to server: {}", channel.remoteAddress());
            }
        });
    }

    public void shutdown(){
        while (pendingRequests.get() > 0) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
                logger.error("Interrupted while waiting for pending requests", e);
                Thread.currentThread().interrupt();
            }
        }

        group.shutdownGracefully().syncUninterruptibly().addListener(future -> {
            if (future.isSuccess()) {
                logger.info("Netty event group shutdown successfully. Executor shutdown awaiting...");
            }
        });
    }
}
