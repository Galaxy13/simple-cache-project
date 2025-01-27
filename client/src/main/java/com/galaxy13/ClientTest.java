package com.galaxy13;

import com.galaxy13.storage.AsyncStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args){
        var storage = new AsyncStorageClient(8081, "localhost");

        long startTime = System.currentTimeMillis();
        storage.put("key1", "value1")
                .onResponse(response -> {
                    if(response.getValue().isPresent()){
                        logger.info("PUT");
                    }
                })
                .execute();

            storage.get("key1")
                    .onResponse(response -> {
                        if(response.getValue().isPresent()){
                            logger.info("GET");
                        }
                    })
                    .execute();
        logger.info("Time taken: " + (System.currentTimeMillis() - startTime));
    }
}
