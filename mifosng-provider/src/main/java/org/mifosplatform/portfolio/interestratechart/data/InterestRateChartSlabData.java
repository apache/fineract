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
    private final CurrencyData currency;

    // template
    private Collection<EnumOptionData> periodTypes;

    public static InterestRateChartSlabData instance(Long id, String description, EnumOptionData periodType, Integer fromPeriod,
            Integer toPeriod, BigDecimal amountRangeFrom, BigDecimal amountRangeTo, BigDecimal annualInterestRate, CurrencyData currency) {
        Collection<EnumOptionData> periodTypes = null;
        return new InterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, currency, periodTypes);
    }
    
    public static InterestRateChartSlabData withTemplate(InterestRateChartSlabData chartSlab, Collection<EnumOptionData> periodTypes) {
        return new InterestRateChartSlabData(chartSlab.id, chartSlab.description, chartSlab.periodType, chartSlab.fromPeriod, chartSlab.toPeriod, chartSlab.amountRangeFrom, chartSlab.amountRangeTo,
                chartSlab.annualInterestRate, chartSlab.currency, periodTypes);
    }

    private InterestRateChartSlabData(Long id, String description, EnumOptionData periodType, Integer fromPeriod, Integer toPeriod,
            BigDecimal amountRangeFrom, BigDecimal amountRangeTo, BigDecimal annualInterestRate, CurrencyData currency, Collection<EnumOptionData> periodTypes) {
        this.id = id;
        this.description = description;
        this.periodType = periodType;
        this.fromPeriod = fromPeriod;
        this.toPeriod = toPeriod;
        this.amountRangeFrom = amountRangeFrom;
        this.amountRangeTo = amountRangeTo;
        this.annualInterestRate = annualInterestRate;
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

    
    public CurrencyData currency() {
        return this.currency;
    }
    
    public Collection<EnumOptionData> periodTypes() {
        return this.periodTypes;
    }
    
}