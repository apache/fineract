/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.core.domain;

import org.apache.commons.lang3.builder.ToStringBuilder;
import org.apache.commons.lang3.builder.ToStringStyle;
import org.joda.time.Days;
import org.joda.time.LocalDate;

public class LocalDateInterval {

    private final LocalDate startDate;
    private final LocalDate endDate;

    public static LocalDateInterval create(final LocalDate startDate, final LocalDate endDate) {
        return new LocalDateInterval(startDate, endDate);
    }

    public LocalDateInterval(final LocalDate startDate, final LocalDate endDate) {
        this.startDate = startDate;
        this.endDate = endDate;
    }

    public LocalDate startDate() {
        return this.startDate;
    }

    public LocalDate endDate() {
        return this.endDate;
    }

    public Integer daysInPeriodInclusiveOfEndDate() {
        return daysBetween() + 1;
    }

    private Integer daysBetween() {
        return Days.daysBetween(this.startDate, this.endDate).getDays();
    }

    public boolean containsPortionOf(final LocalDateInterval interval) {
        return contains(interval.startDate) || contains(interval.endDate);
    }

    public boolean contains(final LocalDateInterval interval) {
        return contains(interval.startDate) && contains(interval.endDate);
    }

    public boolean contains(final LocalDate target) {
        return isBetweenInclusive(this.startDate, this.endDate, target);
    }

    private boolean isBetweenInclusive(final LocalDate start, final LocalDate end, final LocalDate target) {
        return !target.isBefore(start) && !target.isAfter(end);
    }

    public boolean fallsBefore(final LocalDate dateToCheck) {
        return this.endDate.isBefore(dateToCheck);
    }

    @Override
    public String toString() {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.MULTI_LINE_STYLE);
    }
}