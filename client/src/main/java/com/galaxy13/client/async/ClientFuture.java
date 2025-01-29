package com.galaxy13.client.async;

import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientFuture{
    private static final Logger logger = LoggerFactory.getLogger(ClientFuture.class);

    private final String message;
    private final NetworkStorageClient networkClient;

    private ResponseAction responseAction;
    private ErrorAction errorAction = (e) -> logger.error("Error occurred while executing future", e);
    private boolean isErrorActionDefault = true;
    private ClientFuture childFuture;

    public ClientFuture(String message, NetworkStorageClient networkClient) {
        this.message = message;
        this.networkClient = networkClient;
    }

    public ClientFuture onResponse(ResponseAction responseAction) {
        if (childFuture != null) {
            childFuture.onResponse(responseAction);
            return this;
        }
        this.responseAction = responseAction;
        return this;
    }

    public void setChildFuture(ClientFuture childFuture) {
        this.childFuture = childFuture;
    }

    public ClientFuture onError(ErrorAction errorAction) {
        if (childFuture != null) {
            childFuture.onError(errorAction);
            return this;
        }
        this.errorAction = errorAction;
        this.isErrorActionDefault = false;
        return this;
    }

    public void execute(){
        if (responseAction == null) {
            logger.warn("Action for response not set. Result ignored");
            return;
        }
        if (isErrorActionDefault) {
            logger.warn("Action for error not set. Using default error (log) handler");
        }
        try {
            networkClient.sendMessage(message, responseAction, errorAction);
        } catch (InterruptedException e) {
            logger.warn("Interrupted while sending message", e);
        }
    }
}
