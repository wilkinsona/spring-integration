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

import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import org.junit.Test;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.integration.Message;
import org.springframework.integration.MessagingException;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Andy Wilkinson
 */
public class RingBufferChannelTests {

	@Test
	public void messagesAreSentToSubscribedHandler() throws InterruptedException {
		RingBufferChannel channel = new RingBufferChannel(64);
		channel.setBeanFactory(mock(BeanFactory.class));
		channel.onInit();
		channel.start();
		try {
			CountDownLatch latch = new CountDownLatch(1);
			LatchDecrementingMessageHandler handler = new LatchDecrementingMessageHandler(latch);
			channel.subscribe(handler);
			channel.send(MessageBuilder.withPayload("test").build());
			assertTrue(latch.await(30, TimeUnit.SECONDS));
		} finally {
			channel.stop();
		}
	}

	private static final class LatchDecrementingMessageHandler implements MessageHandler {

		private final CountDownLatch latch;

		public LatchDecrementingMessageHandler(CountDownLatch latch) {
			this.latch = latch;
		}

		@Override
		public void handleMessage(Message<?> message) throws MessagingException {
			latch.countDown();
		}
	}
}
