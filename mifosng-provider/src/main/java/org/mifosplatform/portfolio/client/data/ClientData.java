/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.client.data;

import java.util.Collection;
import java.util.List;

import org.apache.commons.lang.StringUtils;
import org.joda.time.LocalDate;
import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.portfolio.group.data.GroupLookup;

/**
 * Immutable data object representing client data.
 */
final public class ClientData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final String firstname;
    private final String middlename;
    private final String lastname;
    private final String fullname;
    private final String displayName;
    private final Long officeId;
    private final String officeName;
    private final LocalDate joinedDate;
    private final String imageKey;
    @SuppressWarnings("unused")
    private final Boolean imagePresent;
    private final Collection<GroupLookup> parentGroups;
    private final List<OfficeLookup> allowedOffices;

    private final ClientData currentChange;
    private final Collection<ClientData> allChanges;

    public static ClientData dataChangeInstance(final Long id, final Long officeId, final String externalId, final String firstname,
            final String middlename, final String lastname, final String fullname, final LocalDate joiningDate) {

        String localDisplayName = null;
        return new ClientData(null, officeId, null, id, firstname, middlename, lastname, fullname, localDisplayName, externalId,
                joiningDate, null, null, null, null, null);
    }

    public static ClientData integrateChanges(final ClientData clientData, ClientData currentChange, final Collection<ClientData> allChanges) {
        return new ClientData(clientData.accountNo, clientData.officeId, clientData.officeName, clientData.id, clientData.firstname,
                clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName, clientData.externalId,
                clientData.joinedDate, clientData.imageKey, clientData.allowedOffices, currentChange, allChanges, clientData.parentGroups);
    }

    public static ClientData template(final Long officeId, final LocalDate joinedDate, final List<OfficeLookup> allowedOffices) {
        return new ClientData(null, officeId, null, null, null, null, null, null, null, null, joinedDate, null, allowedOffices, null, null,
                null);
    }

    public static ClientData templateOnTop(final ClientData clientData, final List<OfficeLookup> allowedOffices) {

        return new ClientData(clientData.accountNo, clientData.officeId, clientData.officeName, clientData.id, clientData.firstname,
                clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName, clientData.externalId,
                clientData.joinedDate, clientData.imageKey, allowedOffices, clientData.currentChange, clientData.allChanges,
                clientData.parentGroups);
    }

    public static ClientData setParentGroups(final ClientData clientData, final Collection<GroupLookup> parentGroups) {
        return new ClientData(clientData.accountNo, clientData.officeId, clientData.officeName, clientData.id, clientData.firstname,
                clientData.middlename, clientData.lastname, clientData.fullname, clientData.displayName, clientData.externalId,
                clientData.joinedDate, clientData.imageKey, clientData.allowedOffices, clientData.currentChange, clientData.allChanges,
                parentGroups);
    }

    public static ClientData clientIdentifier(final Long id, final String accountIdentifier, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final Long officeId,
            final String officeName) {

        return new ClientData(accountIdentifier, officeId, officeName, id, firstname, middlename, lastname, fullname, displayName, null,
                null, null, null, null, null, null);
    }

    public ClientData(final String accountNo, final Long officeId, final String officeName, final Long id, final String firstname,
            final String middlename, final String lastname, final String fullname, final String displayName, final String externalId,
            final LocalDate joinedDate, final String imageKey, final List<OfficeLookup> allowedOffices, final ClientData currentChange,
            final Collection<ClientData> allChanges, final Collection<GroupLookup> parentGroups) {
        this.accountNo = accountNo;
        this.officeId = officeId;
        this.officeName = officeName;
        this.id = id;
        this.firstname = StringUtils.defaultIfEmpty(firstname, null);
        this.middlename = StringUtils.defaultIfEmpty(middlename, null);
        this.lastname = StringUtils.defaultIfEmpty(lastname, null);
        this.fullname = StringUtils.defaultIfEmpty(fullname, null);
        this.displayName = StringUtils.defaultIfEmpty(displayName, null);
        this.externalId = StringUtils.defaultIfEmpty(externalId, null);
        this.joinedDate = joinedDate;
        this.imageKey = imageKey;
        if (imageKey != null) {
            this.imagePresent = Boolean.TRUE;
        } else {
            this.imagePresent = null;
        }
        this.allowedOffices = allowedOffices;
        this.currentChange = currentChange;
        this.allChanges = allChanges;
        this.parentGroups = parentGroups;
    }

    public String displayName() {
        return this.displayName;
    }

    public String officeName() {
        return this.officeName;
    }

    private boolean imageKeyExists() {
        return StringUtils.isNotBlank(this.imageKey);
    }

    public boolean imageKeyDoesNotExist() {
        return !imageKeyExists();
    }

    public String imageKey() {
        return this.imageKey;
    }

    public Long id() {
        return this.id;
    }

    public Long officeId() {
        return this.officeId;
    }

    public ClientData currentChange() {
        return this.currentChange;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public String getFirstname() {
        return this.firstname;
    }

    public String getLastname() {
        return this.lastname;
    }

    public Long getOfficeId() {
        return this.officeId;
    }

    public String getOfficeName() {
        return this.officeName;
    }

    public LocalDate getJoinedDate() {
        return this.joinedDate;
    }

}