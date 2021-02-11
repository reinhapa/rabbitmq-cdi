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

/**
 * Factory class responsible for creating declaration objects which can be assigned to a
 * consumer or producer. This class only creates objects and does not declare anything
 * To apply declarations to rabbitmq channels of consumers or producers the declarations
 * need to be added via methods:
 * @see net.reini.rabbitmq.cdi.EventBinder.QueueBinding#withDeclaration(QueueDeclaration)
 * @see net.reini.rabbitmq.cdi.EventBinder.QueueBinding#withDeclaration(ExchangeDeclaration)
 * @see net.reini.rabbitmq.cdi.EventBinder.QueueBinding#withDeclaration(BindingDeclaration)
 * @see net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding#withDeclaration(QueueDeclaration)
 * @see net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding#withDeclaration(ExchangeDeclaration)
 * @see net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding#withDeclaration(BindingDeclaration)
 *
 */
public class DeclarerFactory {

  /**
   * Creates a new ExchangeDeclaration object
   * @param exchangeName the name of the exchange which should be declared
   * @return a new ExchangeDeclaration object
   */
  public ExchangeDeclaration createExchangeDeclaration(String exchangeName) {
    return new ExchangeDeclaration(exchangeName);
  }

  /**
   * Creates a new QueueDeclaration object
   * @param queueName the name of the queue which should be declared
   * @return a new QueueDeclaration object
   */
  public QueueDeclaration createQueueDeclaration(String queueName) {
    return new QueueDeclaration(queueName);
  }

  /**
   * Creates a new BindingDeclaration object
   * @param queueDeclaration the queue which should be bound to the exchange
   * @param exchangeDeclaration the exchange the queue should be bound to
   * @return a new BindingDeclaration object
   */
  public BindingDeclaration createBindingDeclaration(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    return new BindingDeclaration(queueDeclaration, exchangeDeclaration);
  }
}