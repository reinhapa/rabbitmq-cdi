package net.reini.rabbitmq.cdi;

public class DeclarerFactory {
  public ExchangeDeclaration createExchangeDeclaration(String exchangeName) {
    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(exchangeName);
    return exchangeDeclaration;
  }

  public QueueDeclaration createQueueDeclaration(String queueName) {
    QueueDeclaration exchangeDeclarationConfigEntry = new QueueDeclaration(queueName);
    return exchangeDeclarationConfigEntry;
  }

  public QueueToExchangeBindingDeclaration createQueueToExchangeBindingDeclaration(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    QueueToExchangeBindingDeclaration queueToExchangeBindingDeclaration = new QueueToExchangeBindingDeclaration(queueDeclaration, exchangeDeclaration);
    return queueToExchangeBindingDeclaration;
  }
}