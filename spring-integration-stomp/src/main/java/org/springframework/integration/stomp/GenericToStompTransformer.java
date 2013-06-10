package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;
import org.springframework.web.messaging.stomp.support.StompHeaderMapper;

public class GenericToStompTransformer implements Transformer {

	private final StompHeaderMapper stompHeaderMapper = new StompHeaderMapper();

	@Override
	public Message<?> transform(Message<?> message) {
		StompHeaders stompHeaders = new StompHeaders();
		this.stompHeaderMapper.fromMessageHeaders(message.getHeaders(), stompHeaders);

		Message<?> originalMessage = (Message<?>) message.getHeaders().get("originalMessage");

		StompHeaders originalStompHeaders = new StompHeaders();
		this.stompHeaderMapper.fromMessageHeaders(originalMessage.getHeaders(), originalStompHeaders);

		if (stompHeaders.getSubscription() == null) {
			stompHeaders.setSubscription(originalStompHeaders.getId());
		}

		if (stompHeaders.getDestination() == null) {
			stompHeaders.setDestination(originalStompHeaders.getDestination());
		}

		StompCommand command = (StompCommand) message.getHeaders().get("stompCommand");
		if (command == null) {
			command = StompCommand.MESSAGE;
		}

		return MessageBuilder.withPayload(message.getPayload())
			.copyHeaders(stompHeaderMapper.toMessageHeaders(stompHeaders))
			.setHeader("stompCommand",  command)
			.setHeader("web-socket-session", originalMessage.getHeaders().get("web-socket-session"))
			.build();
	}

}
