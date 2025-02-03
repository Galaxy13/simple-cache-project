package com.galaxy13.network.message;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.message.response.CacheResponse;
import com.galaxy13.network.message.response.Response;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Map;

import static org.assertj.core.api.Assertions.*;

class MessageCreatorTest {
    private static MessageCreator<String, String> messageCreator;

    @BeforeAll
    static void setUp() {
        messageCreator = new MessageCreator<>(";", ":");
    }

    @Test
    void newInstanceAssertion() {
        assertThatThrownBy(() -> new MessageCreator<>("", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MessageCreator<>(":", ""))
                .isInstanceOf(IllegalArgumentException.class);
        assertThatThrownBy(() -> new MessageCreator<>("", "="))
                .isInstanceOf(IllegalArgumentException.class);
    }

    @Test
    void messageCreationTest() {
        Response<String, String> codeResponse = CacheResponse.create(MessageCode.OK);
        String message = messageCreator.fromResponse(codeResponse);
        assertThat(message).isNotNull().isNotEmpty().isEqualTo("code:200;");

        Response<String, String> messageResponse = CacheResponse.createFrom(MessageCode.FORMAT_EXCEPTION, "key", "testKey");
        assertThat(messageCreator.fromResponse(messageResponse)).isNotNull()
                .isNotEmpty()
                .isEqualTo("code:303;key:testKey;");

        Response<String, String> responseFromMap = CacheResponse.createWithParams(MessageCode.SUBSCRIPTION_RESPONSE, Map.of(
                "key", "testKey",
                "value", "testValue"
        ));
        assertThat(messageCreator.fromResponse(responseFromMap)).isNotNull()
                .isNotEmpty()
                .contains("code:201;")
                .contains("key:testKey;")
                .contains("value:testValue;");
    }
}
