/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015, 2019 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */

package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.io.IOException;
import java.util.List;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties.Builder;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.MessageProperties;

@ExtendWith(MockitoExtension.class)
class PublisherConfigurationTest {
  @Mock
  private ConnectionConfig config;
  @Mock
  private Encoder<Object> encoder;
  @Mock
  private BiConsumer<Object, PublishException> errorHandler;
  @Mock
  private Channel channel;
  @Mock
  private List<ExchangeDeclaration> declarationsMock;

  private Builder propertiesBuilder;
  private Object event;

  @BeforeEach
  void prepare() {
    propertiesBuilder = MessageProperties.BASIC.builder();
    event = new Object();
  }

  @Test
  void testPublisherConfiguration() throws EncodeException, IOException {
    byte[] expectedData = "somedata".getBytes();

    when(encoder.contentType()).thenReturn("application/sometype");
    when(encoder.encode(event)).thenReturn(expectedData);

    PublisherConfiguration<Object> publisherConfig =
        new PublisherConfiguration(config, "exchange",
            e -> "routingKey", propertiesBuilder, encoder, errorHandler, declarationsMock);

    publisherConfig.publish(channel, event);

    verify(channel).basicPublish("exchange", "routingKey",
        propertiesBuilder.contentType("application/sometype").build(), expectedData);
  }

  @Test
  void testAcceptError() {
    PublishException publishError = new PublishException("some error", null);
    PublisherConfiguration<Object> publisherConfig = new PublisherConfiguration(config,
        "exchange",
        e -> "routingKey", propertiesBuilder, encoder, errorHandler, declarationsMock);

    publisherConfig.accept(event, publishError);
    
    verify(errorHandler).accept(event, publishError);
  }
}
