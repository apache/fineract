package org.apache.fineract.portfolio.loanaccount.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface GroupLoanIndividualMonitoringRepository extends JpaRepository<GroupLoanIndividualMonitoring, Long>, JpaSpecificationExecutor<GroupLoanIndividualMonitoring> {
	
}
