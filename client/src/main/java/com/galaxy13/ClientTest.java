package com.galaxy13;

import com.galaxy13.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) throws InterruptedException {
        var storage = new Storage(8081, "localhost");
        storage.subscribeOn("test")
                .onResponse(response -> logger.info(response.toString()))
                .execute();

        sleep(1000);

        storage.computeIfAbsent("test", 5).execute();

        sleep(1000);

        storage.put("test", 12345)
                .onResponse(response -> {})
                .execute();
    }
}
