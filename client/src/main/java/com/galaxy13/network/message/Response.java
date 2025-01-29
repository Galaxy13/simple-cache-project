package com.galaxy13.network.message;

import com.galaxy13.network.message.code.MessageCode;

import java.util.Map;
import java.util.Optional;

public class Response {
    private final MessageCode code;
    private final String value;
    private final String key;

    private Response(Map<String, String> message) throws IllegalArgumentException {
        String code = message.get("code");
        if (code == null) {
            throw new IllegalArgumentException("Message does not contain response code");
        }
        this.code = MessageCode.fromString(code);
        if (this.code == null) {
            throw new IllegalArgumentException("Unsupported response code: " + code);
        }
        this.value = message.get("value");
        this.key = message.get("key");
    }

    public static Response readFromMsg(Map<String, String> message) throws IllegalArgumentException {
        return new Response(message);
    }

    public MessageCode getCode() {
        return code;
    }

    public Optional<String> getValue() {
        return Optional.ofNullable(value);
    }

    public Optional<String> getKey() {
        return Optional.ofNullable(key);
    }

    @Override
    public String toString() {
        StringBuilder builder = new StringBuilder();
        builder.append("code: ").append(code);
        if(value != null) {
            builder.append(", value: ").append(value);
        }
        if(key != null) {
            builder.append(", key: ").append(key);
        }

        return builder.toString();
    }
}
