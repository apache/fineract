package org.mifosplatform.useradministration.data;

import java.util.ArrayList;
import java.util.List;

import org.mifosplatform.organisation.office.data.OfficeLookup;

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
    private final List<OfficeLookup> allowedOffices;
    private final List<RoleData> availableRoles;
    private final List<RoleData> selectedRoles;

    public AppUserData(final Long id, final String username, final String email, final Long officeId, final String officeName,
            final String firstname, final String lastname, final List<RoleData> availableRoles, final List<RoleData> selectedRoles) {
        this.id = id;
        this.username = username;
        this.officeId = officeId;
        this.officeName = officeName;
        this.firstname = firstname;
        this.lastname = lastname;
        this.email = email;

        this.allowedOffices = new ArrayList<OfficeLookup>();
        this.availableRoles = availableRoles;
        this.selectedRoles = selectedRoles;
    }

    public AppUserData(final AppUserData user, final List<OfficeLookup> allowedOffices) {
        this.id = user.id;
        this.username = user.username;
        this.officeId = user.officeId;
        this.officeName = user.officeName;
        this.firstname = user.firstname;
        this.lastname = user.lastname;
        this.email = user.email;

        this.allowedOffices = allowedOffices;
        this.availableRoles = user.availableRoles;
        this.selectedRoles = user.selectedRoles;
    }

    public AppUserData(final List<OfficeLookup> allowedOffices, final List<RoleData> availableRoles) {
        this.id = null;
        this.username = null;
        this.officeId = null;
        this.officeName = null;
        this.firstname = null;
        this.lastname = null;
        this.email = null;

        this.allowedOffices = allowedOffices;
        this.availableRoles = availableRoles;
        this.selectedRoles = new ArrayList<RoleData>();
    }

    public boolean hasIdentifyOf(final Long createdById) {
        return this.id.equals(createdById);
    }

    public String username() {
        return this.username;
    }
}