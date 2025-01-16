package com.galaxy13.network;


import com.galaxy13.storage.ResponseAction;

public interface NetworkStorageClient {
    void sendMessage(String message, ResponseAction action) throws InterruptedException;
}
