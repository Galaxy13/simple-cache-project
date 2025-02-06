package com.galaxy13.storage;

import com.galaxy13.client.async.AsyncStorageClient;
import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;
import com.galaxy13.client.blocking.BlockingStorageClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class CacheComponent {
    private final AsyncStorageClient storageClient;
    private final BlockingStorageClient blockingClient;

    public CacheComponent(@Value("${storage.port}") int port,
                          @Value("${storage.host}") String host,
                          @Value("${storage.username}") String username,
                          @Value("${storage.password}") String password) {
        this.storageClient = new AsyncStorageClient(port, host, username, password);
        this.blockingClient = new BlockingStorageClient(port, host, username, password);
    }

    public String get(String key) {
        return blockingClient.get(key);
    }

    public String put(String key, String value) {
        return blockingClient.put(key, value);
    }

    public void subscribe(String key, ResponseAction responseAction, ErrorAction errorAction) {
        storageClient.subscribeOn(key)
                .onResponse(responseAction)
                .onError(errorAction)
                .execute();
    }
}
