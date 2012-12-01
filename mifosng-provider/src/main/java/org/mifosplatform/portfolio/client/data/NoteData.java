package org.mifosplatform.portfolio.client.data;

import org.joda.time.DateTime;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;

/**
 * Immutable data object represent note or case information about a client, loan or loan transaction.
 */
public class NoteData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long clientId;
    @SuppressWarnings("unused")
    private final Long loanId;
    @SuppressWarnings("unused")
    private final Long loanTransactionId;
    @SuppressWarnings("unused")
    private final EnumOptionData noteType;
    @SuppressWarnings("unused")
    private final String note;
    @SuppressWarnings("unused")
    private final Long createdById;
    @SuppressWarnings("unused")
    private final String createdByUsername;
    @SuppressWarnings("unused")
    private final DateTime createdOn;
    @SuppressWarnings("unused")
    private final Long updatedById;
    @SuppressWarnings("unused")
    private final String updatedByUsername;
    @SuppressWarnings("unused")
    private final DateTime updatedOn;

    public NoteData(final Long id, final Long clientId, final Long loanId, final Long transactionId, final EnumOptionData noteType,
            final String note, final DateTime createdDate, final Long createdById, final String createdByUsername,
            final DateTime lastModifiedDate, final Long lastModifiedById, final String updatedByUsername) {
        this.id = id;
        this.clientId = clientId;
        this.loanId = loanId;
        this.loanTransactionId = transactionId;
        this.noteType = noteType;
        this.note = note;
        this.createdOn = createdDate;
        this.createdById = createdById;
        this.createdByUsername = createdByUsername;
        this.updatedOn = lastModifiedDate;
        this.updatedById = lastModifiedById;
        this.updatedByUsername = updatedByUsername;
    }
}