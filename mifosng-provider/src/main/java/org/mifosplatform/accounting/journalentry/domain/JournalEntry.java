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

    @Column(name = "currency_code", length = 3, nullable = false)
    private String currencyCode;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_id")
    private JournalEntry reversalJournalEntry;

    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @Column(name = "manual_entry", nullable = false)
    private boolean manualEntry = false;

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date transactionDate;

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

    public static JournalEntry createNew(final Office office, final GLAccount glAccount, final String currencyCode,
            final String transactionId, final boolean manualEntry, final Date transactionDate, final JournalEntryType journalEntryType,
            final BigDecimal amount, final String description, final Integer entityType, final Long entityId, final String referenceNumber) {
        return new JournalEntry(office, glAccount, currencyCode, transactionId, manualEntry, transactionDate, journalEntryType.getValue(),
                amount, description, entityType, entityId, referenceNumber);
    }

    protected JournalEntry() {
        //
    }

    public JournalEntry(final Office office, final GLAccount glAccount, final String currencyCode, final String transactionId,
            final boolean manualEntry, final Date transactionDate, final Integer type, final BigDecimal amount, final String description,
            final Integer entityType, final Long entityId, final String referenceNumber) {
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

    public String getReferenceNumber() {
        return this.referenceNumber;
    }

    public String getCurrencyCode() {
        return this.currencyCode;
    }

}