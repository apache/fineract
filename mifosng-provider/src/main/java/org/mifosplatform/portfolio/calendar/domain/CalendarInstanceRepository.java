/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.Collection;
import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CalendarInstanceRepository extends JpaRepository<CalendarInstance, Long>, JpaSpecificationExecutor<CalendarInstance> {

    CalendarInstance findByCalendarIdAndEntityIdAndEntityTypeId(Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarInstance> findByEntityIdAndEntityTypeId(Long entityId, Integer entityTypeId);

    CalendarInstance findByEntityIdAndEntityTypeIdAndCalendarTypeId(Long entityId, Integer entityTypeId, Integer calendarTypeId);

    @Query("from CalendarInstance ci where ci.entityId = :loanId and ci.entityTypeId = :entityTypeId")
    CalendarInstance findCalendarInstaneByLoanId(@Param("loanId") Long loanId, @Param("entityTypeId") Integer entityTypeId);

    Collection<CalendarInstance> findByCalendarIdAndEntityTypeId(Long calendarId, Integer entityTypeId);

    /** Should use in clause, can I do it without creating a new class? **/
    @Query("from CalendarInstance ci where ci.entityId in (select id from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and (loan.loanStatus = 100 or loan.loanStatus = 200 or loan.loanStatus = 300)) and ci.entityTypeId = 3")
    List<CalendarInstance> findCalendarInstancesForActiveLoansByGroupIdAndClientId(@Param("groupId") Long groupId,
            @Param("clientId") Long clientId);
}
