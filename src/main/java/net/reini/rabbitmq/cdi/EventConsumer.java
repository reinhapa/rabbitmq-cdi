/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
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

import java.io.IOException;

import javax.enterprise.event.Event;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Envelope;

class EventConsumer<T extends Object> implements EnvelopeConsumer {
  private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

  private final Class<T> eventType;
  private final Decoder<T> decoder;
  private final Event<Object> eventControl;

  EventConsumer(Class<T> eventType, Decoder<T> decoder, Event<Object> eventControl) {
    this.eventType = eventType;
    this.decoder = decoder;
    this.eventControl = eventControl;
  }

  /**
   * Builds a CDI event from a message. The CDI event instance is retrieved from the injection
   * container.
   *
   * @param messageBody The message
   * @return the converted CDI event or {@code null} if the conversion has failed
   */
  T buildEvent(byte[] messageBody) {
    try {
      return decoder.decode(messageBody);
    } catch (Exception e) {
      LOGGER.error("Unable to read decode event from message: {}", new String(messageBody), e);
    }
    return null;
  }

  boolean fireEvent(T event) {
    if (event != null) {
      try {
        eventControl.select(eventType).fire(event);
        LOGGER.trace("successfully fired event: {}", event);
        return true;
      } catch (Exception e) {
        LOGGER.error("Failed to fire event: {}", event, e);
      }
    }
    return false;
  }

  @Override
  public boolean consume(String consumerTag, Envelope envelope, BasicProperties properties,
      byte[] body) throws IOException {
    LOGGER.debug("Handle delivery: consumerTag: {}, envelope: {}, properties: {}", consumerTag,
        envelope, properties);
    String contentType = properties.getContentType();
    if (decoder.willDecode(contentType)) {
      return fireEvent(buildEvent(body));
    } else {
      LOGGER.error("Unable to process unknown message content type: {}", contentType);
      return false;
    }
  }
}
