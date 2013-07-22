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

package org.springframework.integration.channel;

import org.springframework.context.SmartLifecycle;
import org.springframework.integration.Message;
import org.springframework.integration.dispatcher.LoadBalancingStrategy;
import org.springframework.integration.dispatcher.MessageDispatcher;
import org.springframework.integration.dispatcher.RoundRobinLoadBalancingStrategy;
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
public final class RingBufferChannel extends AbstractSubscribableChannel implements SmartLifecycle {

	private static final int DEFAULT_SIZE = 1024;

	private final UnicastingDispatcher dispatcher = new UnicastingDispatcher();

	private final MessageEventDisruptor disruptor;

	private final Object lifecycleMonitor = new Object();

	private boolean running;

	private volatile ErrorHandler errorHandler;

	/**
	 * Creates a new RingBufferChannel that will use a ring buffer containing 1024 slots
	 * to buffer messages.
	 */
	public RingBufferChannel() {
		this(DEFAULT_SIZE);
	}

	/**
	 * Creates a new RingBufferChannel that will use a ring buffer containing 1024 slots
	 * to buffer messages and the given {@code loadBalancingStrategy} when dispatching
	 * messages.
	 *
	 * @param loadBalancingStrategy the load-balancing strategy to use when dispatching
	 *                              messages
	 */
	public RingBufferChannel(LoadBalancingStrategy loadBalancingStrategy) {
		this(DEFAULT_SIZE, loadBalancingStrategy);
	}

	/**
	 * Creates a new RingBufferChannel that will use a ring buffer of the given {@code
	 * size} to buffer messages.
	 *
	 * @param size The size of the underlying ring buffer. Must be a power of two.
	 */
	public RingBufferChannel(int size) {
		this(size, new RoundRobinLoadBalancingStrategy());
	}

	/**
	 * Creates a new RingBufferChannel that will use a ring buffer of the given {@code
	 * size} to buffer messages and the given {@code loadBalancingStrategy} when
	 * dispatching messages.
	 *
	 * @param size The size of the underlying ring buffer. Must be a power of two.
	 * @param loadBalancingStrategy the load-balancing strategy to use when dispatching
	 *                              messages
	 */
	public RingBufferChannel(int size, LoadBalancingStrategy loadBalancingStrategy) {
		this.disruptor = new MessageEventDisruptor(size);
		this.dispatcher.setLoadBalancingStrategy(loadBalancingStrategy);
	}

	/**
	 * Provide an {@link ErrorHandler} strategy for handling Exceptions that occur
	 * downstream from this channel. If no ErrorHandler is provided the default
	 * strategy is a {@link MessagePublishingErrorHandler} that sends error messages to
	 * the failed request Message's error channel header if available or to the default
	 * 'errorChannel' otherwise.
	 *
	 * @param ErrorHandler the error handler to be used by this channel
	 */
	public void setErrorHandler(ErrorHandler errorHandler) {
		this.errorHandler = errorHandler;
	}

	/**
	 * Specify whether the channel's dispatcher should have failover enabled. By default,
	 * it will. Set this value to {@code false} to disable it.
	 *
	 * @param failover {@code true} if failure should be enabled, otherwise {@code false}.
	 */
	public void setFailover(boolean failover) {
		this.dispatcher.setFailover(failover);
	}

	/**
	 * Specify the maximum number of subscribers supported by the channel's dispatcher.
	 *
	 * @param maxSubscribers The maximum number of subscribers
	 */
	public void setMaxSubscribers(int maxSubscribers) {
		this.dispatcher.setMaxSubscribers(maxSubscribers);
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
		if (this.errorHandler == null) {
			this.errorHandler = new MessagePublishingErrorHandler(new BeanFactoryChannelResolver(this.getBeanFactory()));
		}
		this.disruptor.init(new MessageEventHandler(getDispatcher()), this.errorHandler);
	}

	@Override
	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.running) {
				this.disruptor.start();
				this.running = true;
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				this.disruptor.stop();
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
