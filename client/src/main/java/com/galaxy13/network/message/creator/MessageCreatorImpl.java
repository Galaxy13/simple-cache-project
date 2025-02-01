package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;

public class MessageCreatorImpl implements MessageCreator {
    private static final Logger logger = LoggerFactory.getLogger(MessageCreatorImpl.class);

    private final String headerDelimiter;
    private final String equalSign;

    public MessageCreatorImpl(String headerDelimiter, String equalSign) {
        this.headerDelimiter = headerDelimiter;
        this.equalSign = equalSign;
    }

    @Override
    public String createRequest(Operation operation, Map<String, String> headers){
        logger.trace("Creating request message for operation {}", operation);
        StringBuilder builder = new StringBuilder();
        builder.append(formField("op", operation.name()));
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            builder.append(formField(entry.getKey(), entry.getValue()));
        }
        return builder.toString();
    }

    private String formField(String field, String value) {
        return field + equalSign + value + headerDelimiter;
    }
}
