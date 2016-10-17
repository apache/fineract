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

import static org.apache.fineract.portfolio.savings.SavingsApiConstants.amountParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dateFormatParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.dueAsOfDateParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeIntervalParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.feeOnMonthDayParamName;
import static org.apache.fineract.portfolio.savings.SavingsApiConstants.localeParamName;

import java.math.BigDecimal;
import java.math.MathContext;
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

import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.organisation.monetary.domain.Money;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.charge.exception.SavingsAccountChargeWithoutMandatoryFieldException;
import org.joda.time.LocalDate;
import org.joda.time.MonthDay;

/**
 * @author dv6
 * 
 */
@Entity
@Table(name = "m_savings_account_charge")
public class SavingsAccountCharge extends AbstractPersistableCustom<Long> {

    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_id", referencedColumnName = "id", nullable = false)
    private SavingsAccount savingsAccount;

    @ManyToOne(optional = false)
    @JoinColumn(name = "charge_id", referencedColumnName = "id", nullable = false)
    private Charge charge;

    @Column(name = "charge_time_enum", nullable = false)
    private Integer chargeTime;

    @Temporal(TemporalType.DATE)
    @Column(name = "charge_due_date")
    private Date dueDate;

    @Column(name = "fee_on_month", nullable = true)
    private Integer feeOnMonth;

    @Column(name = "fee_on_day", nullable = true)
    private Integer feeOnDay;

    @Column(name = "fee_interval", nullable = true)
    private Integer feeInterval;

    @Column(name = "charge_calculation_enum")
    private Integer chargeCalculation;

    @Column(name = "calculation_percentage", scale = 6, precision = 19, nullable = true)
    private BigDecimal percentage;

    // TODO AA: This field may not require for savings charges
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

    @Column(name = "is_penalty", nullable = false)
    private boolean penaltyCharge = false;

    @Column(name = "is_paid_derived", nullable = false)
    private boolean paid = false;

    @Column(name = "waived", nullable = false)
    private boolean waived = false;

    @Column(name = "is_active", nullable = false)
    private boolean status = true;

    @Temporal(TemporalType.DATE)
    @Column(name = "inactivated_on_date")
    private Date inactivationDate;

    public static SavingsAccountCharge createNewFromJson(final SavingsAccount savingsAccount, final Charge chargeDefinition,
            final JsonCommand command) {

        BigDecimal amount = command.bigDecimalValueOfParameterNamed(amountParamName);
        final LocalDate dueDate = command.localDateValueOfParameterNamed(dueAsOfDateParamName);
        MonthDay feeOnMonthDay = command.extractMonthDayNamed(feeOnMonthDayParamName);
        Integer feeInterval = command.integerValueOfParameterNamed(feeIntervalParamName);
        final ChargeTimeType chargeTime = null;
        final ChargeCalculationType chargeCalculation = null;
        final boolean status = true;

        // If these values is not sent as parameter, then derive from Charge
        // definition
        amount = (amount == null) ? chargeDefinition.getAmount() : amount;
        feeOnMonthDay = (feeOnMonthDay == null) ? chargeDefinition.getFeeOnMonthDay() : feeOnMonthDay;
        feeInterval = (feeInterval == null) ? chargeDefinition.getFeeInterval() : feeInterval;

        return new SavingsAccountCharge(savingsAccount, chargeDefinition, amount, chargeTime, chargeCalculation, dueDate, status,
                feeOnMonthDay, feeInterval);
    }

    public static SavingsAccountCharge createNewWithoutSavingsAccount(final Charge chargeDefinition, final BigDecimal amountPayable,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate, final boolean status,
            final MonthDay feeOnMonthDay, final Integer feeInterval) {
        return new SavingsAccountCharge(null, chargeDefinition, amountPayable, chargeTime, chargeCalculation, dueDate, status,
                feeOnMonthDay, feeInterval);
    }

    protected SavingsAccountCharge() {
        //
    }

