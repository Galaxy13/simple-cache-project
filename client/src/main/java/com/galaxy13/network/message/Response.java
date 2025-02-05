package com.galaxy13.network.message;

import com.galaxy13.network.message.code.MessageCode;

import java.util.Map;
import java.util.Objects;

public class Response {
    private final MessageCode messageCode;
    private final Map<String, String> parameters;

    private Response(Map<String, String> parameters) throws IllegalArgumentException {
        String code = parameters.get("code");
        if (code == null) {
            throw new IllegalArgumentException("Message does not contain response code");
        }
        this.messageCode = MessageCode.fromString(code);
        if (this.messageCode == null) {
            throw new IllegalArgumentException("Unsupported response code: " + code);
        }
        this.parameters = parameters;
    }

    public static Response readFromMsg(Map<String, String> message) throws IllegalArgumentException {
        return new Response(message);
    }

    public MessageCode getCode() {
        return messageCode;
    }

    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("code:").append(messageCode);
        for (Map.Entry<String, String> entry : parameters.entrySet()) {
            builder.append(entry.getKey()).append(":").append(entry.getValue()).append(";");
        }
        return builder.toString();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Response response = (Response) o;
        if (messageCode != response.messageCode) return false;
        return Objects.equals(parameters, response.parameters);
    }

    @Override
    public int hashCode() {
        int result = messageCode != null ? messageCode.hashCode() : 0;
        result = 31 * result + (parameters != null ? parameters.hashCode() : 0);
        return result;
    }
}
