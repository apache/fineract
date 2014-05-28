/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;

/**
 * Immutable data object representing deposit accounts Interest rate Slabs.
 */
public class DepositAccountInterestRateChartSlabData {

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

    // associations

    // template
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> periodTypes;

    public static DepositAccountInterestRateChartSlabData instance(final Long id, final String description,
            final EnumOptionData periodType, final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom,
            final BigDecimal amountRangeTo, final BigDecimal annualInterestRate, final BigDecimal interestRateForFemale,
            final BigDecimal interestRateForChildren, final BigDecimal interestRateForSeniorCitizen, final CurrencyData currency) {
        final Collection<EnumOptionData> periodTypes = null;
        return new DepositAccountInterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom,
                amountRangeTo, annualInterestRate, interestRateForFemale, interestRateForChildren, interestRateForSeniorCitizen, currency,
                periodTypes);
    }

    public static DepositAccountInterestRateChartSlabData from(final InterestRateChartSlabData chartSlabData) {
        final Long id = null;
        return new DepositAccountInterestRateChartSlabData(id, chartSlabData.description(), chartSlabData.periodType(),
                chartSlabData.fromPeriod(), chartSlabData.toPeriod(), chartSlabData.amountRangeFrom(), chartSlabData.amountRangeTo(),
                chartSlabData.annualInterestRate(), chartSlabData.interestRateForFemale(), chartSlabData.interestRateForChildren(),
                chartSlabData.interestRateForSeniorCitizen(), chartSlabData.currency(), chartSlabData.periodTypes());
    }

    public static DepositAccountInterestRateChartSlabData withTemplate(final DepositAccountInterestRateChartSlabData chartSlab,
            final Collection<EnumOptionData> periodTypes) {
        return new DepositAccountInterestRateChartSlabData(chartSlab.id, chartSlab.description, chartSlab.periodType, chartSlab.fromPeriod,
                chartSlab.toPeriod, chartSlab.amountRangeFrom, chartSlab.amountRangeTo, chartSlab.annualInterestRate,
                chartSlab.interestRateForFemale, chartSlab.interestRateForChildren, chartSlab.interestRateForSeniorCitizen,
                chartSlab.currency, periodTypes);
    }

    private DepositAccountInterestRateChartSlabData(final Long id, final String description, final EnumOptionData periodType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final BigDecimal interestRateForFemale, final BigDecimal interestRateForChildren,
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

}