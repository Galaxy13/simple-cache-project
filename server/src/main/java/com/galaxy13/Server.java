package com.galaxy13;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.NettyServer;
import com.galaxy13.network.netty.StorageServer;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.ProcessorControllerImpl;
import com.galaxy13.storage.LRUStorage;
import com.galaxy13.storage.Storage;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        Storage<String> storage = new LRUStorage<>(1000);
        MessageCreator<String, String> messageCreator = new MessageCreator<>(";", ":");
        Credentials credentials = new Credentials("user", "pwd");
        ProcessorController processorController = new ProcessorControllerImpl(storage);
        StorageServer server = new NettyServer(8081, processorController, messageCreator, credentials);
        server.start();
    }
}