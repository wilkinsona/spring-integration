package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

public class SiToSpringMessageChannel<T extends MessageChannel> implements org.springframework.messaging.MessageChannel {

	private final T delegate;

	public SiToSpringMessageChannel(T delegate) {
		this.delegate = delegate;
	}

	@Override
	public boolean send(org.springframework.messaging.Message<?> message) {
		return delegate.send(convertMessage(message));
	}

	@Override
	public boolean send(org.springframework.messaging.Message<?> message, long timeout) {
		return delegate.send(convertMessage(message), timeout);
	}

	private Message<?> convertMessage(org.springframework.messaging.Message<?> message) {
		return MessageBuilder.withPayload(message.getPayload()).copyHeaders(message.getHeaders()).build();
	}

	protected T getDelegate() {
		return delegate;
	}
}
