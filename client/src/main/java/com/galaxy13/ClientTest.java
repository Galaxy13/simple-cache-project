package com.galaxy13;

import com.galaxy13.storage.Storage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ClientTest {
    private static final Logger logger = LoggerFactory.getLogger(ClientTest.class);

    public static void main(String[] args){
        var storage = new Storage(8081, "localhost");
        storage.put("test", 1234, Integer.class)
                .then(logger::info).execute();
        storage.get("test")
                .then(logger::info).execute();
    }
}
