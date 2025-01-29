package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.storage.Value;

public class MessageCreator {

    private final String fieldDelimiter;
    private final String equalSign;

    public MessageCreator(String fieldDelimiter, String equalSign) {
        this.fieldDelimiter = fieldDelimiter;
        this.equalSign = equalSign;
    }

    public MessageBuilder builder(MessageCode messageCode) {
        return new MessageBuilder(messageCode);
    }

    public class MessageBuilder {
        private final MessageCode code;
        private Value value;
        private String key;

        public MessageBuilder(MessageCode code) {
            this.code = code;
        }

        public void setValue(Value value) {
            this.value = value;
        }

        public void setKey(String key) {
            this.key = key;
        }

        public String build() {
            StringBuilder result = new StringBuilder();
            result.append(createFieldValue("code", code.code()));
            if(value != null) {
                result.append(createFieldValue("value", value.value()));
            }
            if(key != null) {
                result.append(createFieldValue("key", key));
            }
            return result.toString();
        }
    }

    private String createFieldValue(String fieldName, String value) {
        return fieldName + equalSign + value + fieldDelimiter;
    }
}
