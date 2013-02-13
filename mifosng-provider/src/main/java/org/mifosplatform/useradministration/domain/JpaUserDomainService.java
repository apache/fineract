/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.domain;

import org.mifosplatform.infrastructure.core.domain.EmailDetail;
import org.mifosplatform.infrastructure.core.service.PlatformEmailService;
import org.mifosplatform.infrastructure.security.service.PlatformPasswordEncoder;
import org.mifosplatform.infrastructure.security.service.RandomPasswordGenerator;
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
    public void create(final AppUser appUser) {

        generateKeyUsedForPasswordSalting(appUser);

        String unencodedPassword = appUser.getPassword();
        if (org.apache.commons.lang.StringUtils.isBlank(unencodedPassword) || "autogenerate".equalsIgnoreCase(unencodedPassword)) {
            unencodedPassword = new RandomPasswordGenerator(13).generate();
            appUser.updatePassword(unencodedPassword);
        }

        final String encodePassword = this.applicationPasswordEncoder.encode(appUser);
        appUser.updatePassword(encodePassword);

        this.userRepository.save(appUser);

        final EmailDetail emailDetail = new EmailDetail(appUser.getFirstname(), appUser.getFirstname(), appUser.getEmail(),
                appUser.getUsername());

        this.emailService.sendToUserAccount(emailDetail, unencodedPassword);
    }

    private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
        this.userRepository.save(appUser);
    }
}