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

public class NettyClient implements NetworkStorageClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyClient.class);

    private final NioEventLoopGroup group = new NioEventLoopGroup();
    private final int port;
    private final String host;


    public NettyClient(int port, String host) {
        this.port = port;
        this.host = host;

    }

    @Override
    public void sendMessage(String message, ResponseAction respAction, ErrorAction errorAction) throws InterruptedException {
        var bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(NioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    public void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new StringDecoder());
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new TCPChannelHandler(new ClientMessageAsyncHandler(respAction, errorAction)));
                    }
                });
        ChannelFuture future = bootstrap.connect().sync();
        var channel = future.channel();
        channel.writeAndFlush(message).addListener((ChannelFutureListener) fut -> {
            if(!fut.isSuccess()){
                logger.error("Error sending data to server, retrying...", future.cause());
                sendMessage(message, respAction, errorAction);
            }
        });
    }
}
