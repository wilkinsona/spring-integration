package org.springframework.integration.stomp;

import static org.junit.Assert.assertEquals;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.websocket.WebSocketMessageDrivenEndpoint;
import org.springframework.web.messaging.stomp.StompMessage;
import org.springframework.web.messaging.stomp.support.StompHeaderMapper;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;
import org.springframework.web.socket.BinaryMessage;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketSession;

public class AnnotationWebSocketStompTests {

	private final StompHeaderMapper stompHeaderMapper = new StompHeaderMapper();

	@Test
	public void subscribeWithNoResponse() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/annotation.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);

		AnnotatedMessageHandler handler = applicationContext.getBean(AnnotatedMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		TextMessage textMessage = new TextMessage("SUBSCRIBE\nid:0\ndestination:/queue/alpha\nack:client\n\n\0");

		endpoint.handleMessage(session, textMessage);

		assertEquals(1, handler.alphaCount);

		verifyNoMoreInteractions(session);
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void subscribeWithSingleResponse() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/annotation.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);

		AnnotatedMessageHandler handler = applicationContext.getBean(AnnotatedMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		TextMessage textMessage = new TextMessage("SUBSCRIBE\nid:0\ndestination:/queue/bravo\nack:client\n\n\0");

		endpoint.handleMessage(session, textMessage);

		assertEquals(1, handler.bravoCount);

		ArgumentCaptor<BinaryMessage> messageCaptor = ArgumentCaptor.forClass(BinaryMessage.class);

		verify(session).sendMessage(messageCaptor.capture());

		BinaryMessage webSocketMessage = messageCaptor.getValue();
		StompMessage stompMessage = new StompMessageConverter().toStompMessage(webSocketMessage.getByteArray());
		System.out.println(stompMessage);
	}

}
