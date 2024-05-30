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
package org.apache.fineract.portfolio.delinquency.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.delinquency.helper.DelinquencyEffectivePauseHelper;
import org.apache.fineract.portfolio.delinquency.validator.LoanDelinquencyActionData;
import org.apache.fineract.portfolio.loanaccount.data.CollectionData;
import org.apache.fineract.portfolio.loanaccount.data.LoanDelinquencyData;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@RequiredArgsConstructor
public class LoanDelinquencyDomainServiceImpl implements LoanDelinquencyDomainService {

    private final DelinquencyEffectivePauseHelper delinquencyEffectivePauseHelper;

    @Override
    @Transactional(readOnly = true)
    public CollectionData getOverdueCollectionData(final Loan loan, List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();

        final MonetaryCurrency loanCurrency = loan.getCurrency();
        LocalDate overdueSinceDate = null;
        CollectionData collectionData = CollectionData.template();
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        boolean oldestOverdueInstallment = false;
        boolean overdueSinceDateWasSet = false;
        boolean firstNotYetDueInstallment = false;
        log.debug("Loan id {} with {} installments", loan.getId(), loan.getRepaymentScheduleInstallments().size());

        // If the Loan is not Active yet, return template data
        // If the Loan is Rejected, Closed written-off, Withdrawn by Client, Closed with outstanding marked for
        // reschedule, Closed obligation met, Overpaid return template data
        if (loan.isSubmittedAndPendingApproval() || loan.isApproved() || loan.isClosed() || loan.getStatus().isOverpaid()) {
            return CollectionData.template();
        }

        // Get the oldest overdue installment if exists one
        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            if (!installment.isObligationsMet()) {
                if (DateUtils.isBefore(installment.getDueDate(), businessDate)) {
                    log.debug("Loan Id: {} with installment {} due date {}", loan.getId(), installment.getInstallmentNumber(),
                            installment.getDueDate());
                    outstandingAmount = outstandingAmount.add(installment.getTotalOutstanding(loanCurrency).getAmount());
                    if (!oldestOverdueInstallment) {
                        log.debug("Oldest installment {} {}", installment.getInstallmentNumber(), installment.getDueDate());
                        CollectionData overDueInstallmentDelinquentData = calculateDelinquencyDataForOverdueInstallment(loan, installment);
                        overdueSinceDate = overDueInstallmentDelinquentData.getDelinquentDate();
                        oldestOverdueInstallment = true;
                        overdueSinceDateWasSet = true;
                    }
                } else if (!firstNotYetDueInstallment) {
                    log.debug("Loan Id: {} with installment {} due date {}", loan.getId(), installment.getInstallmentNumber(),
                            installment.getDueDate());
                    firstNotYetDueInstallment = true;
                    CollectionData nonOverDueInstallmentDelinquentData = calculateDelinquencyDataForNonOverdueInstallment(loan,
                            installment);
                    outstandingAmount = outstandingAmount.add(nonOverDueInstallmentDelinquentData.getDelinquentAmount());
                    if (!overdueSinceDateWasSet) {
                        overdueSinceDate = nonOverDueInstallmentDelinquentData.getDelinquentDate();
                        overdueSinceDateWasSet = true;
                    }
                }
            }
        }

        Integer graceDays = 0;
        if (loan.getLoanProductRelatedDetail().getGraceOnArrearsAgeing() != null) {
            graceDays = loan.getLoanProductRelatedDetail().getGraceOnArrearsAgeing();
        }
        log.debug("Loan id {} with overdue since date {} and outstanding amount {}", loan.getId(), overdueSinceDate, outstandingAmount);

        Long overdueDays = 0L;
        if (overdueSinceDate != null) {
            overdueDays = DateUtils.getDifferenceInDays(overdueSinceDate, businessDate);
            if (overdueDays < 0) {
                overdueDays = 0L;
            }
            collectionData.setPastDueDays(overdueDays);
            overdueSinceDate = overdueSinceDate.plusDays(graceDays.longValue());
            collectionData.setDelinquentDate(overdueSinceDate);
        }
        collectionData.setDelinquentAmount(outstandingAmount);
        collectionData.setDelinquentDays(0L);
        Long delinquentDays = overdueDays - graceDays;
        if (delinquentDays > 0) {
            calculateDelinquentDays(effectiveDelinquencyList, businessDate, collectionData, delinquentDays);
        }

