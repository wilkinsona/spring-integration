/*
 * Copyright 2002-2010 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License"); you may not use this file except in compliance with
 * the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software distributed under the License is distributed on
 * an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations under the License.
 */

package org.springframework.integration.aggregator;

import java.util.Map;

import org.springframework.integration.Message;
import org.springframework.integration.batching.ConflictDiscardingHeaderMerger;
import org.springframework.integration.batching.HeaderMerger;
import org.springframework.integration.store.MessageGroup;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * Base class for MessageGroupProcessor implementations that aggregate the group of Messages into a single Message.
 *
 * @author Iwein Fuld
 * @author Alexander Peters
 * @author Mark Fisher
 * @author Dave Syer
 * @since 2.0
 */
public abstract class AbstractAggregatingMessageGroupProcessor implements MessageGroupProcessor {

	private final HeaderMerger headerMerger = new ConflictDiscardingHeaderMerger();

	public final Object processMessageGroup(MessageGroup group) {
		Assert.notNull(group, "MessageGroup must not be null");

		Map<String, Object> headers = this.aggregateHeaders(group);
		Object payload = this.aggregatePayloads(group, headers);
		MessageBuilder<?> builder;
		if (payload instanceof Message<?>) {
			builder = MessageBuilder.fromMessage((Message<?>) payload).copyHeadersIfAbsent(headers);
		}
		else {
			builder = MessageBuilder.withPayload(payload).copyHeadersIfAbsent(headers);
		}

		return builder.popSequenceDetails().build();
	}

	/**
	 * This default implementation simply returns all headers that have no conflicts among the group. An absent header
	 * on one or more Messages within the group is not considered a conflict. Subclasses may override this method with
	 * more advanced conflict-resolution strategies if necessary.
	 */
	protected Map<String, Object> aggregateHeaders(MessageGroup group) {
		return this.headerMerger.mergeHeaders(group.getMessages());
	}

	protected abstract Object aggregatePayloads(MessageGroup group, Map<String, Object> defaultHeaders);

}
