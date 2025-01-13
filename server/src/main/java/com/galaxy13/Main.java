package com.galaxy13;

import com.galaxy13.network.StorageServer;
import com.galaxy13.network.netty.NettyServer;

public class Main {
    public static void main(String[] args) throws InterruptedException {
        StorageServer server = new NettyServer(8081);
        server.start();
    }
}