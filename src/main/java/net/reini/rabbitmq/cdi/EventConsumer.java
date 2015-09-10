package net.reini.rabbitmq.cdi;

import java.io.IOException;

import javax.enterprise.event.Event;
import javax.enterprise.inject.Instance;

import com.rabbitmq.client.AMQP.BasicProperties;
import com.rabbitmq.client.Consumer;
import com.rabbitmq.client.Envelope;
import com.rabbitmq.client.ShutdownSignalException;

public class EventConsumer implements Consumer {
	private final Event<Object> eventControl;
	private final Instance<Object> eventPool;
	
	private volatile String consumerTag;

	EventConsumer(Event<Object> eventControl, Instance<Object> eventPool) {
		this.eventControl = eventControl;
		this.eventPool = eventPool;
	}

	@Override
	public void handleDelivery(String arg0, Envelope arg1, BasicProperties arg2,
			byte[] arg3) throws IOException {
		// TODO Auto-generated method stub
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
	public void handleShutdownSignal(String consumerTag,
			ShutdownSignalException sig) {
	}

	@Override
	public void handleRecoverOk(String consumerTag) {
	}
}