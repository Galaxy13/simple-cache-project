package com.galaxy13.network;


import com.galaxy13.client.async.action.ErrorAction;
import com.galaxy13.client.async.action.ResponseAction;

public interface NetworkStorageClient{
    void sendMessage(String message, ResponseAction respAction, ErrorAction errAction) throws InterruptedException;
}
