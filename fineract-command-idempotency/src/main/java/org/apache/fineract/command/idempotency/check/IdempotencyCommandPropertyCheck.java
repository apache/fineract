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

        log.info("Command idempotency is enabled.");
    }
}
