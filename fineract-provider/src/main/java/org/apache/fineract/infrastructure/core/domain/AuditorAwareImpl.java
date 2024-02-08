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
package org.apache.fineract.infrastructure.core.domain;

import static org.apache.fineract.useradministration.service.AppUserConstants.ADMIN_USER_ID;

import java.util.Optional;
import org.apache.fineract.useradministration.domain.AppUser;
import org.springframework.data.domain.AuditorAware;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;

public class AuditorAwareImpl implements AuditorAware<Long> {

    @Override
    public Optional<Long> getCurrentAuditor() {
        Optional<Long> currentUserId;
        final SecurityContext securityContext = SecurityContextHolder.getContext();
        if (securityContext != null) {
            final Authentication authentication = securityContext.getAuthentication();
            if (authentication != null) {
                currentUserId = Optional.ofNullable(((AppUser) authentication.getPrincipal()).getId());
            } else {
                currentUserId = retrieveSuperUser();
            }
        } else {
            currentUserId = retrieveSuperUser();
        }
        return currentUserId;
    }

    private Optional<Long> retrieveSuperUser() {
        return Optional.of(ADMIN_USER_ID); // TODO change to SYSTEM_USER_ID and add rights
    }
}
