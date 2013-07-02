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

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import org.springframework.integration.Message;

/**
 * A header merger that combines the headers of all of the messages. In the event of two
 * or more headers having the same name, the header from the first message that had it
 * will be included in the merged headers.
 *
 * @author Andy Wilkinson
 */
public final class FirstWinsHeaderMerger extends AbstractHeaderMerger {

	public Map<String, Object> mergeHeaders(Collection<Message<?>> messages) {
		final Map<String, Object> mergedHeaders = new HashMap<String, Object>();

		super.doMergeHeaders(messages, new HeaderMergeProcessor() {

			@Override
			public void processHeader(String name, Object value) {
				if (!mergedHeaders.containsKey(name)) {
					mergedHeaders.put(name, value);
				}
			}
		});

		return mergedHeaders;
	}
}
