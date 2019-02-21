package net.reini.rabbitmq.cdi;

import java.io.IOException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;

final class ExchangeDeclarer implements Declarer<ExchangeDeclaration> {
  private static final Logger LOGGER =
      LoggerFactory.getLogger(ExchangeDeclarer.class);

  @Override
  public void declare(Channel channel, ExchangeDeclaration declaration) throws IOException {

    String exchangeName = declaration.getExchangeName();
    LOGGER.info("declaring exchange ", exchangeName);
    channel.exchangeDeclare(exchangeName, declaration.getExchangeType(),
        declaration.isDurable(), declaration.isAutoDelete(), declaration.getArguments());
  }

}
