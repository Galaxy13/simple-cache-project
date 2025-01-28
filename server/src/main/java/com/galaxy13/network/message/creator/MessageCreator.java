package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.Value;

public interface MessageCreator {
    String createResponse(MessageCode code, Value value);

    String createResponse(MessageCode messageCode);
}
