/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.joda.time.LocalDate;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.staff.data.StaffData;

/**
 * Immutable data object representing groups.
 */
public class CenterData {

    private final Long id;
    private final String externalId;

    private final EnumOptionData status;
    @SuppressWarnings("unused")
    private final boolean active;
    private final LocalDate activationDate;

    private final String name;

    private final Long officeId;
    private final String officeName;
    private final Long staffId;
    private final String staffName;
    private final String hierarchy;

    // associations
    private final Collection<GroupGeneralData> groupMembers;

    // template
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    private final Collection<GroupGeneralData> groupMembersOptions;

    public static CenterData template(final Long officeId, final LocalDate activationDate, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<GroupGeneralData> groupMembersOptions) {
        return new CenterData(null, null, null, null, activationDate, officeId, null, null, null, null, null, officeOptions, staffOptions,
                groupMembersOptions);
    }

    public static CenterData withTemplate(final CenterData templateCenter, final CenterData center) {
        return new CenterData(center.id, center.name, center.externalId, center.status, center.activationDate, center.officeId,
                center.officeName, center.staffId, center.staffName, center.hierarchy, center.groupMembers, templateCenter.officeOptions,
                templateCenter.staffOptions, templateCenter.groupMembersOptions);
    }

    public static CenterData instance(final Long id, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long staffId, final String staffName,
            final String hierarchy) {

        final Collection<GroupGeneralData> groupMembers = null;
        final Collection<OfficeData> officeOptions = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<GroupGeneralData> groupMembersOptions = null;

        return new CenterData(id, name, externalId, status, activationDate, officeId, officeName, staffId, staffName, hierarchy,
                groupMembers, officeOptions, staffOptions, groupMembersOptions);
    }

    public static CenterData setGroups(final CenterData centerData, final Collection<GroupGeneralData> groupMembers) {
        return new CenterData(centerData.id, centerData.name, centerData.externalId, centerData.status, centerData.activationDate,
                centerData.officeId, centerData.officeName, centerData.staffId, centerData.staffName, centerData.hierarchy, groupMembers,
                centerData.officeOptions, centerData.staffOptions, centerData.groupMembersOptions);
    }

    private CenterData(final Long id, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long staffId, final String staffName,
            final String hierarchy, final Collection<GroupGeneralData> groupMembers, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<GroupGeneralData> groupMembersOptions) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
        this.status = status;
        if (status != null) {
            this.active = status.getId().equals(300L);
        } else {
            this.active = false;
        }
        this.activationDate = activationDate;
        this.officeId = officeId;
        this.officeName = officeName;
        this.staffId = staffId;
        this.staffName = staffName;
        this.hierarchy = hierarchy;

        this.groupMembers = groupMembers;

        //
        this.officeOptions = officeOptions;
        this.staffOptions = staffOptions;
        this.groupMembersOptions = groupMembersOptions;
    }

    public Long officeId() {
        return this.officeId;
    }

    public Long staffId() {
        return this.staffId;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }
}