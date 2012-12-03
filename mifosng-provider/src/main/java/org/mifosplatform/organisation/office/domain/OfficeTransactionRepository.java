package org.mifosplatform.organisation.office.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface OfficeTransactionRepository extends
		JpaRepository<OfficeTransaction, Long>,
		JpaSpecificationExecutor<OfficeTransaction> {
	// no added behaviour
}
