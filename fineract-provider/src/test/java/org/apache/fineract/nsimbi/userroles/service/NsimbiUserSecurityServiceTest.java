/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.when;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneOffset;
import java.util.Optional;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserSecurityProfile;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserSecurityProfileRepository;
import org.junit.jupiter.api.Test;

class NsimbiUserSecurityServiceTest {

    @Test
    void recordsLastLoginAtUsingInjectedClock() {
        NsimbiUserSecurityProfileRepository repository = org.mockito.Mockito.mock(NsimbiUserSecurityProfileRepository.class);
        NsimbiUserSecurityProfile profile = new NsimbiUserSecurityProfile(1L);
        when(repository.findByAppUserId(1L)).thenReturn(Optional.of(profile));
        Clock clock = Clock.fixed(Instant.parse("2026-09-15T12:00:00Z"), ZoneOffset.UTC);

        new NsimbiUserSecurityService(repository, clock).recordSuccessfulLogin(1L);

        assertEquals("2026-09-15T12:00Z", profile.getLastLoginAt().toString());
    }
}
