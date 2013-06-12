package org.springframework.integration.stomp.service;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;

public class MessageServiceMessageHandler implements MessageHandler {

	private final AbstractMessageService messageService;

	public MessageServiceMessageHandler(AbstractMessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {

		StompHeaders stompHeaders = StompHeaders.fromMessageHeaders(message.getHeaders());
		StompCommand stompCommand = stompHeaders.getStompCommand();

		if (StompCommand.SUBSCRIBE == stompCommand) {
			this.messageService.processSubscribe(message);
		} else if (StompCommand.SEND == stompCommand) {
			this.messageService.processMessage(message);
		} else if (StompCommand.CONNECT == stompCommand) {
			this.messageService.processConnect(message);
		} else if (StompCommand.MESSAGE == stompCommand) {
			this.messageService.processOther(message);
		} else if (StompCommand.DISCONNECT == stompCommand) {
			this.messageService.processDisconnect(message);
		} else {
			throw new MessagingException(message, "Unsupported STOMP command: " + stompCommand);
		}
	}
}
