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
package org.apache.fineract.portfolio.group.service;

import static org.assertj.core.api.Assertions.assertThatCode;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.exception.GeneralPlatformDomainRuleException;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.group.domain.GroupTypes;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;

class GroupMakerCheckerGuardTest {

    private final PlatformSecurityContext context = mock(PlatformSecurityContext.class);
    private final ConfigurationDomainService configurationDomainService = mock(ConfigurationDomainService.class);
    private final AppUser user = mock(AppUser.class);
    private final GroupMakerCheckerGuard guard = new GroupMakerCheckerGuard(context, configurationDomainService);

    @Test
    void passesWhenMakerCheckerIsNotEnabledForTheTask() {
        when(configurationDomainService.isMakerCheckerEnabledForTask("CLOSE_CENTER")).thenReturn(false);

        assertThatCode(() -> guard.rejectIfMakerCheckerEnabled("CLOSE", GroupTypes.CENTER)).doesNotThrowAnyException();
    }

    @Test
    void rejectsWhenMakerCheckerIsEnabledForTheTask() {
        when(configurationDomainService.isMakerCheckerEnabledForTask("ACTIVATE_GROUP")).thenReturn(true);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.isCheckerSuperUser()).thenReturn(false);

        assertThatThrownBy(() -> guard.rejectIfMakerCheckerEnabled("ACTIVATE", GroupTypes.GROUP))
                .isInstanceOf(GeneralPlatformDomainRuleException.class).hasMessageContaining("ACTIVATE_GROUP");
    }

    @Test
    void checkerSuperUserIsNotSubjectToMakerChecker() {
        when(configurationDomainService.isMakerCheckerEnabledForTask("ASSIGNROLE_GROUP")).thenReturn(true);
        when(context.authenticatedUser()).thenReturn(user);
        when(user.isCheckerSuperUser()).thenReturn(true);

        assertThatCode(() -> guard.rejectIfMakerCheckerEnabled("ASSIGNROLE_GROUP")).doesNotThrowAnyException();
    }
}
