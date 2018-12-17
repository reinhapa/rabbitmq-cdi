package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.io.IOException;
import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;
import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
  private ConsumerContainer consumerContainer;

  @InjectMocks
  private TestEventBinder eventBinder;

  @BeforeEach
  void prepare() {
    eventBinder.initializeConsumerContainer();
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
  void testBindQueue() {
    QueueBinding<TestEvent> queueBinding = new QueueBinding<>(TestEvent.class, "queue");

    eventBinder.bindQueue(queueBinding);
  }

  @Test
  void testBindExchange() {
    ExchangeBinding<TestEvent> exchangeBinding = new ExchangeBinding<>(TestEvent.class, "exchange");

    eventBinder.bindExchange(exchangeBinding);
  }

  @Test
  void testDeclareExchange() {
    ExchangeDeclaration exchangeDeclaration = eventBinder.declareExchange("hello");
    assertEquals("hello", exchangeDeclaration.getExchangeName());
    assertNotNull(exchangeDeclaration);
  }

  @Test
  void testDeclareQueue() {
    QueueDeclaration queue = eventBinder.declareQueue("hello");
    assertEquals("hello", queue.getQueueName());
    assertNotNull(queue);
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
