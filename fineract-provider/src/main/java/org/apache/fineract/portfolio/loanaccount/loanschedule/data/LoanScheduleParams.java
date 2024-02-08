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
package org.apache.fineract.portfolio.loanaccount.loanschedule.data;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.RecalculationDetail;

public final class LoanScheduleParams {

    // Actual period Number as per the schedule
    private int periodNumber;
    // Actual period Number plus interest only repayments
    private int instalmentNumber;

    private LocalDate periodStartDate;
    private LocalDate actualRepaymentDate;

    // variables for cumulative totals
    private Money totalCumulativePrincipal;
    private Money totalCumulativeInterest;
    private Money totalFeeChargesCharged;
    private Money totalPenaltyChargesCharged;
    private Money totalRepaymentExpected;
    private Money totalOutstandingInterestPaymentDueToGrace;

    // early payments will be added here and as per the selected strategy
    // action will be performed on this value
    private Money reducePrincipal;

    // principal changes will be added along with date(after applying rest)
    // from when these amounts will effect the outstanding balance for
    // interest calculation
    private final Map<LocalDate, Money> principalPortionMap;

    // compounding(principal) amounts will be added along with
    // date(after applying compounding frequency)
    // from when these amounts will effect the outstanding balance for
    // interest calculation
    private final Map<LocalDate, Money> latePaymentMap;

    // compounding(interest/Fee) amounts will be added along with
    // date(after applying compounding frequency)
    // from when these amounts will effect the outstanding balance for
    // interest calculation
    private final Map<LocalDate, Money> compoundingMap;
    private final Map<LocalDate, Map<LocalDate, Money>> compoundingDateVariations = new HashMap<>();
    private Money unCompoundedAmount;
    private Money compoundedInLastInstallment;

    public Money getCompoundedInLastInstallment() {
        return this.compoundedInLastInstallment;
    }

    public void setCompoundedInLastInstallment(Money compoundedInLastInstallment) {
        this.compoundedInLastInstallment = compoundedInLastInstallment;
    }

    // disbursement map for tranche details(will added to outstanding
    // balance as per the start date)
    private final Map<LocalDate, Money> disburseDetailMap;
    private Money principalToBeScheduled;
    private Money outstandingBalance;

    // total outstanding balance as per rest for interest calculation.
    private Money outstandingBalanceAsPerRest;

    private final List<LoanRepaymentScheduleInstallment> installments;
    private final Collection<RecalculationDetail> recalculationDetails;
    private final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor;
    private final LocalDate scheduleTillDate;
    private final boolean partialUpdate;
    private int loanTermInDays;
    private final MonetaryCurrency currency;
    private final boolean applyInterestRecalculation;

    private LoanScheduleParams(final int periodNumber, final int instalmentNumber, int loanTermInDays, LocalDate periodStartDate,
            final LocalDate actualRepaymentDate, final Money totalCumulativePrincipal, final Money totalCumulativeInterest,
            final Money totalFeeChargesCharged, final Money totalPenaltyChargesCharged, final Money totalRepaymentExpected,
            Money totalOutstandingInterestPaymentDueToGrace, final Money reducePrincipal, final Map<LocalDate, Money> principalPortionMap,
            final Map<LocalDate, Money> latePaymentMap, final Map<LocalDate, Money> compoundingMap, final Money unCompoundedAmount,
            final Map<LocalDate, Money> disburseDetailMap, Money principalToBeScheduled, final Money outstandingBalance,
            final Money outstandingBalanceAsPerRest, final List<LoanRepaymentScheduleInstallment> installments,
            final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate,
            final boolean partialUpdate, final MonetaryCurrency currency, final boolean applyInterestRecalculation) {
        this.periodNumber = periodNumber;
        this.instalmentNumber = instalmentNumber;
        this.loanTermInDays = loanTermInDays;
        this.periodStartDate = periodStartDate;
        this.actualRepaymentDate = actualRepaymentDate;
        this.totalCumulativePrincipal = totalCumulativePrincipal;
        this.totalCumulativeInterest = totalCumulativeInterest;
        this.totalFeeChargesCharged = totalFeeChargesCharged;
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalRepaymentExpected = totalRepaymentExpected;
        this.totalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace;
        this.reducePrincipal = reducePrincipal;
        this.principalPortionMap = principalPortionMap;
        this.latePaymentMap = latePaymentMap;
        this.compoundingMap = compoundingMap;
        this.unCompoundedAmount = unCompoundedAmount;
        this.disburseDetailMap = disburseDetailMap;
        this.principalToBeScheduled = principalToBeScheduled;
        this.outstandingBalance = outstandingBalance;
        this.outstandingBalanceAsPerRest = outstandingBalanceAsPerRest;
        this.installments = installments;
        this.recalculationDetails = recalculationDetails;
        this.loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessor;
        this.scheduleTillDate = scheduleTillDate;
        this.partialUpdate = partialUpdate;
        this.currency = currency;
        this.applyInterestRecalculation = applyInterestRecalculation;
        if (this.currency != null) {
            this.compoundedInLastInstallment = Money.zero(this.currency);
        }
    }

