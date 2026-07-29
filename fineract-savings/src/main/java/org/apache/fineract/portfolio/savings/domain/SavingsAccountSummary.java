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
package org.apache.fineract.portfolio.savings.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Embeddable;
import jakarta.persistence.Transient;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;

/**
 * {@link SavingsAccountSummary} encapsulates all the summary details of a {@link SavingsAccount}.
 */
@Embeddable
public final class SavingsAccountSummary {

    @Column(name = "total_deposits_derived", scale = 6, precision = 19)
    private BigDecimal totalDeposits;

    @Column(name = "total_withdrawals_derived", scale = 6, precision = 19)
    private BigDecimal totalWithdrawals;

    @Column(name = "total_interest_earned_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestEarned;

    @Column(name = "total_interest_posted_derived", scale = 6, precision = 19)
    private BigDecimal totalInterestPosted;

    @Column(name = "total_withdrawal_fees_derived", scale = 6, precision = 19)
    private BigDecimal totalWithdrawalFees;

    @Column(name = "total_fees_charge_derived", scale = 6, precision = 19)
    private BigDecimal totalFeeCharge;

    @Column(name = "total_penalty_charge_derived", scale = 6, precision = 19)
    private BigDecimal totalPenaltyCharge;

    @Column(name = "total_annual_fees_derived", scale = 6, precision = 19)
    private BigDecimal totalAnnualFees;

    @Column(name = "account_balance_derived", scale = 6, precision = 19)
    private BigDecimal accountBalance = BigDecimal.ZERO;

    // TODO: AA do we need this data to be persisted.
    @Transient
    private BigDecimal totalFeeChargesWaived = BigDecimal.ZERO;

    @Transient
    private BigDecimal totalPenaltyChargesWaived = BigDecimal.ZERO;

    @Column(name = "total_overdraft_interest_derived", scale = 6, precision = 19)
    private BigDecimal totalOverdraftInterestDerived;

    @Column(name = "total_withhold_tax_derived", scale = 6, precision = 19)
    private BigDecimal totalWithholdTax;

    @Column(name = "last_interest_calculation_date")
    private LocalDate lastInterestCalculationDate;

    // Currently this represents the last interest posting date
    @Column(name = "interest_posted_till_date")
    private LocalDate interestPostedTillDate;

    @Transient
    private BigDecimal runningBalanceOnInterestPostingTillDate = BigDecimal.ZERO;

    SavingsAccountSummary() {
        //
    }

    /**
     * Stores the totals of a full summary recalculation computed by
     * {@link SavingsAccountTransactionSummaryWrapper#calculateSummaryTotals}.
     */
    public void updateSummary(final SavingsAccountSummaryTotals totals) {
        this.totalDeposits = totals.totalDeposits();
        this.totalWithdrawals = totals.totalWithdrawals();
        this.totalInterestPosted = totals.totalInterestPosted();
        this.totalWithdrawalFees = totals.totalWithdrawalFees();
        this.totalAnnualFees = totals.totalAnnualFees();
        this.totalFeeCharge = totals.totalFeeCharge();
        this.totalPenaltyCharge = totals.totalPenaltyCharge();
        this.totalFeeChargesWaived = totals.totalFeeChargesWaived();
        this.totalPenaltyChargesWaived = totals.totalPenaltyChargesWaived();
        this.totalOverdraftInterestDerived = totals.totalOverdraftInterestDerived();
        this.totalWithholdTax = totals.totalWithholdTax();
        if (totals.interestPostedTillDate() != null) {
            this.interestPostedTillDate = totals.interestPostedTillDate();
        }
        this.accountBalance = totals.accountBalance();
    }

    /**
     * Stores the totals refreshed after an interest posting when backdated transactions are allowed, computed by
     * {@link SavingsAccountTransactionSummaryWrapper#calculatePivotSummaryTotals}.
     */
    public void updateSummaryWithPivotConfig(final SavingsAccountPivotSummaryTotals totals) {
        this.totalDeposits = totals.totalDeposits();
        this.totalWithdrawals = totals.totalWithdrawals();
        if (totals.interestPostedTillDate() != null) {
            this.interestPostedTillDate = totals.interestPostedTillDate();
        }
        this.totalInterestPosted = totals.totalInterestPosted();
        this.totalOverdraftInterestDerived = totals.totalOverdraftInterestDerived();
        this.totalWithholdTax = totals.totalWithholdTax();
        this.accountBalance = totals.accountBalance();
    }

    public void updateSummaryWithPivotConfig(final MonetaryCurrency currency, final SavingsAccountTransaction transaction) {
        if (transaction.isReversalTransaction()) {
            return;
        }
        Money transactionAmount = Money.of(currency, transaction.getAmount());
        switch (transaction.getTransactionType()) {
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
                    this.totalFeeChargesWaived = Money.of(currency, this.totalFeeChargesWaived).plus(transactionAmount.getAmount())
                            .getAmount();
                } else if (transaction.isWaivePenaltyChargeAndNotReversed()) {
                    this.totalPenaltyChargesWaived = Money.of(currency, this.totalPenaltyChargesWaived).plus(transactionAmount.getAmount())
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
    }

    public void updateFromInterestPeriodSummaries(final MonetaryCurrency currency, final List<PostingPeriod> allPostingPeriods) {
        Money totalEarned = Money.zero(currency);
        for (final PostingPeriod period : allPostingPeriods) {
            Money interestEarned = period.interest();
            interestEarned = interestEarned == null ? Money.zero(currency) : interestEarned;
            totalEarned = totalEarned.plus(interestEarned);
        }
        this.lastInterestCalculationDate = DateUtils.getBusinessLocalDate();
        this.totalInterestEarned = totalEarned.getAmount();
    }

    public boolean isLessThanOrEqualToAccountBalance(final Money amount) {
        final Money accountBalance = getAccountBalance(amount.getCurrency());
        return accountBalance.isGreaterThanOrEqualTo(amount);
    }

    public Money getAccountBalance(final MonetaryCurrency currency) {
        return Money.of(currency, this.accountBalance);
    }

    public BigDecimal getAccountBalance() {
        return this.accountBalance;
    }

    public void setAccountBalance(BigDecimal accountBalance) {
        this.accountBalance = accountBalance;
    }

    public BigDecimal getTotalInterestPosted() {
        return this.totalInterestPosted;
    }

    public LocalDate getLastInterestCalculationDate() {
        return this.lastInterestCalculationDate;
    }

    public void setInterestPostedTillDate(final LocalDate date) {
        this.interestPostedTillDate = date;
    }

    public LocalDate getInterestPostedTillDate() {
        return this.interestPostedTillDate;
    }

    public void setRunningBalanceOnPivotDate(final BigDecimal runningBalanceOnPivotDate) {
        this.runningBalanceOnInterestPostingTillDate = runningBalanceOnPivotDate;
    }

    public BigDecimal getRunningBalanceOnPivotDate() {
        return this.runningBalanceOnInterestPostingTillDate;
    }

    public BigDecimal getTotalWithdrawals() {
        return this.totalWithdrawals;
    }

    public BigDecimal getTotalDeposits() {
        return this.totalDeposits;
    }

    public BigDecimal getTotalWithdrawalFees() {
        return this.totalWithdrawalFees;
    }

    public BigDecimal getTotalFeeCharge() {
        return this.totalFeeCharge;
    }

    public BigDecimal getTotalPenaltyCharge() {
        return this.totalPenaltyCharge;
    }

    public BigDecimal getTotalAnnualFees() {
        return this.totalAnnualFees;
    }

    public BigDecimal getTotalInterestEarned() {
        return this.totalInterestEarned;
    }

    public BigDecimal getTotalOverdraftInterestDerived() {
        return this.totalOverdraftInterestDerived;
    }

    public BigDecimal getTotalWithholdTax() {
        return this.totalWithholdTax;
    }
}
