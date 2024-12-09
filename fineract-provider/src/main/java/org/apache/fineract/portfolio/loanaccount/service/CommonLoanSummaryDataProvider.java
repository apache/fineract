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
import java.util.Optional;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.data.LoanSummaryData;
import org.apache.fineract.portfolio.loanaccount.data.LoanTransactionBalance;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

public abstract class CommonLoanSummaryDataProvider implements LoanSummaryDataProvider {

    @Override
    public LoanSummaryData withTransactionAmountsSummary(Loan loan, LoanSummaryData defaultSummaryData, LoanScheduleData repaymentSchedule,
            Collection<LoanTransactionBalance> loanTransactionBalances) {
        final LocalDate businessDate = DateUtils.getBusinessLocalDate();

        BigDecimal totalMerchantRefund = BigDecimal.ZERO;
        BigDecimal totalMerchantRefundReversed = BigDecimal.ZERO;
        BigDecimal totalPayoutRefund = BigDecimal.ZERO;
        BigDecimal totalPayoutRefundReversed = BigDecimal.ZERO;
        BigDecimal totalGoodwillCredit = BigDecimal.ZERO;
        BigDecimal totalGoodwillCreditReversed = BigDecimal.ZERO;
        BigDecimal totalChargeAdjustment = BigDecimal.ZERO;
        BigDecimal totalChargeAdjustmentReversed = BigDecimal.ZERO;
        BigDecimal totalChargeback = BigDecimal.ZERO;
        BigDecimal totalCreditBalanceRefund = BigDecimal.ZERO;
        BigDecimal totalCreditBalanceRefundReversed = BigDecimal.ZERO;
        BigDecimal totalRepaymentTransaction = BigDecimal.ZERO;
        BigDecimal totalRepaymentTransactionReversed = BigDecimal.ZERO;
        BigDecimal totalInterestPaymentWaiver = BigDecimal.ZERO;
        BigDecimal totalInterestRefund = BigDecimal.ZERO;
        BigDecimal totalUnpaidPayableDueInterest = BigDecimal.ZERO;
        BigDecimal totalUnpaidPayableNotDueInterest = BigDecimal.ZERO;

        totalChargeAdjustment = fetchLoanTransactionBalanceByType(loanTransactionBalances,
                LoanTransactionType.CHARGE_ADJUSTMENT.getValue());
        totalChargeAdjustmentReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.CHARGE_ADJUSTMENT.getValue());

