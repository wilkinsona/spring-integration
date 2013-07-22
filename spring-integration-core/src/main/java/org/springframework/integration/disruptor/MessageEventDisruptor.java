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

package org.springframework.integration.disruptor;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

import org.springframework.integration.Message;
import org.springframework.integration.MessageDeliveryException;
import org.springframework.util.Assert;
import org.springframework.util.ErrorHandler;

import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.dsl.Disruptor;

/**
 * A helper class to ease the use of an LMAX {@link Distruptor} and {@link RingBuffer}
 * to pass messages between threads.
 *
 * @author Andy Wilkinson
 *
 */
public final class MessageEventDisruptor {

	private final ExecutorService executor;

	private final Disruptor<MessageEvent> disruptor;

	private volatile RingBuffer<MessageEvent> ringBuffer;

	/**
	 * Creates a {@code MessageEventDisruptor} that will use a ring buffer with the given
	 * {@code bufferSize}.
	 *
	 * @param bufferSize The size of the ring buffer
	 */
	public MessageEventDisruptor(int bufferSize) {
		this.executor = Executors.newSingleThreadExecutor();
		this.disruptor = new Disruptor<MessageEvent>(new EventFactory<MessageEvent>() {
			@Override
			public MessageEvent newInstance() {
				return new MessageEvent();
			}

		}, bufferSize, executor);
	}

	/**
	 * Publishes the given message to the distruptor's ring buffer. Prior to publishing
	 * messages, the disruptor must have been {@link #start() started}. If the ring buffer
	 * is full a {@link MessageDeliveryException} is thrown.
	 *
	 * @param message The message to publish
	 */
	public void publish(Message<?> message) {
		long sequence;

		try{
			sequence = this.ringBuffer.tryNext();
		} catch (InsufficientCapacityException ice) {
			throw new MessageDeliveryException(message);
		}

		try {
			MessageEvent messageEvent = this.ringBuffer.get(sequence);
			messageEvent.setMessage(message);
		} finally {
			this.ringBuffer.publish(sequence);
		}
	}

	/**
	 * Initializes the disruptor to use the given {@code eventHandler} and {@code
	 * errorHandler}. The event handler will be called to handle every event that's
	 * {@link #publish(Message) published}. The error handler is called to handle any
	 * errors that occurs during event handling. To prevent further event processing
	 * the error handler may itself throw a {@link RuntimeException}.
	 *
	 * @param eventHandler the event handler to use. Must not be {@code null}.
	 * @param errorHandler the error handler to use. May be {@code null}.
	 *
	 * @see Disruptor#handleEventsWith(EventHandler...)
	 * @see Disruptor#handleExceptionsWith(ExceptionHandler)
	 */
	@SuppressWarnings("unchecked")
	public void init(EventHandler<MessageEvent> eventHandler, ErrorHandler errorHandler) {
		Assert.notNull(eventHandler, "'eventHandler' must not be null");
		this.disruptor.handleEventsWith(eventHandler);
		if (errorHandler != null) {
			this.disruptor.handleExceptionsWith(new ErrorHandlerExceptionHandler(errorHandler));
		}
	}

	/**
	 * Starts the disruptor, enabling it to {@link #publish(Message) publish} messages.
	 */
	public void start() {
		this.ringBuffer = this.disruptor.start();
	}

	/**
	 * Stops the disruptor
	 */
	public void stop() {
		this.disruptor.halt();
		this.executor.shutdown();

		try {
			this.executor.awaitTermination(30,  TimeUnit.SECONDS);
		} catch (InterruptedException e) {
			Thread.currentThread().interrupt();
		}
	}
}
