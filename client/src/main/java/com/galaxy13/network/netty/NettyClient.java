package com.galaxy13.network.netty;

import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.netty.message.ClientMessageHandler;
import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResponseAction;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.Channel;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioSocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

import java.net.InetSocketAddress;

public class NettyClient implements NetworkStorageClient {

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
                        ch.pipeline().addLast(new ClientMessageHandler(respAction, errorAction));
                    }
                });
        ChannelFuture future = bootstrap.connect().sync();
        Channel channel = future.channel();
        channel.writeAndFlush(message);
    }
}
