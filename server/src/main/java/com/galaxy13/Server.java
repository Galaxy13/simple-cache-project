package com.galaxy13;

import com.galaxy13.network.netty.StorageServer;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import com.galaxy13.network.message.handler.MessageHandler;
import com.galaxy13.network.message.handler.MessageHandlerImpl;
import com.galaxy13.network.netty.NettyServer;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.ProcessorControllerImpl;
import com.galaxy13.storage.LRUStorage;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.StorageImpl;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        Storage<String> storage = new LRUStorage<>(1000);
        MessageCreator messageCreator = new MessageCreatorImpl(";", ":");
        ProcessorController processorController = new ProcessorControllerImpl(storage, messageCreator);
        MessageHandler handler = new MessageHandlerImpl(processorController);
        StorageServer server = new NettyServer(8081, handler);
        server.start();
    }
}