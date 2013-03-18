package org.mifosplatform.portfolio.calendar.domain;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

public interface CalendarInstanceRepository extends JpaRepository<CalendarInstance, Long>, JpaSpecificationExecutor<CalendarInstance> {
    
    CalendarInstance findByCalendarAndEntityIdAndEntityTypeId(Long calendarId, Long entityId, Integer entityTypeId);

}
