package net.reini.rabbitmq.cdi;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.function.BiConsumer;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;
import javax.json.Json;
import javax.json.JsonObject;
import javax.json.JsonReader;
import javax.json.JsonValue;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class EventConsumer implements Consumer {
	private static final Logger LOGGER = LoggerFactory.getLogger(EventConsumer.class);

	private final Event<Object> eventControl;
	private final Instance<Object> eventPool;

	private volatile String consumerTag;

	EventConsumer(Event<Object> eventControl, Instance<Object> eventPool) {
		this.eventControl = eventControl;
		this.eventPool = eventPool;
	}

	/**
	 * Builds a CDI event from a message. The CDI event instance is retrieved
	 * from the injection container.
	 *
	 * @param messageBody
	 *            The message
	 * @return The CDI event
	 */
	Object buildEvent(byte[] messageBody) {
		Object event = eventPool.get();
		try (JsonReader jsonReader = Json.createReader(new ByteArrayInputStream(messageBody))) {
			JsonObject object = jsonReader.readObject();
			// TODO:rep add missing implementation
			LOGGER.error("Missing implementation for receiving event {} for consumer tag {}", event, consumerTag);

			object.forEach(new Converter(event));
		}
		return event;
	}

	static class Converter implements BiConsumer<String, JsonValue> {
		private final Object target;

		Converter(Object target) {
			this.target = target;
		}

		@Override
		public void accept(String key, JsonValue value) {
			LOGGER.debug("apply key={} with value={} on {}", key, value, target);
		}
	}

	@Override
	public void handleDelivery(String consumerTag, Envelope envelope, BasicProperties properties, byte[] body)
			throws IOException {
		Object event = buildEvent(body);
		if (event != null) {
			eventControl.fire(event);
		}
	}

	@Override
	public void handleConsumeOk(String consumerTag) {
		this.consumerTag = consumerTag;
	}

	@Override
	public void handleCancelOk(String consumerTag) {
	}

	@Override
	public void handleCancel(String consumerTag) throws IOException {
	}

	@Override
	public void handleShutdownSignal(String consumerTag, ShutdownSignalException sig) {
	}

	@Override
	public void handleRecoverOk(String consumerTag) {
	}
}