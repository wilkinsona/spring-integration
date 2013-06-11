package org.springframework.integration.stomp;

import java.util.Set;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;

public final class StompConnectHandlingChannelInterceptor extends ChannelInterceptorAdapter {

	private final MessageChannel outputChannel;

	public StompConnectHandlingChannelInterceptor(MessageChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		StompHeaders connectHeaders = new StompHeaders(message.getHeaders(), false);
		if (connectHeaders.getProtocolMessageType() == StompCommand.CONNECT) {

			StompHeaders connectedHeaders = new StompHeaders(StompCommand.CONNECTED);
			Set<String> acceptVersions = connectHeaders.getAcceptVersion();
			if (acceptVersions.contains("1.2")) {
				connectedHeaders.setVersion("1.2");
			}
			else if (acceptVersions.contains("1.1")) {
				connectedHeaders.setVersion("1.1");
			}
			else if (acceptVersions.isEmpty()) {
				// 1.0
			}
			connectedHeaders.setHeartbeat(0, 0);

			connectedHeaders.setSessionId(connectHeaders.getSessionId());

			Message<String> connectedMessage = MessageBuilder.withPayload("")
				.copyHeaders(connectedHeaders.getMessageHeaders())
				.build();

			this.outputChannel.send(connectedMessage);
		}
		return message;
	}
}
