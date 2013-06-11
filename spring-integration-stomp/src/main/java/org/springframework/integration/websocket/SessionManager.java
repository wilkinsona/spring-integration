package org.springframework.integration.websocket;

import org.springframework.web.socket.WebSocketSession;

public interface SessionManager {

	void storeSession(WebSocketSession session);

	WebSocketSession retrieveSession(String sessionId);

	void removeSession(String sessionId);
}
