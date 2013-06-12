/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.stomp.service.method;

import org.springframework.core.MethodParameter;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.web.messaging.service.method.ArgumentResolver;

/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class MessageChannelArgumentResolver implements ArgumentResolver {

	private final MessageChannel messageChannel;

	public MessageChannelArgumentResolver(MessageChannel messageChannel) {
		Assert.notNull(messageChannel, "messageChannel is required");
		this.messageChannel = messageChannel;
	}

	@Override
	public boolean supportsParameter(MethodParameter parameter) {
		return MessageChannel.class.equals(parameter.getParameterType());
	}

	@Override
	public Object resolveArgument(MethodParameter parameter, final Message<?> inMessage) throws Exception {

		return new MessageChannel() {
			public boolean send(Message<?> outMessage) {
				org.springframework.integration.Message<?> tranformedOut = MessageBuilder.withPayload(outMessage.getPayload())
					.copyHeaders(outMessage.getHeaders())
					.setHeader("sessionId", inMessage.getHeaders().get("sessionId"))
					.build();
				return messageChannel.send(tranformedOut);
			}
		};
	}

}
