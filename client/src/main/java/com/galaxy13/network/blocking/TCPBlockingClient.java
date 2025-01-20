package com.galaxy13.network.blocking;

import com.galaxy13.network.message.Response;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.IOException;
import java.net.Socket;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.Map;
import java.util.stream.Collectors;

public class TCPBlockingClient{

    private final int port;
    private final String host;

    public TCPBlockingClient(int port, String host){
        this.port = port;
        this.host = host;
    }

    public Response sendMessage(String message) throws IOException {
        try (Socket socket = new Socket(host, port);
             BufferedOutputStream outStream = new BufferedOutputStream(socket.getOutputStream());
             BufferedInputStream inStream = new BufferedInputStream(socket.getInputStream())) {

            byte[] messageBytes = message.getBytes(StandardCharsets.UTF_8);
            outStream.write(messageBytes);
            outStream.flush();

            byte[] responseBuffer = new byte[1024];
            int bytesRead = inStream.read(responseBuffer);
            if (bytesRead != -1) {
                String response = new String(responseBuffer, 0, bytesRead, StandardCharsets.UTF_8);
                Map<String, String> fieldsMap = getValuesFromMsg(response);
                return Response.readFromMsg(fieldsMap);
            } else {
                throw new IOException("No response received from server.");
            }
        } catch (IOException e) {
            return null;
        }
    }

    private Map<String, String> getValuesFromMsg(String msg) {
        return Arrays.stream(msg.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }
}
