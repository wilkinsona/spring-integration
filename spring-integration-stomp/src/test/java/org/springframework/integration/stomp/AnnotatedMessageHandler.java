package org.springframework.integration.stomp;

import java.util.Arrays;
import java.util.List;

import org.springframework.messaging.GenericMessage;
import org.springframework.messaging.Message;
import org.springframework.stereotype.Controller;
import org.springframework.web.messaging.annotation.SubscribeEvent;

@Controller
public class AnnotatedMessageHandler {

	public int alphaCount = 0;

	public int bravoCount = 0;

	public int charlieCount = 0;

	@SubscribeEvent("/queue/alpha")
	public void alpha() {
		alphaCount++;
	}

	@SubscribeEvent("/queue/bravo")
	public Message<?> bravo() {
		bravoCount++;
		return new GenericMessage<String>("response");
	}

	@SubscribeEvent("/queue/charlie")
	public List<String> charlie() {
		charlieCount++;
		return Arrays.asList("response-1", "response-2");
	}

}
