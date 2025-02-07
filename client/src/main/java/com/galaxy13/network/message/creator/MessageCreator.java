package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.Operation;

import java.util.Map;

public interface MessageCreator {
    String createRequest(Operation operation, Map<String, String> headers);
}
