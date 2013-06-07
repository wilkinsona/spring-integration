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

	public WebSocketMessageDrivenEndpoint(MessageChannel outputChannel) {
		this.outputChannel = outputChannel;
	}

	@Override
	protected void doStart() {
	}

	@Override
	protected void doStop() {
	}

	@Override
	public void afterConnectionEstablished(WebSocketSession session) throws Exception {
	}

	@Override
	public void handleMessage(WebSocketSession session, WebSocketMessage<?> webSocketMessage) throws Exception {
		this.outputChannel.send(MessageBuilder.withPayload(webSocketMessage.getPayload()).setHeader("web-socket-session", session).build());
	}

	@Override
	public void handleTransportError(WebSocketSession session, Throwable exception) throws Exception {

	}

	@Override
	public void afterConnectionClosed(WebSocketSession session, CloseStatus closeStatus) throws Exception {

	}

	@Override
	public boolean supportsPartialMessages() {
		return false;
	}
}
