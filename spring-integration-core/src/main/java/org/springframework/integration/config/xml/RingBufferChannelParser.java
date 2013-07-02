/*
 * Copyright 2002-2012 the original author or authors.
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

package org.springframework.integration.config.xml;

import org.springframework.beans.factory.support.BeanDefinitionBuilder;
import org.springframework.beans.factory.xml.ParserContext;
import org.springframework.util.StringUtils;
import org.springframework.util.xml.DomUtils;
import org.w3c.dom.Element;

/**
 * Parser for the &lt;ring-buffer-channel&gt; element.
 *
 * @author Andy Wilkinson
 */
public final class RingBufferChannelParser extends AbstractChannelParser {

	private static final String CLASS_NAME_RING_BUFFER_CHANNEL = IntegrationNamespaceUtils.BASE_PACKAGE + ".channel.RingBufferChannel";

	@Override
	protected BeanDefinitionBuilder buildBeanDefinition(Element element, ParserContext parserContext) {
		BeanDefinitionBuilder builder = BeanDefinitionBuilder.genericBeanDefinition(CLASS_NAME_RING_BUFFER_CHANNEL);

		String size = element.getAttribute("size");
		if (StringUtils.hasText(size)) {
			builder.addConstructorArgValue(size);
		}

		Element dispatcherElement = DomUtils.getChildElementByTagName(element, "dispatcher");
		if (dispatcherElement != null) {
			String taskExecutor = dispatcherElement.getAttribute("task-executor");
			if (StringUtils.hasText(taskExecutor)) {
				parserContext.getReaderContext().error(
						"The 'task-executor' attribute is not supported on a ring buffer channel'", element);
			}

			String loadBalancer = dispatcherElement.getAttribute("load-balancer");
			if ("none".equals(loadBalancer)) {
				builder.addConstructorArgValue(null);
			}

			IntegrationNamespaceUtils.setValueIfAttributeDefined(builder, dispatcherElement, "failover");

			this.setMaxSubscribersProperty(parserContext, builder, dispatcherElement,
					IntegrationNamespaceUtils.DEFAULT_MAX_UNICAST_SUBSCRIBERS_PROPERTY_NAME);
		}

		IntegrationNamespaceUtils.setReferenceIfAttributeDefined(builder, element, "error-handler");
		this.setMaxSubscribersProperty(parserContext, builder, element,
				IntegrationNamespaceUtils.DEFAULT_MAX_BROADCAST_SUBSCRIBERS_PROPERTY_NAME);

		return builder;
	}

}
