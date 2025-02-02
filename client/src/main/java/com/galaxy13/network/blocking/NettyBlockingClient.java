package com.galaxy13.network.blocking;

import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.decoder.ResponseDecoder;
import io.netty.bootstrap.Bootstrap;
import io.netty.channel.*;
import io.netty.channel.oio.OioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.oio.OioSocketChannel;
import io.netty.handler.codec.string.StringEncoder;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.net.InetSocketAddress;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;

@SuppressWarnings({"deprecation", "unused"})
public class NettyBlockingClient implements BlockingClient {
    private static final Logger logger = LoggerFactory.getLogger(NettyBlockingClient.class);


    private final int port;
    private final String host;
    private final EventLoopGroup group;
    private final Credentials credentials;
    private final MessageCreator creator;

    public NettyBlockingClient(int port, String host, Credentials credentials, MessageCreator creator) {
        this.port = port;
        this.host = host;
        group = new OioEventLoopGroup();
        this.credentials = credentials;
        this.creator = creator;
    }

    @Override
    public Response sendMessage(String message) throws Exception {
        AtomicReference<Response> responseRef = new AtomicReference<>();
        CountDownLatch latch = new CountDownLatch(1);

        Bootstrap bootstrap = new Bootstrap();
        bootstrap.group(group)
                .channel(OioSocketChannel.class)
                .remoteAddress(new InetSocketAddress(host, port))
                .option(ChannelOption.TCP_NODELAY, true)
                .handler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline().addLast(new ResponseDecoder(";", ":"));
                        ch.pipeline().addLast(new StringEncoder());
                        ch.pipeline().addLast(new AuthHandler(credentials, creator));
                        ch.pipeline().addLast(new SimpleChannelInboundHandler<Response>() {
                            @Override
                            protected void channelRead0(ChannelHandlerContext ctx, Response msg) {
                                responseRef.set(msg);
                                latch.countDown();
                                ctx.close().addListener(future -> {
                                    if (future.isSuccess()) {
                                        logger.trace("OIO channel closed: {}", ctx.channel());
                                    } else {
                                        logger.warn("OIO channel closing exception: {}", ctx.channel());
                                    }
                                });
                            }

                            @Override
                            public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) {
                                logger.error("OIO channel exception", cause);
                                ctx.close().addListener(future -> {
                                    if (future.isSuccess()) {
                                        logger.warn("OIO channel closed with exception: {}", ctx.channel(), cause);
                                    } else {
                                        logger.error("OIO channel closing exception", cause);
                                    }
                                });
                            }
                        });
                    }
                });
        try {
            ChannelFuture channelFuture = bootstrap.connect().sync();
            Channel channel = channelFuture.channel();

            channel.writeAndFlush(message).addListener((ChannelFutureListener) future -> {
                if (future.isSuccess()) {
                    logger.trace("Message sent successfully: {}", message);
                }
            });

            if (!latch.await(10, TimeUnit.SECONDS)){
                throw new RuntimeException("OIO channel timeout");
            }
            return responseRef.get();
        } finally {
            group.shutdownGracefully();
        }
    }
}
