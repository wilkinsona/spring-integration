package org.springframework.integration.websocket;

import org.springframework.integration.MessageChannel;
import org.springframework.integration.endpoint.AbstractEndpoint;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.socket.CloseStatus;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketMessageDrivenEndpoint extends AbstractEndpoint implements WebSocketHandler {

	private final MessageChannel outputChannel;

	private final SessionManager sessionManager;

	public WebSocketMessageDrivenEndpoint(MessageChannel outputChannel, SessionManager sessionManager) {
		this.outputChannel = outputChannel;
		this.sessionManager = sessionManager;
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
		this.sessionManager.storeSession(session);
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
		this.outputChannel.send(MessageBuilder.withPayload(webSocketMessage.getPayload()).setHeader("sessionId", session.getId()).build());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {
		this.sessionManager.removeSession(session.getId());
	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
