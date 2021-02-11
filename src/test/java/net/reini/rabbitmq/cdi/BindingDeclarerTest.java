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

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class BindingDeclarerTest {
  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final String EXPECTED_ROUTING_KEY = "routingkey";
  private static final String EXPECTED_QUEUE_NAME = "queue";

  @Mock
  private Channel channelMock;

  @Test
  void testDeclareBinding() throws IOException {

    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);

    BindingDeclaration bindingDeclaration = new BindingDeclaration(queueDeclaration, exchangeDeclaration);
    bindingDeclaration.withRoutingKey(EXPECTED_ROUTING_KEY);
    bindingDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);

    BindingDeclarer sut = new BindingDeclarer();
    sut.declare(channelMock, bindingDeclaration);

    Map<String, Object> expectedArgumens = new HashMap<>();
    expectedArgumens.put(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    verify(channelMock, Mockito.times(1)).queueBind(EXPECTED_QUEUE_NAME, EXPECTED_EXCHANGE_NAME, EXPECTED_ROUTING_KEY, expectedArgumens);
  }

}