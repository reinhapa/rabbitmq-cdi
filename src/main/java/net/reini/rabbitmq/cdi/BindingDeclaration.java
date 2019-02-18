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
