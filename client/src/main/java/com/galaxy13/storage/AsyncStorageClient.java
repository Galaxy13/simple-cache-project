package com.galaxy13.storage;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.NettyClient;
import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResourceSupplier;
import com.galaxy13.storage.action.ResponseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class AsyncStorageClient{
    private static final Logger logger = LoggerFactory.getLogger(AsyncStorageClient.class);

    private final NetworkStorageClient networkStorageClient;
    private final MessageCreator messageCreator;

    public AsyncStorageClient(int port, String host) {
        this.networkStorageClient = new NettyClient(port, host);
        this.messageCreator = new MessageCreatorImpl(";", ":");
        logger.info("Storage client created");
    }

    public class ClientFuture{
        private ResponseAction responseAction;
        private ErrorAction errorAction = (e) -> logger.error("Error occurred while executing future", e);
        private boolean isErrorActionDefault = true;
        private final String message;
        private ClientFuture childFuture;

        public ClientFuture(String message) {
            this.message = message;
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
                networkStorageClient.sendMessage(message, responseAction, errorAction);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while sending message", e);
            }
        }
    }

    public ClientFuture put(String key, String value) {
        String putMsg = messageCreator.createRequest(Operation.PUT, Map.of("key", key, "value", value));
        return new ClientFuture(putMsg);
    }

    public ClientFuture get(String key) {
        String getMsg = messageCreator.createRequest(Operation.GET, Map.of("key", key));
        return new ClientFuture(getMsg);
    }

    public ClientFuture putIfAbsent(String key, String value) {
        return computeIfAbsent(key, () -> value);
    }

    public ClientFuture computeIfAbsent(String key, ResourceSupplier<String> supplier) {
        ClientFuture getFuture = this.get(key);
        ClientFuture putFuture = this.put(key, supplier.get());
        getFuture.onResponse(
                response -> {
                    if (response.getCode().equals(MessageCode.NOT_PRESENT)) {
                        putFuture.execute();
                        logger.info("Put future executed");
                    }
                }
        ).onError(error -> logger.error("Error occurred while executing GET operation in putIfAbsent", error));
        getFuture.setChildFuture(putFuture);
        return getFuture;
    }

    public ClientFuture subscribeOn(String key) {
        String subscribeMsg = messageCreator.createRequest(Operation.SUBSCRIBE, Map.of("key", key));
        return new ClientFuture(subscribeMsg);
    }

    public ClientFuture putAndSubscribe(String key, String value) {
        ClientFuture putFuture = this.put(key, value);
        ClientFuture subscribeFuture = this.subscribeOn(key);
        putFuture.onResponse(
                response -> {
                    if (response.getCode().equals(MessageCode.OK)) {
                        subscribeFuture.execute();
                    }
                }
        ).onError(error -> logger.error("Error occurred while executing putAndSubscribe operation", error));
        putFuture.setChildFuture(subscribeFuture);
        return putFuture;
    }
}
