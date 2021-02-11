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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeclarerFactoryTest {

  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_QUEUE_NAME = "queue";
  DeclarerFactory sut = new DeclarerFactory();

  @Test
  void testCreateExchangeDeclaration() {
    ExchangeDeclaration result = sut.createExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    assertNotNull(result);
    assertEquals(EXPECTED_EXCHANGE_NAME, result.getExchangeName());
  }

  @Test
  void testCreateQueueDeclaration() {
    QueueDeclaration result = sut.createQueueDeclaration(EXPECTED_QUEUE_NAME);
    assertNotNull(result);
    assertEquals(EXPECTED_QUEUE_NAME, result.getQueueName());
  }

  @Test
  void testBinding() {
    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    ExchangeDeclaration exchangeDeclaration =new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    BindingDeclaration result = sut.createBindingDeclaration(queueDeclaration,exchangeDeclaration);
    assertNotNull(result);
    assertSame(exchangeDeclaration, result.getExchangeDeclaration());
    assertSame(queueDeclaration, result.getQueueDeclaration());
  }

}