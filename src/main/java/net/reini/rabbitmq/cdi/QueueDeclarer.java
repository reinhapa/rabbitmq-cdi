package net.reini.rabbitmq.cdi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

final class QueueDeclarer implements Declarer<QueueDeclaration> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(QueueDeclarer.class);

  @Override
  public void declare(Channel channel, QueueDeclaration declaration) throws IOException {
    LOGGER.info("declaring queue ", declaration.getQueueName());
    channel.queueDeclare(declaration.getQueueName(), declaration.isDurable(), declaration.isExclusive(),
        declaration.isAutoDelete(), declaration.getArguments());
  }
}
