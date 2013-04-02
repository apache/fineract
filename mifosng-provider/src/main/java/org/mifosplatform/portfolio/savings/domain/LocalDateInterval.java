package org.mifosplatform.portfolio.savings.domain;

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

    public boolean contains(final LocalDate target) {
        return isBetweenInclusive(this.startDate, this.endDate, target);
    }

    private boolean isBetweenInclusive(final LocalDate start, final LocalDate end, final LocalDate target) {
        return !target.isBefore(start) && !target.isAfter(end);
    }
}