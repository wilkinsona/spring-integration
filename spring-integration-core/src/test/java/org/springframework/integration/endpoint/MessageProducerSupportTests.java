/*
 * Copyright 2002-2010 the original author or authors.
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

package org.springframework.integration.endpoint;

import org.junit.Ignore;
import org.junit.Test;

import org.springframework.integration.Message;
import org.springframework.integration.MessageDeliveryException;
import org.springframework.integration.MessagingException;
import org.springframework.integration.channel.DirectChannel;
import org.springframework.integration.channel.PublishSubscribeChannel;
import org.springframework.integration.core.MessageHandler;
import org.springframework.integration.handler.ServiceActivatingHandler;
import org.springframework.integration.message.GenericMessage;
import org.springframework.integration.test.util.TestUtils;

/**
 * @author Oleg Zhurakousky
 *
 */
public class MessageProducerSupportTests {
	
	@Test(expected=MessageDeliveryException.class)
	public void validateExceptionIfNoErrorChannel(){
		DirectChannel outChannel = new DirectChannel();
		outChannel.subscribe(new MessageHandler() {		
			public void handleMessage(Message<?> message) throws MessagingException {
				throw new RuntimeException("problems");
			}
		});
		MessageProducerSupport mps = new MessageProducerSupport() {};
	
		mps.setOutputChannel(outChannel);
		mps.setBeanFactory(TestUtils.createTestApplicationContext());
		mps.afterPropertiesSet();
		mps.start();
		
		mps.sendMessage(new GenericMessage<String>("hello"));	
	}
	
	@Test(expected=MessageDeliveryException.class)
	@Ignore
	public void validateExceptionIfSendToErrorChannelFails(){
		DirectChannel outChannel = new DirectChannel();
		outChannel.subscribe(new MessageHandler() {		
			public void handleMessage(Message<?> message) throws MessagingException {
				throw new RuntimeException("problems");
			}
		});
		PublishSubscribeChannel errorChannel = new PublishSubscribeChannel();
		ServiceActivatingHandler handler  = new ServiceActivatingHandler(new MyOneWayErrorService());
		handler.afterPropertiesSet();
		errorChannel.subscribe(handler);
		MessageProducerSupport mps = new MessageProducerSupport() {};
	
		mps.setOutputChannel(outChannel);
		mps.setErrorChannel(errorChannel);
		mps.setBeanFactory(TestUtils.createTestApplicationContext());
		mps.afterPropertiesSet();
		mps.start();
		
		mps.sendMessage(new GenericMessage<String>("hello"));	
	}
	
	public static class MyOneWayErrorService {
		public void handleErrorMessage(Message<?> errorMessage){
			throw new RuntimeException("ooops");
		}
	}
}
