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
package org.apache.fineract.portfolio.calendar.domain;

import java.util.Collection;
import java.util.List;

import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.group.domain.Group;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    @Query("select ci from CalendarInstance ci where ci.entityId = :entityId and ci.entityTypeId = :entityTypeId")
    CalendarInstance findCalendarInstaneByEntityId(@Param("entityId") Long entityId, @Param("entityTypeId") Integer entityTypeId);

    Collection<CalendarInstance> findByCalendarIdAndEntityTypeId(Long calendarId, Integer entityTypeId);

    /** Should use in clause, can I do it without creating a new class? **/
    @Query("select ci from CalendarInstance ci where ci.entityId in (select loan.id from Loan loan where loan.client.id = :clientId and loan.group.id = :groupId and (loan.loanStatus = 100 or loan.loanStatus = 200 or loan.loanStatus = 300)) and ci.entityTypeId = 3")
    List<CalendarInstance> findCalendarInstancesForActiveLoansByGroupIdAndClientId(@Param("groupId") Long groupId,
            @Param("clientId") Long clientId);
    
    /** 
     *  EntityType = 3 is for loan
     */
    
    @Query("SELECT COUNT(ci.id) FROM CalendarInstance ci, Loan ln WHERE ln.id = ci.entityId AND ci.entityTypeId = 3 AND ci.calendar.id = :calendarId AND ln.loanStatus IN :loanStatuses ") 
    Integer countOfLoansSyncedWithCalendar(@Param("calendarId") Long calendarId, @Param("loanStatuses") Collection<Integer> loanStatuses );

}