    public static LoanScheduleParams createLoanScheduleParamsForPartialUpdate(final int periodNumber, final int instalmentNumber,
            int loanTermInDays, LocalDate periodStartDate, final LocalDate actualRepaymentDate, final Money totalCumulativePrincipal,
            final Money totalCumulativeInterest, final Money totalFeeChargesCharged, final Money totalPenaltyChargesCharged,
            final Money totalRepaymentExpected, Money totalOutstandingInterestPaymentDueToGrace, final Money reducePrincipal,
            final Map<LocalDate, Money> principalPortionMap, final Map<LocalDate, Money> latePaymentMap,
            final Map<LocalDate, Money> compoundingMap, Money unCompoundedAmount, final Map<LocalDate, Money> disburseDetailMap,
            final Money principalToBeScheduled, final Money outstandingBalance, final Money outstandingBalanceAsPerRest,
            final List<LoanRepaymentScheduleInstallment> installments, final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate,
            final MonetaryCurrency currency, final boolean applyInterestRecalculation) {
        final boolean partialUpdate = true;
        return new LoanScheduleParams(periodNumber, instalmentNumber, loanTermInDays, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, totalOutstandingInterestPaymentDueToGrace, reducePrincipal, principalPortionMap, latePaymentMap,
                compoundingMap, unCompoundedAmount, disburseDetailMap, principalToBeScheduled, outstandingBalance,
                outstandingBalanceAsPerRest, installments, recalculationDetails, loanRepaymentScheduleTransactionProcessor,
                scheduleTillDate, partialUpdate, currency, applyInterestRecalculation);
    }

    public static LoanScheduleParams createLoanScheduleParamsForCompleteUpdate(final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate,
            final boolean applyInterestRecalculation) {
        final int periodNumber = 1;
        final int instalmentNumber = 1;
        final LocalDate periodStartDate = null;
        final LocalDate actualRepaymentDate = null;
        final Money totalCumulativePrincipal = null;
        final Money totalCumulativeInterest = null;
        final Money totalFeeChargesCharged = null;
        final Money totalPenaltyChargesCharged = null;
        final Money totalRepaymentExpected = null;
        final Money reducePrincipal = null;
        final Map<LocalDate, Money> principalPortionMap = null;
        final Map<LocalDate, Money> latePaymentMap = null;
        final Map<LocalDate, Money> compoundingMap = null;
        final Map<LocalDate, Money> disburseDetailMap = null;
        final Money principalToBeScheduled = null;
        final Money outstandingBalance = null;
        final Money outstandingBalanceAsPerRest = null;
        final List<LoanRepaymentScheduleInstallment> installments = null;
        final boolean partialUpdate = false;
        final int loanTermInDays = 0;
        final Money totalOutstandingInterestPaymentDueToGrace = null;
        final MonetaryCurrency currency = null;
        final Money unCompoundedAmount = null;
        return new LoanScheduleParams(periodNumber, instalmentNumber, loanTermInDays, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, totalOutstandingInterestPaymentDueToGrace, reducePrincipal, principalPortionMap, latePaymentMap,
                compoundingMap, unCompoundedAmount, disburseDetailMap, principalToBeScheduled, outstandingBalance,
                outstandingBalanceAsPerRest, installments, recalculationDetails, loanRepaymentScheduleTransactionProcessor,
                scheduleTillDate, partialUpdate, currency, applyInterestRecalculation);
    }

