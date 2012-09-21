package org.mifosng.platform.loan.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanChargeRepository extends JpaRepository<LoanCharge, Long>, JpaSpecificationExecutor<LoanCharge> {
    // no added behaviour
}
