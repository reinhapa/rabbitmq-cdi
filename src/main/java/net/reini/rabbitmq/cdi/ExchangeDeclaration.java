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

import com.rabbitmq.client.BuiltinExchangeType;

public final class ExchangeDeclaration implements Declaration {
  private final Map<String, Object> arguments;

  private boolean durable;
  private boolean autoDelete;
  private String exchangeName;
  private String exchangeType;

  public ExchangeDeclaration(String exchangeName) {
    arguments = new HashMap<>();
    autoDelete = true;
    exchangeType = BuiltinExchangeType.DIRECT.getType();
    this.exchangeName = exchangeName;
  }

  public ExchangeDeclaration withDurable(boolean durable) {
    this.durable = durable;
    return this;
  }

  public ExchangeDeclaration withAutoDelete(boolean autoDelete) {
    this.autoDelete = autoDelete;
    return this;
  }

  public ExchangeDeclaration withType(String exchangeType) {
    this.exchangeType = exchangeType;
    return this;
  }

  public ExchangeDeclaration withType(BuiltinExchangeType exchangeType) {
    this.exchangeType = exchangeType.getType();
    return this;
  }

  public ExchangeDeclaration withArgument(String key, Object argument) {
    arguments.put(key, argument);
    return this;
  }

  String getExchangeName() {
    return exchangeName;
  }

  String getExchangeType() {
    return exchangeType;
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

  @Override
  public boolean equals(Object o) {
    if (this == o) {
      return true;
    }
    if (!(o instanceof ExchangeDeclaration)) {
      return false;
    }

    ExchangeDeclaration that = (ExchangeDeclaration) o;

    if (durable != that.durable) {
      return false;
    }
    if (autoDelete != that.autoDelete) {
      return false;
    }
    if (!exchangeName.equals(that.exchangeName)) {
      return false;
    }
    if (!exchangeType.equals(that.exchangeType)) {
      return false;
    }
    return arguments.equals(that.arguments);

  }

  @Override
  public int hashCode() {
    int result = exchangeName.hashCode();
    result = 31 * result + exchangeType.hashCode();
    result = 31 * result + (durable ? 1 : 0);
    result = 31 * result + (autoDelete ? 1 : 0);
    result = 31 * result + arguments.hashCode();
    return result;
  }

  @Override
  public String toString() {
    return "exchange declaration for exchangeName='" + exchangeName + '\'' + ", exchangeType='" + exchangeType + '\''
        + ", durable=" + durable + ", autoDelete=" + autoDelete + ", arguments=" + arguments;
  }
}
