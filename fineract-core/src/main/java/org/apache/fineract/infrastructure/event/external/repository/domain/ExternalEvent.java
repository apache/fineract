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
package org.apache.fineract.infrastructure.event.external.repository.domain;

import jakarta.persistence.Basic;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;

@Entity
@Table(name = "m_external_event")
@Getter
@NoArgsConstructor
public class ExternalEvent extends AbstractPersistableCustom<Long> {

    @Column(name = "type", nullable = false)
    private String type;

    @Column(name = "category", nullable = false)
    private String category;

    @Column(name = "schema", nullable = false)
    private String schema;

    @Basic(fetch = FetchType.LAZY)
    @Column(name = "data", nullable = false)
    private byte[] data;

    @Column(name = "created_at", nullable = false)
    private OffsetDateTime createdAt;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    @Setter
    private ExternalEventStatus status;

    @Column(name = "sent_at", nullable = true)
    @Setter
    private OffsetDateTime sentAt;

    @Column(name = "idempotency_key", nullable = false)
    private String idempotencyKey;

    @Column(name = "business_date", nullable = false)
    private LocalDate businessDate;

    @Column(name = "aggregate_root_id", nullable = true)
    private Long aggregateRootId;

    public ExternalEvent(String type, String category, String schema, byte[] data, String idempotencyKey, Long aggregateRootId) {
        this.type = type;
        this.category = category;
        this.schema = schema;
        this.data = data;
        this.idempotencyKey = idempotencyKey;
        this.aggregateRootId = aggregateRootId;
        this.createdAt = DateUtils.getAuditOffsetDateTime();
        this.status = ExternalEventStatus.TO_BE_SENT;
        this.businessDate = DateUtils.getBusinessLocalDate();
    }
}
