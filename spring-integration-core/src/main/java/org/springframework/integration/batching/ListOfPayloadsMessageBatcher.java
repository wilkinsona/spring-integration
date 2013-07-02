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

package org.springframework.integration.batching;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * A message batcher that creates a message with a payload that is a list of all of the
 * payloads of the messages in the batch.
 *
 * @author Andy Wilkinson
 *
 */
public class ListOfPayloadsMessageBatcher implements MessageBatcher {

	private final HeaderMerger headerMerger;

	/**
	 * Creates a new list of payloads message batcher that will use {@code headerMerger}
	 * to merge the headers of the messages which it processes.
	 *
	 * @param headerMerger The header merger to use
	 */
	public ListOfPayloadsMessageBatcher(HeaderMerger headerMerger) {
		this.headerMerger = headerMerger;
	}

	@Override
	public Message<?> batchMessages(List<Message<?>> messages) {
		List<Object> payloads = new ArrayList<Object>(messages.size());

		for (Message<?> message: messages) {
			payloads.add(message.getPayload());
		}

		Map<String, Object> headers = this.headerMerger.mergeHeaders(messages);

		return MessageBuilder.withPayload(payloads).copyHeaders(headers).build();
	}

}
