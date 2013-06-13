package org.springframework.integration.stomp;

import org.springframework.integration.core.SubscribableChannel;

public class SiToSpringSubscribableChannel extends SiToSpringMessageChannel<SubscribableChannel> implements org.springframework.messaging.SubscribableChannel {

	public SiToSpringSubscribableChannel(SubscribableChannel delegate) {
		super(delegate);
	}

	@Override
	public boolean subscribe(org.springframework.messaging.MessageHandler handler) {
		return getDelegate().subscribe(new SpringToSiMessageHandler(handler));
	}

	@Override
	public boolean unsubscribe(org.springframework.messaging.MessageHandler handler) {
		return getDelegate().unsubscribe(new SpringToSiMessageHandler(handler));
	}
}
