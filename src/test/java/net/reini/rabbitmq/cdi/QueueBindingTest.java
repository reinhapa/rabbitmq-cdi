package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertArrayEquals;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.util.ArrayList;
import java.util.List;
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
  void testAddDeclarations() {
    List<Declaration> expectedDeclarations=new ArrayList<>();
    expectedDeclarations.add(new QueueDeclaration("hello"));
    expectedDeclarations.add(new QueueDeclaration("hello2"));

    binding.withDeclarations(expectedDeclarations);
    List<Declaration> result = binding.getDeclarations();
    assertArrayEquals(expectedDeclarations.toArray(),result.toArray());
  }

  @Test
  void testAddDeclarationsWithArray() {
    QueueDeclaration declaration1 = new QueueDeclaration("hello");
    QueueDeclaration declaration2 = new QueueDeclaration("hello2");

    List<Declaration> expectedDeclarations=new ArrayList<>();
    expectedDeclarations.add(declaration1);
    expectedDeclarations.add(declaration2);

    binding.withDeclarations(declaration1, declaration2);
    List<Declaration> result = binding.getDeclarations();
    assertArrayEquals(expectedDeclarations.toArray(),result.toArray());
  }


  @Test
  void testToString() {
    assertEquals("QueueBinding[type=net.reini.rabbitmq.cdi.TestEvent, queue=queue]",
        binding.toString());
  }

  @Test
  void testHashCode() {
    assertEquals(Objects.hash(TestEvent.class, "queue"), binding.hashCode());
  }

  @Test
  void testEquals() {
    assertNotEquals(binding, null);
    assertNotEquals(binding, new Object());
    assertNotEquals(binding, new QueueBinding<>(Object.class, "queue"));
    assertNotEquals(binding, new QueueBinding<>(TestEvent.class, "queueX"));

    assertEquals(binding, binding);

    QueueBinding<TestEvent> binding1 = new QueueBinding<>(TestEvent.class, "queue");
    assertEquals(binding, binding1);
    assertEquals(binding1, binding);

    QueueBinding<TestEvent> binding2 = new QueueBinding<>(TestEvent.class, "queue");
    assertEquals(binding, binding2);

    assertEquals(binding1, binding1);
  }
}