    private SavingsAccountCharge(final SavingsAccount savingsAccount, final Charge chargeDefinition, final BigDecimal amount,
            final ChargeTimeType chargeTime, final ChargeCalculationType chargeCalculation, final LocalDate dueDate, final boolean status,
            MonthDay feeOnMonthDay, final Integer feeInterval) {

        this.savingsAccount = savingsAccount;
        this.charge = chargeDefinition;
        this.penaltyCharge = chargeDefinition.isPenalty();
        this.chargeTime = (chargeTime == null) ? chargeDefinition.getChargeTimeType() : chargeTime.getValue();

        if (isOnSpecifiedDueDate()) {
            if (dueDate == null) {
                final String defaultUserMessage = "Savings Account charge is missing due date.";
                throw new SavingsAccountChargeWithoutMandatoryFieldException("savingsaccount.charge", dueAsOfDateParamName,
                        defaultUserMessage, chargeDefinition.getId(), chargeDefinition.getName());
            }

        }

        if (isAnnualFee() || isMonthlyFee()) {
            feeOnMonthDay = (feeOnMonthDay == null) ? chargeDefinition.getFeeOnMonthDay() : feeOnMonthDay;
            if (feeOnMonthDay == null) {
                final String defaultUserMessage = "Savings Account charge is missing due date.";
                throw new SavingsAccountChargeWithoutMandatoryFieldException("savingsaccount.charge", dueAsOfDateParamName,
                        defaultUserMessage, chargeDefinition.getId(), chargeDefinition.getName());
            }

            this.feeOnMonth = feeOnMonthDay.getMonthOfYear();
            this.feeOnDay = feeOnMonthDay.getDayOfMonth();

        } else if (isWeeklyFee()) {
            if (dueDate == null) {
                final String defaultUserMessage = "Savings Account charge is missing due date.";
                throw new SavingsAccountChargeWithoutMandatoryFieldException("savingsaccount.charge", dueAsOfDateParamName,
                        defaultUserMessage, chargeDefinition.getId(), chargeDefinition.getName());
            }
            /**
             * For Weekly fee feeOnDay is ISO standard day of the week.
             * Monday=1, Tuesday=2
             */
            this.feeOnDay = dueDate.getDayOfWeek();
        } else {
            this.feeOnDay = null;
            this.feeOnMonth = null;
            this.feeInterval = null;
        }

        if (isMonthlyFee() || isWeeklyFee()) {
            this.feeInterval = (feeInterval == null) ? chargeDefinition.feeInterval() : feeInterval;
        }

        this.dueDate = (dueDate == null) ? null : dueDate.toDate();

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

        if (this.isWithdrawalFee()
        		|| this.isSavingsNoActivity()) {
            this.amountOutstanding = BigDecimal.ZERO;
        }

        this.paid = determineIfFullyPaid();
        this.status = status;
    }

    public void resetPropertiesForRecurringFees() {
        if (isMonthlyFee() || isAnnualFee() || isWeeklyFee()) {
            // FIXME: AA: If charge is percentage of x amount then need to
            // update amount outstanding accordingly.
            // Right now annual and monthly charges supports charge calculation
            // type flat.
            this.amountOutstanding = this.amount;
            this.paid = false;// reset to false for recurring fee.
            this.waived = false;
        }
    }

