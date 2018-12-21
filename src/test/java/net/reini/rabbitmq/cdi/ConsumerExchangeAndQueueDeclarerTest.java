package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;

import java.io.IOException;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class ConsumerExchangeAndQueueDeclarerTest {
  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_EXCHANGE_TYPE = "direct";
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final boolean EXPECTED_EXCHANGE_AUTODELETE_SETTING = true;
  private static final boolean EXPECTED_EXCHANGE_DURABLE_SETTING = true;
  private static final String EXPECTED_QUEUE_NAME = "queue";
  private static final boolean EXPECTED_EXCLUSIVE_ACCESS_SETTING = true;

  @Mock
  private Channel channelMock;

  @Test
  void testExchangeDeclaration() throws IOException {
    ExchangeDeclarationConfig exchangeDeclarationConfig = new ExchangeDeclarationConfig();
    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    exchangeDeclaration.withExchangeType(EXPECTED_EXCHANGE_TYPE);
    exchangeDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    exchangeDeclaration.withAutoDelete(EXPECTED_EXCHANGE_AUTODELETE_SETTING);
    exchangeDeclaration.withDurable(EXPECTED_EXCHANGE_DURABLE_SETTING);
    exchangeDeclarationConfig.addExchangeDeclaration(exchangeDeclaration);
    QueueDeclarationConfig queueDeclarationConfig = new QueueDeclarationConfig();
    ConsumerExchangeAndQueueDeclarer sut =
        new ConsumerExchangeAndQueueDeclarer(exchangeDeclarationConfig, queueDeclarationConfig);
    sut.declareQueuesAndExchanges(channelMock);

    verify(channelMock, Mockito.times(1)).exchangeDeclare(exchangeDeclaration.getExchangeName(),
        exchangeDeclaration.getExchangeType(), exchangeDeclaration.isDurable(),
        exchangeDeclaration.isAutoDelete(), exchangeDeclaration.getArguments());
  }

  @Test
  void testQueueDeclaration() throws IOException {
    ExchangeDeclarationConfig exchangeDeclarationConfig = new ExchangeDeclarationConfig();
    QueueDeclarationConfig queueDeclarationConfig = new QueueDeclarationConfig();
    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    queueDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    queueDeclaration.withAutoDelete(EXPECTED_EXCHANGE_AUTODELETE_SETTING);
    queueDeclaration.withDurable(EXPECTED_EXCHANGE_DURABLE_SETTING);
    queueDeclaration.withExclusiveAccess(EXPECTED_EXCLUSIVE_ACCESS_SETTING);
    queueDeclarationConfig.addQueueDeclaration(queueDeclaration);
    ConsumerExchangeAndQueueDeclarer sut =
        new ConsumerExchangeAndQueueDeclarer(exchangeDeclarationConfig, queueDeclarationConfig);
    sut.declareQueuesAndExchanges(channelMock);

    verify(channelMock, Mockito.times(1)).queueDeclare(queueDeclaration.getQueueName(),
        queueDeclaration.isDurable(), queueDeclaration.isExclusive(),
        queueDeclaration.isAutoDelete(), queueDeclaration.getArguments());
  }
}
