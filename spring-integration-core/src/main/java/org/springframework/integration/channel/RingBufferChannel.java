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

package org.springframework.integration.channel;

import org.springframework.context.Lifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.dispatcher.MessageDispatcher;
import org.springframework.integration.dispatcher.UnicastingDispatcher;
import org.springframework.integration.disruptor.MessageEvent;
import org.springframework.integration.disruptor.MessageEventDisruptor;
import org.springframework.integration.support.channel.BeanFactoryChannelResolver;
import org.springframework.util.ErrorHandler;

import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.RingBuffer;

/**
 * A message channel that uses a {@link RingBuffer} to buffer messages. Messages that
 * are sent to the channel are subsequently dispatched on a separate thread.
 *
 * @author Andy Wilkinson
 */
public final class RingBufferChannel extends AbstractSubscribableChannel implements Lifecycle {

	private final MessageDispatcher dispatcher = new UnicastingDispatcher();

	private final MessageEventDisruptor disruptor;

	private volatile boolean running;

	/**
	 * Creates a new RingBufferChannel that will use a ring buffer of the given {@code size} to
	 * buffer messages.
	 *
	 * @param size The size of the underlying ring buffer
	 */
	public RingBufferChannel(int size) {
		this.disruptor = new MessageEventDisruptor(size);
	}

	@Override
	protected boolean doSend(Message<?> message, long timeout) {
		this.disruptor.publish(message);
		return true;
	}

	@Override
	protected MessageDispatcher getDispatcher() {
		return this.dispatcher;
	}

	protected void onInit() {
		ErrorHandler errorHandler = new MessagePublishingErrorHandler(new BeanFactoryChannelResolver(this.getBeanFactory()));
		this.disruptor.init(new MessageEventHandler(getDispatcher()), errorHandler);
	}

	@Override
	public void start() {
		this.disruptor.start();
		this.running = true;
	}

	@Override
	public void stop() {
		this.running = false;
		this.disruptor.stop();
	}

	@Override
	public boolean isRunning() {
		return this.running;
	}

	private static final class MessageEventHandler implements EventHandler<MessageEvent> {

		private final MessageDispatcher dispatcher;

		public MessageEventHandler(MessageDispatcher dispatcher) {
			this.dispatcher = dispatcher;
		}

		@Override
		public void onEvent(MessageEvent event, long sequence, boolean endOfBatch) throws Exception {
			this.dispatcher.dispatch(event.getMessage());
		}
	}
}
