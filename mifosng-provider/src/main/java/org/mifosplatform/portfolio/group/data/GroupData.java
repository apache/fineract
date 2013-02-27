/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeLookup;
import org.mifosplatform.organisation.staff.data.StaffData;
import org.mifosplatform.portfolio.client.data.ClientLookup;

/**
 * Immutable data object representing groups.
 */
public class GroupData {

    private final Long id;
    private final String name;
    private final String externalId;
    private final Long officeId;
    private final String officeName;
    private final Long staffId;
    private final String staffName;
    private final Long groupLevel;
    private final Long parentId;
    private final String parentName;
    private final String hierarchy;
    @SuppressWarnings("unused")
    private final GroupSummaryData groupSummaryData;
    @SuppressWarnings("unused")
    private final GroupLevelData groupLevelData;
    private final Collection<ClientLookup> clientMembers;
    @SuppressWarnings("unused")
    private final Collection<ClientLookup> allowedClients;
    @SuppressWarnings("unused")
    private final Collection<OfficeLookup> allowedOffices;
    @SuppressWarnings("unused")
    private final Collection<GroupLookupData> allowedParentGroups;
    @SuppressWarnings("unused")
    private final Collection<StaffData> allowedStaffs;
    @SuppressWarnings("unused")
    private final Collection<GroupLookupData> childGroups;

    public GroupData(final Long id, final Long officeId, final String officeName, final String name, final String externalId,
            final Long groupLevel, final Long parentId, final String parentName, final Long staffId, final String staffName , final String hierarchy) {
        this.id = id;
        this.officeId = officeId;
        this.officeName = officeName;
        this.name = name;
        this.externalId = externalId;
        this.groupLevel = groupLevel;
        this.parentId = parentId;
        this.parentName = parentName;
        this.staffId = staffId;
        this.staffName = staffName;
        this.hierarchy = hierarchy;
        this.groupSummaryData = null;
        this.groupLevelData = null;
        this.clientMembers = null;
        this.allowedClients = null;
        this.allowedOffices = null;
        this.allowedParentGroups = null;
        this.allowedStaffs = null;
        this.childGroups = null;
    }

    public GroupData(final GroupData group, final Collection<ClientLookup> clientMembers, final Collection<ClientLookup> allowedClients,
            final Collection<OfficeLookup> allowedOffices, final Collection<GroupLookupData> allowedParentGroups,
            final GroupLevelData groupLevelData, final Collection<StaffData> allowedStaffs, final Collection<GroupLookupData> childGroups , final GroupSummaryData groupSummaryData) {
        this.id = group.getId();
        this.officeId = group.getOfficeId();
        this.officeName = group.getOfficeName();
        this.name = group.getName();
        this.externalId = group.getExternalId();
        this.groupLevel = group.getGroupLevel();
        this.parentId = group.getParentId();
        this.parentName = group.getParentName();
        this.staffId = group.getStaffId();
        this.staffName = group.getStaffName();
        this.hierarchy = group.getHierarchy();

        this.groupSummaryData = groupSummaryData;
        this.groupLevelData = groupLevelData;
        this.clientMembers = clientMembers;
        this.allowedClients = allowedClients;
        this.allowedOffices = allowedOffices;
        this.allowedParentGroups = allowedParentGroups;
        this.allowedStaffs = allowedStaffs;
        this.childGroups = childGroups;
    }

    public GroupData(final Long officeId, final Collection<ClientLookup> allowedClients, final Collection<OfficeLookup> allowedOffices,
            final Collection<GroupLookupData> allowedParentGroups, final GroupLevelData groupLevelData,
            final Collection<StaffData> allowedStaffs) {
        this.id = null;
        this.officeId = officeId;
        this.officeName = null;
        this.name = null;
        this.externalId = null;
        this.clientMembers = null;
        this.groupLevel = null;
        this.parentId = null;
        this.parentName = null;
        this.staffId = null;
        this.staffName = null;
        this.hierarchy = null;
        this.groupSummaryData = null;
        this.groupLevelData = groupLevelData;
        this.allowedClients = allowedClients;
        this.allowedOffices = allowedOffices;
        this.allowedParentGroups = allowedParentGroups;
        this.allowedStaffs = allowedStaffs;
        this.childGroups = null;
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

    public Long getGroupLevel() {
        return this.groupLevel;
    }

    public Long getParentId() {
        return this.parentId;
    }

    public Long getStaffId() {
        return this.staffId;
    }

    public String getStaffName() {
        return this.staffName;
    }

    public String getParentName() {
        return this.parentName;
    }

    
    public String getHierarchy() {
        return this.hierarchy;
    }

    public Collection<ClientLookup> clientMembers() {
        return this.clientMembers;
    }
}