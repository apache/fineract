/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanaccount.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.organisation.monetary.domain.MonetaryCurrency;
import org.mifosplatform.organisation.monetary.domain.Money;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_loan_installment_charge")
public class LoanInstallmentCharge extends AbstractPersistable<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_charge_id", referencedColumnName = "id", nullable = false)
    private LoanCharge loancharge;

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_schedule_id", referencedColumnName = "id", nullable = false)
    private LoanRepaymentScheduleInstallment installment;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "amount_paid_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPaid;

    @Column(name = "amount_waived_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWaived;

    @Column(name = "amount_writtenoff_derived", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountWrittenOff;

    @Column(name = "amount_outstanding_derived", scale = 6, precision = 19, nullable = false)
    private BigDecimal amountOutstanding;

    @Column(name = "amount_through_charge_payment", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountThroughChargePayment;

    @Column(name = "is_paid_derived", nullable = false)
    private boolean paid = false;

    @Column(name = "waived", nullable = false)
    private boolean waived = false;


    public LoanInstallmentCharge() {
        // TODO Auto-generated constructor stub
    }

    public LoanInstallmentCharge(final BigDecimal amount,final LoanCharge loanCharge,final LoanRepaymentScheduleInstallment installment) {
        this.loancharge = loanCharge;
        this.installment = installment;
        this.amount = amount;
        this.amountOutstanding = amount;
        this.amountPaid = null;
        this.amountWaived = null;
        this.amountWrittenOff = null;
    }

    public void copyFrom(final LoanInstallmentCharge loanChargePerInstallment){
        this.amount = loanChargePerInstallment.amount;
        this.installment = loanChargePerInstallment.installment;
        this.amountOutstanding = calculateOutstanding();
        this.paid = determineIfFullyPaid();
    }


    public Money waive(final MonetaryCurrency currency) {
        this.amountWaived = this.amountOutstanding;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = false;
        this.waived = true;
        return getAmountWaived(currency);
    }


    public Money getAmountWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWaived);
    }

    private boolean determineIfFullyPaid() {
        if(this.amount == null){
            return true;
        }
        return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
    }

    private BigDecimal calculateOutstanding() {
        if(this.amount == null){
            return null;
        }
        BigDecimal amountPaidLocal = BigDecimal.ZERO;
        if (this.amountPaid != null) {
            amountPaidLocal = this.amountPaid;
        }

        BigDecimal amountWaivedLocal = BigDecimal.ZERO;
        if (this.amountWaived != null) {
            amountWaivedLocal = this.amountWaived;
        }

        BigDecimal amountWrittenOffLocal = BigDecimal.ZERO;
        if (this.amountWrittenOff != null) {
            amountWrittenOffLocal = this.amountWrittenOff;
        }

        final BigDecimal totalAccountedFor = amountPaidLocal.add(amountWaivedLocal).add(amountWrittenOffLocal);

        return this.amount.subtract(totalAccountedFor);
    }


    public BigDecimal getAmount() {
        return this.amount;
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    private Money getAmountPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountPaid);
    }

    public BigDecimal getAmountOutstanding() {
        return this.amountOutstanding;
    }

    private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
        return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
    }


    public boolean isPaid() {
        return this.paid;
    }

    public boolean isWaived() {
        return this.waived;
    }

    public boolean isPending(){
        return !(isPaid() || isWaived());
    }


    public LoanRepaymentScheduleInstallment getRepaymentInstallment() {
        return this.installment;
    }

    public Money updatePaidAmountBy(final Money incrementBy, final Money feeAmount) {

        Money amountPaidToDate = Money.of(incrementBy.getCurrency(), this.amountPaid);
        final Money amountOutstanding = Money.of(incrementBy.getCurrency(), this.amountOutstanding);

        Money amountPaidOnThisCharge = Money.zero(incrementBy.getCurrency());
        if (incrementBy.isGreaterThanOrEqualTo(amountOutstanding)) {
            amountPaidOnThisCharge = amountOutstanding;
            amountPaidToDate = amountPaidToDate.plus(amountOutstanding);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = BigDecimal.ZERO;
        } else {
            amountPaidOnThisCharge = incrementBy;
            amountPaidToDate = amountPaidToDate.plus(incrementBy);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        this.amountThroughChargePayment = feeAmount.getAmount();
        this.paid = determineIfFullyPaid();

        return amountPaidOnThisCharge;
    }


    public Money getAmountWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWrittenOff);
    }

    public void resetPaidAmount(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
    }

    public void resetToOriginal(final MonetaryCurrency currency) {
        this.amountPaid = BigDecimal.ZERO;
        this.amountWaived = BigDecimal.ZERO;
        this.amountWrittenOff = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.waived = false;
    }


    public Money getAmountThroughChargePayment(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountThroughChargePayment);
    }

}