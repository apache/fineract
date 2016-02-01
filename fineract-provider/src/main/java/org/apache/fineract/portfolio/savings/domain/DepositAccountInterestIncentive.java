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

import org.mifosplatform.portfolio.interestratechart.domain.InterestIncentivesFields;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_deposit_account_interest_incentives")
public class DepositAccountInterestIncentive extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "deposit_account_interest_rate_slab_id", nullable = false)
    private DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs;

    @Embedded
    private InterestIncentivesFields interestIncentivesFields;

    protected DepositAccountInterestIncentive() {
        // TODO Auto-generated constructor stub
    }

    public DepositAccountInterestIncentive(final DepositAccountInterestRateChartSlabs depositAccountInterestRateChartSlabs,
            final InterestIncentivesFields interestIncentivesFields) {
        this.depositAccountInterestRateChartSlabs = depositAccountInterestRateChartSlabs;
        this.interestIncentivesFields = interestIncentivesFields;
    }

}
