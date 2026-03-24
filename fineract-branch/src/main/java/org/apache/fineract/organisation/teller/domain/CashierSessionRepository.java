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
package org.apache.fineract.organisation.teller.domain;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface CashierSessionRepository extends JpaRepository<CashierSession, Long> {

    // Find OPEN session for a cashier+teller+date (enforces one session per station per day)
    @Query("SELECT cs FROM CashierSession cs WHERE cs.cashier.id = :cashierId AND cs.teller.id = :tellerId AND cs.sessionDate = :sessionDate AND cs.status = org.apache.fineract.organisation.teller.domain.CashierSessionStatus.OPEN")
    Optional<CashierSession> findOpenSession(@Param("cashierId") Long cashierId, @Param("tellerId") Long tellerId,
            @Param("sessionDate") LocalDate sessionDate);

    // Find OPEN session by user on any teller (for GL routing)
    @Query("SELECT cs FROM CashierSession cs WHERE cs.userId = :userId AND cs.office.id = :officeId AND cs.sessionDate = :sessionDate AND cs.status = org.apache.fineract.organisation.teller.domain.CashierSessionStatus.OPEN")
    Optional<CashierSession> findOpenSessionByUser(@Param("userId") Long userId, @Param("officeId") Long officeId,
            @Param("sessionDate") LocalDate sessionDate);

    // Find any unsettled OPEN sessions from prior days (blocks opening new session)
    @Query("SELECT cs FROM CashierSession cs WHERE cs.userId = :userId AND cs.sessionDate < :today AND cs.status = org.apache.fineract.organisation.teller.domain.CashierSessionStatus.OPEN")
    List<CashierSession> findUnsettledPriorSessions(@Param("userId") Long userId, @Param("today") LocalDate today);

    // List all sessions for a cashier on a teller
    List<CashierSession> findByCashierIdAndTellerIdOrderBySessionDateDesc(Long cashierId, Long tellerId);
}
