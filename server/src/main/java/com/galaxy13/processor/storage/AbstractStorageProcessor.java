package com.galaxy13.processor.storage;

import com.galaxy13.network.netty.decoder.CacheMessage;
import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.Channel;

import java.util.Optional;

public abstract class AbstractStorageProcessor implements StorageProcessor {
    protected final Storage storage;
    protected final MessageCreator messageCreator;

    protected AbstractStorageProcessor(Storage storage, MessageCreator creator) {
        this.storage = storage;
        this.messageCreator = creator;
    }

    @Override
    public Optional<Value> process(Channel channel, CacheMessage message) {
        return Optional.empty();
    }

    @Override
    public boolean isModifying() {
        return false;
    }
}
