package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;

public class GenericToStompTransformer implements Transformer {

	private final StompCommand defaultStompCommand;

	public GenericToStompTransformer(StompCommand defaultStompCommand) {
		this.defaultStompCommand = defaultStompCommand;
	}

	@Override
	public Message<?> transform(Message<?> message) {

		StompHeaders stompHeaders = StompHeaders.fromMessageHeaders(message.getHeaders());
		stompHeaders.setStompCommandIfNotSet(this.defaultStompCommand);

		StompCommand stompCommand = stompHeaders.getStompCommand();

		// TODO Proper heartbeat handling
		if (stompCommand == StompCommand.CONNECTED || stompCommand == StompCommand.CONNECT) {
			stompHeaders.setHeartbeat(0, 0);
		}

		return MessageBuilder.withPayload(message.getPayload())
			.copyHeaders(stompHeaders.toMessageHeaders())
			.build();
	}
}
