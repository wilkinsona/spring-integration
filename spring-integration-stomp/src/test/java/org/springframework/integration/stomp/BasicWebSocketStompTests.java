package org.springframework.integration.stomp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.websocket.TestMessageHandler;
import org.springframework.integration.websocket.WebSocketMessageDrivenEndpoint;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class BasicWebSocketStompTests {

	@Test
	public void connectCommand() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/basic.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);
		TestMessageHandler inputHandler = applicationContext.getBean(TestMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		when(session.getId()).thenReturn("sessionId");

		TextMessage textMessage = new TextMessage("CONNECT\naccept-version:1.2\nhost:stomp.github.org\n\n\0");

		endpoint.afterConnectionEstablished(session);
		endpoint.handleMessage(session, textMessage);

		Message<?> message = inputHandler.getMessage();
		StompHeaders stompHeaders = StompHeaders.fromMessageHeaders(message.getHeaders());

		assertEquals(StompCommand.CONNECT, stompHeaders.getStompCommand());

		assertEquals("1.2", stompHeaders.getAcceptVersion().iterator().next());
		assertEquals("stomp.github.org", stompHeaders.getExternalSourceHeaders().get("host").get(0));
		assertEquals(0, ((byte[])message.getPayload()).length);

		verify(session).sendMessage(any(BinaryMessage.class));
	}

	@Test
	public void sendCommand() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/basic.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);
		TestMessageHandler inputHandler = applicationContext.getBean(TestMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		when(session.getId()).thenReturn("sessionId");

		TextMessage textMessage = new TextMessage("SEND\ndestination:/queue/a\ncontent-type:text/plain\n\nhello queue a\0");

		endpoint.afterConnectionEstablished(session);
		endpoint.handleMessage(session, textMessage);

		Message<?> message = inputHandler.getMessage();
		StompHeaders headers = StompHeaders.fromMessageHeaders(message.getHeaders());

		assertEquals(StompCommand.SEND, headers.getStompCommand());

		assertEquals("/queue/a", headers.getDestination());
		assertEquals(MediaType.TEXT_PLAIN, headers.getContentType());
		assertEquals("hello queue a", new String((byte[])message.getPayload()));

		verify(session, times(0)).sendMessage(any(WebSocketMessage.class));
	}
}
