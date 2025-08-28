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

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationBaseTransactionDTO;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface LoanCapitalizedIncomeBalanceRepository extends JpaRepository<LoanCapitalizedIncomeBalance, Long>,
        JpaSpecificationExecutor<LoanCapitalizedIncomeBalance>, CustomizedLoanCapitalizedIncomeBalanceRepository {

    String FIND_BALANCE_REPAYMENT_SCHEDULE_DATA = "SELECT new org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData(lcib.loanTransaction.id, lcib.loan.id, lcib.loanTransaction.dateOf, lcib.loanTransaction.reversed, lcib.amount, lcib.unrecognizedAmount, lcib.loanTransaction.feeChargesPortion) FROM LoanCapitalizedIncomeBalance lcib ";

    @Query("SELECT lcib FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id=:loanId AND lcib.closed = FALSE ORDER BY lcib.date, lcib.createdDate")
    List<LoanCapitalizedIncomeBalance> findAllByLoanIdAndClosedFalse(@Param("loanId") Long loanId);

    @Query("SELECT lcib FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id=:loanId AND lcib.closed = FALSE AND lcib.deleted = FALSE ORDER BY lcib.date, lcib.createdDate")
    List<LoanCapitalizedIncomeBalance> findAllByLoanIdAndDeletedFalseAndClosedFalse(@Param("loanId") Long loanId);

    LoanCapitalizedIncomeBalance findByLoanIdAndLoanTransactionIdAndDeletedFalseAndClosedFalse(Long loanId, Long transactionId);

    @Query(FIND_BALANCE_REPAYMENT_SCHEDULE_DATA
            + " WHERE lcib.loan.id = :loanId AND lcib.deleted = false AND lcib.closed = false ORDER BY lcib.date, lcib.createdDate")
    List<LoanTransactionRepaymentPeriodData> findRepaymentPeriodDataByLoanId(Long loanId);

    @Query("SELECT SUM(lcib.amount) FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id = :loanId AND lcib.deleted = false AND lcib.closed = false")
    BigDecimal calculateCapitalizedIncome(Long loanId);

    @Query("SELECT SUM(lcib.amountAdjustment) FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id = :loanId AND lcib.deleted = false AND lcib.closed = false")
    BigDecimal calculateCapitalizedIncomeAdjustment(Long loanId);

    @Query("SELECT lcib FROM LoanCapitalizedIncomeBalance lcib, LoanTransaction lt, LoanTransactionRelation ltr WHERE lt.loan.id = lcib.loan.id AND ltr.fromTransaction.id =:transactionId AND ltr.toTransaction.id=lt.id AND lcib.loanTransaction.id = lt.id AND lcib.deleted = false AND lcib.closed = false")
    LoanCapitalizedIncomeBalance findBalanceForAdjustment(Long transactionId);

    @Query("""
            SELECT new org.apache.fineract.portfolio.loanaccount.data.AmortizationAllocationBaseTransactionDTO(
                l.id, l.externalId, lcib.loanTransaction.id, lcib.date, lcib.amount,
                lcib.unrecognizedAmount, lcib.chargedOffAmount, lcib.amountAdjustment
            ) FROM LoanCapitalizedIncomeBalance lcib JOIN lcib.loan l JOIN lcib.loanTransaction bt
            WHERE lcib.loanTransaction.id = :loanTransactionId AND l.id = :loanId
            """)
    AmortizationAllocationBaseTransactionDTO findBaseTransactionInfo(@Param("loanTransactionId") Long loanTransactionId,
            @Param("loanId") Long loanId);
}
