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
package org.apache.fineract.command.idempotency.distinctor;

import org.apache.fineract.command.core.Command;

/**
 * Decides what an idempotency key is unique together with.
 *
 * <p>
 * A command row is unique on its idempotency key <em>and</em> the value returned here, stored alongside the key as
 * {@code custom_idempotency_distinctor}. Returning the same value for every command makes a key unique per tenant
 * across all commands; returning something derived from the command scopes it, so that the same key may be used once
 * per distinct value. A repeat is matched, and replayed, only against the row with the same key and the same value.
 *
 * <p>
 * The value is computed once, when the key is claimed, from the command as it is about to execute. It must be stable
 * for a given request - a repeat of that request must produce the same value, or it is not recognised as a repeat - and
 * it must not be {@code null} or longer than {@link #MAX_LENGTH} characters, the size of the column it is stored in.
 *
 * <p>
 * The implementation is chosen with {@code fineract.command.idempotency.custom-distinctor}; see
 * {@link CustomIdempotencyDistinctorType}. An application can replace it altogether by defining its own bean of this
 * type.
 */
@FunctionalInterface
public interface CustomIdempotencyDistinctor {

    /** The width of the {@code custom_idempotency_distinctor} column. */
    int MAX_LENGTH = 255;

    /**
     * @return the value the command's idempotency key is unique together with; never {@code null}
     */
    String distinctorFor(Command<?> command);
}
