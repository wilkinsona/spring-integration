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

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Andy Wilkinson
 */
public abstract class AbstractHeaderMergerTests {

	private final HeaderMerger headerMerger;

	protected AbstractHeaderMergerTests(HeaderMerger headerMerger) {
		this.headerMerger = headerMerger;
	}

	@Test
	public final void matchingHeadersAreIncluded() {
		Map<String, Object> headers = new HashMap<String, Object>();
		headers.put("k1", "value1");
		headers.put("k2", new Integer(2));

		Message<?> message1 = MessageBuilder.withPayload("").copyHeaders(headers).build();
		Message<?> message2 = MessageBuilder.withPayload("").copyHeaders(headers).build();

		Map<String, Object> mergedHeaders = headerMerger.mergeHeaders(Arrays.<Message<?>>asList(message1, message2));
		assertNotNull(mergedHeaders);
		assertEquals(2, mergedHeaders.size());
		assertEquals("value1", mergedHeaders.get("k1"));
		assertEquals(2, mergedHeaders.get("k2"));
	}

	@Test
	public final void disjointHeadersAreIncluded() {
		Map<String, Object> headers1 = new HashMap<String, Object>();
		headers1.put("k1", "value1");
		headers1.put("k2", new Integer(2));
		Message<?> message1 = MessageBuilder.withPayload("").copyHeaders(headers1).build();

		Map<String, Object> headers2 = new HashMap<String, Object>();
		headers2.put("k3", "value3");
		headers2.put("k4", new Integer(4));
		Message<?> message2 = MessageBuilder.withPayload("").copyHeaders(headers2).build();

		Map<String, Object> mergedHeaders = headerMerger.mergeHeaders(Arrays.<Message<?>>asList(message1, message2));
		assertNotNull(mergedHeaders);
		assertEquals(4, mergedHeaders.size());
		assertEquals("value1", mergedHeaders.get("k1"));
		assertEquals(2, mergedHeaders.get("k2"));
		assertEquals("value3", mergedHeaders.get("k3"));
		assertEquals(4, mergedHeaders.get("k4"));
	}

	protected Map<String, Object> getMergedConflictingHeaders() {
		Map<String, Object> headers1 = new HashMap<String, Object>();
		headers1.put("k1", "value1");
		headers1.put("k2", new Integer(2));
		Message<?> message1 = MessageBuilder.withPayload("").copyHeaders(headers1).build();

		Map<String, Object> headers2 = new HashMap<String, Object>();
		headers2.put("k1", "value3");
		headers2.put("k2", new Integer(4));
		Message<?> message2 = MessageBuilder.withPayload("").copyHeaders(headers2).build();

		return headerMerger.mergeHeaders(Arrays.<Message<?>>asList(message1, message2));
	}
}
