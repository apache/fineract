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
package org.apache.fineract.command.audit.check;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import org.apache.fineract.command.audit.starter.AuditCommandAutoConfiguration;
import org.apache.fineract.command.audit.starter.AuditCommandPropertyCheckAutoConfiguration;
import org.apache.fineract.command.core.CommandHookAfter;
import org.apache.fineract.command.core.CommandHookBefore;
import org.apache.fineract.command.core.CommandHookError;
import org.apache.fineract.command.core.CommandStore;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;

@ExtendWith(OutputCaptureExtension.class)
class AuditCommandPropertyCheckTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(AuditCommandPropertyCheckAutoConfiguration.class))
            .withBean(CommandStore.class, () -> mock(CommandStore.class));

    @Test
    void theCheckIsRegisteredSoThatItCanActuallyReport(CapturedOutput output) {
        runner.run(context -> {
            assertThat(context).hasSingleBean(AuditCommandPropertyCheck.class);
            assertThat(output).contains("Audit pre command hook enabled.");
        });
    }

    @Test
    void auditingIsOnWhenNothingIsConfigured(CapturedOutput output) {
        // losing the audit trail must be a decision, not the consequence of an unset property
        runner.run(context -> {
            assertThat(output).contains("Audit pre command hook enabled.");
            assertThat(output).contains("Audit post command hook enabled.");
            assertThat(output).contains("Audit error command hook enabled.");
            assertThat(output).doesNotContain("disabled!");
        });
    }

    @Test
    void anExplicitlyDisabledHookIsReportedAsDisabled(CapturedOutput output) {
        runner.withPropertyValues("fineract.command.audit.hook-pre-enabled=false", "fineract.command.audit.hook-post-enabled=false",
                "fineract.command.audit.hook-error-enabled=false").run(context -> {
                    assertThat(output).contains("Audit pre command hook disabled!");
                    assertThat(output).contains("Audit post command hook disabled!");
                    assertThat(output).contains("Audit error command hook disabled!");
                    assertThat(output).doesNotContain("command hook enabled.");
                });
    }

    @Test
    void anExplicitlyDisabledModuleIsReportedAsDisabled(CapturedOutput output) {
        runner.withPropertyValues("fineract.command.audit.enabled=false").run(context -> {
            assertThat(context).hasSingleBean(AuditCommandPropertyCheck.class);
            assertThat(output).contains("Command audit is disabled!");
        });
    }

    @Test
    void theReportedDefaultsMatchTheHooksThatAreActuallyCreated() {
        // guards against the properties class describing behaviour the application does not have
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuditCommandAutoConfiguration.class))
                .withBean(CommandStore.class, () -> mock(CommandStore.class)).run(context -> {
                    assertThat(context).hasSingleBean(CommandHookBefore.class);
                    assertThat(context).hasSingleBean(CommandHookAfter.class);
                    assertThat(context).hasSingleBean(CommandHookError.class);
                });
    }

    @Test
    void noHooksAreCreatedWhenTheModuleIsExplicitlyDisabled() {
        new ApplicationContextRunner().withConfiguration(AutoConfigurations.of(AuditCommandAutoConfiguration.class))
                .withBean(CommandStore.class, () -> mock(CommandStore.class)).withPropertyValues("fineract.command.audit.enabled=false")
                .run(context -> {
                    assertThat(context).doesNotHaveBean(CommandHookBefore.class);
                    assertThat(context).doesNotHaveBean(CommandHookAfter.class);
                    assertThat(context).doesNotHaveBean(CommandHookError.class);
                });
    }
}
