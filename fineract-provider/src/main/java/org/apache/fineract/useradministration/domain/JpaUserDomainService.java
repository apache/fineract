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
package org.apache.fineract.useradministration.domain;

import org.apache.fineract.infrastructure.core.service.PlatformEmailService;
import org.apache.fineract.infrastructure.security.service.PlatformPasswordEncoder;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaUserDomainService implements UserDomainService {

    private final AppUserRepository userRepository;
    private final PlatformPasswordEncoder applicationPasswordEncoder;
    private final PlatformEmailService emailService;

    @Autowired
    public JpaUserDomainService(final AppUserRepository userRepository, final PlatformPasswordEncoder applicationPasswordEncoder,
            final PlatformEmailService emailService) {
        this.userRepository = userRepository;
        this.applicationPasswordEncoder = applicationPasswordEncoder;
        this.emailService = emailService;
    }

    @Transactional
    @Override
    public void create(final AppUser appUser, final Boolean sendPasswordToEmail) {

        generateKeyUsedForPasswordSalting(appUser);

        final String unencodedPassword = appUser.getPassword();

        final String encodePassword = this.applicationPasswordEncoder.encode(appUser);
        appUser.updatePassword(encodePassword);

        this.userRepository.saveAndFlush(appUser);

        if (sendPasswordToEmail.booleanValue()) {
            this.emailService.sendToUserAccount(appUser.getOffice().getName(),
                    appUser.getFirstname(), appUser.getEmail(), appUser.getUsername(), unencodedPassword);
        }
    }

    private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
        this.userRepository.save(appUser);
    }
}