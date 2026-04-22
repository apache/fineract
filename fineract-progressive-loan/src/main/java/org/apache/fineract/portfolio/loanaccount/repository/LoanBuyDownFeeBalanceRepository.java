/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.loanaccount.repository;

import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationBaseTransactionDTO;
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanBuyDownFeeBalanceRepository
        extends JpaRepository<LoanBuyDownFeeBalance, Long>, JpaSpecificationExecutor<LoanBuyDownFeeBalance> {

    @Query("SELECT lbdfb FROM LoanBuyDownFeeBalance lbdfb WHERE lbdfb.loan.id=:loanId AND lbdfb.closed = FALSE ORDER BY lbdfb.date, lbdfb.createdDate")
    List<LoanBuyDownFeeBalance> findAllByLoanIdAndClosedFalse(@Param("loanId") Long loanId);

    @Query("SELECT lbdfb FROM LoanBuyDownFeeBalance lbdfb WHERE lbdfb.loan.id=:loanId AND lbdfb.closed = FALSE AND lbdfb.deleted = FALSE ORDER BY lbdfb.date, lbdfb.createdDate")
    List<LoanBuyDownFeeBalance> findAllByLoanIdAndDeletedFalseAndClosedFalse(@Param("loanId") Long loanId);

    LoanBuyDownFeeBalance findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(Long loanId, Long transactionId);

    @Query("SELECT lbdfb FROM LoanBuyDownFeeBalance lbdfb, LoanTransaction lt, LoanTransactionRelation ltr WHERE lt.loan.id = lbdfb.loan.id AND ltr.fromTransaction.id =:transactionId AND ltr.toTransaction.id=lt.id AND lbdfb.loanTransaction.id = lt.id AND lbdfb.deleted = false AND lbdfb.closed = false")
    LoanBuyDownFeeBalance findBalanceForAdjustment(@Param("transactionId") Long transactionId);

    @Query("""
            SELECT new org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationBaseTransactionDTO(
                l.id, l.externalId, lbdfb.loanTransaction.id, lbdfb.date, lbdfb.amount,
                lbdfb.unrecognizedAmount, lbdfb.chargedOffAmount, lbdfb.amountAdjustment
            ) FROM LoanBuyDownFeeBalance lbdfb JOIN lbdfb.loan l JOIN lbdfb.loanTransaction bt
            WHERE lbdfb.loanTransaction.id = :loanTransactionId AND l.id = :loanId
            """)
    AmortizationAllocationBaseTransactionDTO findBaseTransactionInfo(@Param("loanTransactionId") Long loanTransactionId,
            @Param("loanId") Long loanId);
}
