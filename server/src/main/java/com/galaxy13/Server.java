package com.galaxy13;

import com.galaxy13.network.StorageServer;
import com.galaxy13.network.message.MessageCreator;
import com.galaxy13.network.message.MessageCreatorImpl;
import com.galaxy13.network.message.MessageHandler;
import com.galaxy13.network.message.MessageHandlerImpl;
import com.galaxy13.network.netty.NettyServer;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.ProcessorControllerImpl;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.StorageImpl;

public class Server {
    public static void main(String[] args) throws InterruptedException {
        Storage storage = new StorageImpl(100);
        ProcessorController processorController = new ProcessorControllerImpl();
        MessageCreator messageCreator = new MessageCreatorImpl(";", ":");
        MessageHandler handler = new MessageHandlerImpl(storage, processorController, messageCreator);
        StorageServer server = new NettyServer(8081, handler);
        server.start();
    }
}