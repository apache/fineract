package org.mifosng.platform.user.domain;

import org.mifosng.platform.infrastructure.EmailDetail;
import org.mifosng.platform.infrastructure.PlatformEmailService;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.RandomPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaUserDomainService implements UserDomainService {

    @Autowired
    private AppUserRepository userRepository;

    @Autowired
    private PlatformPasswordEncoder applicationPasswordEncoder;

    @Autowired
    private PlatformEmailService emailService;

    @Transactional
    @Override
    public void create(final AppUser appUser) {
        generateKeyUsedForPasswordSalting(appUser);

        String unencodedPassword = appUser.getPassword();
        if (org.apache.commons.lang.StringUtils.isBlank(unencodedPassword) || "autogenerate".equalsIgnoreCase(unencodedPassword)) {
        	unencodedPassword = new RandomPasswordGenerator(13).generate();
        	appUser.updatePassword(unencodedPassword);
        }
        
        String encodePassword = this.applicationPasswordEncoder.encode(appUser);
		appUser.updatePassword(encodePassword);

		this.userRepository.save(appUser);
        
        EmailDetail emailDetail = new EmailDetail(appUser.getFirstname(), appUser.getFirstname(), appUser.getEmail(), appUser.getUsername());

        this.emailService.sendToUserAccount(emailDetail, unencodedPassword);
    }

	private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
		this.userRepository.save(appUser);
	}
}