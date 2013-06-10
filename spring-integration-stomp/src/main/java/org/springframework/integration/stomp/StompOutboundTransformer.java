/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.integration.stomp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.Charset;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.messaging.stomp.StompCommand;
import org.springframework.web.messaging.stomp.StompHeaders;
import org.springframework.web.messaging.stomp.StompMessage;
import org.springframework.web.messaging.stomp.support.StompHeaderMapper;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;

/**
 * @author Andy Wilkinson
 */
public final class StompOutboundTransformer extends AbstractStompTransformer {

	private final StompHeaderMapper stompHeaderMapper = new StompHeaderMapper();

	private final StompMessageConverter stompMessageConverter = new StompMessageConverter();

	@Override
	public Message<?> transform(Message<?> message) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		try {
			StompCommand command = (StompCommand) message.getHeaders().get(StompInboundTransformer.HEADER_COMMAND);

			StompHeaders headers = new StompHeaders();
			this.stompHeaderMapper.fromMessageHeaders(message.getHeaders(), headers);

			byte[] payload = getPayloadAsByteArray(message);

			outputStream.write(this.stompMessageConverter.fromStompMessage(new StompMessage(command, headers, payload)));
		}
		catch (IOException e) {
			throw new StompException("Failed to serialize " + message, e);
		}
		return MessageBuilder.withPayload(outputStream.toByteArray()).copyHeaders(message.getHeaders()).build();
	}

	private byte[] getPayloadAsByteArray(Message<?> message) {
		byte[] payload;

		if (message.getPayload() instanceof String) {
			payload = ((String)message.getPayload()).getBytes(Charset.forName("UTF-8"));
		} else if (message.getPayload() instanceof byte[]) {
			payload = (byte[])message.getPayload();
		} else {
			throw new IllegalArgumentException("Payload of 'message' must be a String or a byte[]");
		}
		return payload;
	}
}
