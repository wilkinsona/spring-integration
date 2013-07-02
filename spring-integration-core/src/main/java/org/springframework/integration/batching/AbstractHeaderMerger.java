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

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.MessageHeaders;

/**
 * Base class for HeaderMerger implementations to handles iterating over each messages
 * headers, and omitting ignored headers from the result.
 *
 * @author Andy Wilkinson
 *
 */
public abstract class AbstractHeaderMerger implements HeaderMerger {

	private static final List<String> IGNORED_HEADERS = Arrays.asList(
		MessageHeaders.ID,
		MessageHeaders.TIMESTAMP,
		MessageHeaders.SEQUENCE_SIZE,
		MessageHeaders.SEQUENCE_NUMBER
	);

	protected void doMergeHeaders(Collection<Message<?>> messages, HeaderMergeProcessor processor) {
		for (Message<?> message : messages) {
			MessageHeaders currentHeaders = message.getHeaders();
			for (Map.Entry<String, Object> header: currentHeaders.entrySet()) {
				if (!isIgnoredHeader(header.getKey())) {
					processor.processHeader(header.getKey(), header.getValue());
				}
			}
		}
	}

	private boolean isIgnoredHeader(String headerName) {
		return IGNORED_HEADERS.contains(headerName);
	}

	protected static interface HeaderMergeProcessor {
		void processHeader(String name, Object value);
	}
}
