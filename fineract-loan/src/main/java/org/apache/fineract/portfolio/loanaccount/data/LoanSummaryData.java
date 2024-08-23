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
package org.apache.fineract.portfolio.loanaccount.data;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.Collection;
import java.util.Optional;
import lombok.Builder;
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.MathUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanScheduleData;
import org.apache.fineract.portfolio.loanaccount.loanschedule.data.LoanSchedulePeriodData;

/**
 * Immutable data object representing loan summary information.
 */
@Data
@Builder
@Accessors(chain = true)
public class LoanSummaryData {

    private final CurrencyData currency;
    private final BigDecimal principalDisbursed;
    private final BigDecimal principalAdjustments;
    private final BigDecimal principalPaid;
    private final BigDecimal principalWrittenOff;
    private final BigDecimal principalOutstanding;
    private final BigDecimal principalOverdue;
    private final BigDecimal interestCharged;
    private final BigDecimal interestPaid;
    private final BigDecimal interestWaived;
    private final BigDecimal interestWrittenOff;
    private final BigDecimal interestOutstanding;
    private final BigDecimal interestOverdue;
    private final BigDecimal feeChargesCharged;
    private final BigDecimal feeAdjustments;
    private final BigDecimal feeChargesDueAtDisbursementCharged;
    private final BigDecimal feeChargesPaid;
    private final BigDecimal feeChargesWaived;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargesOutstanding;
    private final BigDecimal feeChargesOverdue;
    private final BigDecimal penaltyChargesCharged;
    private final BigDecimal penaltyAdjustments;
    private final BigDecimal penaltyChargesPaid;
    private final BigDecimal penaltyChargesWaived;
    private final BigDecimal penaltyChargesWrittenOff;
    private final BigDecimal penaltyChargesOutstanding;
    private final BigDecimal penaltyChargesOverdue;
    private final BigDecimal totalExpectedRepayment;
    private final BigDecimal totalRepayment;
    private final BigDecimal totalExpectedCostOfLoan;
    private final BigDecimal totalCostOfLoan;
    private final BigDecimal totalWaived;
    private final BigDecimal totalWrittenOff;
    private final BigDecimal totalOutstanding;
    private final BigDecimal totalOverdue;
    private final BigDecimal totalRecovered;
    private final LocalDate overdueSinceDate;
    private final Long writeoffReasonId;
    private final String writeoffReason;

    // Adding fields for transaction summary
    private BigDecimal totalMerchantRefund;
    private BigDecimal totalMerchantRefundReversed;
    private BigDecimal totalPayoutRefund;
    private BigDecimal totalPayoutRefundReversed;
    private BigDecimal totalGoodwillCredit;
    private BigDecimal totalGoodwillCreditReversed;
    private BigDecimal totalChargeAdjustment;
    private BigDecimal totalChargeAdjustmentReversed;
    private BigDecimal totalChargeback;
    private BigDecimal totalCreditBalanceRefund;
    private BigDecimal totalCreditBalanceRefundReversed;
    private BigDecimal totalRepaymentTransaction;
    private BigDecimal totalRepaymentTransactionReversed;
    private BigDecimal totalInterestPaymentWaiver;
    private BigDecimal totalInterestRefund;
    private final Long chargeOffReasonId;
    private final String chargeOffReason;

    private BigDecimal totalUnpaidPayableDueInterest;
    private BigDecimal totalUnpaidPayableNotDueInterest;

    public static LoanSummaryData withTransactionAmountsSummary(final LoanSummaryData defaultSummaryData,
            final LoanScheduleData repaymentSchedule, final Collection<LoanTransactionBalance> loanTransactionBalances) {
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
            totalUnpaidPayableNotDueInterest = computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(repaymentSchedule.getPeriods(),
                    businessDate, defaultSummaryData.currency);
        }

