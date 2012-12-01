package org.mifosplatform.portfolio.loanaccount.domain;

import org.mifosplatform.portfolio.loanproduct.domain.LoanTransactionProcessingStrategy;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface LoanTransactionProcessingStrategyRepository extends JpaRepository<LoanTransactionProcessingStrategy, Long>,
		JpaSpecificationExecutor<LoanTransactionProcessingStrategy> {
	// no added behaviour
}