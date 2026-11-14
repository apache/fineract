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
package org.apache.fineract.command.idempotency.check;

import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_ENABLED;
import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_ERROR_ENABLED;
import static org.apache.fineract.command.audit.AuditCommandConstants.COMMAND_AUDIT_PROPERTY_HOOK_POST_ENABLED;
import static org.apache.fineract.command.idempotency.IdempotencyCommandConstants.COMMAND_IDEMPOTENCY_PROPERTY_ENABLED;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.audit.AuditCommandProperties;
import org.apache.fineract.command.idempotency.IdempotencyCommandProperties;
import org.springframework.stereotype.Component;

/**
 * Refuses to start an application that has idempotency switched on and auditing switched off.
 *
 * <p>
 * Claiming an idempotency key writes the command row, but nothing in this module ever writes it again: it is the audit
 * hooks that move a claimed command out of {@code UNDER_PROCESSING} and store the result a repeat is answered with. Run
 * idempotency without them and the first request for a key succeeds, the row stays {@code UNDER_PROCESSING} for ever,
 * and every repeat of that key is answered {@code 425 Too Early} - the key is poisoned, silently, and only from the
 * second request onwards. That is worth failing a startup over rather than warning about.
 *
 * <p>
 * Switching auditing on as a whole is not enough: the after hook records a command that completed and the error hook
 * one that failed, and each can be switched off on its own. Without the after hook every successful command stays
 * claimed; without the error hook every failed one does. Either way a repeat is told to retry until the claim times
 * out, and is then told the command never finished - which, for a command that completed, is untrue. The before hook is
 * not required: claiming the key has already written the row it would write.
 */
@Slf4j
@RequiredArgsConstructor
@Component
final class IdempotencyCommandPropertyCheck {

    private final IdempotencyCommandProperties idempotency;

    private final AuditCommandProperties audit;

    @PostConstruct
    void check() {
        if (!Boolean.TRUE.equals(idempotency.getEnabled())) {
            log.info("Command idempotency is disabled: commands are still audited, but no idempotency key is read and "
                    + "a repeated request is executed again rather than replayed.");

            return;
        }

        if (!Boolean.TRUE.equals(audit.getEnabled())) {
            throw new IllegalStateException("Command idempotency is enabled but command auditing is not: " + COMMAND_AUDIT_PROPERTY_ENABLED
                    + " must be true. Idempotency relies on the audit hooks to record how a "
                    + "command finished; without them every repeat of an idempotency key would be answered 425 Too Early for "
                    + "ever. Enable auditing, or set " + COMMAND_IDEMPOTENCY_PROPERTY_ENABLED + "=false.");
        }

        requireHook(COMMAND_AUDIT_PROPERTY_HOOK_POST_ENABLED, audit.getHookPostEnabled(), "completed");
        requireHook(COMMAND_AUDIT_PROPERTY_HOOK_ERROR_ENABLED, audit.getHookErrorEnabled(), "failed");

        var claimTimeout = idempotency.getClaimTimeout();

        if (claimTimeout != null && claimTimeout.isNegative()) {
            // a negative age is always exceeded, so every unfinished claim - including one still running - would be
            // declared abandoned; zero is how the timeout is switched off
            throw new IllegalStateException("fineract.command.idempotency.claim-timeout must not be negative, but is " + claimTimeout
                    + ". Use 0 to switch the timeout off.");
        }

        if (claimTimeout == null || claimTimeout.isZero()) {
            log.info("Command idempotency claim timeout is off: a repeat of an unfinished command is answered 425 Too Early for as "
                    + "long as it stays unfinished.");
        }

        log.info("Command idempotency is enabled.");
    }

    private static void requireHook(String property, Boolean enabled, String outcome) {
        if (!Boolean.TRUE.equals(enabled)) {
            throw new IllegalStateException("Command idempotency is enabled but " + property + " is not: it is the audit hook that "
                    + "records a command that " + outcome + ", so without it such a command stays under processing and every "
                    + "repeat of its idempotency key is refused instead of being answered. Enable the hook, or set "
                    + COMMAND_IDEMPOTENCY_PROPERTY_ENABLED + "=false.");
        }
    }
}
