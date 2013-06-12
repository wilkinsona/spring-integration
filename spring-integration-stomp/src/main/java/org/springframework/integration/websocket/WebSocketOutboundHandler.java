package org.springframework.integration.websocket;

import java.io.IOException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public final class WebSocketOutboundHandler implements MessageHandler {

	private final Log logger = LogFactory.getLog(getClass());

	private final SessionManager sessionManager;

	public WebSocketOutboundHandler(SessionManager sessionManager) {
		this.sessionManager = sessionManager;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		String sessionId = (String)message.getHeaders().get("sessionId");

		WebSocketSession session = sessionManager.retrieveSession(sessionId);
		try {
			WebSocketMessage<?> webSocketMessage;
			Object payload = message.getPayload();
			if (payload instanceof byte[]) {
				// TODO AbstractSockJsSession can only handle TextMessages, but this may mangle the message
				webSocketMessage = new TextMessage(new String((byte[])payload));
			} else if (payload instanceof CharSequence) {
				webSocketMessage = new TextMessage((CharSequence)payload);
			} else {
				throw new IllegalArgumentException("The payload of 'message' must be byte[] or CharSequence");
			}
			if (session == null) {
				logger.warn("Session with id '" + sessionId + "' not available. Cannot send message: " + webSocketMessage);
			}
			session.sendMessage(webSocketMessage);
		} catch (IOException e) {
			throw new MessagingException(message, e);
		}
	}
}
