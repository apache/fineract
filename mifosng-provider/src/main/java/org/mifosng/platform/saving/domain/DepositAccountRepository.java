package org.mifosng.platform.saving.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepositAccountRepository extends
		JpaRepository<DepositAccount, Long>,
		JpaSpecificationExecutor<DepositAccount> {

}
