package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.Channel;

@ExtendWith(MockitoExtension.class)
class QueueToExchangeBindingDeclarerTest {
  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_EXCHANGE_TYPE = "direct";
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final boolean EXPECTED_EXCHANGE_AUTODELETE_SETTING = true;
  private static final boolean EXPECTED_EXCHANGE_DURABLE_SETTING = true;
  private static final String EXPECTED_ROUTING_KEY = "routingkey";
  private static final String EXPECTED_QUEUE_NAME = "queue";

  @Mock
  private Channel channelMock;

  @Test
  void testDeclareQueueToExchangeBinding() throws IOException {

    ExchangeDeclaration exchangeDeclaration = new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);

    QueueToExchangeBindingDeclaration queueToExchangeBindingDeclaration = new QueueToExchangeBindingDeclaration(queueDeclaration, exchangeDeclaration);
    queueToExchangeBindingDeclaration.withRoutingKey(EXPECTED_ROUTING_KEY);
    queueToExchangeBindingDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);

    QueueToExchangeBindingDeclarer sut = new QueueToExchangeBindingDeclarer();
    sut.declare(channelMock, queueToExchangeBindingDeclaration);

    Map<String, Object> expectedArgumens = new HashMap();
    expectedArgumens.put(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    verify(channelMock, Mockito.times(1)).queueBind(EXPECTED_QUEUE_NAME, EXPECTED_EXCHANGE_NAME, EXPECTED_ROUTING_KEY, expectedArgumens);
  }

}