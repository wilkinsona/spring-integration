package org.springframework.integration.stomp;

import org.springframework.messaging.GenericMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.annotation.MessageMapping;
import org.springframework.stereotype.Controller;
import org.springframework.web.messaging.annotation.SubscribeEvent;

@Controller
public class AnnotatedMessageHandler {

	public int alphaCount = 0;

	public int bravoCount = 0;

	@SubscribeEvent("/queue/alpha")
	@MessageMapping("/queue/alpha")
	public void alpha() {
		alphaCount++;
	}

	@SubscribeEvent("/queue/bravo")
	public Message<?> bravo() {
		bravoCount++;
		return new GenericMessage<String>("response");
	}
}
