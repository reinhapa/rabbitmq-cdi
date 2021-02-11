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

import javax.enterprise.event.TransactionPhase;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Test the basic methods of {@link EventKey}.
 *
 * @author Patrick Reinhart
 */
class EventKeyTest {
  private EventKey<TestEvent> eventKey;

  @BeforeEach
  void prepare() {
    eventKey = EventKey.of(TestEvent.class, TransactionPhase.IN_PROGRESS);
  }

  /**
   * Test method for {@link EventKey#toString()}.
   */
  @Test
  void testToString() {
    assertEquals("EventKey[TestEvent, IN_PROGRESS]", eventKey.toString());
  }

  /**
   * Test method for {@link EventKey#hashCode()}.
   */
  @Test
  void testHashCode() {
    assertEquals(TestEvent.class.hashCode(), eventKey.hashCode());
  }

  /**
   * Test method for {@link EventKey#equals(Object)}.
   */
  @Test
  void testEqualsObject() {
    assertNotEquals(eventKey, null);
    assertNotEquals(eventKey, new Object());
    assertNotEquals(eventKey, EventKey.of(Object.class, TransactionPhase.IN_PROGRESS));
    assertNotEquals(eventKey, EventKey.of(TestEvent.class, TransactionPhase.AFTER_COMPLETION));

    assertEquals(eventKey, eventKey);

    EventKey<TestEvent> eventKey1 = EventKey.of(TestEvent.class, TransactionPhase.IN_PROGRESS);
    assertEquals(eventKey, eventKey1);
    assertEquals(eventKey1, eventKey);

    EventKey<TestEvent> eventKey2 = EventKey.of(TestEvent.class, TransactionPhase.IN_PROGRESS);
    assertEquals(eventKey, eventKey2);

    assertEquals(eventKey1, eventKey2);
    assertEquals(eventKey1.hashCode(), eventKey2.hashCode());
  }
}
