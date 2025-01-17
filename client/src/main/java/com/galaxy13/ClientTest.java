package com.galaxy13;

import com.galaxy13.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args) throws InterruptedException {
        var storage = new Storage(8081, "localhost");
        storage.putIfAbsent("test", 1234)
                .then(response -> logger.info(response.toString()))
                .execute();

        sleep(1000);

        storage.putIfAbsent("test", 12345)
                .then(response -> logger.info(response.toString()))
                .execute();

        sleep(1000);
        storage.get("test")
                .then(response -> {
                    if (response.getValue().isPresent()){
                        logger.info(response.getValue().get());
                    }
                })
                .execute();
    }
}
