package org.mifosplatform.portfolio.loanproduct.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanProductRepository extends JpaRepository<LoanProduct, Long>, JpaSpecificationExecutor<LoanProduct> {
    // no behaviour added
}