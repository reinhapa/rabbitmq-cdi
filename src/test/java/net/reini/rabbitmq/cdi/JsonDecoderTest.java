package net.reini.rabbitmq.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import org.junit.Before;
import org.junit.Test;

public class JsonDecoderTest {
  private Decoder<TestEvent> decoder;

  @Before
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


  @Test(expected = DecodeException.class)
  public void testDecode_withFailure() throws DecodeException {
    byte[] messageBody = "illegal message".getBytes();

    decoder.decode(messageBody);
  }

  @Test
  public void testWillDecode() {
    assertTrue(decoder.willDecode("application/json"));
  }
}