    public static LoanScheduleParams createLoanScheduleParams(final MonetaryCurrency currency, final Money chargesDueAtTimeOfDisbursement,
            final LocalDate periodStartDate, final Money principalToBeScheduled) {
        final int loanTermInDays = 0;
        final int periodNumber = 1;
        final int instalmentNumber = 1;
        final Money totalCumulativePrincipal = Money.zero(currency);
        final Money totalCumulativeInterest = Money.zero(currency);
        final Money totalOutstandingInterestPaymentDueToGrace = Money.zero(currency);
        final LocalDate actualRepaymentDate = periodStartDate;
        final Money totalFeeChargesCharged = chargesDueAtTimeOfDisbursement;
        final Money totalPenaltyChargesCharged = Money.zero(currency);
        final Money totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        final Money reducePrincipal = Money.zero(currency);
        final Map<LocalDate, Money> principalPortionMap = new HashMap<>();
        final Map<LocalDate, Money> latePaymentMap = new HashMap<>();
        final Map<LocalDate, Money> compoundingMap = new TreeMap<>();
        final Map<LocalDate, Money> disburseDetailMap = new TreeMap<>();
        final Money outstandingBalance = principalToBeScheduled;
        final Money outstandingBalanceAsPerRest = principalToBeScheduled;
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        final boolean partialUpdate = false;
        final Collection<RecalculationDetail> recalculationDetails = null;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = null;
        final LocalDate scheduleTillDate = null;
        final Money unCompoundedAmount = Money.zero(currency);
        final boolean applyInterestRecalculation = false;
        return new LoanScheduleParams(periodNumber, instalmentNumber, loanTermInDays, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, totalOutstandingInterestPaymentDueToGrace, reducePrincipal, principalPortionMap, latePaymentMap,
                compoundingMap, unCompoundedAmount, disburseDetailMap, principalToBeScheduled, outstandingBalance,
                outstandingBalanceAsPerRest, installments, recalculationDetails, loanRepaymentScheduleTransactionProcessor,
                scheduleTillDate, partialUpdate, currency, applyInterestRecalculation);
    }

    public static LoanScheduleParams createLoanScheduleParams(final MonetaryCurrency currency, final Money chargesDueAtTimeOfDisbursement,
            final LocalDate periodStartDate, final Money principalToBeScheduled, final LoanScheduleParams loanScheduleParams) {
        final int loanTermInDays = 0;
        final int periodNumber = 1;
        final int instalmentNumber = 1;
        final Money totalCumulativePrincipal = Money.zero(currency);
        final Money totalCumulativeInterest = Money.zero(currency);
        final Money totalOutstandingInterestPaymentDueToGrace = Money.zero(currency);
        final LocalDate actualRepaymentDate = periodStartDate;
        final Money totalFeeChargesCharged = chargesDueAtTimeOfDisbursement;
        final Money totalPenaltyChargesCharged = Money.zero(currency);
        final Money totalRepaymentExpected = chargesDueAtTimeOfDisbursement;
        final Money reducePrincipal = Money.zero(currency);
        final Map<LocalDate, Money> principalPortionMap = new HashMap<>();
        final Map<LocalDate, Money> latePaymentMap = new HashMap<>();
        final Map<LocalDate, Money> compoundingMap = new TreeMap<>();
        final Map<LocalDate, Money> disburseDetailMap = new TreeMap<>();
        final Money outstandingBalance = principalToBeScheduled;
        final Money outstandingBalanceAsPerRest = principalToBeScheduled;
        final List<LoanRepaymentScheduleInstallment> installments = new ArrayList<>();
        final boolean partialUpdate = false;
        final Collection<RecalculationDetail> recalculationDetails = loanScheduleParams.recalculationDetails;
        final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor = loanScheduleParams.loanRepaymentScheduleTransactionProcessor;
        final LocalDate scheduleTillDate = loanScheduleParams.scheduleTillDate;
        final boolean applyInterestRecalculation = loanScheduleParams.applyInterestRecalculation;
        final Money unCompoundedAmount = Money.zero(currency);
        return new LoanScheduleParams(periodNumber, instalmentNumber, loanTermInDays, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, totalOutstandingInterestPaymentDueToGrace, reducePrincipal, principalPortionMap, latePaymentMap,
                compoundingMap, unCompoundedAmount, disburseDetailMap, principalToBeScheduled, outstandingBalance,
                outstandingBalanceAsPerRest, installments, recalculationDetails, loanRepaymentScheduleTransactionProcessor,
                scheduleTillDate, partialUpdate, currency, applyInterestRecalculation);
    }

