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
package org.apache.fineract.accounting.reconciliation.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "reconciliation_audit_log")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReconciliationAuditLog extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_import_id", nullable = false)
    private BankStatementImport statementImport;

    @Column(name = "action", length = 50, nullable = false)
    private String action;

    @Column(name = "description", columnDefinition = "TEXT")
    private String description;

    @Column(name = "performed_by", nullable = false)
    private Long performedBy;

    @Column(name = "performed_date", nullable = false)
    private OffsetDateTime performedDate;

    @Column(name = "old_value", columnDefinition = "TEXT")
    private String oldValue;

    @Column(name = "new_value", columnDefinition = "TEXT")
    private String newValue;

    public static ReconciliationAuditLog create(BankStatementImport statementImport, String action, String description, Long performedBy,
            OffsetDateTime performedDate, String oldValue, String newValue) {
        return new ReconciliationAuditLog().setStatementImport(statementImport).setAction(action).setDescription(description)
                .setPerformedBy(performedBy).setPerformedDate(performedDate).setOldValue(oldValue).setNewValue(newValue);
    }
}
