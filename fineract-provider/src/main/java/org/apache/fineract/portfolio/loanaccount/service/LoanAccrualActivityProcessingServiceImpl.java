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

import jakarta.validation.constraints.NotNull;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.infrastructure.event.business.domain.loan.LoanAdjustTransactionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionAccrualActivityPostBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.loan.transaction.LoanTransactionAccrualActivityPreBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountDomainService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Component
@RequiredArgsConstructor
public class LoanAccrualActivityProcessingServiceImpl implements LoanAccrualActivityProcessingService {

    private final LoanRepositoryWrapper loanRepositoryWrapper;
    private final ExternalIdFactory externalIdFactory;
    private final BusinessEventNotifierService businessEventNotifierService;
    private final LoanTransactionAssembler loanTransactionAssembler;
    private final LoanAccountDomainService loanAccountDomainService;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void makeAccrualActivityTransaction(@NotNull Long loanId, @NotNull LocalDate currentDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        makeAccrualActivityTransaction(loan, currentDate);
    }

    @Override
    public void makeAccrualActivityTransaction(@NotNull Loan loan, @NotNull LocalDate currentDate) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            return;
        }
        // check if loan has installment in the past or due on current date
        List<LoanRepaymentScheduleInstallment> installments = loan
                .getRepaymentScheduleInstallments(i -> !i.isDownPayment() && !DateUtils.isBefore(currentDate, i.getDueDate()));
        for (LoanRepaymentScheduleInstallment installment : installments) {
            LocalDate dueDate = installment.getDueDate();
            // check if there is any not-replayed-accrual-activity related to business date
            ArrayList<LoanTransaction> existingActivities = new ArrayList<>(
                    loan.getLoanTransactions(t -> t.isNotReversed() && t.isAccrualActivity() && t.getTransactionDate().isEqual(dueDate)));
            boolean hasExisting = !existingActivities.isEmpty();
            LoanTransaction existingActivity = hasExisting ? existingActivities.get(0) : null;
            makeOrReplayActivity(loan, installment, existingActivity);
            if (hasExisting) {
                existingActivities.remove(existingActivity);
                existingActivities.forEach(this::reverseAccrualActivityTransaction);
            }
        }
    }

    @Override
    @Transactional
    public void processAccrualActivityForLoanClosure(@NotNull Loan loan) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            return;
        }
        LocalDate date = loan.isOverPaid() ? loan.getOverpaidOnDate() : loan.getClosedOnDate();
        // reverse after closure activities
        loan.getLoanTransactions(t -> t.isAccrualActivity() && !t.isReversed() && t.getDateOf().isAfter(date))
                .forEach(this::reverseAccrualActivityTransaction);

        // calculate activity amounts
        BigDecimal feeChargesPortion = BigDecimal.ZERO;
        BigDecimal penaltyChargesPortion = BigDecimal.ZERO;
        BigDecimal interestPortion = BigDecimal.ZERO;
        // collect installment amounts
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            feeChargesPortion = MathUtil.add(feeChargesPortion, installment.getFeeChargesCharged());
            penaltyChargesPortion = MathUtil.add(penaltyChargesPortion, installment.getPenaltyCharges());
            interestPortion = MathUtil.add(interestPortion, installment.getInterestCharged());
        }
        List<LoanTransaction> accrualActivities = loan.getLoanTransactions().stream().filter(LoanTransaction::isAccrualActivity)
                .filter(LoanTransaction::isNotReversed).toList();
        // subtract already posted activities
        for (LoanTransaction accrualActivity : accrualActivities) {
            feeChargesPortion = MathUtil.subtract(feeChargesPortion, accrualActivity.getFeeChargesPortion());
            penaltyChargesPortion = MathUtil.subtract(penaltyChargesPortion, accrualActivity.getPenaltyChargesPortion());
            interestPortion = MathUtil.subtract(interestPortion, accrualActivity.getInterestPortion());
        }
        BigDecimal transactionAmount = MathUtil.add(feeChargesPortion, penaltyChargesPortion, interestPortion);
        if (!MathUtil.isGreaterThanZero(transactionAmount)) {
            return;
        }
        if (MathUtil.isLessThanZero(feeChargesPortion) || MathUtil.isLessThanZero(penaltyChargesPortion)
                || MathUtil.isLessThanZero(interestPortion)) {
            // TODO reverse latest accrual activity if any amount is negative
            return;
        }
        LoanTransaction newActivity = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.ACCRUAL_ACTIVITY.getValue(), date,
                transactionAmount, null, interestPortion, feeChargesPortion, penaltyChargesPortion, null, false, null,
                externalIdFactory.create());
        makeAccrualActivityTransaction(loan, newActivity);
    }

    @Override
    @Transactional
    public void processAccrualActivityForLoanReopen(@NotNull Loan loan) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            return;
        }
        // grab the latest AccrualActivityTransaction
        // it does not matter if it is on an installment due date or not because it was posted due to loan close
        LoanTransaction lastAccrualActivityMarkedToReverse = loan.getLoanTransactions().stream()
                .filter(loanTransaction -> loanTransaction.isNotReversed() && loanTransaction.isAccrualActivity())
                .sorted(Comparator.comparing(LoanTransaction::getDateOf)).reduce((first, second) -> second).orElse(null);
        final LocalDate lastAccrualActivityTransactionDate = lastAccrualActivityMarkedToReverse == null ? null
                : lastAccrualActivityMarkedToReverse.getDateOf();
        LocalDate today = DateUtils.getBusinessLocalDate();
        final List<LoanRepaymentScheduleInstallment> installmentsBetweenBusinessDateAndLastAccrualActivityTransactionDate = loan
                .getRepaymentScheduleInstallments().stream()
                .filter(installment -> installment.getDueDate().isBefore(today)
                        && (DateUtils.isAfter(installment.getDueDate(), lastAccrualActivityTransactionDate)
                                // if close event happened on installment due date
                                // we should reverse replay it to calculate installment related accrual parts only
                                || installment.getDueDate().isEqual(lastAccrualActivityTransactionDate)))
                .sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate)).toList();
        for (LoanRepaymentScheduleInstallment installment : installmentsBetweenBusinessDateAndLastAccrualActivityTransactionDate) {
            makeOrReplayActivity(loan, installment, lastAccrualActivityMarkedToReverse);
            lastAccrualActivityMarkedToReverse = null;
        }
        if (lastAccrualActivityMarkedToReverse != null) {
            reverseAccrualActivityTransaction(lastAccrualActivityMarkedToReverse);
        }
    }

    private void makeOrReplayActivity(@NotNull Loan loan, @NotNull LoanRepaymentScheduleInstallment installment,
            LoanTransaction existingActivity) {
        LocalDate dueDate = installment.getDueDate();
        if (existingActivity == null) {
            makeAccrualActivityTransaction(loan, installment, dueDate);
        } else {
            reverseReplayAccrualActivityTransaction(loan, existingActivity, installment, dueDate);
        }
    }

    private LoanTransaction reverseReplayAccrualActivityTransaction(@NotNull Loan loan, @NotNull LoanTransaction loanTransaction,
            @NotNull LoanRepaymentScheduleInstallment installment, @NotNull LocalDate transactionDate) {
        if (validateActivityTransaction(installment, loanTransaction)) {
            return loanTransaction;
        }

        LoanTransaction newLoanTransaction = loanTransactionAssembler.assembleAccrualActivityTransaction(loan, installment,
                transactionDate);
        if (newLoanTransaction != null) {
            newLoanTransaction.copyLoanTransactionRelations(loanTransaction.getLoanTransactionRelations());
            newLoanTransaction.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(newLoanTransaction,
                    loanTransaction, LoanTransactionRelationTypeEnum.REPLAYED));
            loanAccountDomainService.saveLoanTransactionWithDataIntegrityViolationChecks(newLoanTransaction);
            loan.addLoanTransaction(newLoanTransaction);

            LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
            data.setNewTransactionDetail(newLoanTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
        }
        reverseAccrualActivityTransaction(loanTransaction);
        return newLoanTransaction;
    }

    private boolean validateActivityTransaction(@NotNull LoanRepaymentScheduleInstallment installment,
            @NotNull LoanTransaction transaction) {
        return DateUtils.isEqual(installment.getDueDate(), transaction.getDateOf())
                && MathUtil.isEqualTo(transaction.getInterestPortion(), installment.getInterestCharged())
                && MathUtil.isEqualTo(transaction.getFeeChargesPortion(), installment.getFeeChargesCharged())
                && MathUtil.isEqualTo(transaction.getPenaltyChargesPortion(), installment.getPenaltyCharges());
    }

    private void reverseAccrualActivityTransaction(LoanTransaction loanTransaction) {
        loanTransaction.reverse();
        loanTransaction.updateExternalId(null);
        LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
    }

    private LoanTransaction makeAccrualActivityTransaction(@NotNull Loan loan, @NotNull LoanRepaymentScheduleInstallment installment,
            @NotNull LocalDate transactionDate) {
        LoanTransaction newAccrualActivityTransaction = loanTransactionAssembler.assembleAccrualActivityTransaction(loan, installment,
                transactionDate);
        return newAccrualActivityTransaction == null ? null : makeAccrualActivityTransaction(loan, newAccrualActivityTransaction);
    }

    private LoanTransaction makeAccrualActivityTransaction(@NotNull Loan loan, @NotNull LoanTransaction newAccrualActivityTransaction) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionAccrualActivityPreBusinessEvent(loan));
        newAccrualActivityTransaction = loanAccountDomainService
                .saveLoanTransactionWithDataIntegrityViolationChecks(newAccrualActivityTransaction);

        loan.addLoanTransaction(newAccrualActivityTransaction);
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanTransactionAccrualActivityPostBusinessEvent(newAccrualActivityTransaction));
        return newAccrualActivityTransaction;
    }
}
