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
package org.apache.fineract.portfolio.loanaccount.payoffpreview.data;

import java.time.LocalDate;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;

/**
 * A single-unit ISO-8601 period (e.g. {@code P6M}) as accepted by the payoff schedule preview.
 */
public record PayoffSchedulePreviewPeriod(String iso, PeriodFrequencyType unit, int value) {

    /**
     * Counted from {@code startDate} rather than chained period by period, so month-end start dates do not drift (Jan
     * 31 gives Feb 29, Mar 31, ...), matching the seed-date handling of the regular schedule generator.
     */
    public LocalDate addTo(final LocalDate startDate, final int times) {
        final long amount = (long) value * times;
        return switch (unit) {
            case MONTHS -> startDate.plusMonths(amount);
            case WEEKS -> startDate.plusWeeks(amount);
            case DAYS -> startDate.plusDays(amount);
            default -> throw new IllegalStateException("Unsupported period unit: " + unit);
        };
    }
}
