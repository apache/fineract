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
package org.apache.fineract.infrastructure.event.external.repository;

import java.time.LocalDate;
import java.time.OffsetDateTime;
import java.util.List;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEvent;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventStatus;
import org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventView;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface ExternalEventRepository extends JpaRepository<ExternalEvent, Long>, JpaSpecificationExecutor<ExternalEvent> {

    List<ExternalEventView> findByStatusOrderByBusinessDateAscIdAsc(ExternalEventStatus status, Pageable batchSize);

    @Modifying(flushAutomatically = true)
    @Query("delete from ExternalEvent e where e.status = :status and e.businessDate <= :dateForPurgeCriteria")
    void deleteOlderEventsWithSentStatus(@Param("status") ExternalEventStatus status,
            @Param("dateForPurgeCriteria") LocalDate dateForPurgeCriteria);

    @Modifying
    @Query("UPDATE ExternalEvent e SET e.status = org.apache.fineract.infrastructure.event.external.repository.domain.ExternalEventStatus.SENT, e.sentAt = :sentAt WHERE e.id IN :ids")
    void markEventsSent(@Param("ids") List<Long> ids, @Param("sentAt") OffsetDateTime sentAt);
}
