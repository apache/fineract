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

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualActivityProcessingServiceImpl implements LoanAccrualActivityProcessingService {

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanWritePlatformService loanWritePlatformService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void makeAccrualActivityTransaction(Long loanId, final LocalDate currentDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        makeAccrualActivityTransaction(loan, currentDate);
    }

    @Override
    public Loan makeAccrualActivityTransaction(Loan loan, final LocalDate currentDate) {
        if (loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            // check if loan has installment due on business day
            Optional<LoanRepaymentScheduleInstallment> first = loan.getRepaymentScheduleInstallments().stream()
                    .filter(loanRepaymentScheduleInstallment -> loanRepaymentScheduleInstallment.getDueDate().isEqual(currentDate))
                    .findFirst();
            if (first.isPresent()) {
                final LoanRepaymentScheduleInstallment installment = first.get();
                // check if there is no not-replayed-accrual-activity related to business date
                List<LoanTransaction> loanTransactions = loan.getLoanTransactions(loanTransaction -> loanTransaction.isNotReversed()
                        && loanTransaction.isAccrualActivity() && loanTransaction.getTransactionDate().isEqual(currentDate));
                if (loanTransactions.isEmpty()) {
                    loan = loanWritePlatformService.makeAccrualActivityTransaction(loan, installment, currentDate);
                }
            }
        }
        return loan;
    }

}
