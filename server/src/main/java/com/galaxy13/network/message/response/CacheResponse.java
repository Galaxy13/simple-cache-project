package com.galaxy13.network.message.response;

import com.galaxy13.network.message.MessageCode;

import java.util.HashMap;
import java.util.Map;

@SuppressWarnings({"unused"})
public class CacheResponse implements Response<String, String> {
    private final MessageCode code;
    private final Map<String, String> parameters;

    private CacheResponse(final MessageCode code) {
        if (code == null) {
            throw new IllegalArgumentException("Message code cannot be null");
        }
        this.code = code;
        this.parameters = new HashMap<>();
    }

    private CacheResponse(final MessageCode code, final Map<String, String> parameters) {
        if (code == null || parameters == null) {
            throw new IllegalArgumentException("Message code or parameters cannot be null");
        }
        this.code = code;
        this.parameters = parameters;
    }

    public static Response<String, String> create(final MessageCode code) {
        return new CacheResponse(code);
    }

    public static Response<String, String> createWithParams(final MessageCode code, Map<String, String> parameters) {
        return new CacheResponse(code, parameters);
    }

    public static Response<String, String> createFrom(final MessageCode code, String key1, String value1) {
        Map<String, String> parameters = Map.of(key1, value1);
        return new CacheResponse(code, parameters);
    }

    public static Response<String, String> createFrom(final MessageCode code, String key1, String value1, String key2, String value2) {
        Map<String, String> parameters = Map.of(key1, value1, key2, value2);
        return new CacheResponse(code, parameters);
    }

    public static Response<String, String> createFrom(final MessageCode code, String key1, String value1, String key2, String value2, String key3, String value3) {
        Map<String, String> parameters = Map.of(key1, value1, key2, value2, key3, value3);
        return new CacheResponse(code, parameters);
    }

    @Override
    public MessageCode messageCode() {
        return code;
    }

    @Override
    public String getParameter(String name) {
        return parameters.get(name);
    }

    @Override
    public Map<String, String> getParameters() {
        return parameters;
    }
}
