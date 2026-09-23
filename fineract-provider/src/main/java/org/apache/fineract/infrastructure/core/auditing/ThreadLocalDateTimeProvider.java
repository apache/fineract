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

import java.time.temporal.TemporalAccessor;
import java.util.Optional;
import org.springframework.data.auditing.DateTimeProvider;

/**
 * A {@link DateTimeProvider} that resolves to whichever provider the current thread selected.
 *
 * <p>
 * {@link CustomAuditingHandler} picks between local and UTC timestamps per entity, but the handler itself is a
 * singleton shared by every request thread, so the choice cannot live in a field on the handler: one thread would
 * overwrite another's. Holding the choice in a thread local keeps the per-entity behaviour without making the handler
 * mutable, which is what previously forced the bean to be a prototype and to rebuild its auditing metadata on every
 * single command.
 */
final class ThreadLocalDateTimeProvider implements DateTimeProvider {

    static final ThreadLocalDateTimeProvider INSTANCE = new ThreadLocalDateTimeProvider();

    private static final ThreadLocal<DateTimeProvider> CURRENT = new ThreadLocal<>();

    private ThreadLocalDateTimeProvider() {}

    /**
     * Selects the provider used by the calling thread and returns whatever it had selected before, which the caller
     * must hand back to {@link #restore(DateTimeProvider)}. Marking an entity is never re-entrant on the paths this
     * handler is wired into today, but the previous selection is preserved so that a caller that does re-enter -- an
     * {@link org.springframework.data.domain.AuditorAware} or an audit field setter that touches another entity --
     * cannot silently strip the outer entity of its choice and stamp it with the wrong clock.
     */
    static DateTimeProvider setAndReturnPreviousValue(final DateTimeProvider provider) {
        final DateTimeProvider previous = CURRENT.get();
        CURRENT.set(provider);
        return previous;
    }

    /**
     * Puts back the selection returned by {@link #setAndReturnPreviousValue(DateTimeProvider)}, dropping the calling
     * thread's selection altogether when there was none. This is deliberately the only way to undo a
     * {@code setAndReturnPreviousValue}: an unconditional clear would be wrong inside a nested call, where the outer
     * selection still has to survive.
     */
    static void restore(final DateTimeProvider previous) {
        if (previous == null) {
            CURRENT.remove();
        } else {
            CURRENT.set(previous);
        }
    }

    @Override
    public Optional<TemporalAccessor> getNow() {
        final DateTimeProvider current = CURRENT.get();
        // No selection means the caller went through a path that does not setAndReturnPreviousValue one; the local-time
        // provider is the
        // same default the handler had before this class existed.
        return (current == null ? CustomDateTimeProvider.INSTANCE : current).getNow();
    }
}
