package com.galaxy13.network.message;

import com.galaxy13.storage.Operation;

import java.util.Map;

public interface MessageCreator {
    String createRequest(Operation operation, Map<String, String> headers);
}
