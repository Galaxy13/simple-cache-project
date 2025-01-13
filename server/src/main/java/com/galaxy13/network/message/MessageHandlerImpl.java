package com.galaxy13.network.message;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class MessageHandlerImpl implements MessageHandler{
    @Override
    public void handleMessage(String message) {
        List<String> messageFields = List.of(message.split(";"));
    }

    private Optional<Map<String, String>> getFieldsAndValidate(String message) {
        Map<String, String> fieldsAndValidate = new HashMap<>();
        Optional<String> key = findField(message, "key");
        Optional<String> valueType = findField(message, "valueType");
        Optional<String> value = findField(message, "value");
        if (key.isPresent() && valueType.isPresent() && value.isPresent()) {

        }
    }

    private Optional<String> findField(String message, String fieldName) {
        Matcher matcher = Pattern.compile(String.format("(?<=%s:)[^;]+", fieldName)).matcher(message);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private boolean validateValue(String value, String valueType){

    }
}
