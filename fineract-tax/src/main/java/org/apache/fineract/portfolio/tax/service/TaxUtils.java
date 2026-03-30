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
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.configuration.service.TemporaryConfigurationServiceContainer;
import org.apache.fineract.portfolio.tax.data.TaxComponentData;
import org.apache.fineract.portfolio.tax.data.TaxGroupMappingsData;
import org.apache.fineract.portfolio.tax.domain.TaxComponent;
import org.apache.fineract.portfolio.tax.domain.TaxGroupMappings;

public final class TaxUtils {

    private static final BigDecimal HUNDRED = BigDecimal.valueOf(100);

    private TaxUtils() {}

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
                        map.put(component, BigDecimal.valueOf(tax));
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
                        map.put(component,
                                BigDecimal.valueOf(tax).setScale(scale, TemporaryConfigurationServiceContainer.getTaxRoundingMode()));
                    }
                }
            }
        }
        return map;
    }

    public static BigDecimal incomeAmount(final BigDecimal amount, final LocalDate date, final Set<TaxGroupMappings> taxGroupMappings,
            final int scale) {
        Map<TaxComponent, BigDecimal> map = splitTax(amount, date, taxGroupMappings, scale);
        Map<TaxComponent, BigDecimal> roundedMap = new HashMap<>();
        for (Map.Entry<TaxComponent, BigDecimal> entry : map.entrySet()) {
            roundedMap.put(entry.getKey(), entry.getValue().setScale(scale, TemporaryConfigurationServiceContainer.getTaxRoundingMode()));
        }

        return incomeAmount(amount, roundedMap);
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
        if (amount == null || amount.compareTo(BigDecimal.ZERO) <= 0) {
            return amount;
        }
        BigDecimal totalPercentage = getTotalPercentage(date, taxGroupMappings);
        if (totalPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return amount;
        }
        BigDecimal taxInclusiveMultiplier = HUNDRED.add(totalPercentage).divide(HUNDRED, scale + 2,
                TemporaryConfigurationServiceContainer.getTaxRoundingMode());
        return amount.multiply(taxInclusiveMultiplier).setScale(scale, TemporaryConfigurationServiceContainer.getTaxRoundingMode());
    }

    public static BigDecimal extractBaseAmountFromTaxInclusive(final BigDecimal taxInclusiveAmount, final LocalDate date,
            final Set<TaxGroupMappings> taxGroupMappings, final int scale) {
        if (taxInclusiveAmount == null || taxInclusiveAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return taxInclusiveAmount;
        }
        BigDecimal totalPercentage = getTotalPercentage(date, taxGroupMappings);
        if (totalPercentage.compareTo(BigDecimal.ZERO) <= 0) {
            return taxInclusiveAmount;
        }
        return taxInclusiveAmount.multiply(HUNDRED)
                .divide(HUNDRED.add(totalPercentage), scale + 4, TemporaryConfigurationServiceContainer.getTaxRoundingMode())
                .setScale(scale, TemporaryConfigurationServiceContainer.getTaxRoundingMode());
    }

    public static Map<TaxComponent, BigDecimal> splitTaxForLoanCharge(final BigDecimal taxInclusiveAmount, final LocalDate date,
            final Set<TaxGroupMappings> taxGroupMappings, final int scale) {
        if (taxInclusiveAmount == null || taxInclusiveAmount.compareTo(BigDecimal.ZERO) <= 0) {
            return new HashMap<>();
        }
        BigDecimal baseAmount = extractBaseAmountFromTaxInclusive(taxInclusiveAmount, date, taxGroupMappings, scale);
        Map<TaxComponent, BigDecimal> rawMap = splitTax(baseAmount, date, taxGroupMappings, scale);

        Map<TaxComponent, BigDecimal> roundedMap = new HashMap<>();
        for (Map.Entry<TaxComponent, BigDecimal> entry : rawMap.entrySet()) {
            roundedMap.put(entry.getKey(), entry.getValue().setScale(scale, TemporaryConfigurationServiceContainer.getTaxRoundingMode()));
        }

        return roundedMap;
    }

    private static BigDecimal getTotalPercentage(final LocalDate date, final Iterable<TaxGroupMappings> taxGroupMappings) {
        BigDecimal totalPercentage = BigDecimal.ZERO;
        for (TaxGroupMappings groupMappings : taxGroupMappings) {
            if (groupMappings.occursOnDayFromAndUpToAndIncluding(date)) {
                TaxComponent component = groupMappings.getTaxComponent();
                BigDecimal percentage = component.getApplicablePercentage(date);
                if (percentage != null) {
                    totalPercentage = totalPercentage.add(percentage);
                }
            }
        }
        return totalPercentage;
    }
}
