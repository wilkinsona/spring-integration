package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;

public final class TestMessageHandler implements MessageHandler {

	private Message<?> message;

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		this.message = message;
	}

	public Message<?> getMessage() {
		return this.message;
	}

}
