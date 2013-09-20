/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.portfolio.account.data.AccountTransferData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionData;
import org.mifosplatform.portfolio.loanaccount.data.LoanTransactionEnumData;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.paymentdetail.data.PaymentDetailData;
import org.mifosplatform.portfolio.paymentdetail.domain.PaymentDetail;
import org.springframework.data.jpa.domain.AbstractPersistable;

/**
 * All monetary transactions against a loan are modelled through this entity.
 * Disbursements, Repayments, Waivers, Write-off etc
 */
@Entity
@Table(name = "m_loan_transaction", uniqueConstraints = { @UniqueConstraint(columnNames = { "external_id" }, name = "external_id_UNIQUE") })
public final class LoanTransaction extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_id", nullable = false)
    private Loan loan;

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne(optional = true)
    @JoinColumn(name = "payment_detail_id", nullable = true)
    private PaymentDetail paymentDetail;

    @Column(name = "transaction_type_enum", nullable = false)
    private final Integer typeOf;

    @Temporal(TemporalType.DATE)
    @Column(name = "transaction_date", nullable = false)
    private final Date dateOf;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalPortion;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPortion;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPortion;

    @Column(name = "is_reversed", nullable = false)
    private boolean reversed;

    @Column(name = "external_id", length = 100, nullable = true, unique = true)
    private String externalId;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(cascade = CascadeType.ALL, mappedBy = "loanTransaction", orphanRemoval = true)
    private Set<LoanChargePaidBy> loanChargesPaid = new HashSet<LoanChargePaidBy>();

    protected LoanTransaction() {
        this.loan = null;
        this.dateOf = null;
        this.typeOf = null;
    }

    public static LoanTransaction disbursement(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate disbursementDate, final String externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.DISBURSEMENT, paymentDetail, amount.getAmount(), disbursementDate,
                externalId);
    }

    public static LoanTransaction repayment(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final String externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction loanPayment(final Loan loan, final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final String externalId, final LoanTransactionType transactionType) {
        return new LoanTransaction(loan, office, transactionType, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction repaymentAtDisbursement(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final String externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REPAYMENT_AT_DISBURSEMENT, paymentDetail, amount.getAmount(),
                paymentDate, externalId);
    }

    public static LoanTransaction waiver(final Office office, final Loan loan, final Money waived, final LocalDate waiveDate) {
        return new LoanTransaction(loan, office, LoanTransactionType.WAIVE_INTEREST, waived.getAmount(), waiveDate, null);
    }

    public static LoanTransaction applyInterest(final Office office, final Loan loan, final Money amount,
            final LocalDate interestAppliedDate) {
        return new LoanTransaction(loan, office, LoanTransactionType.APPLY_INTEREST, amount.getAmount(), interestAppliedDate, null);
    }

    public static LoanTransaction initiateTransfer(final Office office, final Loan loan, final LocalDate transferDate) {
        return new LoanTransaction(loan, office, LoanTransactionType.INITIATE_TRANSFER.getValue(), transferDate.toDateMidnight().toDate(),
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary()
                        .getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary()
                        .getTotalPenaltyChargesOutstanding(), false, null);
    }

    public static LoanTransaction approveTransfer(final Office office, final Loan loan, final LocalDate transferDate) {
        return new LoanTransaction(loan, office, LoanTransactionType.APPROVE_TRANSFER.getValue(), transferDate.toDateMidnight().toDate(),
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary()
                        .getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary()
                        .getTotalPenaltyChargesOutstanding(), false, null);
    }

    public static LoanTransaction withdrawTransfer(final Office office, final Loan loan, final LocalDate transferDate) {
        return new LoanTransaction(loan, office, LoanTransactionType.WITHDRAW_TRANSFER.getValue(), transferDate.toDateMidnight().toDate(),
                loan.getSummary().getTotalOutstanding(), loan.getSummary().getTotalPrincipalOutstanding(), loan.getSummary()
                        .getTotalInterestOutstanding(), loan.getSummary().getTotalFeeChargesOutstanding(), loan.getSummary()
                        .getTotalPenaltyChargesOutstanding(), false, null);
    }

    public static LoanTransaction refund(final Office office, final Money amount, final PaymentDetail paymentDetail,
            final LocalDate paymentDate, final String externalId) {
        return new LoanTransaction(null, office, LoanTransactionType.REFUND, paymentDetail, amount.getAmount(), paymentDate, externalId);
    }

    public static LoanTransaction copyTransactionProperties(final LoanTransaction loanTransaction) {
        return new LoanTransaction(loanTransaction.loan, loanTransaction.office, loanTransaction.typeOf, loanTransaction.dateOf,
                loanTransaction.amount, loanTransaction.principalPortion, loanTransaction.interestPortion,
                loanTransaction.feeChargesPortion, loanTransaction.penaltyChargesPortion, loanTransaction.reversed,
                loanTransaction.paymentDetail);
    }

    public static LoanTransaction applyLoanCharge(final Loan loan, final Office office, final Money amount, final LocalDate applyDate,
            final Money feeCharges, final Money penaltyCharges) {
        final LoanTransaction applyCharge = new LoanTransaction(loan, office, LoanTransactionType.APPLY_CHARGES, amount.getAmount(),
                applyDate, null);
        applyCharge.updateChargesComponents(feeCharges, penaltyCharges);
        return applyCharge;
    }

    public static boolean transactionAmountsMatch(final MonetaryCurrency currency, final LoanTransaction loanTransaction,
            final LoanTransaction newLoanTransaction) {
        if (loanTransaction.getAmount(currency).isEqualTo(newLoanTransaction.getAmount(currency))
                && (loanTransaction.getPrincipalPortion(currency).isEqualTo(newLoanTransaction.getPrincipalPortion(currency)))
                && (loanTransaction.getInterestPortion(currency).isEqualTo(newLoanTransaction.getInterestPortion(currency)))
                && (loanTransaction.getFeeChargesPortion(currency).isEqualTo(newLoanTransaction.getFeeChargesPortion(currency)))
                && (loanTransaction.getPenaltyChargesPortion(currency).isEqualTo(newLoanTransaction.getPenaltyChargesPortion(currency)))) { return true; }
        return false;
    }

    private LoanTransaction(final Loan loan, final Office office, final Integer typeOf, final Date dateOf, final BigDecimal amount,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final boolean reversed, final PaymentDetail paymentDetail) {
        super();
        this.loan = loan;
        this.typeOf = typeOf;
        this.dateOf = dateOf;
        this.amount = amount;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.reversed = reversed;
        this.paymentDetail = paymentDetail;
        this.office = office;
    }

    public static LoanTransaction waiveLoanCharge(final Loan loan, final Office office, final Money waived, final LocalDate waiveDate,
            final Money feeChargesWaived, final Money penaltyChargesWaived) {
        final LoanTransaction waiver = new LoanTransaction(loan, office, LoanTransactionType.WAIVE_CHARGES, waived.getAmount(), waiveDate,
                null);
        waiver.updateChargesComponents(feeChargesWaived, penaltyChargesWaived);

        return waiver;
    }

    public static LoanTransaction writeoff(final Loan loan, final Office office, final LocalDate writeOffDate, final String externalId) {
        return new LoanTransaction(loan, office, LoanTransactionType.WRITEOFF, null, writeOffDate, externalId);
    }

    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final BigDecimal amount,
            final LocalDate date, final String externalId) {
        this.loan = loan;
        this.typeOf = type.getValue();
        this.amount = amount;
        this.dateOf = date.toDateMidnight().toDate();
        this.externalId = externalId;
        this.office = office;
    }

    private LoanTransaction(final Loan loan, final Office office, final LoanTransactionType type, final PaymentDetail paymentDetail,
            final BigDecimal amount, final LocalDate date, final String externalId) {
        this.loan = loan;
        this.typeOf = type.getValue();
        this.paymentDetail = paymentDetail;
        this.amount = amount;
        this.dateOf = date.toDateMidnight().toDate();
        this.externalId = externalId;
        this.office = office;
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

    public Money getFeeChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.feeChargesPortion);
    }

    public Money getPenaltyChargesPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.penaltyChargesPortion);
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    public LocalDate getTransactionDate() {
        return new LocalDate(this.dateOf);
    }

    public Date getDateOf() {
        return this.dateOf;
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

    public boolean isAnyTypeOfRepayment() {
        return isRepayment() || isRepaymentAtDisbursement() || isRecoveryRepayment();
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

    public boolean isRecoveryRepayment() {
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

    public boolean isChargePayment() {
        return getTypeOf().isChargePayment() && isNotReversed();
    }

    public boolean isPenaltyPayment() {
        boolean isPenalty = false;
        if (isChargePayment()) {
            for (final LoanChargePaidBy chargePaidBy : this.loanChargesPaid) {
                isPenalty = chargePaidBy.getLoanCharge().isPenaltyCharge();
                break;
            }
        }
        return isPenalty;
    }

    public boolean isWriteOff() {
        return getTypeOf().isWriteOff() && isNotReversed();
    }

    public boolean isIdentifiedBy(final Long identifier) {
        return getId().equals(identifier);
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

    public LoanTransactionData toData(final CurrencyData currencyData, final AccountTransferData transfer) {
        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);
        PaymentDetailData paymentDetailData = null;
        if (this.paymentDetail != null) {
            paymentDetailData = this.paymentDetail.toData();
        }
        return new LoanTransactionData(getId(), this.office.getId(), this.office.getName(), transactionType, paymentDetailData,
                currencyData, getTransactionDate(), this.amount, this.principalPortion, this.interestPortion, this.feeChargesPortion,
                this.penaltyChargesPortion, this.externalId, transfer);
    }

    public Map<String, Object> toMapData(final CurrencyData currencyData) {
        final Map<String, Object> thisTransactionData = new LinkedHashMap<String, Object>();

        final LoanTransactionEnumData transactionType = LoanEnumerations.transactionType(this.typeOf);

        thisTransactionData.put("id", getId());
        thisTransactionData.put("officeId", this.office.getId());
        thisTransactionData.put("type", transactionType);
        thisTransactionData.put("reversed", Boolean.valueOf(isReversed()));
        thisTransactionData.put("date", getTransactionDate());
        thisTransactionData.put("currency", currencyData);
        thisTransactionData.put("amount", this.amount);
        thisTransactionData.put("principalPortion", this.principalPortion);
        thisTransactionData.put("interestPortion", this.interestPortion);
        thisTransactionData.put("feeChargesPortion", this.feeChargesPortion);
        thisTransactionData.put("penaltyChargesPortion", this.penaltyChargesPortion);

        if (this.paymentDetail != null) {
            thisTransactionData.put("paymentTypeId", this.paymentDetail.getPaymentType().getId());
        }

        if (!this.loanChargesPaid.isEmpty()) {
            final List<Map<String, Object>> loanChargesPaidData = new ArrayList<Map<String, Object>>();
            for (final LoanChargePaidBy chargePaidBy : this.loanChargesPaid) {
                final Map<String, Object> loanChargePaidData = new LinkedHashMap<String, Object>();
                loanChargePaidData.put("chargeId", chargePaidBy.getLoanCharge().getCharge().getId());
                loanChargePaidData.put("isPenalty", chargePaidBy.getLoanCharge().getCharge().isPenalty());
                loanChargePaidData.put("loanChargeId", chargePaidBy.getLoanCharge().getId());
                loanChargePaidData.put("amount", chargePaidBy.getAmount());

                loanChargesPaidData.add(loanChargePaidData);
            }
            thisTransactionData.put("loanChargesPaid", loanChargesPaidData);
        }

        return thisTransactionData;
    }

    public Loan getLoan() {
        return this.loan;
    }

    public Set<LoanChargePaidBy> getLoanChargesPaid() {
        return this.loanChargesPaid;
    }

    public void setLoanChargesPaid(final Set<LoanChargePaidBy> loanChargesPaid) {
        this.loanChargesPaid = loanChargesPaid;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public boolean isRefund() {
        return LoanTransactionType.REFUND.equals(getTypeOf()) && isNotReversed();
    }

}