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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessageChannel;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Andy Wilkinson
 */
public final class BatchingMessageHandlerTests {

	@Test
	public void messagesAreOutputInBatches() throws InterruptedException {
		int messages = 1000;

		final CountDownLatch latch = new CountDownLatch(messages);
		final AtomicInteger batches = new AtomicInteger();

		BatchingMessageHandler messageHandler = new BatchingMessageHandler(1024, 16);

		messageHandler.setOutputChannel(new MessageChannel() {

			@SuppressWarnings("unchecked")
			@Override
			public boolean send(Message<?> message) {
				List<Object> payload = (List<Object>) message.getPayload();
				batches.incrementAndGet();

				for (int i = 0; i < payload.size(); i++) {
					latch.countDown();
				}

				try {
					Thread.sleep(10);
				} catch (InterruptedException e) {
					e.printStackTrace();
				}
				return true;
			}

			@Override
			public boolean send(Message<?> message, long timeout) {
				return send(message);
			}

		});
		messageHandler.setBeanFactory(mock(BeanFactory.class));
		messageHandler.onInit();
		messageHandler.start();

		try {
			for (int i = 0; i < messages; i++) {
				messageHandler.handleMessage(MessageBuilder.withPayload("test").build());
			}
			assertTrue(latch.await(30, TimeUnit.SECONDS));
			assertTrue(batches.get() < messages);
		} finally {
			messageHandler.stop();
		}
	}
}
