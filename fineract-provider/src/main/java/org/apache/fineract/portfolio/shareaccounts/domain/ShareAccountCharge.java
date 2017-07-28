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
package org.apache.fineract.portfolio.shareaccounts.domain;

import java.math.BigDecimal;
import java.math.MathContext;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;

@Entity
@Table(name = "m_share_account_charge")
public class ShareAccountCharge extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "account_id", referencedColumnName = "id", nullable = false)
    private ShareAccount shareAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "calculation_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal percentage;

    @Column(name = "calculation_on_amount", scale = 6, precision = 19, nullable = true)
    private BigDecimal amountPercentageAppliedTo;

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

    @Column(name = "is_paid_derived", nullable = false)
    private boolean paid = false;

    @Column(name = "waived", nullable = false)
    private boolean waived = false;

    @Column(name = "is_active", nullable = false)
    private boolean active = true;

    @Column(name = "charge_amount_or_percentage")
    private BigDecimal amountOrPercentage;

    public static ShareAccountCharge createNewWithoutShareAccount(final Charge chargeDefinition, final BigDecimal amountPayable,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final boolean status) {
        return new ShareAccountCharge(null, chargeDefinition, amountPayable, chargeTime, chargeCalculation, status);
    }

    protected ShareAccountCharge() {
        //
    }

    private ShareAccountCharge(final ShareAccount shareAccount, final Charge chargeDefinition, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final boolean status) {

        this.shareAccount = shareAccount;
        this.charge = chargeDefinition;
        this.chargeTime = (chargeTime == null) ? chargeDefinition.getChargeTimeType() : chargeTime.getValue();

        this.chargeCalculation = chargeDefinition.getChargeCalculation();
        if (chargeCalculation != null) {
            this.chargeCalculation = chargeCalculation.getValue();
        }

        BigDecimal chargeAmount = chargeDefinition.getAmount();
        if (amount != null) {
            chargeAmount = amount;
        }

        final BigDecimal transactionAmount = new BigDecimal(0);

        populateDerivedFields(transactionAmount, chargeAmount);
        this.paid = determineIfFullyPaid();
        this.active = status;
    }

    private void populateDerivedFields(final BigDecimal transactionAmount, final BigDecimal chargeAmount) {
        this.amountOrPercentage = chargeAmount ;
        if (this.chargeCalculation.equals(ChargeCalculationType.FLAT.getValue())) {
            this.percentage = null;
            this.amount = BigDecimal.ZERO;
            this.amountPercentageAppliedTo = null;
            this.amountPaid = null;
            this.amountOutstanding = BigDecimal.ZERO;
            this.amountWaived = null;
            this.amountWrittenOff = null;
        } else if (this.chargeCalculation.equals(ChargeCalculationType.PERCENT_OF_AMOUNT.getValue())) {
            this.percentage = chargeAmount;
            this.amountPercentageAppliedTo = transactionAmount;
            this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
            this.amountPaid = null;
            this.amountOutstanding = calculateOutstanding();
            this.amountWaived = null;
            this.amountWrittenOff = null;
        }
    }

    public void markAsFullyPaid() {
        this.amountPaid = this.amount;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = true;
    }

    public void resetToOriginal(final MonetaryCurrency currency) {
        this.amount = BigDecimal.ZERO ;
        this.amountPaid = BigDecimal.ZERO;
        this.amountWaived = BigDecimal.ZERO;
        this.amountWrittenOff = BigDecimal.ZERO;
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.waived = false;
    }

    public void undoPayment(final MonetaryCurrency currency, final Money transactionAmount) {
        Money amountPaid = getAmountPaid(currency);
        amountPaid = amountPaid.minus(transactionAmount);
        this.amountPaid = amountPaid.getAmount();
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.paid = false;
        this.active = true;
    }

    public Money waive(final MonetaryCurrency currency) {
        Money amountWaivedToDate = Money.of(currency, this.amountWaived);
        Money amountOutstanding = Money.of(currency, this.amountOutstanding);
        this.amountWaived = amountWaivedToDate.plus(amountOutstanding).getAmount();
        this.amountOutstanding = BigDecimal.ZERO;
        this.waived = true;
        return amountOutstanding;
    }

    public void undoWaiver(final MonetaryCurrency currency, final Money transactionAmount) {
        Money amountWaived = getAmountWaived(currency);
        amountWaived = amountWaived.minus(transactionAmount);
        this.amountWaived = amountWaived.getAmount();
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.waived = false;
        this.active = true;
    }

    public Money pay(final MonetaryCurrency currency, final Money amountPaid) {
        Money amountPaidToDate = Money.of(currency, this.amountPaid);
        Money amountOutstanding = Money.of(currency, this.amountOutstanding);
        amountPaidToDate = amountPaidToDate.plus(amountPaid);
        amountOutstanding = amountOutstanding.minus(amountPaid);
        this.amountPaid = amountPaidToDate.getAmount();
        this.amountOutstanding = amountOutstanding.getAmount();
        this.paid = determineIfFullyPaid();
        return Money.of(currency, this.amountOutstanding);
    }

    private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
        return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
    }

    public void update(final ShareAccount shareAccount) {
        this.shareAccount = shareAccount;
    }

    public void update(final BigDecimal transactionAmount, final BigDecimal amount) {
       populateDerivedFields(transactionAmount, amount);
    }

    private boolean isGreaterThanZero(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 1;
    }

    private boolean determineIfFullyPaid() {
        return BigDecimal.ZERO.compareTo(calculateOutstanding()) == 0;
    }

    private BigDecimal calculateOutstanding() {

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

    private BigDecimal percentageOf(final BigDecimal value, final BigDecimal percentage) {
        BigDecimal percentageOf = BigDecimal.ZERO;
        if (isGreaterThanZero(value)) {
            final MathContext mc = new MathContext(8, MoneyHelper.getRoundingMode());
            final BigDecimal multiplicand = percentage.divide(BigDecimal.valueOf(100l), mc);
            percentageOf = value.multiply(multiplicand, mc);
        }
        return percentageOf;
    }

    public BigDecimal percentageOrAmount() {
        return this.amountOrPercentage;
    }

    public BigDecimal amoutOutstanding() {
        return this.amountOutstanding;
    }

    public boolean isNotFullyPaid() {
        return !isPaid();
    }

    public boolean isPaid() {
        return this.paid;
    }

    public boolean isWaived() {
        return this.waived;
    }

    public boolean isPaidOrPartiallyPaid(final MonetaryCurrency currency) {

        final Money amountWaivedOrWrittenOff = getAmountWaived(currency).plus(getAmountWrittenOff(currency));
        return Money.of(currency, this.amountPaid).plus(amountWaivedOrWrittenOff).isGreaterThanZero();
    }

    public Money getAmount(final MonetaryCurrency currency) {
        return Money.of(currency, this.amount);
    }

    private Money getAmountPaid(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountPaid);
    }

    public Money getAmountWaived(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWaived);
    }

    public Money getAmountWrittenOff(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountWrittenOff);
    }

    public Money getAmountOutstanding(final MonetaryCurrency currency) {
        return Money.of(currency, this.amountOutstanding);
    }

    /**
     * @param incrementBy
     *            Amount used to pay off this charge
     * @return Actual amount paid on this charge
     */
    public Money updatePaidAmountBy(final Money incrementBy) {
        Money amountPaidToDate = Money.of(incrementBy.getCurrency(), this.amountPaid);
        Money amountPaidOnThisCharge = Money.zero(incrementBy.getCurrency());
        amountPaidOnThisCharge = incrementBy;
        amountPaidToDate = amountPaidToDate.plus(incrementBy);
        this.amountPaid = amountPaidToDate.getAmount();
        final Money amountExpected = Money.of(incrementBy.getCurrency(), this.amountPaid);
        this.amountOutstanding = amountExpected.minus(amountPaidToDate).getAmount();
        this.paid = determineIfFullyPaid();
        return amountPaidOnThisCharge;
    }

    public String name() {
        return this.charge.getName();
    }

    public String currencyCode() {
        return this.charge.getCurrencyCode();
    }

    public Charge getCharge() {
        return this.charge;
    }

    public ShareAccount shareAccount() {
        return this.shareAccount;
    }

    public boolean isShareAccountActivation() {
        return ChargeTimeType.fromInt(this.chargeTime).isShareAccountActivation();
    }

    public boolean isShareAccountClosure() {
        return ChargeTimeType.fromInt(this.chargeTime).isSavingsClosure();
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        if (this.currencyCode() == null || matchingCurrencyCode == null) { return false; }
        return this.currencyCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    public BigDecimal updateWithdralFeeAmount(final BigDecimal transactionAmount) {
        BigDecimal amountPaybale = BigDecimal.ZERO;
        if (ChargeCalculationType.fromInt(this.chargeCalculation).isFlat()) {
            amountPaybale = this.amount;
        } else if (ChargeCalculationType.fromInt(this.chargeCalculation).isPercentageOfAmount()) {
            amountPaybale = transactionAmount.multiply(this.percentage).divide(BigDecimal.valueOf(100l));
        }
        this.amountOutstanding = amountPaybale;
        return amountPaybale;
    }

    public boolean isActive() {
        return this.active;
    }

    public boolean isNotActive() {
        return !isActive();
    }

    public Long getChargeId() {
        return this.charge.getId();
    }

    public boolean isSharesPurchaseCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).isSharesPurchase();
    }

    public boolean isSharesRedeemCharge() {
        return ChargeTimeType.fromInt(this.chargeTime).isSharesRedeem();
    }

    public Integer getChargeTimeType() {
        return this.chargeTime;
    }

    public BigDecimal deriveChargeAmount(BigDecimal transactionAmount, final MonetaryCurrency currency) {
        BigDecimal toReturnAmount = amountOrPercentage;
        if (ChargeCalculationType.fromInt(this.chargeCalculation) == ChargeCalculationType.PERCENT_OF_AMOUNT) {
            toReturnAmount = Money.of(currency, percentageOf(transactionAmount, this.percentage)).getAmount() ;
            this.amountPercentageAppliedTo = transactionAmount;
            this.amount = Money.of(currency, percentageOf(this.amountPercentageAppliedTo, this.percentage)).getAmount() ;
            this.amountPaid = null;
            this.amountOutstanding = calculateOutstanding();
            this.amountWaived = null;
            this.amountWrittenOff = null;
        }else {
            this.amount = this.amountOrPercentage;
            this.amountOutstanding = calculateOutstanding();
            this.amountWaived = null;
            this.amountWrittenOff = null;
        }
        return toReturnAmount;
    }

    public BigDecimal updateChargeDetailsForAdditionalSharesRequest(final BigDecimal transactionAmount, final MonetaryCurrency currency) {
        BigDecimal toReturnAmount = amountOrPercentage;
        if (ChargeCalculationType.fromInt(this.chargeCalculation) == ChargeCalculationType.PERCENT_OF_AMOUNT) {
            toReturnAmount = Money.of(currency, percentageOf(transactionAmount, this.percentage)).getAmount() ;
            this.amountPercentageAppliedTo = this.amountPercentageAppliedTo.add(transactionAmount);
            this.amount = Money.of(currency, percentageOf(this.amountPercentageAppliedTo, this.percentage)).getAmount() ;
            this.amountOutstanding = calculateOutstanding();
            this.amountWaived = null;
            this.amountWrittenOff = null;
        } else {
            this.amount = this.amount.add(this.amountOrPercentage);
            this.amountOutstanding = calculateOutstanding();
            this.amountWaived = null;
            this.amountWrittenOff = null;
        }
        return toReturnAmount;
    }

    public void setActive(boolean active) {
        this.active = active ;
    }
    
    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final ShareAccountCharge rhs = (ShareAccountCharge) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.charge.getId(), rhs.charge.getId()) //
                .append(this.amount, rhs.amount) //
                .isEquals();
    }
}
