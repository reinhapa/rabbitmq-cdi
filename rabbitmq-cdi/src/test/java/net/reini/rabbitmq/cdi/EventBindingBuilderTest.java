package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.verify;

import java.util.function.Consumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.reini.rabbitmq.cdi.EventBinder.EventBindingBuilder;
import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;
import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;

@ExtendWith(MockitoExtension.class)
class EventBindingBuilderTest {
  @Mock
  private Consumer<QueueBinding<TestEvent>> queueConsumer;
  @Mock
  private Consumer<ExchangeBinding<TestEvent>> exchangeConsumer;

  private EventBindingBuilder<TestEvent> builder;

  @BeforeEach
  void prepare() {
    builder = new EventBindingBuilder<>(TestEvent.class, queueConsumer, exchangeConsumer);
  }

  @Test
  void testToExchange() {
    ExchangeBinding<TestEvent> expected = new ExchangeBinding<>(TestEvent.class, "theExchange");

    assertEquals(expected, builder.toExchange("theExchange"));

    verify(exchangeConsumer).accept(expected);
  }

  @Test
  void testToQueue() {
    QueueBinding<TestEvent> expected = new QueueBinding<>(TestEvent.class, "theQueue");

    assertEquals(expected, builder.toQueue("theQueue"));

    verify(queueConsumer).accept(expected);
  }
}
