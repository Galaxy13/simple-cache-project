package com.galaxy13;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.NettyServer;
import com.galaxy13.network.netty.StorageServer;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.ProcessorControllerImpl;
import com.galaxy13.storage.LRUStorage;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Optional;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);
    private static final int DEFAULT_CAPACITY = 1024;

    public static void main(String[] args) throws InterruptedException, IllegalArgumentException {
        int port = Integer.parseInt(Optional.ofNullable(System.getenv("PORT"))
                .orElseThrow(() -> new IllegalArgumentException("No env")));
        logger.info("Starting server on port {} ...", port);

        int capacity = Integer.parseInt(Optional.ofNullable(System.getenv("CAPACITY"))
        .orElse(String.valueOf(DEFAULT_CAPACITY)));
        Storage<String, Value> storage = new LRUStorage<>(capacity);
        logger.info("Created storage with capacity: {}", capacity);

        MessageCreator<String, String> messageCreator = new MessageCreator<>(";", ":");

        Optional<String> login = Optional.ofNullable(System.getenv("LOGIN"));
        Optional<String> password = Optional.ofNullable(System.getenv("PASSWORD"));

        Credentials credentials;
        if (login.isPresent() && password.isPresent()) {
            credentials = new Credentials(login.get(), password.get());
        } else {
            logger.warn("User and/or password not provided. Credentials will be ignored.");
            credentials = new Credentials("", "");
        }

        ProcessorController processorController = new ProcessorControllerImpl(storage);
        StorageServer server = new NettyServer(port, processorController, messageCreator, credentials);
        server.start();
    }
}