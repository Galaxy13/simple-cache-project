package com.galaxy13.network.message;

import com.galaxy13.network.message.code.MessageCode;

import java.util.Map;
import java.util.Optional;

public class Response {
    private final MessageCode code;
    private String valueType;
    private String value;

    private Response(Map<String, String> message) throws IllegalArgumentException {
        String code = message.get("code");
        if (code == null) {
            throw new IllegalArgumentException("Message does not contain response code");
        }
        this.code = MessageCode.fromString(code);
        if (this.code == null) {
            throw new IllegalArgumentException("Unsupported response code: " + code);
        }
        if (this.code.equals(MessageCode.OK)) {
            this.valueType = message.get("value_type");
            this.value = message.get("value");
        }
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

    @Override
    public String toString() {
        return "code: " + code + ";value_type: " + valueType + ";value: " + value + ";";
    }
}
