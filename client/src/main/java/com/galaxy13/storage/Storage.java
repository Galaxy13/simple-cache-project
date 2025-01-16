package com.galaxy13.storage;

import com.galaxy13.network.NettyClient;
import com.galaxy13.network.NetworkStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.StringJoiner;

public class Storage implements StorageClient {
    private static final Logger logger = LoggerFactory.getLogger(Storage.class);

    private final NetworkStorageClient networkStorageClient;

    public Storage(int port, String host) {
        this.networkStorageClient = new NettyClient(port, host);
        logger.info("Storage client created");
    }

    public class ClientFuture{
        private ResponseAction responseAction;
        private final String message;

        public ClientFuture(String message) {
            this.message = message;
        }

        public ClientFuture then(ResponseAction responseAction) {
            this.responseAction = responseAction;
            return this;
        }

        public void execute(){
            if (responseAction == null) {
                logger.warn("Action for response not set. Result ignored");
                return;
            }
            try {
                networkStorageClient.sendMessage(message, responseAction);
            } catch (InterruptedException e) {
                logger.warn("Interrupted while sending message", e);
            }
        }
    }

    @Override
    public <T> ClientFuture put(String key, T value, Class<T> clazz) {
        StringJoiner basicMessage = createBasicMessage(Operation.PUT, key);
        basicMessage.add(formField("value_type", clazz.getSimpleName()));
        basicMessage.add(formField("value", value.toString()));
        return new ClientFuture(basicMessage.toString());
    }

    @Override
    public ClientFuture get(String key) {
        StringJoiner basicMessage = createBasicMessage(Operation.GET, key);
        return new ClientFuture(basicMessage.toString());
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
