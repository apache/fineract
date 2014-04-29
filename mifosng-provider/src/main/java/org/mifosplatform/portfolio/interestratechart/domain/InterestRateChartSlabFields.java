/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.amountRangeFromParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.amountRangeToParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.annualInterestRateParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.descriptionParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.fromPeriodParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.periodTypeParamName;
import static org.mifosplatform.portfolio.interestratechart.InterestRateChartApiConstants.toPeriodParamName;

import java.math.BigDecimal;
import java.util.Map;

import javax.persistence.Column;
import javax.persistence.Embeddable;

import org.joda.time.Days;
import org.joda.time.LocalDate;
import org.joda.time.Months;
import org.joda.time.Weeks;
import org.joda.time.Years;
import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.mifosplatform.portfolio.savings.SavingsPeriodFrequencyType;

@Embeddable
public class InterestRateChartSlabFields {

    @Column(name = "description", nullable = true)
    private String description;

    @Column(name = "period_type_enum", nullable = false)
    private Integer periodType;

    @Column(name = "from_period", nullable = false)
    private Integer fromPeriod;

    @Column(name = "to_period", nullable = true)
    private Integer toPeriod;

    @Column(name = "amount_range_from", scale = 6, precision = 19)
    private BigDecimal amountRangeFrom;

    @Column(name = "amount_range_to", scale = 6, precision = 19)
    private BigDecimal amountRangeTo;

    @Column(name = "annual_interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal annualInterestRate;

    @Column(name = "currency_code", nullable = false)
    private String currencyCode;

    protected InterestRateChartSlabFields() {
        //
    }

    public static InterestRateChartSlabFields createNew(String description, final SavingsPeriodFrequencyType periodFrequencyType, Integer fromPeriod, Integer toPeriod,
            BigDecimal amountRangeFrom, BigDecimal amountRangeTo, BigDecimal annualInterestRate, String currencyCode) {
        return new InterestRateChartSlabFields(description, periodFrequencyType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, currencyCode);
    }

    private InterestRateChartSlabFields(String description, final SavingsPeriodFrequencyType periodFrequencyType, Integer fromPeriod, Integer toPeriod,
            BigDecimal amountRangeFrom, BigDecimal amountRangeTo, BigDecimal annualInterestRate, String currencyCode) {
        this.description = description;
        this.periodType = (periodFrequencyType == null) ? null : periodFrequencyType.getValue();
        this.fromPeriod = fromPeriod;
        this.toPeriod = toPeriod;
        this.amountRangeFrom = amountRangeFrom;
        this.amountRangeTo = amountRangeTo;
        this.annualInterestRate = annualInterestRate;
        this.currencyCode = currencyCode;
    }

    public void update(JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator) {

        if (command.isChangeInStringParameterNamed(descriptionParamName, this.description)) {
            final String newValue = command.stringValueOfParameterNamed(descriptionParamName);
            actualChanges.put(descriptionParamName, newValue);
            this.description = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(periodTypeParamName, periodType)) {
            final Integer newValue = command.integerValueOfParameterNamed(periodTypeParamName);
            actualChanges.put(periodTypeParamName, newValue);
            this.periodType = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(fromPeriodParamName, fromPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(fromPeriodParamName);
            actualChanges.put(fromPeriodParamName, newValue);
            this.fromPeriod = newValue;
        }

        if (command.isChangeInIntegerParameterNamed(toPeriodParamName, toPeriod)) {
            final Integer newValue = command.integerValueOfParameterNamed(toPeriodParamName);
            actualChanges.put(toPeriodParamName, newValue);
            this.toPeriod = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountRangeFromParamName, amountRangeFrom)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountRangeFromParamName);
            actualChanges.put(amountRangeFromParamName, newValue);
            this.amountRangeFrom = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(amountRangeToParamName, amountRangeTo)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(amountRangeToParamName);
            actualChanges.put(amountRangeToParamName, newValue);
            this.amountRangeTo = newValue;
        }

        if (command.isChangeInBigDecimalParameterNamed(annualInterestRateParamName, annualInterestRate)) {
            final BigDecimal newValue = command.bigDecimalValueOfParameterNamed(annualInterestRateParamName);
            actualChanges.put(annualInterestRateParamName, newValue);
            this.annualInterestRate = newValue;
        }
        
        this.validateChartSlabPlatformRules(command, baseDataValidator);
    }
    
    public void validateChartSlabPlatformRules(JsonCommand chartSlabsCommand, DataValidatorBuilder baseDataValidator) {
        if (this.isFromPeriodGreaterThanToPeriod()) {
            final Integer fromPeriod = chartSlabsCommand.integerValueOfParameterNamed(fromPeriodParamName);
            baseDataValidator.parameter(fromPeriodParamName).value(fromPeriod).failWithCode("from.period.is.greater.than.to.period");
        }

        if (this.isAmountRangeFromGreaterThanTo()) {
            final BigDecimal amountRangeFrom = chartSlabsCommand.bigDecimalValueOfParameterNamed(amountRangeFromParamName);
            baseDataValidator.parameter(amountRangeFromParamName).value(amountRangeFrom)
                    .failWithCode("amount.range.from.is.greater.than.amount.range.to");
        }
    }

    public boolean isFromPeriodGreaterThanToPeriod() {
        boolean isGreater = false;
        if (this.toPeriod != null && fromPeriod.compareTo(this.toPeriod) > 1) {
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

    public boolean isPeriodOverlapping(InterestRateChartSlabFields that) {
        if (that.toPeriod == null){
            if (this.toPeriod == null){
                return true;
            }
            return that.fromPeriod <= this.toPeriod;
        }
        return this.fromPeriod <= that.toPeriod && that.fromPeriod <= this.toPeriod;
    }
    
    public boolean isBetweenPeriod(final LocalDate periodStartDate, final LocalDate periodEndDate){
        final Integer compare = depositPeriod(periodStartDate, periodEndDate);
        return (compare < this.fromPeriod || (this.toPeriod != null && compare > this.toPeriod)) ? false : true;
    }
    
    public boolean isAmountRangeProvided(){
        return (this.amountRangeFrom == null) ? false : true;
    }
    
    public BigDecimal annualInterestRate() {
        return this.annualInterestRate;
    }
    
    public Integer depositPeriod(final LocalDate periodStartDate, final LocalDate periodEndDate) {
        Integer actualDepositPeriod = 0;
        final SavingsPeriodFrequencyType periodFrequencyType = SavingsPeriodFrequencyType.fromInt(this.periodType());
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
    
    public boolean isAmountBetween(final BigDecimal depositAmount){
        return depositAmount.compareTo(amountRangeFrom) >= 0 && depositAmount.compareTo(amountRangeTo) <= 0;
    }
}