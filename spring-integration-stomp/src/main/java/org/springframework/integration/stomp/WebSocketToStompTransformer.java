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

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.web.messaging.stomp.StompConversionException;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;

/**
 * @author Andy Wilkinson
 */
public final class WebSocketToStompTransformer extends AbstractStompTransformer {

	private final StompMessageConverter stompMessageConverter = new StompMessageConverter();

	@Override
	public Message<?> transform(Message<?> message) {

		try {
			org.springframework.messaging.Message<byte[]> stompMessage = this.stompMessageConverter.toMessage(message.getPayload(), (String)message.getHeaders().get("sessionId"));

			return MessageBuilder.withPayload(stompMessage.getPayload())
				.copyHeaders(stompMessage.getHeaders())
				.build();
		} catch (StompConversionException sce) {
			// TODO A heartbeat (a single EOL character) will cause a StompConversionException, but so will a corrupted frame.
			return null;
		}
	}
}
