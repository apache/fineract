/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.teller.data;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.organisation.teller.domain.TellerStatus;

import java.io.Serializable;
import java.util.Collection;
import java.util.Date;

/**
 * {@code TellerData} represents an immutable data object for teller data.
 *
 * @version 1.0
<<<<<<< HEAD
 * @since 2.0.0
 * @see java.io.Serializable
 * @since 2.0.0
 */
public final class TellerData implements Serializable {

    private final Long id;
    private final Long officeId;
    private final Long debitAccountId;
    private final Long creditAccountId;
    private final String name;
    
    private final String description;
    private final LocalDate startDate;
    private final LocalDate endDate;
    private final TellerStatus status;
    private final Boolean hasTransactions;
    private final Boolean hasMappedCashiers;
    
    private String officeName;
    
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;

    /*
     * Sole private CTOR to create a new instance.
     */
    private TellerData(final Long id, final Long officeId, final Long debitAccountId, final Long creditAccountId,
                       final String name, final String description, final LocalDate startDate, final LocalDate endDate,
                       final TellerStatus status, final Boolean hasTransactions, final Boolean hasMappedCashiers) {
        super();
        this.id = id;
        this.officeId = officeId;
        this.debitAccountId = debitAccountId;
        this.creditAccountId = creditAccountId;
        this.name = name;
        this.description = description;
        this.startDate = startDate;
        this.endDate = endDate;
        this.status = status;
        this.hasTransactions = hasTransactions;
        this.hasMappedCashiers = hasMappedCashiers;
        this.officeOptions = null;
        this.staffOptions = null;
    }

    /**
     * Creates a new teller data object.
     *
     * @param id                - id of the teller
     * @param officeId          - id of the related office
     * @param debitAccountId    - id of the debit account to use
     * @param creditAccountId   - id of the credit account to use
     * @param name              - name of the teller
     * @param description       - description of the teller
     * @param startDate         - date when the teller becomes available
     * @param endDate           - date when the teller becomes unavailable
     * @param status            - current state of the teller, eg. active, inactive, pending
     * @param hasTransactions   - indicates that this teller already is used to perform postings
     * @param hasMappedCashiers - indicates that the teller already has @code Cashier}s assigned to it
     * @return the new created {@code TellerData}
     */
    public static TellerData instance(final Long id, final Long officeId, final Long debitAccountId,
                                      final Long creditAccountId, final String name, final String description,
                                      final LocalDate startDate, final LocalDate endDate, final TellerStatus status,
                                      final String officeName,
                                      final Boolean hasTransactions, final Boolean hasMappedCashiers) {
        TellerData tellerData = new TellerData(id, officeId, debitAccountId, creditAccountId, name, description, startDate, endDate,
                status, hasTransactions, hasMappedCashiers);
        tellerData.officeName = officeName;
        return tellerData;
    }
    
    public static TellerData lookup(final Long id, final String name) {
        return new TellerData(id, null, null, null, name, null, null, null, null, null, null);
    }

    public Long getId() {
        return this.id;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public Long getDebitAccountId() {
        return this.debitAccountId;
    }

    public Long getCreditAccountId() {
        return this.creditAccountId;
    }

    public String getName() {
        return this.name;
    }
    
    public String getOfficeName() {
        return this.officeName;
    }

    public String getDescription() {
        return this.description;
    }

    public LocalDate getStartDate() {
        return this.startDate;
    }

    public LocalDate getEndDate() {
        return this.endDate;
    }

    public TellerStatus getStatus() {
        return this.status;
    }

    public Boolean hasTransactions() {
        return this.hasTransactions;
    }

    public Boolean hasMappedCashiers() {
        return this.hasMappedCashiers;
    }
}
