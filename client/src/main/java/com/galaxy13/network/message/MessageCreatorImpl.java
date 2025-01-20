package com.galaxy13.network.message;

import com.galaxy13.storage.Operation;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Map;
import java.util.StringJoiner;

public class MessageCreatorImpl implements MessageCreator {
    private static final Logger logger = LoggerFactory.getLogger(MessageCreatorImpl.class);

    private final String headerDelimiter;
    private final String equalSign;

    public MessageCreatorImpl(String headerDelimiter, String equalSign) {
        this.headerDelimiter = headerDelimiter;
        this.equalSign = equalSign;
    }

    @Override
    public String createRequest(Operation operation, Map<String, String> headers) throws IllegalArgumentException {
        String key = headers.get("key");
        if (key == null) {
            throw new IllegalArgumentException("Message format exception. Key can't be null for operation: " + operation);
        }
        StringJoiner joiner = createBasicMessage(operation, key);
        if (operation.equals(Operation.PUT)){
            String value = headers.get("value");
            if (value != null) {
                return put(joiner, value);
            }
            throw new IllegalArgumentException("Message format exception. Value can't be null for operation: " + operation);
        }
        return joiner + headerDelimiter;
    }

    private StringJoiner createBasicMessage(Operation operation, String key) {
        StringJoiner sj = new StringJoiner(this.headerDelimiter);
        sj.add(formField("op", operation.toString()));
        sj.add(formField("key", key));
        return sj;
    }

    private String formField(String field, String value) {
        return field + equalSign + value;
    }

    private String put(StringJoiner joiner, String value) {
        return joiner.add(formField("value", value)) + headerDelimiter;
    }
}
