package com.galaxy13.network.message;

import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResponseAction;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientMessageHandler extends SimpleChannelInboundHandler<String> {
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageHandler.class);

    private final ResponseAction responseAction;
    private final ErrorAction errorAction;

    public ClientMessageHandler(ResponseAction respAction, ErrorAction errAction) {
        this.responseAction = respAction;
        this.errorAction = errAction;
    }

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        super.channelActive(ctx);
    }

    @Override
    protected void channelRead0(ChannelHandlerContext ctx, String msg) {
        logger.info("Received message: {}", msg);
        Map<String, String> msgMap = getValuesFromMsg(msg);
        Response response;
        try {
            response = Response.readFromMsg(msgMap);
        } catch (IllegalArgumentException e){
            logger.error("Response parsing error: {}", e.getMessage());
            return;
        }
        responseAction.action(response);
        ctx.channel().close();
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        logger.error("Exception caught", cause);
        errorAction.execute((Exception) cause);
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
