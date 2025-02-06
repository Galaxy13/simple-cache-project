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
                .onResponse((resp) -> {
                    if (resp.getCode() == MessageCode.OK) {
                        logger.info("Response received");
                    }
                }).execute();

        storage.put("test", "45")
                .onResponse((resp) -> {
                    if (resp.getCode() == MessageCode.OK) {
                        logger.info("OK");
                    }
                }).execute();

        var putFuture = storage.get("subTest")
                .onResponse((response -> {
                    if (response.getCode().equals(MessageCode.OK)){
                        logger.info(response.getParameter("value"));
                    }
                }))
                .onError((error -> logger.error(error.getMessage())));
        putFuture.execute();
    }
}
