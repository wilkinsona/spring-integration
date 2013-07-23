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

package org.springframework.integration.core;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.context.SmartLifecycle;
import org.springframework.core.task.TaskExecutor;
import org.springframework.core.task.TaskRejectedException;

import com.lmax.disruptor.BlockingWaitStrategy;
import com.lmax.disruptor.EventFactory;
import com.lmax.disruptor.EventHandler;
import com.lmax.disruptor.ExceptionHandler;
import com.lmax.disruptor.InsufficientCapacityException;
import com.lmax.disruptor.RingBuffer;
import com.lmax.disruptor.WaitStrategy;
import com.lmax.disruptor.dsl.Disruptor;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * A {@code TaskExecutor} implementation that uses an LMAX Disruptor {@link RingBuffer} to
 * queue tasks that are yet to be executed. A single thread is used to execute tasks.
 *
 * @author Andy Wilkinson
 */
public class RingBufferTaskExecutor implements TaskExecutor, SmartLifecycle {

	private final Log logger = LogFactory.getLog(RingBufferTaskExecutor.class);

	private final ExecutorService executor;

	private final Disruptor<TaskEvent> disruptor;

	private final Object lifecycleMonitor = new Object();

	private final Publisher publisher;

	private volatile RingBuffer<TaskEvent> ringBuffer;

	private boolean running;

	/**
	 * Creates a new RingBufferTaskExecutor that will use a ring buffer with 1024 slots
	 * to hold tasks that are waiting to be executed. The ring buffer will be configured
	 * with a producer type of {@link ProducerType#MULTI multi} and a {@link
	 * BlockingWaitStrategy blocking wait strategy}. The {@link
	 * ExecutionStrategy#REJECT_WHEN_FULL REJECT_WHEN_FULL} execution strategy will be
	 * used.
	 *
	 */
	public RingBufferTaskExecutor() {
		this(1024, ProducerType.MULTI, new BlockingWaitStrategy(), ExecutionStrategy.REJECT_WHEN_FULL);
	}

	/**
	 * Creates a new RingBufferTaskExecutor that will use a ring buffer of the given
	 * {@code size} to queue tasks. The ring buffer will be configured with a producer
	 * type of {@link ProducerType#MULTI multi} and a {@link BlockingWaitStrategy blocking
	 * wait strategy}. The {@link ExecutionStrategy#REJECT_WHEN_FULL REJECT_WHEN_FULL}
	 * execution strategy will be used.
	 *
	 * @param size The size of the ring buffer. Must be a power of two.
	 */
	public RingBufferTaskExecutor(int size) {
		this(size, ProducerType.MULTI, new BlockingWaitStrategy(), ExecutionStrategy.REJECT_WHEN_FULL);
	}

	/**
	 * Creates a new RingBufferTaskExecutor that will use a ring buffer of the given
	 * {@code size} to queue tasks. The ring buffer will be configured with the given
	 * {@code producerType} and {@code waitStrategy}. The given {@code
	 * executionStrategy} will be used to determine how task execution is handled when
	 * the ring buffer is full.
	 *
	 * @param size The size of the ring buffer. Must be a power of two.
	 * @param producerType The ring buffer's producer type
	 * @param waitStrategy The wait strategy for the ring buffer to use
	 * @param executionStrategy The execution strategy to use
	 */
	@SuppressWarnings("unchecked")
	public RingBufferTaskExecutor(int size, ProducerType producerType, WaitStrategy waitStrategy, ExecutionStrategy executionStrategy) {
		this.executor = Executors.newFixedThreadPool(1);
		this.disruptor = new Disruptor<TaskEvent>(new TaskEventFactory(), size, executor, producerType, waitStrategy);
		this.disruptor.handleExceptionsWith(new LoggingExceptionHandler());
		this.disruptor.handleEventsWith(new TaskEventHandler());

		switch (executionStrategy) {
			case BLOCK_WHEN_FULL: {
				this.publisher = new BlockWhenFullPublisher();
				break;
			}
			case REJECT_WHEN_FULL: {
				this.publisher = new RejectWhenFullPublisher();
				break;
			}
			default: {
				throw new IllegalArgumentException("Unsupported publishing strategy: " + executionStrategy);
			}
		}
	}

