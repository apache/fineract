/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.journalentry.domain;

import java.math.BigDecimal;
import java.util.Date;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.accounting.glaccount.domain.GLAccount;
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "acc_gl_journal_entry")
public class JournalEntry extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private GLAccount glAccount;

    @SuppressWarnings("unused")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_id")
    private JournalEntry reversalJournalEntry;

    @SuppressWarnings("unused")
    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @SuppressWarnings("unused")
    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @SuppressWarnings("unused")
    @Column(name = "manual_entry", nullable = false)
    private boolean manualEntry = false;

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

    @Column(name = "type_enum", nullable = false)
    private Integer type;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @SuppressWarnings("unused")
    @Column(name = "description", length = 500)
    private String description;

    @SuppressWarnings("unused")
    @Column(name = "entity_type_enum", length = 50)
    private Integer entityType;

    @SuppressWarnings("unused")
    @Column(name = "entity_id")
    private Long entityId;

    public static JournalEntry createNew(final Office office, final GLAccount glAccount, final String transactionId,
            final boolean manualEntry, final Date transactionDate, final JournalEntryType journalEntryType, final BigDecimal amount,
            final String description, final Integer entityType, final Long entityId) {
        return new JournalEntry(office, glAccount, transactionId, manualEntry, transactionDate, journalEntryType.getValue(), amount,
                description, entityType, entityId);
    }

    protected JournalEntry() {
        //
    }

    public JournalEntry(final Office office, final GLAccount glAccount, final String transactionId, final boolean manualEntry,
            final Date transactionDate, final Integer type, final BigDecimal amount, final String description, final Integer entityType,
            final Long entityId) {
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
    }

    public boolean isDebitEntry() {
        return JournalEntryType.DEBIT.getValue().equals(this.type);
    }

    public Integer getType() {
        return this.type;
    }

    public Office getOffice() {
        return this.office;
    }

    public GLAccount getGlAccount() {
        return this.glAccount;
    }

    public Date getTransactionDate() {
        return this.transactionDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setReversalJournalEntry(final JournalEntry reversalJournalEntry) {
        this.reversalJournalEntry = reversalJournalEntry;
    }

    public void setReversed(final boolean reversed) {
        this.reversed = reversed;
    }

}