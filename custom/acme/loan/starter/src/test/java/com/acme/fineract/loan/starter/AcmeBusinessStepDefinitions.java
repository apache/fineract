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
package com.acme.fineract.loan.starter;

import static org.assertj.core.api.Assertions.assertThat;

import io.cucumber.java8.En;
import java.util.Set;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.springframework.boot.autoconfigure.AutoConfigurations;
import org.springframework.boot.test.context.runner.ApplicationContextRunner;

@Slf4j
public class AcmeBusinessStepDefinitions implements En {

    private ApplicationContextRunner contextRunner;

    private COBBusinessStepService businessStepService;

    private COBBusinessStep<Loan> businessStep;

    private Set<BusinessStepNameAndOrder> result;

    public AcmeBusinessStepDefinitions() {
        Given("/^An auto configuration (.*) and a service configuration (.*)$/",
                (String autoConfigurationClassName, String configurationClassName) -> {
                    contextRunner = new ApplicationContextRunner()
                            .withConfiguration(AutoConfigurations.of(Class.forName(autoConfigurationClassName)))
                            .withPropertyValues("acme.loan.enabled", "true")
                            .withUserConfiguration(Class.forName(configurationClassName.trim()));
                });

        When("/^The user retrieves the step service with step class (.*) and name (.*)$/", (String stepClass, String stepName) -> {
            contextRunner.run((ctx) -> {
                this.businessStepService = ctx.getBean(COBBusinessStepService.class);

                this.businessStep = ctx.getBean((Class<COBBusinessStep<Loan>>) Class.forName(stepClass));

                // TODO: not yet working, because no storage configured/mocked
                this.result = businessStepService.getCOBBusinessSteps(this.businessStep.getClass(), stepName);
            });
        });

        Then("/^The step service should have a result$/", () -> {
            assertThat(this.businessStep).isNotNull();
            assertThat(this.result).isNotNull();
            // log.warn(">>>>>>>>>>>>>>>>>> RESULT: {}", this.result);
        });
    }
}
