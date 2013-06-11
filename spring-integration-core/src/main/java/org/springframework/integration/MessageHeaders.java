/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.integration;

import java.util.Map;

/**
 * The headers for a {@link Message}.<br>
 * IMPORTANT: MessageHeaders are immutable. Any mutating operation (e.g., put(..), putAll(..) etc.)
 * will result in {@link UnsupportedOperationException}
 * To create MessageHeaders instance use fluent MessageBuilder API
 * <pre>
 * MessageBuilder.withPayload("foo").setHeader("key1", "value1").setHeader("key2", "value2");
 * </pre>
 * or create an instance of GenericMessage passing payload as {@link Object} and headers as a regular {@link Map}
 * <pre>
 * Map headers = new HashMap();
 * headers.put("key1", "value1");
 * headers.put("key2", "value2");
 * new GenericMessage("foo", headers);
 * </pre>
 *
 * @author Arjen Poutsma
 * @author Mark Fisher
 * @author Oleg Zhurakousky
 * @author Gary Russell
 */
public final class MessageHeaders extends org.springframework.messaging.MessageHeaders {

	private static final long serialVersionUID = 6901029029524535147L;

	/**
	 * The key for the Message ID. This is an automatically generated UUID and
	 * should never be explicitly set in the header map <b>except</b> in the
	 * case of Message deserialization where the serialized Message's generated
	 * UUID is being restored.
	 */
	public static final String ID = org.springframework.messaging.MessageHeaders.ID;

	public static final String TIMESTAMP = org.springframework.messaging.MessageHeaders.TIMESTAMP;

	public static final String CORRELATION_ID = "correlationId";

	public static final String REPLY_CHANNEL = "replyChannel";

	public static final String ERROR_CHANNEL = "errorChannel";

	public static final String EXPIRATION_DATE = "expirationDate";

	public static final String PRIORITY = "priority";

	public static final String SEQUENCE_NUMBER = "sequenceNumber";

	public static final String SEQUENCE_SIZE = "sequenceSize";

	public static final String SEQUENCE_DETAILS = "sequenceDetails";

	public static final String CONTENT_TYPE = "contentType";

	public static final String POSTPROCESS_RESULT = "postProcessResult";

	public MessageHeaders(Map<String, Object> headers) {
		super(headers);
	}

	public Long getExpirationDate() {
		return this.get(EXPIRATION_DATE, Long.class);
	}

	public Object getCorrelationId() {
		return this.get(CORRELATION_ID);
	}

	public Integer getSequenceNumber() {
		Integer sequenceNumber = this.get(SEQUENCE_NUMBER, Integer.class);
		return (sequenceNumber != null ? sequenceNumber : 0);
	}

	public Integer getSequenceSize() {
		Integer sequenceSize = this.get(SEQUENCE_SIZE, Integer.class);
		return (sequenceSize != null ? sequenceSize : 0);
	}

	public Integer getPriority() {
		return this.get(PRIORITY, Integer.class);
	}

	public Object getReplyChannel() {
        return this.get(REPLY_CHANNEL);
    }

    public Object getErrorChannel() {
        return this.get(ERROR_CHANNEL);
    }

	public static interface IdGenerator extends org.springframework.messaging.MessageHeaders.IdGenerator {

	}
}
