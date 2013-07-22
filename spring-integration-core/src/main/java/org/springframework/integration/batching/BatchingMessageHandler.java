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

import org.springframework.beans.factory.BeanFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.MessagePublishingErrorHandler;
import org.springframework.integration.core.MessageProducer;
import org.springframework.integration.core.MessagingTemplate;
import org.springframework.integration.disruptor.MessageEvent;
import org.springframework.integration.disruptor.MessageEventDisruptor;
import org.springframework.integration.handler.AbstractMessageHandler;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;

/**
 * A message handler that batches messages using a {@link RingBuffer}. A batch is
 * released when the configurable batch size is reached, or the buffer is empty. When a
 * batch is released the configured message batcher is used to create a message that
 * represents the batch. The default message batcher will create a message with a payload
 * that is a list of the payloads of all the messages in the batch.
 *
 * @author Andy Wilkinson
 *
 */
public final class BatchingMessageHandler extends AbstractMessageHandler implements SmartLifecycle, MessageProducer {

	private final MessagingTemplate messagingTemplate = new MessagingTemplate();

	private final MessageEventDisruptor disruptor;

	private final EventHandler<MessageEvent> eventHandler;

	private final Object lifecycleMonitor = new Object();

	private boolean running;

	private volatile MessageChannel outputChannel;

	private volatile MessageBatcher messageBatcher = new ListOfPayloadsMessageBatcher(new ConflictDiscardingHeaderMerger());

	/**
	 * Creates a new batching message handler. A buffer of the given {@code bufferSize}
	 * will be used to buffer messages that are to be batched. The batches that are
	 * created will contain up to {@code batchSize} messages.
	 *
	 * @param bufferSize The size of the buffer
	 * @param batchSize The maximum size of each batch
	 */
	public BatchingMessageHandler(int bufferSize, int batchSize) {
		this.disruptor = new MessageEventDisruptor(bufferSize);
		this.eventHandler = new BatchingMessageEventHandler(batchSize);
	}

	@Override
	protected void handleMessageInternal(Message<?> message) throws Exception {
		disruptor.publish(message);
	}

	protected void onInit() {
		ErrorHandler errorHandler = new MessagePublishingErrorHandler(new BeanFactoryChannelResolver(this.getBeanFactory()));
		this.disruptor.init(this.eventHandler, errorHandler);

		BeanFactory beanFactory = this.getBeanFactory();
		if (beanFactory != null) {
			this.messagingTemplate.setBeanFactory(beanFactory);
		}
	}

	public void setMessageBatcher(MessageBatcher messageBatcher) {
		Assert.notNull(messageBatcher, "'messageBatcher' must not be null");
		this.messageBatcher = messageBatcher;
	}

	@Override
	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.running) {
				disruptor.start();
				this.running = true;
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				disruptor.stop();
				this.running = false;
			}
		}
	}

	@Override
	public boolean isRunning() {
		synchronized (this.lifecycleMonitor) {
			return this.running;
		}
	}

	@Override
	public int getPhase() {
		return 0;
	}

	@Override
	public boolean isAutoStartup() {
		return true;
	}

	@Override
	public void stop(Runnable callback) {
		stop();
		callback.run();
	}

	@Override
	public void setOutputChannel(MessageChannel outputChannel) {
		Assert.notNull(outputChannel, "'outputChannel' must not be null");
		this.outputChannel = outputChannel;
	}

	private final class BatchingMessageEventHandler implements EventHandler<MessageEvent> {

		private final List<Message<?>> messages = new ArrayList<Message<?>>();

		private final int batchSize;

		public BatchingMessageEventHandler(int batchSize) {
			this.batchSize = batchSize;
		}

		@Override
		public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
			this.messages.add(event.getMessage());
			if (endOfBatch || this.messages.size() == this.batchSize) {
				try {
					Message<?> batchedMessage = messageBatcher.batchMessages(messages);
					Object replyChannel = getReplyChannel(batchedMessage);
					sendMessage(batchedMessage, replyChannel);
				} finally {
					this.messages.clear();
				}
			}
		}

		private void sendMessage(Message<?> message, Object channel) {
			if (channel instanceof MessageChannel) {
				messagingTemplate.send((MessageChannel) channel, (Message<?>) message);
			} else if (channel instanceof String) {
				messagingTemplate.send((String) channel, (Message<?>) message);
			} else {
				throw new MessagingException("replyChannel must be a MessageChannel or String");
			}
		}

		private Object getReplyChannel(Message<?> message) {
			Object replyChannel = outputChannel;
			if (replyChannel == null && message != null) {
				replyChannel = message.getHeaders().getReplyChannel();
			}
			Assert.notNull(replyChannel, "no outputChannel or replyChannel header available");
			return replyChannel;
		}
	}
}
