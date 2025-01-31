package com.galaxy13;

import com.galaxy13.client.async.AsyncStorageClient;
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
    }
}
