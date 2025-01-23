package com.galaxy13;

import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.storage.AsyncStorageClient;
import com.galaxy13.storage.BlockingStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Objects;

import static java.lang.Thread.sleep;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args){
        var storage = new AsyncStorageClient(8081, "localhost");

        int numberOfRequests = 100_000;

//        long startPutTime = System.currentTimeMillis();
//        for (int i = 0; i < numberOfRequests; i++) {
//            String value = storage.put("key" + i, "value" + i);
//            if (value.equals("value" + i)) {
//                logger.error("Error in putting key " + i + ": " + value);
//            }
//        }
//        logger.info("Stored " + numberOfRequests + " keys in {} ms", System.currentTimeMillis() - startPutTime);

        long startGetTime = System.currentTimeMillis();
        storage.put("key1", "value1")
                .execute();
        for (int i = 0; i < numberOfRequests; i++) {
            storage.get("key1")
                    .onResponse((response) -> {})
                    .execute();
        }
        logger.info("Get " + numberOfRequests + " keys in {} ms", System.currentTimeMillis() - startGetTime);
    }
}
