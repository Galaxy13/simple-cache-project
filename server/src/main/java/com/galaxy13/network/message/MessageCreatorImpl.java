package com.galaxy13.network.message;

import com.galaxy13.network.MessageCode;
import com.galaxy13.storage.Value;

import java.util.StringJoiner;

public class MessageCreatorImpl implements MessageCreator {

    private final String fieldDelimiter;
    private final String equalSign;

    public MessageCreatorImpl(String fieldDelimiter, String equalSign) {
        this.fieldDelimiter = fieldDelimiter;
        this.equalSign = equalSign;
    }

    @Override
    public String createResponse(Value value) {
        StringJoiner joiner = new StringJoiner(fieldDelimiter);
        joiner.add(createCodeMessage(MessageCode.OK));
        joiner.add("value_type" + equalSign + value.type());
        joiner.add("value" + equalSign + value.value());
        return joiner + fieldDelimiter;
    }

    @Override
    public String createCodeMessage(MessageCode messageCode) {
        return "code" + equalSign + messageCode.code() + fieldDelimiter;
    }

    @Override
    public String createSubscriptionResponse(String key, Value value) {
        StringJoiner joiner = new StringJoiner(fieldDelimiter);
        joiner.add(createCodeMessage(MessageCode.SUBSCRIPTION_RESPONSE));
        joiner.add("key" + equalSign + key);
        joiner.add("value" + equalSign + value.value());
        return joiner + fieldDelimiter;
    }
}
