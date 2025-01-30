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

import java.security.SecureRandom;
import java.util.Base64;

public class AuthHandler extends SimpleChannelInboundHandler<CacheMessage> {
    private static final SecureRandom secureRandom = new SecureRandom();
    private static final Base64.Encoder base64Encoder = Base64.getUrlEncoder();

    private final String username;
    private final String password;

    public AuthHandler(final String username, final String password) {
        this.username = username;
        this.password = password;
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, CacheMessage msg) throws Exception {
        if (msg.getOperation().equals(Operation.AUTHENTICATE)){
            handleAuthorization(ctx, msg);
        } else {
            String token = msg.getParameter("token");
        }
    }

    private String generateNewToken() {
        byte[] randomBytes = new byte[24];
        secureRandom.nextBytes(randomBytes);
        return base64Encoder.encodeToString(randomBytes);
    }

    private void handleAuthorization(ChannelHandlerContext ctx, CacheMessage msg) throws Exception {
        String login = msg.getParameter("login");
        String password = msg.getParameter("password");
        MessageCode responseCode;
        String token = "";
        if (this.username.equals(login) && this.password.equals(password)){
            token = generateNewToken();
            responseCode = MessageCode.AUTHENTICATION_SUCCESS;
        } else {
            responseCode = MessageCode.AUTHENTICATION_FAILURE;
        }
        Response<Value> response = CacheResponse.createAuthResponse(responseCode, token);
        ctx.writeAndFlush(response).addListener(ChannelFutureListener.CLOSE);
    }
}
