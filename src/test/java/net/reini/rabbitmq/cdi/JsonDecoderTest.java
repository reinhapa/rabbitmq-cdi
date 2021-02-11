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
