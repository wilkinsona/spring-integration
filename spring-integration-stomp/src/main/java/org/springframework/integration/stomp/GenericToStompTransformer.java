package org.springframework.integration.stomp;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.transformer.Transformer;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;

public class GenericToStompTransformer implements Transformer {

	private final StompCommand defaultProtocolMessageType;

	public GenericToStompTransformer(StompCommand defaultProtocolMessageType) {
		this.defaultProtocolMessageType = defaultProtocolMessageType;
	}

	@Override
	public Message<?> transform(Message<?> message) {

		StompHeaders stompHeaders = new StompHeaders(message.getHeaders(), false);
		if (stompHeaders.getProtocolMessageType() == null) {
			stompHeaders.setProtocolMessageType(this.defaultProtocolMessageType);
		}

		// TODO Proper heartbeat handling
		if (stompHeaders.getHeartbeat() == null) {
			stompHeaders.setHeartbeat(0, 0);
		}

		if (stompHeaders.getProtocolMessageType() == StompCommand.MESSAGE) {
			// TODO Remove once using Rossen's update that handles this automatically
			if (stompHeaders.getMessageId() == null) {
				stompHeaders.setMessageId(message.getHeaders().getId().toString());
			}
			stompHeaders.setSubscriptionId((String)message.getHeaders().get("subscriptionId"));
		}

		// TODO need both raw and message headers to make sure all headers are retained. Is this the right approach?
		return MessageBuilder.withPayload(message.getPayload())
			.copyHeaders(stompHeaders.getRawHeaders())
			.copyHeaders(stompHeaders.getMessageHeaders())
			.build();
	}

}
