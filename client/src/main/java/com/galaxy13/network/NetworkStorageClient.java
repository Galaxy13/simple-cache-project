package com.galaxy13.network;


import com.galaxy13.storage.action.ErrorAction;
import com.galaxy13.storage.action.ResponseAction;

public interface NetworkStorageClient {
    void sendMessage(String message, ResponseAction respAction, ErrorAction errAction) throws InterruptedException;
}
