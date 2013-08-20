/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "m_loan_repayment_schedule")
public final class LoanRepaymentScheduleInstallment extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id")
    private Loan loan;

    @Column(name = "installment", nullable = false)
    private final Integer installmentNumber;

    @Temporal(TemporalType.DATE)
    @Column(name = "fromdate", nullable = true)
    private Date fromDate;

    @Temporal(TemporalType.DATE)
    @Column(name = "duedate", nullable = false)
    private Date dueDate;

    @Column(name = "principal_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal principal;

    @Column(name = "principal_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalCompleted;

    @Column(name = "principal_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalWrittenOff;

    @Column(name = "interest_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestCharged;

    @Column(name = "interest_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPaid;

    @Column(name = "interest_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWaived;

    @Column(name = "interest_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestWrittenOff;

    @Column(name = "fee_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesCharged;

    @Column(name = "fee_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPaid;

    @Column(name = "fee_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWrittenOff;

    @Column(name = "fee_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesWaived;

    @Column(name = "penalty_charges_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyCharges;

    @Column(name = "penalty_charges_completed_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPaid;

    @Column(name = "penalty_charges_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWrittenOff;

    @Column(name = "penalty_charges_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesWaived;

    @Column(name = "total_paid_in_advance_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidInAdvance;

    @Column(name = "total_paid_late_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal totalPaidLate;

    @Column(name = "completed_derived", nullable = false)
    private boolean obligationsMet;

    @Temporal(TemporalType.DATE)
    @Column(name = "obligations_met_on_date")
    private Date obligationsMetOnDate;

    protected LoanRepaymentScheduleInstallment() {
        this.installmentNumber = null;
        this.fromDate = null;
        this.dueDate = null;
        this.obligationsMet = false;
    }

    public LoanRepaymentScheduleInstallment(final Loan loan, final Integer installmentNumber, final LocalDate fromDate,
            final LocalDate dueDate, final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges) {
        this.loan = loan;
        this.installmentNumber = installmentNumber;
        this.fromDate = fromDate.toDateMidnight().toDate();
        this.dueDate = dueDate.toDateMidnight().toDate();
        this.principal = defaultToNullIfZero(principal);
        this.interestCharged = defaultToNullIfZero(interest);
        this.feeChargesCharged = defaultToNullIfZero(feeCharges);
        this.penaltyCharges = defaultToNullIfZero(penaltyCharges);
        this.obligationsMet = false;
    }

    private BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public Integer getInstallmentNumber() {
        return this.installmentNumber;
    }

    public LocalDate getFromDate() {
        LocalDate fromLocalDate = null;
        if (this.fromDate != null) {
            fromLocalDate = new LocalDate(this.fromDate);
        }

        return fromLocalDate;
    }

    public LocalDate getDueDate() {
        return new LocalDate(this.dueDate);
    }

    public Money getPrincipal(final MonetaryCurrency currency) {
        return Money.of(currency, this.principal);
    }

    public Money getPrincipalCompleted(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalCompleted);
    }

    public Money getPrincipalWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalWrittenOff);
    }

    public Money getPrincipalOutstanding(final MonetaryCurrency currency) {
        final Money principalAccountedFor = getPrincipalCompleted(currency).plus(getPrincipalWrittenOff(currency));
        return getPrincipal(currency).minus(principalAccountedFor);
    }

    public Money getInterestCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestCharged);
    }

    public Money getInterestPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPaid);
    }

    public Money getInterestWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWaived);
    }

    public Money getInterestWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestWrittenOff);
    }

    public Money getInterestOutstanding(final MonetaryCurrency currency) {
        final Money interestAccountedFor = getInterestPaid(currency).plus(getInterestWaived(currency))
                .plus(getInterestWrittenOff(currency));
        return getInterestCharged(currency).minus(interestAccountedFor);
    }

    public Money getFeeChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesCharged);
    }

    public Money getFeeChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPaid);
    }

    public Money getFeeChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWaived);
    }

    public Money getFeeChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesWrittenOff);
    }

    public Money getFeeChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getFeeChargesPaid(currency).plus(getFeeChargesWaived(currency)).plus(
                getFeeChargesWrittenOff(currency));
        return getFeeChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public Money getPenaltyChargesCharged(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyCharges);
    }

    public Money getPenaltyChargesPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPaid);
    }

    public Money getPenaltyChargesWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWaived);
    }

    public Money getPenaltyChargesWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesWrittenOff);
    }

    public Money getPenaltyChargesOutstanding(final MonetaryCurrency currency) {
        final Money feeChargesAccountedFor = getPenaltyChargesPaid(currency).plus(getPenaltyChargesWaived(currency)).plus(
                getPenaltyChargesWrittenOff(currency));
        return getPenaltyChargesCharged(currency).minus(feeChargesAccountedFor);
    }

    public boolean isInterestDue(final MonetaryCurrency currency) {
        return getInterestOutstanding(currency).isGreaterThanZero();
    }

    public Money getTotalPrincipalAndInterest(final MonetaryCurrency currency) {
        return getPrincipal(currency).plus(getInterestCharged(currency));
    }

    public Money getTotalOutstanding(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).plus(getInterestOutstanding(currency)).plus(getFeeChargesOutstanding(currency))
                .plus(getPenaltyChargesOutstanding(currency));
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    public boolean isObligationsMet() {
        return this.obligationsMet;
    }

    public boolean isNotFullyPaidOff() {
        return !this.obligationsMet;
    }

    public boolean isPrincipalNotCompleted(final MonetaryCurrency currency) {
        return !isPrincipalCompleted(currency);
    }

    public boolean isPrincipalCompleted(final MonetaryCurrency currency) {
        return getPrincipalOutstanding(currency).isZero();
    }

    public void resetDerivedComponents() {
        this.principalCompleted = null;
        this.principalWrittenOff = null;
        this.interestPaid = null;
        this.interestWaived = null;
        this.interestWrittenOff = null;
        this.feeChargesPaid = null;
        this.feeChargesWaived = null;
        this.feeChargesWrittenOff = null;
        this.penaltyChargesPaid = null;
        this.penaltyChargesWaived = null;
        this.penaltyChargesWrittenOff = null;
        this.totalPaidInAdvance = null;
        this.totalPaidLate = null;

        this.obligationsMet = false;
        this.obligationsMetOnDate = null;
    }

    public Money payPenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money penaltyPortionOfTransaction = Money.zero(currency);

        final Money penaltyChargesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penaltyChargesDue)) {
            this.penaltyChargesPaid = getPenaltyChargesPaid(currency).plus(penaltyChargesDue).getAmount();
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(penaltyChargesDue);
        } else {
            this.penaltyChargesPaid = getPenaltyChargesPaid(currency).plus(transactionAmountRemaining).getAmount();
            penaltyPortionOfTransaction = penaltyPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.penaltyChargesPaid = defaultToNullIfZero(this.penaltyChargesPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyPortionOfTransaction;
    }

    public Money payFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money feePortionOfTransaction = Money.zero(currency);

        final Money feeChargesDue = getFeeChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feeChargesDue)) {
            this.feeChargesPaid = getFeeChargesPaid(currency).plus(feeChargesDue).getAmount();
            feePortionOfTransaction = feePortionOfTransaction.plus(feeChargesDue);
        } else {
            this.feeChargesPaid = getFeeChargesPaid(currency).plus(transactionAmountRemaining).getAmount();
            feePortionOfTransaction = feePortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.feeChargesPaid = defaultToNullIfZero(this.feeChargesPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, feePortionOfTransaction);

        return feePortionOfTransaction;
    }

    public Money payInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money interestPortionOfTransaction = Money.zero(currency);

        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            this.interestPaid = getInterestPaid(currency).plus(interestDue).getAmount();
            interestPortionOfTransaction = interestPortionOfTransaction.plus(interestDue);
        } else {
            this.interestPaid = getInterestPaid(currency).plus(transactionAmountRemaining).getAmount();
            interestPortionOfTransaction = interestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.interestPaid = defaultToNullIfZero(this.interestPaid);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, interestPortionOfTransaction);

        return interestPortionOfTransaction;
    }

    public Money payPrincipalComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {

        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money principalPortionOfTransaction = Money.zero(currency);

        final Money principalDue = getPrincipalOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(principalDue)) {
            this.principalCompleted = getPrincipalCompleted(currency).plus(principalDue).getAmount();
            principalPortionOfTransaction = principalPortionOfTransaction.plus(principalDue);
        } else {
            this.principalCompleted = getPrincipalCompleted(currency).plus(transactionAmountRemaining).getAmount();
            principalPortionOfTransaction = principalPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.principalCompleted = defaultToNullIfZero(this.principalCompleted);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        trackAdvanceAndLateTotalsForRepaymentPeriod(transactionDate, currency, principalPortionOfTransaction);

        return principalPortionOfTransaction;
    }

    public Money waiveInterestComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedInterestPortionOfTransaction = Money.zero(currency);

        final Money interestDue = getInterestOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(interestDue)) {
            this.interestWaived = getInterestWaived(currency).plus(interestDue).getAmount();
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(interestDue);
        } else {
            this.interestWaived = getInterestWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedInterestPortionOfTransaction = waivedInterestPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.interestWaived = defaultToNullIfZero(this.interestWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedInterestPortionOfTransaction;
    }

    public Money waivePenaltyChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedPenaltyChargesPortionOfTransaction = Money.zero(currency);

        final Money penanltiesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(penanltiesDue)) {
            this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(penanltiesDue).getAmount();
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(penanltiesDue);
        } else {
            this.penaltyChargesWaived = getPenaltyChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedPenaltyChargesPortionOfTransaction = waivedPenaltyChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.penaltyChargesWaived = defaultToNullIfZero(this.penaltyChargesWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedPenaltyChargesPortionOfTransaction;
    }

    public Money waiveFeeChargesComponent(final LocalDate transactionDate, final Money transactionAmountRemaining) {
        final MonetaryCurrency currency = transactionAmountRemaining.getCurrency();
        Money waivedFeeChargesPortionOfTransaction = Money.zero(currency);

        final Money feesDue = getPenaltyChargesOutstanding(currency);
        if (transactionAmountRemaining.isGreaterThanOrEqualTo(feesDue)) {
            this.feeChargesWaived = getFeeChargesWaived(currency).plus(feesDue).getAmount();
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(feesDue);
        } else {
            this.feeChargesWaived = getFeeChargesWaived(currency).plus(transactionAmountRemaining).getAmount();
            waivedFeeChargesPortionOfTransaction = waivedFeeChargesPortionOfTransaction.plus(transactionAmountRemaining);
        }

        this.feeChargesWaived = defaultToNullIfZero(this.feeChargesWaived);

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return waivedFeeChargesPortionOfTransaction;
    }

    public Money writeOffOutstandingPrincipal(final LocalDate transactionDate, final MonetaryCurrency currency) {

        final Money principalDue = getPrincipalOutstanding(currency);
        this.principalWrittenOff = defaultToNullIfZero(principalDue.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return principalDue;
    }

    public Money writeOffOutstandingInterest(final LocalDate transactionDate, final MonetaryCurrency currency) {

        final Money interestDue = getInterestOutstanding(currency);
        this.interestWrittenOff = defaultToNullIfZero(interestDue.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return interestDue;
    }

    public Money writeOffOutstandingFeeCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money feeChargesOutstanding = getFeeChargesOutstanding(currency);
        this.feeChargesWrittenOff = defaultToNullIfZero(feeChargesOutstanding.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return feeChargesOutstanding;
    }

    public Money writeOffOutstandingPenaltyCharges(final LocalDate transactionDate, final MonetaryCurrency currency) {
        final Money penaltyChargesOutstanding = getPenaltyChargesOutstanding(currency);
        this.penaltyChargesWrittenOff = defaultToNullIfZero(penaltyChargesOutstanding.getAmount());

        checkIfRepaymentPeriodObligationsAreMet(transactionDate, currency);

        return penaltyChargesOutstanding;
    }

    public boolean isOverdueOn(final LocalDate date) {
        return getDueDate().isBefore(date);
    }

    public void updateChargePortion(final Money feeChargesDue, final Money feeChargesWaived, final Money feeChargesWrittenOff,
            final Money penaltyChargesDue, final Money penaltyChargesWaived, final Money penaltyChargesWrittenOff) {
        this.feeChargesCharged = defaultToNullIfZero(feeChargesDue.getAmount());
        this.feeChargesWaived = defaultToNullIfZero(feeChargesWaived.getAmount());
        this.feeChargesWrittenOff = defaultToNullIfZero(feeChargesWrittenOff.getAmount());
        this.penaltyCharges = defaultToNullIfZero(penaltyChargesDue.getAmount());
        this.penaltyChargesWaived = defaultToNullIfZero(penaltyChargesWaived.getAmount());
        this.penaltyChargesWrittenOff = defaultToNullIfZero(penaltyChargesWrittenOff.getAmount());
    }

    public void updateDerivedFields(final MonetaryCurrency currency, final LocalDate actualDisbursementDate) {
        if (!this.obligationsMet && getTotalOutstanding(currency).isZero()) {
            this.obligationsMet = true;
            this.obligationsMetOnDate = actualDisbursementDate.toDate();
        }
    }

    private void trackAdvanceAndLateTotalsForRepaymentPeriod(final LocalDate transactionDate, final MonetaryCurrency currency,
            final Money amountPaidInRepaymentPeriod) {
        if (isInAdvance(transactionDate)) {
            this.totalPaidInAdvance = asMoney(this.totalPaidInAdvance, currency).plus(amountPaidInRepaymentPeriod).getAmount();
        } else if (isLatePayment(transactionDate)) {
            this.totalPaidLate = asMoney(this.totalPaidLate, currency).plus(amountPaidInRepaymentPeriod).getAmount();
        }
    }

    private Money asMoney(final BigDecimal decimal, final MonetaryCurrency currency) {
        return Money.of(currency, decimal);
    }

    private boolean isInAdvance(final LocalDate transactionDate) {
        return transactionDate.isBefore(getDueDate());
    }

    private boolean isLatePayment(final LocalDate transactionDate) {
        return transactionDate.isAfter(getDueDate());
    }

    private void checkIfRepaymentPeriodObligationsAreMet(final LocalDate transactionDate, final MonetaryCurrency currency) {
        this.obligationsMet = getTotalOutstanding(currency).isZero();
        if (this.obligationsMet) {
            this.obligationsMetOnDate = transactionDate.toDate();
        }
    }
    
    public void updateDueDate(final LocalDate newDueDate){
        if(newDueDate != null){
            this.dueDate = newDueDate.toDate();
        }
    }
    
    public void updateFromDate(final LocalDate newFromDate){
        if(newFromDate != null){
            this.fromDate = newFromDate.toDate();
        }
    }
}