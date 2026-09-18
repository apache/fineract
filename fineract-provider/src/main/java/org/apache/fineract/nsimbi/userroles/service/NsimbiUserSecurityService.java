/**
 * Licensed to the Apache Software Foundation (ASF) under one or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information regarding copyright ownership. The ASF licenses this file to you under the Apache
 * License, Version 2.0 (the "License"); you may not use this file except in compliance with the License. You may obtain a copy of the
 * License at http://www.apache.org/licenses/LICENSE-2.0. Unless required by applicable law or agreed to in writing, software distributed
 * under the License is distributed on an "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied. See the
 * License for the specific language governing permissions and limitations under the License.
 */
package org.apache.fineract.nsimbi.userroles.service;

import java.time.Clock;
import java.time.OffsetDateTime;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserSecurityProfile;
import org.apache.fineract.nsimbi.userroles.domain.NsimbiUserSecurityProfileRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class NsimbiUserSecurityService {

    private final NsimbiUserSecurityProfileRepository profiles;
    private final Clock clock;

    public boolean isSuspended(Long userId) {
        return userId != null && this.profiles.findByAppUserId(userId).map(NsimbiUserSecurityProfile::isSuspended).orElse(false);
    }

    @Transactional
    public void recordSuccessfulLogin(Long userId) {
        profile(userId).recordSuccessfulLogin(OffsetDateTime.now(this.clock));
    }

    @Transactional
    public void suspend(Long userId, Long actorId) {
        profile(userId).suspend(actorId, OffsetDateTime.now(this.clock));
    }

    @Transactional
    public void reactivate(Long userId) {
        profile(userId).reactivate();
    }

    private NsimbiUserSecurityProfile profile(Long userId) {
        if (userId == null) {
            throw new IllegalArgumentException("userId must not be null");
        }
        return this.profiles.findByAppUserId(userId).orElseGet(() -> this.profiles.save(new NsimbiUserSecurityProfile(userId)));
    }
}
