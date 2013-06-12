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

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.http.MediaType;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.message.GenericMessage;
import org.springframework.web.messaging.converter.CompositeMessageConverter;
import org.springframework.web.messaging.converter.MessageConverter;
import org.springframework.web.messaging.stomp.StompHeaders;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;

/**
 * @author Andy Wilkinson
 */
public final class StompToWebSocketTransformer extends AbstractStompTransformer {

	private final Log logger = LogFactory.getLog(getClass());

	private MessageConverter payloadConverter = new CompositeMessageConverter(null);

	private final StompMessageConverter stompMessageConverter = new StompMessageConverter();

	@Override
	public Message<?> transform(Message<?> message) {
		StompHeaders stompHeaders = StompHeaders.fromMessageHeaders(message.getHeaders());

		byte[] payload;
		try {
			MediaType contentType = stompHeaders.getContentType();
			payload = payloadConverter.convertToPayload(message.getPayload(), contentType);
		}
		catch (Exception e) {
			logger.error("Failed to send " + message, e);
			throw new MessagingException(message, e);
		}

		Map<String, Object> messageHeaders = new HashMap<String, Object>(stompHeaders.toMessageHeaders());

		// TODO Is this required?
		// messageHeaders.putAll(stompHeaders.getRawHeaders());

		Message<byte[]> byteMessage = new GenericMessage<byte[]>(payload, messageHeaders);
		byte[] bytes = stompMessageConverter.fromMessage(byteMessage);

		return new GenericMessage<byte[]>(bytes, messageHeaders);
	}
}
