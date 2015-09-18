package net.reini.rabbitmq.cdi;

import static org.junit.Assert.*;
import static org.mockito.Mockito.when;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

@RunWith(MockitoJUnitRunner.class)
public class EventConsumerTest {
    @Mock
    private Event<Object> eventControl;
    @Mock
    private Instance<Object> eventPool;
    private EventConsumer consumer;
    private TestEvent event;

    @Before
    public void setUp() throws Exception {
	consumer = new EventConsumer(TestEvent.class, eventControl, eventPool);
	event = new TestEvent();
    }

    @Test
    public void test() {
	byte[] messageBody = "{\"id\":\"theId\",\"booleanValue\":true}"
		.getBytes();

	when(eventPool.get()).thenReturn(event);

	TestEvent eventObject = (TestEvent) consumer.buildEvent(messageBody);
	assertEquals("theId", eventObject.getId());
	assertTrue(eventObject.isBooleanValue());
    }
}