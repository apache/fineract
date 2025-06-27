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
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import java.util.stream.Collectors;
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
import org.apache.fineract.portfolio.loanaccount.domain.LoanAccountService;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelation;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRelationTypeEnum;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.data.domain.PageRequest;
import org.springframework.lang.NonNull;
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
    private final LoanAccountService loanAccountService;
    private final LoanBalanceService loanBalanceService;
    private final LoanTransactionRepository loanTransactionRepository;

    @Override
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void makeAccrualActivityTransaction(final @NonNull Long loanId, final @NonNull LocalDate currentDate) {
        Loan loan = loanRepositoryWrapper.findOneWithNotFoundDetection(loanId, true);
        makeAccrualActivityTransaction(loan, currentDate);
    }

    @Override
    public void makeAccrualActivityTransaction(final @NonNull Loan loan, final @NonNull LocalDate currentDate) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting() || !loan.isOpen()) {
            return;
        }
        // check if loan has installment in the past or due on current date
        final List<LoanRepaymentScheduleInstallment> installments = loan
                .getRepaymentScheduleInstallments(i -> !i.isDownPayment() && !DateUtils.isBefore(currentDate, i.getDueDate()));

        if (installments.isEmpty()) {
            return;
        }

        final Map<LocalDate, List<LoanTransaction>> existingActivitiesByDate = loadExistingAccrualActivitiesByDate(loan, installments);

        installments.forEach(installment -> {
            final LocalDate dueDate = installment.getDueDate();
            final List<LoanTransaction> existingActivities = existingActivitiesByDate.getOrDefault(dueDate, Collections.emptyList());

            final boolean hasExisting = !existingActivities.isEmpty();
            final LoanTransaction existingActivity = hasExisting ? existingActivities.getFirst() : null;
            makeOrReplayActivity(loan, installment, existingActivity);
            if (hasExisting) {
                existingActivities.remove(existingActivity);
                existingActivities.forEach(this::reverseAccrualActivityTransaction);
            }
        });
    }

    @Override
    @Transactional
    public void processAccrualActivityForLoanClosure(final @NonNull Loan loan) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            return;
        }

        LocalDate closureDate = loanBalanceService.isOverPaid(loan) ? loan.getOverpaidOnDate() : loan.getClosedOnDate();

        // Reverse accrual activities posted after the closure date
        loanTransactionRepository.findNonReversedByLoanAndTypeAndAfterDate(loan, LoanTransactionType.ACCRUAL_ACTIVITY, closureDate)
                .forEach(this::reverseAccrualActivityTransaction);

        BigDecimal feeChargesPortion = BigDecimal.ZERO;
        BigDecimal penaltyChargesPortion = BigDecimal.ZERO;
        BigDecimal interestPortion = BigDecimal.ZERO;

        // Calculate total portions from all installments
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (!installment.isDownPayment()) { // Exclude downpayment installments
                feeChargesPortion = MathUtil.add(feeChargesPortion, installment.getFeeChargesCharged());
                penaltyChargesPortion = MathUtil.add(penaltyChargesPortion, installment.getPenaltyCharges());
                interestPortion = MathUtil.add(interestPortion, installment.getInterestCharged());
            }
        }

        List<LoanTransaction> accrualActivities = loanTransactionRepository.findNonReversedByLoanAndType(loan,
                LoanTransactionType.ACCRUAL_ACTIVITY);

        // Check each past installment for accrual activity
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (!installment.isDownPayment() && !installment.isAdditional() && installment.getDueDate().isBefore(closureDate)) {
                List<LoanTransaction> installmentAccruals = accrualActivities.stream()
                        .filter(t -> t.getDateOf().isEqual(installment.getDueDate())).toList();

                if (installmentAccruals.isEmpty()) {
                    // No AAT for this installment; create one
                    makeAccrualActivityTransaction(loan, installment, installment.getDueDate());

                    // Subtract processed portions
                } else if (installmentAccruals.size() > 1) {
                    // Reverse and recreate if inconsistent or duplicate
                    installmentAccruals.forEach(this::reverseAccrualActivityTransaction);
                    makeAccrualActivityTransaction(loan, installment, installment.getDueDate());
                } else if (!validateActivityTransaction(installment, installmentAccruals.getFirst())) {
                    reverseReplayAccrualActivityTransaction(loan, installmentAccruals.getFirst(), installment, installment.getDueDate());
                }
            }
        }

        // Subtract already posted accrual activities
        accrualActivities = loanTransactionRepository.findNonReversedByLoanAndType(loan, LoanTransactionType.ACCRUAL_ACTIVITY);
        for (LoanTransaction accrualActivity : accrualActivities) {
            feeChargesPortion = MathUtil.subtract(feeChargesPortion, accrualActivity.getFeeChargesPortion());
            penaltyChargesPortion = MathUtil.subtract(penaltyChargesPortion, accrualActivity.getPenaltyChargesPortion());
            interestPortion = MathUtil.subtract(interestPortion, accrualActivity.getInterestPortion());
        }

        // Skip final accrual activity creation if no portions remain
        if (MathUtil.isGreaterThanZero(feeChargesPortion) || MathUtil.isGreaterThanZero(penaltyChargesPortion)
                || MathUtil.isGreaterThanZero(interestPortion)) {
            BigDecimal transactionAmount = MathUtil.add(feeChargesPortion, penaltyChargesPortion, interestPortion);
            LoanTransaction newActivity = new LoanTransaction(loan, loan.getOffice(), LoanTransactionType.ACCRUAL_ACTIVITY, closureDate,
                    transactionAmount, null, interestPortion, feeChargesPortion, penaltyChargesPortion, null, false, null,
                    externalIdFactory.create());
            makeAccrualActivityTransaction(loan, newActivity);
        }
    }

    @Override
    @Transactional
    public void processAccrualActivityForLoanReopen(final @NonNull Loan loan) {
        if (!loan.getLoanProductRelatedDetail().isEnableAccrualActivityPosting()) {
            return;
        }
        // grab the latest AccrualActivityTransaction
        // it does not matter if it is on an installment due date or not because it was posted due to loan close
        Optional<LoanTransaction> lastAccrualActivityMarkedToReverse = loanTransactionRepository
                .findNonReversedByLoanAndType(loan, LoanTransactionType.ACCRUAL_ACTIVITY, PageRequest.of(0, 1)) //
                .stream().findFirst();

        final Optional<LocalDate> lastAccrualActivityTransactionDate = lastAccrualActivityMarkedToReverse.map(LoanTransaction::getDateOf);
        final LocalDate today = DateUtils.getBusinessLocalDate();

        final List<LoanRepaymentScheduleInstallment> installments = loan.getRepaymentScheduleInstallments().stream().filter(installment -> {
            boolean isDueBefore = installment.getDueDate().isBefore(today);
            boolean isAfterOrEqualToLastAccrualDate = lastAccrualActivityTransactionDate
                    .map(date -> DateUtils.isAfter(installment.getDueDate(), date)
                            // if close event happened on installment due date
                            // we should reverse replay it to calculate installment related accrual parts only
                            || installment.getDueDate().isEqual(date))
                    .orElse(true);
            return isDueBefore && isAfterOrEqualToLastAccrualDate;
        }).sorted(Comparator.comparing(LoanRepaymentScheduleInstallment::getDueDate)).toList();

        for (LoanRepaymentScheduleInstallment installment : installments) {
            makeOrReplayActivity(loan, installment, lastAccrualActivityMarkedToReverse.orElse(null));
            lastAccrualActivityMarkedToReverse = Optional.empty();
        }

        if (installments.isEmpty()) {
            lastAccrualActivityMarkedToReverse.ifPresent(this::reverseAccrualActivityTransaction);
        }
    }

    private Map<LocalDate, List<LoanTransaction>> loadExistingAccrualActivitiesByDate(final @NonNull Loan loan,
            final List<LoanRepaymentScheduleInstallment> installments) {
        final Set<LocalDate> dueDates = installments.stream().map(LoanRepaymentScheduleInstallment::getDueDate).collect(Collectors.toSet());

        final List<LoanTransaction> allActivities = loanTransactionRepository.findNonReversedLoanAndTypeAndDates(loan,
                LoanTransactionType.ACCRUAL_ACTIVITY, dueDates);

        return allActivities.stream().collect(Collectors.groupingBy(LoanTransaction::getDateOf));
    }

    private void makeOrReplayActivity(final @NonNull Loan loan, final @NonNull LoanRepaymentScheduleInstallment installment,
            LoanTransaction existingActivity) {
        LocalDate dueDate = installment.getDueDate();
        if (existingActivity == null) {
            makeAccrualActivityTransaction(loan, installment, dueDate);
        } else {
            reverseReplayAccrualActivityTransaction(loan, existingActivity, installment, dueDate);
        }
    }

    private void reverseReplayAccrualActivityTransaction(final @NonNull Loan loan, final @NonNull LoanTransaction loanTransaction,
            final @NonNull LoanRepaymentScheduleInstallment installment, final @NonNull LocalDate transactionDate) {
        if (validateActivityTransaction(installment, loanTransaction)) {
            return;
        }

        LoanTransaction newLoanTransaction = loanTransactionAssembler.assembleAccrualActivityTransaction(loan, installment,
                transactionDate);
        if (newLoanTransaction != null) {
            newLoanTransaction.copyLoanTransactionRelations(loanTransaction.getLoanTransactionRelations());
            newLoanTransaction.getLoanTransactionRelations().add(LoanTransactionRelation.linkToTransaction(newLoanTransaction,
                    loanTransaction, LoanTransactionRelationTypeEnum.REPLAYED));

            newLoanTransaction.updateExternalId(loanTransaction.getExternalId());
            loanTransaction.reverse();
            loanTransaction.updateExternalId(null);
            loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(loanTransaction);

            loanAccountService.saveLoanTransactionWithDataIntegrityViolationChecks(newLoanTransaction);
            loan.addLoanTransaction(newLoanTransaction);

            LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
            data.setNewTransactionDetail(newLoanTransaction);
            businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
        } else {
            reverseAccrualActivityTransaction(loanTransaction);
        }
    }

    private boolean validateActivityTransaction(final @NonNull LoanRepaymentScheduleInstallment installment,
            final @NonNull LoanTransaction transaction) {
        return DateUtils.isEqual(installment.getDueDate(), transaction.getDateOf())
                && MathUtil.isEqualTo(transaction.getInterestPortion(), installment.getInterestCharged())
                && MathUtil.isEqualTo(transaction.getFeeChargesPortion(), installment.getFeeChargesCharged())
                && MathUtil.isEqualTo(transaction.getPenaltyChargesPortion(), installment.getPenaltyCharges());
    }

    private void reverseAccrualActivityTransaction(final @NonNull LoanTransaction loanTransaction) {
        loanTransaction.reverse();

        LoanAdjustTransactionBusinessEvent.Data data = new LoanAdjustTransactionBusinessEvent.Data(loanTransaction);
        businessEventNotifierService.notifyPostBusinessEvent(new LoanAdjustTransactionBusinessEvent(data));
    }

    private void makeAccrualActivityTransaction(final @NonNull Loan loan, final @NonNull LoanRepaymentScheduleInstallment installment,
            final @NonNull LocalDate transactionDate) {
        LoanTransaction newAccrualActivityTransaction = loanTransactionAssembler.assembleAccrualActivityTransaction(loan, installment,
                transactionDate);

        if (newAccrualActivityTransaction != null) {
            LoanTransaction savedNewTransaction = makeAccrualActivityTransaction(loan, newAccrualActivityTransaction);
            loan.addLoanTransaction(savedNewTransaction);
        }
    }

    private LoanTransaction makeAccrualActivityTransaction(final @NonNull Loan loan,
            @NonNull LoanTransaction newAccrualActivityTransaction) {
        businessEventNotifierService.notifyPreBusinessEvent(new LoanTransactionAccrualActivityPreBusinessEvent(loan));
        LoanTransaction savedNewAccrualActivityTransaction = loanAccountService
                .saveLoanTransactionWithDataIntegrityViolationChecks(newAccrualActivityTransaction);
        businessEventNotifierService
                .notifyPostBusinessEvent(new LoanTransactionAccrualActivityPostBusinessEvent(savedNewAccrualActivityTransaction));
        return savedNewAccrualActivityTransaction;
    }

}
