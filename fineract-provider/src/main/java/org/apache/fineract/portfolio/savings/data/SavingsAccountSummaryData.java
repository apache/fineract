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
package org.apache.fineract.portfolio.savings.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransaction;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountTransactionSummaryWrapper;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;

/**
 * Immutable data object representing Savings Account summary information.
 */
@SuppressWarnings("unused")
public class SavingsAccountSummaryData implements Serializable {

    private final CurrencyData currency;
    private BigDecimal totalDeposits;
    private BigDecimal totalWithdrawals;
    private BigDecimal totalWithdrawalFees;
    private BigDecimal totalAnnualFees;
    private BigDecimal totalInterestEarned;
    private BigDecimal totalInterestPosted;
    private BigDecimal accountBalance;
    private BigDecimal totalFeeCharge;
    private BigDecimal totalPenaltyCharge;
    private BigDecimal totalOverdraftInterestDerived;
    private BigDecimal totalWithholdTax;
    private BigDecimal interestNotPosted;
    private LocalDate lastInterestCalculationDate;
    private BigDecimal availableBalance;
    private LocalDate interestPostedTillDate;
    private LocalDate prevInterestPostedTillDate;
    private transient BigDecimal runningBalanceOnInterestPostingTillDate = BigDecimal.ZERO;

    public SavingsAccountSummaryData(final CurrencyData currency, final BigDecimal totalDeposits, final BigDecimal totalWithdrawals,
            final BigDecimal totalWithdrawalFees, final BigDecimal totalAnnualFees, final BigDecimal totalInterestEarned,
            final BigDecimal totalInterestPosted, final BigDecimal accountBalance, final BigDecimal totalFeeCharge,
            final BigDecimal totalPenaltyCharge, final BigDecimal totalOverdraftInterestDerived, final BigDecimal totalWithholdTax,
            final BigDecimal interestNotPosted, final LocalDate lastInterestCalculationDate, final BigDecimal availableBalance,
            final LocalDate interestPostedTillDate) {
        this.currency = currency;
        this.totalDeposits = totalDeposits;
        this.totalWithdrawals = totalWithdrawals;
        this.totalWithdrawalFees = totalWithdrawalFees;
        this.totalAnnualFees = totalAnnualFees;
        this.totalInterestEarned = totalInterestEarned;
        this.totalInterestPosted = totalInterestPosted;
        this.accountBalance = accountBalance;
        this.totalFeeCharge = totalFeeCharge;
        this.totalPenaltyCharge = totalPenaltyCharge;
        this.totalOverdraftInterestDerived = totalOverdraftInterestDerived;
        this.totalWithholdTax = totalWithholdTax;
        this.interestNotPosted = interestNotPosted;
        this.lastInterestCalculationDate = lastInterestCalculationDate;
        this.availableBalance = availableBalance;
        this.interestPostedTillDate = interestPostedTillDate;
    }

    public void setPrevInterestPostedTillDate(LocalDate interestPostedTillDate) {
        this.prevInterestPostedTillDate = interestPostedTillDate;
    }

    public LocalDate getPrevInterestPostedTillDate() {
        return this.prevInterestPostedTillDate;
    }

    public LocalDate getInterestPostedTillDate() {
        return this.interestPostedTillDate;
    }

    public BigDecimal getTotalInterestPosted() {
        return this.totalInterestPosted;
    }

    public BigDecimal getTotalWithdrawalFees() {
        return this.totalWithdrawalFees;
    }

    public BigDecimal getTotalInterestEarned() {
        return this.totalInterestEarned;
    }

    public BigDecimal getTotalDeposits() {
        return this.totalDeposits;
    }

    public BigDecimal getTotalWithdrawals() {
        return this.totalWithdrawals;
    }

    public BigDecimal getTotalFeeCharge() {
        return this.totalFeeCharge;
    }

    public BigDecimal getAvailableBalance() {
        return this.availableBalance;
    }

    public BigDecimal getTotalOverdraftInterestDerived() {
        return this.totalOverdraftInterestDerived;
    }

    public BigDecimal getTotalWithholdTax() {
        return this.totalWithholdTax;
    }

    public BigDecimal getTotalPenaltyCharge() {
        return this.totalPenaltyCharge;
    }

    public BigDecimal getTotalAnnualFees() {
        return this.totalAnnualFees;
    }

    public LocalDate getLastInterestCalculationDate() {
        return this.lastInterestCalculationDate;
    }

