package net.reini.rabbitmq.cdi;

/**
 * Factory class responsible for creating declaration objects which can be assigned to a
 * consumer or producer. This class only creates objects and does not declare anything
 * To apply declarations to rabbitmq channels of consumers or producers the declarations
 * need to be added via methods:
 * @see net.reini.rabbitmq.cdi.EventBinder.QueueBinding#withDeclarations(Declaration...)
 * @see net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding#withDeclarations(Declaration...)
 *
 */
public class DeclarerFactory {

  /**
   * Creates a new ExchangeDeclaration object
   * @param exchangeName the name of the exchange which should be declared
   * @return a new ExchangeDeclaration object
   */
  public ExchangeDeclaration createExchangeDeclaration(String exchangeName) {
    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(exchangeName);
    return exchangeDeclaration;
  }

  /**
   * Creates a new QueueDeclaration object
   * @param queueName the name of the queue which should be declared
   * @return a new QueueDeclaration object
   */
  public QueueDeclaration createQueueDeclaration(String queueName) {
    QueueDeclaration exchangeDeclarationConfigEntry = new QueueDeclaration(queueName);
    return exchangeDeclarationConfigEntry;
  }

  /**
   * Creates a new BindingDeclaration object
   * @param queueDeclaration the queue which should be bound to the exchange
   * @param exchangeDeclaration the exchange the queue should be bound to
   * @return a new BindingDeclaration object
   */
  public BindingDeclaration createBindingDeclaration(QueueDeclaration queueDeclaration, ExchangeDeclaration exchangeDeclaration) {
    BindingDeclaration bindingDeclaration = new BindingDeclaration(queueDeclaration, exchangeDeclaration);
    return bindingDeclaration;
  }
}