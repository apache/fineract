/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.domain;

import java.math.BigDecimal;
import java.util.HashSet;
import java.util.Set;

import javax.persistence.CascadeType;
import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;

import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.mifosplatform.portfolio.client.domain.Client;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChart;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartFields;
import org.mifosplatform.portfolio.interestratechart.domain.InterestRateChartSlab;
import org.mifosplatform.portfolio.interestratechart.incentive.AttributeIncentiveCalculation;
import org.mifosplatform.portfolio.interestratechart.incentive.AttributeIncentiveCalculationFactory;
import org.mifosplatform.portfolio.interestratechart.incentive.IncentiveDTO;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_savings_account_interest_rate_chart")
public class DepositAccountInterestRateChart extends AbstractPersistable<Long> {

    @Embedded
    private InterestRateChartFields chartFields;

    @OneToOne
    @JoinColumn(name = "savings_account_id", nullable = false)
    private SavingsAccount account;

    @LazyCollection(LazyCollectionOption.FALSE)
    @OneToMany(mappedBy = "depositAccountInterestRateChart", cascade = CascadeType.ALL, orphanRemoval = true)
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
            if (interestRateChartSlab.getId().equals(chartSlabId)) { return interestRateChartSlab; }
        }
        return null;
    }

    public LocalDate getFromDateAsLocalDate() {
        return this.chartFields.getFromDateAsLocalDate();
    }

    public LocalDate getEndDateAsLocalDate() {
        return this.chartFields.getEndDateAsLocalDate();
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
}