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

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.command.idempotency.starter.IdempotencyCommandPropertyCheckAutoConfiguration;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

/**
 * Covers the ways idempotency and auditing can be configured against each other.
 *
 * <p>
 * The mistakes are the ones that would otherwise go unnoticed: idempotency on with auditing off, or with the audit hook
 * that records a completed or a failed command off. Nothing fails at startup, the first request for a key is answered
 * normally, and the breakage shows up only when a client retries - as a 425 that does not resolve. The point of the
 * check is to turn that into a startup failure.
 */
class IdempotencyCommandPropertyCheckTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(IdempotencyCommandPropertyCheckAutoConfiguration.class));

    @Test
    void bothOnIsTheDefaultAndStarts() {
        contextRunner.run(context -> assertThat(context)
                .as("Idempotency and auditing are both on unless switched off, which is the supported arrangement.").hasNotFailed());
    }

    @Test
    void auditingOnItsOwnStarts() {
        contextRunner.withPropertyValues("fineract.command.idempotency.enabled=false").run(context -> assertThat(context)
                .as("Auditing without idempotency is fine: commands are recorded, no key is read, nothing is replayed.").hasNotFailed());
    }

    @Test
    void idempotencyWithoutAuditingRefusesToStart() {
        contextRunner.withPropertyValues("fineract.command.audit.enabled=false").run(context -> assertThat(context)
                .as("A claimed key would never leave UNDER_PROCESSING, so every repeat would answer 425 for ever.").hasFailed().getFailure()
                .rootCause().isInstanceOf(IllegalStateException.class).hasMessageContaining("fineract.command.audit.enabled"));
    }

    @Test
    void idempotencyWithoutTheAfterHookRefusesToStart() {
        contextRunner.withPropertyValues("fineract.command.audit.hook-post-enabled=false").run(context -> assertThat(context)
                .as("Nothing would record a completed command, so its repeats would be told it never finished.").hasFailed().getFailure()
                .rootCause().isInstanceOf(IllegalStateException.class).hasMessageContaining("fineract.command.audit.hook-post-enabled"));
    }

    @Test
    void idempotencyWithoutTheErrorHookRefusesToStart() {
        contextRunner.withPropertyValues("fineract.command.audit.hook-error-enabled=false")
                .run(context -> assertThat(context)
                        .as("Nothing would record a failed command, so its repeats would never be answered with the failure.").hasFailed()
                        .getFailure().rootCause().isInstanceOf(IllegalStateException.class)
                        .hasMessageContaining("fineract.command.audit.hook-error-enabled"));
    }

    @Test
    void idempotencyWithoutTheBeforeHookStarts() {
        contextRunner.withPropertyValues("fineract.command.audit.hook-pre-enabled=false").run(
                context -> assertThat(context).as("Claiming the key already writes the row the before hook would write.").hasNotFailed());
    }

    @Test
    void auditHooksOffWithoutIdempotencyStarts() {
        contextRunner
                .withPropertyValues("fineract.command.idempotency.enabled=false", "fineract.command.audit.hook-post-enabled=false",
                        "fineract.command.audit.hook-error-enabled=false")
                .run(context -> assertThat(context).as("Without idempotency nothing depends on the hooks.").hasNotFailed());
    }

    @Test
    void aClaimTimeoutOfZeroSwitchesTheTimeoutOffAndStarts() {
        contextRunner.withPropertyValues("fineract.command.idempotency.claim-timeout=0").run(context -> assertThat(context)
                .as("Zero is how the timeout is switched off, which is the legacy behaviour.").hasNotFailed());
    }

    @Test
    void anEmptyClaimTimeoutSwitchesTheTimeoutOffAndStarts() {
        contextRunner.withPropertyValues("fineract.command.idempotency.claim-timeout=").run(context -> assertThat(context)
                .as("An empty value, as an unset environment variable gives, is the same as zero.").hasNotFailed());
    }

    @Test
    void aNegativeClaimTimeoutRefusesToStart() {
        contextRunner.withPropertyValues("fineract.command.idempotency.claim-timeout=-1m")
                .run(context -> assertThat(context)
                        .as("A negative age is always exceeded, so even a running command would be declared abandoned.").hasFailed()
                        .getFailure().rootCause().isInstanceOf(IllegalStateException.class).hasMessageContaining("claim-timeout"));
    }

    @Test
    void bothOffStarts() {
        contextRunner.withPropertyValues("fineract.command.idempotency.enabled=false", "fineract.command.audit.enabled=false").run(
                context -> assertThat(context).as("Neither feature is on, so nothing is written to the command table.").hasNotFailed());
    }
}
