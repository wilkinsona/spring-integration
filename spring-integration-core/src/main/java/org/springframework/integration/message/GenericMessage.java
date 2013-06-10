/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.message;

import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;

/**
 * Base Message class defining common properties such as id, payload, and headers.
 * Once created this object is immutable.
 *
 * @author Mark Fisher
 */
public class GenericMessage<T> extends org.springframework.messaging.GenericMessage<T> implements Message<T> {

	private static final long serialVersionUID = 3649200745084232821L;

	private final MessageHeaders messageHeaders;

	/**
	 * Create a new message with the given payload.
	 *
	 * @param payload the message payload
	 */
	public GenericMessage(T payload) {
		super(payload);
		this.messageHeaders = new MessageHeaders(super.getHeaders());
	}

	/**
	 * Create a new message with the given payload. The provided map
	 * will be used to populate the message headers
	 *
	 * @param payload the message payload
	 * @param headers message headers
	 * @see MessageHeaders
	 */
	public GenericMessage(T payload, Map<String, Object> headers) {
		super(payload, headers);
		this.messageHeaders = new MessageHeaders(super.getHeaders());
	}

	@Override
	public MessageHeaders getHeaders() {
		return messageHeaders;
	}
}
