/*
 * Copyright 2013 the original author or authors.
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

import org.springframework.context.Lifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.channel.MessagePublishingErrorHandler;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.disruptor.MessageEvent;
import org.springframework.integration.disruptor.MessageEventDisruptor;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.util.ErrorHandler;

import com.lmax.disruptor.EventHandler;

/**
 * A message handler that batches messages using a {@link RingBuffer}. A batch is released when the
 * configurable batch size is reached, or the buffer is empty. When a batch is released a message
 * containing a list of the payloads of each message in the batch is sent to the handler's
 * output channel.
 *
 * @author Andy Wilkinson
 *
 */
public final class BatchingMessageHandler extends AbstractMessageHandler implements Lifecycle {

	private final MessageEventDisruptor disruptor;

	private final EventHandler<MessageEvent> eventHandler;

	private volatile boolean running;

	/**
	 * Creates a new batching message handler. A buffer of the given {@code bufferSize} will be used
	 * to buffer messages that are to be batched. The batches that are created will contain up to
	 * {@code batchSize} messages. Each batch will be sent to the given {@code outputChannel}.
	 *
	 * @param bufferSize The size of the buffer
	 * @param batchSize The maximum size of each batch
	 * @param outputChannel The output channel to which each batched message is sent
	 */
	public BatchingMessageHandler(int bufferSize, int batchSize, MessageChannel outputChannel) {
		this.disruptor = new MessageEventDisruptor(bufferSize);
		this.eventHandler = new BatchingMessageEventHandler(batchSize, outputChannel);
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		disruptor.publish(message);
	}

	protected void onInit() {
		ErrorHandler errorHandler = new MessagePublishingErrorHandler(new BeanFactoryChannelResolver(this.getBeanFactory()));
		this.disruptor.init(this.eventHandler, errorHandler);
	}

	@Override
	public void start() {
		disruptor.start();
		this.running = true;
	}

	@Override
	public void stop() {
		disruptor.stop();
		this.running = false;
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	private final class BatchingMessageEventHandler implements EventHandler<MessageEvent> {

		private final MessagingTemplate messagingTemplate = new MessagingTemplate();

		private final List<Object> payloads = new ArrayList<Object>();

		private final int batchSize;

		private final MessageChannel outputChannel;

		public BatchingMessageEventHandler(int batchSize, MessageChannel outputChannel) {
			this.batchSize = batchSize;
			this.outputChannel = outputChannel;
		}

		@Override
		public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
			this.payloads.add(event.getMessage().getPayload());
			if (endOfBatch || payloads.size() == this.batchSize) {
				this.messagingTemplate.send(this.outputChannel, MessageBuilder.withPayload(payloads).build());
				this.payloads.clear();
			}
		}
	}

}
