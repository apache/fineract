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
package org.apache.fineract.command.idempotency.hook;

import static org.apache.fineract.command.core.CommandState.ERROR;
import static org.apache.fineract.command.core.CommandState.PROCESSED;
import static org.apache.fineract.command.core.CommandState.UNDER_PROCESSING;
import static org.apache.fineract.command.core.exception.AbstractCommandIdempotencyException.IDEMPOTENT_CACHE_HEADER;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_HOOK_CHECK_BEFORE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE;

import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandReplay;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.core.exception.CommandIdempotencyConflictException;
import org.apache.fineract.command.core.exception.CommandIdempotencyFailedException;
import org.apache.fineract.command.core.exception.CommandIdempotencyInProgressException;
import org.apache.fineract.command.core.exception.CommandIdempotencyProcessedException;
import org.apache.fineract.command.util.ServletHeaderUtil;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_IDEMPOTENCY_HOOK_CHECK_BEFORE)
@ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE, havingValue = "true", matchIfMissing = true)
final class IdempotencyCheckCommandHook implements CommandHookBefore<Object, Object> {

    private final CommandStore store;

    @Override
    public void onBefore(CommandContext<Object, Object> ctx) {
        var command = ctx.getCommand();
        var key = command.getIdempotencyKey();

        if (StringUtils.isEmpty(key)) {
            return;
        }

        // claiming the key is what detects duplicates: the unique constraint means exactly one concurrent request can
        // win, whereas a read-only check followed by execution lets every racer through
        if (store.claim(command)) {
            rememberClaimForResponseCapture(key);

            return;
        }

        // the command is never executed again, so it is not audited again either; the claimed row already owns this key
        ctx.setSkipExecution(true);
        ctx.setSkipHooks(true);

        var replay = replayFor(key);

        log.warn("Duplicate idempotency key detected: {} (state = {}, user = {}, ip = {})", key, replay.state(),
                command.getInitiatedByUsername(), command.getIpAddress());

        if (UNDER_PROCESSING.equals(replay.state())) {
            // the original has not finished, so there is nothing to replay yet and retrying is the right thing to do
            throw new CommandIdempotencyInProgressException(key, replay.response());
        }

        if (!store.matchesStoredRequest(key, command.getPayload())) {
            // replaying here would answer a question that was never asked
            throw new CommandIdempotencyConflictException(key, null);
        }

        if (ERROR.equals(replay.state())) {
            // a repeat of a failed command fails exactly the way the original did
            throw new CommandIdempotencyFailedException(key, replay.statusCode(), replay.response());
        }

        if (PROCESSED.equals(replay.state())) {
            replayProcessed(ctx, key, replay);

            return;
        }

        throw new CommandIdempotencyConflictException(key, replay.response());
    }

    /**
     * Reads what was stored for a key, turning a result that cannot be reconstructed into a rejection.
     *
     * <p>
     * A stored result that will not rehydrate - a type that has been renamed or removed since, a field that no longer
     * deserialises - leaves the original answer unreachable. Letting that surface as a server error would say the
     * request failed, and answering with an empty body would say it succeeded with no content; both are untrue. The key
     * has been used and its answer cannot be produced, so the request is refused and told why.
     */
    private CommandReplay replayFor(String key) {
        try {
            return store.getReplayByKey(key);
        } catch (RuntimeException e) {
            log.error("Cannot reconstruct the stored result for idempotency key {}", key, e);

            throw new CommandIdempotencyConflictException(key,
                    Map.of("idempotencyKey", key, "error",
                            "The result stored under this idempotency key cannot be reconstructed, so the original answer "
                                    + "cannot be replayed. Retry with a new idempotency key."),
                    e);
        }
    }

    /**
     * Answers a repeat of a command that completed.
     *
     * <p>
     * Where the command stored a result of its own, that result is handed back through the dispatcher rather than
     * thrown: {@code skipExecution} makes the dispatcher return it, the resource method returns it as it returned the
     * original, and the web layer renders it with the serializer and the status it used the first time. Nothing about
     * the original answer has to have been recorded for that to come out identical - not the status, not the bytes.
     *
     * <p>
     * Only a command that stored no result of its own falls back to the answer that was captured as it went out, and
     * that is text rather than an object, so it has to be thrown with the status recorded alongside it.
     */
    private void replayProcessed(CommandContext<Object, Object> ctx, String key, CommandReplay replay) {
        var response = replay.response();

        if (response == null || response instanceof String) {
            throw new CommandIdempotencyProcessedException(key, replay.statusCode(), response);
        }

        markServedFromCache(key);

        ctx.setResponse(response);
    }

    /**
     * The replay header is set here rather than by an exception mapper, because a replay answered from the command's
     * own result never reaches one - it leaves through the ordinary return path.
     */
    private static void markServedFromCache(String key) {
        if (!ServletHeaderUtil.setResponseHeader(IDEMPOTENT_CACHE_HEADER, "true")) {
            log.warn("Replaying idempotency key {} outside an HTTP response, so it is not marked as served from cache", key);
        }
    }

    private static void rememberClaimForResponseCapture(String key) {
        var attributes = RequestContextHolder.getRequestAttributes();

        if (attributes != null) {
            attributes.setAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE, key, RequestAttributes.SCOPE_REQUEST);
        }
    }
}
