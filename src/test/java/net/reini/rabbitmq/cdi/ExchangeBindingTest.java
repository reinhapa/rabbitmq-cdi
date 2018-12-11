package net.reini.rabbitmq.cdi;

import static com.rabbitmq.client.MessageProperties.BASIC;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_TEXT_PLAIN;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

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
    Map<String, Object> headers = emptyMap();
    assertEquals(BASIC.builder().headers(headers).build(),
        binding.getBasicPropertiesBuilder().build());
    assertSame(binding, binding.withProperties(PERSISTENT_TEXT_PLAIN));
    assertEquals(PERSISTENT_TEXT_PLAIN.builder().headers(headers).build(),
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

  @Test
  void withHeader() {
    Object headerValue = new Object();
    assertEquals(BASIC.builder().headers(emptyMap()).build(),
        binding.getBasicPropertiesBuilder().build());
    assertSame(binding, binding.withHeader("header", headerValue));
    assertEquals(BASIC.builder().headers(singletonMap("header", headerValue)).build(),
        binding.getBasicPropertiesBuilder().build());
  }

  @Test
  void testToString() {
    assertEquals("ExchangeBinding[type=net.reini.rabbitmq.cdi.TestEvent, exchange=exchange]",
        binding.toString());
  }

  @Test
  void testHashCode() {
    assertEquals(Objects.hash(TestEvent.class, "exchange"), binding.hashCode());
  }
}

