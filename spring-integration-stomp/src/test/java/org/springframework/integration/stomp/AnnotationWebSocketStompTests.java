package org.springframework.integration.stomp;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import org.junit.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.context.ApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;
import org.springframework.integration.websocket.WebSocketMessageDrivenEndpoint;
import org.springframework.messaging.Message;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;
import org.springframework.web.socket.TextMessage;
import org.springframework.web.socket.WebSocketMessage;
import org.springframework.web.socket.WebSocketSession;

public class AnnotationWebSocketStompTests {

	@Test
	public void subscribeWithNoResponse() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/annotation.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);

		AnnotatedMessageHandler handler = applicationContext.getBean(AnnotatedMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		when(session.getId()).thenReturn("sessionId");

		TextMessage textMessage = new TextMessage("SUBSCRIBE\nid:0\ndestination:/queue/alpha\nack:client\n\n\0");

		endpoint.afterConnectionEstablished(session);
		endpoint.handleMessage(session, textMessage);

		assertEquals(1, handler.alphaCount);

		verify(session, times(0)).sendMessage(any(WebSocketMessage.class));
	}

	@SuppressWarnings("rawtypes")
	@Test
	public void subscribeWithSingleResponse() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/annotation.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);

		AnnotatedMessageHandler handler = applicationContext.getBean(AnnotatedMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		when(session.getId()).thenReturn("sessionId");

		TextMessage textMessage = new TextMessage("SUBSCRIBE\nid:0\ndestination:/queue/bravo\nack:client\n\n\0");

		endpoint.afterConnectionEstablished(session);
		endpoint.handleMessage(session, textMessage);

		assertEquals(1, handler.bravoCount);

		ArgumentCaptor<TextMessage> messageCaptor = ArgumentCaptor.forClass(TextMessage.class);

		verify(session).sendMessage(messageCaptor.capture());

		TextMessage webSocketMessage = messageCaptor.getValue();

		StompMessageConverter stompMessageConverter = new StompMessageConverter();
		Message<byte[]> stompMessage = stompMessageConverter.toMessage(webSocketMessage.getPayload(),  null);
		assertEquals("response", new String(stompMessage.getPayload()));

		StompHeaders stompHeaders = new StompHeaders(stompMessage.getHeaders(), true);
		assertEquals(StompCommand.MESSAGE, stompHeaders.getProtocolMessageType());
		assertEquals("/queue/bravo", stompHeaders.getDestination());
		// TODO How do I access the subscription header?
		// assertEquals("0", stompHeaders.getSubscription());
		assertNotNull(stompHeaders.getMessageId());
	}

	@Test
	public void sendWithNoResponse() throws Exception {
		ApplicationContext applicationContext = new ClassPathXmlApplicationContext("/org/springframework/integration/stomp/annotation.xml");
		WebSocketMessageDrivenEndpoint endpoint = applicationContext.getBean(WebSocketMessageDrivenEndpoint.class);

		AnnotatedMessageHandler handler = applicationContext.getBean(AnnotatedMessageHandler.class);

		WebSocketSession session = mock(WebSocketSession.class);
		when(session.getId()).thenReturn("sessionId");

		TextMessage textMessage = new TextMessage("SEND\ndestination:/queue/alpha\n\n\0");

		endpoint.afterConnectionEstablished(session);
		endpoint.handleMessage(session, textMessage);

		assertEquals(1, handler.alphaCount);

		verify(session, times(0)).sendMessage(any(WebSocketMessage.class));
	}

}
