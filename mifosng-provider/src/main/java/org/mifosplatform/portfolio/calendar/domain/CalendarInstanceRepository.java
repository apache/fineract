/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.calendar.domain;

import java.util.Collection;
import java.util.List;

import org.mifosplatform.portfolio.group.domain.Group;
import org.mifosplatform.portfolio.loanaccount.domain.Loan;
import org.mifosplatform.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import ch.qos.logback.core.net.server.Client;

public interface CalendarInstanceRepository extends JpaRepository<CalendarInstance, Long>, JpaSpecificationExecutor<CalendarInstance> {

    CalendarInstance findByCalendarIdAndEntityIdAndEntityTypeId(Long calendarId, Long entityId, Integer entityTypeId);

    Collection<CalendarInstance> findByEntityIdAndEntityTypeId(Long entityId, Integer entityTypeId);

    /**
     * @param entityId : Id of {@link Client}, {@link Group}, {@link Loan} or {@link SavingsAccount}.
     * @param entityTypeId: {@link CalendarEntityType}
     * @param calendarTypeId: {@link CalendarType}
     * @return
     */
    CalendarInstance findByEntityIdAndEntityTypeIdAndCalendarTypeId(Long entityId, Integer entityTypeId, Integer calendarTypeId);

    @Query("from CalendarInstance ci where ci.entityId = :entityId and ci.entityTypeId = :entityTypeId")
    CalendarInstance findCalendarInstaneByEntityId(@Param("entityId") Long entityId, @Param("entityTypeId") Integer entityTypeId);

    Collection<CalendarInstance> findByCalendarIdAndEntityTypeId(Long calendarId, Integer entityTypeId);

    /** Should use in clause, can I do it without creating a new class? **/
    @Query("from CalendarInstance ci where ci.entityId in (select id from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and (loan.loanStatus = 100 or loan.loanStatus = 200 or loan.loanStatus = 300)) and ci.entityTypeId = 3")
    List<CalendarInstance> findCalendarInstancesForActiveLoansByGroupIdAndClientId(@Param("groupId") Long groupId,
            @Param("clientId") Long clientId);
    
    /** 
     *  EntityType = 3 is for loan
     */
    
    @Query("SELECT COUNT(ci.id) FROM CalendarInstance ci, Loan ln WHERE ln.id = ci.entityId AND ci.entityTypeId = 3 AND ci.calendar.id = :calendarId AND ln.loanStatus IN :loanStatuses ") 
    Integer countOfLoansSyncedWithCalendar(@Param("calendarId") Long calendarId, @Param("loanStatuses") Collection<Integer> loanStatuses );

}
