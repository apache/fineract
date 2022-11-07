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
package org.apache.fineract.cob.common;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;

import io.cucumber.java8.En;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepositoryWrapper;
import org.springframework.batch.repeat.RepeatStatus;
import org.springframework.security.core.context.SecurityContextHolder;

public class InitialisationTaskletStepDefinitions implements En {

    private AppUserRepositoryWrapper userRepository = mock(AppUserRepositoryWrapper.class);

    private InitialisationTasklet initialisationTasklet = new InitialisationTasklet(userRepository);

    private AppUser appUser = mock(AppUser.class);
    private RepeatStatus resultItem;

    public InitialisationTaskletStepDefinitions() {
        Given("/^The InitialisationTasklet.execute method with action (.*)$/", (String action) -> {

            if ("error".equals(action)) {
                lenient().when(this.userRepository.fetchSystemUser()).thenThrow(new RuntimeException("fail"));
            } else {
                lenient().when(this.userRepository.fetchSystemUser()).thenReturn(appUser);
            }

        });

        When("InitialisationTasklet.execute method executed", () -> {
            resultItem = this.initialisationTasklet.execute(null, null);
        });

        Then("InitialisationTasklet.execute result should match", () -> {
            assertEquals(RepeatStatus.FINISHED, resultItem);
            assertEquals(appUser, SecurityContextHolder.getContext().getAuthentication().getPrincipal());
        });

        Then("throw exception InitialisationTasklet.execute method", () -> {
            assertThrows(RuntimeException.class, () -> {
                resultItem = this.initialisationTasklet.execute(null, null);
            });
        });
    }
}
