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
import lombok.AllArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalance;
import org.apache.fineract.portfolio.loanaccount.domain.ChangedTransactionDetail;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransaction;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.PeriodDueDetails;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.ProgressiveLoanInterestScheduleModel;
import org.apache.fineract.portfolio.loanproduct.calc.EMICalculator;
import org.springframework.stereotype.Component;

@Component
@AllArgsConstructor
@Slf4j
public class ProgressiveLoanSummaryDataProvider extends CommonLoanSummaryDataProvider {

    private final AdvancedPaymentScheduleTransactionProcessor advancedPaymentScheduleTransactionProcessor;
    private final EMICalculator emiCalculator;
    private final LoanRepositoryWrapper loanRepository;

    @Override
    public boolean accept(String loanProcessingStrategyCode) {
        return loanProcessingStrategyCode.equalsIgnoreCase("advanced-payment-allocation-strategy");
    }

    @Override
    public LoanSummaryData withTransactionAmountsSummary(Long loanId, LoanSummaryData defaultSummaryData,
            LoanScheduleData repaymentSchedule, Collection<LoanTransactionBalance> loanTransactionBalances) {
        final Loan loan = loanRepository.findOneWithNotFoundDetection(loanId, true);
        return super.withTransactionAmountsSummary(loan, defaultSummaryData, repaymentSchedule, loanTransactionBalances);
    }

    @Override
    public LoanSummaryData withTransactionAmountsSummary(Loan loan, LoanSummaryData defaultSummaryData, LoanScheduleData repaymentSchedule,
            Collection<LoanTransactionBalance> loanTransactionBalances) {
        return super.withTransactionAmountsSummary(loan, defaultSummaryData, repaymentSchedule, loanTransactionBalances);
    }

    private LoanRepaymentScheduleInstallment getRelatedRepaymentScheduleInstallment(Loan loan, LocalDate businessDate) {
        return loan.getRepaymentScheduleInstallments().stream().filter(i -> !i.isDownPayment() && !i.isAdditional()
                && !businessDate.isBefore(i.getFromDate()) && businessDate.isBefore(i.getDueDate())).findFirst().orElseGet(() -> {
                    List<LoanRepaymentScheduleInstallment> list = loan.getRepaymentScheduleInstallments().stream()
                            .filter(i -> !i.isDownPayment() && !i.isAdditional()).toList();
                    return !list.isEmpty() ? list.get(list.size() - 1) : null;
                });
    }

    @Override
    public BigDecimal computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(final Loan loan,
            final Collection<LoanSchedulePeriodData> periods, final LocalDate businessDate, final CurrencyData currency) {

        LoanRepaymentScheduleInstallment loanRepaymentScheduleInstallment = getRelatedRepaymentScheduleInstallment(loan, businessDate);
        if (loan.isInterestBearing() && loanRepaymentScheduleInstallment != null) {
            List<LoanTransaction> transactionsToReprocess = loan.retrieveListOfTransactionsForReprocessing().stream()
                    .filter(t -> !t.isAccrualActivity()).toList();
            Pair<ChangedTransactionDetail, ProgressiveLoanInterestScheduleModel> changedTransactionDetailProgressiveLoanInterestScheduleModelPair = advancedPaymentScheduleTransactionProcessor
                    .reprocessProgressiveLoanTransactions(loan.getDisbursementDate(), businessDate, transactionsToReprocess,
                            loan.getCurrency(), loan.getRepaymentScheduleInstallments(), loan.getActiveCharges());
            ProgressiveLoanInterestScheduleModel model = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getRight();
            if (!changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getLeft().getCurrentTransactionToOldId().isEmpty()
                    || !changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getLeft().getNewTransactionMappings().isEmpty()) {
                List<Long> replayedTransactions = changedTransactionDetailProgressiveLoanInterestScheduleModelPair.getLeft()
                        .getNewTransactionMappings().keySet().stream().toList();
                log.warn("Reprocessed transactions show differences: There are unsaved changes of the following transactions: {}",
                        replayedTransactions);
            }
            if (model != null) {
                PeriodDueDetails dueAmounts = emiCalculator.getDueAmounts(model, loanRepaymentScheduleInstallment.getDueDate(),
                        businessDate);
                if (dueAmounts != null) {
                    BigDecimal interestPaid = loanRepaymentScheduleInstallment.getInterestPaid();
                    BigDecimal dueInterest = dueAmounts.getDueInterest().getAmount();
                    if (interestPaid == null) {
                        return dueInterest;
                    }
                    return dueInterest.subtract(interestPaid);
                }
            }
        }

        return BigDecimal.ZERO;
    }
}
