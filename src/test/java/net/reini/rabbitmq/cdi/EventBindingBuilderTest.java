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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.reini.rabbitmq.cdi.EventBinder.EventBindingBuilder;
import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;
import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;

@ExtendWith(MockitoExtension.class)
class EventBindingBuilderTest {
  @Mock
  private Consumer<QueueBinding<TestEvent>> queueConsumer;
  @Mock
  private Consumer<ExchangeBinding<TestEvent>> exchangeConsumer;

  private EventBindingBuilder<TestEvent> builder;

  @BeforeEach
  void prepare() {
    builder = new EventBindingBuilder<>(TestEvent.class, queueConsumer, exchangeConsumer);
  }

  @Test
  void testToExchange() {
    ExchangeBinding<TestEvent> expected = new ExchangeBinding<>(TestEvent.class, "theExchange");

    assertEquals(expected, builder.toExchange("theExchange"));

    verify(exchangeConsumer).accept(expected);
  }

  @Test
  void testToQueue() {
    QueueBinding<TestEvent> expected = new QueueBinding<>(TestEvent.class, "theQueue");

    assertEquals(expected, builder.toQueue("theQueue"));

    verify(queueConsumer).accept(expected);
  }
}
