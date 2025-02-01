package com.galaxy13.client.blocking;


import com.galaxy13.network.blocking.BlockingClient;
import com.galaxy13.network.blocking.SocketBlockingClient;
import com.galaxy13.network.blocking.handler.ClientMessageBlockingHandler;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

@SuppressWarnings({"unused"})
public class BlockingStorageClient {
    private static final Logger logger = LoggerFactory.getLogger(BlockingStorageClient.class);

    private final BlockingClient blockingClient;
    private final MessageCreator messageCreator;
    private final Credentials credentials;

    public BlockingStorageClient(int port, String host, String username, String password) {
        this.blockingClient = new SocketBlockingClient(port, host, new ClientMessageBlockingHandler());
        this.messageCreator = new MessageCreatorImpl(";", ":");
        this.credentials = new Credentials(username, password);
    }

    public String put(String key, String value) {
        checkToken();
        String msg = messageCreator.createRequest(Operation.PUT,
                Map.of("key", key, "value", value, "token", credentials.getToken()));
        return handleResponse(msg);
    }

    public String get(String key) {
        checkToken();
        String msg = messageCreator.createRequest(Operation.GET,
                Map.of("key", key, "token", credentials.getToken()));
        return handleResponse(msg);
    }

    private String handleResponse(String msg) {
        try {
            Response response = blockingClient.sendMessage(msg);
            if (response.getCode().equals(MessageCode.OK)){
                return response.getParameter("value");
            } else {
                return null;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return null;
        }
    }

    private void checkToken() {
        if (!credentials.containsToken()) {
            boolean authResult = authenticate();
            if (!authResult) {
                throw new RuntimeException(
                        String.format("Bad credentials provider: user: %s ; password: %s  Server rejected connection.",
                                credentials.getLogin(), credentials.getPassword()));
            }
        }
    }

    private boolean authenticate() {
        String authMsg = messageCreator.createRequest(Operation.AUTHENTICATE,
                Map.of("login", credentials.getLogin(), "password", credentials.getPassword()));
        try {
            Response authResponse = blockingClient.sendMessage(authMsg);
            if (authResponse.getCode().equals(MessageCode.AUTHENTICATION_SUCCESS)) {
                String token = authResponse.getParameter("token");
                credentials.setToken(token);
                return true;
            } else {
                return false;
            }
        } catch (Exception e) {
            logger.error(e.getMessage(), e);
            return false;
        }
    }
}
