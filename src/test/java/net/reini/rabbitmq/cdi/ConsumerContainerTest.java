package net.reini.rabbitmq.cdi;

import org.junit.Assert;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class ConsumerContainerTest
{
    private static final String EXPECTED_QUEUE_NAME = "queue";
    private static final boolean EXPECTED_AUTOACK = true;

    @Mock
    private ConnectionRepository connectionRepositoryMock;
    @Mock
    private CopyOnWriteArrayList<ConsumerHolder> consumerHolderListMock;
    @Mock
    private ExchangeDeclarationConfig exchangeDeclarationConfigMock;
    @Mock
    private QueueDeclarationConfig queueDeclarationConfigMock;
    @Mock
    private ExchangeDeclaration exchangeDeclarationMock;
    @Mock
    private QueueDeclaration queueDeclarationMock;
    @Mock
    private ConnectionConfiguration connectionConfigMock;
    @Mock
    private EventConsumer consumerMock;
    @Mock
    private ConsumerHolderFactory consumerHolderFactoryMock;
    @Mock
    private ConsumerHolder consumerHolderMock;

    @Test
    void testDelegateExchangeDeclaration()
    {
        ConsumerContainer sut = new ConsumerContainer(null, null, null, exchangeDeclarationConfigMock, null, null);
        sut.addExchangeDeclaration(exchangeDeclarationMock);
        Mockito.verify(exchangeDeclarationConfigMock).addExchangeDeclaration(exchangeDeclarationMock);
    }

    @Test
    void testDelegateQueueDeclaration()
    {
        ConsumerContainer sut = new ConsumerContainer(null, null, null, null, queueDeclarationConfigMock, null);
        sut.addQueueDeclaration(queueDeclarationMock);
        Mockito.verify(queueDeclarationConfigMock).addQueueDeclaration(queueDeclarationMock);
    }

    @Test
    void testAddConsumerHolder()
    {
        List<ConsumerHolder> consumerHolders = new ArrayList<>();
        ConsumerContainer sut = new ConsumerContainer(connectionConfigMock, connectionRepositoryMock, consumerHolders, exchangeDeclarationConfigMock, queueDeclarationConfigMock, consumerHolderFactoryMock);
        when(consumerHolderFactoryMock.createConsumerHolder(consumerMock, EXPECTED_QUEUE_NAME, EXPECTED_AUTOACK, connectionRepositoryMock, connectionConfigMock, exchangeDeclarationConfigMock, queueDeclarationConfigMock)).thenReturn(consumerHolderMock);
        sut.addConsumer(consumerMock, EXPECTED_QUEUE_NAME, EXPECTED_AUTOACK);

        Assert.assertEquals(1, consumerHolders.size());
        ConsumerHolder consumerHolder = consumerHolders.get(0);
        Assert.assertSame(consumerHolderMock, consumerHolder);
    }

}
