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
import lombok.Data;
import lombok.experimental.Accessors;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.loanaccount.domain.LoanTransactionType;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object representing loan summary information.
 */
@Data
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
    private final BigDecimal feeChargesDueAtDisbursementCharged;
    private final BigDecimal feeChargesPaid;
    private final BigDecimal feeChargesWaived;
    private final BigDecimal feeChargesWrittenOff;
    private final BigDecimal feeChargesOutstanding;
    private final BigDecimal feeChargesOverdue;
    private final BigDecimal penaltyChargesCharged;
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
    private final Long chargeOffReasonId;
    private final String chargeOffReason;

    public LoanSummaryData(final CurrencyData currency, final BigDecimal principalDisbursed, final BigDecimal principalAdjustments,
            final BigDecimal principalPaid, final BigDecimal principalWrittenOff, final BigDecimal principalOutstanding,
            final BigDecimal principalOverdue, final BigDecimal interestCharged, final BigDecimal interestPaid,
            final BigDecimal interestWaived, final BigDecimal interestWrittenOff, final BigDecimal interestOutstanding,
            final BigDecimal interestOverdue, final BigDecimal feeChargesCharged, final BigDecimal feeChargesDueAtDisbursementCharged,
            final BigDecimal feeChargesPaid, final BigDecimal feeChargesWaived, final BigDecimal feeChargesWrittenOff,
            final BigDecimal feeChargesOutstanding, final BigDecimal feeChargesOverdue, final BigDecimal penaltyChargesCharged,
            final BigDecimal penaltyChargesPaid, final BigDecimal penaltyChargesWaived, final BigDecimal penaltyChargesWrittenOff,
            final BigDecimal penaltyChargesOutstanding, final BigDecimal penaltyChargesOverdue, final BigDecimal totalExpectedRepayment,
            final BigDecimal totalRepayment, final BigDecimal totalExpectedCostOfLoan, final BigDecimal totalCostOfLoan,
            final BigDecimal totalWaived, final BigDecimal totalWrittenOff, final BigDecimal totalOutstanding,
            final BigDecimal totalOverdue, final LocalDate overdueSinceDate, final Long writeoffReasonId, final String writeoffReason,
            final BigDecimal totalRecovered, final Long chargeOffReasonId, final String chargeOffReason) {
        this.currency = currency;
        this.principalDisbursed = principalDisbursed;
        this.principalAdjustments = principalAdjustments;
        this.principalPaid = principalPaid;
        this.principalWrittenOff = principalWrittenOff;
        this.principalOutstanding = principalOutstanding;
        this.principalOverdue = principalOverdue;
        this.interestCharged = interestCharged;
        this.interestPaid = interestPaid;
        this.interestWaived = interestWaived;
        this.interestWrittenOff = interestWrittenOff;
        this.interestOutstanding = interestOutstanding;
        this.interestOverdue = interestOverdue;
        this.feeChargesCharged = feeChargesCharged;
        this.feeChargesDueAtDisbursementCharged = feeChargesDueAtDisbursementCharged;
        this.feeChargesPaid = feeChargesPaid;
        this.feeChargesWaived = feeChargesWaived;
        this.feeChargesWrittenOff = feeChargesWrittenOff;
        this.feeChargesOutstanding = feeChargesOutstanding;
        this.feeChargesOverdue = feeChargesOverdue;
        this.penaltyChargesCharged = penaltyChargesCharged;
        this.penaltyChargesPaid = penaltyChargesPaid;
        this.penaltyChargesWaived = penaltyChargesWaived;
        this.penaltyChargesWrittenOff = penaltyChargesWrittenOff;
        this.penaltyChargesOutstanding = penaltyChargesOutstanding;
        this.penaltyChargesOverdue = penaltyChargesOverdue;
        this.totalExpectedRepayment = totalExpectedRepayment;
        this.totalRepayment = totalRepayment;
        this.totalExpectedCostOfLoan = totalExpectedCostOfLoan;
        this.totalCostOfLoan = totalCostOfLoan;
        this.totalWaived = totalWaived;
        this.totalWrittenOff = totalWrittenOff;
        this.totalOutstanding = totalOutstanding;
        this.totalOverdue = totalOverdue;
        this.overdueSinceDate = overdueSinceDate;
        this.writeoffReasonId = writeoffReasonId;
        this.writeoffReason = writeoffReason;
        this.totalRecovered = totalRecovered;
        this.chargeOffReasonId = chargeOffReasonId;
        this.chargeOffReason = chargeOffReason;
    }

    public static LoanSummaryData withTransactionAmountsSummary(final LoanSummaryData defaultSummaryData,
            final Collection<LoanTransactionData> loanTransactions) {

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

        if (!CollectionUtils.isEmpty(loanTransactions)) {

            totalMerchantRefund = computeTotalAmountForNonReversedTransactions(LoanTransactionType.MERCHANT_ISSUED_REFUND,
                    loanTransactions);
            totalMerchantRefundReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.MERCHANT_ISSUED_REFUND,
                    loanTransactions);
            totalPayoutRefund = computeTotalAmountForNonReversedTransactions(LoanTransactionType.PAYOUT_REFUND, loanTransactions);
            totalPayoutRefundReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.PAYOUT_REFUND, loanTransactions);
            totalGoodwillCredit = computeTotalAmountForNonReversedTransactions(LoanTransactionType.GOODWILL_CREDIT, loanTransactions);
            totalGoodwillCreditReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.GOODWILL_CREDIT, loanTransactions);
            totalChargeAdjustment = computeTotalAmountForNonReversedTransactions(LoanTransactionType.CHARGE_ADJUSTMENT, loanTransactions);
            totalChargeAdjustmentReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.CHARGE_ADJUSTMENT,
                    loanTransactions);
            totalChargeback = computeTotalAmountForNonReversedTransactions(LoanTransactionType.CHARGEBACK, loanTransactions);
            totalCreditBalanceRefund = computeTotalAmountForNonReversedTransactions(LoanTransactionType.CREDIT_BALANCE_REFUND,
                    loanTransactions);
            totalCreditBalanceRefundReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.CREDIT_BALANCE_REFUND,
                    loanTransactions);
            totalRepaymentTransaction = computeTotalAmountForNonReversedTransactions(LoanTransactionType.REPAYMENT, loanTransactions);
            totalRepaymentTransactionReversed = computeTotalAmountForReversedTransactions(LoanTransactionType.REPAYMENT, loanTransactions);
        }

        return new LoanSummaryData(defaultSummaryData.currency, defaultSummaryData.principalDisbursed,
                defaultSummaryData.principalAdjustments, defaultSummaryData.principalPaid, defaultSummaryData.principalWrittenOff,
                defaultSummaryData.principalOutstanding, defaultSummaryData.principalOverdue, defaultSummaryData.interestCharged,
                defaultSummaryData.interestPaid, defaultSummaryData.interestWaived, defaultSummaryData.interestWrittenOff,
                defaultSummaryData.interestOutstanding, defaultSummaryData.interestOverdue, defaultSummaryData.feeChargesCharged,
                defaultSummaryData.feeChargesDueAtDisbursementCharged, defaultSummaryData.feeChargesPaid,
                defaultSummaryData.feeChargesWaived, defaultSummaryData.feeChargesWrittenOff, defaultSummaryData.feeChargesOutstanding,
                defaultSummaryData.feeChargesOverdue, defaultSummaryData.penaltyChargesCharged, defaultSummaryData.penaltyChargesPaid,
                defaultSummaryData.penaltyChargesWaived, defaultSummaryData.penaltyChargesWrittenOff,
                defaultSummaryData.penaltyChargesOutstanding, defaultSummaryData.penaltyChargesOverdue,
                defaultSummaryData.totalExpectedRepayment, defaultSummaryData.totalRepayment, defaultSummaryData.totalExpectedCostOfLoan,
                defaultSummaryData.totalCostOfLoan, defaultSummaryData.totalWaived, defaultSummaryData.totalWrittenOff,
                defaultSummaryData.totalOutstanding, defaultSummaryData.totalOverdue, defaultSummaryData.overdueSinceDate,
                defaultSummaryData.writeoffReasonId, defaultSummaryData.writeoffReason, defaultSummaryData.totalRecovered,
                defaultSummaryData.chargeOffReasonId, defaultSummaryData.chargeOffReason).setTotalMerchantRefund(totalMerchantRefund)
                .setTotalMerchantRefundReversed(totalMerchantRefundReversed).setTotalPayoutRefund(totalPayoutRefund)
                .setTotalPayoutRefundReversed(totalPayoutRefundReversed).setTotalGoodwillCredit(totalGoodwillCredit)
                .setTotalGoodwillCreditReversed(totalGoodwillCreditReversed).setTotalChargeAdjustment(totalChargeAdjustment)
                .setTotalChargeAdjustmentReversed(totalChargeAdjustmentReversed).setTotalChargeback(totalChargeback)
                .setTotalCreditBalanceRefund(totalCreditBalanceRefund).setTotalCreditBalanceRefundReversed(totalCreditBalanceRefundReversed)
                .setTotalRepaymentTransaction(totalRepaymentTransaction)
                .setTotalRepaymentTransactionReversed(totalRepaymentTransactionReversed);
    }

    public static LoanSummaryData withOnlyCurrencyData(CurrencyData currencyData) {
        return new LoanSummaryData(currencyData, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null, null,
                null, null, null, null);
    }

    private static BigDecimal computeTotalAmountForReversedTransactions(LoanTransactionType transactionType,
            Collection<LoanTransactionData> loanTransactions) {
        return loanTransactions.stream().filter(
                transaction -> transaction.getType().getCode().equals(transactionType.getCode()) && transaction.getReversedOnDate() != null)
                .map(txn -> txn.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    private static BigDecimal computeTotalAmountForNonReversedTransactions(LoanTransactionType transactionType,
            Collection<LoanTransactionData> loanTransactions) {
        return loanTransactions.stream().filter(
                transaction -> transaction.getType().getCode().equals(transactionType.getCode()) && transaction.getReversedOnDate() == null)
                .map(txn -> txn.getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
