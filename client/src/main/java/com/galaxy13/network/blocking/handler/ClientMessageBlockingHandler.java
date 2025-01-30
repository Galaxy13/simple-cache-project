package com.galaxy13.network.blocking.handler;

import com.galaxy13.network.message.Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class ClientMessageBlockingHandler implements MessageBlockingHandler {
    private static final Logger logger = LoggerFactory.getLogger(ClientMessageBlockingHandler.class);

    @Override
    public Response handle(String message) {
        Map<String, String> fieldsMap = getValuesFromMsg(message);
        return Response.readFromMsg(fieldsMap);
    }

    @Override
    public void exceptionCaught(Throwable cause) {
        logger.error("Exception caught while handling message in blocking client: {}", cause.getMessage(), cause);
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
