package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.MessagingException;

final class SiToSpringMessageHandler implements org.springframework.messaging.MessageHandler {

	private final MessageHandler delegate;

	SiToSpringMessageHandler(MessageHandler delegate) {
		this.delegate = delegate;
	}

	@Override
	public void handleMessage(org.springframework.messaging.Message<?> message) throws MessagingException {
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