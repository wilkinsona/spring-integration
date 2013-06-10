package org.springframework.integration.stomp.service;

import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.web.messaging.stomp.StompCommand;

public class MessageServiceMessageHandler implements MessageHandler {

	private final AbstractMessageService messageService;

	public MessageServiceMessageHandler(AbstractMessageService messageService) {
		this.messageService = messageService;
	}

	@Override
	public void handleMessage(Message<?> message) throws MessagingException {
		StompCommand stompCommand = (StompCommand) message.getHeaders().get("stompCommand");
		System.out.println(stompCommand);

		if (StompCommand.SUBSCRIBE == stompCommand) {
			this.messageService.processSubscribe(message);
		}
	}

}
