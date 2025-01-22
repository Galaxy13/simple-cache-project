package com.galaxy13.network.netty;

import com.galaxy13.network.message.handler.MessageHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.string.StringDecoder;
import io.netty.handler.codec.string.StringEncoder;

public class SimpleTCPChanelInitializer extends ChannelInitializer<SocketChannel> {

    private final MessageHandler handler;

    public SimpleTCPChanelInitializer(MessageHandler handler) {
        this.handler  = handler;
    }
    @Override
    protected void initChannel(SocketChannel socketChannel) {
        socketChannel.pipeline().addLast(new StringEncoder());
        socketChannel.pipeline().addLast(new StringDecoder());
        socketChannel.pipeline().addLast(new SimpleTCPChannelHandler(handler));
    }
}
