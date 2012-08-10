package org.mifosng.platform.deposit.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface DepositProductRepository extends
		JpaRepository<DepositProduct, Long>,
		JpaSpecificationExecutor<DepositProduct> {

}
