package org.apache.fineract.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanRepaymentReminderRepository
        extends JpaRepository<LoanRepaymentReminder, Long>, JpaSpecificationExecutor<LoanRepaymentReminder> {

}
