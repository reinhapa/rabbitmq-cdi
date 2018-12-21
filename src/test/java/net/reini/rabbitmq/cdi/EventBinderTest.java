package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.mockito.Mockito.verify;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;

import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;
import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;

@ExtendWith(MockitoExtension.class)
class EventBinderTest {
  @Mock
  private Event<Object> remoteEventControl;
  @Mock
  private Instance<Object> remoteEventPool;
  @Mock
  private EventPublisher eventPublisher;
  @Mock
  private ConnectionRepository connectionRepository;
  @Mock
  private ConsumerContainerFactory consumerContainerFactory;
  @Mock
  private ConsumerContainer consumerContainerMock;


  @InjectMocks
  private TestEventBinder eventBinder;

  @BeforeEach
  void prepare() {
    Mockito.when(consumerContainerFactory.create(Mockito.any(), Mockito.any()))
        .thenReturn(consumerContainerMock);
    eventBinder.initializeConsumerContainer();
  }

  @Test
  void testStop() {

    eventBinder.stop();
    verify(consumerContainerMock).stop();
  }


  @Test
  void testBind() {
    assertNotNull(eventBinder.bind(TestEvent.class));
  }

  @Test
  void testConfiguration() {
    assertNotNull(eventBinder.configuration());
  }

  @Test
  void testInitialize() throws IOException {
    eventBinder.initialize();
  }

  @Test
  void testBindQueue() throws IOException {
    QueueBinding<TestEvent> queueBinding = new QueueBinding<>(TestEvent.class, "queue");

    eventBinder.bindQueue(queueBinding);
    eventBinder.initialize();
  }

  @Test
  void testBindExchange() throws IOException {
    ExchangeBinding<TestEvent> exchangeBinding = new ExchangeBinding<>(TestEvent.class, "exchange");

    eventBinder.bindExchange(exchangeBinding);
    eventBinder.initialize();
  }

  @Test
  void testDeclareExchange() throws IOException {
    ExchangeDeclaration exchangeDeclaration = eventBinder.declareExchange("hello");
    assertEquals("hello", exchangeDeclaration.getExchangeName());
    assertNotNull(exchangeDeclaration);
    eventBinder.initialize();
    verify(consumerContainerMock).addExchangeDeclaration(exchangeDeclaration);
  }

  @Test
  void testDeclareQueue() throws IOException {
    QueueDeclaration queue = eventBinder.declareQueue("hello");
    assertEquals("hello", queue.getQueueName());
    assertNotNull(queue);
    eventBinder.initialize();
    verify(consumerContainerMock).addQueueDeclaration(queue);
  }

  @Test
  void testUriDecode() {
    assertEquals("stock + stein", EventBinder.uriDecode("stock%20+%20stein"));
  }


  static class TestEventBinder extends EventBinder {
    @Override
    protected void bindEvents() {
    }
  }
}
