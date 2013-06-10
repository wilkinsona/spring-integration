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
import org.springframework.web.messaging.stomp.StompMessage;
import org.springframework.web.messaging.stomp.support.StompHeaderMapper;
import org.springframework.web.messaging.stomp.support.StompMessageConverter;

/**
 * @author Andy Wilkinson
 */
public final class WebSocketToStompTransformer extends AbstractStompTransformer {

	private final StompMessageConverter stompMessageConverter = new StompMessageConverter();

	private final StompHeaderMapper stompHeaderMapper = new StompHeaderMapper();

	@Override
	public Message<?> transform(Message<?> message) {

		StompMessage stompMessage = this.stompMessageConverter.toStompMessage(message.getPayload());

		return MessageBuilder.withPayload(stompMessage.getPayload()) //
			.copyHeaders(message.getHeaders()) //
			.copyHeaders(stompHeaderMapper.toMessageHeaders(stompMessage.getHeaders())) //
			.setHeader(HEADER_COMMAND, stompMessage.getCommand()) //
			.build();
	}
}
