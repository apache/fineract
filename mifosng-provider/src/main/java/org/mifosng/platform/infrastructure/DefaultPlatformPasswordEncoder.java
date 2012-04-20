package org.mifosng.platform.infrastructure;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.security.authentication.dao.SaltSource;
import org.springframework.security.authentication.encoding.PasswordEncoder;
import org.springframework.stereotype.Service;

@Service(value = "applicationPasswordEncoder")
@Scope("singleton")
public class DefaultPlatformPasswordEncoder implements PlatformPasswordEncoder {

    private final PasswordEncoder passwordEncoder;
    private final SaltSource      saltSource;

    @Autowired
    public DefaultPlatformPasswordEncoder(final PasswordEncoder passwordEncoder, final SaltSource saltSource) {
        this.passwordEncoder = passwordEncoder;
        this.saltSource = saltSource;
    }

    @Override
    public String encode(final PlatformUser appUser) {
        return this.passwordEncoder.encodePassword(appUser.getPassword(), this.saltSource.getSalt(appUser));
    }
}