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

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.Transient;

import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.savings.domain.interest.PostingPeriod;
import org.joda.time.LocalDate;

/**
 * {@link SavingsAccountSummary} encapsulates all the summary details of a
 * {@link SavingsAccount}.
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
    
    @Temporal(TemporalType.DATE)
    @Column(name = "last_interest_calculation_date")
    private Date lastInterestCalculationDate;

    protected SavingsAccountSummary() {
        //
    }

    public void updateSummary(final MonetaryCurrency currency, final SavingsAccountTransactionSummaryWrapper wrapper,
            final List<SavingsAccountTransaction> transactions) {

        this.totalDeposits = wrapper.calculateTotalDeposits(currency, transactions);
        this.totalWithdrawals = wrapper.calculateTotalWithdrawals(currency, transactions);
        this.totalInterestPosted = wrapper.calculateTotalInterestPosted(currency, transactions);
        this.totalWithdrawalFees = wrapper.calculateTotalWithdrawalFees(currency, transactions);
        this.totalAnnualFees = wrapper.calculateTotalAnnualFees(currency, transactions);
        this.totalFeeCharge = wrapper.calculateTotalFeesCharge(currency, transactions);
        this.totalPenaltyCharge = wrapper.calculateTotalPenaltyCharge(currency, transactions);
        this.totalFeeChargesWaived = wrapper.calculateTotalFeesChargeWaived(currency, transactions);
        this.totalPenaltyChargesWaived = wrapper.calculateTotalPenaltyChargeWaived(currency, transactions);
        this.totalOverdraftInterestDerived = wrapper.calculateTotalOverdraftInterest(currency, transactions);
        this.totalWithholdTax = wrapper.calculateTotalWithholdTaxWithdrawal(currency, transactions);
        

        this.accountBalance = Money.of(currency, this.totalDeposits).plus(this.totalInterestPosted).minus(this.totalWithdrawals)
                .minus(this.totalWithdrawalFees).minus(this.totalAnnualFees).minus(this.totalFeeCharge).minus(this.totalPenaltyCharge)
                .minus(totalOverdraftInterestDerived).minus(totalWithholdTax).getAmount();
    }

    public void updateFromInterestPeriodSummaries(final MonetaryCurrency currency, final List<PostingPeriod> allPostingPeriods) {

        Money totalEarned = Money.zero(currency);
        LocalDate interestCalculationDate = DateUtils.getLocalDateOfTenant();
        for (final PostingPeriod period : allPostingPeriods) {
            Money interestEarned = period.interest();
            interestEarned = interestEarned == null ? Money.zero(currency) : interestEarned;
            totalEarned = totalEarned.plus(interestEarned);
        }
        this.lastInterestCalculationDate = interestCalculationDate.toDate();
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

    public BigDecimal getTotalInterestPosted() {
        return this.totalInterestPosted;
    }

}