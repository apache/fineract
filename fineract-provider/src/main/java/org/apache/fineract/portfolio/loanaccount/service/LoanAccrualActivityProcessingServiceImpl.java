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
import java.util.Comparator;
import java.util.List;
import java.util.Optional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualActivityProcessingServiceImpl implements LoanAccrualActivityProcessingService {

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final LoanWritePlatformService loanWritePlatformService;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;

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

    @Override
    @Transactional
    public void processAccrualActivityForLoanClosure(Loan loan) {
        LocalDate date = loan.isOverPaid() ? loan.getOverpaidOnDate() : loan.getClosedOnDate();
        List<LoanTransaction> accrualActivityTransaction = loan.getLoanTransactions().stream().filter(LoanTransaction::isNotReversed)
                .filter(LoanTransaction::isAccrualActivity).filter(loanTransaction -> loanTransaction.getDateOf().isAfter(date)).toList();
        if (!accrualActivityTransaction.isEmpty()) {
            accrualActivityTransaction.forEach(this::reverseAccrualActivityTransaction);
        }
        LoanTransaction loanTransaction = assembleClosingAccrualActivityTransaction(loan, date);
        if (loanTransaction.getAmount().compareTo(BigDecimal.ZERO) != 0) {
            loanWritePlatformService.makeAccrualActivityTransaction(loan, loanTransaction);
        }
    }

    private void reverseAccrualActivityTransaction(LoanTransaction loanTransaction) {
        loanTransaction.reverse();
        LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
    }

    private LoanTransaction assembleClosingAccrualActivityTransaction(Loan loan, LocalDate date) {
        // collect fees
        BigDecimal feeChargesPortion = BigDecimal.ZERO;
        // collect penalties
        BigDecimal penaltyChargesPortion = BigDecimal.ZERO;
        // collect interests
        BigDecimal interestPortion = BigDecimal.ZERO;
        var currency = loan.getCurrency();
        // sum up all accruals
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            feeChargesPortion = installment.getFeeAccrued(currency).getAmount().add(feeChargesPortion);
            penaltyChargesPortion = installment.getPenaltyAccrued(currency).getAmount().add(penaltyChargesPortion);
            interestPortion = installment.getInterestAccrued(currency).getAmount().add(interestPortion);
        }
        List<LoanTransaction> accrualActivities = loan.getLoanTransactions().stream().filter(LoanTransaction::isAccrualActivity)
                .filter(LoanTransaction::isNotReversed).toList();
        // subtract already Posted accruals
        for (LoanTransaction accrualActivity : accrualActivities) {
            if (accrualActivity.getFeeChargesPortion() != null) {
                feeChargesPortion = feeChargesPortion.subtract(accrualActivity.getFeeChargesPortion());
            }
            if (accrualActivity.getPenaltyChargesPortion() != null) {
                penaltyChargesPortion = penaltyChargesPortion.subtract(accrualActivity.getPenaltyChargesPortion());
            }
            if (accrualActivity.getInterestPortion() != null) {
                interestPortion = interestPortion.subtract(accrualActivity.getInterestPortion());
            }
        }

        BigDecimal transactionAmount = feeChargesPortion.add(penaltyChargesPortion).add(interestPortion);
        ExternalId externalId = externalIdFactory.create();

        return new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.ACCRUAL_ACTIVITY.getValue(), date, transactionAmount, null,
                interestPortion, feeChargesPortion, penaltyChargesPortion, null, false, null, externalId);
    }

    @Override
    @Transactional
    public void processAccrualActivityForLoanReopen(Loan loan) {
        LoanTransaction lastAccrualActivityMarkedToReverse = null;
        List<LoanTransaction> accrualActivityTransaction = loan.getLoanTransactions().stream()
                .filter(loanTransaction -> loanTransaction.isNotReversed() && loanTransaction.isAccrualActivity())
                .sorted(Comparator.comparing(LoanTransaction::getDateOf)).toList();
        // grab the latest AccrualActivityTransaction
        // it does not matter if it is on an installment due date or not because it was posted due to loan close
        if (!accrualActivityTransaction.isEmpty()) {
            lastAccrualActivityMarkedToReverse = accrualActivityTransaction.get(accrualActivityTransaction.size() - 1);
        }
        final LocalDate lastAccrualActivityTransactionDate = lastAccrualActivityMarkedToReverse == null ? null
                : lastAccrualActivityMarkedToReverse.getDateOf();
        LocalDate today = DateUtils.getBusinessLocalDate();
        final List<LoanRepaymentScheduleInstallment> installmentsBetweenBusinessDateAndLastAccrualActivityTransactionDate = loan
                .getRepaymentScheduleInstallments().stream()
                .filter(installment -> installment.getDueDate().isBefore(today) && (lastAccrualActivityTransactionDate == null
                        || installment.getDueDate().isAfter(lastAccrualActivityTransactionDate)
                        // if close event happened on installment due date
                        // we should reverse replay it to calculate installment related accrual parts only
                        || installment.getDueDate().isEqual(lastAccrualActivityTransactionDate)))
                .sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate)).toList();
        for (LoanRepaymentScheduleInstallment installment : installmentsBetweenBusinessDateAndLastAccrualActivityTransactionDate) {
            if (lastAccrualActivityMarkedToReverse != null) {
                loanWritePlatformService.reverseReplayAccrualActivityTransaction(loan, lastAccrualActivityMarkedToReverse, installment,
                        installment.getDueDate());
                lastAccrualActivityMarkedToReverse = null;
            } else {
                loanWritePlatformService.makeAccrualActivityTransaction(loan, installment, installment.getDueDate());
            }
        }
        if (lastAccrualActivityMarkedToReverse != null) {
            reverseAccrualActivityTransaction(lastAccrualActivityMarkedToReverse);
        }
    }

}
