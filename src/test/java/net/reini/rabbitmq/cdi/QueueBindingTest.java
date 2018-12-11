package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.Objects;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import net.reini.rabbitmq.cdi.EventBinder.QueueBinding;

@ExtendWith(MockitoExtension.class)
class QueueBindingTest {
  @Mock
  private Decoder<TestEvent> decoder;

  private QueueBinding<TestEvent> binding;

  @BeforeEach
  void prepare() {
    binding = new QueueBinding<>(TestEvent.class, "queue");
  }

  @Test
  void testGetEventType() {
    assertEquals(TestEvent.class, binding.getEventType());
  }

  @Test
  void testGetQueue() {
    assertEquals("queue", binding.getQueue());
  }

  @Test
  void testAutoAck() {
    assertFalse(binding.isAutoAck());
    assertSame(binding, binding.autoAck());
    assertTrue(binding.isAutoAck());
  }

  @Test
  void testGetDecoder() {
    assertEquals(JsonDecoder.class, binding.getDecoder().getClass());
    assertSame(binding, binding.withDecoder(decoder));
    assertEquals(decoder, binding.getDecoder());
  }

  @Test
  void testToString() {
    assertEquals("QueueBinding[type=net.reini.rabbitmq.cdi.TestEvent, queue=queue]", binding.toString());
  }

  @Test
  void testHashCode() {
    assertEquals(Objects.hash(TestEvent.class, "queue"), binding.hashCode());
  }
}
