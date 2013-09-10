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
package org.springframework.integration.handler.advice;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;

import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.MessagingException;
import org.springframework.messaging.core.GenericMessagingTemplate;
import org.springframework.messaging.support.ErrorMessage;
import org.springframework.retry.RecoveryCallback;
import org.springframework.retry.RetryContext;
import org.springframework.util.Assert;

/**
 * RecoveryCallback that sends the final throwable as an ErrorMessage after
 * retry exhaustion.
 * @author Gary Russell
 * @since 2.2
 *
 */
public class ErrorMessageSendingRecoverer implements RecoveryCallback<Object> {

	private final static Log logger = LogFactory.getLog(ErrorMessageSendingRecoverer.class);

	private final GenericMessagingTemplate messagingTemplate = new GenericMessagingTemplate();

	public ErrorMessageSendingRecoverer(MessageChannel channel) {
		Assert.notNull(channel, "channel cannot be null");
		this.messagingTemplate.setDefaultDestination(channel);
	}

	public void setSendTimeout(long sendTimeout) {
		this.messagingTemplate.setSendTimeout(sendTimeout);
	}

	public Object recover(RetryContext context) throws Exception {
		Throwable lastThrowable = context.getLastThrowable();
		if (lastThrowable == null) {
			lastThrowable = new RetryExceptionNotAvailableException(
					(Message<?>) context.getAttribute("message"),
					"No retry exception available; " +
					"this can occur, for example, if the RetryPolicy allowed zero attempts to execute the handler; " +
					"RetryContext: " + context.toString());
		}
		if (logger.isDebugEnabled()) {
			String supplement = "";
			if (lastThrowable instanceof MessagingException) {
				supplement = ":failedMessage:" + ((MessagingException) lastThrowable).getFailedMessage();
			}
			logger.debug("Sending ErrorMessage " + supplement, lastThrowable);
		}
		messagingTemplate.send(new ErrorMessage(lastThrowable));
		return null;
	}

	public static class RetryExceptionNotAvailableException extends MessagingException {

		private static final long serialVersionUID = 1L;

		public RetryExceptionNotAvailableException(Message<?> message, String description) {
			super(message, description);
		}
	}
}
