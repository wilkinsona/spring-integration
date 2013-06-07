package org.springframework.integration.stomp;

import java.util.Set;

import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.interceptor.ChannelInterceptorAdapter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.socket.WebSocketSession;

public final class StompConnectHandlingChannelInterceptor extends ChannelInterceptorAdapter {

	private final MessageChannel outputChannel;

	public StompConnectHandlingChannelInterceptor(MessageChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	@Override
	public Message<?> preSend(Message<?> message, MessageChannel channel) {
		if (message.getHeaders().get(StompInboundTransformer.HEADER_COMMAND) == StompCommand.CONNECT) {
			WebSocketSession session = (WebSocketSession) message.getHeaders().get("web-socket-session");
			StompHeaders connectHeaders = (StompHeaders) message.getHeaders().get(StompInboundTransformer.HEADER_HEADERS);

			StompHeaders connectedHeaders = new StompHeaders();
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

			Message<String> connectedMessage = MessageBuilder.withPayload("") //
			.setHeader("web-socket-session", session) //
			.setHeader(StompInboundTransformer.HEADER_HEADERS, connectedHeaders) //
			.setHeader(StompInboundTransformer.HEADER_COMMAND, StompCommand.CONNECTED).build();

			this.outputChannel.send(connectedMessage);
		}
		return message;
	}
}
