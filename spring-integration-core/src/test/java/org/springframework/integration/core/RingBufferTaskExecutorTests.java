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

import static org.junit.Assert.assertTrue;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.core.task.TaskRejectedException;

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
	public void taskRejectedExceptionIsThrownWhenBufferIsFull() {
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
}
