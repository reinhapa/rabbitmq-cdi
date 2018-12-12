package net.reini.rabbitmq.cdi;

import static com.rabbitmq.client.MessageProperties.BASIC;
import static com.rabbitmq.client.MessageProperties.PERSISTENT_BASIC;
import static java.util.Collections.emptyMap;
import static java.util.Collections.singletonMap;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertSame;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.function.BiConsumer;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import com.rabbitmq.client.AMQP.BasicProperties;

import net.reini.rabbitmq.cdi.EventBinder.ExchangeBinding;

@ExtendWith(MockitoExtension.class)
class ExchangeBindingTest {
  @Mock
  private Encoder<TestEvent> encoder;
  @Mock
  private BiConsumer<TestEvent, PublishException> errorHandler;

  private BasicProperties basicProperties;
  private ExchangeBinding<TestEvent> binding;

  @BeforeEach
  void prepare() {
    basicProperties = BASIC.builder().headers(emptyMap()).build();
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
    assertEquals(basicProperties, binding.getBasicPropertiesBuilder().build());
    BasicProperties properties = PERSISTENT_BASIC.builder().headers(emptyMap()).build();
    assertEquals(emptyMap(), properties.getHeaders());
    assertSame(binding, binding.withProperties(properties));
    assertEquals(properties, binding.getBasicPropertiesBuilder().build());

    assertSame(binding, binding.withProperties(properties.builder().headers(null).build()));
    assertEquals(properties, binding.getBasicPropertiesBuilder().build());
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
    assertEquals(basicProperties, binding.getBasicPropertiesBuilder().build());
    assertSame(binding, binding.withHeader("header", headerValue));
    BasicProperties expected = BASIC.builder().headers(singletonMap("header", headerValue)).build();
    assertEquals(expected, binding.getBasicPropertiesBuilder().build());
  }

  @Test
  void withHeader_preserving_existing_headers() {
    BasicProperties properties =
        PERSISTENT_BASIC.builder().headers(singletonMap("oldheader", "oldvalue")).build();
    binding.withProperties(properties);
    assertSame(binding, binding.withHeader("header", "value"));
    Map<String, Object> expectedHeaders = new HashMap<>();
    expectedHeaders.put("oldheader", "oldvalue");
    expectedHeaders.put("header", "value");
    assertEquals(PERSISTENT_BASIC.builder().headers(expectedHeaders).build(),
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

  @Test
  void testEquals() {
    assertNotEquals(binding, null);
    assertNotEquals(binding, new Object());
    assertNotEquals(binding, new ExchangeBinding<>(Object.class, "exchange"));
    assertNotEquals(binding, new ExchangeBinding<>(TestEvent.class, "exchangeX"));

    assertEquals(binding, binding);

    ExchangeBinding<TestEvent> binding1 = new ExchangeBinding<>(TestEvent.class, "exchange");
    assertEquals(binding, binding1);
    assertEquals(binding1, binding);

    ExchangeBinding<TestEvent> binding2 = new ExchangeBinding<>(TestEvent.class, "exchange");
    assertEquals(binding, binding2);

    assertEquals(binding1, binding1);
  }
}
