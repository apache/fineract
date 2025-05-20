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
import org.apache.fineract.portfolio.loanaccount.domain.LoanCapitalizedIncomeBalance;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;

public interface LoanCapitalizedIncomeBalanceRepository
        extends JpaRepository<LoanCapitalizedIncomeBalance, Long>, JpaSpecificationExecutor<LoanCapitalizedIncomeBalance> {

    String FIND_BALANCE_REPAYMENT_SCHEDULE_DATA = "SELECT new org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepaymentPeriodData(lcib.loanTransaction.id, lcib.loan.id, lcib.loanTransaction.dateOf, lcib.loanTransaction.reversed, lcib.amount, lcib.unrecognizedAmount, lcib.loanTransaction.feeChargesPortion) FROM LoanCapitalizedIncomeBalance lcib ";

    List<LoanCapitalizedIncomeBalance> findAllByLoanId(Long loanId);

    LoanCapitalizedIncomeBalance findByLoanIdAndLoanTransactionId(Long loanId, Long transactionId);

    @Query(FIND_BALANCE_REPAYMENT_SCHEDULE_DATA + " WHERE lcib.loan.id = :loanId")
    List<LoanTransactionRepaymentPeriodData> findRepaymentPeriodDataByLoanId(Long loanId);

    @Query("SELECT SUM(lcib.amount) FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id = :loanId")
    BigDecimal calculateCapitalizedIncome(Long loanId);

    @Query("SELECT SUM(lcib.amountAdjustment) FROM LoanCapitalizedIncomeBalance lcib WHERE lcib.loan.id = :loanId")
    BigDecimal calculateCapitalizedIncomeAdjustment(Long loanId);

    @Query("SELECT lcib FROM LoanCapitalizedIncomeBalance lcib, LoanTransaction lt, LoanTransactionRelation ltr WHERE lt.loan.id = lcib.loan.id AND ltr.fromTransaction.id =:transactionId AND ltr.toTransaction.id=lt.id AND lcib.loanTransaction.id = lt.id")
    LoanCapitalizedIncomeBalance findBalanceForAdjustment(Long transactionId);
}
