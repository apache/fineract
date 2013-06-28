package org.mifosplatform.portfolio.loanaccount.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanCycleRepository extends JpaRepository<LoanCycle, Long>, JpaSpecificationExecutor<LoanCycle> {

    @Query("from LoanCycle lc where lc.client.id=:clientId and lc.loanProduct.id=:loanProductId order by lc.runningCount")
    List<LoanCycle> findByClientIdAndLoanProductId(@Param("clientId") Long clientId, @Param("loanProductId") Long loanProductId);

}
