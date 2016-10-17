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

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentives;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.apache.fineract.portfolio.interestratechart.domain.InterestRateChartSlabFields;

@Entity
@Table(name = "m_savings_account_interest_rate_slab")
public class DepositAccountInterestRateChartSlabs extends AbstractPersistableCustom<Long> {

    @Embedded
    private InterestRateChartSlabFields slabFields;

    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_interest_rate_chart_id", referencedColumnName = "id", nullable = false)
    private DepositAccountInterestRateChart depositAccountInterestRateChart;

    @OneToMany(mappedBy = "depositAccountInterestRateChartSlabs", cascade = CascadeType.ALL, orphanRemoval = true, fetch=FetchType.EAGER)
    private Set<DepositAccountInterestIncentives> interestIncentives = new HashSet<>();

    protected DepositAccountInterestRateChartSlabs() {
        //
    }

    private DepositAccountInterestRateChartSlabs(InterestRateChartSlabFields slabFields,
            DepositAccountInterestRateChart depositAccountInterestRateChart, final Set<DepositAccountInterestIncentives> interestIncentives) {
        this.slabFields = slabFields;
        this.depositAccountInterestRateChart = depositAccountInterestRateChart;
        this.interestIncentives = interestIncentives;
    }

    public void setDepositAccountInterestRateChart(DepositAccountInterestRateChart depositAccountInterestRateChart) {
        this.depositAccountInterestRateChart = depositAccountInterestRateChart;
    }

    public Long savingsProductId() {
        return this.depositAccountInterestRateChart.savingsAccountId();
    }

    public InterestRateChartSlabFields slabFields() {
        return this.slabFields;
    }

    public static DepositAccountInterestRateChartSlabs from(InterestRateChartSlab interestRateChartSlab,
            DepositAccountInterestRateChart depositAccountInterestRateChart) {
        InterestRateChartSlabFields slabFields = interestRateChartSlab.slabFields();
        Set<DepositAccountInterestIncentives> depositInterestIncentives = new HashSet<>();
        Set<InterestIncentives> incentives = interestRateChartSlab.setOfInterestIncentives();
        for (InterestIncentives incentive : incentives) {
            depositInterestIncentives.add(DepositAccountInterestIncentives.from(null, incentive.interestIncentivesFields()));
        }
        DepositAccountInterestRateChartSlabs chartSlabs = new DepositAccountInterestRateChartSlabs(slabFields,
                depositAccountInterestRateChart, depositInterestIncentives);
        chartSlabs.updateIncentiveReference();
        return chartSlabs;
    }

    private void updateIncentiveReference() {
        final Set<DepositAccountInterestIncentives> incentives = setOfIncentives();
        for (DepositAccountInterestIncentives depositInterestIncentives : incentives) {
            depositInterestIncentives.updateDepositAccountInterestRateChartSlabs(this);
        }
    }

    public Set<DepositAccountInterestIncentives> setOfIncentives() {
        if (this.interestIncentives == null) {
            this.interestIncentives = new HashSet<>();
        }
        return this.interestIncentives;
    }

    public void updateChartReference(final DepositAccountInterestRateChart chart) {
        this.depositAccountInterestRateChart = chart;
    }
}