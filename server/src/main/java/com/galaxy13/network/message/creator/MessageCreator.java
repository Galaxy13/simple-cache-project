package com.galaxy13.network.message.creator;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.response.Response;

import java.util.Map;

public class MessageCreator<K, V> {

    private final String fieldDelimiter;
    private final String equalSign;

    public MessageCreator(String fieldDelimiter, String equalSign) {
        if ((fieldDelimiter == null || equalSign == null) || (fieldDelimiter.isEmpty() || equalSign.isEmpty())) {
            throw new IllegalArgumentException("Field delimiter sign or equal sign could not be empty or null");
        }
        this.fieldDelimiter = fieldDelimiter;
        this.equalSign = equalSign;
    }

    public String fromResponse(Response<K, V> response) {
        MessageCode code = response.messageCode();
        Map<K, V> parameters = response.getParameters();

        StringBuilder sb = new StringBuilder();
        sb.append("code").append(equalSign).append(code.code()).append(fieldDelimiter);
        if (parameters != null) {
            for (Map.Entry<K, V> entry : parameters.entrySet()) {
                sb.append(createFieldValue(entry.getKey(), entry.getValue()));
            }
        }
        return sb.toString();
    }

    private String createFieldValue(K fieldName, V value) {
        return fieldName.toString() + equalSign + value.toString() + fieldDelimiter;
    }
}
