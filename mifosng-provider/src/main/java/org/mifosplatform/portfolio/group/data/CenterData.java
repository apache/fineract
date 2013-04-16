/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.group.data;

import java.util.Collection;

import org.mifosplatform.organisation.office.data.OfficeData;
import org.mifosplatform.organisation.staff.data.StaffData;

/**
 * Immutable data object representing groups.
 */
public class CenterData {

    private final Long id;
    private final String name;
    private final String externalId;
    private final Long officeId;
    private final String officeName;
    private final Long staffId;
    private final String staffName;
    private final String hierarchy;

    // associations
    @SuppressWarnings("unused")
    private final Collection<GroupGeneralData> groups;

    // template
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;

    public static CenterData template(final Long officeId, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions) {
        return new CenterData(null, null, null, officeId, null, null, null, null, officeOptions, staffOptions, null);
    }

    public static CenterData withTemplate(final CenterData templateCenter, final CenterData center) {
        return new CenterData(center.id, center.name, center.externalId, center.officeId, center.officeName, center.staffId,
                center.staffName, center.hierarchy, templateCenter.officeOptions, templateCenter.staffOptions, null);
    }

    public static CenterData instance(final Long id, final String name, final String externalId, final Long officeId,
            final String officeName, final Long staffId, final String staffName, final String hierarchy) {

        final Collection<OfficeData> officeOptions = null;
        final Collection<StaffData> staffOptions = null;

        return new CenterData(id, name, externalId, officeId, officeName, staffId, staffName, hierarchy, officeOptions, staffOptions, null);
    }
    
    public static CenterData setGroups(final CenterData centerData, final Collection<GroupGeneralData> groups){
        return new CenterData(centerData.id, centerData.name, centerData.externalId, centerData.officeId, centerData.officeName, centerData.staffId, centerData.staffName, centerData.hierarchy, centerData.officeOptions, centerData.staffOptions, groups);
    }

    private CenterData(final Long id, final String name, final String externalId, final Long officeId, final String officeName,
            final Long staffId, final String staffName, final String hierarchy, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<GroupGeneralData> groups) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
        this.officeId = officeId;
        this.officeName = officeName;
        this.staffId = staffId;
        this.staffName = staffName;
        this.hierarchy = hierarchy;

        this.officeOptions = officeOptions;
        this.staffOptions = staffOptions;
        this.groups = groups;
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