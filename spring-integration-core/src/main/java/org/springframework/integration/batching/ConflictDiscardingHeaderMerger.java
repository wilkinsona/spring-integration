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
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.integration.Message;


/**
 * A header merger implementation that merges the messages' headers together by
 * discarding any headers that are present in multiple messages with different values.
 * Headers that are only present in some messages but have the same value in each of
 * those messages are not discarded.
 *
 * @author Andy Wilkinson
 *
 */
public final class ConflictDiscardingHeaderMerger extends AbstractHeaderMerger {

	private final Log logger = LogFactory.getLog(getClass());

	public Map<String, Object> mergeHeaders(Collection<Message<?>> messages) {
		final Map<String, Object> mergedHeaders = new HashMap<String, Object>();
		final Set<String> conflictKeys = new HashSet<String>();

		super.doMergeHeaders(messages, new HeaderMergeProcessor() {

			@Override
			public void processHeader(String name, Object value) {
				if (!mergedHeaders.containsKey(name)) {
					mergedHeaders.put(name, value);
				}
				else if (!value.equals(mergedHeaders.get(name))) {
					conflictKeys.add(name);
				}
			}
		});

		for (String keyToRemove : conflictKeys) {
			if (logger.isDebugEnabled()) {
				logger.debug("Excluding header '" + keyToRemove + "' due to merge conflict");
			}
			mergedHeaders.remove(keyToRemove);
		}

		return mergedHeaders;
	}
}
