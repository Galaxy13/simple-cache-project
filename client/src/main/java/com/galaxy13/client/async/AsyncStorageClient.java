package com.galaxy13.client.async;

import com.galaxy13.client.async.action.ResourceSupplier;
import com.galaxy13.network.NetworkStorageClient;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.netty.NettyClient;
import com.galaxy13.network.netty.auth.Credentials;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@SuppressWarnings("unused")
public class AsyncStorageClient{
    private static final Logger logger = LoggerFactory.getLogger(AsyncStorageClient.class);

    private final NetworkStorageClient networkStorageClient;
    private final MessageCreator messageCreator;

    public AsyncStorageClient(int port, String host, String username, String password, ExecutorService executor){
        this.messageCreator = new MessageCreatorImpl(";", ":");
        this.networkStorageClient = new NettyClient(port,
                host,
                executor,
                new Credentials(username, password),
                messageCreator);
    }

    public static AsyncStorageClient create(int port, String host) {
        logger.warn("Cache client created without login and password. Using default executor");
        return new AsyncStorageClient(port, host, "", "", Executors.newCachedThreadPool());
    }

    public static AsyncStorageClient createWithCredentials(int port, String host, String username, String password) {
        return new AsyncStorageClient(port, host, username, password, Executors.newCachedThreadPool());
    }

    public AsyncStorageClient(int port, String host, String username, String password) {
        this.messageCreator = new MessageCreatorImpl(";", ":");
        this.networkStorageClient = new NettyClient(port,
                host,
                Executors.newCachedThreadPool(),
                new Credentials(username, password),
                messageCreator);
        logger.info("Storage client created");
    }

    public AsyncStorageClient(int port, String host, ExecutorService executor) {
        logger.info("Storage client created with provided executor: {}", executor);
        this.messageCreator = new MessageCreatorImpl(";", ":");
        this.networkStorageClient = new NettyClient(port,
                host,
                executor,
                new Credentials("user", "pwd"),
                messageCreator);
    }

    public void shutdown() {
        try {
            if (networkStorageClient instanceof NettyClient nettyClient) {
                nettyClient.shutdown();
            }
        } catch (Exception e) {
            logger.error("Error closing network storage", e);
            Thread.currentThread().interrupt();
        }
    }

    public ClientFuture put(String key, String value) {
        String putMsg = messageCreator.createRequest(Operation.PUT, Map.of("key", key, "value", value));
        return new ClientFuture(putMsg, networkStorageClient);
    }

    public ClientFuture get(String key) {
        String getMsg = messageCreator.createRequest(Operation.GET, Map.of("key", key));
        return new ClientFuture(getMsg, networkStorageClient);
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
        return new ClientFuture(subscribeMsg, networkStorageClient);
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
