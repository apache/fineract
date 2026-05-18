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
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.infrastructure.core.service.DateUtils;
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
        var map = new HashMap<TaxComponent, BigDecimal>();

        if (amount != null) {
            var amountVal = amount.doubleValue();
            var cent_percentage = Double.parseDouble("100.0");

            for (var groupMappings : taxGroupMappings) {
                if (groupMappings.occursOnDayFromAndUpToAndIncluding(date)) {
                    var component = groupMappings.getTaxComponent();
                    var percentage = component.getApplicablePercentage(date);

                    if (percentage != null) {
                        var percentageVal = percentage.doubleValue();
                        var tax = amountVal * percentageVal / cent_percentage;

                        map.put(component, BigDecimal.valueOf(tax).setScale(scale, MoneyHelper.getRoundingMode()));
                    }
                }
            }
        }

        return map;
    }

    public static Map<TaxComponentData, BigDecimal> splitTaxData(final BigDecimal amount, final LocalDate date,
            final Set<TaxGroupMappingsData> taxGroupMappings, final int scale) {
        var map = new HashMap<TaxComponentData, BigDecimal>();

        if (amount != null) {
            var amountVal = amount.doubleValue();
            var centPercentage = Double.parseDouble("100.0");

            for (var groupMappings : taxGroupMappings) {
                if (occursOnDayFromAndUpToAndIncluding(groupMappings.getStartDate(), groupMappings.getEndDate(), date)) {
                    var component = groupMappings.getTaxComponent();
                    var percentage = component.getApplicablePercentage(date);

                    if (percentage != null) {
                        var percentageVal = percentage.doubleValue();
                        var tax = amountVal * percentageVal / centPercentage;

                        map.put(component, BigDecimal.valueOf(tax).setScale(scale, MoneyHelper.getRoundingMode()));
                    }
                }
            }
        }

        return map;
    }

    public static BigDecimal totalTaxAmount(final Map<TaxComponent, BigDecimal> map) {
        var totalTax = BigDecimal.ZERO;

        for (var tax : map.values()) {
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

    public static Map<TaxComponent, BigDecimal> computeTax(final TaxGroup taxGroup, final BigDecimal baseAmount,
            final LocalDate effectiveDate, final int scale) {
        if (taxGroup == null || baseAmount == null || baseAmount.compareTo(BigDecimal.ZERO) == 0) {
            return Collections.emptyMap();
        }

        return splitTax(baseAmount, effectiveDate, taxGroup.getTaxGroupMappings(), scale);
    }

    private static boolean occursOnDayFromAndUpToAndIncluding(final LocalDate startDate, final LocalDate endDate, final LocalDate target) {
        return DateUtils.isAfter(target, startDate) && (endDate == null || !DateUtils.isAfter(target, endDate));
    }
}