    public int getPeriodNumber() {
        return this.periodNumber;
    }

    public int getInstalmentNumber() {
        return this.instalmentNumber;
    }

    public LocalDate getActualRepaymentDate() {
        return this.actualRepaymentDate;
    }

    public Money getTotalCumulativePrincipal() {
        return this.totalCumulativePrincipal;
    }

    public void addTotalCumulativePrincipal(final Money totalCumulativePrincipal) {
        this.totalCumulativePrincipal = this.totalCumulativePrincipal.plus(totalCumulativePrincipal);
    }

    public Money getTotalCumulativeInterest() {
        return this.totalCumulativeInterest;
    }

    public void addTotalCumulativeInterest(final Money totalCumulativeInterest) {
        this.totalCumulativeInterest = this.totalCumulativeInterest.plus(totalCumulativeInterest);
    }

    public Money getTotalFeeChargesCharged() {
        return this.totalFeeChargesCharged;
    }

    public void addTotalFeeChargesCharged(final Money totalFeeChargesCharged) {
        this.totalFeeChargesCharged = this.totalFeeChargesCharged.plus(totalFeeChargesCharged);
    }

    public Money getTotalPenaltyChargesCharged() {
        return this.totalPenaltyChargesCharged;
    }

    public void addTotalPenaltyChargesCharged(final Money totalPenaltyChargesCharged) {
        this.totalPenaltyChargesCharged = this.totalPenaltyChargesCharged.plus(totalPenaltyChargesCharged);
    }

    public Money getTotalRepaymentExpected() {
        return this.totalRepaymentExpected;
    }

    public void addTotalRepaymentExpected(final Money totalRepaymentExpected) {
        this.totalRepaymentExpected = this.totalRepaymentExpected.plus(totalRepaymentExpected);
    }

    public Money getReducePrincipal() {
        return this.reducePrincipal;
    }

    public Map<LocalDate, Money> getPrincipalPortionMap() {
        return this.principalPortionMap;
    }

    public Map<LocalDate, Money> getLatePaymentMap() {
        return this.latePaymentMap;
    }

    public Map<LocalDate, Money> getCompoundingMap() {
        return this.compoundingMap;
    }

    public Map<LocalDate, Money> getDisburseDetailMap() {
        return this.disburseDetailMap;
    }

    public Money getOutstandingBalance() {
        return this.outstandingBalance;
    }

    public Money getOutstandingBalanceAsPerRest() {
        return this.outstandingBalanceAsPerRest;
    }

    public List<LoanRepaymentScheduleInstallment> getInstallments() {
        return this.installments;
    }

    public Collection<RecalculationDetail> getRecalculationDetails() {
        return this.recalculationDetails;
    }

    public LoanRepaymentScheduleTransactionProcessor getLoanRepaymentScheduleTransactionProcessor() {
        return this.loanRepaymentScheduleTransactionProcessor;
    }

    public LocalDate getScheduleTillDate() {
        return this.scheduleTillDate;
    }

    public boolean isPartialUpdate() {
        return this.partialUpdate;
    }

    public LocalDate getPeriodStartDate() {
        return this.periodStartDate;
    }

