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

import org.springframework.integration.Message;
import org.springframework.integration.support.MessageBuilder;
import org.springframework.util.Assert;

/**
 * @author Gary Russell
 * @author Andy Wilkinson
 */
public final class StompInboundTransformer extends AbstractStompTransformer {

	private static final byte CR = '\r';

	@Override
	public Message<?> transform(Message<?> message) {
		byte[] stompBytes;

		if (message.getPayload() instanceof byte[]) {
			stompBytes = (byte[])message.getPayload();
		} else if (message.getPayload() instanceof CharSequence) {
			stompBytes = ((CharSequence)message.getPayload()).toString().getBytes(HEADER_CHARSET);
		} else {
			throw new IllegalArgumentException("Payload of 'message' must be a byte[] or a CharSequence");
		}

		int totalLength = stompBytes.length;
		if (stompBytes[totalLength-1] == 0) {
			totalLength--;
		}
		int payloadIndex = findPayloadStart(stompBytes);
		if (payloadIndex == 0) {
			throw new StompException("No command found");
		}

		byte[] payload = new byte[totalLength - payloadIndex];
		System.arraycopy(stompBytes, payloadIndex, payload, 0, totalLength - payloadIndex);

		String headerString = new String(stompBytes, 0, payloadIndex, HEADER_CHARSET);
		Parser parser = new Parser(headerString);

		MessageBuilder<byte[]> builder = MessageBuilder.withPayload(payload).copyHeaders(message.getHeaders());

		StompCommand command = StompCommand.valueOf(parser.nextToken(LF).trim());
		Assert.notNull(command, "No command found");

		builder.setHeader(HEADER_COMMAND, command);

		StompHeaders headers = new StompHeaders();

		while (parser.hasNext()) {
			String header = parser.nextToken(HEADER_SEPARATOR);
			if (header != null) {
				if (parser.hasNext()) {
					String value = parser.nextToken(LF);
					headers.add(header,  value);
				}
				else {
					throw new StompException("Parse exception for " + headerString);
				}
			}
		}

		builder.setHeader(HEADER_HEADERS, headers);

		return builder.build();
	}

	private int findPayloadStart(byte[] bytes) {
		int i;
		// ignore any leading EOL from the previous message
		for (i = 0; i < bytes.length; i++) {
			if (bytes[i] != '\n' && bytes[i] != '\r' ) {
				break;
			}
			bytes[i] = ' ';
		}
		int payloadOffset = 0;
		for (; i < bytes.length - 1; i++) {
			if ((bytes[i] == LF && bytes[i+1] == LF)) {
				payloadOffset = i + 2;
				break;
			}
			if (i < bytes.length - 3 &&
				(bytes[i] == CR && bytes[i+1] == LF &&
				 bytes[i+2] == CR && bytes[i+3] == LF)) {
				payloadOffset = i + 4;
				break;
			}
		}
		if (i >= bytes.length) {
			throw new StompException("No end of headers found");
		}
		return payloadOffset;
	}

	private class Parser {

		private final String content;

		private int offset;

		public Parser(String content) {
			this.content = content;
		}

		public boolean hasNext() {
			return this.offset < this.content.length();
		}

		public String nextToken(byte delimiter) {
			if (this.offset >= this.content.length()) {
				return null;
			}
			int delimAt = this.content.indexOf(delimiter, this.offset);
			if (delimAt == -1) {
				if (this.offset == this.content.length() - 1 && delimiter == HEADER_SEPARATOR &&
						this.content.charAt(this.offset) == LF) {
					this.offset++;
					return null;
				}
				else if (this.offset == this.content.length() - 2 && delimiter == HEADER_SEPARATOR &&
						this.content.charAt(this.offset) == CR &&
						this.content.charAt(this.offset + 1) == LF) {
					this.offset += 2;
					return null;
				}
				else {
					throw new StompException("No delimiter found at offset " + offset + " in " + this.content);
				}
			}
			int escapeAt = this.content.indexOf('\\', this.offset);
			String token = this.content.substring(this.offset, delimAt + 1);
			this.offset += token.length();
			if (escapeAt >= 0 && escapeAt < delimAt) {
				char escaped = this.content.charAt(escapeAt + 1);
				if (escaped == 'n' || escaped == 'c' || escaped == '\\') {
					token = token.replaceAll("\\\\n", "\n")
							.replaceAll("\\\\r", "\r")
							.replaceAll("\\\\c", ":")
							.replaceAll("\\\\\\\\", "\\\\");
				}
				else {
					throw new StompException("Invalid escape sequence \\" + escaped);
				}
			}
			int length = token.length();
			if (delimiter == LF && length > 1 && token.charAt(length - 2) == CR) {
				return token.substring(0, length - 2);
			}
			else {
				return token.substring(0, length - 1);
			}
		}
	}
}
