/*
 * Copyright 2002-2013 the original author or authors.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.springframework.integration.stomp.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.messaging.Message;
import org.springframework.util.AntPathMatcher;
import org.springframework.util.Assert;
import org.springframework.util.PathMatcher;
import org.springframework.web.messaging.MessageType;
import org.springframework.web.messaging.event.EventBus;
import org.springframework.web.messaging.event.EventConsumer;


/**
 * @author Rossen Stoyanchev
 * @since 4.0
 */
public abstract class AbstractMessageService {

	public static final String MESSAGE_KEY = "messageKey";

	public static final String CLIENT_CONNECTION_CLOSED_KEY = "clientConnectionClosed";

	protected final Log logger = LogFactory.getLog(getClass());

	private final List<String> allowedDestinations = new ArrayList<String>();

	private final List<String> disallowedDestinations = new ArrayList<String>();

	private final PathMatcher pathMatcher = new AntPathMatcher();

	/**
	 * Ant-style destination patterns that this service is allowed to process.
	 */
	public void setAllowedDestinations(String... patterns) {
		this.allowedDestinations.clear();
		this.allowedDestinations.addAll(Arrays.asList(patterns));
	}

	/**
	 * Ant-style destination patterns that this service should skip.
	 */
	public void setDisallowedDestinations(String... patterns) {
		this.disallowedDestinations.clear();
		this.disallowedDestinations.addAll(Arrays.asList(patterns));
	}

	protected boolean isAllowedDestination(Message<?> message) {
		String destination = (String) message.getHeaders().get("destination");
		if (destination == null) {
			return true;
		}
		if (!this.disallowedDestinations.isEmpty()) {
			for (String pattern : this.disallowedDestinations) {
				if (this.pathMatcher.match(pattern, destination)) {
					if (logger.isTraceEnabled()) {
						logger.trace("Skip notification: " + message);
					}
					return false;
				}
			}
		}
		if (!this.allowedDestinations.isEmpty()) {
			for (String pattern : this.allowedDestinations) {
				if (this.pathMatcher.match(pattern, destination)) {
					return true;
				}
			}
			if (logger.isTraceEnabled()) {
				logger.trace("Skip notification: " + message);
			}
			return false;
		}
		return true;
	}

	protected void processConnect(Message<?> message) {
	}

	protected void processMessage(Message<?> message) {
	}

	protected void processSubscribe(Message<?> message) {
	}

	protected void processUnsubscribe(Message<?> message) {
	}

	protected void processDisconnect(Message<?> message) {
	}

	protected void processOther(Message<?> message) {
	}

	protected void processClientConnectionClosed(String sessionId) {
	}

}
