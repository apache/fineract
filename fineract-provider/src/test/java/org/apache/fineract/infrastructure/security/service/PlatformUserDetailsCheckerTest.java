/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.infrastructure.security.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.when;

import org.apache.fineract.nsimbi.userroles.service.NsimbiUserSecurityService;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.springframework.security.authentication.DisabledException;

class PlatformUserDetailsCheckerTest {

    @Test
    void rejectsAdministrativelySuspendedUser() {
        NsimbiUserSecurityService securityService = org.mockito.Mockito.mock(NsimbiUserSecurityService.class);
        AppUser user = org.mockito.Mockito.mock(AppUser.class);
        when(user.getId()).thenReturn(7L);
        when(securityService.isSuspended(7L)).thenReturn(true);

        PlatformUserDetailsChecker checker = new PlatformUserDetailsChecker(securityService);

        assertThrows(DisabledException.class, () -> checker.check(user));
    }
}
