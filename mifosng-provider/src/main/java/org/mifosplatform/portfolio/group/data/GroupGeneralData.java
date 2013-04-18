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
import org.mifosplatform.portfolio.client.data.ClientData;

/**
 * Immutable data object representing a general group (so may or may not have a
 * parent).
 */
public class GroupGeneralData {

    private final Long id;
    private final String name;
    private final String externalId;

    private final EnumOptionData status;
    @SuppressWarnings("unused")
    private final Boolean active;
    private final LocalDate activationDate;

    private final Long officeId;
    private final String officeName;
    private final Long centerId;
    private final String centerName;
    private final Long staffId;
    private final String staffName;
    private final String hierarchy;

    // associations
    private final Collection<ClientData> clientMembers;

    // template
    private final Collection<CenterData> centerOptions;
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    private final Collection<ClientData> clientOptions;

    public static GroupGeneralData lookup(final Long groupId, final String groupName) {
        final Collection<ClientData> clientMembers = null;

        return new GroupGeneralData(groupId, groupName, null, null, null, null, null, null, null, null, null, null, clientMembers, null,
                null, null, null);
    }

    public static GroupGeneralData template(final Long officeId, final Long centerId, final String centerName, final Long staffId,
            final String staffName, final Collection<CenterData> centerOptions, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<ClientData> clientOptions) {

        final Collection<ClientData> clientMembers = null;

        return new GroupGeneralData(null, null, null, null, null, officeId, null, centerId, centerName, staffId, staffName, null,
                clientMembers, centerOptions, officeOptions, staffOptions, clientOptions);
    }

    public static GroupGeneralData withTemplate(final GroupGeneralData templatedGrouping, final GroupGeneralData grouping) {
        return new GroupGeneralData(grouping.id, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, grouping.clientMembers, templatedGrouping.centerOptions, templatedGrouping.officeOptions,
                templatedGrouping.staffOptions, templatedGrouping.clientOptions);
    }

    public static GroupGeneralData withTemplateAndAssociations(final GroupGeneralData templatedGrouping, final GroupGeneralData grouping,
            final Collection<ClientData> membersOfGroup) {
        return new GroupGeneralData(grouping.id, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, membersOfGroup, templatedGrouping.centerOptions, templatedGrouping.officeOptions,
                templatedGrouping.staffOptions, templatedGrouping.clientOptions);
    }

    public static GroupGeneralData withAssocations(final GroupGeneralData grouping, final Collection<ClientData> membersOfGroup) {
        return new GroupGeneralData(grouping.id, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, membersOfGroup, grouping.centerOptions, grouping.officeOptions, grouping.staffOptions,
                grouping.clientOptions);
    }

    public static GroupGeneralData instance(final Long id, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long centerId, final String centerName,
            final Long staffId, final String staffName, final String hierarchy) {

        final Collection<ClientData> clientMembers = null;
        final Collection<CenterData> centerOptions = null;
        final Collection<OfficeData> officeOptions = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<ClientData> clientOptions = null;

        return new GroupGeneralData(id, name, externalId, status, activationDate, officeId, officeName, centerId, centerName, staffId,
                staffName, hierarchy, clientMembers, centerOptions, officeOptions, staffOptions, clientOptions);
    }

    private GroupGeneralData(final Long id, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long centerId, final String centerName,
            final Long staffId, final String staffName, final String hierarchy, final Collection<ClientData> clientMembers,
            final Collection<CenterData> centerOptions, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<ClientData> clientOptions) {
        this.id = id;
        this.name = name;
        this.externalId = externalId;
        this.status = status;
        if (status != null) {
            this.active = status.getId().equals(300l);
        } else {
            this.active = null;
        }
        this.activationDate = activationDate;

        this.officeId = officeId;
        this.officeName = officeName;
        this.centerId = centerId;
        this.centerName = centerName;
        this.staffId = staffId;
        this.staffName = staffName;
        this.hierarchy = hierarchy;

        // associations
        this.clientMembers = clientMembers;

        // template
        this.centerOptions = centerOptions;
        this.officeOptions = officeOptions;
        this.staffOptions = staffOptions;

        if (clientMembers != null && clientOptions != null) {
            clientOptions.removeAll(clientMembers);
        }
        this.clientOptions = clientOptions;
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public Long officeId() {
        return this.officeId;
    }
}