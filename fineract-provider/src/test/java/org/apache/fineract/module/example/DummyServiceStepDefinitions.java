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
package org.apache.fineract.module.example;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java8.En;
import org.apache.fineract.dummy.core.data.DummyMessage;
import org.apache.fineract.dummy.core.service.DummyService;
import org.apache.fineract.dummy.starter.DummyAutoConfiguration;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class DummyServiceStepDefinitions implements En {

    private DummyMessage message;

    private ApplicationContextRunner contextRunner = new ApplicationContextRunner()
            .withConfiguration(AutoConfigurations.of(DummyAutoConfiguration.class));

    public DummyServiceStepDefinitions() {
        Given("/^A dummy service configuration (.*)$/", (String configurationClass) -> {
            contextRunner = contextRunner.withUserConfiguration(Class.forName(configurationClass.trim()));
        });

        When("The user gets the dummy service message", () -> {
            contextRunner.run((ctx) -> {
                assertThat(ctx).hasSingleBean(DummyService.class);
                this.message = ctx.getBean(DummyService.class).getMessage();
            });
        });

        Then("/^The dummy service message should match (.*)$/", (String msg) -> {
            assertThat(this.message).isNotNull();
            assertThat(msg).isEqualTo(this.message.getMessage());
        });
    }
}
