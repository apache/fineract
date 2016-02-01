/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.interestratechart.domain;

import java.util.Locale;
import java.util.Map;

import javax.persistence.Embedded;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;

import org.mifosplatform.infrastructure.core.api.JsonCommand;
import org.mifosplatform.infrastructure.core.data.DataValidatorBuilder;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_interest_incentives")
public class InterestIncentives extends AbstractPersistable<Long> {

    @ManyToOne
    @JoinColumn(name = "interest_rate_slab_id", nullable = false)
    private InterestRateChartSlab interestRateChartSlab;

    @Embedded
    private InterestIncentivesFields interestIncentivesFields;

    protected InterestIncentives() {

    }

    /*
     * public InterestIncentives(final InterestRateChartSlab
     * interestRateChartSlab, final InterestIncentivesFields
     * interestIncentivesFields) { this.interestRateChartSlab =
     * interestRateChartSlab; this.interestIncentivesFields =
     * interestIncentivesFields; }
     */

    public InterestRateChartSlab interestRateChartSlab() {
        return this.interestRateChartSlab;
    }

    public void updateInterestRateChartSlab(InterestRateChartSlab interestRateChartSlab) {
        this.interestRateChartSlab = interestRateChartSlab;
    }

    public InterestIncentives(final InterestRateChartSlab interestRateChartSlab, final InterestIncentivesFields interestIncentivesFields) {
        this.interestRateChartSlab = interestRateChartSlab;
        this.interestIncentivesFields = interestIncentivesFields;
        if (this.interestRateChartSlab != null) {
            interestRateChartSlab.addInterestIncentive(this);
        }
    }

    public void update(final JsonCommand command, final Map<String, Object> actualChanges, final DataValidatorBuilder baseDataValidator,
            final Locale locale) {
        this.interestIncentivesFields.update(command, actualChanges, baseDataValidator, locale);
    }

    public InterestIncentivesFields interestIncentivesFields() {
        return this.interestIncentivesFields;
    }

}
