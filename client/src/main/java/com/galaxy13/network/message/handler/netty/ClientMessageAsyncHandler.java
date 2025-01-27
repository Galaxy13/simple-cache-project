package com.galaxy13.network.message.handler.netty;

import com.galaxy13.network.message.Response;
import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResponseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientMessageAsyncHandler implements MessageAsyncHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageAsyncHandler.class);

    private final ResponseAction responseAction;
    private final ErrorAction errorAction;

    public ClientMessageAsyncHandler(ResponseAction respAction, ErrorAction errAction) {
        this.responseAction = respAction;
        this.errorAction = errAction;
    }

    public void handleMessage(String msg) {
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
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        errorAction.execute((Exception) cause);
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
