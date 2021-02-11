/*
 * The MIT License (MIT)
 *
 * Copyright (c) 2015-2020 Patrick Reinhart
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in
 * all copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN
 * THE SOFTWARE.
 */
package net.reini.rabbitmq.cdi;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class JsonEncoderTest {
  private Encoder<TestEvent> encoder;

  @BeforeEach
  public void setUp() {
    encoder = new JsonEncoder<>();
  }

  @Test
  public void testEncode() throws EncodeException {
    TestEvent eventObject = new TestEvent();
    eventObject.setId("theId");
    eventObject.setBooleanValue(true);
    byte[] messageBody = encoder.encode(eventObject);

    assertEquals("{\"id\":\"theId\",\"booleanValue\":true}", new String(messageBody));
    assertTrue(eventObject.isBooleanValue());
  }

  @Test
  public void testEncode_with_error() {
    RuntimeException ex = new RuntimeException("some error");
    TestEvent eventObject = new TestEvent() {
      @Override
      public String getId() {
        throw ex;
      }
    };
    assertThrows(EncodeException.class, () -> {
      encoder.encode(eventObject);
    });
  }

  @Test
  public void testContentType() {
    assertEquals("application/json", encoder.contentType());
  }
}
