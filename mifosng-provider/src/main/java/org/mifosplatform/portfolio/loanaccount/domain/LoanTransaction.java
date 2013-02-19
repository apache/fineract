/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * All monetary transactions against a loan are modelled through this entity.
 * Disbursements, Repayments, Waivers, Write-off etc
 */
@Entity
@Table(name = "m_loan_transaction")
public final class LoanTransaction extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @Column(name = "transaction_type_enum", nullable = false)
    private final Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private final Date dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalPortion = BigDecimal.ZERO;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPortion = BigDecimal.ZERO;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPortion = BigDecimal.ZERO;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPortion = BigDecimal.ZERO;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    protected LoanTransaction() {
        this.loan = null;
        this.dateOf = null;
        this.typeOf = null;
    }

    public static LoanTransaction disbursement(final Money amount, final LocalDate disbursementDate) {
        return new LoanTransaction(null, LoanTransactionType.DISBURSEMENT, amount.getAmount(), disbursementDate);
    }

    public static LoanTransaction repayment(final Money amount, final LocalDate paymentDate) {
        return new LoanTransaction(null, LoanTransactionType.REPAYMENT, amount.getAmount(), paymentDate);
    }

    public static LoanTransaction repaymentAtDisbursement(final Money amount, final LocalDate paymentDate) {
        return new LoanTransaction(null, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT, amount.getAmount(), paymentDate);
    }

    public static LoanTransaction waiver(final Loan loan, final Money waived, final LocalDate waiveDate) {
        return new LoanTransaction(loan, LoanTransactionType.WAIVE_INTEREST, waived.getAmount(), waiveDate);
    }

    public static LoanTransaction waiveLoanCharge(final Loan loan, final Money waived, final LocalDate waiveDate,
            final Money feeChargesWaived, final Money penaltyChargesWaived) {
        LoanTransaction waiver = new LoanTransaction(loan, LoanTransactionType.WAIVE_CHARGES, waived.getAmount(), waiveDate);
        waiver.updateChargesComponents(feeChargesWaived, penaltyChargesWaived);

        return waiver;
    }

    public static LoanTransaction writeoff(final Loan loan, final LocalDate writeOffDate) {
        return new LoanTransaction(loan, LoanTransactionType.WRITEOFF, null, writeOffDate);
    }

    private LoanTransaction(final Loan loan, final LoanTransactionType type, final BigDecimal amount, final LocalDate date) {
        this.loan = loan;
        this.typeOf = type.getValue();
        this.amount = amount;
        this.dateOf = date.toDateMidnight().toDate();
    }

    public void reverse() {
        this.reversed = true;
    }

    public void resetDerivedComponents() {
        this.principalPortion = null;
        this.interestPortion = null;
        this.feeChargesPortion = null;
        this.penaltyChargesPortion = null;
    }

    public void updateLoan(final Loan loan) {
        this.loan = loan;
    }

    /**
     * This updates the derived fields of a loan transaction for the principal,
     * interest and interest waived portions.
     * 
     * This accumulates the values passed to the already existent values for
     * each of the portions.
     */
    public void updateComponents(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = principal.getCurrency();
        this.principalPortion = defaultToNullIfZero(getPrincipalPortion(currency).plus(principal).getAmount());
        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest).getAmount());
        updateChargesComponents(feeCharges, penaltyCharges);
    }

    private void updateChargesComponents(final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = feeCharges.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeCharges).getAmount());
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyCharges).getAmount());
    }

    public void updateComponentsAndTotal(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        updateComponents(principal, interest, feeCharges, penaltyCharges);

        final MonetaryCurrency currency = principal.getCurrency();
        this.amount = getPrincipalPortion(currency).plus(getInterestPortion(currency)).plus(getFeeChargesPortion(currency))
                .plus(getPenaltyChargesPortion(currency)).getAmount();
    }

    public Money getPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalPortion);
    }

    public BigDecimal getPrincipalPortion() {
        return this.principalPortion;
    }

    public Money getInterestPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.interestPortion);
    }

    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }

    public Money getFeeChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPortion);
    }

    public BigDecimal getFeePortion() {
        return this.feeChargesPortion;
    }

    public Money getPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPortion);
    }

    public BigDecimal getPenaltyChargesPortion() {
        return this.penaltyChargesPortion;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public LocalDate getTransactionDate() {
        return new LocalDate(this.dateOf);
    }

    public Date getDateOf() {
        return dateOf;
    }

    public LoanTransactionType getTypeOf() {
        return LoanTransactionType.fromInt(this.typeOf);
    }

    public boolean isReversed() {
        return this.reversed;
    }

    public boolean isNotReversed() {
        return !isReversed();
    }

    public boolean isRepayment() {
        return LoanTransactionType.REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotRepayment() {
        return !isRepayment();
    }

    public boolean isDisbursement() {
        return LoanTransactionType.DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isRepaymentAtDisbursement() {
        return LoanTransactionType.REPAYMENT_AT_DISBURSEMENT.equals(getTypeOf()) && isNotReversed();
    }

    private boolean isRecoveryRepayment() {
        return LoanTransactionType.RECOVERY_REPAYMENT.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isInterestWaiver() {
        return LoanTransactionType.WAIVE_INTEREST.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isChargesWaiver() {
        return LoanTransactionType.WAIVE_CHARGES.equals(getTypeOf()) && isNotReversed();
    }

    public boolean isNotInterestWaiver() {
        return !isInterestWaiver();
    }

    public boolean isWaiver() {
        return isInterestWaiver() || isChargesWaiver();
    }

    public boolean isNotWaiver() {
        return !isInterestWaiver() && !isChargesWaiver();
    }

    public boolean isWriteOff() {
        return getTypeOf().isWriteOff() && isNotReversed();
    }

    public boolean isIdentifiedBy(final Long identifier) {
        return this.getId().equals(identifier);
    }

    public boolean isBelongingToLoanOf(final Loan check) {
        return this.loan.getId().equals(check.getId());
    }

    public boolean isNotBelongingToLoanOf(final Loan check) {
        return !isBelongingToLoanOf(check);
    }

    public boolean isNonZero() {
        return this.amount.subtract(BigDecimal.ZERO).doubleValue() > 0;
    }

    public boolean isGreaterThan(final Money monetaryAmount) {
        return getAmount(monetaryAmount.getCurrency()).isGreaterThan(monetaryAmount);
    }

    public boolean isGreaterThanZero(final MonetaryCurrency currency) {
        return getAmount(currency).isGreaterThanZero();
    }

    public boolean isNotZero(final MonetaryCurrency currency) {
        return !getAmount(currency).isZero();
    }

    private BigDecimal defaultToNullIfZero(final BigDecimal value) {
        BigDecimal result = value;
        if (BigDecimal.ZERO.compareTo(value) == 0) {
            result = null;
        }
        return result;
    }

    public LoanTransactionData toData(final CurrencyData currencyData) {
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);
        return new LoanTransactionData(this.getId(), transactionType, currencyData, getTransactionDate(), this.amount,
                this.principalPortion, this.interestPortion, this.feeChargesPortion, this.penaltyChargesPortion);
    }

    public Map<String, Object> toMapData(final CurrencyData currencyData) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<String, Object>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", this.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("disbursement", Boolean.valueOf(this.isDisbursement()));
        thisTransactionData.put("repaymentAtDisbursement", Boolean.valueOf(this.isRepaymentAtDisbursement()));
        thisTransactionData.put("repayment", Boolean.valueOf(this.isRepayment()));
        thisTransactionData.put("recoveryRepayment", Boolean.valueOf(this.isRecoveryRepayment()));
        thisTransactionData.put("contra", Boolean.valueOf(this.isReversed()));
        thisTransactionData.put("waiveInterest", Boolean.valueOf(this.isInterestWaiver()));
        thisTransactionData.put("waiveCharges", Boolean.valueOf(this.isChargesWaiver()));
        thisTransactionData.put("writeOff", Boolean.valueOf(this.isWriteOff()));

        thisTransactionData.put("date", this.getTransactionDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("principalPortion", this.principalPortion);
        thisTransactionData.put("interestPortion", this.interestPortion);
        thisTransactionData.put("feeChargesPortion", this.feeChargesPortion);
        thisTransactionData.put("penaltyChargesPortion", this.penaltyChargesPortion);

        return thisTransactionData;
    }
}