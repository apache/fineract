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
package com.acme.fineract.portfolio.note.starter;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java8.En;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

public class AcmeNoteServiceStepDefinitions implements En {

    private Class interfaceClass;

    private Class implementationClass;

    private ApplicationContextRunner contextRunner;

    public AcmeNoteServiceStepDefinitions() {
        Given("/^An auto configuration (.*) and a service configuration (.*)$/",
                (String autoConfigurationClassName, String configurationClassName) -> {
                    contextRunner = new ApplicationContextRunner()
                            .withConfiguration(AutoConfigurations.of(Class.forName(autoConfigurationClassName)))
                            .withPropertyValues("acme.note.enabled", "true")
                            .withUserConfiguration(Class.forName(configurationClassName.trim()));
                });

        When("/^The user retrieves the service of interface class (.*)$/", (String interfaceClassName) -> {
            contextRunner.run((ctx) -> {
                this.interfaceClass = Class.forName(interfaceClassName.trim());

                assertThat(this.interfaceClass.isInterface()).isTrue();
                assertThat(ctx).hasSingleBean(this.interfaceClass);

                this.implementationClass = ctx.getBean(interfaceClass).getClass();
            });
        });

        Then("/^The service class should match (.*)$/", (String serviceClassName) -> {
            assertThat(Class.forName(serviceClassName.trim())).isEqualTo(this.implementationClass);
        });
    }
}