        return LoanSummaryData.builder().currency(defaultSummaryData.currency).principalDisbursed(defaultSummaryData.principalDisbursed)
                .principalAdjustments(defaultSummaryData.principalAdjustments).principalPaid(defaultSummaryData.principalPaid)
                .principalWrittenOff(defaultSummaryData.principalWrittenOff).principalOutstanding(defaultSummaryData.principalOutstanding)
                .principalOverdue(defaultSummaryData.principalOverdue).interestCharged(defaultSummaryData.interestCharged)
                .interestPaid(defaultSummaryData.interestPaid).interestWaived(defaultSummaryData.interestWaived)
                .interestWrittenOff(defaultSummaryData.interestWrittenOff).interestOutstanding(defaultSummaryData.interestOutstanding)
                .interestOverdue(defaultSummaryData.interestOverdue).feeChargesCharged(defaultSummaryData.feeChargesCharged)
                .feeAdjustments(defaultSummaryData.feeAdjustments)
                .feeChargesDueAtDisbursementCharged(defaultSummaryData.feeChargesDueAtDisbursementCharged)
                .feeChargesPaid(defaultSummaryData.feeChargesPaid).feeChargesWaived(defaultSummaryData.feeChargesWaived)
                .feeChargesWrittenOff(defaultSummaryData.feeChargesWrittenOff)
                .feeChargesOutstanding(defaultSummaryData.feeChargesOutstanding).feeChargesOverdue(defaultSummaryData.feeChargesOverdue)
                .penaltyChargesCharged(defaultSummaryData.penaltyChargesCharged).penaltyAdjustments(defaultSummaryData.penaltyAdjustments)
                .penaltyChargesPaid(defaultSummaryData.penaltyChargesPaid).penaltyChargesWaived(defaultSummaryData.penaltyChargesWaived)
                .penaltyChargesWrittenOff(defaultSummaryData.penaltyChargesWrittenOff)
                .penaltyChargesOutstanding(defaultSummaryData.penaltyChargesOutstanding)
                .penaltyChargesOverdue(defaultSummaryData.penaltyChargesOverdue)
                .totalExpectedRepayment(defaultSummaryData.totalExpectedRepayment).totalRepayment(defaultSummaryData.totalRepayment)
                .totalExpectedCostOfLoan(defaultSummaryData.totalExpectedCostOfLoan).totalCostOfLoan(defaultSummaryData.totalCostOfLoan)
                .totalWaived(defaultSummaryData.totalWaived).totalWrittenOff(defaultSummaryData.totalWrittenOff)
                .totalOutstanding(defaultSummaryData.totalOutstanding).totalOverdue(defaultSummaryData.totalOverdue)
                .overdueSinceDate(defaultSummaryData.overdueSinceDate).writeoffReasonId(defaultSummaryData.writeoffReasonId)
                .writeoffReason(defaultSummaryData.writeoffReason).totalRecovered(defaultSummaryData.totalRecovered)
                .chargeOffReasonId(defaultSummaryData.chargeOffReasonId).chargeOffReason(defaultSummaryData.chargeOffReason)
                .totalMerchantRefund(totalMerchantRefund).totalMerchantRefundReversed(totalMerchantRefundReversed)
                .totalPayoutRefund(totalPayoutRefund).totalPayoutRefundReversed(totalPayoutRefundReversed)
                .totalGoodwillCredit(totalGoodwillCredit).totalGoodwillCreditReversed(totalGoodwillCreditReversed)
                .totalChargeAdjustment(totalChargeAdjustment).totalChargeAdjustmentReversed(totalChargeAdjustmentReversed)
                .totalChargeback(totalChargeback).totalCreditBalanceRefund(totalCreditBalanceRefund)
                .totalCreditBalanceRefundReversed(totalCreditBalanceRefundReversed).totalRepaymentTransaction(totalRepaymentTransaction)
                .totalRepaymentTransactionReversed(totalRepaymentTransactionReversed).totalInterestPaymentWaiver(totalInterestPaymentWaiver)
                .totalUnpaidPayableDueInterest(totalUnpaidPayableDueInterest)
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

    public static LoanSummaryData withOnlyCurrencyData(CurrencyData currencyData) {
        return LoanSummaryData.builder().currency(currencyData).build();
    }

    private static BigDecimal computeTotalUnpaidPayableDueInterestAmount(Collection<LoanSchedulePeriodData> periods,
            final LocalDate businessDate) {
        return periods.stream().filter(period -> !period.getDownPaymentPeriod() && businessDate.compareTo(period.getDueDate()) >= 0)
                .map(period -> period.getInterestOutstanding()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal computeTotalUnpaidPayableNotDueInterestAmountOnActualPeriod(final Collection<LoanSchedulePeriodData> periods,
            final LocalDate businessDate, final CurrencyData currency) {
        // Find the current Period (If exists one) based on the Business date
        final Optional<LoanSchedulePeriodData> optCurrentPeriod = periods.stream()
                .filter(period -> !period.getDownPaymentPeriod() && period.isActualPeriodForNotDuePayableCalculation(businessDate))
                .findFirst();

        if (optCurrentPeriod.isPresent()) {
            final LoanSchedulePeriodData currentPeriod = optCurrentPeriod.get();
            final long remainingDays = currentPeriod.getDaysInPeriod()
                    - DateUtils.getDifferenceInDays(currentPeriod.getFromDate(), businessDate);

            return computeAccruedInterestTillDay(currentPeriod, remainingDays, currency);
        }
        // Default value equal to Zero
        return BigDecimal.ZERO;
    }

    public static BigDecimal computeAccruedInterestTillDay(final LoanSchedulePeriodData period, final long untilDay,
            final CurrencyData currency) {
        Integer remainingDays = period.getDaysInPeriod();
        BigDecimal totalAccruedInterest = BigDecimal.ZERO;
        while (remainingDays > untilDay) {
            final BigDecimal accruedInterest = period.getInterestDue().subtract(totalAccruedInterest)
                    .divide(BigDecimal.valueOf(remainingDays), MoneyHelper.getMathContext());
            totalAccruedInterest = totalAccruedInterest.add(accruedInterest);
            remainingDays--;
        }

        totalAccruedInterest = totalAccruedInterest.subtract(period.getInterestPaid()).subtract(period.getInterestWaived());
        if (MathUtil.isLessThanZero(totalAccruedInterest)) {
            // Set Zero If the Interest Paid + Waived is greather than Interest Accrued
            totalAccruedInterest = BigDecimal.ZERO;
        }

        return Money.of(currency, totalAccruedInterest).getAmount();
    }

}
