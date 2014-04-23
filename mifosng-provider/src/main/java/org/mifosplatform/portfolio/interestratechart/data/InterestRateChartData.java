/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object representing a InterestRateChart.
 */
public class InterestRateChartData {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate fromDate;
    private final LocalDate endDate;
    private final Long productId;
    private final String productName;

    // associations
    private Set<InterestRateChartSlabData> chartSlabs;

    // template
    private Collection<EnumOptionData> periodTypes;

    public static InterestRateChartData instance(Long id, String name, String description, LocalDate fromDate, LocalDate endDate,
            Long savingsProductId, String savingsProductName) {
        Collection<EnumOptionData> periodTypes = null;
        Set<InterestRateChartSlabData> chartSlabs = null;
        return new InterestRateChartData(id, name, description, fromDate, endDate, savingsProductId, savingsProductName, chartSlabs,
                periodTypes);
    }

    public static InterestRateChartData withSlabs(InterestRateChartData interestRateChartData,
            Set<InterestRateChartSlabData> chartSlabs) {
        return new InterestRateChartData(interestRateChartData.id, interestRateChartData.name, interestRateChartData.description,
                interestRateChartData.fromDate, interestRateChartData.endDate, interestRateChartData.productId,
                interestRateChartData.productName, chartSlabs, interestRateChartData.periodTypes);
    }

    public static InterestRateChartData withTemplate(InterestRateChartData interestRateChartData, Collection<EnumOptionData> periodTypes) {
        return new InterestRateChartData(interestRateChartData.id, interestRateChartData.name, interestRateChartData.description,
                interestRateChartData.fromDate, interestRateChartData.endDate, interestRateChartData.productId,
                interestRateChartData.productName, interestRateChartData.chartSlabs, periodTypes);
    }

    public static InterestRateChartData template(Collection<EnumOptionData> periodTypes) {
        final Long id = null;
        final String name = null;
        final String description = null;
        final LocalDate fromDate = null;
        final LocalDate endDate = null;
        final Long savingsProductId = null;
        final String savingsProductName = null;
        final Set<InterestRateChartSlabData> chartSlabs = null;

        return new InterestRateChartData(id, name, description, fromDate, endDate, savingsProductId, savingsProductName, chartSlabs,
                periodTypes);
    }

    private InterestRateChartData(Long id, String name, String description, LocalDate fromDate, LocalDate endDate, Long savingsProductId,
            String savingsProductName, Set<InterestRateChartSlabData> chartSlabs, Collection<EnumOptionData> periodTypes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.endDate = endDate;
        this.chartSlabs = chartSlabs;
        this.productId = savingsProductId;
        this.productName = savingsProductName;
        this.periodTypes = periodTypes;
    }

    public void addChartSlab(final InterestRateChartSlabData chartSlab) {
        if (this.chartSlabs == null) {
            this.chartSlabs = new HashSet<InterestRateChartSlabData>();
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

    public String name() {
        return this.name;
    }

    public String description() {
        return this.description;
    }

    public Set<InterestRateChartSlabData> chartSlabs() {
        return this.chartSlabs;
    }

    public Collection<EnumOptionData> periodTypes() {
        return this.periodTypes;
    }
}