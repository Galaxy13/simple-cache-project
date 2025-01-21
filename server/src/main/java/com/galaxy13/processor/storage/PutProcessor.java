package com.galaxy13.processor.storage;

import com.galaxy13.network.message.MessageCreator;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Optional;

public class PutProcessor extends AbstractStorageProcessor implements StorageProcessor {
    public PutProcessor(Storage storage, MessageCreator creator) {
        super(storage, creator);
    }

    @Override
    public Optional<Value> process(Channel channel, Map<String, String> fields) {
        String key = fields.get("key");
        String value = fields.get("value");
        if (key != null && value != null) {
            Value result = storage.put(key, new Value(value));
            return Optional.of(result);
        } else {
            return Optional.empty();
        }
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
