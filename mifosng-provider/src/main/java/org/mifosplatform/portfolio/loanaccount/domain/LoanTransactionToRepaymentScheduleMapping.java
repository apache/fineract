/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_transaction_repayment_schedule_mapping")
public class LoanTransactionToRepaymentScheduleMapping extends AbstractPersistable<Long> {

    @ManyToOne(optional = false, cascade = CascadeType.PERSIST)
    @JoinColumn(name = "loan_repayment_schedule_id", nullable = false)
    private LoanRepaymentScheduleInstallment installment;

    @Column(name = "principal_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal principalPortion;

    @Column(name = "interest_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal interestPortion;

    @Column(name = "fee_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal feeChargesPortion;

    @Column(name = "penalty_charges_portion_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal penaltyChargesPortion;

    @Column(name = "amount", scale = 6, precision = 19)
    private BigDecimal amount;

    protected LoanTransactionToRepaymentScheduleMapping() {

    }

    private LoanTransactionToRepaymentScheduleMapping(final LoanRepaymentScheduleInstallment installment,
            final BigDecimal principalPortion, final BigDecimal interestPortion, final BigDecimal feeChargesPortion,
            final BigDecimal penaltyChargesPortion, final BigDecimal amount) {
        this.installment = installment;
        this.principalPortion = principalPortion;
        this.interestPortion = interestPortion;
        this.feeChargesPortion = feeChargesPortion;
        this.penaltyChargesPortion = penaltyChargesPortion;
        this.amount = amount;
    }

    public static LoanTransactionToRepaymentScheduleMapping createFrom(final LoanRepaymentScheduleInstallment installment,
            final Money principalPortion, final Money interestPortion, final Money feeChargesPortion, final Money penaltyChargesPortion) {
        return new LoanTransactionToRepaymentScheduleMapping(installment, defaultToNullIfZero(principalPortion),
                defaultToNullIfZero(interestPortion), defaultToNullIfZero(feeChargesPortion), defaultToNullIfZero(penaltyChargesPortion),
                defaultToNullIfZero(principalPortion.plus(interestPortion).plus(feeChargesPortion).plus(penaltyChargesPortion)));
    }

    private static BigDecimal defaultToNullIfZero(final Money value) {
        BigDecimal result = value.getAmount();
        if (value.isZero()) {
            result = null;
        }
        return result;
    }

    private BigDecimal defaultToZeroIfNull(final BigDecimal value) {
        BigDecimal result = value;
        if (value == null) {
            result = BigDecimal.ZERO;
        }
        return result;
    }

    public LoanRepaymentScheduleInstallment getLoanRepaymentScheduleInstallment() {
        return this.installment;
    }

    public void updateComponents(final Money principal, final Money interest, final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = principal.getCurrency();
        this.principalPortion = defaultToNullIfZero(getPrincipalPortion(currency).plus(principal));
        this.interestPortion = defaultToNullIfZero(getInterestPortion(currency).plus(interest));
        updateChargesComponents(feeCharges, penaltyCharges);
        updateAmount();
    }

    private void updateAmount() {
        this.amount = defaultToZeroIfNull(getPrincipalPortion()).add(defaultToZeroIfNull(getInterestPortion()))
                .add(defaultToZeroIfNull(getFeeChargesPortion())).add(defaultToZeroIfNull(getPenaltyChargesPortion()));
    }

    public void setComponents(final BigDecimal principal, final BigDecimal interest, final BigDecimal feeCharges,
            final BigDecimal penaltyCharges) {
        this.principalPortion = principal;
        this.interestPortion = interest;
        this.feeChargesPortion = feeCharges;
        this.penaltyChargesPortion = penaltyCharges;
        updateAmount();
    }

    private void updateChargesComponents(final Money feeCharges, final Money penaltyCharges) {
        final MonetaryCurrency currency = feeCharges.getCurrency();
        this.feeChargesPortion = defaultToNullIfZero(getFeeChargesPortion(currency).plus(feeCharges));
        this.penaltyChargesPortion = defaultToNullIfZero(getPenaltyChargesPortion(currency).plus(penaltyCharges));
    }

    public Money getPrincipalPortion(final MonetaryCurrency currency) {
        return Money.of(currency, this.principalPortion);
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

    public BigDecimal getPrincipalPortion() {
        return this.principalPortion;
    }

    public BigDecimal getInterestPortion() {
        return this.interestPortion;
    }

    public BigDecimal getFeeChargesPortion() {
        return this.feeChargesPortion;
    }

    public BigDecimal getPenaltyChargesPortion() {
        return this.penaltyChargesPortion;
    }
}