        log.debug("Result: {}", collectionData.toString());
        return collectionData;
    }

    @Override
    public LoanDelinquencyData getLoanDelinquencyData(final Loan loan, List<LoanDelinquencyActionData> effectiveDelinquencyList) {

        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate overdueSinceDate = null;
        CollectionData collectionData = CollectionData.template();
        Map<Long, CollectionData> loanInstallmentsCollectionData = new HashMap<>();
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        boolean oldestOverdueInstallment = false;
        boolean overdueSinceDateWasSet = false;
        boolean firstNotYetDueInstallment = false;
        log.debug("Loan id {} with {} installments", loan.getId(), loan.getRepaymentScheduleInstallments().size());

        // If the Loan is not Active yet, return template data
        // If the Loan is Rejected, Closed written-off, Withdrawn by Client, Closed with outstanding marked for
        // reschedule, Closed obligation met, Overpaid, return template data
        if (loan.isSubmittedAndPendingApproval() || loan.isApproved() || loan.isClosed() || loan.getStatus().isOverpaid()) {
            return new LoanDelinquencyData(collectionData, loanInstallmentsCollectionData);
        }

        for (LoanRepaymentScheduleInstallment installment : loan.getRepaymentScheduleInstallments()) {
            CollectionData installmentCollectionData = CollectionData.template();
            if (!installment.isObligationsMet()) {
                installmentCollectionData = getInstallmentOverdueCollectionData(loan, installment, effectiveDelinquencyList);
                outstandingAmount = outstandingAmount.add(installmentCollectionData.getDelinquentAmount());
                // Get the oldest overdue installment if exists
                if (DateUtils.isBefore(installment.getDueDate(), businessDate)) {
                    if (!oldestOverdueInstallment) {
                        overdueSinceDate = installmentCollectionData.getDelinquentDate();
                        oldestOverdueInstallment = true;
                        overdueSinceDateWasSet = true;
                    }
                } else if (!firstNotYetDueInstallment) {
                    firstNotYetDueInstallment = true;
                    if (!overdueSinceDateWasSet) {
                        overdueSinceDate = installmentCollectionData.getDelinquentDate();
                        overdueSinceDateWasSet = true;
                    }
                }
            }
            // if installment level delinquency enabled add delinquency data for installment
            if (loan.isEnableInstallmentLevelDelinquency()) {
                loanInstallmentsCollectionData.put(installment.getId(), installmentCollectionData);
            }

        }

        Integer graceDays = 0;
        if (loan.getLoanProductRelatedDetail().getGraceOnArrearsAgeing() != null) {
            graceDays = loan.getLoanProductRelatedDetail().getGraceOnArrearsAgeing();
        }
        log.debug("Loan id {} with overdue since date {} and outstanding amount {}", loan.getId(), overdueSinceDate, outstandingAmount);

        Long overdueDays = 0L;
        if (overdueSinceDate != null) {
            overdueDays = DateUtils.getDifferenceInDays(overdueSinceDate, businessDate);
            if (overdueDays < 0) {
                overdueDays = 0L;
            }
            collectionData.setPastDueDays(overdueDays);
            overdueSinceDate = overdueSinceDate.plusDays(graceDays.longValue());
            collectionData.setDelinquentDate(overdueSinceDate);
        }
        collectionData.setDelinquentAmount(outstandingAmount);
        collectionData.setDelinquentDays(0L);
        Long delinquentDays = overdueDays - graceDays;
        if (delinquentDays > 0) {
            calculateDelinquentDays(effectiveDelinquencyList, businessDate, collectionData, delinquentDays);
        }
        return new LoanDelinquencyData(collectionData, loanInstallmentsCollectionData);
    }

    private void calculateDelinquentDays(List<LoanDelinquencyActionData> effectiveDelinquencyList, LocalDate businessDate,
            CollectionData collectionData, Long delinquentDays) {
        Long pausedDays = delinquencyEffectivePauseHelper.getPausedDaysBeforeDate(effectiveDelinquencyList, businessDate);
        Long calculatedDelinquentDays = delinquentDays - pausedDays;
        collectionData.setDelinquentDays(calculatedDelinquentDays > 0 ? calculatedDelinquentDays : 0L);
    }

    private CollectionData getInstallmentOverdueCollectionData(final Loan loan, final LoanRepaymentScheduleInstallment installment,
            List<LoanDelinquencyActionData> effectiveDelinquencyList) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        LocalDate overdueSinceDate = null;
        CollectionData collectionData = CollectionData.template();
        BigDecimal outstandingAmount = BigDecimal.ZERO;
        if (DateUtils.isBefore(installment.getDueDate(), businessDate)) {
            // checking overdue installment delinquency data
            CollectionData overDueInstallmentDelinquentData = calculateDelinquencyDataForOverdueInstallment(loan, installment);
            outstandingAmount = outstandingAmount.add(overDueInstallmentDelinquentData.getDelinquentAmount());
            overdueSinceDate = overDueInstallmentDelinquentData.getDelinquentDate();

        } else {
            // checking non overdue installment for chargeback transactions before installment due date and before
            // business date
            CollectionData nonOverDueInstallmentDelinquentData = calculateDelinquencyDataForNonOverdueInstallment(loan, installment);
            outstandingAmount = outstandingAmount.add(nonOverDueInstallmentDelinquentData.getDelinquentAmount());
            overdueSinceDate = nonOverDueInstallmentDelinquentData.getDelinquentDate();
        }

        // Grace days are not considered for installment level delinquency calculation currently.

        Long overdueDays = 0L;
        if (overdueSinceDate != null) {
            overdueDays = DateUtils.getDifferenceInDays(overdueSinceDate, businessDate);
            if (overdueDays < 0) {
                overdueDays = 0L;
            }
            collectionData.setPastDueDays(overdueDays);
            collectionData.setDelinquentDate(overdueSinceDate);
        }
        collectionData.setDelinquentAmount(outstandingAmount);
        collectionData.setDelinquentDays(0L);
        Long delinquentDays = overdueDays;
        if (delinquentDays > 0) {
            calculateDelinquentDays(effectiveDelinquencyList, businessDate, collectionData, delinquentDays);
        }
        return collectionData;

    }

    private CollectionData calculateDelinquencyDataForOverdueInstallment(final Loan loan,
            final LoanRepaymentScheduleInstallment installment) {
        final MonetaryCurrency loanCurrency = loan.getCurrency();
        LoanRepaymentScheduleInstallment latestInstallment = loan.getLastLoanRepaymentScheduleInstallment();
        List<LoanTransaction> chargebackTransactions = loan.getLoanTransactions(LoanTransaction::isChargeback);
        LocalDate overdueSinceDate = null;
        CollectionData collectionData = CollectionData.template();
        BigDecimal outstandingAmount = BigDecimal.ZERO;

        outstandingAmount = outstandingAmount.add(installment.getTotalOutstanding(loanCurrency).getAmount());
        overdueSinceDate = installment.getDueDate();
        BigDecimal amountAvailable = installment.getTotalPaid(loanCurrency).getAmount();
        boolean isLatestInstallment = Objects.equals(installment.getId(), latestInstallment.getId());
        for (LoanTransaction loanTransaction : chargebackTransactions) {
            boolean isLoanTransactionIsOnOrAfterInstallmentFromDate = DateUtils.isEqual(loanTransaction.getTransactionDate(),
                    installment.getFromDate()) || DateUtils.isAfter(loanTransaction.getTransactionDate(), installment.getFromDate());
            boolean isLoanTransactionIsBeforeNotLastInstallmentDueDate = !isLatestInstallment
                    && DateUtils.isBefore(loanTransaction.getTransactionDate(), installment.getDueDate());
            boolean isLoanTransactionIsOnOrBeforeLastInstallmentDueDate = isLatestInstallment
                    && (DateUtils.isEqual(loanTransaction.getTransactionDate(), installment.getDueDate())
                            || DateUtils.isBefore(loanTransaction.getTransactionDate(), installment.getDueDate()));
            if (isLoanTransactionIsOnOrAfterInstallmentFromDate
                    && (isLoanTransactionIsBeforeNotLastInstallmentDueDate || isLoanTransactionIsOnOrBeforeLastInstallmentDueDate)) {
                amountAvailable = amountAvailable.subtract(loanTransaction.getAmount());
                if (amountAvailable.compareTo(BigDecimal.ZERO) < 0) {
                    overdueSinceDate = loanTransaction.getTransactionDate();
                    break;
                }
            }
        }
        collectionData.setDelinquentDate(overdueSinceDate);
        collectionData.setDelinquentAmount(outstandingAmount);
        return collectionData;
    }

    private CollectionData calculateDelinquencyDataForNonOverdueInstallment(final Loan loan,
            final LoanRepaymentScheduleInstallment installment) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();
        final MonetaryCurrency loanCurrency = loan.getCurrency();

        LocalDate overdueSinceDate = null;
        CollectionData collectionData = CollectionData.template();
        BigDecimal outstandingAmount = BigDecimal.ZERO;

        List<LoanTransaction> chargebackTransactions = loan.getLoanTransactions(LoanTransaction::isChargeback);
        BigDecimal amountAvailable = installment.getTotalPaid(loanCurrency).getAmount();
        for (LoanTransaction loanTransaction : chargebackTransactions) {

            boolean isLoanTransactionIsOnOrAfterInstallmentFromDate = DateUtils.isEqual(loanTransaction.getTransactionDate(),
                    installment.getFromDate()) || DateUtils.isAfter(loanTransaction.getTransactionDate(), installment.getFromDate());
            boolean isLoanTransactionIsBeforeInstallmentDueDate = DateUtils.isBefore(loanTransaction.getTransactionDate(),
                    installment.getDueDate());
            boolean isLoanTransactionIsBeforeBusinessDate = DateUtils.isBefore(loanTransaction.getTransactionDate(), businessDate);
            if (isLoanTransactionIsOnOrAfterInstallmentFromDate && isLoanTransactionIsBeforeInstallmentDueDate
                    && isLoanTransactionIsBeforeBusinessDate) {
                amountAvailable = amountAvailable.subtract(loanTransaction.getAmount());
                if (amountAvailable.compareTo(BigDecimal.ZERO) < 0) {
                    overdueSinceDate = loanTransaction.getTransactionDate();
                }
            }
        }
        if (amountAvailable.compareTo(BigDecimal.ZERO) < 0) {
            outstandingAmount = outstandingAmount.add(amountAvailable.abs());
        }
        collectionData.setDelinquentDate(overdueSinceDate);
        collectionData.setDelinquentAmount(outstandingAmount);
        return collectionData;
    }

}
