package net.reini.rabbitmq.cdi;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class DeclarerFactoryTest {

  private static final String EXPECTED_EXCHANGE_NAME = "exchange";
  private static final String EXPECTED_QUEUE_NAME = "queue";
  DeclarerFactory sut = new DeclarerFactory();

  @Test
  void testCreateExchangeDeclaration() {
    ExchangeDeclaration result = sut.createExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    Assert.assertNotNull(result);
    Assert.assertEquals(EXPECTED_EXCHANGE_NAME, result.getExchangeName());
  }

  @Test
  void testCreateQueueDeclaration() {
    QueueDeclaration result = sut.createQueueDeclaration(EXPECTED_QUEUE_NAME);
    Assert.assertNotNull(result);
    Assert.assertEquals(EXPECTED_QUEUE_NAME, result.getQueueName());
  }

  @Test
  void testCreateQueueToExchangeBinding() {
    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    ExchangeDeclaration exchangeDeclaration =new ExchangeDeclaration(EXPECTED_EXCHANGE_NAME);
    QueueToExchangeBindingDeclaration result = sut.createQueueToExchangeBindingDeclaration(queueDeclaration,exchangeDeclaration);
    Assert.assertNotNull(result);
    Assert.assertSame(exchangeDeclaration, result.getExchangeDeclaration());
    Assert.assertSame(queueDeclaration, result.getQueueDeclaration());
  }

}