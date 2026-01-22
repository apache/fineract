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
@Table(name = "reconciliation_match")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class ReconciliationMatch extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_import_id", nullable = false)
    private BankStatementImport statementImport;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "bank_transaction_id")
    private BankStatementTransaction bankTransaction;

    @Column(name = "gl_journal_entry_id")
    private Long glJournalEntryId;

    @Column(name = "match_type", length = 50, nullable = false)
    private String matchType;

    @Column(name = "match_confidence", scale = 2, precision = 5)
    private BigDecimal matchConfidence;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    @Column(name = "created_by", nullable = false)
    private Long createdBy;

    @Column(name = "created_date", nullable = false)
    private OffsetDateTime createdDate;

    public static ReconciliationMatch create(BankStatementImport statementImport, BankStatementTransaction bankTransaction,
            Long glJournalEntryId, MatchType matchType, BigDecimal matchConfidence, BigDecimal amount, String notes, Long createdBy,
            OffsetDateTime createdDate) {
        return new ReconciliationMatch().setStatementImport(statementImport).setBankTransaction(bankTransaction)
                .setGlJournalEntryId(glJournalEntryId).setMatchType(matchType.name()).setMatchConfidence(matchConfidence)
                .setAmount(amount).setNotes(notes).setCreatedBy(createdBy).setCreatedDate(createdDate);
    }

    public boolean isAutoMatch() {
        MatchType type = MatchType.fromString(this.matchType);
        return type != null && type.isAuto();
    }

    public boolean isManualMatch() {
        MatchType type = MatchType.fromString(this.matchType);
        return type != null && type.isManual();
    }
}
