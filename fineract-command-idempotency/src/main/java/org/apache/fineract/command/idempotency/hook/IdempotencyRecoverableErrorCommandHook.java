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

import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_HOOK_RECOVERABLE_ERROR;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE;

import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.CommandContext;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.core.CommandStore;
import org.apache.fineract.command.core.RecoverableCommandErrorClassifier;
import org.apache.fineract.command.idempotency.RecoverableErrorKey;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.RequestContextHolder;

/**
 * Releases the idempotency key of a command that failed recoverably, so that a retry under the same key executes the
 * command again instead of being answered with the failure.
 *
 * <p>
 * The failure is not discarded: the command is moved to {@code <key>_RE<id>} (see {@link RecoverableErrorKey}) and the
 * audit error hook, which runs after this one, records it there - outcome, error and the answer the client was sent.
 * Moving it is done by changing the key on the command, because the audit hook rewrites the whole row from the command;
 * changing only the row would be undone a moment later.
 *
 * <p>
 * Anything that goes wrong here leaves the key where it is, which is what happens to every other failure: the retry is
 * answered with the stored failure. Failing the request over it would replace the error the client needs to see.
 */
@Slf4j
@RequiredArgsConstructor
@Component
@Order(COMMAND_IDEMPOTENCY_HOOK_RECOVERABLE_ERROR)
@ConditionalOnProperty(value = COMMAND_IDEMPOTENCY_PROPERTY_HOOK_CHECK_PRE, havingValue = "true", matchIfMissing = true)
final class IdempotencyRecoverableErrorCommandHook implements CommandHookError<Object, Object> {

    private final CommandStore store;

    private final RecoverableCommandErrorClassifier classifier;

    @Override
    public void onError(CommandContext<Object, Object> ctx) {
        var command = ctx.getCommand();
        var key = command.getIdempotencyKey();

        // a repeat that was refused never claimed the key, and a command without a key or a row has nothing to release
        if (ctx.isSkipExecution() || StringUtils.isEmpty(key) || command.getCommandId() == null) {
            return;
        }

        try {
            if (!classifier.isRecoverable(unwrap(ctx.getError()))) {
                return;
            }

            var distinctor = command.getCustomIdempotencyDistinctor();
            var movedKey = RecoverableErrorKey.movedKey(key, command.getCommandId());

            if (store.existsByKey(movedKey, distinctor)) {
                // only possible when a client sent that exact key itself; storing the failure under the original key
                // is the same thing the legacy processing does then
                log.warn("Idempotency key {} is already taken, keeping the recoverable failure under the original key {}", movedKey, key);

                return;
            }

            command.setIdempotencyKey(movedKey);
            rememberMovedKeyForResponseCapture(key, movedKey);

            log.info("Command {} failed recoverably; idempotency key {} is released and the failure is kept under {}",
                    command.getCommandId(), key, movedKey);
        } catch (RuntimeException e) {
            command.setIdempotencyKey(key);

            log.warn("Could not release idempotency key {} after a recoverable error, keeping the failure under it", key, e);
        }
    }

    /** The dispatchers that run commands elsewhere hand the failure back wrapped. */
    private static Throwable unwrap(Throwable error) {
        var current = error;

        while ((current instanceof CompletionException || current instanceof ExecutionException) && current.getCause() != null) {
            current = current.getCause();
        }

        return current;
    }

    /** The response capture filter records the answer against the claimed key, which is now the moved one. */
    private static void rememberMovedKeyForResponseCapture(String key, String movedKey) {
        var attributes = RequestContextHolder.getRequestAttributes();

        if (attributes != null
                && key.equals(attributes.getAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE, RequestAttributes.SCOPE_REQUEST))) {
            attributes.setAttribute(COMMAND_IDEMPOTENCY_CLAIMED_KEY_ATTRIBUTE, movedKey, RequestAttributes.SCOPE_REQUEST);
        }
    }
}
