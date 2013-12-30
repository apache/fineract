/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

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

    protected DepositAccountInterestRateChartSlabs() {
        //
    }


    private DepositAccountInterestRateChartSlabs(InterestRateChartSlabFields slabFields,
            DepositAccountInterestRateChart depositAccountInterestRateChart) {
        this.slabFields = slabFields;
        this.depositAccountInterestRateChart = depositAccountInterestRateChart;
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
    
    public static DepositAccountInterestRateChartSlabs from(InterestRateChartSlabFields slabFields,
            DepositAccountInterestRateChart depositAccountInterestRateChart){
        return new DepositAccountInterestRateChartSlabs(slabFields, depositAccountInterestRateChart);
    }
    
    public void updateChartReference(final DepositAccountInterestRateChart chart){
        this.depositAccountInterestRateChart = chart;
    }
}