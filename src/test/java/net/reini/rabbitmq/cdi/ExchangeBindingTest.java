package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.MessageProperties;

import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;

@ExtendWith(MockitoExtension.class)
class ExchangeBindingTest {
  @Mock
  private Encoder<TestEvent> encoder;
  @Mock
  private BiConsumer<TestEvent, PublishException> errorHandler;

  private ExchangeBinding<TestEvent> binding;

  @BeforeEach
  void prepare() {
    binding = new ExchangeBinding<>(TestEvent.class, "exchange");
  }

  @Test
  void testGetEventType() {
    assertEquals(TestEvent.class, binding.getEventType());
  }

  @Test
  void testGetExchange() {
    assertEquals("exchange", binding.getExchange());
  }


  @Test
  void testGetRoutingKey() {
    assertEquals("", binding.getRoutingKey());
    assertSame(binding, binding.withRoutingKey("key"));
    assertEquals("key", binding.getRoutingKey());
  }

  @Test
  void withEncoder() {
    assertEquals(JsonEncoder.class, binding.getEncoder().getClass());
    assertSame(binding, binding.withEncoder(encoder));
    assertEquals(encoder, binding.getEncoder());
  }

  @Test
  void withProperties() {
    assertEquals(MessageProperties.BASIC, binding.getBasicPropertiesBuilder().build());
    assertSame(binding, binding.withProperties(MessageProperties.PERSISTENT_TEXT_PLAIN));
    assertEquals(MessageProperties.PERSISTENT_TEXT_PLAIN,
        binding.getBasicPropertiesBuilder().build());
  }

  @Test
  void withErrorHandler() {
    assertNotNull(binding.getErrorHandler());
    assertSame(binding, binding.withErrorHandler(errorHandler));
    assertEquals(errorHandler, binding.getErrorHandler());
    assertSame(binding, binding.withErrorHandler(null));
    assertNotEquals(errorHandler, binding.getErrorHandler());
  }
}

