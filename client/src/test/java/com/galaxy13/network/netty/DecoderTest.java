package com.galaxy13.network.netty;

import com.galaxy13.network.message.Response;
import com.galaxy13.network.message.code.MessageCode;
import com.galaxy13.network.netty.decoder.ResponseDecoder;
import io.netty.buffer.ByteBuf;
import io.netty.buffer.Unpooled;
import io.netty.channel.embedded.EmbeddedChannel;
import io.netty.handler.codec.CorruptedFrameException;
import io.netty.util.CharsetUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.*;

class DecoderTest {
    private EmbeddedChannel channel;

    @BeforeEach
    void setUp() {
        var decoder = new ResponseDecoder(";", ":");
        channel = new EmbeddedChannel();
        channel.pipeline().addLast(decoder);
    }

    @Test
    void decoderThrowsExceptionTest1(){
        ByteBuf errMsg1 = Unpooled.copiedBuffer("Wrong cache message", CharsetUtil.UTF_8);
        assertThatThrownBy(() -> channel.writeInbound(errMsg1)).isInstanceOf(CorruptedFrameException.class);
    }

    @Test
    void decoderThrowsExceptionTest2(){
        ByteBuf errMsg2 = Unpooled.copiedBuffer("code:000", CharsetUtil.UTF_8);
        assertThatThrownBy(() -> channel.writeInbound(errMsg2)).isInstanceOf(CorruptedFrameException.class);
    }

    @Test
    void decoderThrowsExceptionTest3(){
        ByteBuf errMsg3 = Unpooled.copiedBuffer("", CharsetUtil.UTF_8);
        channel.writeInbound(errMsg3);

        ByteBuf output = channel.readInbound();
        assertThat(output).isNull();
    }

    @Test
    void decoderParsesCodeMessageTest(){
        ByteBuf codeMsg = Unpooled.copiedBuffer("code:200", CharsetUtil.UTF_8);
        channel.writeInbound(codeMsg);

        assertThat(channel.finish()).isTrue();

        Response response = channel.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(MessageCode.OK);
    }

    @Test
    void decoderParsesPutGetResponseMessageTest(){
        ByteBuf responsePutMsg = Unpooled.copiedBuffer("code:200;key:test;value:1234", CharsetUtil.UTF_8);
        channel.writeInbound(responsePutMsg);

        assertThat(channel.finish()).isTrue();

        Response response = channel.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(MessageCode.OK);
        assertThat(response.getParameter("key")).isEqualTo("test");
        assertThat(response.getParameter("value")).isEqualTo("1234");
    }

    @Test
    void decoderParsesSubscriptionResponseMessageTest(){
        ByteBuf subscriptionMsg = Unpooled.copiedBuffer("code:201;key:test;value:1234", CharsetUtil.UTF_8);

        channel.writeInbound(subscriptionMsg);
        assertThat(channel.finish()).isTrue();

        Response response = channel.readInbound();
        assertThat(response).isNotNull();
        assertThat(response.getCode()).isEqualTo(MessageCode.SUBSCRIPTION_RESPONSE);
        assertThat(response.getParameter("key")).isEqualTo("test");
        assertThat(response.getParameter("value")).isEqualTo("1234");
    }
}
