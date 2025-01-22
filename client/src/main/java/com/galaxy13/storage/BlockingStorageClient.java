package com.galaxy13.storage;


import com.galaxy13.network.blocking.TCPBlockingClient;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.message.handler.blocking.ClientMessageBlockingHandler;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.util.Map;

public class BlockingStorageClient {
    private static final Logger logger = LoggerFactory.getLogger(BlockingStorageClient.class);

    private final TCPBlockingClient blockingClient;
    private final MessageCreator messageCreator;

    public BlockingStorageClient(int port, String host) {
        this.blockingClient = new TCPBlockingClient(port, host, new ClientMessageBlockingHandler());
        this.messageCreator = new MessageCreatorImpl(";", ":");
    }

    public String put(String key, String value) {
        String msg = messageCreator.createRequest(Operation.PUT, Map.of("key", key, "value", value));
        return handleResponse(msg);
    }

    public String get(String key) {
        String msg = messageCreator.createRequest(Operation.GET, Map.of("key", key));
        return handleResponse(msg);
    }

    private String handleResponse(String msg) {
        try {
            Response response = blockingClient.sendMessage(msg);
            if (response.getCode().equals(MessageCode.OK) && response.getValue().isPresent()){
                return response.getValue().get();
            } else {
                return null;
            }
        } catch (IOException e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }
}
