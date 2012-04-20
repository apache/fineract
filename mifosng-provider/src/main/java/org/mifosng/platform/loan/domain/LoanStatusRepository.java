package org.mifosng.platform.loan.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanStatusRepository extends JpaRepository<LoanStatus, Long>, JpaSpecificationExecutor<LoanStatus> {
	// no added behaviour
}