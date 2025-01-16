package com.galaxy13.network.message;

import com.galaxy13.network.MessageCode;
import com.galaxy13.processor.storage.MessageProcessor;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

public class MessageHandlerImpl implements MessageHandler{
    private final Logger logger = LoggerFactory.getLogger(MessageHandlerImpl.class);

    private final Storage storage;
    private final ProcessorController controller;

    public MessageHandlerImpl(Storage storage, ProcessorController controller) {
        this.storage = storage;
        this.controller = controller;
    }

    @Override
    public String handleMessage(String message) {
        Map<String, String> fields = getFields(message);
        Optional<MessageProcessor> messageProcessor = controller.getMessageProcessor(fields);
        if (messageProcessor.isPresent()) {
            var processor = messageProcessor.get();
            var result = processor.process(storage, fields);
            if (result.isPresent()){
                return createMessage(result.get());
            }
            return faultCode(MessageCode.NOT_PRESENT);
        }
        return faultCode(MessageCode.UNSUPPORTED_OPERATION);
    }

    private Optional<String> findField(String message, String fieldName) {
        Matcher matcher = Pattern.compile(String.format("(?<=%s:)[^;]+", fieldName)).matcher(message);
        if (matcher.find()) {
            return Optional.of(matcher.group());
        }
        return Optional.empty();
    }

    private Map<String, String> getFields(String message) {
        return Arrays.stream(message.split(";"))
                .map(s -> s.split(":"))
                .filter(parts -> parts.length == 2)
                .collect(Collectors.toMap(parts -> parts[0], parts -> parts[1].strip()));
    }

    private String createMessage(Value value){
        StringJoiner joiner = new StringJoiner(";");
        joiner.add("code:" + MessageCode.OK);
        joiner.add("value_type:" + value.type());
        joiner.add("value:" + value.value());
        return joiner.toString();
    }

    private String faultCode(MessageCode messageCode) {
        return "code:" + messageCode;
    }
}
