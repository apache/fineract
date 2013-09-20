/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.account.domain;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface AccountTransferRepository extends JpaRepository<AccountTransfer, Long>, JpaSpecificationExecutor<AccountTransfer> {

    @Query("from AccountTransfer at where at.fromLoanAccount.id= :accountNumber and at.reversed=false")
    List<AccountTransfer> findByFromLoanId(@Param("accountNumber") Long accountNumber);

    @Query("from AccountTransfer at where (at.fromLoanAccount.id= :accountNumber or at.toLoanAccount.id=:accountNumber) and at.reversed=false")
    List<AccountTransfer> findAllByLoanId(@Param("accountNumber") Long accountNumber);
}