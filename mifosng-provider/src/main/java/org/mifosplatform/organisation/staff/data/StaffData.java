/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.organisation.staff.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeData;

/**
 * Immutable data object representing staff data.
 */
public class StaffData {

    private final Long id;
    private final String firstname;
    private final String lastname;
    private final String displayName;
    private final Long officeId;
    private final String officeName;
    private final Boolean isLoanOfficer;

    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;

    public static StaffData templateData(final StaffData staff, final Collection<OfficeData> allowedOffices) {
        return new StaffData(staff.id, staff.firstname, staff.lastname, staff.displayName, staff.officeId, staff.officeName,
                staff.isLoanOfficer, allowedOffices);
    }

    public static StaffData lookup(final Long id, final String displayName) {
        return new StaffData(id, null, null, displayName, null, null, null, null);
    }

    public static StaffData instance(final Long id, final String firstname, final String lastname, final String displayName,
            final Long officeId, final String officeName, final Boolean isLoanOfficer) {
        return new StaffData(id, firstname, lastname, displayName, officeId, officeName, isLoanOfficer, null);
    }

    private StaffData(final Long id, final String firstname, final String lastname, final String displayName, final Long officeId,
            final String officeName, final Boolean isLoanOfficer, final Collection<OfficeData> allowedOffices) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.displayName = displayName;
        this.officeName = officeName;
        this.isLoanOfficer = isLoanOfficer;
        this.officeId = officeId;
        this.allowedOffices = allowedOffices;
    }

    public Long getId() {
        return this.id;
    }

    public String getDisplayName() {
        return this.displayName;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public String getOfficeName() {
        return this.officeName;
    }
}