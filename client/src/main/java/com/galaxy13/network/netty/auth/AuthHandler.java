package com.galaxy13.network.netty.auth;

import com.galaxy13.network.exception.CredentialException;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import io.netty.channel.ChannelDuplexHandler;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelPromise;
import io.netty.handler.codec.CorruptedFrameException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayDeque;
import java.util.Map;
import java.util.Queue;
import java.util.concurrent.atomic.AtomicBoolean;

public class AuthHandler extends ChannelDuplexHandler {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private final MessageCreator messageCreator;
    private final Credentials credentials;
    private Queue<QueuedMessage> queue;
    private final AtomicBoolean waitingForAuth;

    public AuthHandler(Credentials credentials, MessageCreator messageCreator) {
        this.credentials = credentials;
        this.messageCreator = messageCreator;
        this.queue = new ArrayDeque<>();
        this.waitingForAuth = new AtomicBoolean(false);
    }

    private record QueuedMessage(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) {

        public void sendMessage() {
                ctx.channel().writeAndFlush(msg, promise);
            }
        }

    @Override
    public void write(ChannelHandlerContext ctx, Object msg, ChannelPromise promise) throws Exception {
        if (waitingForAuth.get()) {
            queue.add(new QueuedMessage(ctx, msg, promise));
            logger.info("Message added to queue while waiting for auth");
            return;
        }

        if (!credentials.containsToken()) {
            waitingForAuth.set(true);
            String login = credentials.getLogin();
            String password = credentials.getPassword();
            String message = messageCreator.createRequest(Operation.AUTHENTICATE,
                    Map.of("login", login, "password", password));
            ctx.writeAndFlush(message).addListener(future -> {
                if (!future.isSuccess()) {
                    logger.error("Failed to write message", future.cause());
                    waitingForAuth.set(false);
                }
            });
            queue.add(new QueuedMessage(ctx, msg, promise));
            return;
        }
        msg = msg + "token:" + credentials.getToken() + ";";
        super.write(ctx, msg, promise);
    }

    @Override
    public void channelRead(ChannelHandlerContext ctx, Object msg) throws Exception {
        if (msg instanceof Response response) {
            if (response.getCode().equals(MessageCode.AUTHENTICATION_SUCCESS)){
                String token = response.getParameter("token");
                if (token != null) {
                    credentials.setToken(token);
                    waitingForAuth.set(false);
                    logger.trace("Processing queued requests");
                    while (!queue.isEmpty()) {
                        QueuedMessage queuedMessage = queue.poll();
                        queuedMessage.sendMessage();
                    }
                } else {
                    logger.warn("No token provided with response from server: {}", ctx.channel().remoteAddress());
                }
            } else if (response.getCode().equals(MessageCode.AUTHENTICATION_FAILURE)){
                queue = new ArrayDeque<>();
                throw new CredentialException(credentials);
            } else if (response.getCode().equals(MessageCode.INVALID_TOKEN)){
                logger.warn("Invalid token provided. Token set to null. Authentication retrying...");
                credentials.setToken(null);
                ctx.channel().writeAndFlush(messageCreator.createRequest(Operation.AUTHENTICATE,
                        Map.of("login", credentials.getLogin(), "password", credentials.getPassword())));
            } else {
                ctx.fireChannelRead(msg);
            }
        } else {
            throw new CorruptedFrameException("Received unexpected response from decoder");
        }
    }
}
