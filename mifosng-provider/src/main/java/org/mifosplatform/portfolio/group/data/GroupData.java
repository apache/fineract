/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.portfolio.client.data.ClientLookup;
import org.mifosplatform.portfolio.group.domain.GroupLevel;

/**
 * Immutable data object representing groups.
 */
public class GroupData {

    private final Long id;
    private final String name;
    private final String externalId;
    private final Long officeId;
    private final String officeName;
    private final GroupLevelData groupLevelData; 

    private final Collection<ClientLookup> clientMembers;
    @SuppressWarnings("unused")
    private final Collection<ClientLookup> allowedClients;
    @SuppressWarnings("unused")
    private final Collection<OfficeLookup> allowedOffices;
    @SuppressWarnings("unused")
    private final Collection<GroupLookupData> allowedParentGroups;

    public GroupData(final Long id, final Long officeId, final String officeName, final String name, final String externalId ) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.name = name;
        this.externalId = externalId;

        this.groupLevelData = null;
        this.clientMembers = null;
        this.allowedClients = null;
        this.allowedOffices = null;
        this.allowedParentGroups = null;
    }

    public GroupData(final GroupData group, final Collection<ClientLookup> clientMembers, final Collection<ClientLookup> allowedClients,
            final Collection<OfficeLookup> allowedOffices, final Collection<GroupLookupData> allowedParentGroups , final GroupLevelData groupLevelData) {
        this.id = group.getId();
        this.officeId = group.getOfficeId();
        this.officeName = group.getOfficeName();
        this.name = group.getName();
        this.externalId = group.getExternalId();

        this.groupLevelData = groupLevelData;
        this.clientMembers = clientMembers;
        this.allowedClients = allowedClients;
        this.allowedOffices = allowedOffices;
        this.allowedParentGroups = allowedParentGroups;
    }

    public GroupData(final Long officeId, final Collection<ClientLookup> allowedClients, final Collection<OfficeLookup> allowedOffices,
            final Collection<GroupLookupData> allowedParentGroups , final GroupLevelData groupLevelData ) {
        this.id = null;
        this.officeId = officeId;
        this.officeName = null;
        this.name = null;
        this.externalId = null;
        this.clientMembers = null;
        this.groupLevelData = groupLevelData;
        this.allowedClients = allowedClients;
        this.allowedOffices = allowedOffices;
        this.allowedParentGroups = allowedParentGroups;

    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getExternalId() {
        return externalId;
    }

    public Long getOfficeId() {
        return officeId;
    }

    public String getOfficeName() {
        return officeName;
    }

    public Collection<ClientLookup> clientMembers() {
        return this.clientMembers;
    }
}