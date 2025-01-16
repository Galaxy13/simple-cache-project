package com.galaxy13;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Storage implements StorageClient{
    private static Logger logger = LoggerFactory.getLogger(Storage.class);

    private final int port;
    private final String host;

    public Storage(int port, String host) {
        this.port = port;
        this.host = host;
        logger.info("Storage client created");
    }

    @Override
    public <T> T put(String key, T value) {
        return null;
    }

    @Override
    public <T> T get(String key) {
        return null;
    }
}
