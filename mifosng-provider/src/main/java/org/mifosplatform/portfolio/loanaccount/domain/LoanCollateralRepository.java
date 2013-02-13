package org.mifosplatform.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanCollateralRepository extends JpaRepository<LoanCollateral, Long>, JpaSpecificationExecutor<LoanCollateral> {
    // no added behaviour
}
