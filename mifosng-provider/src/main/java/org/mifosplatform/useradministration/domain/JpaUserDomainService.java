/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.mifosplatform.infrastructure.core.domain.EmailDetail;
import org.mifosplatform.infrastructure.core.service.PlatformEmailService;
import org.mifosplatform.infrastructure.security.service.PlatformPasswordEncoder;
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
            final EmailDetail emailDetail = new EmailDetail(appUser.getOffice().getName(), appUser.getFirstname(), appUser.getEmail(),
                    appUser.getUsername());

            this.emailService.sendToUserAccount(emailDetail, unencodedPassword);
        }
    }

    private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
        this.userRepository.save(appUser);
    }
}