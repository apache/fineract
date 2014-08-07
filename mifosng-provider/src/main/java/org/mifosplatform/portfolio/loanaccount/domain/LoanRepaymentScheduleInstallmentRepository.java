package org.mifosplatform.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepaymentScheduleInstallmentRepository extends JpaRepository<LoanRepaymentScheduleInstallment, Long>,
        JpaSpecificationExecutor<LoanRepaymentScheduleInstallment> {

}
