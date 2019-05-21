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