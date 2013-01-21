package org.mifosplatform.accounting.domain;

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
import org.mifosplatform.infrastructure.core.domain.AbstractAuditableCustom;
import org.mifosplatform.organisation.office.domain.Office;
import org.mifosplatform.useradministration.domain.AppUser;

@Entity
@Table(name = "acc_gl_journal_entry")
public class GLJournalEntry extends AbstractAuditableCustom<AppUser, Long> {

    @ManyToOne
    @JoinColumn(name = "office_id", nullable = false)
    private Office office;

    @ManyToOne
    @JoinColumn(name = "account_id", nullable = false)
    private GLAccount glAccount;

    @SuppressWarnings("unused")
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reversal_id")
    private GLJournalEntry reversalJournalEntry;

    @SuppressWarnings("unused")
    @Column(name = "transaction_id", nullable = false, length = 50)
    private String transactionId;

    @SuppressWarnings("unused")
    @Column(name = "reversed", nullable = false)
    private boolean reversed = false;

    @SuppressWarnings("unused")
    @Column(name = "portfolio_generated", nullable = false)
    private boolean portfolioGenerated = false;

    @Column(name = "entry_date")
    @Temporal(TemporalType.DATE)
    private Date entryDate;

    @Column(name = "type_enum", nullable = false, length = 50)
    private Integer type;

    @Column(name = "amount", scale = 6, precision = 19, nullable = false)
    private BigDecimal amount;

    @SuppressWarnings("unused")
    @Column(name = "description", length = 500)
    private String description;

    @SuppressWarnings("unused")
    @Column(name = "entity_type", length = 50)
    private String entityType;

    @SuppressWarnings("unused")
    @Column(name = "entity_id")
    private Long entityId;

    public static GLJournalEntry createNew(Office office, GLAccount glAccount, String transactionId, boolean portfolioGenerated,
            Date entryDate, JournalEntryType journalEntryType, BigDecimal amount, String description, String entityType, Long entityId) {
        return new GLJournalEntry(office, glAccount, transactionId, portfolioGenerated, entryDate, journalEntryType.getValue(), amount,
                description, entityType, entityId);
    }

    protected GLJournalEntry() {
        //
    }

    public GLJournalEntry(Office office, GLAccount glAccount, String transactionId, boolean portfolioGenerated, Date entryDate,
            Integer type, BigDecimal amount, String description, String entityType, Long entityId) {
        this.office = office;
        this.glAccount = glAccount;
        this.reversalJournalEntry = null;
        this.transactionId = transactionId;
        this.reversed = false;
        this.portfolioGenerated = portfolioGenerated;
        this.entryDate = entryDate;
        this.type = type;
        this.amount = amount;
        this.description = StringUtils.defaultIfEmpty(description, null);
        this.entityType = StringUtils.defaultIfEmpty(entityType, null);
        this.entityId = entityId;
    }

    public boolean isDebitEntry() {
        return JournalEntryType.DEBIT.getValue().equals(type);
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

    public Date getEntryDate() {
        return this.entryDate;
    }

    public BigDecimal getAmount() {
        return this.amount;
    }

    public void setReversalJournalEntry(GLJournalEntry reversalJournalEntry) {
        this.reversalJournalEntry = reversalJournalEntry;
    }

    public void setReversed(boolean reversed) {
        this.reversed = reversed;
    }

}