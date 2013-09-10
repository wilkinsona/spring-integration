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

package org.springframework.integration.router.config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

import static org.mockito.Mockito.mock;

import java.io.ByteArrayInputStream;

import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.BeanDefinitionStoreException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.xml.XmlBeanDefinitionReader;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.GenericApplicationContext;
import org.springframework.core.io.InputStreamResource;
import org.springframework.integration.endpoint.EventDrivenConsumer;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.integration.test.util.TestUtils;
import org.springframework.messaging.Message;
import org.springframework.messaging.PollableChannel;
import org.springframework.messaging.core.BeanFactoryMessageChannelDestinationResolver;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.SpringJUnit4ClassRunner;

/**
 * @author Oleg Zhurakousky
 */
@ContextConfiguration
@RunWith(SpringJUnit4ClassRunner.class)
public class PayloadTypeRouterParserTests {

	@Autowired
	private ConfigurableApplicationContext context;

	@Autowired
	private TestService testService;

	@Test
	public void testPayloadTypeRouter() {
		context.start();
		Message<?> message1 = MessageBuilder.withPayload("Hello").build();
		Message<?> message2 = MessageBuilder.withPayload(25).build();
		Message<?> message3 = MessageBuilder.withPayload(new Integer[]{23, 24, 34}).build();
		Message<?> message4 = MessageBuilder.withPayload(new Long[]{23L, 24L, 34L}).build();
		testService.foo(message1);
		testService.foo(message2);
		testService.foo(message3);
		testService.foo(message4);
		PollableChannel chanel1 = (PollableChannel) context.getBean("channel1");
		PollableChannel chanel2 = (PollableChannel) context.getBean("channel2");
		PollableChannel chanel3 = (PollableChannel) context.getBean("channel3");
		PollableChannel chanel4 = (PollableChannel) context.getBean("channel4");
		assertTrue(chanel1.receive(100).getPayload() instanceof String);
		assertTrue(chanel2.receive(100).getPayload() instanceof Integer);
		assertTrue(chanel3.receive(100).getPayload().getClass().isArray());
		assertTrue(chanel4.receive(100).getPayload().getClass().isArray());

		EventDrivenConsumer edc = context.getBean("routerWithChannelResolver", EventDrivenConsumer.class);
		assertEquals(context.getBean("cr"), TestUtils.getPropertyValue(edc, "handler.channelResolver"));
	}

	@Test(expected=BeanDefinitionStoreException.class)
	public void testNoMappingElement(){
		ByteArrayInputStream stream = new ByteArrayInputStream(routerConfigNoMaping.getBytes());
		GenericApplicationContext ac = new GenericApplicationContext();
		XmlBeanDefinitionReader reader = new XmlBeanDefinitionReader(ac);
		reader.setValidationMode(XmlBeanDefinitionReader.VALIDATION_XSD);
		reader.loadBeanDefinitions(new InputStreamResource(stream));
	}

	@SuppressWarnings("unused")
	private String routerConfigFakeType =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	    "<beans:beans xmlns=\"http://www.springframework.org/schema/integration\"" +
		"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:beans=\"http://www.springframework.org/schema/beans\"" +
		"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans" +
		"		http://www.springframework.org/schema/beans/spring-beans.xsd" +
		"		http://www.springframework.org/schema/integration" +
		"		http://www.springframework.org/schema/integration/spring-integration.xsd\">" +
		"   <channel id=\"routingChannel\" />" +
		"   <payload-type-router input-channel=\"routingChannel\">" +
		"	   <mapping type=\"FAKE_TYPE\" channel=\"channel1\" />" +
		"  </payload-type-router>" +
	    "</beans:beans>";

	private String routerConfigNoMaping =
		"<?xml version=\"1.0\" encoding=\"UTF-8\"?>" +
	    "<beans:beans xmlns=\"http://www.springframework.org/schema/integration\"" +
		"    xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\" xmlns:beans=\"http://www.springframework.org/schema/beans\"" +
		"    xsi:schemaLocation=\"http://www.springframework.org/schema/beans" +
		"		http://www.springframework.org/schema/beans/spring-beans.xsd" +
		"		http://www.springframework.org/schema/integration" +
		"		http://www.springframework.org/schema/integration/spring-integration.xsd\">" +
		"   <channel id=\"routingChannel\" />" +
		"   <payload-type-router input-channel=\"routingChannel\"/>" +
	    "</beans:beans>";


	public static interface TestService{
		public void foo(Message<?> message);
	}

	public static class MyChannelResolver extends BeanFactoryMessageChannelDestinationResolver {

		MyChannelResolver() {
			super(mock(BeanFactory.class));
		}
	}
}
