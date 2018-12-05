package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertSame;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

class PublishExceptionTest {
  private Exception cause;

  @BeforeEach
  void prepare() {
    cause = new Exception("some cause");
  }

  @Test
  void testPublishExceptionThrowable() {
    PublishException exception = new PublishException(cause);

    assertEquals("java.lang.Exception: some cause", exception.getMessage());
    assertSame(cause, exception.getCause());
  }

  @Test
  void testPublishExceptionStringThrowable() {
    PublishException exception = new PublishException("the message", cause);

    assertEquals("the message", exception.getMessage());
    assertSame(cause, exception.getCause());
  }

}
