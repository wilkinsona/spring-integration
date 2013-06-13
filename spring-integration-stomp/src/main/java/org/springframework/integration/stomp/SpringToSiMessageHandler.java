package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessagingException;

public final class SpringToSiMessageHandler implements MessageHandler {

	private final org.springframework.messaging.MessageHandler delegate;

	public SpringToSiMessageHandler(org.springframework.messaging.MessageHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		delegate.handleMessage(convertMessage(message));
	}

	@Override
	public int hashCode() {
		return super.hashCode();
	}

	@Override
	public boolean equals(Object obj) {
		return super.equals(obj);
	}

	private Message<?> convertMessage(org.springframework.messaging.Message<?> message) {
		return MessageBuilder.withPayload(message.getPayload()).copyHeaders(message.getHeaders()).build();
	}

}