    public BigDecimal getRunningBalanceOnPivotDate() {
        return this.runningBalanceOnInterestPostingTillDate;
    }

    public void updateSummaryWithPivotConfig(final CurrencyData currency, final SavingsAccountTransactionSummaryWrapper wrapper,
            final SavingsAccountTransaction transaction, final List<SavingsAccountTransactionData> savingsAccountTransactions) {

        if (transaction != null) {
            Money transactionAmount = Money.of(currency, transaction.getAmount());
            switch (SavingsAccountTransactionType.fromInt(transaction.getTypeOf())) {
                case DEPOSIT:
                    if (transaction.isDepositAndNotReversed() || transaction.isDividendPayoutAndNotReversed()) {
                        this.totalDeposits = Money.of(currency, this.totalDeposits).plus(transactionAmount).getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).plus(transactionAmount).getAmount();
                    }
                break;
                case WITHDRAWAL:
                    if (transaction.isWithdrawal() && transaction.isNotReversed()) {
                        this.totalWithdrawals = Money.of(currency, this.totalWithdrawals).plus(transactionAmount).getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                case WITHDRAWAL_FEE:
                    if (transaction.isWithdrawalFeeAndNotReversed() && transaction.isNotReversed()) {
                        this.totalWithdrawalFees = Money.of(currency, this.totalWithdrawalFees).plus(transactionAmount).getAmount();
                        this.totalFeeCharge = Money.of(currency, this.totalFeeCharge).plus(transactionAmount).getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                case ANNUAL_FEE:
                    if (transaction.isAnnualFeeAndNotReversed() && transaction.isNotReversed()) {
                        this.totalAnnualFees = Money.of(currency, this.totalAnnualFees).plus(transactionAmount).getAmount();
                        this.totalFeeCharge = Money.of(currency, this.totalFeeCharge).plus(transactionAmount).getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                case WAIVE_CHARGES:
                    if (transaction.isWaiveFeeChargeAndNotReversed()) {
                        this.totalFeeCharge = Money.of(currency, this.totalFeeCharge).plus(transactionAmount.getAmount()).getAmount();
                    } else if (transaction.isWaivePenaltyChargeAndNotReversed()) {
                        this.totalPenaltyCharge = Money.of(currency, this.totalPenaltyCharge).plus(transactionAmount.getAmount())
                                .getAmount();
                    }
                break;
                case PAY_CHARGE:
                    if (transaction.isFeeChargeAndNotReversed()) {
                        this.totalFeeCharge = Money.of(currency, this.totalFeeCharge).plus(transactionAmount).getAmount();
                    } else if (transaction.isPenaltyChargeAndNotReversed()) {
                        this.totalPenaltyCharge = Money.of(currency, this.totalPenaltyCharge).plus(transactionAmount).getAmount();
                    }
                    if (transaction.isFeeChargeAndNotReversed() || transaction.isPenaltyChargeAndNotReversed()) {
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                case OVERDRAFT_INTEREST:
                    if (transaction.isOverdraftInterestAndNotReversed()) {
                        this.totalOverdraftInterestDerived = Money.of(currency, this.totalOverdraftInterestDerived).plus(transactionAmount)
                                .getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                case WITHHOLD_TAX:
                    if (transaction.isWithHoldTaxAndNotReversed()) {
                        this.totalWithholdTax = Money.of(currency, this.totalWithholdTax).plus(transactionAmount).getAmount();
                        this.accountBalance = Money.of(currency, this.accountBalance).minus(transactionAmount).getAmount();
                    }
                break;
                default:
                break;
            }
        } else {
            // boolean isUpdated = false;
            Money interestTotal = Money.of(currency, this.totalInterestPosted);
            Money withHoldTaxTotal = Money.of(currency, this.totalWithholdTax);

            final HashMap<String, Money> map = updateRunningBalanceAndPivotDate(true, savingsAccountTransactions, interestTotal,
                    withHoldTaxTotal, currency);
            interestTotal = map.get("interestTotal");
            withHoldTaxTotal = map.get("withHoldTax");
            this.totalInterestPosted = interestTotal.getAmountDefaultedToNullIfZero();
            this.totalWithholdTax = withHoldTaxTotal.getAmountDefaultedToNullIfZero();
            this.accountBalance = Money.of(currency, this.accountBalance).plus(this.totalInterestPosted).minus(this.totalWithholdTax)
                    .getAmount();
        }
    }

    public void updateFromInterestPeriodSummaries(final MonetaryCurrency currency, final List<PostingPeriod> allPostingPeriods) {

        Money totalEarned = Money.zero(currency);
        LocalDate interestCalculationDate = DateUtils.getLocalDateOfTenant();
        for (final PostingPeriod period : allPostingPeriods) {
            Money interestEarned = period.interest();
            interestEarned = interestEarned == null ? Money.zero(currency) : interestEarned;
            totalEarned = totalEarned.plus(interestEarned);
        }
        this.lastInterestCalculationDate = interestCalculationDate;
        this.totalInterestEarned = totalEarned.getAmount();
    }

    @SuppressWarnings("unchecked")
    private HashMap<String, Money> updateRunningBalanceAndPivotDate(final boolean backdatedTxnsAllowedTill,
            final List<SavingsAccountTransactionData> savingsAccountTransactions, Money interestTotal, Money withHoldTaxTotal,
            CurrencyData currency) {
        boolean isUpdated = false;
        HashMap<String, Money> map = new HashMap<>();
        for (int i = savingsAccountTransactions.size() - 1; i >= 0; i--) {
            final SavingsAccountTransactionData savingsAccountTransaction = savingsAccountTransactions.get(i);
            if (savingsAccountTransaction.isInterestPostingAndNotReversed() && savingsAccountTransaction.isNotReversed() && !isUpdated) {
                setRunningBalanceOnPivotDate(savingsAccountTransaction.getRunningBalance(currency).getAmount());
                setInterestPostedTillDate(savingsAccountTransaction.getTransactionDate());
                isUpdated = true;
                if (!backdatedTxnsAllowedTill) {
                    break;
                }
            }
            if (backdatedTxnsAllowedTill) {
                if (savingsAccountTransaction.isInterestPostingAndNotReversed() && savingsAccountTransaction.isNotReversed()) {
                    interestTotal = interestTotal.plus(savingsAccountTransaction.getAmount());
                }

                if (savingsAccountTransaction.isWithHoldTaxAndNotReversed()) {
                    withHoldTaxTotal = withHoldTaxTotal.plus(savingsAccountTransaction.getAmount());
                }
            }
        }
        if (backdatedTxnsAllowedTill) {
            map.put("interestTotal", interestTotal);
            map.put("withHoldTax", withHoldTaxTotal);
        }
        return map;
    }

    public void updateSummary(final CurrencyData currency, final SavingsAccountTransactionSummaryWrapper wrapper,
            final List<SavingsAccountTransactionData> transactions) {

        this.totalDeposits = wrapper.calculateTotalDeposits(currency, transactions);
        this.totalWithdrawals = wrapper.calculateTotalWithdrawals(currency, transactions);
        this.totalInterestPosted = wrapper.calculateTotalInterestPosted(currency, transactions);
        this.totalWithdrawalFees = wrapper.calculateTotalWithdrawalFees(currency, transactions);
        this.totalAnnualFees = wrapper.calculateTotalAnnualFees(currency, transactions);
        this.totalFeeCharge = wrapper.calculateTotalFeesCharge(currency, transactions);
        this.totalPenaltyCharge = wrapper.calculateTotalPenaltyCharge(currency, transactions);
        this.totalFeeCharge = wrapper.calculateTotalFeesChargeWaived(currency, transactions);
        this.totalPenaltyCharge = wrapper.calculateTotalPenaltyChargeWaived(currency, transactions);
        this.totalOverdraftInterestDerived = wrapper.calculateTotalOverdraftInterest(currency, transactions);
        this.totalWithholdTax = wrapper.calculateTotalWithholdTaxWithdrawal(currency, transactions);

        // boolean isUpdated = false;
        updateRunningBalanceAndPivotDate(false, transactions, null, null, currency);

        this.accountBalance = Money.of(currency, this.totalDeposits).plus(this.totalInterestPosted).minus(this.totalWithdrawals)
                .minus(this.totalWithdrawalFees).minus(this.totalAnnualFees).minus(this.totalFeeCharge).minus(this.totalPenaltyCharge)
                .minus(totalOverdraftInterestDerived).minus(totalWithholdTax).getAmount();
    }

    public void setRunningBalanceOnPivotDate(final BigDecimal runningBalanceOnPivotDate) {
        this.runningBalanceOnInterestPostingTillDate = runningBalanceOnPivotDate;
    }

    public void setInterestPostedTillDate(final LocalDate date) {
        this.interestPostedTillDate = date;
    }

}
