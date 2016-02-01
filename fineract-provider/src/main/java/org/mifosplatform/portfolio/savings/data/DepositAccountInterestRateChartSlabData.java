/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.interestratechart.data.InterestIncentiveData;
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
    private final CurrencyData currency;

    // associations
    private Collection<DepositAccountInterestIncentiveData> incentives;

    // template
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> periodTypes;
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> entityTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> attributeNameOptions;
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> conditionTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<EnumOptionData> incentiveTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> genderOptions;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> clientTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<CodeValueData> clientClassificationOptions;

    public static DepositAccountInterestRateChartSlabData instance(final Long id, final String description,
            final EnumOptionData periodType, final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom,
            final BigDecimal amountRangeTo, final BigDecimal annualInterestRate, final CurrencyData currency) {
        final Collection<EnumOptionData> periodTypes = null;
        final Collection<EnumOptionData> entityTypeOptions = null;
        final Collection<EnumOptionData> attributeNameOptions = null;
        final Collection<EnumOptionData> conditionTypeOptions = null;
        final Collection<EnumOptionData> incentiveTypeOptions = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        final Collection<DepositAccountInterestIncentiveData> incentives = null;
        return new DepositAccountInterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom,
                amountRangeTo, annualInterestRate, incentives, currency, periodTypes, entityTypeOptions, attributeNameOptions,
                conditionTypeOptions, incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    public static DepositAccountInterestRateChartSlabData from(final InterestRateChartSlabData chartSlabData) {
        final Long id = null;
        Set<DepositAccountInterestIncentiveData> fromProdIncentives = new HashSet<>();
        Set<InterestIncentiveData> productIncentiveData = chartSlabData.incentives();
        if (productIncentiveData != null) {
            for (InterestIncentiveData incentive : productIncentiveData) {
                fromProdIncentives.add(DepositAccountInterestIncentiveData.from(incentive));
            }
        }
        return new DepositAccountInterestRateChartSlabData(id, chartSlabData.description(), chartSlabData.periodType(),
                chartSlabData.fromPeriod(), chartSlabData.toPeriod(), chartSlabData.amountRangeFrom(), chartSlabData.amountRangeTo(),
                chartSlabData.annualInterestRate(), fromProdIncentives, chartSlabData.currency(), chartSlabData.periodTypes(),
                chartSlabData.entityTypeOptions(), chartSlabData.attributeNameOptions(), chartSlabData.conditionTypeOptions(),
                chartSlabData.incentiveTypeOptions(), chartSlabData.genderOptions(), chartSlabData.clientTypeOptions(),
                chartSlabData.clientClassificationOptions());
    }

    public static DepositAccountInterestRateChartSlabData withTemplate(final DepositAccountInterestRateChartSlabData chartSlab,
            final Collection<EnumOptionData> periodTypes, final Collection<EnumOptionData> entityTypeOptions,
            final Collection<EnumOptionData> attributeNameOptions, final Collection<EnumOptionData> conditionTypeOptions,
            final Collection<EnumOptionData> incentiveTypeOptions, final Collection<CodeValueData> genderOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions) {
        return new DepositAccountInterestRateChartSlabData(chartSlab.id, chartSlab.description, chartSlab.periodType, chartSlab.fromPeriod,
                chartSlab.toPeriod, chartSlab.amountRangeFrom, chartSlab.amountRangeTo, chartSlab.annualInterestRate, chartSlab.incentives,
                chartSlab.currency, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions, incentiveTypeOptions,
                genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    private DepositAccountInterestRateChartSlabData(final Long id, final String description, final EnumOptionData periodType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final Collection<DepositAccountInterestIncentiveData> incentivesData,
            final CurrencyData currency, final Collection<EnumOptionData> periodTypes, final Collection<EnumOptionData> entityTypeOptions,
            final Collection<EnumOptionData> attributeNameOptions, final Collection<EnumOptionData> conditionTypeOptions,
            final Collection<EnumOptionData> incentiveTypeOptions, final Collection<CodeValueData> genderOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions) {
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
        this.incentives = incentivesData;
        this.attributeNameOptions = attributeNameOptions;
        this.entityTypeOptions = entityTypeOptions;
        this.conditionTypeOptions = conditionTypeOptions;
        this.incentiveTypeOptions = incentiveTypeOptions;
        this.genderOptions = genderOptions;
        this.clientTypeOptions = clientTypeOptions;
        this.clientClassificationOptions = clientClassificationOptions;
    }

    public void addIncentives(final DepositAccountInterestIncentiveData incentive) {
        if (this.incentives == null) {
            this.incentives = new HashSet<>();
        }
        this.incentives.add(incentive);
    }

}