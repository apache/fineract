package org.mifosplatform.accounting.api.data;

import java.util.ArrayList;
import java.util.List;

import org.joda.time.LocalDate;
import org.mifosplatform.organisation.office.data.OfficeLookup;

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
    private final Long createdByUserId;
    @SuppressWarnings("unused")
    private final String createdByUsername;
    @SuppressWarnings("unused")
    private final Long lastUpdatedByUserId;
    @SuppressWarnings("unused")
    private final String lastUpdatedByUsername;
    @SuppressWarnings("unused")
    private final String comments;

    private List<OfficeLookup> allowedOffices = new ArrayList<OfficeLookup>();

    public GLClosureData(Long id, Long officeId, String officeName, LocalDate closingDate, boolean deleted, LocalDate createdDate,
            LocalDate lastUpdatedDate, Long createdByUserId, String createdByUsername, Long lastUpdatedByUserId,
            String lastUpdatedByUsername, String comments) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.closingDate = closingDate;
        this.deleted = deleted;
        this.createdDate = createdDate;
        this.lastUpdatedDate = lastUpdatedDate;
        this.createdByUserId = createdByUserId;
        this.createdByUsername = createdByUsername;
        this.lastUpdatedByUserId = lastUpdatedByUserId;
        this.lastUpdatedByUsername = lastUpdatedByUsername;
        this.comments = comments;
    }

    public List<OfficeLookup> getAllowedOffices() {
        return this.allowedOffices;
    }

    public void setAllowedOffices(List<OfficeLookup> allowedOffices) {
        this.allowedOffices = allowedOffices;
    }

}