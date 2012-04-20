package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeRepository extends JpaRepository<Office, Long>,
		JpaSpecificationExecutor<Office> {
    // no added behaviour
}
