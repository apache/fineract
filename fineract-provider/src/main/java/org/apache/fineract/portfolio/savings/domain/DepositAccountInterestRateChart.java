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
package org.apache.fineract.portfolio.savings.domain;

import jakarta.persistence.CascadeType;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChart;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartFields;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.incentive.AttributeIncentiveCalculation;
import org.apache.fineract.portfolio.interestratechart.incentive.AttributeIncentiveCalculationFactory;
import org.apache.fineract.portfolio.interestratechart.incentive.IncentiveDTO;

@Entity
@Table(name = "m_savings_account_interest_rate_chart")
public class DepositAccountInterestRateChart extends AbstractPersistableCustom {

    @Embedded
    private InterestRateChartFields chartFields;

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount account;

    @OneToMany(mappedBy = "depositAccountInterestRateChart", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    private Set<DepositAccountInterestRateChartSlabs> chartSlabs = new HashSet<>();

    protected DepositAccountInterestRateChart() {
        //
    }

    public static DepositAccountInterestRateChart from(InterestRateChart productChart) {
        final Set<InterestRateChartSlab> chartSlabs = productChart.setOfChartSlabs();
        final Set<DepositAccountInterestRateChartSlabs> depostiChartSlabs = new HashSet<>();
        for (InterestRateChartSlab interestRateChartSlab : chartSlabs) {
            depostiChartSlabs.add(DepositAccountInterestRateChartSlabs.from(interestRateChartSlab, null));
        }
        final DepositAccountInterestRateChart depositChart = new DepositAccountInterestRateChart(productChart.chartFields(), null,
                depostiChartSlabs);
        // update deposit account interest rate chart ference to chart Slabs
        depositChart.updateChartSlabsReference();
        return depositChart;
    }

    private DepositAccountInterestRateChart(InterestRateChartFields chartFields, SavingsAccount account,
            Set<DepositAccountInterestRateChartSlabs> chartSlabs) {
        this.chartFields = chartFields;
        this.account = account;
        this.chartSlabs = chartSlabs;
    }

    private void updateChartSlabsReference() {
        final Set<DepositAccountInterestRateChartSlabs> chartSlabs = setOfChartSlabs();
        for (DepositAccountInterestRateChartSlabs chartSlab : chartSlabs) {
            chartSlab.updateChartReference(this);
        }
    }

    public Set<DepositAccountInterestRateChartSlabs> setOfChartSlabs() {
        if (this.chartSlabs == null) {
            this.chartSlabs = new HashSet<>();
        }

        return this.chartSlabs;
    }

    public DepositAccountInterestRateChartSlabs findChartSlab(Long chartSlabId) {
        final Set<DepositAccountInterestRateChartSlabs> chartSlabs = setOfChartSlabs();

        for (DepositAccountInterestRateChartSlabs interestRateChartSlab : chartSlabs) {
            if (interestRateChartSlab.getId().equals(chartSlabId)) {
                return interestRateChartSlab;
            }
        }
        return null;
    }

    public LocalDate getFromDate() {
        return this.chartFields.getFromDate();
    }

    public LocalDate getEndDate() {
        return this.chartFields.getEndDate();
    }

    public Long savingsAccountId() {
        return this.account == null ? null : this.account.getId();
    }

    public SavingsAccount savingsAccount() {
        return this.account;
    }

    public InterestRateChartFields chartFields() {
        return this.chartFields;
    }

    public void updateDepositAccountReference(final SavingsAccount account) {
        this.account = account;
    }

    public BigDecimal getApplicableInterestRate(final BigDecimal depositAmount, final LocalDate periodStartDate,
            final LocalDate periodEndDate, final Client client) {
        BigDecimal effectiveInterestRate = BigDecimal.ZERO;
        for (DepositAccountInterestRateChartSlabs slab : setOfChartSlabs()) {
            if (slab.slabFields().isBetweenPeriod(periodStartDate, periodEndDate) && slab.slabFields().isAmountBetween(depositAmount)) {

                effectiveInterestRate = slab.slabFields().annualInterestRate();
                Set<DepositAccountInterestIncentives> depositInterestIncentives = slab.setOfIncentives();
                for (DepositAccountInterestIncentives incentives : depositInterestIncentives) {
                    AttributeIncentiveCalculation attributeIncentiveCalculation = AttributeIncentiveCalculationFactory
                            .findAttributeIncentiveCalculation(incentives.interestIncentivesFields().entiryType());
                    IncentiveDTO incentiveDTO = new IncentiveDTO(client, effectiveInterestRate, incentives.interestIncentivesFields());
                    effectiveInterestRate = attributeIncentiveCalculation.calculateIncentive(incentiveDTO);
                }

                // effectiveInterestRate is zero or null then reset to default
                // interest rate.
                if (effectiveInterestRate == null || effectiveInterestRate.compareTo(BigDecimal.ZERO) == 0) {
                    effectiveInterestRate = slab.slabFields().annualInterestRate();
                }
            }
        }

        return effectiveInterestRate;
    }

    public boolean isPrimaryGroupingByAmount() {
        return this.chartFields.isPrimaryGroupingByAmount();
    }
}
