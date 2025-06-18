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
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalance;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionRepository;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.apache.fineract.portfolio.loanproduct.calc.data.OutstandingDetails;
import org.apache.fineract.portfolio.loanproduct.calc.data.ProgressiveLoanInterestScheduleModel;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@AllArgsConstructor
@Slf4j
public class ProgressiveLoanSummaryDataProvider extends CommonLoanSummaryDataProvider {

    private final AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor;
    private final EMICalculator emiCalculator;
    private final LoanRepositoryWrapper loanRepository;
    private final LoanTransactionRepository loanTransactionRepository;
    private final InterestScheduleModelRepositoryWrapper modelRepository;

    @Override
    public boolean accept(String loanProcessingStrategyCode) {
        return loanProcessingStrategyCode.equalsIgnoreCase("advanced-payment-allocation-strategy");
    }

    @Override
    @Transactional(readOnly = true)
    public LoanSummaryData withTransactionAmountsSummary(Long loanId, LoanSummaryData defaultSummaryData,
            LoanScheduleData repaymentSchedule, Collection<? extends LoanTransactionBalance> loanTransactionBalances) {
        final Loan loan = loanRepository.findOneWithNotFoundDetection(loanId, true);
        return super.withTransactionAmountsSummary(loan, defaultSummaryData, repaymentSchedule, loanTransactionBalances);
    }

    @Override
    public LoanSummaryData withTransactionAmountsSummary(Loan loan, LoanSummaryData defaultSummaryData, LoanScheduleData repaymentSchedule,
            Collection<? extends LoanTransactionBalance> loanTransactionBalances) {
        return super.withTransactionAmountsSummary(loan, defaultSummaryData, repaymentSchedule, loanTransactionBalances);
    }

    private Optional<LoanRepaymentScheduleInstallment> getRelatedRepaymentScheduleInstallment(Loan loan, LocalDate businessDate) {
        return loan.getRepaymentScheduleInstallments().stream().filter(i -> !i.isDownPayment() && !i.isAdditional()
                && businessDate.isAfter(i.getFromDate()) && !businessDate.isAfter(i.getDueDate())).findFirst();
    }

    private ProgressiveLoanInterestScheduleModel calculateModel(Loan loan, LocalDate businessDate) {
        final List<LoanTransaction> transactionsToReprocess = loanTransactionRepository
                .findNonReversedTransactionsForReprocessingByLoan(loan).stream().filter(t -> !t.isAccrualActivity()).toList();
        Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> changedTransactionDetailProgressiveLoanInterestScheduleModelPair = advancedPaymentScheduleTransactionProcessor
                .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), businessDate, transactionsToReprocess, loan.getCurrency(),
                        loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
        ProgressiveLoanInterestScheduleModel model = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getRight();
        final List<Long> replayedTransactions = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getLeft()
                .getTransactionChanges().stream().filter(change -> change.getOldTransaction() != null && change.getNewTransaction() != null)
                .map(change -> change.getNewTransaction().getId()).filter(Objects::nonNull).toList();

        if (!replayedTransactions.isEmpty()) {
            log.warn("Reprocessed transactions show differences: There are unsaved changes of the following transactions: {}",
                    replayedTransactions);
        }
        return model;
    }

    @Override
    public BigDecimal computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(final Loan loan,
            final Collection<LoanSchedulePeriodData> periods, final LocalDate businessDate, final CurrencyData currency,
            BigDecimal totalUnpaidPayableDueInterest) {
        if (loan.isMatured(businessDate) || !loan.isInterestBearing()) {
            return BigDecimal.ZERO;
        }

        Optional<LoanRepaymentScheduleInstallment> currentRepaymentPeriod = getRelatedRepaymentScheduleInstallment(loan, businessDate);

        if (currentRepaymentPeriod.isPresent()) {
            if (loan.isChargedOff() || loan.hasContractTerminationTransaction()) {
                return MathUtil.subtractToZero(currentRepaymentPeriod.get().getInterestOutstanding(loan.getCurrency()).getAmount(),
                        totalUnpaidPayableDueInterest);
            } else {

                Optional<ProgressiveLoanInterestScheduleModel> savedModel = modelRepository.getSavedModel(loan, businessDate);

                ProgressiveLoanInterestScheduleModel model = savedModel.orElseGet(() -> calculateModel(loan, businessDate));
                if (model != null) {
                    OutstandingDetails outstandingDetails = emiCalculator.getOutstandingAmountsTillDate(model, businessDate);
                    if (!loan.isInterestRecalculationEnabled()) {
                        BigDecimal interestPaid = periods.stream().map(LoanSchedulePeriodData::getInterestPaid).reduce(BigDecimal.ZERO,
                                BigDecimal::add);
                        BigDecimal dueInterest = outstandingDetails.getOutstandingInterest().getAmount();
                        return MathUtil.subtractToZero(dueInterest, interestPaid, totalUnpaidPayableDueInterest);
                    } else {
                        return MathUtil.subtractToZero(outstandingDetails.getOutstandingInterest().getAmount(),
                                totalUnpaidPayableDueInterest);
                    }
                }
            }
        }

        return BigDecimal.ZERO;
    }
}
