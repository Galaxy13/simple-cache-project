package com.galaxy13.network.message.response;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.Value;

public class CacheResponse implements Response<Value> {
    private final MessageCode code;
    private Value value;
    private String key;
    private String token;

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

    private CacheResponse(final MessageCode code, final String token) {
        this.code = code;
        this.token = token;
    }

    public static CacheResponse create(final MessageCode code) {
        return new CacheResponse(code);
    }

    public static CacheResponse createWithValue(final MessageCode code, final Value value) {
        return new CacheResponse(code, value);
    }

    public static CacheResponse createAuthResponse(final MessageCode code, final String token) {
        return new CacheResponse(code, token);
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
    public String token() {
        return token;
    }

    @Override
    public String key() {
        return key;
    }
}
