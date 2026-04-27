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
package org.apache.fineract.useradministration.service;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.PlatformEmailService;
import org.apache.fineract.infrastructure.security.service.RandomPasswordGenerator;
import org.apache.fineract.useradministration.domain.AppUser;
import org.apache.fineract.useradministration.domain.AppUserRepository;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class ForgotPasswordServiceImpl implements ForgotPasswordService {

    private static final int TEMPORARY_PASSWORD_LENGTH = 13;
    private static final int TEMPORARY_PASSWORD_EXPIRY_HOURS = 1;

    private final AppUserRepository appUserRepository;
    private final PasswordEncoder passwordEncoder;
    private final PlatformEmailService emailService;

    @Override
    @Transactional
    public void requestPasswordReset(final String email) {
        final AppUser user = this.appUserRepository.findActiveUserByEmail(email);
        if (user == null) {
            log.debug("Password reset requested for non-existent or inactive email: {}", email);
            return;
        }

        if (!user.isPasswordResetAllowed()) {
            log.debug("Password reset is disabled for user: {}", user.getUsername());
            return;
        }

        final String temporaryPassword = new RandomPasswordGenerator(TEMPORARY_PASSWORD_LENGTH).generate();
        final String encodedPassword = this.passwordEncoder.encode(temporaryPassword);
        final OffsetDateTime expiryTime = OffsetDateTime.now(ZoneOffset.UTC).plusHours(TEMPORARY_PASSWORD_EXPIRY_HOURS);

        user.updateTemporaryPassword(encodedPassword, expiryTime);
        this.appUserRepository.saveAndFlush(user);

        final String organisationName = user.getOffice().getName();
        final String contactName = user.getFirstname() + " " + user.getLastname();

        this.emailService.sendForgotPasswordEmail(organisationName, contactName, email, user.getUsername(), temporaryPassword);

        log.info("Password reset email sent to user: {}", user.getUsername());
    }
}
