package org.springframework.integration.stomp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.http.MediaType;
import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;
import org.springframework.integration.websocket.TestMessageHandler;
import org.springframework.integration.websocket.WebSocketMessageDrivenEndpoint;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class WebSocketStompTests {

	@Test
	public void connectCommand() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/basic.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);
		TestMessageHandler inputHandler = applicationContext.getBean(TestMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		TextMessage textMessage = new TextMessage("CONNECT\naccept-version:1.2\nhost:stomp.github.org\n\n\0");

		endpoint.handleMessage(session, textMessage);

		Message<?> message = inputHandler.getMessage();
		MessageHeaders messageHeaders = message.getHeaders();

		assertEquals(StompCommand.CONNECT, messageHeaders.get("__stomp-command"));
		StompHeaders headers = (StompHeaders) messageHeaders.get("__stomp-headers");
		assertEquals("1.2", headers.getFirst("accept-version"));
		assertEquals("stomp.github.org", headers.getFirst("host"));
		assertEquals(0, ((byte[])message.getPayload()).length);

		verify(session).sendMessage(any(BinaryMessage.class));
	}

	@Test
	public void sendCommand() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/basic.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);
		TestMessageHandler inputHandler = applicationContext.getBean(TestMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		TextMessage textMessage = new TextMessage("SEND\ndestination:/queue/a\ncontent-type:text/plain\n\nhello queue a\0");

		endpoint.handleMessage(session, textMessage);

		Message<?> message = inputHandler.getMessage();
		MessageHeaders messageHeaders = message.getHeaders();

		assertEquals(StompCommand.SEND, messageHeaders.get("__stomp-command"));
		StompHeaders headers = (StompHeaders) messageHeaders.get("__stomp-headers");
		assertEquals("/queue/a", headers.getDestination());
		assertEquals(MediaType.TEXT_PLAIN, headers.getContentType());
		assertEquals("hello queue a", new String((byte[])message.getPayload()));

		verifyNoMoreInteractions(session);
	}

}
