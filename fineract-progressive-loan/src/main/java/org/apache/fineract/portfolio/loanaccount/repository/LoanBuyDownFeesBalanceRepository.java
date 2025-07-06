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
import org.apache.fineract.portfolio.loanaccount.domain.LoanBuyDownFeeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LoanBuyDownFeesBalanceRepository
        extends JpaRepository<LoanBuyDownFeeBalance, Long>, JpaSpecificationExecutor<LoanBuyDownFeeBalance> {

    String FIND_BALANCE_REPAYMENT_SCHEDULE_DATA = "SELECT new org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData(lbfb.loanTransaction.id, lbfb.loan.id, lbfb.loanTransaction.dateOf, lbfb.loanTransaction.reversed, lbfb.amount, lbfb.unrecognizedAmount, lbfb.loanTransaction.feeChargesPortion) FROM LoanBuyDownFeeBalance lbfb ";

    List<LoanBuyDownFeeBalance> findAllByLoanId(Long loanId);

    LoanBuyDownFeeBalance findByLoanIdAndLoanTransactionId(Long loanId, Long transactionId);

    @Query(FIND_BALANCE_REPAYMENT_SCHEDULE_DATA + " WHERE lbfb.loan.id = :loanId")
    List<LoanTransactionRepaymentPeriodData> findRepaymentPeriodDataByLoanId(Long loanId);

    @Query("SELECT SUM(lbfb.amount) FROM LoanBuyDownFeeBalance lbfb WHERE lbfb.loan.id = :loanId")
    BigDecimal calculateBuydownFeeAmount(Long loanId);

    @Query("SELECT SUM(lbfb.amountAdjustment) FROM LoanBuyDownFeeBalance lbfb WHERE lbfb.loan.id = :loanId")
    BigDecimal calculateBuydownFeeAdjustment(Long loanId);

    @Query("SELECT lbfb FROM LoanBuyDownFeeBalance lbfb, LoanTransaction lt, LoanTransactionRelation ltr WHERE lt.loan.id = lbfb.loan.id AND ltr.fromTransaction.id =:transactionId AND ltr.toTransaction.id=lt.id AND lbfb.loanTransaction.id = lt.id")
    LoanBuyDownFeeBalance findBalanceForAdjustment(Long transactionId);
}
