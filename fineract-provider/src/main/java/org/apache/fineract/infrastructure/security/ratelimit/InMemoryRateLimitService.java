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

import com.github.benmanes.caffeine.cache.Cache;
import com.github.benmanes.caffeine.cache.Caffeine;
import jakarta.annotation.PostConstruct;
import java.util.Optional;
import java.util.concurrent.TimeUnit;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class InMemoryRateLimitService implements RateLimitService {

    @Value("${fineract.security.ratelimit.max-attempts-per-host-per-minute:60}")
    private int maxAttemptsPerHostPerMinute;

    private Cache<String, Integer> cache;

    @PostConstruct
    public void setup() {
        cache = Caffeine.newBuilder().expireAfterWrite(1, TimeUnit.MINUTES).maximumSize(100_000).build();
    }

    @Override
    public boolean isRateLimited(String remoteAddress) {
        Integer existingAttempts = cache.getIfPresent(remoteAddress);
        int currentAttempt = 1 + Optional.ofNullable(existingAttempts).orElse(0);
        cache.put(remoteAddress, currentAttempt);

        return currentAttempt > maxAttemptsPerHostPerMinute;
    }

    @Override
    public void resetRateLimit(String remoteAddress) {
        cache.invalidate(remoteAddress);
    }
}
