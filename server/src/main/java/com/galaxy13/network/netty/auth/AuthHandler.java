package com.galaxy13.network.netty.auth;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.network.message.response.Response;
import com.galaxy13.storage.Value;
import io.netty.channel.ChannelFutureListener;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.security.SecureRandom;
import java.util.Base64;

public class AuthHandler extends SimpleChannelInboundHandler<CacheMessage> {
    private static final Logger logger = LoggerFactory.getLogger(AuthHandler.class);

    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private final Credentials credentials;

    public AuthHandler(Credentials credentials) {
        this.credentials = credentials;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CacheMessage msg) {
        logger.info("Received a cache message to Auth Handler");
        if (msg.getOperation().equals(Operation.AUTHENTICATE)){
            logger.info("Authentication request received from {}", ctx.channel().remoteAddress());
            handleAuthorization(ctx, msg);
        } else {
            logger.trace("{} accepted {} from {}",
                    AuthHandler.class.getSimpleName(),
                    msg.getClass().getSimpleName(),
                    ctx.channel().remoteAddress());
            handleTokenCheck(ctx, msg);
        }
    }

    private String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private void handleAuthorization(ChannelHandlerContext ctx, CacheMessage msg) {
        logger.trace("Authorization handling started for message: {}", msg.toString());
        String login = msg.getParameter("login");
        String password = msg.getParameter("password");
        MessageCode responseCode;
        String token = "";
        if (credentials.checkCredentials(login, password)) {
            logger.trace("Authentication successful for {} with password: {}", login, password);
            token = generateNewToken();
            credentials.addToken(token);
            responseCode = MessageCode.AUTHENTICATION_SUCCESS;
        } else {
            logger.trace("Authentication failed for {} with password: {}", login, password);
            responseCode = MessageCode.AUTHENTICATION_FAILURE;
        }
        Response<Value> response = CacheResponse.createAuthResponse(responseCode, token);
        ctx.writeAndFlush(response);
    }

    private void handleTokenCheck(ChannelHandlerContext ctx, CacheMessage msg) {
        logger.trace("Token check handling started for message: {}", msg.toString());
        String token = msg.getParameter("token");
        if (token != null && credentials.containsToken(token)) {
            logger.trace("Token check successful for {} with token: {}", ctx.channel().remoteAddress(), token);
            ctx.fireChannelRead(msg);
        } else {
            logger.trace("Token check failed for {} with token: {}", ctx.channel().remoteAddress(), token);
            Response<Value> response = CacheResponse.createAuthResponse(MessageCode.INVALID_TOKEN, null);
            ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
        }
    }
}
