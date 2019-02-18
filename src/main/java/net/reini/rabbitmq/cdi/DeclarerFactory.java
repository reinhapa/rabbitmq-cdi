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

  public BindingDeclaration createBindingDeclaration(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    BindingDeclaration bindingDeclaration = new BindingDeclaration(queueDeclaration, exchangeDeclaration);
    return bindingDeclaration;
  }
}