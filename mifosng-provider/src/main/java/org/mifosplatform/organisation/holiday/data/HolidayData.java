package org.mifosplatform.organisation.holiday.data;

import org.joda.time.LocalDate;


public class HolidayData {
    
    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final String name;
    @SuppressWarnings("unused")
    private final LocalDate fromDate;
    @SuppressWarnings("unused")
    private final LocalDate toDate;
    @SuppressWarnings("unused")
    private final LocalDate repaymentsScheduleTO;
    @SuppressWarnings("unused")
    private final Long officeId;
    
    public HolidayData(final Long id, final String name, final LocalDate fromDate, final LocalDate toDate,
            final LocalDate repaymentsScheduleTO) {
        this.id = id;
        this.name = name;
        this.fromDate = fromDate;
        this.toDate = toDate;
        this.repaymentsScheduleTO = repaymentsScheduleTO;
        this.officeId = null;
        
    }
}
