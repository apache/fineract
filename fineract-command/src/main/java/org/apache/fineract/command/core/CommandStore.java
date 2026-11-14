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
package org.apache.fineract.command.core;

public interface CommandStore {

    Command<Object> getById(Long id);

    /*
     * An idempotency key is unique together with a distinctor (see Command#getCustomIdempotencyDistinctor()), so every
     * lookup by key takes both: the same key may exist once per distinctor.
     */

    Command<Object> getByKey(String key, String distinctor);

    boolean existsByKey(String key, String distinctor);

    /**
     * @return everything needed to answer a repeated request exactly as the original one was answered - the state, and
     *         the status code and body it completed with. A command that completed, one that is still running and one
     *         that failed cannot be answered the same way, so the caller has to be able to tell them apart. Never
     *         {@code null}; an unknown key reports {@link CommandState#UNKNOWN}.
     */
    CommandReplay getReplayByKey(String key, String distinctor);

    /**
     * Records what the original request was answered with, for the parts a command cannot know: the status code always,
     * and the body only when the command stored no result of its own. A stored result is never replaced, because a
     * replay is rendered from that result rather than from captured text.
     */
    void recordResponse(String key, String distinctor, int statusCode, String body);

    /**
     * Atomically claims the command's idempotency key, together with its distinctor, by persisting it, so that no other
     * request can execute a command under the same key and distinctor. Must not be retried, and the conflict must not
     * be swallowed.
     *
     * @return {@code true} if the key was claimed by this call, {@code false} if it was already claimed
     */
    boolean claim(Command<?> command);

    /**
     * @return {@code true} if the given payload is identical to the request stored under that idempotency key.
     *         Comparing the whole request, not just its type, is what stops a key reused for a different request from
     *         replaying an unrelated response.
     */
    boolean matchesStoredRequest(String key, String distinctor, Object payload);

    void store(CommandContext<?, ?> ctx);
}
