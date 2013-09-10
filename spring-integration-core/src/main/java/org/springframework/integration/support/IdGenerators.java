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

package org.springframework.integration.support;

import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;

import org.springframework.messaging.MessageHeaders.IdGenerator;

// TODO Discuss and agree where these should go. In SI or in Spring 4?
public class IdGenerators {

	public static class JdkIdGenerator implements IdGenerator {

        @Override
        public UUID generateId() {
            return UUID.randomUUID();
        }

    }

    public static class SimpleIncrementingIdGenerator implements IdGenerator {

        private final AtomicLong topBits = new AtomicLong();

        private final AtomicLong bottomBits = new AtomicLong();

        @Override
        public UUID generateId() {
            long bottomBits = this.bottomBits.incrementAndGet();
            if (bottomBits == 0) {
                this.topBits.incrementAndGet();
            }
            return new UUID(this.topBits.get(), bottomBits);
        }

    }
}