package org.mifosplatform.accounting.api.data;

import org.joda.time.LocalDate;

/**
 * Immutable object representing a General Ledger Account
 * 
 * Note: no getter/setters required as google-gson will produce json from fields
 * of object.
 */
public class GLClosureData {

    @SuppressWarnings("unused")
    private final Long id;
    @SuppressWarnings("unused")
    private final Long officeId;
    @SuppressWarnings("unused")
    private final String officeName;
    @SuppressWarnings("unused")
    private final LocalDate closingDate;
    @SuppressWarnings("unused")
    private final boolean deleted;
    @SuppressWarnings("unused")
    private final LocalDate createdDate;
    @SuppressWarnings("unused")
    private final LocalDate lastUpdatedDate;
    @SuppressWarnings("unused")
    private final Long creatingByUserId;
    @SuppressWarnings("unused")
    private final Long lastUpdatedByUserId;
    @SuppressWarnings("unused")
    private final String comments;

    public GLClosureData(Long id, Long officeId, String officeName, LocalDate closingDate, boolean deleted, LocalDate createdDate,
            LocalDate lastUpdatedDate, Long creatingByUserId, Long lastUpdatedByUserId, String comments) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.closingDate = closingDate;
        this.deleted = deleted;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.creatingByUserId = creatingByUserId;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.comments = comments;
    }

}