/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object representing a InterestRateChartSlab.
 */
public class InterestRateChartSlabData {

    private final Long id;
    private final String description;
    private final EnumOptionData periodType;
    private final Integer fromPeriod;
    private final Integer toPeriod;
    private final BigDecimal amountRangeFrom;
    private final BigDecimal amountRangeTo;
    private final BigDecimal annualInterestRate;
    private final BigDecimal interestRateForFemale;
    private final BigDecimal interestRateForChildren;
    private final BigDecimal interestRateForSeniorCitizen;
    private final CurrencyData currency;

    // template
    private final Collection<EnumOptionData> periodTypes;

    public static InterestRateChartSlabData instance(final Long id, final String description, final EnumOptionData periodType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final BigDecimal interestRateForFemale, final BigDecimal interestRateForChildren,
            final BigDecimal interestRateForSeniorCitizen, final CurrencyData currency) {
        final Collection<EnumOptionData> periodTypes = null;
        return new InterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, interestRateForFemale, interestRateForChildren, interestRateForSeniorCitizen, currency, periodTypes);
    }

    public static InterestRateChartSlabData withTemplate(final InterestRateChartSlabData chartSlab,
            final Collection<EnumOptionData> periodTypes) {
        return new InterestRateChartSlabData(chartSlab.id, chartSlab.description, chartSlab.periodType, chartSlab.fromPeriod,
                chartSlab.toPeriod, chartSlab.amountRangeFrom, chartSlab.amountRangeTo, chartSlab.annualInterestRate,
                chartSlab.interestRateForFemale, chartSlab.interestRateForChildren, chartSlab.interestRateForSeniorCitizen,
                chartSlab.currency, periodTypes);
    }

    public static InterestRateChartSlabData template(final Collection<EnumOptionData> periodTypes) {
        final Long id = null;
        final String description = null;
        final EnumOptionData periodType = null;
        final Integer fromPeriod = null;
        final Integer toPeriod = null;
        final BigDecimal amountRangeFrom = null;
        final BigDecimal amountRangeTo = null;
        final BigDecimal annualInterestRate = null;
        final BigDecimal interestRateForFemale = null;
        final BigDecimal interestRateForChildren = null;
        final BigDecimal interestRateForSeniorCitizen = null;
        final CurrencyData currency = null;
        return new InterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, interestRateForFemale, interestRateForChildren, interestRateForSeniorCitizen, currency, periodTypes);
    }

    private InterestRateChartSlabData(final Long id, final String description, final EnumOptionData periodType, final Integer fromPeriod,
            final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo, final BigDecimal annualInterestRate,
            final BigDecimal interestRateForFemale, final BigDecimal interestRateForChildren,
            final BigDecimal interestRateForSeniorCitizen, final CurrencyData currency, final Collection<EnumOptionData> periodTypes) {
        this.id = id;
        this.description = description;
        this.periodType = periodType;
        this.fromPeriod = fromPeriod;
        this.toPeriod = toPeriod;
        this.amountRangeFrom = amountRangeFrom;
        this.amountRangeTo = amountRangeTo;
        this.annualInterestRate = annualInterestRate;
        this.interestRateForFemale = interestRateForFemale;
        this.interestRateForChildren = interestRateForChildren;
        this.interestRateForSeniorCitizen = interestRateForSeniorCitizen;
        this.currency = currency;
        this.periodTypes = periodTypes;
    }

    public String description() {
        return this.description;
    }

    public EnumOptionData periodType() {
        return this.periodType;
    }

    public Integer fromPeriod() {
        return this.fromPeriod;
    }

    public Integer toPeriod() {
        return this.toPeriod;
    }

    public BigDecimal amountRangeFrom() {
        return this.amountRangeFrom;
    }

    public BigDecimal amountRangeTo() {
        return this.amountRangeTo;
    }

    public BigDecimal annualInterestRate() {
        return this.annualInterestRate;
    }
        
    public BigDecimal interestRateForFemale() {
        return this.interestRateForFemale;
    }
    
    public BigDecimal interestRateForChildren() {
        return this.interestRateForChildren;
    }
    
    public BigDecimal interestRateForSeniorCitizen() {
        return this.interestRateForSeniorCitizen;
    }

    public CurrencyData currency() {
        return this.currency;
    }

    public Collection<EnumOptionData> periodTypes() {
        return this.periodTypes;
    }

}