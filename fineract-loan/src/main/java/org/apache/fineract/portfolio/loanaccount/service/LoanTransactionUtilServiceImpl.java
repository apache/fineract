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
package org.apache.fineract.portfolio.loanaccount.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanCharge;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.transaction.annotation.Transactional;

@RequiredArgsConstructor
@Transactional(readOnly = true)
public class LoanTransactionUtilServiceImpl implements LoanTransactionUtilService {

    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    public boolean shouldGenerateRepaymentSchedule(final Loan loan) {
        final boolean hasContractTerminationTransaction = hasContractTerminationTransaction(loan);
        final boolean hasChargeOffTransaction = hasChargeOffTransaction(loan);
        return (loan.isProgressiveSchedule()
                && ((hasChargeOffTransaction && loan.hasAccelerateChargeOffStrategy()) || hasContractTerminationTransaction));
    }

    @Override
    public boolean hasContractTerminationTransaction(final Loan loan) {
        return loanTransactionRepository.hasLoanTransactionByType(loan, LoanTransactionType.CONTRACT_TERMINATION);
    }

    @Override
    public boolean hasChargeOffTransaction(final Loan loan) {
        return loanTransactionRepository.hasLoanTransactionByType(loan, LoanTransactionType.CHARGE_OFF);
    }

    @Override
    public boolean isChargeOffOnDate(final Loan loan, final LocalDate onDate) {
        final LoanTransaction chargeOffTransaction = loanTransactionRepository.findLoanTransactionByType(loan,
                LoanTransactionType.CHARGE_OFF);
        return chargeOffTransaction != null && chargeOffTransaction.getDateOf().compareTo(onDate) <= 0;
    }

    @Override
    public boolean hasMonetaryActivityAfter(final Loan loan, final LocalDate transactionDate) {
        if (loanTransactionRepository.hasMonetaryActivityAfter(loan, transactionDate)) {
            return true;
        }
        for (LoanCharge loanCharge : loan.getLoanCharges()) {
            if (!loanCharge.determineIfFullyPaid() && loanCharge.getSubmittedOnDate().isAfter(transactionDate)) {
                return true;
            }
        }
        return false;
    }

    @Override
    public LoanTransaction getLastUserTransaction(final Loan loan) {
        List<LoanTransaction> loanTransactions = loanTransactionRepository.findLastUserTransactionsByLoan(loan, PageRequest.of(0, 1));
        return (!loanTransactions.isEmpty()) ? loanTransactions.get(0) : null;
    }

    @Override
    public BigDecimal sumInterestPortionTillChargeOffDate(final Loan loan, final LocalDate chargeOffDate,
            final LoanTransactionType transactionType) {
        return loanTransactionRepository.sumInterestPortionAmountTillDate(loan, chargeOffDate, transactionType);
    }

    @Override
    public List<LoanTransaction> findPaymentTransactionsByLoan(final Loan loan) {
        return loanTransactionRepository.findPaymentTransactionsByLoan(loan);
    }

}
