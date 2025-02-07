package com.galaxy13;

import com.galaxy13.client.async.AsyncStorageClient;

import com.galaxy13.network.message.code.MessageCode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicInteger;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) {
        var storage = new AsyncStorageClient(8081, "localhost", "test", "test");

        AtomicInteger success = new AtomicInteger();
        AtomicInteger failed = new AtomicInteger();

        long startTime = System.currentTimeMillis();

        for (int i = 0; i < 100_000; i++) {
            if (i % 10_000 == 0 && i != 0){
                logger.info("Completed operations: {}", i);
            }
            storage.put("key" + i, "value" + i)
                    .onResponse(response -> {
                        if (response.getCode() == MessageCode.OK) {
                            success.getAndIncrement();
                        } else {
                            failed.getAndIncrement();
                        }
                    })
                    .onError(error -> {
                        logger.error(error.getMessage());
                        failed.getAndIncrement();
                    }).execute();
        }
        storage.shutdown();

        long time = System.currentTimeMillis() - startTime;
        logger.info("Success: {}, Failed: {}, Time: {}", success, failed, time);
    }
}
