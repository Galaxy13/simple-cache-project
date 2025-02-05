package com.galaxy13.network.server;

import com.galaxy13.network.message.creator.MessageCreator;
import com.galaxy13.network.netty.auth.AuthHandler;
import com.galaxy13.network.netty.auth.Credentials;
import com.galaxy13.network.netty.decoder.CacheMessageDecoder;
import com.galaxy13.network.netty.encoder.ResponseEncoder;
import com.galaxy13.network.netty.handler.BasicCacheProcessingHandler;
import com.galaxy13.processor.ProcessorController;
import com.galaxy13.processor.ProcessorControllerImpl;
import com.galaxy13.storage.Storage;
import com.galaxy13.storage.Value;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentMatchers;
import org.mockito.Mockito;

import java.nio.charset.StandardCharsets;
import java.util.Optional;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.Mockito.*;

class FullPipelineTest {
    private EmbeddedChannel channel;
    private Storage<String, Value> storage;
    private Credentials credentials;
    private MessageCreator<String, String> messageCreator;
    private ProcessorController controller;

    @SuppressWarnings("unchecked")
    @BeforeEach
    void setUp() {
        storage = (Storage<String, Value>) Mockito.mock(Storage.class);
        credentials = Mockito.mock(Credentials.class);
        messageCreator = new MessageCreator<>(";", ":");
        controller = new ProcessorControllerImpl(storage);

        var decoder = new CacheMessageDecoder();
        var encoder = new ResponseEncoder<>(messageCreator);
        var authHandler = new AuthHandler(credentials);
        var cacheHandler = new BasicCacheProcessingHandler(controller);
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(encoder).addLast(decoder).addLast(authHandler).addLast(cacheHandler);
    }

    @Test
    void testWrongFormat() {
        ByteBuf message = Unpooled.copiedBuffer("Wrong Message".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> channel.writeInbound(message)).isInstanceOf(CorruptedFrameException.class);
        assertThat(channel.finish()).isTrue();

        ByteBuf response = channel.readOutbound();
        assertThat(response).isNotNull();
        String responseString = response.toString(StandardCharsets.UTF_8);
        assertThat(responseString).isEqualTo("code:303;");
    }

    @Test
    void testWrongOperation() {
        ByteBuf message = Unpooled.copiedBuffer("op:UNKNOWN;key:testKey".getBytes(StandardCharsets.UTF_8));
        assertThatThrownBy(() -> channel.writeInbound(message)).isInstanceOf(CorruptedFrameException.class);
        assertThat(channel.finish()).isTrue();

        ByteBuf response = channel.readOutbound();
        assertThat(response).isNotNull();

        String strResponse = response.toString(StandardCharsets.UTF_8);
        assertThat(strResponse).isEqualTo("code:303;");
    }

    @Test
    void testWrongCredentials() {
        String wrongLogin = "wrongLogin";
        String wrongPassword = "wrongPassword";
        when(credentials.checkCredentials(wrongLogin, wrongPassword)).thenReturn(false);
        ByteBuf message = Unpooled.copiedBuffer(
                String.format("op:AUTHENTICATE;login:%s;password:%s;", wrongLogin, wrongPassword).getBytes(StandardCharsets.UTF_8));
        channel.writeInbound(message);
        assertThat(channel.isOpen()).isFalse();
        assertThat(channel.finish()).isTrue();

        ByteBuf response = channel.readOutbound();
        assertThat(response).isNotNull();
        String strResponse = response.toString(StandardCharsets.UTF_8);
        assertThat(strResponse).contains("code:305;");
    }

    @Test
    void testTokenResponse() {
        String login = "test";
        String password = "pass";
        when(credentials.checkCredentials(ArgumentMatchers.anyString(), ArgumentMatchers.anyString())).thenReturn(false);
        when(credentials.checkCredentials(login, password)).thenReturn(true);

        ByteBuf message = Unpooled.copiedBuffer(
                String.format("op:AUTHENTICATE;login:%s;password:%s;", login, password).getBytes(StandardCharsets.UTF_8));
        channel.writeInbound(message);
        assertThat(channel.isOpen()).isTrue();
        assertThat(channel.finish()).isTrue();

        ByteBuf response = channel.readOutbound();
        assertThat(response).isNotNull();
        String strResponse = response.toString(StandardCharsets.UTF_8);
        assertThat(strResponse).contains("code:203;").containsPattern("(?<=token:)\\S+(?=;)");
    }

    @Test
    void testPutOperation() {
        String mockToken  = "mockToken";
        mockTokenCheck(mockToken);
        String key = "testKey";
        Value value = new Value("testValue");
        when(storage.put(key, value)).thenReturn(value);

        ByteBuf message = Unpooled.copiedBuffer(
                String.format("op:PUT;key:%s;value:%s;token:%s;", key, value.value(), mockToken).getBytes(StandardCharsets.UTF_8));

        channel.writeInbound(message);
        assertThat(channel.isOpen()).isFalse();
        assertThat(channel.finish()).isTrue();

        verify(storage, times(1)).put(key, value);

        ByteBuf response = channel.readOutbound();
        assertThat(response).isNotNull();
        String strResponse = response.toString(StandardCharsets.UTF_8);
        assertThat(strResponse).contains("code:200;")
                .contains("key:" + key + ";")
                .contains("value:" + value.value() + ";");
    }

    @Test
    void testGetOperation() {
        String mockToken  = "mockToken";
        mockTokenCheck(mockToken);
        String key = "testKey";
        Value value = new Value("testValue");
        when(storage.get(key)).thenReturn(Optional.of(value));

        ByteBuf message = Unpooled.copiedBuffer(
                String.format("op:GET;key:%s;value:%s;token:%s", key, value.value(), mockToken)
                        .getBytes(StandardCharsets.UTF_8));
        channel.writeInbound(message);

        assertThat(channel.isOpen()).isFalse();
        assertThat(channel.finish()).isTrue();

        String response = ((ByteBuf) channel.readOutbound()).toString(StandardCharsets.UTF_8);
        assertThat(response).isNotNull().contains("code:200;")
                .contains("key:" + key + ";")
                .contains("value:" + value.value() + ";");
    }

    private void mockTokenCheck(String token){
        when(credentials.containsToken(ArgumentMatchers.anyString())).thenReturn(false);
        when(credentials.containsToken(token)).thenReturn(true);
    }
}
