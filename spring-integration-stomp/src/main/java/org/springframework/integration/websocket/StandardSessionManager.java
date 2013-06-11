package org.springframework.integration.websocket;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

import org.springframework.web.socket.WebSocketSession;

public class StandardSessionManager implements SessionManager {

	private final Map<String, WebSocketSession> sessions = new ConcurrentHashMap<String, WebSocketSession>();

	@Override
	public void storeSession(WebSocketSession session) {
		sessions.put(session.getId(),  session);
	}

	@Override
	public WebSocketSession retrieveSession(String sessionId) {
		return sessions.get(sessionId);
	}

	@Override
	public void removeSession(String sessionId) {
		sessions.remove(sessionId);
	}

}
