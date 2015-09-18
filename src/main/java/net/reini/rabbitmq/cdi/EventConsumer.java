package net.reini.rabbitmq.cdi;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class EventConsumer implements Consumer {
    private static final Logger LOGGER = LoggerFactory
	    .getLogger(EventConsumer.class);
    private static final ObjectMapper MAPPER = new ObjectMapper()
	    .disable(SerializationFeature.FAIL_ON_EMPTY_BEANS);

    private final Class<?> eventType;
    private final Event<Object> eventControl;
    private final Instance<Object> eventPool;

    EventConsumer(Class<?> eventType, Event<Object> eventControl,
	    Instance<Object> eventPool) {
	this.eventType = eventType;
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
	Object event;
	try {
	    event = MAPPER.readValue(messageBody, eventType);
	} catch (IOException e) {
	    LOGGER.error("Unable to read JSON event from message: "
		    .concat(new String(messageBody)), e);
	    event = eventPool.get();
	}
	return event;
    }

    @Override
    public void handleDelivery(String consumerTag, Envelope envelope,
	    BasicProperties properties, byte[] body) throws IOException {
	String contentType = properties.getContentType();
	if ("application/json".equals(contentType)) {
	    Object event = buildEvent(body);
	    eventControl.fire(event);
	} else {
	    LOGGER.error("Unable to process unknown message content type: {}",
		    contentType);
	}
    }

    @Override
    public void handleConsumeOk(String consumerTag) {
    }

    @Override
    public void handleCancelOk(String consumerTag) {
    }

    @Override
    public void handleCancel(String consumerTag) throws IOException {
    }

    @Override
    public void handleShutdownSignal(String consumerTag,
	    ShutdownSignalException sig) {
    }

    @Override
    public void handleRecoverOk(String consumerTag) {
    }
}