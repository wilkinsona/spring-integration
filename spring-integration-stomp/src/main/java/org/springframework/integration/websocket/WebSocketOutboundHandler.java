package org.springframework.integration.websocket;

import java.io.IOException;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public final class WebSocketOutboundHandler implements MessageHandler {

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		WebSocketSession session = (WebSocketSession) message.getHeaders().get("web-socket-session");
		try {
			WebSocketMessage<?> webSocketMessage;
			Object payload = message.getPayload();
			if (payload instanceof byte[]) {
				webSocketMessage = new BinaryMessage((byte[])payload);
			} else if (payload instanceof CharSequence) {
				webSocketMessage = new TextMessage((CharSequence)payload);
			} else {
				throw new IllegalArgumentException("The payload of 'message' must be byte[] or CharSequence");
			}
			session.sendMessage(webSocketMessage);
		} catch (IOException e) {
			throw new MessagingException(message, e);
		}
	}
}
