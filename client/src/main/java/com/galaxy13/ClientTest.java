package com.galaxy13;

import com.galaxy13.client.async.AsyncStorageClient;
import com.galaxy13.network.message.code.MessageCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) {
        var storage = new AsyncStorageClient(8081, "localhost", "test", "test");

        storage.put("subTest", "1234")
                .onResponse(resp -> {
                    if (resp.getCode() == MessageCode.OK) {
                        logger.info("Response received: {}", resp);
                    }
                }).execute();

        storage.put("test", "45")
                .onResponse(resp -> {
                    if (resp.getCode() == MessageCode.OK) {
                        logger.info("Put response received: {}", resp);
                    }
                }).execute();

        var putFuture = storage.subscribeOn("subTest")
                .onResponse((response -> {
                    if (response.getCode().equals(MessageCode.SUBSCRIPTION_SUCCESS) || response.getCode().equals(MessageCode.SUBSCRIPTION_RESPONSE)){
                        logger.info("Subscription response received: {}", response);
                    }
                }))
                .onError((error -> logger.error(error.getMessage())));
        putFuture.execute();
    }
}
