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

import java.util.HashMap;
import java.util.Map;

public final class BindingDeclaration implements Declaration {
  private final QueueDeclaration queueDeclaration;
  private final ExchangeDeclaration exchangeDeclaration;
  private String routingKey;
  private Map<String, Object> arguments;

  public BindingDeclaration(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    this.queueDeclaration = queueDeclaration;
    this.exchangeDeclaration = exchangeDeclaration;
    this.routingKey = "";
    this.arguments = new HashMap<>();
  }

  public BindingDeclaration withArgument(String key, Object argument) {
    arguments.put(key, argument);
    return this;
  }

  public BindingDeclaration withRoutingKey(String routingKey) {
    this.routingKey = routingKey;
    return this;
  }

  ExchangeDeclaration getExchangeDeclaration() {
    return exchangeDeclaration;
  }

  QueueDeclaration getQueueDeclaration() {
    return queueDeclaration;
  }

  String getRoutingKey() {
    return routingKey;
  }

  Map<String, Object> getArguments() {
    return arguments;
  }

  @Override
  public String toString() {
    return "queue to exchange binding" +
        " for queue " + queueDeclaration.getQueueName() + " to exchange " + exchangeDeclaration.getExchangeName() +
        ", routingKey='" + routingKey + '\'' +
        ", arguments=" + arguments;
  }
}
