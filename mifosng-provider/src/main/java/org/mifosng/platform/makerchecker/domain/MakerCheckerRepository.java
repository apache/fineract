package org.mifosng.platform.makerchecker.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface MakerCheckerRepository extends
		JpaRepository<MakerChecker, Long>,
		JpaSpecificationExecutor<MakerChecker> {
	// no added behaviour
}