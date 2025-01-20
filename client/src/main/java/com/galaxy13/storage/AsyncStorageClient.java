package com.galaxy13.storage;

import com.galaxy13.network.netty.NettyClient;
import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResourceSupplier;
import com.galaxy13.storage.action.ResponseAction;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringJoiner;

public class AsyncStorageClient{
    private static final Logger logger = LoggerFactory.getLogger(AsyncStorageClient.class);

    private final NetworkStorageClient networkStorageClient;

    public AsyncStorageClient(int port, String host) {
        this.networkStorageClient = new NettyClient(port, host);
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

    public <T> ClientFuture put(String key, T value) {
        StringJoiner basicMessage = createBasicMessage(Operation.PUT, key);
        basicMessage.add(formField("value_type", value.getClass().getSimpleName()));
        basicMessage.add(formField("value", value.toString()));
        return new ClientFuture(basicMessage + ";");
    }

    public ClientFuture get(String key) {
        StringJoiner basicMessage = createBasicMessage(Operation.GET, key);
        return new ClientFuture(basicMessage + ";");
    }

    public <T> ClientFuture putIfAbsent(String key, T value) {
        return computeIfAbsent(key, () -> value);
    }

    public <T> ClientFuture computeIfAbsent(String key, ResourceSupplier<T> supplier) {
        ClientFuture getFuture = this.get(key);
        ClientFuture putFuture = this.put(key, supplier.get());
        getFuture.onResponse(
                response -> {
                    if (response.getCode().equals(MessageCode.NOT_PRESENTED)) {
                        putFuture.execute();
                        logger.info("Put future executed");
                    }
                }
        ).onError(error -> logger.error("Error occurred while executing GET operation in putIfAbsent", error));
        getFuture.setChildFuture(putFuture);
        return getFuture;
    }

    public ClientFuture subscribeOn(String key) {
        StringJoiner basicMessage = createBasicMessage(Operation.SUBSCRIBE, key);
        return new ClientFuture(basicMessage + ";");
    }

    public <T> ClientFuture putAndSubscribe(String key, T value) {
        return null;
    }


    private StringJoiner createBasicMessage(Operation operation, String key) {
        StringJoiner sj = new StringJoiner(";");
        sj.add(formField("op", operation.toString()));
        sj.add(formField("key", key));
        return sj;
    }

    private String formField(String field, String value) {
        return field + ":" + value;
    }
}
