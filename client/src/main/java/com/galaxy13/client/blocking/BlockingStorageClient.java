package com.galaxy13.client.blocking;


import com.galaxy13.client.blocking.exception.CredentialException;
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

    private static final String KEY_FIELD_NAME = "key";
    private static final String VALUE_FIELD_NAME = "value";
    private static final String TOKEN_FIELD_NAME = "token";
    private static final String LOGIN_FIELD_NAME = "login";
    private static final String PASSWORD_FIELD_NAME = "password";

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
                Map.of(KEY_FIELD_NAME, key, VALUE_FIELD_NAME, value, TOKEN_FIELD_NAME, credentials.getToken()));
        return handleResponse(msg);
    }

    public String get(String key) {
        checkToken();
        String msg = messageCreator.createRequest(Operation.GET,
                Map.of(KEY_FIELD_NAME, key, TOKEN_FIELD_NAME, credentials.getToken()));
        return handleResponse(msg);
    }

    private String handleResponse(String msg) {
        try {
            Response response = blockingClient.sendMessage(msg);
            if (response.getCode().equals(MessageCode.OK)){
                return response.getParameter(VALUE_FIELD_NAME);
            } else {
                return null;
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return null;
    }

    private void checkToken() {
        if (!credentials.containsToken()) {
            boolean authResult = authenticate();
            if (!authResult) {
                throw new CredentialException(credentials);
            }
        }
    }

    private boolean authenticate() {
        String authMsg = messageCreator.createRequest(Operation.AUTHENTICATE,
                Map.of(LOGIN_FIELD_NAME, credentials.getLogin(), PASSWORD_FIELD_NAME, credentials.getPassword()));
        try {
            Response authResponse = blockingClient.sendMessage(authMsg);
            if (authResponse.getCode().equals(MessageCode.AUTHENTICATION_SUCCESS)) {
                String token = authResponse.getParameter(TOKEN_FIELD_NAME);
                credentials.setToken(token);
                return true;
            } else {
                return false;
            }
        } catch (InterruptedException e) {
            logger.error(e.getMessage(), e);
            Thread.currentThread().interrupt();
        }
        return false;
    }
}
