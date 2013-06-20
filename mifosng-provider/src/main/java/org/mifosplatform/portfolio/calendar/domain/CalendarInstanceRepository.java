/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.Collection;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarInstanceRepository extends JpaRepository<CalendarInstance, Long>, JpaSpecificationExecutor<CalendarInstance> {
    
    CalendarInstance findByCalendarAndEntityIdAndEntityTypeId(Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarInstance> findByEntityIdAndEntityTypeId(Long entityId, Integer entityTypeId);

    @Query("from CalendarInstance ci where ci.entityId = :loanId and ci.entityTypeId = :entityTypeId")
    CalendarInstance findCalendarInstaneByLoanId(@Param("loanId") Long loanId, @Param("entityTypeId") Integer entityTypeId);
}
