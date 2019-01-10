package net.reini.rabbitmq.cdi;

import java.util.HashMap;
import java.util.Map;

public final class QueueDeclaration {
  private final Map<String, Object> arguments;

  private boolean durable;
  private boolean autoDelete;
  private boolean exclusive;
  private String queueName;

  QueueDeclaration(String queueName) {
    arguments = new HashMap<>();
    autoDelete = true;
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
    return "queueName='" + queueName + '\'' + ", durable=" + durable + ", autoDelete=" + autoDelete
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
