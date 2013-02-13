/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeData;

/**
 * Immutable data object for application user data.
 */
public class AppUserData {

    private final Long id;
    private final String username;
    private final Long officeId;
    private final String officeName;
    private final String firstname;
    private final String lastname;
    private final String email;

    @SuppressWarnings("unused")
    private final Collection<OfficeData> allowedOffices;
    private final Collection<RoleData> availableRoles;
    private final Collection<RoleData> selectedRoles;

    public static AppUserData template(AppUserData user, Collection<OfficeData> officesForDropdown) {
        return new AppUserData(user.id, user.username, user.email, user.officeId, user.officeName, user.firstname, user.lastname,
                user.availableRoles, user.selectedRoles, officesForDropdown);
    }

    public static AppUserData template(final Collection<OfficeData> offices, final Collection<RoleData> availableRoles) {
        return new AppUserData(null, null, null, null, null, null, null, availableRoles, null, offices);
    }
    
    public static AppUserData dropdown(final Long id, final String username) {
        return new AppUserData(id, username, null, null, null, null, null, null, null, null);
    }

    public static AppUserData instance(final Long id, final String username, final String email, final Long officeId,
            final String officeName, final String firstname, final String lastname, final Collection<RoleData> availableRoles,
            final Collection<RoleData> selectedRoles) {
        return new AppUserData(id, username, email, officeId, officeName, firstname, lastname, availableRoles, selectedRoles, null);
    }

    private AppUserData(final Long id, final String username, final String email, final Long officeId, final String officeName,
            final String firstname, final String lastname, final Collection<RoleData> availableRoles,
            final Collection<RoleData> selectedRoles, final Collection<OfficeData> allowedOffices) {
        this.id = id;
        this.username = username;
        this.officeId = officeId;
        this.officeName = officeName;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;
        this.allowedOffices = allowedOffices;
        this.availableRoles = availableRoles;
        this.selectedRoles = selectedRoles;
    }

    public boolean hasIdentifyOf(final Long createdById) {
        return this.id.equals(createdById);
    }

    public String username() {
        return this.username;
    }
}