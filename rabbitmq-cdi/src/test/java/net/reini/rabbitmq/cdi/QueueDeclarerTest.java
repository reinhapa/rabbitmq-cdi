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
class QueueDeclarerTest {
  private static final String EXPECTED_QUEUE_NAME = "queue";
  private static final boolean EXPECTED_EXCLUSIVE_ACCESS_SETTING = true;
  private static final String EXPECTED_ARGUMENT_NAME = "argument";
  private static final Object EXPECTED_ARGUMENT_VALUE = new Object();
  private static final boolean EXPECTED_EXCHANGE_AUTODELETE_SETTING = true;
  private static final boolean EXPECTED_EXCHANGE_DURABLE_SETTING = true;

  @Mock
  private Channel channelMock;

  @Test
  void testQueueDeclaration() throws IOException {

    QueueDeclaration queueDeclaration = new QueueDeclaration(EXPECTED_QUEUE_NAME);
    queueDeclaration.withArgument(EXPECTED_ARGUMENT_NAME, EXPECTED_ARGUMENT_VALUE);
    queueDeclaration.withAutoDelete(EXPECTED_EXCHANGE_AUTODELETE_SETTING);
    queueDeclaration.withDurable(EXPECTED_EXCHANGE_DURABLE_SETTING);
    queueDeclaration.withExclusiveAccess(EXPECTED_EXCLUSIVE_ACCESS_SETTING);
    QueueDeclarer sut = new QueueDeclarer();
    sut.declare(channelMock,queueDeclaration);

    verify(channelMock, Mockito.times(1)).queueDeclare(queueDeclaration.getQueueName(),
        queueDeclaration.isDurable(), queueDeclaration.isExclusive(),
        queueDeclaration.isAutoDelete(), queueDeclaration.getArguments());
  }

}