package org.mifosplatform.organisation.staff.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeLookup;

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
    private final boolean isLoanOfficer;

    @SuppressWarnings("unused")
    private final Collection<OfficeLookup> allowedOffices;

    public static StaffData templateData(final StaffData staff, final Collection<OfficeLookup> allowedOffices) {
        return new StaffData(staff.id, staff.firstname, staff.lastname, staff.displayName, staff.officeId, staff.officeName,
                staff.isLoanOfficer, allowedOffices);
    }

    public StaffData(final Long id, final String firstname, final String lastname, final String displayName, final Long officeId,
            final String officeName, final boolean isLoanOfficer, final Collection<OfficeLookup> allowedOffices) {
        this.id = id;
        this.firstname = firstname;
        this.lastname = lastname;
        this.displayName = displayName;
        this.officeName = officeName;
        this.isLoanOfficer = isLoanOfficer;
        this.officeId = officeId;
        this.allowedOffices = allowedOffices;
    }
}