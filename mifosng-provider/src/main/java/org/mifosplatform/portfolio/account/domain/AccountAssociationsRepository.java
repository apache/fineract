package org.mifosplatform.portfolio.account.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountAssociationsRepository extends JpaRepository<AccountAssociations, Long>,
        JpaSpecificationExecutor<AccountAssociations> {

    @Query("from AccountAssociations aa where aa.loanAccount.id= :loanId")
    AccountAssociations findByLoanId(@Param("loanId") Long loanId);
 
}
