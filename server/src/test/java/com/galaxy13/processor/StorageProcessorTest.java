package com.galaxy13.processor;

import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.processor.storage.GetProcessor;
import com.galaxy13.processor.storage.PutProcessor;
import com.galaxy13.processor.storage.StorageProcessor;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class StorageProcessorTest {
    private static Storage<String, Value> storage;
    private static StorageProcessor processor;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        storage = (Storage<String, Value>) Mockito.mock(Storage.class);
    }

    @SuppressWarnings("unchecked")
    @AfterEach
    void tearDown() {
        storage = (Storage<String, Value>) Mockito.mock(Storage.class);
    }

    @Test
    void getProcessor() {
        processor = new GetProcessor(storage);
        when(storage.get("key1")).thenReturn(Optional.of(new Value("value1")));
        assertThat(processor.isModifying()).isFalse();
        CacheMessage cacheMessage = new CacheMessage() {
            @Override
            public Operation getOperation() {
                return Operation.GET;
            }

            @Override
            public String getParameter(String key) {
                if ("key".equals(key)) {
                    return "key1";
                }
                return null;
            }
        };
        Optional<Value> result = processor.process(cacheMessage);
        assertThat(result).isPresent();
        assertThat(result.get().value()).isEqualTo("value1");

        assertThat(processor.process(new CacheMessage() {
            @Override
            public Operation getOperation() {
                return Operation.GET;
            }

            @Override
            public String getParameter(String key) {
                return null;
            }
        })).isEmpty();

        assertThat(processor.process(new CacheMessage() {
            @Override
            public Operation getOperation() {
                return Operation.GET;
            }

            @Override
            public String getParameter(String key) {
                return "key2";
            }
        })).isEmpty();
    }

    @Test
    void putProcessor() {
        processor = new PutProcessor(storage);
        Value testValue = new Value("value1");
        when(storage.put("key1", testValue)).thenReturn(testValue);

        assertThat(processor.isModifying()).isTrue();
        CacheMessage cacheMessage = new CacheMessage() {

            @Override
            public Operation getOperation() {
                return Operation.PUT;
            }

            @Override
            public String getParameter(String key) {
                if ("key".equals(key)) {
                    return "key1";
                } else if ("value".equals(key)) {
                    return "value1";
                }
                return null;
            }
        };
        Optional<Value> result = processor.process(cacheMessage);
        assertThat(result).isPresent();
        assertThat(result.get().value()).isEqualTo("value1");

        assertThat(processor.process(new CacheMessage() {
            @Override
            public Operation getOperation() {
                return Operation.PUT;
            }

            @Override
            public String getParameter(String key) {
                return null;
            }
        })).isEmpty();
    }
}
