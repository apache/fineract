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
import org.apache.fineract.infrastructure.core.domain.AbstractPersistableCustom;

@Entity
@Table(name = "bank_statement_transaction")
@Getter
@Setter
@NoArgsConstructor
@Accessors(chain = true)
public class BankStatementTransaction extends AbstractPersistableCustom<Long> {

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "statement_import_id", nullable = false)
    private BankStatementImport statementImport;

    @Column(name = "transaction_date", nullable = false)
    private LocalDate transactionDate;

    @Column(name = "value_date")
    private LocalDate valueDate;

    @Column(name = "description", length = 500, nullable = false)
    private String description;

    @Column(name = "reference_number", length = 100)
    private String referenceNumber;

    @Column(name = "check_number", length = 50)
    private String checkNumber;

    @Column(name = "debit_amount", scale = 6, precision = 19)
    private BigDecimal debitAmount;

    @Column(name = "credit_amount", scale = 6, precision = 19)
    private BigDecimal creditAmount;

    @Column(name = "balance", scale = 6, precision = 19)
    private BigDecimal balance;

    @Column(name = "transaction_type", length = 50)
    private String transactionType;

    @Column(name = "is_matched", nullable = false)
    private boolean matched = false;

    @Column(name = "matched_date")
    private OffsetDateTime matchedDate;

    @Column(name = "matched_by")
    private Long matchedBy;

    @Column(name = "notes", columnDefinition = "TEXT")
    private String notes;

    public static BankStatementTransaction create(BankStatementImport statementImport, LocalDate transactionDate, LocalDate valueDate,
            String description, String referenceNumber, String checkNumber, BigDecimal debitAmount, BigDecimal creditAmount,
            BigDecimal balance, String transactionType, String notes) {
        return new BankStatementTransaction().setStatementImport(statementImport).setTransactionDate(transactionDate)
                .setValueDate(valueDate).setDescription(description).setReferenceNumber(referenceNumber).setCheckNumber(checkNumber)
                .setDebitAmount(debitAmount).setCreditAmount(creditAmount).setBalance(balance).setTransactionType(transactionType)
                .setNotes(notes);
    }

    public void markAsMatched(Long userId, OffsetDateTime matchedDate) {
        this.matched = true;
        this.matchedBy = userId;
        this.matchedDate = matchedDate;
    }

    public void unmarkAsMatched() {
        this.matched = false;
        this.matchedBy = null;
        this.matchedDate = null;
    }

    public BigDecimal getAmount() {
        if (debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0) {
            return debitAmount;
        }
        if (creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0) {
            return creditAmount;
        }
        return BigDecimal.ZERO;
    }

    public boolean isDebit() {
        return debitAmount != null && debitAmount.compareTo(BigDecimal.ZERO) > 0;
    }

    public boolean isCredit() {
        return creditAmount != null && creditAmount.compareTo(BigDecimal.ZERO) > 0;
    }
}
