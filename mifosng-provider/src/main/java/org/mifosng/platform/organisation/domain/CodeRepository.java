package org.mifosng.platform.organisation.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CodeRepository extends JpaRepository<Office, Long>,
		JpaSpecificationExecutor<Code> {
    // no added behaviour
}
