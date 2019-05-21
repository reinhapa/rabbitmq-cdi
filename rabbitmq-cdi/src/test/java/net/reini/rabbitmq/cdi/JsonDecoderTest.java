package net.reini.rabbitmq.cdi;

import static org.hamcrest.CoreMatchers.containsString;
import static org.hamcrest.MatcherAssert.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonDecoderTest {
  private Decoder<TestEvent> decoder;

  @BeforeEach
  public void setUp() {
    decoder = new JsonDecoder<>(TestEvent.class);
  }

  @Test
  public void testDecode() throws DecodeException {
    byte[] messageBody = "{\"id\":\"theId\",\"booleanValue\":true}".getBytes();

    TestEvent eventObject = decoder.decode(messageBody);

    assertEquals("theId", eventObject.getId());
    assertTrue(eventObject.isBooleanValue());
  }


  @Test
  public void testDecode_withFailure() {
    byte[] messageBody = "illegal message".getBytes();

    Throwable exception = assertThrows(DecodeException.class, () -> {
      decoder.decode(messageBody);
    });
    assertThat(exception.getMessage(), containsString("Unrecognized token 'illegal'"));
  }

  @Test
  public void testWillDecode() {
    assertTrue(decoder.willDecode("application/json"));
  }
}
