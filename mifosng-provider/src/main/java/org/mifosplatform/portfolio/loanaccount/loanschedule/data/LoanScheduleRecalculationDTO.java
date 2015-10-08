/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.loanschedule.data;

import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.domain.LoanRepaymentScheduleInstallment;
import org.mifosplatform.portfolio.loanaccount.domain.transactionprocessor.LoanRepaymentScheduleTransactionProcessor;
import org.mifosplatform.portfolio.loanaccount.loanschedule.domain.RecalculationDetail;

public class LoanScheduleRecalculationDTO {

    private final int periodNumber;
    private final int instalmentNumber;
    private final LocalDate periodStartDate;
    private final LocalDate actualRepaymentDate;
    private final Money totalCumulativePrincipal;
    private final Money totalCumulativeInterest;
    private final Money totalFeeChargesCharged;
    private final Money totalPenaltyChargesCharged;
    private final Money totalRepaymentExpected;
    private final Money reducePrincipal;
    private final Map<LocalDate, Money> principalPortionMap;
    private final Map<LocalDate, Money> latePaymentMap;
    private final TreeMap<LocalDate, Money> compoundingMap;
    private final Map<LocalDate, Money> disburseDetailMap;
    private final Money outstandingBalance;
    private final Money outstandingBalanceAsPerRest;
    private final List<LoanRepaymentScheduleInstallment> installments;
    private final Collection<RecalculationDetail> recalculationDetails;
    private final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor;
    private final LocalDate scheduleTillDate;
    private final boolean partialUpdate;

    private LoanScheduleRecalculationDTO(final int periodNumber, final int instalmentNumber, LocalDate periodStartDate,
            final LocalDate actualRepaymentDate, final Money totalCumulativePrincipal, final Money totalCumulativeInterest,
            final Money totalFeeChargesCharged, final Money totalPenaltyChargesCharged, final Money totalRepaymentExpected,
            final Money reducePrincipal, final Map<LocalDate, Money> principalPortionMap, final Map<LocalDate, Money> latePaymentMap,
            final TreeMap<LocalDate, Money> compoundingMap, final Map<LocalDate, Money> disburseDetailMap, final Money outstandingBalance,
            final Money outstandingBalanceAsPerRest, final List<LoanRepaymentScheduleInstallment> installments,
            final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate,
            final boolean partialUpdate) {
        this.periodNumber = periodNumber;
        this.instalmentNumber = instalmentNumber;
        this.periodStartDate = periodStartDate;
        this.actualRepaymentDate = actualRepaymentDate;
        this.totalCumulativePrincipal = totalCumulativePrincipal;
        this.totalCumulativeInterest = totalCumulativeInterest;
        this.totalFeeChargesCharged = totalFeeChargesCharged;
        this.totalPenaltyChargesCharged = totalPenaltyChargesCharged;
        this.totalRepaymentExpected = totalRepaymentExpected;
        this.reducePrincipal = reducePrincipal;
        this.principalPortionMap = principalPortionMap;
        this.latePaymentMap = latePaymentMap;
        this.compoundingMap = compoundingMap;
        this.disburseDetailMap = disburseDetailMap;
        this.outstandingBalance = outstandingBalance;
        this.outstandingBalanceAsPerRest = outstandingBalanceAsPerRest;
        this.installments = installments;
        this.recalculationDetails = recalculationDetails;
        this.loanRepaymentScheduleTransactionProcessor = loanRepaymentScheduleTransactionProcessor;
        this.scheduleTillDate = scheduleTillDate;
        this.partialUpdate = partialUpdate;
    }

    public static LoanScheduleRecalculationDTO createLoanScheduleDTOForPartialUpdate(final int periodNumber, final int instalmentNumber,
            LocalDate periodStartDate, final LocalDate actualRepaymentDate, final Money totalCumulativePrincipal,
            final Money totalCumulativeInterest, final Money totalFeeChargesCharged, final Money totalPenaltyChargesCharged,
            final Money totalRepaymentExpected, final Money reducePrincipal, final Map<LocalDate, Money> principalPortionMap,
            final Map<LocalDate, Money> latePaymentMap, final TreeMap<LocalDate, Money> compoundingMap,
            final Map<LocalDate, Money> disburseDetailMap, final Money outstandingBalance, final Money outstandingBalanceAsPerRest,
            final List<LoanRepaymentScheduleInstallment> installments, final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate) {
        final boolean partialUpdate = true;
        return new LoanScheduleRecalculationDTO(periodNumber, instalmentNumber, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, reducePrincipal, principalPortionMap, latePaymentMap, compoundingMap, disburseDetailMap,
                outstandingBalance, outstandingBalanceAsPerRest, installments, recalculationDetails,
                loanRepaymentScheduleTransactionProcessor, scheduleTillDate, partialUpdate);
    }

    public static LoanScheduleRecalculationDTO createLoanScheduleDTOForCompleteUpdate(
            final Collection<RecalculationDetail> recalculationDetails,
            final LoanRepaymentScheduleTransactionProcessor loanRepaymentScheduleTransactionProcessor, final LocalDate scheduleTillDate) {
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
        final TreeMap<LocalDate, Money> compoundingMap = null;
        final Map<LocalDate, Money> disburseDetailMap = null;
        final Money outstandingBalance = null;
        final Money outstandingBalanceAsPerRest = null;
        final List<LoanRepaymentScheduleInstallment> installments = null;
        final boolean partialUpdate = false;
        return new LoanScheduleRecalculationDTO(periodNumber, instalmentNumber, periodStartDate, actualRepaymentDate,
                totalCumulativePrincipal, totalCumulativeInterest, totalFeeChargesCharged, totalPenaltyChargesCharged,
                totalRepaymentExpected, reducePrincipal, principalPortionMap, latePaymentMap, compoundingMap, disburseDetailMap,
                outstandingBalance, outstandingBalanceAsPerRest, installments, recalculationDetails,
                loanRepaymentScheduleTransactionProcessor, scheduleTillDate, partialUpdate);
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

    public Money getTotalCumulativeInterest() {
        return this.totalCumulativeInterest;
    }

    public Money getTotalFeeChargesCharged() {
        return this.totalFeeChargesCharged;
    }

    public Money getTotalPenaltyChargesCharged() {
        return this.totalPenaltyChargesCharged;
    }

    public Money getTotalRepaymentExpected() {
        return this.totalRepaymentExpected;
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

    public TreeMap<LocalDate, Money> getCompoundingMap() {
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

}
