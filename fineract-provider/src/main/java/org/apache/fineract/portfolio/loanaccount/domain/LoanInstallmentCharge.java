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
package org.apache.fineract.portfolio.loanaccount.domain;

import java.math.BigDecimal;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;

@Entity
@Table(name = "m_loan_installment_charge")
public class LoanInstallmentCharge extends AbstractPersistableCustom<Long> implements Comparable<LoanInstallmentCharge> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "loan_charge_id", referencedColumnName = "id", nullable = false)
    private LoanCharge loancharge;

    @ManyToOne(optional = false, cascade = CascadeType.ALL)
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

    @Override
    public int compareTo(LoanInstallmentCharge o) {
        return this.installment.getInstallmentNumber().compareTo(o.installment.getInstallmentNumber());
    }
    
    public LoanInstallmentCharge(final BigDecimal amount, final LoanCharge loanCharge, final LoanRepaymentScheduleInstallment installment) {
        this.loancharge = loanCharge;
        this.installment = installment;
        this.amount = amount;
        this.amountOutstanding = amount;
        this.amountPaid = null;
        this.amountWaived = null;
        this.amountWrittenOff = null;
    }

    public void copyFrom(final LoanInstallmentCharge loanChargePerInstallment) {
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
        if (this.amount == null) { return true; }
        return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
    }

    private BigDecimal calculateOutstanding() {
        if (this.amount == null) { return null; }
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

    public Money getAmountPaid(final MonetaryCurrency currency) {
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

    public boolean isPending() {
        return !(isPaid() || isWaived());
    }

    public boolean isChargeAmountpaid(MonetaryCurrency currency) {
        Money amounPaidThroughChargePayment = Money.of(currency, this.amountThroughChargePayment);
        Money paid = Money.of(currency, this.amountPaid);
        return amounPaidThroughChargePayment.isEqualTo(paid);
    }

    public LoanRepaymentScheduleInstallment getRepaymentInstallment() {
        return this.installment;
    }

    public Money updatePaidAmountBy(final Money incrementBy, final Money feeAmount) {

        Money amountPaidToDate = Money.of(incrementBy.getCurrency(), this.amountPaid);
        final Money amountOutstanding = Money.of(incrementBy.getCurrency(), this.amountOutstanding);
        Money amountPaidPreviously = amountPaidToDate;
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
        Money amountFromChargePayment = Money.of(incrementBy.getCurrency(), this.amountThroughChargePayment);
        if (amountPaidPreviously.isGreaterThanZero()) {
            amountFromChargePayment = amountFromChargePayment.plus(feeAmount);
        } else {
            amountFromChargePayment = feeAmount;
        }
        this.amountThroughChargePayment = amountFromChargePayment.getAmount();
        if (determineIfFullyPaid()) {
            Money waivedAmount = getAmountWaived(incrementBy.getCurrency());
            if (waivedAmount.isGreaterThanZero()) {
                this.waived = true;
            } else {
                this.paid = true;
            }
        }

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
        this.amountThroughChargePayment = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.waived = false;

    }

    public Money getAmountThroughChargePayment(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountThroughChargePayment);
    }

    public Money getUnpaidAmountThroughChargePayment(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountThroughChargePayment).minus(this.amountPaid);
    }

    private void updateAmountThroughChargePayment(final MonetaryCurrency currency) {
        Money amountThroughChargePayment = getAmountThroughChargePayment(currency);
        if (amountThroughChargePayment.isGreaterThanZero() && amountThroughChargePayment.isGreaterThan(this.getAmount(currency))) {
            this.amountThroughChargePayment = this.getAmount();
        }
    }

    public Money updateWaivedAndAmountPaidThroughChargePaymentAmount(final MonetaryCurrency currency) {
        updateWaivedAmount(currency);
        updateAmountThroughChargePayment(currency);
        return getAmountWaived(currency);
    }

    private void updateWaivedAmount(final MonetaryCurrency currency) {
        Money waivedAmount = getAmountWaived(currency);
        if (waivedAmount.isGreaterThanZero()) {
            if (waivedAmount.isGreaterThan(this.getAmount(currency))) {
                this.amountWaived = this.getAmount();
                this.amountOutstanding = BigDecimal.ZERO;
                this.paid = false;
                this.waived = true;
            } else if (waivedAmount.isLessThan(this.getAmount(currency))) {
                this.paid = false;
                this.waived = false;
            }
        }
    }

    
    public void updateInstallment(LoanRepaymentScheduleInstallment installment) {
        this.installment = installment;
    }
    
    public Money undoPaidAmountBy(final Money incrementBy, final Money feeAmount) {

        Money amountPaidToDate = Money.of(incrementBy.getCurrency(), this.amountPaid);
       
        Money amountToDeductOnThisCharge = Money.zero(incrementBy.getCurrency());
        if (incrementBy.isGreaterThanOrEqualTo(amountPaidToDate)) {
                amountToDeductOnThisCharge = amountPaidToDate;
            amountPaidToDate = Money.zero(incrementBy.getCurrency());
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = this.amount;
        } else {
                amountToDeductOnThisCharge = incrementBy;
            amountPaidToDate = amountPaidToDate.minus(incrementBy);
            this.amountPaid = amountPaidToDate.getAmount();
            this.amountOutstanding = calculateAmountOutstanding(incrementBy.getCurrency());
        }
        this.amountThroughChargePayment = feeAmount.getAmount();
        this.paid = determineIfFullyPaid();

        return amountToDeductOnThisCharge;
    }

	public LoanCharge getLoancharge() {
		return this.loancharge;
	}
	public LoanRepaymentScheduleInstallment getInstallment() {
		return this.installment;
	}

    
}