package com.galaxy13.network.message;

import com.galaxy13.network.MessageCode;
import com.galaxy13.storage.Value;

public interface MessageCreator {
    String createResponse(Value value);

    String createCodeMessage(MessageCode messageCode);

    String createSubscriptionResponse(String key, Value value);
}
