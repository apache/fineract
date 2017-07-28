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
package org.apache.fineract.portfolio.savings.data;

import java.util.ArrayList;
import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartSlabData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a deposit account interest rate chart.
 */
public class DepositAccountInterestRateChartData {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate fromDate;
    private final LocalDate endDate;
    private final boolean isPrimaryGroupingByAmount;
    private final Long accountId;
    private final String accountNumber;
    // associations
    private Collection<DepositAccountInterestRateChartSlabData> chartSlabs;

    // template
    private final Collection<EnumOptionData> periodTypes;
    private final Collection<EnumOptionData> entityTypeOptions;
    private final Collection<EnumOptionData> attributeNameOptions;
    private final Collection<EnumOptionData> conditionTypeOptions;
    private final Collection<EnumOptionData> incentiveTypeOptions;
    private final Collection<CodeValueData> genderOptions;
    private final Collection<CodeValueData> clientTypeOptions;
    private final Collection<CodeValueData> clientClassificationOptions;

    public static DepositAccountInterestRateChartData instance(Long id, String name, String description, LocalDate fromDate,
            LocalDate endDate, boolean isPrimaryGroupingByAmount, Long accountId, String accountNumber,
            Collection<DepositAccountInterestRateChartSlabData> chartSlabs, Collection<EnumOptionData> periodTypes) {

        final Collection<EnumOptionData> entityTypeOptions = null;
        final Collection<EnumOptionData> attributeNameOptions = null;
        final Collection<EnumOptionData> conditionTypeOptions = null;
        final Collection<EnumOptionData> incentiveTypeOptions = null;
        final Collection<CodeValueData> genderOptions = null;
        final Collection<CodeValueData> clientTypeOptions = null;
        final Collection<CodeValueData> clientClassificationOptions = null;
        return new DepositAccountInterestRateChartData(id, name, description, fromDate, endDate, isPrimaryGroupingByAmount, accountId,
                accountNumber, chartSlabs, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions,
                incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    public static DepositAccountInterestRateChartData from(InterestRateChartData productChartData) {
        final Long id = null;
        final Long accountId = null;
        final String accountNumber = null;
        Collection<DepositAccountInterestRateChartSlabData> fromProdChartSlabs = new ArrayList<>();
        Collection<InterestRateChartSlabData> productChartSlabDatas = productChartData.chartSlabs();
        if (productChartSlabDatas != null) {
            for (InterestRateChartSlabData productChartSlabData : productChartSlabDatas) {
                fromProdChartSlabs.add(DepositAccountInterestRateChartSlabData.from(productChartSlabData));
            }
        }

        return new DepositAccountInterestRateChartData(id, productChartData.name(), productChartData.description(),
                productChartData.fromDate(), productChartData.endDate(), productChartData.isPrimaryGroupingByAmount(), accountId,
                accountNumber, fromProdChartSlabs, productChartData.periodTypes(), productChartData.entityTypeOptions(),
                productChartData.attributeNameOptions(), productChartData.conditionTypeOptions(), productChartData.incentiveTypeOptions(),
                productChartData.genderOptions(), productChartData.clientTypeOptions(), productChartData.clientClassificationOptions());
    }

    public static DepositAccountInterestRateChartData withSlabs(DepositAccountInterestRateChartData interestRateChartData,
            Collection<DepositAccountInterestRateChartSlabData> chartSlabs) {
        return new DepositAccountInterestRateChartData(interestRateChartData.id, interestRateChartData.name,
                interestRateChartData.description, interestRateChartData.fromDate, interestRateChartData.endDate,
                interestRateChartData.isPrimaryGroupingByAmount, interestRateChartData.accountId, interestRateChartData.accountNumber,
                chartSlabs, interestRateChartData.periodTypes, interestRateChartData.entityTypeOptions,
                interestRateChartData.attributeNameOptions, interestRateChartData.conditionTypeOptions,
                interestRateChartData.incentiveTypeOptions, interestRateChartData.genderOptions, interestRateChartData.clientTypeOptions,
                interestRateChartData.clientClassificationOptions);
    }

    public static DepositAccountInterestRateChartData withTemplate(DepositAccountInterestRateChartData interestRateChartData,
            Collection<EnumOptionData> periodTypes, final Collection<EnumOptionData> entityTypeOptions,
            final Collection<EnumOptionData> attributeNameOptions, final Collection<EnumOptionData> conditionTypeOptions,
            final Collection<EnumOptionData> incentiveTypeOptions, final Collection<CodeValueData> genderOptions,
            final Collection<CodeValueData> clientTypeOptions, final Collection<CodeValueData> clientClassificationOptions) {
        return new DepositAccountInterestRateChartData(interestRateChartData.id, interestRateChartData.name,
                interestRateChartData.description, interestRateChartData.fromDate, interestRateChartData.endDate,
                interestRateChartData.isPrimaryGroupingByAmount, interestRateChartData.accountId, interestRateChartData.accountNumber,
                interestRateChartData.chartSlabs, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions,
                incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    public static DepositAccountInterestRateChartData template(Collection<EnumOptionData> periodTypes,
            final Collection<EnumOptionData> entityTypeOptions, final Collection<EnumOptionData> attributeNameOptions,
            final Collection<EnumOptionData> conditionTypeOptions, final Collection<EnumOptionData> incentiveTypeOptions,
            final Collection<CodeValueData> genderOptions, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions) {
        final Long id = null;
        final String name = null;
        final String description = null;
        final LocalDate fromDate = null;
        final LocalDate endDate = null;
        final Long accountId = null;
        final String accountNumber = null;
        final boolean isPrimaryGroupingByAmount = false;
        final Collection<DepositAccountInterestRateChartSlabData> chartSlabs = null;
        return new DepositAccountInterestRateChartData(id, name, description, fromDate, endDate, isPrimaryGroupingByAmount, accountId,
                accountNumber, chartSlabs, periodTypes, entityTypeOptions, attributeNameOptions, conditionTypeOptions,
                incentiveTypeOptions, genderOptions, clientTypeOptions, clientClassificationOptions);
    }

    private DepositAccountInterestRateChartData(Long id, String name, String description, LocalDate fromDate, LocalDate endDate,
            final boolean isPrimaryGroupingByAmount, Long accountId, String accountNumber,
            Collection<DepositAccountInterestRateChartSlabData> chartSlabs, Collection<EnumOptionData> periodTypes,
            final Collection<EnumOptionData> entityTypeOptions, final Collection<EnumOptionData> attributeNameOptions,
            final Collection<EnumOptionData> conditionTypeOptions, final Collection<EnumOptionData> incentiveTypeOptions,
            final Collection<CodeValueData> genderOptions, final Collection<CodeValueData> clientTypeOptions,
            final Collection<CodeValueData> clientClassificationOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.endDate = endDate;
        this.isPrimaryGroupingByAmount = isPrimaryGroupingByAmount;
        this.chartSlabs = chartSlabs;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.periodTypes = periodTypes;
        this.attributeNameOptions = attributeNameOptions;
        this.entityTypeOptions = entityTypeOptions;
        this.conditionTypeOptions = conditionTypeOptions;
        this.incentiveTypeOptions = incentiveTypeOptions;
        this.genderOptions = genderOptions;
        this.clientTypeOptions = clientTypeOptions;
        this.clientClassificationOptions = clientClassificationOptions;
    }

    public void addChartSlab(final DepositAccountInterestRateChartSlabData chartSlab) {
        if (this.chartSlabs == null) {
            this.chartSlabs = new ArrayList<>();
        }

        this.chartSlabs.add(chartSlab);
    }

    public boolean isFromDateAfter(final LocalDate compareDate) {
        return (compareDate == null) ? false : this.fromDate.isAfter(compareDate);
    }

    public LocalDate endDate() {
        return this.endDate;
    }

    public LocalDate fromDate() {
        return this.fromDate;
    }
}