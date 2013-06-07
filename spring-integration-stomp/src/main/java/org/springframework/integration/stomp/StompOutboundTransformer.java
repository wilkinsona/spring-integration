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
package org.springframework.integration.stomp;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.List;
import java.util.Map.Entry;

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;

/**
 * @author Gary Russell
 * @author Andy Wilkinson
 */
public final class StompOutboundTransformer extends AbstractStompTransformer {

	@Override
	public Message<?> transform(Message<?> message) {
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		StompCommand command = (StompCommand) message.getHeaders().get(StompInboundTransformer.HEADER_COMMAND);
		try {
			outputStream.write(command.toString().getBytes(HEADER_CHARSET));
			outputStream.write(LF);

			StompHeaders headers = (StompHeaders) message.getHeaders().get(HEADER_HEADERS);

			for (Entry<String, List<String>> entry : headers.entrySet()) {
				String key = entry.getKey();
				key = replaceAllOutbound(key);
				for (String value : entry.getValue()) {
					outputStream.write(key.getBytes("UTF-8"));
					outputStream.write(HEADER_SEPARATOR);
					value = replaceAllOutbound(value);
					outputStream.write(value.getBytes("UTF-8"));
					outputStream.write(LF);
				}
			}
			outputStream.write(LF);
			outputStream.write(0);
		}
		catch (IOException e) {
			throw new StompException("Failed to serialize " + message, e);
		}
		return MessageBuilder.withPayload(outputStream.toByteArray()).copyHeaders(message.getHeaders()).build();
	}

	private String replaceAllOutbound(String string) {
		return string.replaceAll("\\\\", "\\\\")
				.replaceAll(":", "\\\\c")
				.replaceAll("\n", "\\\\n")
				.replaceAll("\r", "\\\\r");
	}
}
