package com.galaxy13.processor.storage;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Map;
import java.util.Optional;

public class GetProcessor extends AbstractStorageProcessor implements StorageProcessor {
    public GetProcessor(Storage storage, MessageCreator creator) {
        super(storage, creator);
    }

    @Override
    public Optional<Value> process(Channel channel,  Map<String, String> fields) {
        String key = fields.get("key");
        if (key != null) {
            Optional<Value> value = storage.get(key);
            if (value.isPresent()) {
                Value v = value.get();
                return Optional.of(v);
            } else {
                return Optional.empty();
            }
        }
        return Optional.empty();
    }

    @Override
    public boolean isModifying() {
        return true;
    }
}
