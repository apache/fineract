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
package org.apache.fineract.command.check;

import static org.assertj.core.api.Assertions.assertThat;

import java.util.Map;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import org.springframework.boot.test.system.CapturedOutput;
import org.springframework.boot.test.system.OutputCaptureExtension;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;
import org.springframework.core.env.StandardEnvironment;
import org.springframework.core.env.SystemEnvironmentPropertySource;

/**
 * A hook switched off under its 1.16.x property name would otherwise come back on without a word, because every hook is
 * on unless switched off.
 */
@ExtendWith(OutputCaptureExtension.class)
class RenamedCommandPropertyCheckTest {

    private final ApplicationContextRunner runner = new ApplicationContextRunner().withUserConfiguration(RenamedCommandPropertyCheck.class);

    @Test
    void namesTheReplacementOfEveryOldPropertyThatIsStillSet(CapturedOutput output) {
        runner.withPropertyValues("fineract.command.hooks.audit-pre=false", "fineract.command.hooks.audit-post=false",
                "fineract.command.hooks.audit-error=false", "fineract.command.hooks.servlet-header-pre=false",
                "fineract.command.hooks.timestamp-pre=false", "fineract.command.hooks.username-pre=false").run(context -> {
                    assertThat(RenamedCommandPropertyCheck.RENAMED).hasSize(6);
                    RenamedCommandPropertyCheck.RENAMED.forEach((oldName, newName) -> assertThat(output)
                            .contains("Property '" + oldName + "' is set but no longer has any effect. Use '" + newName + "' instead."));
                });
    }

    @Test
    void warnsAboutAnOldPropertySetThroughAnEnvironmentVariable(CapturedOutput output) {
        var environment = new StandardEnvironment();
        environment.getPropertySources()
                .addFirst(new SystemEnvironmentPropertySource(StandardEnvironment.SYSTEM_ENVIRONMENT_PROPERTY_SOURCE_NAME + "-test",
                        Map.of("FINERACT_COMMAND_HOOKS_AUDIT_POST", "false")));

        new ApplicationContextRunner(() -> {
            var context = new AnnotationConfigApplicationContext();
            context.setEnvironment(environment);
            return context;
        }).withUserConfiguration(RenamedCommandPropertyCheck.class).run(context -> assertThat(output).contains(
                "Property 'fineract.command.hooks.audit-post' is set but no longer has any effect. Use 'fineract.command.audit.hook-post-enabled' instead."));
    }

    @Test
    void staysQuietWhenOnlyTheNewNamesAreSet(CapturedOutput output) {
        runner.withPropertyValues("fineract.command.audit.hook-post-enabled=false", "fineract.command.hook-timestamp-enabled=false")
                .run(context -> {
                    assertThat(context).hasSingleBean(RenamedCommandPropertyCheck.class);
                    assertThat(output).doesNotContain("no longer has any effect");
                });
    }

}
