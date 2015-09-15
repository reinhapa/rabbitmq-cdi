package net.reini.rabbitmq.cdi;

import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.reini.rabbitmq.cdi.EventPublisher.PublisherConfiguration;

@RunWith(MockitoJUnitRunner.class)
public class GenericPublisherTest {
	@Mock
	private ConnectionFactory connectionFactory;
	@Mock
	private Connection connection;
	@Mock
	private Channel channel;

	private GenericPublisher publisher;
	private TestEvent event;

	@Before
	public void setUp() throws Exception {
		publisher = new GenericPublisher(connectionFactory);
		event = new TestEvent();
		event.id = "theId";
		event.booleanValue = true;
	}

	@Test
	public void test() throws Exception {
		BasicProperties props = new BasicProperties();
		PublisherConfiguration publisherConfiguration = new PublisherConfiguration("exchange", "routingKey",
				Boolean.FALSE, props);

		when(connectionFactory.newConnection()).thenReturn(connection);
		when(connection.createChannel()).thenReturn(channel);

		publisher.publish(event, publisherConfiguration);
		
		verify(channel).basicPublish("exchange", "routingKey", props, "{\"id\":\"theId\",\"booleanValue\":true}".getBytes());
	}
}