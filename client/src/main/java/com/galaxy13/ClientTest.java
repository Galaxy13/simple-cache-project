package com.galaxy13;

import com.galaxy13.client.async.AsyncStorageClient;
import com.galaxy13.network.message.code.MessageCode;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private final static Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) {
        var storage = new AsyncStorageClient(8081, "localhost", "login", "password");

        var putFuture = storage.get("testKey")
                .onResponse((response -> {
                    if (response.getCode().equals(MessageCode.OK)){
                        logger.info(response.getParameter("value"));
                    }
                }))
                .onError((error -> {
                    logger.error(error.getMessage());
                }));
        putFuture.execute();
    }
}
