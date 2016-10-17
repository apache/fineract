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
package org.apache.fineract.portfolio.interestratechart.domain;

import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeFromParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.amountRangeToParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.annualInterestRateParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.descriptionParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.fromPeriodParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.periodTypeParamName;
import static org.apache.fineract.portfolio.interestratechart.InterestRateChartSlabApiConstants.toPeriodParamName;

import java.math.BigDecimal;
import java.util.Locale;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.portfolio.savings.SavingsPeriodFrequencyType;
import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;

@Embeddable
public class InterestRateChartSlabFields {

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "period_type_enum", nullable = true)
    private Integer periodType;

    @Column(name = "from_period")
    private Integer fromPeriod;

    @Column(name = "to_period")
    private Integer toPeriod;

    @Column(name = "amount_range_from", scale = 6, precision = 19)
    private BigDecimal amountRangeFrom;

    public BigDecimal getAmountRangeFrom() {
        return this.amountRangeFrom;
    }

    public BigDecimal getAmountRangeTo() {
        return this.amountRangeTo;
    }

    @Column(name = "amount_range_to", scale = 6, precision = 19)
    private BigDecimal amountRangeTo;

    @Column(name = "annual_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    protected InterestRateChartSlabFields() {
        //
    }

    public static InterestRateChartSlabFields createNew(final String description, final SavingsPeriodFrequencyType periodFrequencyType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final String currencyCode) {
        return new InterestRateChartSlabFields(description, periodFrequencyType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, currencyCode);
    }

    private InterestRateChartSlabFields(final String description, final SavingsPeriodFrequencyType periodFrequencyType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final String currencyCode) {
        this.description = description;
        this.periodType = (periodFrequencyType == null || periodFrequencyType.isInvalid()) ? null : periodFrequencyType.getValue();
        this.fromPeriod = fromPeriod;
        this.toPeriod = toPeriod;
        this.amountRangeFrom = amountRangeFrom;
        this.amountRangeTo = amountRangeTo;
        this.annualInterestRate = annualInterestRate;
        this.currencyCode = currencyCode;
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(periodTypeParamName, this.periodType, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(periodTypeParamName, locale);
            actualChanges.put(periodTypeParamName, newValue);
            this.periodType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(fromPeriodParamName, this.fromPeriod, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(fromPeriodParamName, locale);
            actualChanges.put(fromPeriodParamName, newValue);
            this.fromPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(toPeriodParamName, this.toPeriod, locale)) {
            final Integer newValue = command.integerValueOfParameterNamed(toPeriodParamName, locale);
            actualChanges.put(toPeriodParamName, newValue);
            this.toPeriod = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountRangeFromParamName, this.amountRangeFrom, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountRangeFromParamName, locale);
            actualChanges.put(amountRangeFromParamName, newValue);
            this.amountRangeFrom = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountRangeToParamName, this.amountRangeTo, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountRangeToParamName, locale);
            actualChanges.put(amountRangeToParamName, newValue);
            this.amountRangeTo = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(annualInterestRateParamName, this.annualInterestRate, locale)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(annualInterestRateParamName, locale);
            actualChanges.put(annualInterestRateParamName, newValue);
            this.annualInterestRate = newValue;
        }

        validateChartSlabPlatformRules(command, baseDataValidator, locale);
    }

    public void validateChartSlabPlatformRules(final JsonCommand chartSlabsCommand, final DataValidatorBuilder baseDataValidator,
            Locale locale) {
        if (isFromPeriodGreaterThanToPeriod()) {
            final Integer fromPeriod = chartSlabsCommand.integerValueOfParameterNamed(fromPeriodParamName, locale);
            baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("from.period.is.greater.than.to.period");
        }

        if (isAmountRangeFromGreaterThanTo()) {
            final BigDecimal amountRangeFrom = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeFromParamName, locale);
            baseDataValidator.parameter(amountRangeFromParamName).value(amountRangeFrom)
                    .failWithCode("amount.range.from.is.greater.than.amount.range.to");
        }
    }

    public boolean isFromPeriodGreaterThanToPeriod() {
        boolean isGreater = false;
        if (this.toPeriod != null && this.fromPeriod.compareTo(this.toPeriod) > 1) {
            isGreater = true;
        }
        return isGreater;
    }

    public boolean isAmountRangeFromGreaterThanTo() {
        boolean isGreater = false;
        if (this.amountRangeFrom != null && this.amountRangeTo != null && this.amountRangeFrom.compareTo(this.amountRangeTo) > 1) {
            isGreater = true;
        }
        return isGreater;
    }

    public Integer periodType() {
        return this.periodType;
    }

    public Integer fromPeriod() {
        return this.fromPeriod;
    }

    public Integer toPeriod() {
        return this.toPeriod;
    }

    public boolean isRateChartHasGap(final InterestRateChartSlabFields that, final boolean isPrimaryGroupingByAmount) {
        boolean isPeriodSame = isPeriodsSame(that);
        boolean isAmountSame = isAmountSame(that);
        boolean hasPeriods = this.fromPeriod != null || that.fromPeriod != null;
        boolean hasAmounts = this.amountRangeFrom != null || that.amountRangeFrom != null;
        if (isPrimaryGroupingByAmount) {
            if (isAmountSame) {
                if (hasPeriods) {
                    if (this.toPeriod == null) { return true; }
                    return isNotProperPeriodStart(that.fromPeriod);
                }
            } else {
                return isNotProperAmountStart(that.amountRangeFrom) || isNotProperPeriodStart(that);
            }
        } else {
            if (isPeriodSame) {
                if (hasAmounts) {
                    if (this.amountRangeTo == null) { return true; }
                    return isNotProperAmountStart(that.amountRangeFrom);
                }
            } else {
                return isNotProperPeriodStart(that.fromPeriod) || isNotProperAmountStart(that);
            }
        }
        return false;
    }

    public boolean isValidChart(boolean isPrimaryGroupingByAmount) {
        return (!isPrimaryGroupingByAmount && this.fromPeriod != null) || (isPrimaryGroupingByAmount && this.amountRangeFrom != null);
    }

    public boolean isNotProperChartStart() {
        return isNotProperPeriodStart(this) || isNotProperAmountStart(this);
    }

    public static boolean isNotProperAmountStart(final InterestRateChartSlabFields interestRateChartSlabFields) {
        return interestRateChartSlabFields.amountRangeFrom != null
                && (interestRateChartSlabFields.amountRangeFrom.compareTo(BigDecimal.ONE) != 0 && interestRateChartSlabFields.amountRangeFrom
                        .compareTo(BigDecimal.ZERO) != 0);
    }

    private boolean isNotProperAmountStart(final BigDecimal amount) {
        return this.amountRangeTo == null || (amount != null && amount.compareTo(this.amountRangeTo.add(BigDecimal.ONE)) != 0);
    }

    private boolean isNotProperPeriodStart(final Integer period) {
        return this.toPeriod == null || (period != null && period.compareTo(this.toPeriod + 1) != 0);
    }

    public static boolean isNotProperPeriodStart(InterestRateChartSlabFields interestRateChartSlabFields) {
        return interestRateChartSlabFields.fromPeriod != null
                && !(interestRateChartSlabFields.fromPeriod.equals(1) || interestRateChartSlabFields.fromPeriod.equals(0));
    }

    public boolean isNotProperPriodEnd() {
        return !(this.toPeriod == null && this.amountRangeTo == null);

    }

    public boolean isRateChartOverlapping(final InterestRateChartSlabFields that, final boolean isPrimaryGroupingByAmount) {
        boolean isPeriodOverLapping = isPeriodOverlapping(that);
        boolean isAmountOverLapping = isAmountOverlapping(that);
        boolean isPeriodSame = isPeriodsSame(that);
        boolean isAmountSame = isAmountSame(that);
        boolean isOverlapping = false;
        if (isPrimaryGroupingByAmount) {
            isOverlapping = (isAmountOverLapping && !isAmountSame) || (isPeriodOverLapping && isAmountSame);
        } else {
            isOverlapping = (isPeriodOverLapping && !isPeriodSame) || (isAmountOverLapping && isPeriodSame);
        }

        return isOverlapping;
    }

    private boolean isPeriodOverlapping(final InterestRateChartSlabFields that) {
        if (isIntegerSame(that.toPeriod, this.toPeriod)) {
            return true;
        } else if (isIntegerSame(that.fromPeriod, this.fromPeriod)) {
            return true;
        } else if (this.toPeriod == null) {
            return true;
        } else if (that.toPeriod == null) { return that.fromPeriod <= this.toPeriod; }
        return this.fromPeriod <= that.toPeriod && that.fromPeriod <= this.toPeriod;
    }

    private boolean isAmountOverlapping(final InterestRateChartSlabFields that) {
        if (isBigDecimalSame(that.amountRangeFrom, this.amountRangeFrom)) {
            return true;
        } else if (isBigDecimalSame(that.amountRangeTo, this.amountRangeTo)) {
            return true;
        } else if (this.amountRangeTo == null) {
            return true;
        } else if (that.amountRangeTo == null) { return that.amountRangeFrom.compareTo(this.amountRangeTo) < 1; }
        return this.amountRangeFrom.compareTo(that.amountRangeTo) < 1 && that.amountRangeFrom.compareTo(this.amountRangeTo) < 1;
    }

    public boolean isAmountSame(final InterestRateChartSlabFields that) {
        return isBigDecimalSame(this.amountRangeFrom, that.amountRangeFrom) && isBigDecimalSame(this.amountRangeTo, that.amountRangeTo);
    }

    public boolean isPeriodsSame(final InterestRateChartSlabFields that) {
        return isIntegerSame(this.fromPeriod, that.fromPeriod) && isIntegerSame(this.toPeriod, that.toPeriod);
    }

    public boolean isIntegerSame(final Integer obj1, final Integer obj2) {
        if (obj1 == null || obj2 == null) {
            if (obj1 == obj2) { return true; }
            return false;
        }
        return obj1.equals(obj2);
    }

    public boolean isBigDecimalSame(final BigDecimal obj1, final BigDecimal obj2) {
        if (obj1 == null || obj2 == null) {
            if (obj1 == obj2) { return true; }
            return false;
        }
        return obj1.compareTo(obj2) == 0;
    }

    public boolean isBetweenPeriod(final LocalDate periodStartDate, final LocalDate periodEndDate) {
        final Integer compare = depositPeriod(periodStartDate, periodEndDate);
        return isPeriodBetween(compare);
    }

    public boolean isAmountRangeProvided() {
        return (this.amountRangeFrom == null) ? false : true;
    }

    public BigDecimal annualInterestRate() {
        return this.annualInterestRate;
    }

    public Integer depositPeriod(final LocalDate periodStartDate, final LocalDate periodEndDate) {
        Integer actualDepositPeriod = 0;
        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(periodType());
        switch (periodFrequencyType) {
            case DAYS:
                actualDepositPeriod = Days.daysBetween(periodStartDate, periodEndDate).getDays();
            break;
            case WEEKS:
                actualDepositPeriod = Weeks.weeksBetween(periodStartDate, periodEndDate).getWeeks();
            break;
            case MONTHS:
                actualDepositPeriod = Months.monthsBetween(periodStartDate, periodEndDate).getMonths();
            break;
            case YEARS:
                actualDepositPeriod = Years.yearsBetween(periodStartDate, periodEndDate).getYears();
            break;
            case INVALID:
                actualDepositPeriod = 0;// default value
            break;
        }
        return actualDepositPeriod;
    }

    public boolean isAmountBetween(final BigDecimal depositAmount) {
        boolean returnValue = true;
        if (amountRangeFrom != null && amountRangeTo != null) {
            returnValue = depositAmount.compareTo(amountRangeFrom) >= 0 && depositAmount.compareTo(amountRangeTo) <= 0;
        } else if (amountRangeFrom != null) {
            returnValue = depositAmount.compareTo(amountRangeFrom) >= 0;
        }
        return returnValue;
    }

    public boolean isPeriodBetween(final Integer periods) {
        boolean returnValue = true;
        if (fromPeriod != null && toPeriod != null) {
            returnValue = periods.compareTo(fromPeriod) >= 0 && periods.compareTo(toPeriod) <= 0;
        } else if (fromPeriod != null) {
            returnValue = periods.compareTo(fromPeriod) >= 0;
        }
        return returnValue;
    }

}