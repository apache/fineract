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
package org.apache.fineract.portfolio.floatingrates.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateDTO;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRatePeriodData;

@Entity
@Table(name = "m_floating_rates_periods")
public class FloatingRatePeriod extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @ManyToOne
    @JoinColumn(name = "floating_rates_id", nullable = false)
    private FloatingRate floatingRate;

    @Column(name = "from_date", nullable = false)
    private LocalDate fromDate;

    @Column(name = "interest_rate", scale = 6, precision = 19, nullable = false)
    private BigDecimal interestRate;

    @Column(name = "is_differential_to_base_lending_rate", nullable = false)
    private boolean isDifferentialToBaseLendingRate;

    @Column(name = "is_active", nullable = false)
    private boolean isActive;

    /*
     * Deprecated since common Auditable fields were introduced. Columns and data left untouched to help migration.
     *
     * @Column(name = "created_date", nullable = false) private LocalDateTime createdOn;
     *
     * @Column(name = "lastmodified_date", nullable = false) private LocalDateTime modifiedOn;
     */

    public FloatingRatePeriod() {

    }

    public FloatingRatePeriod(LocalDate fromDate, BigDecimal interestRate, boolean isDifferentialToBaseLendingRate, boolean isActive) {
        this.fromDate = fromDate;
        this.interestRate = interestRate;
        this.isDifferentialToBaseLendingRate = isDifferentialToBaseLendingRate;
        this.isActive = isActive;
    }

    public void updateFloatingRate(FloatingRate floatingRate) {
        this.floatingRate = floatingRate;
    }

    public FloatingRate getFloatingRate() {
        return this.floatingRate;
    }

    public LocalDate getFromDate() {
        return this.fromDate;
    }

    public BigDecimal getInterestRate() {
        return this.interestRate;
    }

    public boolean isDifferentialToBaseLendingRate() {
        return this.isDifferentialToBaseLendingRate;
    }

    public boolean isActive() {
        return this.isActive;
    }

    public void setActive(boolean isActive) {
        this.isActive = isActive;
    }

    public LocalDate fetchFromDate() {
        return fromDate;
    }

    public FloatingRatePeriodData toData(final FloatingRateDTO floatingRateDTO) {

        BigDecimal interest = getInterestRate().add(floatingRateDTO.getInterestRateDiff());
        if (isDifferentialToBaseLendingRate()) {
            interest = interest.add(floatingRateDTO.fetchBaseRate(fetchFromDate()));
        }

        final LocalDate fromDate = getFromDate();
        return new FloatingRatePeriodData(getId(), fromDate, interest, isDifferentialToBaseLendingRate(), isActive());
    }

}
