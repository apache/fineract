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
import java.math.BigDecimal;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "reconciliation_adjustment")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReconciliationAdjustment extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_import_id", nullable = false)
    private BankStatementImport statementImport;

    @Column(name = "adjustment_type", length = 50, nullable = false)
    private String adjustmentType;

    @Column(name = "description", columnDefinition = "TEXT", nullable = false)
    private String description;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "gl_account_debit")
    private Long glAccountDebit;

    @Column(name = "gl_account_credit")
    private Long glAccountCredit;

    @Column(name = "journal_entry_id")
    private Long journalEntryId;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    @Column(name = "approved", nullable = false)
    private boolean approved = false;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    public static ReconciliationAdjustment create(BankStatementImport statementImport, AdjustmentType adjustmentType, String description,
            BigDecimal amount, Long glAccountDebit, Long glAccountCredit, Long createdBy, OffsetDateTime createdDate) {
        return new ReconciliationAdjustment().setStatementImport(statementImport).setAdjustmentType(adjustmentType.name())
                .setDescription(description).setAmount(amount).setGlAccountDebit(glAccountDebit).setGlAccountCredit(glAccountCredit)
                .setCreatedBy(createdBy).setCreatedDate(createdDate);
    }

    public void approve(Long userId, OffsetDateTime approvedDate) {
        this.approved = true;
        this.approvedBy = userId;
        this.approvedDate = approvedDate;
    }

    public void linkJournalEntry(Long journalEntryId) {
        this.journalEntryId = journalEntryId;
    }
}
