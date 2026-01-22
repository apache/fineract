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
import java.time.LocalDate;
import java.time.OffsetDateTime;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;
import org.apache.fineract.organisation.office.domain.Office;

@Entity
@Table(name = "bank_statement_import")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class BankStatementImport extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "gl_account_id", nullable = false)
    private GLAccount glAccount;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @Column(name = "statement_date", nullable = false)
    private LocalDate statementDate;

    @Column(name = "opening_balance", scale = 6, precision = 19, nullable = false)
    private BigDecimal openingBalance;

    @Column(name = "closing_balance", scale = 6, precision = 19, nullable = false)
    private BigDecimal closingBalance;

    @Column(name = "file_name", length = 255)
    private String fileName;

    @Column(name = "file_type", length = 50, nullable = false)
    private String fileType;

    @Column(name = "status", length = 50, nullable = false)
    private String status;

    @Column(name = "imported_by", nullable = false)
    private Long importedBy;

    @Column(name = "imported_date", nullable = false)
    private OffsetDateTime importedDate;

    @Column(name = "reconciled_by")
    private Long reconciledBy;

    @Column(name = "reconciled_date")
    private OffsetDateTime reconciledDate;

    @Column(name = "approved_by")
    private Long approvedBy;

    @Column(name = "approved_date")
    private OffsetDateTime approvedDate;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public static BankStatementImport create(GLAccount glAccount, Office office, LocalDate statementDate,
            BigDecimal openingBalance, BigDecimal closingBalance, String fileName, String fileType, Long importedBy,
            OffsetDateTime importedDate, String notes) {
        return new BankStatementImport().setGlAccount(glAccount).setOffice(office).setStatementDate(statementDate)
                .setOpeningBalance(openingBalance).setClosingBalance(closingBalance).setFileName(fileName).setFileType(fileType)
                .setStatus(ReconciliationStatus.DRAFT.name()).setImportedBy(importedBy).setImportedDate(importedDate).setNotes(notes);
    }

    public void complete(Long userId, OffsetDateTime completedDate) {
        this.status = ReconciliationStatus.COMPLETED.name();
        this.reconciledBy = userId;
        this.reconciledDate = completedDate;
    }

    public void approve(Long userId, OffsetDateTime approvedDate) {
        this.status = ReconciliationStatus.APPROVED.name();
        this.approvedBy = userId;
        this.approvedDate = approvedDate;
    }

    public void cancel() {
        this.status = ReconciliationStatus.CANCELLED.name();
    }

    public boolean isCompleted() {
        return ReconciliationStatus.COMPLETED.name().equals(this.status) || ReconciliationStatus.APPROVED.name().equals(this.status);
    }

    public boolean isApproved() {
        return ReconciliationStatus.APPROVED.name().equals(this.status);
    }

    public boolean isDraft() {
        return ReconciliationStatus.DRAFT.name().equals(this.status);
    }
}
