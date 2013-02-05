package org.mifosplatform.accounting.api.data;

import java.math.BigDecimal;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google will produce json from fields of
 * object.
 */
public class GLJournalEntryData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final String glAccountName;
    @SuppressWarnings("unused")
    private final Long glAccountId;
    @SuppressWarnings("unused")
    private final String glAccountCode;
    @SuppressWarnings("unused")
    private final EnumOptionData glAccountClassification;
    @SuppressWarnings("unused")
    private final LocalDate entryDate;
    @SuppressWarnings("unused")
    private final EnumOptionData entryType;
    @SuppressWarnings("unused")
    private final BigDecimal amount;
    @SuppressWarnings("unused")
    private final String transactionId;
    @SuppressWarnings("unused")
    private final Boolean portfolioGenerated;
    @SuppressWarnings("unused")
    private final String entityType;
    @SuppressWarnings("unused")
    private final Long entityId;
    @SuppressWarnings("unused")
    private final Long createdByUserId;
    @SuppressWarnings("unused")
    private final LocalDate createdDate;
    @SuppressWarnings("unused")
    private final String createdByUserName;
    @SuppressWarnings("unused")
    private final String comments;
    @SuppressWarnings("unused")
    private final Boolean reversed;

    public GLJournalEntryData(Long id, Long officeId, String officeName, String glAccountName, Long glAccountId, String glAccountCode,
            EnumOptionData glAccountClassification, LocalDate entryDate, EnumOptionData entryType, BigDecimal amount, String transactionId,
            Boolean portfolioGenerated, String entityType, Long entityId, Long createdByUserId, LocalDate createdDate,
            String createdByUserName, String comments, Boolean reversed) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.glAccountName = glAccountName;
        this.glAccountId = glAccountId;
        this.glAccountCode = glAccountCode;
        this.glAccountClassification = glAccountClassification;
        this.entryDate = entryDate;
        this.entryType = entryType;
        this.amount = amount;
        this.transactionId = transactionId;
        this.portfolioGenerated = portfolioGenerated;
        this.entityType = entityType;
        this.entityId = entityId;
        this.createdByUserId = createdByUserId;
        this.createdDate = createdDate;
        this.createdByUserName = createdByUserName;
        this.comments = comments;
        this.reversed = reversed;
    }

}