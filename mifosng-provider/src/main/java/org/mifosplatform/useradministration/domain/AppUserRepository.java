package org.mifosplatform.useradministration.domain;

import org.mifosplatform.infrastructure.security.domain.PlatformUserRepository;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface AppUserRepository extends JpaRepository<AppUser, Long>, JpaSpecificationExecutor<AppUser>, PlatformUserRepository {
	// no behaviour added
}
