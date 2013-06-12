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
import org.springframework.messaging.GenericMessage;
import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.util.Assert;
import org.springframework.web.messaging.PubSubHeaders;
import org.springframework.web.messaging.service.method.ReturnValueHandler;


/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public class MessageReturnValueHandler implements ReturnValueHandler {

	private final MessageChannel replyChannel;

	public MessageReturnValueHandler(MessageChannel replyChannel) {
		this.replyChannel = replyChannel;
	}

	@Override
	public boolean supportsReturnType(MethodParameter returnType) {
		Class<?> paramType = returnType.getParameterType();
		return Message.class.isAssignableFrom(paramType);

//		if (Message.class.isAssignableFrom(paramType)) {
//			return true;
//		}
//		else if (List.class.isAssignableFrom(paramType)) {
//			Type type = returnType.getGenericParameterType();
//			if (type instanceof ParameterizedType) {
//				Type genericType = ((ParameterizedType) type).getActualTypeArguments()[0];
//			}
//		}
//		return Message.class.isAssignableFrom(paramType);
	}

	@Override
	public void handleReturnValue(Object returnValue, MethodParameter returnType, Message<?> message)
			throws Exception {

		Message<?> returnMessage = (Message<?>) returnValue;
		if (returnMessage == null) {
			return;
		}

		PubSubHeaders inHeaders = new PubSubHeaders(message.getHeaders(), true);
		String sessionId = inHeaders.getSessionId();
		String subscriptionId = inHeaders.getSubscriptionId();
		Assert.notNull(sessionId, "No session id: " + message);
		Assert.notNull(subscriptionId, "No subscription id: " + message);

		PubSubHeaders outHeaders = new PubSubHeaders(returnMessage.getHeaders(), false);
		outHeaders.setSessionId(sessionId);
		outHeaders.setSubscriptionId(subscriptionId);
		if (outHeaders.getDestination() == null) {
			// TODO This allows a custom destination to be specified. Is this too flexible?
			outHeaders.setDestination(inHeaders.getDestination());
		}

		returnMessage = new GenericMessage<Object>(returnMessage.getPayload(), outHeaders.getMessageHeaders());

		this.replyChannel.send(returnMessage);
 	}

}
