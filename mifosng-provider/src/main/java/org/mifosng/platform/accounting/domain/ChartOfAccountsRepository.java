package org.mifosng.platform.accounting.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface ChartOfAccountsRepository extends
		JpaRepository<ChartOfAccounts, Long>,
		JpaSpecificationExecutor<ChartOfAccounts> {
	// no added behaviour
}
