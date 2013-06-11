package org.springframework.integration.websocket;

import static org.junit.Assert.*;
import static org.mockito.Mockito.mock;

import org.junit.Test;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public final class WebSocketTests {

	@Test
	public void basicMessageHandling() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/websocket/basic.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);
		TestMessageHandler handler = applicationContext.getBean(TestMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		TextMessage message = new TextMessage("test");

		endpoint.handleMessage(session, message);

		assertNotNull(handler.getMessage());

		assertEquals("test", handler.getMessage().getPayload());
	}
}
