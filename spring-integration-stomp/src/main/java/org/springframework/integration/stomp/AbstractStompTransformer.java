package org.springframework.integration.stomp;

import java.nio.charset.Charset;

import org.springframework.integration.transformer.Transformer;

abstract class AbstractStompTransformer implements Transformer {

	protected static final byte HEADER_SEPARATOR = ':';

	protected static final byte LF = '\n';

	protected static final Charset HEADER_CHARSET = Charset.forName("UTF-8");

	public static final String HEADER_COMMAND = "stompCommand";

}
