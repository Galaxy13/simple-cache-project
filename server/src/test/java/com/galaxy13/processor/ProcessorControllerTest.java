package com.galaxy13.processor;

import com.galaxy13.network.message.MessageCode;
import com.galaxy13.network.message.Operation;
import com.galaxy13.network.message.request.CacheMessage;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.channel.embedded.EmbeddedChannel;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;

import java.util.Map;
import java.util.Optional;

import static org.mockito.Mockito.*;
import static org.assertj.core.api.Assertions.*;

class ProcessorControllerTest {
    private static ProcessorController controller;
    private static Storage<String, Value> storage;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        storage = (Storage<String, Value>) Mockito.mock(Storage.class);
        controller = new ProcessorControllerImpl(storage);
    }

    @Test
    void testProcessorControllerWithPutAndGetOperation() {
        String key = "key1";
        Value value = new Value("value1");
        when(storage.put(key, value)).thenReturn(value);
        when(storage.get(key)).thenReturn(Optional.of(value));

        CacheMessage putMessage = new CacheMessage() {
            private final Map<String, String> map = Map.of("key", key, "value", value.value());

            @Override
            public Operation getOperation() {
                return Operation.PUT;
            }

            @Override
            public String getParameter(String key) {
                return map.get(key);
            }
        };

        CacheMessage getMessage = new CacheMessage() {
            private final Map<String, String> map = Map.of("key", key);
            @Override
            public Operation getOperation() {
                return Operation.GET;
            }

            @Override
            public String getParameter(String key) {
                return map.get(key);
            }
        };

        EmbeddedChannel putChannel = createChannel();

        controller.processMessage(putMessage, putChannel);
        verify(storage, times(1)).put(key, value);
        CacheResponse putResponse = (CacheResponse) putChannel.outboundMessages().poll();

        assertThat(putResponse).isNotNull();
        assertThat(putResponse.messageCode()).isEqualTo(MessageCode.OK);
        assertThat(putResponse.getParameter("key")).isEqualTo(key);
        assertThat(putResponse.getParameter("value")).isEqualTo(value.value());

        EmbeddedChannel getChannel = createChannel();

        controller.processMessage(getMessage, getChannel);
        verify(storage, times(1)).put(key, value);
        verify(storage, times(1)).get(key);

        CacheResponse getResponse = (CacheResponse) getChannel.outboundMessages().poll();
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.messageCode()).isEqualTo(MessageCode.OK);
        assertThat(getResponse.getParameter("key")).isEqualTo(key);
        assertThat(getResponse.getParameter("value")).isEqualTo(value.value());
    }

    @Test
    void testProcessorControllerWithNullAndEmptyFields() {
        CacheMessage putMessage = new CacheMessage() {

            @Override
            public Operation getOperation() {
                return Operation.PUT;
            }

            @Override
            public String getParameter(String key) {
                return null;
            }
        };

        CacheMessage getMessage = new CacheMessage() {
            @Override
            public Operation getOperation() {
                return Operation.GET;
            }

            @Override
            public String getParameter(String key) {
                return "";
            }
        };
        EmbeddedChannel putChannel = createChannel();
        controller.processMessage(putMessage, putChannel);

        verifyNoInteractions(storage);
        CacheResponse putResponse = (CacheResponse) putChannel.outboundMessages().poll();
        assertThat(putResponse).isNotNull();
        assertThat(putResponse.messageCode()).isEqualTo(MessageCode.NOT_PRESENT);

        EmbeddedChannel getChannel = createChannel();
        controller.processMessage(getMessage, getChannel);

        verifyNoInteractions(storage);
        CacheResponse getResponse = (CacheResponse) getChannel.outboundMessages().poll();
        assertThat(getResponse).isNotNull();
        assertThat(getResponse.messageCode()).isEqualTo(MessageCode.NOT_PRESENT);
    }

    private EmbeddedChannel createChannel() {
        return new EmbeddedChannel();
    }
}
