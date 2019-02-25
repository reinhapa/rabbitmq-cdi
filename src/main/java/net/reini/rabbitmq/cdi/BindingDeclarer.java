package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.Map;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

public class BindingDeclarer implements Declarer<BindingDeclaration> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(BindingDeclaration.class);

  @Override
  public void declare(Channel channel, BindingDeclaration declaration) throws IOException {
    ExchangeDeclaration exchangeDeclaration = declaration.getExchangeDeclaration();
    QueueDeclaration queueDeclaration = declaration.getQueueDeclaration();
    String queueName = queueDeclaration.getQueueName();
    String exchangeName = exchangeDeclaration.getExchangeName();
    String routingKey = declaration.getRoutingKey();
    Map<String, Object> arguments = declaration.getArguments();

    LOGGER.info("binding queue {} to exchange {} with routingkey {}",queueName,exchangeName, routingKey);
    channel.queueBind(queueName, exchangeName, routingKey, arguments);
  }
}
