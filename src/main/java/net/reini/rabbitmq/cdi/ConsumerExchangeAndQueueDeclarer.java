package net.reini.rabbitmq.cdi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

class ConsumerExchangeAndQueueDeclarer {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ConsumerExchangeAndQueueDeclarer.class);
  private final ExchangeDeclarationConfig exchangeDeclarationConfig;
  private final QueueDeclarationConfig queueDeclarationConfig;

  public ConsumerExchangeAndQueueDeclarer(ExchangeDeclarationConfig exchangeDeclarationConfig,
      QueueDeclarationConfig queueDeclarationConfig) {
    this.exchangeDeclarationConfig = exchangeDeclarationConfig;
    this.queueDeclarationConfig = queueDeclarationConfig;
  }

  public void declareQueuesAndExchanges(Channel channel) throws IOException {
    declareExchanges(channel);
    declareQueues(channel);
  }

  private void declareExchanges(Channel channel) throws IOException {
    for (ExchangeDeclaration config : exchangeDeclarationConfig.getExchangeDeclarations()) {
      LOGGER.info("declaring exchange ", config);
      channel.exchangeDeclare(config.getExchangeName(), config.getExchangeType(),
          config.isDurable(), config.isAutoDelete(), config.getArguments());
    }
  }

  private void declareQueues(Channel channel) throws IOException {
    for (QueueDeclaration config : queueDeclarationConfig.getQueueDeclarations()) {
      LOGGER.info("declaring queue ", config);
      channel.queueDeclare(config.getQueueName(), config.isDurable(), config.isExclusive(),
          config.isAutoDelete(), config.getArguments());
    }
  }
}
