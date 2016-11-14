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

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.portfolio.interestratechart.domain.InterestIncentivesFields;

@Entity
@Table(name = "m_savings_interest_incentives")
public class DepositAccountInterestIncentives extends AbstractPersistableCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "deposit_account_interest_rate_slab_id", nullable = false)
    private DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs;

    @Embedded
    private InterestIncentivesFields interestIncentivesFields;

    protected DepositAccountInterestIncentives() {

    }

    private DepositAccountInterestIncentives(final DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs,
            final InterestIncentivesFields interestIncentivesFields) {
        this.depositAccountInterestRateChartSlabs = depositAccountInterestRateChartSlabs;
        this.interestIncentivesFields = interestIncentivesFields;
    }

    public static DepositAccountInterestIncentives from(final DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs,
            final InterestIncentivesFields interestIncentivesFields) {
        return new DepositAccountInterestIncentives(depositAccountInterestRateChartSlabs, interestIncentivesFields);
    }

    public void updateDepositAccountInterestRateChartSlabs(DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs) {
        this.depositAccountInterestRateChartSlabs = depositAccountInterestRateChartSlabs;
    }

    public InterestIncentivesFields interestIncentivesFields() {
        return this.interestIncentivesFields;
    }

}
