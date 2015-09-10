package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import net.reini.rabbitmq.cdi.EventPublisher.PublisherConfiguration;

public interface MessagePublisher extends AutoCloseable {

	void publish(Object event, PublisherConfiguration publisherConfiguration)
			throws IOException, TimeoutException;

	/**
	 * Closes the publisher by closing its underlying channel.
	 * 
	 * @throws IOException
	 *             if the channel cannot be closed correctly. Usually occurs
	 *             when the channel is already closing or is already closed.
	 * @throws TimeoutException
	 *             if a timeout occurs while closing the publisher.
	 */
	@Override
	void close() throws IOException, TimeoutException;
}
