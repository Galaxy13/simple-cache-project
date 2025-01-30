package com.galaxy13.network.message.response;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.Value;

public class CacheResponse implements Response<Value> {
    private final MessageCode code;
    private Value value;
    private String key;

    public CacheResponse(final MessageCode code) {
        this.code = code;
    }

    public CacheResponse(final MessageCode code, final Value value) {
        this.code = code;
        this.value = value;
    }

    public CacheResponse(final MessageCode code, final String key, final Value value) {
        this.code = code;
        this.value = value;
        this.key = key;
    }

    public static CacheResponse create(final MessageCode code) {
        return new CacheResponse(code);
    }

    public static CacheResponse createWithValue(final MessageCode code, final Value value) {
        return new CacheResponse(code, value);
    }

    @Override
    public MessageCode messageCode() {
        return code;
    }

    @Override
    public Value value() {
        return value;
    }

    @Override
    public String key() {
        return key;
    }
}
