package org.mifosplatform.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanTransactionRepository extends JpaRepository<LoanTransaction, Long>,
		JpaSpecificationExecutor<LoanTransaction> {
	// no added behaviour
}