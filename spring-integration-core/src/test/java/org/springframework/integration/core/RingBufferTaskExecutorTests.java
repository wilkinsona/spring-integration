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

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.Thread.State;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.TaskRejectedException;
import org.springframework.integration.core.RingBufferTaskExecutor.ExecutionStrategy;

import com.lmax.disruptor.YieldingWaitStrategy;
import com.lmax.disruptor.dsl.ProducerType;

/**
 * @author Andy Wilkinson
 */
public final class RingBufferTaskExecutorTests {

	@Test
	public void tasksAreExecuted() throws InterruptedException {
		RingBufferTaskExecutor taskExecutor = new RingBufferTaskExecutor();
		taskExecutor.start();

		try {
			final CountDownLatch latch = new CountDownLatch(1);

			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					latch.countDown();
				}
			});

			assertTrue(latch.await(30,  TimeUnit.SECONDS));
		} finally {
			taskExecutor.stop();
		}
	}

	@Test
	public void tasksAreStillExecutedAfterAnExecutionError() throws InterruptedException {
		RingBufferTaskExecutor taskExecutor = new RingBufferTaskExecutor();
		taskExecutor.start();

		try {
			taskExecutor.execute(new Runnable() {

				@Override
				public void run() {
					throw new RuntimeException();
				}
			});

			final CountDownLatch latch = new CountDownLatch(1);

			taskExecutor.execute(new Runnable() {
				@Override
				public void run() {
					latch.countDown();
				}
			});

			assertTrue(latch.await(30,  TimeUnit.SECONDS));
		} finally {
			taskExecutor.stop();
		}
	}

	@Test(expected=TaskRejectedException.class)
	public void rejectWhenFullThrowsATaskRejectedExceptionWhenBufferIsFull() {
		RingBufferTaskExecutor taskExecutor = new RingBufferTaskExecutor(1);
		taskExecutor.start();

		try {
			final CountDownLatch latch = new CountDownLatch(1);
			Runnable blockingTask = new Runnable() {

				@Override
				public void run() {
					try {
						latch.await();
					} catch (InterruptedException e) {
					}
				}
			};

			try {
				taskExecutor.execute(blockingTask);
				taskExecutor.execute(blockingTask);
			} finally {
				latch.countDown();
			}
		} finally {
			taskExecutor.stop();
		}
	}

	@Test
	public void blockWhenFullAllowsTaskToBeExecutedWhenBufferIsFull() throws InterruptedException {
		final RingBufferTaskExecutor taskExecutor = new RingBufferTaskExecutor(1,
																		       ProducerType.SINGLE,
																		       new YieldingWaitStrategy(),
																		       ExecutionStrategy.BLOCK_WHEN_FULL);
		taskExecutor.start();

		try {
			final CountDownLatch blockingLatch = new CountDownLatch(1);
			final CountDownLatch completionLatch = new CountDownLatch(2);

			final Runnable blockingTask = new Runnable() {
				@Override
				public void run() {
					try {
						blockingLatch.await();
						completionLatch.countDown();
					} catch (InterruptedException e) {
					}
				}
			};

			Thread executionThread = new Thread(new Runnable() {
				@Override
				public void run() {
					taskExecutor.execute(blockingTask);
					taskExecutor.execute(blockingTask);
				}
			});
			executionThread.start();

			long endTime = System.currentTimeMillis() + 5000;

			State state;

			do {
				state = executionThread.getState();
			} while (state != State.TIMED_WAITING && System.currentTimeMillis() < endTime);

			if (state != State.TIMED_WAITING) {
				fail("Thread did not reach TIMED_WAITING state within 5 seconds");
			}

			blockingLatch.countDown();

			completionLatch.await(30, TimeUnit.SECONDS);
		} finally {
			taskExecutor.stop();
		}
	}

	@Test
	public void isRunning() {
		RingBufferTaskExecutor executor = new RingBufferTaskExecutor();
		assertFalse(executor.isRunning());
		executor.start();
		assertTrue(executor.isRunning());
		executor.stop();
		assertFalse(executor.isRunning());
	}

	@Test
	public void isStartedAutomatically() {
		RingBufferTaskExecutor executor = new RingBufferTaskExecutor();
		assertTrue(executor.isAutoStartup());
	}
}