	/**
	 * Submits the given {@code task} for execution at some time in the future. If the
	 * underlying ring buffer is full, the behavior of this method is determined by the
	 * executor's {@link ExecutionStrategy}. For example, the call may block until
	 * capacity is available or a {@link TaskRejectedException} may be thrown.
	 *
	 * @param task the <code>Runnable</code> to execute. Must not be {@code null}.
	 *
	 * @throws TaskRejectedException if the buffer is full and the execution strategy
	 *                               indicates that the task should be rejected
	 */
	@Override
	public void execute(Runnable task) {
		this.publisher.publish(task, this.ringBuffer);
	}

	@Override
	public void start() {
		synchronized (this.lifecycleMonitor) {
			if (!this.running) {
				this.ringBuffer = this.disruptor.start();
				this.running = true;
			}
		}
	}

	@Override
	public void stop() {
		synchronized (this.lifecycleMonitor) {
			if (this.running) {
				this.disruptor.halt();
				this.executor.shutdown();
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

	private static final class TaskEvent {

		private volatile Runnable task;

		public String toString() {
			Runnable localTask = task;
			if (localTask != null) {
				return localTask.toString();
			} else {
				return super.toString();
			}
		}
	}

	private static final class TaskEventFactory implements EventFactory<TaskEvent> {
		@Override
		public TaskEvent newInstance() {
			return new TaskEvent();
		}
	}

	private static final class TaskEventHandler implements EventHandler<TaskEvent> {

		@Override
		public void onEvent(TaskEvent event, long sequence, boolean endOfBatch) throws Exception {
			event.task.run();
		}
	}

	private final class LoggingExceptionHandler implements ExceptionHandler {


		@Override
		public void handleEventException(Throwable ex, long sequence, Object event) {
			logger.warn("Handling of event '" + event + "' failed", ex);

		}

		@Override
		public void handleOnStartException(Throwable ex) {
			logger.warn("Start failure", ex);
		}

		@Override
		public void handleOnShutdownException(Throwable ex) {
			logger.warn("Shutdown failure", ex);
		}
	}

	/**
	 * An enumeration of the possible behaviors of a {@link RingBufferTaskExecutor} when
	 * an attempt is made to execute a task and the ring buffer is full.
	 *
	 * @author Andy Wilkinson
	 *
	 */
	public enum ExecutionStrategy {
		/**
		 * The calling thread will be blocked until a slot is available in the ring buffer
		 * to hold the task that is to be executed.
		 */
		BLOCK_WHEN_FULL,
		/**
		 * A {@link TaskRejectedException} will be thrown if an attempt is made to execute
		 * a task when the ring buffer is full.
		 */
		REJECT_WHEN_FULL;
	}

	private static interface Publisher {
		void publish(Runnable task, RingBuffer<TaskEvent> ringBuffer);
	}

	private static abstract class AbstractPublisher implements Publisher {

		@Override
		public final void publish(Runnable task, RingBuffer<TaskEvent> ringBuffer) {
			long sequence = getNextSequence(ringBuffer);
			try {
				ringBuffer.get(sequence).task = task;
			} finally {
				ringBuffer.publish(sequence);
			}
		}

		protected abstract long getNextSequence(RingBuffer<?> ringBuffer);

	}

	private static final class BlockWhenFullPublisher extends AbstractPublisher {

		@Override
		protected long getNextSequence(RingBuffer<?> ringBuffer) {
			return ringBuffer.next();
		}
	}

	private static final class RejectWhenFullPublisher extends AbstractPublisher {

		@Override
		protected long getNextSequence(RingBuffer<?> ringBuffer) {
			try {
				return ringBuffer.tryNext();
			} catch (InsufficientCapacityException ice) {
				throw new TaskRejectedException("The ring buffer is at capacity", ice);
			}
		}
	}
}
