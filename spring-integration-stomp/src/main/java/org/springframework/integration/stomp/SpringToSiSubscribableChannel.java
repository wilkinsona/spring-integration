package org.springframework.integration.stomp;

import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.core.SubscribableChannel;
import org.springframework.messaging.GenericMessage;

public class SpringToSiSubscribableChannel extends SpringToSiMessageChannel<org.springframework.messaging.SubscribableChannel> implements SubscribableChannel {

	public SpringToSiSubscribableChannel(org.springframework.messaging.SubscribableChannel delegate) {
		super(delegate);
	}

	@Override
	public boolean subscribe(final MessageHandler handler) {
		return getDelegate().subscribe(new SiToSpringMessageHandler(handler));
	}

	@Override
	public boolean unsubscribe(MessageHandler handler) {
		return getDelegate().unsubscribe(new SiToSpringMessageHandler(handler));
	}

}
