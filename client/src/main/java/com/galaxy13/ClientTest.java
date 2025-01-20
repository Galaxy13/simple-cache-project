package com.galaxy13;

import com.galaxy13.network.message.MessageCreatorImpl;
import com.galaxy13.storage.AsyncStorageClient;
import com.galaxy13.storage.BlockingStorageClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.Thread.sleep;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args){
        var messageCreator = new MessageCreatorImpl(";", ":");
        var storage = new BlockingStorageClient(8081, "localhost", messageCreator);

        logger.info(storage.put("key1", "value1"));
        logger.info(storage.put("key2", "value2"));

        logger.info(storage.get("key1"));
        logger.info(storage.get("key2"));

        logger.info(storage.get("key3"));
    }
}
