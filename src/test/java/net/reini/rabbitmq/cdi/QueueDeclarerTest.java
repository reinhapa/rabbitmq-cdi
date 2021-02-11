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

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class QueueDeclarerTest {
  private static final String EXPECTED_QUEUE_NAME = "queue";
  private static final boolean EXPECTED_EXCLUSIVE_ACCESS_SETTING = true;
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final boolean EXPECTED_EXCHANGE_AUTODELETE_SETTING = true;
  private static final boolean EXPECTED_EXCHANGE_DURABLE_SETTING = true;

  @Mock
  private Channel channelMock;

  @Test
  void testQueueDeclaration() throws IOException {

    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    queueDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    queueDeclaration.withAutoDelete(EXPECTED_EXCHANGE_AUTODELETE_SETTING);
    queueDeclaration.withDurable(EXPECTED_EXCHANGE_DURABLE_SETTING);
    queueDeclaration.withExclusiveAccess(EXPECTED_EXCLUSIVE_ACCESS_SETTING);
    QueueDeclarer sut = new QueueDeclarer();
    sut.declare(channelMock,queueDeclaration);

    verify(channelMock, Mockito.times(1)).queueDeclare(queueDeclaration.getQueueName(),
        queueDeclaration.isDurable(), queueDeclaration.isExclusive(),
        queueDeclaration.isAutoDelete(), queueDeclaration.getArguments());
  }

}