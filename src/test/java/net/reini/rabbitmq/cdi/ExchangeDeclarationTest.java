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
import static org.junit.jupiter.api.Assertions.assertNotEquals;

import org.junit.jupiter.api.Test;

import com.rabbitmq.client.BuiltinExchangeType;

class ExchangeDeclarationTest {

  private static final int EXPECTED_HASHCODE = -606493483;

  @Test
  void testExchangeType() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    ExchangeDeclaration result = sut.withType(BuiltinExchangeType.FANOUT);
    assertEquals(BuiltinExchangeType.FANOUT.getType(), result.getExchangeType());
  }

  @Test
  void testEquals() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withType("FANOUT");
    sut.withArgument("key", Long.valueOf(1));


    assertNotEquals(sut, null);
    assertNotEquals(sut, new Object());
    assertNotEquals(sut, new ExchangeDeclaration("hello2"));
    assertEquals(sut, sut);

    ExchangeDeclaration copy = new ExchangeDeclaration("hello");
    copy.withDurable(true);
    copy.withAutoDelete(true);
    copy.withType("FANOUT");
    copy.withArgument("key", Long.valueOf(1));

    assertEquals(sut, copy);

    copy.withDurable(false);
    assertNotEquals(sut, copy);
    copy.withDurable(true);
    assertEquals(sut, copy);

    copy.withAutoDelete(false);
    assertNotEquals(sut, copy);
    copy.withAutoDelete(true);
    assertEquals(sut, copy);

    copy.withType("DIRECT");
    assertNotEquals(sut, copy);
    copy.withType("FANOUT");
    assertEquals(sut, copy);

    copy.withArgument("test", "test");
    assertNotEquals(sut, copy);

    assertNotEquals(new ExchangeDeclaration("hello"), new ExchangeDeclaration("hello2"));
  }

  @Test
  void testHashCode() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withDurable(true);
    sut.withAutoDelete(true);
    sut.withType("FANOUT");
    sut.withArgument("key", Long.valueOf(1));
    int result = sut.hashCode();
    assertEquals(EXPECTED_HASHCODE,result);
  }

  @Test
  void testToString() {
    ExchangeDeclaration sut = new ExchangeDeclaration("hello");
    sut.withType(BuiltinExchangeType.DIRECT);
    sut.withAutoDelete(true);
    sut.withDurable(true);
    sut.withArgument("key","value");
    
    String result = sut.toString();
    System.out.println(result);
    assertEquals("exchange declaration for exchangeName='hello', exchangeType='direct', durable=true, autoDelete=true, arguments={key=value}",result);

  }
}
