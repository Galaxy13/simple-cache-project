package com.galaxy13.processor;

import com.galaxy13.processor.storage.GetProcessor;
import com.galaxy13.processor.storage.MessageProcessor;
import com.galaxy13.processor.storage.PutProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProcessorControllerImpl implements ProcessorController {

    private final Map<String, MessageProcessor> processors = new HashMap<>(){{
        put("GET", new GetProcessor());
        put("PUT", new PutProcessor());
    }};

    @Override
    public Optional<MessageProcessor> getMessageProcessor(Map<String, String> messageFields) {
        String operation = messageFields.get("op");
        if (operation == null) {
            return Optional.empty();
        }
       MessageProcessor processor = processors.get(operation);
       if (processor == null) {
           return Optional.empty();
       }
       return Optional.of(processor);
    }
}
