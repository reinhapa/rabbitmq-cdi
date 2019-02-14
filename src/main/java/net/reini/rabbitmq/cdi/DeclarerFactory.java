package net.reini.rabbitmq.cdi;

public class DeclarerFactory {
  public ExchangeDeclaration declareExchange(String exchangeName) {
    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(exchangeName);
    return exchangeDeclaration;
  }

  public QueueDeclaration declareQueue(String queueName) {
    QueueDeclaration exchangeDeclarationConfigEntry = new QueueDeclaration(queueName);
    return exchangeDeclarationConfigEntry;
  }

  public QueueToExchangeBindingDeclaration declareQueueToExchangeBinding(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    QueueToExchangeBindingDeclaration queueToExchangeBindingDeclaration = new QueueToExchangeBindingDeclaration(queueDeclaration, exchangeDeclaration);
    return queueToExchangeBindingDeclaration;
  }

}
