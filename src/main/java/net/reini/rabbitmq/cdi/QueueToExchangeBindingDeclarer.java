package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class QueueToExchangeBindingDeclarer implements Declarer<QueueToExchangeBindingDeclaration> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(QueueToExchangeBindingDeclaration.class);

  @Override
  public void declare(Channel channel, QueueToExchangeBindingDeclaration declaration) throws IOException {
    ExchangeDeclaration exchangeDeclaration = declaration.getExchangeDeclaration();
    QueueDeclaration queueDeclaration = declaration.getQueueDeclaration();
    String queueName = queueDeclaration.getQueueName();
    String exchangeName = exchangeDeclaration.getExchangeName();
    String routingKey = declaration.getRoutingKey();
    Map<String, Object> arguments = declaration.getArguments();

    LOGGER.info("binding queue " + queueName + " to exchange " + exchangeName + " with routingkey " + routingKey);
    channel.queueBind(queueName, exchangeName, routingKey, arguments);
  }
}
