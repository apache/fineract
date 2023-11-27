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
package org.apache.fineract.accounting.journalentry.domain;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import java.math.BigDecimal;
import java.time.LocalDate;
import lombok.Getter;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.accounting.glaccount.domain.GLAccount;
import org.apache.fineract.infrastructure.core.domain.AbstractAuditableWithUTCDateTimeCustom;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.organisation.office.domain.Office;
import org.apache.fineract.portfolio.paymentdetail.domain.PaymentDetail;

@Entity
@Getter
@Table(name = "acc_gl_journal_entry")
public class JournalEntry extends AbstractAuditableWithUTCDateTimeCustom {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne()
    @JoinColumn(name = "payment_details_id")
    private PaymentDetail paymentDetail;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private GLAccount glAccount;

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_id")
    private JournalEntry reversalJournalEntry;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Column(name = "loan_transaction_id")
    private Long loanTransactionId;

    @Column(name = "savings_transaction_id")
    private Long savingsTransactionId;

    @Column(name = "client_transaction_id")
    private Long clientTransactionId;

    @Column(name = "share_transaction_id")
    private Long shareTransactionId;

    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "manual_entry", nullable = false)
    private boolean manualEntry = false;

    @Column(name = "entry_date")
    private LocalDate transactionDate;

    @Column(name = "type_enum", nullable = false)
    private Integer type;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @Column(name = "description", length = 500)
    private String description;

    @Column(name = "entity_type_enum", length = 50)
    private Integer entityType;

    @Column(name = "entity_id")
    private Long entityId;

    @Column(name = "ref_num")
    private String referenceNumber;

    @Column(name = "submitted_on_date", nullable = false)
    private LocalDate submittedOnDate;

    protected JournalEntry() {
        //
    }

    protected JournalEntry(final Office office, final PaymentDetail paymentDetail, final GLAccount glAccount, final String currencyCode,
            final String transactionId, final boolean manualEntry, final LocalDate transactionDate, final Integer type,
            final BigDecimal amount, final String description, final Integer entityType, final Long entityId, final String referenceNumber,
            final Long loanTransactionId, final Long savingsTransactionId, final Long clientTransactionId, final Long shareTransactionId) {
        this.office = office;
        this.glAccount = glAccount;
        this.reversalJournalEntry = null;
        this.transactionId = transactionId;
        this.reversed = false;
        this.manualEntry = manualEntry;
        this.transactionDate = transactionDate;
        this.type = type;
        this.amount = amount;
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.entityType = entityType;
        this.entityId = entityId;
        this.referenceNumber = referenceNumber;
        this.currencyCode = currencyCode;
        this.loanTransactionId = loanTransactionId;
        this.savingsTransactionId = savingsTransactionId;
        this.clientTransactionId = clientTransactionId;
        this.paymentDetail = paymentDetail;
        this.shareTransactionId = shareTransactionId;
        this.submittedOnDate = DateUtils.getBusinessLocalDate();
    }

    public static JournalEntry createNew(final Office office, final PaymentDetail paymentDetail, final GLAccount glAccount,
            final String currencyCode, final String transactionId, final boolean manualEntry, final LocalDate transactionDate,
            final JournalEntryType journalEntryType, final BigDecimal amount, final String description, final Integer entityType,
            final Long entityId, final String referenceNumber, final Long loanTransaction, final Long savingsTransaction,
            final Long clientTransaction, Long shareTransactionId) {
        return new JournalEntry(office, paymentDetail, glAccount, currencyCode, transactionId, manualEntry, transactionDate,
                journalEntryType.getValue(), amount, description, entityType, entityId, referenceNumber, loanTransaction,
                savingsTransaction, clientTransaction, shareTransactionId);
    }

    public boolean isDebitEntry() {
        return JournalEntryType.DEBIT.getValue().equals(this.type);
    }

    public void setReversalJournalEntry(final JournalEntry reversalJournalEntry) {
        this.reversalJournalEntry = reversalJournalEntry;
    }

    public void setReversed(final boolean reversed) {
        this.reversed = reversed;
    }
}
