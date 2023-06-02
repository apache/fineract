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
package org.apache.fineract.portfolio.interestratechart.domain;

import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.DataValidatorBuilder;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "m_interest_incentives")
public class InterestIncentives extends AbstractPersistableCustom {

    @ManyToOne
    @JoinColumn(name = "interest_rate_slab_id", nullable = false)
    private InterestRateChartSlab interestRateChartSlab;

    @Embedded
    private InterestIncentivesFields interestIncentivesFields;

    protected InterestIncentives() {

    }

    /*
     * public InterestIncentives(final InterestRateChartSlab interestRateChartSlab, final InterestIncentivesFields
     * interestIncentivesFields) { this.interestRateChartSlab = interestRateChartSlab; this.interestIncentivesFields =
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
