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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import lombok.Setter;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;

@Getter
@Setter
@Entity
@Table(name = "m_journal_entry_aggregation_summary")
public class JournalEntrySummary extends AbstractAuditableWithUTCDateTimeCustom<Long> {

    @Column(name = "product_id", nullable = false)
    private Long product;

    @Column(name = "gl_account_id", nullable = false)
    private Long glAccountId;

    @Column(name = "office_id", nullable = false)
    private Long office;

    @Column(name = "entity_type_enum", nullable = false)
    private Long entityTypeEnum;

    @Column(name = "aggregated_on_date", nullable = false)
    private LocalDate aggregatedOnDate;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    @Column(name = "external_owner_id", nullable = false)
    private Long externalOwnerId;

    @Column(name = "debit_amount")
    private BigDecimal debitAmount;

    @Column(name = "credit_amount")
    private BigDecimal creditAmount;

    @Column(name = "manual_entry", nullable = false)
    private Boolean manualEntry = false;

    @Column(name = "job_execution_id", nullable = false)
    private Long jobExecutionId;

}
