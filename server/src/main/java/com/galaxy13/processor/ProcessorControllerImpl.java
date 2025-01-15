package com.galaxy13.processor;

import com.galaxy13.processor.storage.GetProcessor;
import com.galaxy13.processor.storage.MessageProcessor;
import com.galaxy13.processor.storage.PutProcessor;

import java.util.HashMap;
import java.util.Map;
import java.util.Optional;

public class ProcessorControllerImpl<V> implements ProcessorController<V> {

    private final Map<String, MessageProcessor<V>> processors = new HashMap<>(){{
        put("GET", new GetProcessor<>());
        put("PUT", new PutProcessor<>());
    }};

    @Override
    public Optional<MessageProcessor<V>> getMessageProcessor(Map<String, String> messageFields) {
        String operation = messageFields.get("operation");
        if (operation == null) {
            return Optional.empty();
        }
       MessageProcessor<V> processor = processors.get(operation);
       if (processor == null) {
           return Optional.empty();
       }
       return Optional.of(processor);
    }
}