    public Money getPrincipalToBeScheduled() {
        return this.principalToBeScheduled;
    }

    public void setPeriodNumber(int periodNumber) {
        this.periodNumber = periodNumber;
    }

    public void incrementPeriodNumber() {
        this.periodNumber++;
    }

    public void incrementInstalmentNumber() {
        this.instalmentNumber++;
    }

    public void setPeriodStartDate(LocalDate periodStartDate) {
        this.periodStartDate = periodStartDate;
    }

    public void setActualRepaymentDate(LocalDate actualRepaymentDate) {
        this.actualRepaymentDate = actualRepaymentDate;
    }

    public void setReducePrincipal(Money reducePrincipal) {
        this.reducePrincipal = reducePrincipal;
    }

    public void addReducePrincipal(Money reducePrincipal) {
        this.reducePrincipal = this.reducePrincipal.plus(reducePrincipal);
    }

    public void reduceReducePrincipal(Money reducePrincipal) {
        this.reducePrincipal = this.reducePrincipal.minus(reducePrincipal);
    }

    public void setPrincipalToBeScheduled(Money principalToBeScheduled) {
        this.principalToBeScheduled = principalToBeScheduled;
    }

    public void addPrincipalToBeScheduled(Money principalToBeScheduled) {
        this.principalToBeScheduled = this.principalToBeScheduled.plus(principalToBeScheduled);
    }

    public void setOutstandingBalance(Money outstandingBalance) {
        this.outstandingBalance = outstandingBalance;
    }

    public void addOutstandingBalance(Money outstandingBalance) {
        this.outstandingBalance = this.outstandingBalance.plus(outstandingBalance);
    }

    public void reduceOutstandingBalance(Money outstandingBalance) {
        this.outstandingBalance = this.outstandingBalance.minus(outstandingBalance);
    }

    public void setOutstandingBalanceAsPerRest(Money outstandingBalanceAsPerRest) {
        this.outstandingBalanceAsPerRest = outstandingBalanceAsPerRest;
    }

    public void addOutstandingBalanceAsPerRest(Money outstandingBalanceAsPerRest) {
        this.outstandingBalanceAsPerRest = this.outstandingBalanceAsPerRest.plus(outstandingBalanceAsPerRest);
    }

    public void reduceOutstandingBalanceAsPerRest(Money outstandingBalanceAsPerRest) {
        this.outstandingBalanceAsPerRest = this.outstandingBalanceAsPerRest.minus(outstandingBalanceAsPerRest);
    }

    public int getLoanTermInDays() {
        return this.loanTermInDays;
    }

    public void addLoanTermInDays(int loanTermInDays) {
        this.loanTermInDays += loanTermInDays;
    }

    public Money getTotalOutstandingInterestPaymentDueToGrace() {
        return this.totalOutstandingInterestPaymentDueToGrace;
    }

    public void setTotalOutstandingInterestPaymentDueToGrace(Money totalOutstandingInterestPaymentDueToGrace) {
        this.totalOutstandingInterestPaymentDueToGrace = totalOutstandingInterestPaymentDueToGrace;
    }

    public Map<LocalDate, Map<LocalDate, Money>> getCompoundingDateVariations() {
        return this.compoundingDateVariations;
    }

    public MonetaryCurrency getCurrency() {
        return this.currency;
    }

    public boolean applyInterestRecalculation() {
        return this.applyInterestRecalculation;
    }

    public Money getUnCompoundedAmount() {
        return this.unCompoundedAmount;
    }

    public void addUnCompoundedAmount(Money unCompoundedAmount) {
        this.unCompoundedAmount = this.unCompoundedAmount.plus(unCompoundedAmount);
    }

    public void minusUnCompoundedAmount(Money unCompoundedAmount) {
        this.unCompoundedAmount = this.unCompoundedAmount.minus(unCompoundedAmount);
    }

    public void setUnCompoundedAmount(Money unCompoundedAmount) {
        this.unCompoundedAmount = unCompoundedAmount;
    }

    public boolean isFirstPeriod() {
        return 1 == instalmentNumber;
    }
}
