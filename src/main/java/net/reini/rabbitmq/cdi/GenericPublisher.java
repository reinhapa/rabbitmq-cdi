package net.reini.rabbitmq.cdi;

import java.io.IOException;
import java.util.concurrent.TimeoutException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.Channel;
import com.rabbitmq.client.Connection;
import com.rabbitmq.client.ConnectionFactory;

import net.reini.rabbitmq.cdi.EventPublisher.PublisherConfiguration;

public class GenericPublisher implements MessagePublisher {
	private static final Logger LOGGER = LoggerFactory
			.getLogger(GenericPublisher.class);

	public static final int DEFAULT_RETRY_ATTEMPTS = 3;
	public static final int DEFAULT_RETRY_INTERVAL = 1000;

	private final ConnectionFactory connectionFactory;

	private Channel channel;

	public GenericPublisher(ConnectionFactory connectionFactory) {
		this.connectionFactory = connectionFactory;
		// TODO Auto-generated constructor stub
	}

	/**
	 * Initializes a channel if there is not already an open channel.
	 *
	 * @return The initialized or already open channel.
	 * @throws IOException
	 *             if the channel cannot be initialized
	 * @throws TimeoutException
	 */
	protected Channel provideChannel() throws IOException, TimeoutException {
		if (channel == null || !channel.isOpen()) {
			Connection connection = connectionFactory.newConnection();
			channel = connection.createChannel();
		}
		return channel;
	}

	/**
	 * Handles an IOException depending on the already used attempts to send a
	 * message. Also performs a soft reset of the currently used channel.
	 *
	 * @param attempt
	 *            Current attempt count
	 * @param ioException
	 *            The thrown exception
	 * @throws IOException
	 *             if the maximum amount of attempts is exceeded
	 */

	protected <T extends Exception> void handleIoException(int attempt,
			T ioException) throws T {
		if (channel != null && channel.isOpen()) {
			try {
				channel.close();
			} catch (IOException | TimeoutException e) {
				LOGGER.warn("Failed to close channel after failed publish", e);
			}
		}
		channel = null;
		if (attempt == DEFAULT_RETRY_ATTEMPTS) {
			throw ioException;
		}
		try {
			Thread.sleep(DEFAULT_RETRY_INTERVAL);
		} catch (InterruptedException e) {
			LOGGER.warn(
					"Sending message interrupted while waiting for retry attempt",
					e);
		}
	}

	@Override
	public void publish(Object event,
			PublisherConfiguration publisherConfiguration) throws IOException, TimeoutException {
		for (int attempt = 1; attempt <= DEFAULT_RETRY_ATTEMPTS; attempt++) {
			if (attempt > 1) {
				LOGGER.debug("Attempt {} to send message", attempt);
			}
			try {
				Channel channel = provideChannel();
				// TODO
				return;
			} catch (IOException e) {
				handleIoException(attempt, e);
			} catch (TimeoutException e) {
				handleIoException(attempt, e);
			}
		}
	}

	/**
	 * {@inheritDoc}
	 */
	@Override
	public void close() throws IOException, TimeoutException {
		if (channel == null) {
			LOGGER.warn(
					"Attempt to close a publisher channel that has not been initialized");
			return;
		} else if (!channel.isOpen()) {
			LOGGER.warn(
					"Attempt to close a publisher channel that has already been closed or is already closing");
			return;
		}
		LOGGER.debug("Closing publisher channel");
		channel.close();
		channel = null;
		LOGGER.debug("Successfully closed publisher channel");
	}
}
