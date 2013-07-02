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

import java.util.Map;

import org.junit.Test;

/**
 * @author Andy Wilkinson
 */
public final class LastWinsHeaderMergerTests extends AbstractHeaderMergerTests {

	public LastWinsHeaderMergerTests() {
		super(new LastWinsHeaderMerger());
	}

	@Test
	public void lastHeaderWinsWhenHeadersConflict() {
		Map<String, Object> mergedHeaders = getMergedConflictingHeaders();
		assertNotNull(mergedHeaders);
		assertEquals(2, mergedHeaders.size());
		assertEquals("value3", mergedHeaders.get("k1"));
		assertEquals(4, mergedHeaders.get("k2"));
	}

}