package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.messaging.GenericMessage;


public class SpringToSiMessageChannel<T extends org.springframework.messaging.MessageChannel> implements MessageChannel {

	private final T delegate;

	public SpringToSiMessageChannel(T delegate) {
		this.delegate = delegate;
	}

	protected final T getDelegate() {
		return delegate;
	}

	@Override
	public boolean send(Message<?> message) {
		return delegate.send(convertMessage(message));
	}

	@Override
	public boolean send(Message<?> message, long timeout) {
		return delegate.send(convertMessage(message), timeout);
	}

	@SuppressWarnings({ "rawtypes", "unchecked" })
	private org.springframework.messaging.Message<?> convertMessage(Message<?> message) {
		return new GenericMessage(message.getPayload(), message.getHeaders());
	}
}
