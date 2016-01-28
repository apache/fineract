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
package org.apache.fineract.portfolio.interestratechart.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;

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

    // associations
    private Set<InterestIncentiveData> incentives;

    // template
    private final Collection<EnumOptionData> periodTypes;
    private final Collection<EnumOptionData> entityTypeOptions;
    private final Collection<EnumOptionData> attributeNameOptions;
    private final Collection<EnumOptionData> conditionTypeOptions;
    private final Collection<EnumOptionData> incentiveTypeOptions;
    private final Collection<CodeValueData> genderOptions;
    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> clientClassificationOptions;

    public static InterestRateChartSlabData instance(final Long id, final String description, final EnumOptionData periodType,
            final Integer fromPeriod, final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo,
            final BigDecimal annualInterestRate, final CurrencyData currency) {
        final Collection<EnumOptionData> periodTypes = null;
        final Set<InterestIncentiveData> incentivesData = null;
        final Collection<EnumOptionData> entityTypeOptions = null;
        final Collection<EnumOptionData> attributeNameOptions = null;
        final Collection<EnumOptionData> conditionTypeOptions = null;
        final Collection<EnumOptionData> incentiveTypeOptions = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        return new InterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, currency, incentivesData, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions,
                incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    public static InterestRateChartSlabData withTemplate(final InterestRateChartSlabData chartSlab,
            final Collection<EnumOptionData> periodTypes, final Collection<EnumOptionData> entityTypeOptions,
            final Collection<EnumOptionData> attributeNameOptions, final Collection<EnumOptionData> conditionTypeOptions,
            final Collection<EnumOptionData> incentiveTypeOptions, final Collection<CodeValueData> genderOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions) {
        return new InterestRateChartSlabData(chartSlab.id, chartSlab.description, chartSlab.periodType, chartSlab.fromPeriod,
                chartSlab.toPeriod, chartSlab.amountRangeFrom, chartSlab.amountRangeTo, chartSlab.annualInterestRate, chartSlab.currency,
                chartSlab.incentives, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions, incentiveTypeOptions,
                genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    public static InterestRateChartSlabData template(final Collection<EnumOptionData> periodTypes,
            final Collection<EnumOptionData> entityTypeOptions, final Collection<EnumOptionData> attributeNameOptions,
            final Collection<EnumOptionData> conditionTypeOptions, final Collection<EnumOptionData> incentiveTypeOptions,
            final Collection<CodeValueData> genderOptions, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions) {
        final Long id = null;
        final String description = null;
        final EnumOptionData periodType = null;
        final Integer fromPeriod = null;
        final Integer toPeriod = null;
        final BigDecimal amountRangeFrom = null;
        final BigDecimal amountRangeTo = null;
        final BigDecimal annualInterestRate = null;
        final CurrencyData currency = null;
        final Set<InterestIncentiveData> incentivesData = null;
        return new InterestRateChartSlabData(id, description, periodType, fromPeriod, toPeriod, amountRangeFrom, amountRangeTo,
                annualInterestRate, currency, incentivesData, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions,
                incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    private InterestRateChartSlabData(final Long id, final String description, final EnumOptionData periodType, final Integer fromPeriod,
            final Integer toPeriod, final BigDecimal amountRangeFrom, final BigDecimal amountRangeTo, final BigDecimal annualInterestRate,
            final CurrencyData currency, final Set<InterestIncentiveData> incentivesData, final Collection<EnumOptionData> periodTypes,
            final Collection<EnumOptionData> entityTypeOptions, final Collection<EnumOptionData> attributeNameOptions,
            final Collection<EnumOptionData> conditionTypeOptions, final Collection<EnumOptionData> incentiveTypeOptions,
            final Collection<CodeValueData> genderOptions, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions) {
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

    public void addIncentives(final InterestIncentiveData incentiveData) {
        if (this.incentives == null) {
            this.incentives = new HashSet<>();
        }

        this.incentives.add(incentiveData);
    }

    public Set<InterestIncentiveData> incentives() {
        return this.incentives;
    }

    public Collection<EnumOptionData> entityTypeOptions() {
        return this.entityTypeOptions;
    }

    public Collection<EnumOptionData> attributeNameOptions() {
        return this.attributeNameOptions;
    }

    public Collection<EnumOptionData> conditionTypeOptions() {
        return this.conditionTypeOptions;
    }

    public Collection<EnumOptionData> incentiveTypeOptions() {
        return this.incentiveTypeOptions;
    }

    public Collection<CodeValueData> genderOptions() {
        return this.genderOptions;
    }

    public Collection<CodeValueData> clientTypeOptions() {
        return this.clientTypeOptions;
    }

    public Collection<CodeValueData> clientClassificationOptions() {
        return this.clientClassificationOptions;
    }

}