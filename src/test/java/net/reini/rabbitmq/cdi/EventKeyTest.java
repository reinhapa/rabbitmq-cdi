package net.reini.rabbitmq.cdi;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;

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
