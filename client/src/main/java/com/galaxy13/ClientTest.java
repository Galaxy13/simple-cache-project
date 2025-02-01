package com.galaxy13;

import com.galaxy13.client.async.AsyncStorageClient;
import com.galaxy13.client.blocking.BlockingStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) throws InterruptedException {
        var storage = AsyncStorageClient.start(8081, "localhost");
        storage.put("key1", "value1234")
                    .onResponse(response -> logger.info(response.getParameter("value")))
                    .execute();

        Thread.sleep(100);

        storage.subscribeOn("key1")
                        .onResponse(response -> logger.info("Subscribe answer OK. Value: {}", response.toString()))
                .execute();

        var blockingStorage = new BlockingStorageClient(8081, "localhost", "user", "pwd");

        logger.info("block rec: {}", blockingStorage.get("key1"));

        blockingStorage.put("key3", "Complex values");
        blockingStorage.put("key1", "new value");

        logger.info("block rec: {}", blockingStorage.get("key3"));
    }
}