    private void populateDerivedFields(final BigDecimal transactionAmount, final BigDecimal chargeAmount) {

        switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
            case INVALID:
                this.percentage = null;
                this.amount = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = BigDecimal.ZERO;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case FLAT:
                this.percentage = null;
                this.amount = chargeAmount;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = chargeAmount;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case PERCENT_OF_AMOUNT:
                this.percentage = chargeAmount;
                this.amountPercentageAppliedTo = transactionAmount;
                this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
                this.amountPaid = null;
                this.amountOutstanding = calculateOutstanding();
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case PERCENT_OF_AMOUNT_AND_INTEREST:
                this.percentage = null;
                this.amount = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = BigDecimal.ZERO;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
            case PERCENT_OF_INTEREST:
                this.percentage = null;
                this.amount = null;
                this.amountPercentageAppliedTo = null;
                this.amountPaid = null;
                this.amountOutstanding = BigDecimal.ZERO;
                this.amountWaived = null;
                this.amountWrittenOff = null;
            break;
        }
    }

    public void markAsFullyPaid() {
        this.amountPaid = this.amount;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = true;
    }

    public void resetToOriginal(final MonetaryCurrency currency) {
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

        if (this.isWithdrawalFee()) {
            this.amountOutstanding = BigDecimal.ZERO;
        }
        // to reset amount outstanding for annual and monthly fee
        resetPropertiesForRecurringFees();
        updateToPreviousDueDate();// reset annual and monthly due date.
        this.paid = false;
        this.status = true;
    }

    public Money waive(final MonetaryCurrency currency) {
        Money amountWaivedToDate = Money.of(currency, this.amountWaived);
        Money amountOutstanding = Money.of(currency, this.amountOutstanding);
        this.amountWaived = amountWaivedToDate.plus(amountOutstanding).getAmount();
        this.amountOutstanding = BigDecimal.ZERO;
        this.waived = true;

        resetPropertiesForRecurringFees();
        updateNextDueDateForRecurringFees();

        return amountOutstanding;
    }

    public void undoWaiver(final MonetaryCurrency currency, final Money transactionAmount) {
        Money amountWaived = getAmountWaived(currency);
        amountWaived = amountWaived.minus(transactionAmount);
        this.amountWaived = amountWaived.getAmount();
        this.amountOutstanding = calculateAmountOutstanding(currency);
        this.waived = false;
        this.status = true;

        resetPropertiesForRecurringFees();
        updateToPreviousDueDate();
    }

    public Money pay(final MonetaryCurrency currency, final Money amountPaid) {
        Money amountPaidToDate = Money.of(currency, this.amountPaid);
        Money amountOutstanding = Money.of(currency, this.amountOutstanding);
        amountPaidToDate = amountPaidToDate.plus(amountPaid);
        amountOutstanding = amountOutstanding.minus(amountPaid);
        this.amountPaid = amountPaidToDate.getAmount();
        this.amountOutstanding = amountOutstanding.getAmount();
        this.paid = determineIfFullyPaid();

        if (BigDecimal.ZERO.compareTo(this.amountOutstanding) == 0) {
            // full outstanding is paid, update to next due date
            updateNextDueDateForRecurringFees();
            resetPropertiesForRecurringFees();
        }

        return Money.of(currency, this.amountOutstanding);
    }

    private BigDecimal calculateAmountOutstanding(final MonetaryCurrency currency) {
        return getAmount(currency).minus(getAmountWaived(currency)).minus(getAmountPaid(currency)).getAmount();
    }

    public void update(final SavingsAccount savingsAccount) {
        this.savingsAccount = savingsAccount;
    }

    public void update(final BigDecimal amount, final LocalDate dueDate, final MonthDay feeOnMonthDay, final Integer feeInterval) {
        final BigDecimal transactionAmount = BigDecimal.ZERO;
        if (dueDate != null) {
            this.dueDate = dueDate.toDate();
            if (isWeeklyFee()) {
                this.feeOnDay = dueDate.getDayOfWeek();
            }
        }

        if (feeOnMonthDay != null) {
            this.feeOnMonth = feeOnMonthDay.getMonthOfYear();
            this.feeOnDay = feeOnMonthDay.getDayOfMonth();
        }

        if (feeInterval != null) {
            this.feeInterval = feeInterval;
        }

        if (amount != null) {
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    this.amount = amount;
                break;
                case PERCENT_OF_AMOUNT:
                    this.percentage = amount;
                    this.amountPercentageAppliedTo = transactionAmount;
                    this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
                    this.amountOutstanding = calculateOutstanding();
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    this.percentage = amount;
                    this.amount = null;
                    this.amountPercentageAppliedTo = null;
                    this.amountOutstanding = null;
                break;
                case PERCENT_OF_INTEREST:
                    this.percentage = amount;
                    this.amount = null;
                    this.amountPercentageAppliedTo = null;
                    this.amountOutstanding = null;
                break;
            }
        }
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        if (command.isChangeInLocalDateParameterNamed(dueAsOfDateParamName, getDueLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(dueAsOfDateParamName);
            actualChanges.put(dueAsOfDateParamName, valueAsInput);
            actualChanges.put(dateFormatParamName, dateFormatAsInput);
            actualChanges.put(localeParamName, localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(dueAsOfDateParamName);
            this.dueDate = newValue.toDate();
            if (this.isWeeklyFee()) {
                this.feeOnDay = newValue.getDayOfWeek();
            }
        }

        if (command.hasParameter(feeOnMonthDayParamName)) {
            final MonthDay monthDay = command.extractMonthDayNamed(feeOnMonthDayParamName);
            final String actualValueEntered = command.stringValueOfParameterNamed(feeOnMonthDayParamName);
            final Integer dayOfMonthValue = monthDay.getDayOfMonth();
            if (this.feeOnDay != dayOfMonthValue) {
                actualChanges.put(feeOnMonthDayParamName, actualValueEntered);
                actualChanges.put(localeParamName, localeAsInput);
                this.feeOnDay = dayOfMonthValue;
            }

            final Integer monthOfYear = monthDay.getMonthOfYear();
            if (this.feeOnMonth != monthOfYear) {
                actualChanges.put(feeOnMonthDayParamName, actualValueEntered);
                actualChanges.put(localeParamName, localeAsInput);
                this.feeOnMonth = monthOfYear;
            }
        }

        if (command.isChangeInBigDecimalParameterNamed(amountParamName, this.amount)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountParamName);
            actualChanges.put(amountParamName, newValue);
            actualChanges.put(localeParamName, localeAsInput);
            switch (ChargeCalculationType.fromInt(this.chargeCalculation)) {
                case INVALID:
                break;
                case FLAT:
                    this.amount = newValue;
                    this.amountOutstanding = calculateOutstanding();
                break;
                case PERCENT_OF_AMOUNT:
                    this.percentage = newValue;
                    this.amountPercentageAppliedTo = null;
                    this.amount = percentageOf(this.amountPercentageAppliedTo, this.percentage);
                    this.amountOutstanding = calculateOutstanding();
                break;
                case PERCENT_OF_AMOUNT_AND_INTEREST:
                    this.percentage = newValue;
                    this.amount = null;
                    this.amountPercentageAppliedTo = null;
                    this.amountOutstanding = null;
                break;
                case PERCENT_OF_INTEREST:
                    this.percentage = newValue;
                    this.amount = null;
                    this.amountPercentageAppliedTo = null;
                    this.amountOutstanding = null;
                break;
            }
        }

        return actualChanges;
    }

    private boolean isGreaterThanZero(final BigDecimal value) {
        return value.compareTo(BigDecimal.ZERO) == 1;
    }

    public LocalDate getDueLocalDate() {
        LocalDate dueDate = null;
        if (this.dueDate != null) {
            dueDate = new LocalDate(this.dueDate);
        }
        return dueDate;
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

    public BigDecimal amount() {
        return this.amount;
    }

    public BigDecimal amoutOutstanding() {
        return this.amountOutstanding;
    }

    public boolean isFeeCharge() {
        return !this.penaltyCharge;
    }

    public boolean isPenaltyCharge() {
        return this.penaltyCharge;
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

            final Money amountExpected = Money.of(incrementBy.getCurrency(), this.amount);
            this.amountOutstanding = amountExpected.minus(amountPaidToDate).getAmount();
        }

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

    public SavingsAccount savingsAccount() {
        return this.savingsAccount;
    }

    public boolean isOnSpecifiedDueDate() {
        return ChargeTimeType.fromInt(this.chargeTime).isOnSpecifiedDueDate();
    }

    public boolean isSavingsActivation() {
        return ChargeTimeType.fromInt(this.chargeTime).isSavingsActivation();
    }
    
    public boolean isSavingsNoActivity(){
    	return ChargeTimeType.fromInt(this.chargeTime).isSavingsNoActivityFee();
    }

    public boolean isSavingsClosure() {
        return ChargeTimeType.fromInt(this.chargeTime).isSavingsClosure();
    }

    public boolean isWithdrawalFee() {
        return ChargeTimeType.fromInt(this.chargeTime).isWithdrawalFee();
    }

    public boolean isOverdraftFee() {
        return ChargeTimeType.fromInt(this.chargeTime).isOverdraftFee();
    }

    public boolean isAnnualFee() {
        return ChargeTimeType.fromInt(this.chargeTime).isAnnualFee();
    }

    public boolean isMonthlyFee() {
        return ChargeTimeType.fromInt(this.chargeTime).isMonthlyFee();
    }

    public boolean isWeeklyFee() {
        return ChargeTimeType.fromInt(this.chargeTime).isWeeklyFee();
    }

    public boolean hasCurrencyCodeOf(final String matchingCurrencyCode) {
        if (this.currencyCode() == null || matchingCurrencyCode == null) { return false; }
        return this.currencyCode().equalsIgnoreCase(matchingCurrencyCode);
    }

    @Override
    public boolean equals(final Object obj) {
        if (obj == null) { return false; }
        if (obj == this) { return true; }
        if (obj.getClass() != getClass()) { return false; }
        final SavingsAccountCharge rhs = (SavingsAccountCharge) obj;
        return new EqualsBuilder().appendSuper(super.equals(obj)) //
                .append(getId(), rhs.getId()) //
                .append(this.charge.getId(), rhs.charge.getId()) //
                .append(this.amount, rhs.amount) //
                .append(getDueLocalDate(), rhs.getDueLocalDate()) //
                .isEquals();
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

    public void updateToNextDueDateFrom(final LocalDate startingDate) {
        if (isAnnualFee() || isMonthlyFee() || isWeeklyFee()) {
            this.dueDate = getNextDueDateFrom(startingDate).toDate();
        }
    }

    public LocalDate getNextDueDateFrom(final LocalDate startingDate) {
        LocalDate nextDueLocalDate = null;
        if (isAnnualFee() || isMonthlyFee()) {
            nextDueLocalDate = startingDate.withMonthOfYear(this.feeOnMonth);
            nextDueLocalDate = setDayOfMonth(nextDueLocalDate);
            while (startingDate.isAfter(nextDueLocalDate)) {
                nextDueLocalDate = calculateNextDueDate(nextDueLocalDate);
            }
        } else if (isWeeklyFee()) {
            nextDueLocalDate = getDueLocalDate();
            while (startingDate.isAfter(nextDueLocalDate)) {
                nextDueLocalDate = calculateNextDueDate(nextDueLocalDate);
            }
        } else {
            nextDueLocalDate = calculateNextDueDate(startingDate);
        }
        return nextDueLocalDate;
    }

    private LocalDate calculateNextDueDate(final LocalDate date) {
        LocalDate nextDueLocalDate = null;
        if (isAnnualFee()) {
            nextDueLocalDate = date.withMonthOfYear(this.feeOnMonth).plusYears(1);
            nextDueLocalDate = setDayOfMonth(nextDueLocalDate);
        } else if (isMonthlyFee()) {
            nextDueLocalDate = date.plusMonths(this.feeInterval);
            nextDueLocalDate = setDayOfMonth(nextDueLocalDate);
        } else if (isWeeklyFee()) {
            nextDueLocalDate = date.plusWeeks(this.feeInterval);
            nextDueLocalDate = setDayOfWeek(nextDueLocalDate);
        }
        return nextDueLocalDate;
    }

    private LocalDate setDayOfMonth(LocalDate nextDueLocalDate) {
        int maxDayOfMonth = nextDueLocalDate.dayOfMonth().withMaximumValue().getDayOfMonth();
        int newDayOfMonth = (this.feeOnDay.intValue() < maxDayOfMonth) ? this.feeOnDay.intValue() : maxDayOfMonth;
        nextDueLocalDate = nextDueLocalDate.withDayOfMonth(newDayOfMonth);
        return nextDueLocalDate;
    }

    private LocalDate setDayOfWeek(LocalDate nextDueLocalDate) {
        if (this.feeOnDay != nextDueLocalDate.getDayOfWeek()) {
            nextDueLocalDate = nextDueLocalDate.withDayOfWeek(this.feeOnDay);
        }
        return nextDueLocalDate;
    }

    public void updateNextDueDateForRecurringFees() {
        if (isAnnualFee() || isMonthlyFee() || isWeeklyFee()) {
            LocalDate nextDueLocalDate = new LocalDate(dueDate);
            nextDueLocalDate = calculateNextDueDate(nextDueLocalDate);
            this.dueDate = nextDueLocalDate.toDate();
        }
    }

    public void updateToPreviousDueDate() {
        if (isAnnualFee() || isMonthlyFee() || isWeeklyFee()) {
            LocalDate nextDueLocalDate = new LocalDate(dueDate);
            if (isAnnualFee()) {
                nextDueLocalDate = nextDueLocalDate.withMonthOfYear(this.feeOnMonth).minusYears(1);
                nextDueLocalDate = setDayOfMonth(nextDueLocalDate);
            } else if (isMonthlyFee()) {
                nextDueLocalDate = nextDueLocalDate.minusMonths(this.feeInterval);
                nextDueLocalDate = setDayOfMonth(nextDueLocalDate);
            } else if (isWeeklyFee()) {
                nextDueLocalDate = nextDueLocalDate.minusDays(7 * this.feeInterval);
                nextDueLocalDate = setDayOfWeek(nextDueLocalDate);
            }

            this.dueDate = nextDueLocalDate.toDate();
        }
    }

    public boolean feeSettingsNotSet() {
        return !feeSettingsSet();
    }

    public boolean feeSettingsSet() {
        return this.feeOnDay != null && this.feeOnMonth != null;
    }

    public boolean isRecurringFee() {
        return isWeeklyFee() || isMonthlyFee() || isAnnualFee();
    }

    public boolean isChargeIsDue(final LocalDate nextDueDate) {
        return this.getDueLocalDate().isBefore(nextDueDate);
    }

    public boolean isChargeIsOverPaid(final LocalDate nextDueDate) {
        final BigDecimal amountPaid = this.amountPaid == null ? BigDecimal.ZERO : amountPaid();
        return this.getDueLocalDate().isAfter(nextDueDate) && amountPaid.compareTo(BigDecimal.ZERO) == 1;
    }

    private BigDecimal amountPaid() {
        return this.amountPaid;
    }

    public void inactiavateCharge(final LocalDate inactivationOnDate) {
        this.inactivationDate = inactivationOnDate.toDate();
        this.status = false;
        this.amountOutstanding = BigDecimal.ZERO;
        this.paid = true;
    }

    public boolean isActive() {
        return this.status;
    }

    public boolean isNotActive() {
        return !isActive();
    }

    /**
     * This method is to identify the charges which can override the savings
     * rules(for example if there is a minimum enforced balance of 1000 on
     * savings account with account balance of 1000, still these charges can be
     * collected as these charges are initiated by system and it can bring down
     * the balance below the enforced minimum balance).
     * 
     */
    public boolean canOverriteSavingAccountRules() {
        return !(this.isSavingsActivation() || this.isWithdrawalFee());
    }
}