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
import org.apache.commons.cli.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Server {
    private static final Logger logger = LoggerFactory.getLogger(Server.class);

    public static void main(String[] args) throws InterruptedException, ParseException {
        final Options options = new Options();
        options.addOption("h", "help", false, "Prints help message");
        options.addOption("p", "port", true, "Server listening port");
        options.addOption("l", "login", false, "Auth login (ignored if password is not provided)");
        options.addOption("k", "password", false, "Auth password (ignored if login is not provided)");
        options.addOption("c", "capacity", false, "LRU cache capacity (Default: 1000)");

        CommandLineParser parser = new DefaultParser();
        CommandLine cmd = parser.parse(options, args);
        start(cmd);
    }

    private static void start(CommandLine cmd) throws InterruptedException, ParseException {
        int port;
        if (cmd.hasOption("p")) {
            try {
                port = Integer.parseInt(cmd.getOptionValue("p"));
            } catch (NumberFormatException e) {
                throw new ParseException("Provided port is not an integer value. Server start aborted.");
            }
        } else {
            throw new ParseException("Port is not provided. Server start aborted.");
        }

        Storage<String, Value> storage;
        if (cmd.hasOption("c")) {
            try {
                final int capacity = Integer.parseInt(cmd.getOptionValue("p"));
                storage = new LRUStorage<>(capacity);
            } catch (NumberFormatException e) {
                throw new ParseException("Provided capacity is not an integer value. Server start aborted.");
            }
        } else {
            logger.info("LRU cache capacity is not provided. Using default 1000 unit capacity.");
            storage = new LRUStorage<>(1000);
        }

        MessageCreator<String, String> messageCreator = new MessageCreator<>(";", ":");

        Credentials credentials;
        if (cmd.hasOption("l") && cmd.hasOption("k")) {
            credentials = new Credentials(cmd.getOptionValue("l"), cmd.getOptionValue("k"));
        } else {
            logger.warn("User and/or password not provided. Credentials will be ignored.");
            credentials = new Credentials("", "");
        }

        ProcessorController processorController = new ProcessorControllerImpl(storage);
        StorageServer server = new NettyServer(port, processorController, messageCreator, credentials);
        server.start();
    }
}