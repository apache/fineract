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
package org.apache.fineract.portfolio.floatingrates.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.joda.time.LocalDate;

public class FloatingRateDTO {

    private final boolean isFloatingInterestRate;
    private final LocalDate startDate;
    private BigDecimal interestRateDiff;
    private BigDecimal actualInterestRateDiff;
    private final Collection<FloatingRatePeriodData> baseLendingRatePeriods;

    public FloatingRateDTO(final boolean isFloatingInterestRate, final LocalDate startDate, final BigDecimal interestRateDiff,
            final Collection<FloatingRatePeriodData> baseLendingRatePeriods) {
        this.isFloatingInterestRate = isFloatingInterestRate;
        this.startDate = startDate;
        this.interestRateDiff = interestRateDiff;
        this.actualInterestRateDiff = interestRateDiff;
        this.baseLendingRatePeriods = baseLendingRatePeriods;
    }

    public BigDecimal fetchBaseRate(LocalDate date) {
        BigDecimal rate = null;
        for (FloatingRatePeriodData periodData : this.baseLendingRatePeriods) {
            final LocalDate periodFromDate = new LocalDate(periodData.getFromDate());
            if (periodFromDate.isBefore(date) || periodFromDate.isEqual(date)) {
                rate = periodData.getInterestRate();
                break;
            }
        }
        return rate;
    }

    public void addInterestRateDiff(final BigDecimal diff) {
        this.interestRateDiff = this.interestRateDiff.add(diff);
    }

    public boolean isFloatingInterestRate() {
        return this.isFloatingInterestRate;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public BigDecimal getInterestRateDiff() {
        return this.interestRateDiff;
    }

    public Collection<FloatingRatePeriodData> getBaseLendingRatePeriods() {
        return this.baseLendingRatePeriods;
    }

    public void resetInterestRateDiff() {
        this.interestRateDiff = this.actualInterestRateDiff;
    }

}
