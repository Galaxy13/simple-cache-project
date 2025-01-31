package com.galaxy13.network.message;

import com.galaxy13.network.message.code.MessageCode;

import java.util.Map;
import java.util.Optional;

public class Response {
    private final MessageCode code;
    private final Map<String, String> parameters;

    private Response(Map<String, String> parameters) throws IllegalArgumentException {
        String code = parameters.get("code");
        if (code == null) {
            throw new IllegalArgumentException("Message does not contain response code");
        }
        this.code = MessageCode.fromString(code);
        if (this.code == null) {
            throw new IllegalArgumentException("Unsupported response code: " + code);
        }
        this.parameters = parameters;
    }

    public static Response readFromMsg(Map<String, String> message) throws IllegalArgumentException {
        return new Response(message);
    }

    public MessageCode getCode() {
        return code;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("code: ").append(code);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        return builder.toString();
    }
}
