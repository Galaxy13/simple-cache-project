package com.galaxy13.processor.storage;

import com.galaxy13.network.netty.decoder.CacheMessage;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Optional;

public class PutProcessor extends AbstractStorageProcessor implements StorageProcessor {
    public PutProcessor(Storage storage, MessageCreator creator) {
        super(storage, creator);
    }

    @Override
    public Optional<Value> process(Channel channel, CacheMessage message) {
        String key = message.getParameter("key");
        String value = message.getParameter("value");
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