        totalChargeback = fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.CHARGEBACK.getValue());

        totalCreditBalanceRefund = fetchLoanTransactionBalanceByType(loanTransactionBalances,
                LoanTransactionType.CREDIT_BALANCE_REFUND.getValue());
        totalCreditBalanceRefundReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.CREDIT_BALANCE_REFUND.getValue());

        totalGoodwillCredit = fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.GOODWILL_CREDIT.getValue());
        totalGoodwillCreditReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.GOODWILL_CREDIT.getValue());

        totalInterestRefund = fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.INTEREST_REFUND.getValue());

        totalInterestPaymentWaiver = fetchLoanTransactionBalanceByType(loanTransactionBalances,
                LoanTransactionType.INTEREST_PAYMENT_WAIVER.getValue());

        totalMerchantRefund = fetchLoanTransactionBalanceByType(loanTransactionBalances,
                LoanTransactionType.MERCHANT_ISSUED_REFUND.getValue());
        totalMerchantRefundReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.MERCHANT_ISSUED_REFUND.getValue());

        totalPayoutRefund = fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.PAYOUT_REFUND.getValue());
        totalPayoutRefundReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.PAYOUT_REFUND.getValue());

        totalRepaymentTransaction = fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.REPAYMENT.getValue())
                .add(fetchLoanTransactionBalanceByType(loanTransactionBalances, LoanTransactionType.DOWN_PAYMENT.getValue()));
        totalRepaymentTransactionReversed = fetchLoanTransactionBalanceReversedByType(loanTransactionBalances,
                LoanTransactionType.REPAYMENT.getValue());

        if (repaymentSchedule != null) {
            // Outstanding Interest on Past due installments
            totalUnpaidPayableDueInterest = computeTotalUnpaidPayableDueInterestAmount(repaymentSchedule.getPeriods(), businessDate);

            // Accumulated daily interest of the current Installment period
            totalUnpaidPayableNotDueInterest = computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(loan,
                    repaymentSchedule.getPeriods(), businessDate, defaultSummaryData.getCurrency());
        }

        return LoanSummaryData.builder().currency(defaultSummaryData.getCurrency())
                .principalDisbursed(defaultSummaryData.getPrincipalDisbursed())
                .principalAdjustments(defaultSummaryData.getPrincipalAdjustments()).principalPaid(defaultSummaryData.getPrincipalPaid())
                .principalWrittenOff(defaultSummaryData.getPrincipalWrittenOff())
                .principalOutstanding(defaultSummaryData.getPrincipalOutstanding())
                .principalOverdue(defaultSummaryData.getPrincipalOverdue()).interestCharged(defaultSummaryData.getInterestCharged())
                .interestPaid(defaultSummaryData.getInterestPaid()).interestWaived(defaultSummaryData.getInterestWaived())
                .interestWrittenOff(defaultSummaryData.getInterestWrittenOff())
                .interestOutstanding(defaultSummaryData.getInterestOutstanding()).interestOverdue(defaultSummaryData.getInterestOverdue())
                .feeChargesCharged(defaultSummaryData.getFeeChargesCharged()).feeAdjustments(defaultSummaryData.getFeeAdjustments())
                .feeChargesDueAtDisbursementCharged(defaultSummaryData.getFeeChargesDueAtDisbursementCharged())
                .feeChargesPaid(defaultSummaryData.getFeeChargesPaid()).feeChargesWaived(defaultSummaryData.getFeeChargesWaived())
                .feeChargesWrittenOff(defaultSummaryData.getFeeChargesWrittenOff())
                .feeChargesOutstanding(defaultSummaryData.getFeeChargesOutstanding())
                .feeChargesOverdue(defaultSummaryData.getFeeChargesOverdue())
                .penaltyChargesCharged(defaultSummaryData.getPenaltyChargesCharged())
                .penaltyAdjustments(defaultSummaryData.getPenaltyAdjustments())
                .penaltyChargesPaid(defaultSummaryData.getPenaltyChargesPaid())
                .penaltyChargesWaived(defaultSummaryData.getPenaltyChargesWaived())
                .penaltyChargesWrittenOff(defaultSummaryData.getPenaltyChargesWrittenOff())
                .penaltyChargesOutstanding(defaultSummaryData.getPenaltyChargesOutstanding())
                .penaltyChargesOverdue(defaultSummaryData.getPenaltyChargesOverdue())
                .totalExpectedRepayment(defaultSummaryData.getTotalExpectedRepayment())
                .totalRepayment(defaultSummaryData.getTotalRepayment())
                .totalExpectedCostOfLoan(defaultSummaryData.getTotalExpectedCostOfLoan())
                .totalCostOfLoan(defaultSummaryData.getTotalCostOfLoan()).totalWaived(defaultSummaryData.getTotalWaived())
                .totalWrittenOff(defaultSummaryData.getTotalWrittenOff()).totalOutstanding(defaultSummaryData.getTotalOutstanding())
                .totalOverdue(defaultSummaryData.getTotalOverdue()).overdueSinceDate(defaultSummaryData.getOverdueSinceDate())
                .writeoffReasonId(defaultSummaryData.getWriteoffReasonId()).writeoffReason(defaultSummaryData.getWriteoffReason())
                .totalRecovered(defaultSummaryData.getTotalRecovered()).chargeOffReasonId(defaultSummaryData.getChargeOffReasonId())
                .chargeOffReason(defaultSummaryData.getChargeOffReason()).totalMerchantRefund(totalMerchantRefund)
                .totalMerchantRefundReversed(totalMerchantRefundReversed).totalPayoutRefund(totalPayoutRefund)
                .totalPayoutRefundReversed(totalPayoutRefundReversed).totalGoodwillCredit(totalGoodwillCredit)
                .totalGoodwillCreditReversed(totalGoodwillCreditReversed).totalChargeAdjustment(totalChargeAdjustment)
                .totalChargeAdjustmentReversed(totalChargeAdjustmentReversed).totalChargeback(totalChargeback)
                .totalCreditBalanceRefund(totalCreditBalanceRefund).totalCreditBalanceRefundReversed(totalCreditBalanceRefundReversed)
                .totalRepaymentTransaction(totalRepaymentTransaction).totalRepaymentTransactionReversed(totalRepaymentTransactionReversed)
                .totalInterestPaymentWaiver(totalInterestPaymentWaiver).totalUnpaidPayableDueInterest(totalUnpaidPayableDueInterest)
                .totalUnpaidPayableNotDueInterest(totalUnpaidPayableNotDueInterest).totalInterestRefund(totalInterestRefund).build();
    }

    private static BigDecimal fetchLoanTransactionBalanceByType(final Collection<LoanTransactionBalance> loanTransactionBalances,
            final Integer transactionType) {
        final Optional<LoanTransactionBalance> optLoanTransactionBalance = loanTransactionBalances.stream()
                .filter(balance -> balance.getTransactionType().equals(transactionType) && !balance.isReversed()).findFirst();
        return optLoanTransactionBalance.isPresent() ? optLoanTransactionBalance.get().getAmount() : BigDecimal.ZERO;
    }

    private static BigDecimal fetchLoanTransactionBalanceReversedByType(final Collection<LoanTransactionBalance> loanTransactionBalances,
            final Integer transactionType) {
        final Optional<LoanTransactionBalance> optLoanTransactionBalance = loanTransactionBalances.stream()
                .filter(balance -> balance.getTransactionType().equals(transactionType) && balance.isReversed()
                        && balance.isManuallyAdjustedOrReversed())
                .findFirst();
        return optLoanTransactionBalance.isPresent() ? optLoanTransactionBalance.get().getAmount() : BigDecimal.ZERO;
    }

    @Override
    public BigDecimal computeTotalUnpaidPayableDueInterestAmount(Collection<LoanSchedulePeriodData> periods, final LocalDate businessDate) {
        return periods.stream().filter(period -> !period.isDownPaymentPeriod() && !businessDate.isBefore(period.getDueDate()))
                .map(LoanSchedulePeriodData::getInterestOutstanding).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    @Override
    public LoanSummaryData withOnlyCurrencyData(CurrencyData currencyData) {
        return LoanSummaryData.builder().currency(currencyData).build();
    }
}
