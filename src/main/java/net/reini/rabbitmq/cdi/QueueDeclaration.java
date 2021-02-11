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

public final class QueueDeclaration implements Declaration {
  private final Map<String, Object> arguments;

  private boolean durable;
  private boolean autoDelete;
  private boolean exclusive;
  private String queueName;

  QueueDeclaration(String queueName) {
    arguments = new HashMap<>();
    this.queueName = queueName;
  }

  public QueueDeclaration withDurable(boolean durable) {
    this.durable = durable;
    return this;
  }

  public QueueDeclaration withAutoDelete(boolean autoDelete) {
    this.autoDelete = autoDelete;
    return this;
  }

  public QueueDeclaration withArgument(String key, Object argument) {
    arguments.put(key, argument);
    return this;
  }

  public QueueDeclaration withExclusiveAccess(boolean exclusive) {
    this.exclusive = exclusive;
    return this;
  }

  @Override
  public String toString() {
    return "queue declaration for queueName='" + queueName + '\'' + ", durable=" + durable + ", autoDelete=" + autoDelete
        + ", arguments=" + arguments + ", exclusive=" + exclusive;
  }

  boolean isExclusive() {
    return exclusive;
  }

  String getQueueName() {
    return this.queueName;
  }

  boolean isDurable() {
    return durable;
  }

  boolean isAutoDelete() {
    return autoDelete;
  }

  Map<String, Object> getArguments() {
    return arguments;
  }
}
