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
package org.apache.fineract.portfolio.tax.service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.organisation.monetary.domain.MoneyHelper;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupMappingsData;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroup;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;

public final class TaxUtils {

    private TaxUtils() {

    }

    public static Map<TaxComponent, BigDecimal> splitTax(final BigDecimal amount, final LocalDate date,
            final Set<TaxGroupMappings> taxGroupMappings, final int scale) {
        Map<TaxComponent, BigDecimal> map = new HashMap<>(3);
        if (amount != null) {
            final double amountVal = amount.doubleValue();
            double cent_percentage = Double.parseDouble("100.0");
            for (TaxGroupMappings groupMappings : taxGroupMappings) {
                if (groupMappings.occursOnDayFromAndUpToAndIncluding(date)) {
                    TaxComponent component = groupMappings.getTaxComponent();
                    BigDecimal percentage = component.getApplicablePercentage(date);
                    if (percentage != null) {
                        double percentageVal = percentage.doubleValue();
                        double tax = amountVal * percentageVal / cent_percentage;
                        map.put(component, BigDecimal.valueOf(tax).setScale(scale, MoneyHelper.getRoundingMode()));
                    }
                }
            }
        }
        return map;
    }

    public static Map<TaxComponentData, BigDecimal> splitTaxData(final BigDecimal amount, final LocalDate date,
            final Set<TaxGroupMappingsData> taxGroupMappings, final int scale) {
        Map<TaxComponentData, BigDecimal> map = new HashMap<>(3);
        if (amount != null) {
            final double amountVal = amount.doubleValue();
            double cent_percentage = Double.parseDouble("100.0");
            for (TaxGroupMappingsData groupMappings : taxGroupMappings) {
                if (groupMappings.occursOnDayFromAndUpToAndIncluding(date)) {
                    TaxComponentData component = groupMappings.getTaxComponent();
                    BigDecimal percentage = component.getApplicablePercentage(date);
                    if (percentage != null) {
                        double percentageVal = percentage.doubleValue();
                        double tax = amountVal * percentageVal / cent_percentage;
                        map.put(component, BigDecimal.valueOf(tax).setScale(scale, MoneyHelper.getRoundingMode()));
                    }
                }
            }
        }
        return map;
    }

    public static BigDecimal incomeAmount(final BigDecimal amount, final LocalDate date, final Set<TaxGroupMappings> taxGroupMappings,
            final int scale) {
        Map<TaxComponent, BigDecimal> map = splitTax(amount, date, taxGroupMappings, scale);
        return incomeAmount(amount, map);
    }

    public static BigDecimal incomeAmount(final BigDecimal amount, final Map<TaxComponent, BigDecimal> map) {
        BigDecimal totalTax = totalTaxAmount(map);
        return amount.subtract(totalTax);
    }

    public static BigDecimal totalTaxAmount(final Map<TaxComponent, BigDecimal> map) {
        BigDecimal totalTax = BigDecimal.ZERO;
        for (BigDecimal tax : map.values()) {
            totalTax = totalTax.add(tax);
        }
        return totalTax;
    }

    public static BigDecimal totalTaxDataAmount(final Map<TaxComponentData, BigDecimal> map) {
        BigDecimal totalTax = BigDecimal.ZERO;
        for (BigDecimal tax : map.values()) {
            totalTax = totalTax.add(tax);
        }
        return totalTax;
    }

    public static BigDecimal addTax(final BigDecimal amount, final LocalDate date, final List<TaxGroupMappings> taxGroupMappings,
            final int scale) {
        BigDecimal totalAmount = null;
        if (amount != null && amount.compareTo(BigDecimal.ZERO) > 0) {
            double percentageVal = 0;
            double amountVal = amount.doubleValue();
            double cent_percentage = Double.parseDouble("100.0");
            for (TaxGroupMappings groupMappings : taxGroupMappings) {
                if (groupMappings.occursOnDayFromAndUpToAndIncluding(date)) {
                    TaxComponent component = groupMappings.getTaxComponent();
                    BigDecimal percentage = component.getApplicablePercentage(date);
                    if (percentage != null) {
                        percentageVal = percentageVal + percentage.doubleValue();
                    }
                }
            }
            double total = amountVal * cent_percentage / (cent_percentage - percentageVal);
            totalAmount = BigDecimal.valueOf(total).setScale(scale, MoneyHelper.getRoundingMode());
        }
        return totalAmount;
    }

    public static BigDecimal calculateChargeAmountWithTax(final BigDecimal baseAmount, final TaxGroup taxGroup,
            final LocalDate transactionDate, final int scale) {
        if (baseAmount == null) {
            return null;
        }
        if (taxGroup == null) {
            return baseAmount;
        }
        final Set<TaxGroupMappings> taxGroupMappings = taxGroup.getTaxGroupMappings();
        if (taxGroupMappings == null || taxGroupMappings.isEmpty()) {
            return baseAmount;
        }
        final List<TaxGroupMappings> mappingsList = new ArrayList<>(taxGroupMappings);
        final BigDecimal totalAmount = addTax(baseAmount, transactionDate, mappingsList, scale);
        return totalAmount != null ? totalAmount : baseAmount;
    }

    /**
     * Returns applicable tax components (name, id, percentage) for a transaction date after filtering by mapping
     * start/end date and effective occurrence rules.
     */
    public static List<String> getApplicableTaxComponentSummaries(final TaxGroup taxGroup, final LocalDate transactionDate) {
        if (taxGroup == null || transactionDate == null || taxGroup.getTaxGroupMappings() == null
                || taxGroup.getTaxGroupMappings().isEmpty()) {
            return List.of();
        }
        return taxGroup.getTaxGroupMappings().stream().filter(mapping -> isApplicableOnDate(mapping, transactionDate)).map(mapping -> {
            final TaxComponent component = mapping.getTaxComponent();
            final BigDecimal percentage = component != null ? component.getApplicablePercentage(transactionDate) : null;
            if (component == null || percentage == null) {
                return null;
            }
            return "id=" + component.getId() + ",name=" + component.getName() + ",percentage=" + percentage.toPlainString();
        }).filter(value -> value != null && !value.isBlank()).collect(Collectors.toList());
    }

    private static boolean isApplicableOnDate(final TaxGroupMappings groupMappings, final LocalDate transactionDate) {
        if (groupMappings.startDate() != null && transactionDate.isBefore(groupMappings.startDate())) {
            return false;
        }
        if (groupMappings.endDate() != null && transactionDate.isAfter(groupMappings.endDate())) {
            return false;
        }
        return groupMappings.occursOnDayFromAndUpToAndIncluding(transactionDate);
    }
}
