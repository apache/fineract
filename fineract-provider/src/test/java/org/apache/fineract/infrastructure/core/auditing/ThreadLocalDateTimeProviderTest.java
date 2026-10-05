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
package org.apache.fineract.infrastructure.core.auditing;

import static org.junit.jupiter.api.Assertions.assertInstanceOf;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertSame;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.auditing.DateTimeProvider;

public class ThreadLocalDateTimeProviderTest {

    @AfterEach
    public void tearDown() {
        ThreadLocalDateTimeProvider.restore(null);
    }

    @Test
    public void fallsBackToLocalTimeWhenNoProviderWasSelected() {
        Optional<TemporalAccessor> now = ThreadLocalDateTimeProvider.INSTANCE.getNow();

        assertTrue(now.isPresent());
        assertInstanceOf(LocalDateTime.class, now.get());
    }

    @Test
    public void usesTheProviderSelectedByTheCallingThread() {
        ThreadLocalDateTimeProvider.setAndReturnPreviousValue(CustomDateTimeProvider.UTC);

        assertInstanceOf(OffsetDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());
    }

    @Test
    public void restoringNoPreviousSelectionRestoresTheFallback() {
        ThreadLocalDateTimeProvider.setAndReturnPreviousValue(CustomDateTimeProvider.UTC);
        ThreadLocalDateTimeProvider.restore(null);

        assertInstanceOf(LocalDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());
    }

    /**
     * A nested selection must give the outer one back rather than dropping it, so that a re-entrant mark cannot leave
     * the outer entity without a choice halfway through being stamped.
     */
    @Test
    public void restoreGivesBackTheOuterSelection() {
        final DateTimeProvider noneBefore = ThreadLocalDateTimeProvider.setAndReturnPreviousValue(CustomDateTimeProvider.UTC);
        assertNull(noneBefore, "nothing was selected before");

        final DateTimeProvider outer = ThreadLocalDateTimeProvider.setAndReturnPreviousValue(CustomDateTimeProvider.INSTANCE);
        assertSame(CustomDateTimeProvider.UTC, outer);
        assertInstanceOf(LocalDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());

        ThreadLocalDateTimeProvider.restore(outer);
        assertInstanceOf(OffsetDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());

        ThreadLocalDateTimeProvider.restore(noneBefore);
        assertInstanceOf(LocalDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());
    }

    /**
     * The whole point of the thread local: a shared handler must not let one request's choice of timestamp leak into
     * another request running at the same time. This is the race that previously forced the bean to be a prototype.
     */
    @Test
    public void oneThreadsSelectionDoesNotLeakIntoAnother() throws Exception {
        ThreadLocalDateTimeProvider.setAndReturnPreviousValue(CustomDateTimeProvider.UTC);
        final CountDownLatch done = new CountDownLatch(1);
        final AtomicReference<TemporalAccessor> seenByOtherThread = new AtomicReference<>();

        final Thread other = new Thread(() -> {
            try {
                seenByOtherThread.set(ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());
            } finally {
                done.countDown();
            }
        });
        other.start();
        assertTrue(done.await(10, TimeUnit.SECONDS), "the other thread did not finish");

        assertInstanceOf(LocalDateTime.class, seenByOtherThread.get());
        assertInstanceOf(OffsetDateTime.class, ThreadLocalDateTimeProvider.INSTANCE.getNow().orElseThrow());
    }
}
