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

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Consumer;

/**
 * Factory responsible creating {@link Consumer} instances.
 *
 * @author Patrick Reinhart
 */
class ConsumerFactory {
  /**
   * Creates a simple consumer that does not acknowledge the message received.
   * 
   * @param consumer the event consumer
   * @return the message consumer instance
   */
  public Consumer create(EventConsumer<?> consumer) {
    return ConsumerImpl.create(consumer);
  }

  /**
   * Creates a acknowledge aware message consumer that only do acknowledge messages when the event
   * has been sent successfully.
   * 
   * @param consumer the event consumer
   * @return the message consumer instance
   */
  public Consumer createAcknowledged(EventConsumer<?> consumer, Channel channel) {
    return ConsumerImpl.createAcknowledged(consumer, channel);
  }
}
