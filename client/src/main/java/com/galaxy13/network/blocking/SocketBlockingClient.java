package com.galaxy13.network.blocking;

import com.galaxy13.network.message.Response;
import com.galaxy13.network.blocking.handler.MessageBlockingHandler;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketBlockingClient implements BlockingClient {

    private final int port;
    private final String host;
    private final MessageBlockingHandler handler;

    public SocketBlockingClient(int port, String host, MessageBlockingHandler messageHandler){
        this.port = port;
        this.host = host;
        this.handler = messageHandler;
    }

    @Override
    public Response sendMessage(String message) {
        try (Socket socket = new Socket(host, port);
             BufferedOutputStream outStream = new BufferedOutputStream(socket.getOutputStream());
             BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream())) {

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            outStream.write(messageBytes);
            outStream.flush();

            return handleResponseBytes(inStream);
        } catch (IOException e) {
            handler.exceptionCaught(e);
            return null;
        }
    }

    private Response handleResponseBytes(InputStream inputStream) throws IOException {
        byte[] responseBuffer = new byte[1024];
        int bytesRead = inputStream.read(responseBuffer);
        if (bytesRead != -1) {
            String response = new String(responseBuffer, 0, bytesRead, StandardCharsets.UTF_8);
            return handler.handle(response);
        } else {
            throw new IOException("No response received from server.");
        }
    }
}
