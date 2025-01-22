package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.Value;

public class MessageCreatorImpl implements MessageCreator {

    private final String fieldDelimiter;
    private final String equalSign;

    public MessageCreatorImpl(String fieldDelimiter, String equalSign) {
        this.fieldDelimiter = fieldDelimiter;
        this.equalSign = equalSign;
    }

    @Override
    public String createResponse(Value value) {
        return createCodeMessage(MessageCode.OK) +
                createFieldValue("value", value.value());
    }

    @Override
    public String createCodeMessage(MessageCode messageCode) {
        return createFieldValue("code", messageCode.code());
    }

    private String createFieldValue(String fieldName, String value) {
        return fieldName + equalSign + value + fieldDelimiter;
    }

    @Override
    public String createSubscriptionResponse(String key, Value value) {
        return createCodeMessage(MessageCode.SUBSCRIPTION_RESPONSE) +
                createFieldValue("key", key) +
                createFieldValue("value", value.value());
    }
}
