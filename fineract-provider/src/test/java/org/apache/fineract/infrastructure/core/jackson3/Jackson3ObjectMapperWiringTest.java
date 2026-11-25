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
package org.apache.fineract.infrastructure.core.jackson3;

import static org.assertj.core.api.Assertions.assertThat;

import org.apache.fineract.infrastructure.core.jersey.JacksonWireFormatCharacterizationTest;
import org.junit.jupiter.api.Test;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.jackson.autoconfigure.JacksonAutoConfiguration;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;
import tools.jackson.databind.ObjectMapper;

/**
 * Checks that the mapper {@link Jackson3ObjectMapperConfig} defines is the one injected, not merely the one defined.
 *
 * <p>
 * Spring Boot auto-configures its own Jackson 3 mapper, annotated {@code @Primary}, and backs off only on finding a
 * bean declared as a {@code JsonMapper}. Get that detail wrong and the context still starts, every mapper-level test
 * still passes, and the command store quietly writes through Boot's stock mapper instead — unmasked credentials and
 * all. Nothing else in the suite can see that, because everything else builds its mapper by calling the configuration
 * class directly rather than by asking the container for one.
 */
class Jackson3ObjectMapperWiringTest {

    private final ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(JacksonAutoConfiguration.class))
            .withUserConfiguration(Jackson3ObjectMapperConfig.class);

    @Test
    void theConfiguredMapperIsTheOneInjected() {
        contextRunner.run(context -> {
            ObjectMapper injected = context.getBean(ObjectMapper.class);

            assertThat(injected.writeValueAsString(JacksonWireFormatCharacterizationTest.populated()))
                    .as("The Jackson 3 mapper handed to the command store must be the configured one, "
                            + "which masks credentials and writes a LocalDate property as an ISO string")
                    .doesNotContain("secret-value") //
                    .contains("\"localDate\":\"2026-10-05\"");
        });
    }

    @Test
    void bootDoesNotAlsoContributeAMapperOfItsOwn() {
        contextRunner.run(context -> assertThat(context.getBeanNamesForType(ObjectMapper.class))
                .as("Two Jackson 3 mappers means the @Primary one wins, and it is not ours").containsExactly("jackson3ObjectMapper"));
    }
}
