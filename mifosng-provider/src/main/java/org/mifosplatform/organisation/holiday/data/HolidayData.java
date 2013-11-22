package org.mifosplatform.organisation.holiday.data;

import org.joda.time.LocalDate;

public class HolidayData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final String description;
    @SuppressWarnings("unused")
    private final LocalDate fromDate;
    @SuppressWarnings("unused")
    private final LocalDate toDate;
    @SuppressWarnings("unused")
    private final LocalDate repaymentsRescheduledTo;
    @SuppressWarnings("unused")
    private final Long officeId;

    public HolidayData(final Long id, final String name, final String description, final LocalDate fromDate, final LocalDate toDate,
            final LocalDate repaymentsRescheduledTo) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.repaymentsRescheduledTo = repaymentsRescheduledTo;
        this.officeId = null;

    }
}
