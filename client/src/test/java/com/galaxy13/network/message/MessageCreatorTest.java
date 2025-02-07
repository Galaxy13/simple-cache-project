package com.galaxy13.network.message;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.creator.MessageCreatorImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class MessageCreatorTest {
    private MessageCreator messageCreator;

    @BeforeEach
    void setUp() {
        this.messageCreator = new MessageCreatorImpl(";", ":");
    }

    @Test
    void requestCreationTest(){
        String message1 = messageCreator.createRequest(Operation.GET, Map.of("key", "key1"));
        assertThat(message1).isNotNull().startsWith("op:GET;")
                .contains("key:key1;");

        String message2 = messageCreator.createRequest(Operation.PUT, Map.of("key", "key1", "value", "value1"));
        assertThat(message2).isNotNull().startsWith("op:PUT;")
                .contains("key:key1;")
                .contains("value:value1;");
    }
}
