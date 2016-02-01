/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentives;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlabFields;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_account_interest_rate_slab")
public class DepositAccountInterestRateChartSlabs extends AbstractPersistable<Long> {

    @Embedded
    private InterestRateChartSlabFields slabFields;

    @ManyToOne(optional = false)
    @JoinColumn(name = "savings_account_interest_rate_chart_id", referencedColumnName = "id", nullable = false)
    private DepositAccountInterestRateChart depositAccountInterestRateChart;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "depositAccountInterestRateChartSlabs", cascade = CascadeType.ALL, orphanRemoval = true)
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