package org.mifosng.platform.user.domain;

import org.mifosng.platform.client.service.RollbackTransactionAsCommandIsNotApprovedByCheckerException;
import org.mifosng.platform.infrastructure.EmailDetail;
import org.mifosng.platform.infrastructure.PlatformEmailService;
import org.mifosng.platform.infrastructure.PlatformPasswordEncoder;
import org.mifosng.platform.infrastructure.RandomPasswordGenerator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class JpaUserDomainService implements UserDomainService {

    private final AppUserRepository userRepository;
    private final PlatformPasswordEncoder applicationPasswordEncoder;
    private final PlatformEmailService emailService;
    private final PermissionRepository permissionRepository;
    
    @Autowired
    public JpaUserDomainService(final AppUserRepository userRepository, final PlatformPasswordEncoder applicationPasswordEncoder,
            final PlatformEmailService emailService, final PermissionRepository permissionRepository) {
        this.userRepository = userRepository;
        this.applicationPasswordEncoder = applicationPasswordEncoder;
        this.emailService = emailService;
        this.permissionRepository = permissionRepository;
    }
    
    @Transactional
    @Override
    public void create(final AppUser appUser, final boolean isApprovedByChecker) {
        
        generateKeyUsedForPasswordSalting(appUser);

        String unencodedPassword = appUser.getPassword();
        if (org.apache.commons.lang.StringUtils.isBlank(unencodedPassword) || "autogenerate".equalsIgnoreCase(unencodedPassword)) {
            unencodedPassword = new RandomPasswordGenerator(13).generate();
            appUser.updatePassword(unencodedPassword);
        }

        final String encodePassword = this.applicationPasswordEncoder.encode(appUser);
        appUser.updatePassword(encodePassword);

        this.userRepository.save(appUser);
        
        final Permission thisTask = this.permissionRepository.findOneByCode("CREATE_USER");
        if (thisTask.hasMakerCheckerEnabled() && !isApprovedByChecker) { throw new RollbackTransactionAsCommandIsNotApprovedByCheckerException(); }

        final EmailDetail emailDetail = new EmailDetail(appUser.getFirstname(), appUser.getFirstname(), appUser.getEmail(), appUser.getUsername());

        this.emailService.sendToUserAccount(emailDetail, unencodedPassword);
    }

    private void generateKeyUsedForPasswordSalting(final AppUser appUser) {
        this.userRepository.save(appUser);
    }
}