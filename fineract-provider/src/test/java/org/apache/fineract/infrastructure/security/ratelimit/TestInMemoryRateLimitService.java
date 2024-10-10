/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.security.ratelimit;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.github.benmanes.caffeine.cache.Caffeine;
import java.util.concurrent.TimeUnit;
import org.junit.jupiter.api.Test;
import org.springframework.test.util.ReflectionTestUtils;

class TestInMemoryRateLimitService {

    @Test
    public void testDefaultRateLimiter() {
        InMemoryRateLimitService ratelimiter = new InMemoryRateLimitService();
        ratelimiter.setup();
        ReflectionTestUtils.setField(ratelimiter, "maxAttemptsPerHostPerMinute", 3);

        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertTrue(ratelimiter.isRateLimited("127.0.0.1"));
    }

    @Test
    public void testTimeEviction() throws InterruptedException {
        InMemoryRateLimitService ratelimiter = new InMemoryRateLimitService();
        ratelimiter.setup();
        ReflectionTestUtils.setField(ratelimiter, "maxAttemptsPerHostPerMinute", 2);
        ReflectionTestUtils.setField(ratelimiter, "cache",
                Caffeine.newBuilder().expireAfterWrite(500, TimeUnit.MILLISECONDS).maximumSize(100_000).build());

        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertTrue(ratelimiter.isRateLimited("127.0.0.1"));
        Thread.sleep(1_000L);
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
    }

    @Test
    public void testReset() {
        InMemoryRateLimitService ratelimiter = new InMemoryRateLimitService();
        ratelimiter.setup();
        ReflectionTestUtils.setField(ratelimiter, "maxAttemptsPerHostPerMinute", 2);
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
        assertTrue(ratelimiter.isRateLimited("127.0.0.1"));

        ratelimiter.resetRateLimit("127.0.0.1");
        assertFalse(ratelimiter.isRateLimited("127.0.0.1"));
    }
}
