/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartSlabData;

/**
 * Immutable data object representing a deposit account interest rate chart.
 */
public class DepositAccountInterestRateChartData {

    private final Long id;
    private final String name;
    private final String description;
    private final LocalDate fromDate;
    private final LocalDate endDate;
    private final Long accountId;
    private final String accountNumber;
    // associations
    private Set<DepositAccountInterestRateChartSlabData> chartSlabs;

    // template
    private Collection<EnumOptionData> periodTypes;

    public static DepositAccountInterestRateChartData instance(Long id, String name, String description, LocalDate fromDate,
            LocalDate endDate, Long accountId, String accountNumber, Set<DepositAccountInterestRateChartSlabData> chartSlabs,
            Collection<EnumOptionData> periodTypes) {

        return new DepositAccountInterestRateChartData(id, name, description, fromDate, endDate, accountId, accountNumber, chartSlabs,
                periodTypes);
    }

    public static DepositAccountInterestRateChartData from(InterestRateChartData productChartData) {
        final Long id = null;
        final Long accountId = null;
        final String accountNumber = null;
        Set<DepositAccountInterestRateChartSlabData> fromProdChartSlabs = new HashSet<DepositAccountInterestRateChartSlabData>();
        Set<InterestRateChartSlabData> productChartSlabDatas = productChartData.chartSlabs();
        if (productChartSlabDatas != null) {
            for (InterestRateChartSlabData productChartSlabData : productChartSlabDatas) {
                fromProdChartSlabs.add(DepositAccountInterestRateChartSlabData.from(productChartSlabData));
            }
        }

        return new DepositAccountInterestRateChartData(id, productChartData.name(), productChartData.description(),
                productChartData.fromDate(), productChartData.endDate(), accountId, accountNumber, fromProdChartSlabs,
                productChartData.periodTypes());
    }

    public static DepositAccountInterestRateChartData withSlabs(DepositAccountInterestRateChartData interestRateChartData,
            Set<DepositAccountInterestRateChartSlabData> chartSlabs) {
        return new DepositAccountInterestRateChartData(interestRateChartData.id, interestRateChartData.name,
                interestRateChartData.description, interestRateChartData.fromDate, interestRateChartData.endDate,
                interestRateChartData.accountId, interestRateChartData.accountNumber, chartSlabs, interestRateChartData.periodTypes);
    }

    public static DepositAccountInterestRateChartData withTemplate(DepositAccountInterestRateChartData interestRateChartData,
            Collection<EnumOptionData> periodTypes) {
        return new DepositAccountInterestRateChartData(interestRateChartData.id, interestRateChartData.name,
                interestRateChartData.description, interestRateChartData.fromDate, interestRateChartData.endDate,
                interestRateChartData.accountId, interestRateChartData.accountNumber, interestRateChartData.chartSlabs, periodTypes);
    }

    public static DepositAccountInterestRateChartData template(Collection<EnumOptionData> periodTypes) {
        final Long id = null;
        final String name = null;
        final String description = null;
        final LocalDate fromDate = null;
        final LocalDate endDate = null;
        final Long accountId = null;
        final String accountNumber = null;
        final Set<DepositAccountInterestRateChartSlabData> chartSlabs = null;
        return new DepositAccountInterestRateChartData(id, name, description, fromDate, endDate, accountId, accountNumber, chartSlabs,
                periodTypes);
    }

    private DepositAccountInterestRateChartData(Long id, String name, String description, LocalDate fromDate, LocalDate endDate,
            Long accountId, String accountNumber, Set<DepositAccountInterestRateChartSlabData> chartSlabs,
            Collection<EnumOptionData> periodTypes) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.endDate = endDate;
        this.chartSlabs = chartSlabs;
        this.accountId = accountId;
        this.accountNumber = accountNumber;
        this.periodTypes = periodTypes;
    }

    public void addChartSlab(final DepositAccountInterestRateChartSlabData chartSlab) {
        if (this.chartSlabs == null) {
            this.chartSlabs = new HashSet<DepositAccountInterestRateChartSlabData>();
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