/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.group.data;

import java.util.Collection;

import org.apache.fineract.infrastructure.codes.data.CodeValueData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.joda.time.LocalDate;

/**
 * Immutable data object representing a general group (so may or may not have a
 * parent).
 */
public class GroupGeneralData {

    private final Long id;
    private final String accountNo;
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
    private final String groupLevel;

    // associations
    private final Collection<ClientData> clientMembers;
    private final Collection<ClientData> activeClientMembers;
    private final Collection<GroupRoleData> groupRoles;
    private final Collection<CalendarData> calendarsData;
    private final CalendarData collectionMeetingCalendar;

    // template
    private final Collection<CenterData> centerOptions;
    private final Collection<OfficeData> officeOptions;
    private final Collection<StaffData> staffOptions;
    private final Collection<ClientData> clientOptions;
    private final Collection<CodeValueData> availableRoles;
    private final GroupRoleData selectedRole;
    private final Collection<CodeValueData> closureReasons;
    private final GroupTimelineData timeline;

    public static GroupGeneralData lookup(final Long groupId, final String accountNo, final String groupName) {
        final Collection<ClientData> clientMembers = null;
        final Collection<GroupRoleData> groupRoles = null;
        final Collection<CodeValueData> closureReasons = null;
        return new GroupGeneralData(groupId, accountNo, groupName, null, null, null, null, null, null, null, null, null, null, null, clientMembers, null, null,
                null, null, null, groupRoles, null, null, null, null, closureReasons, null);
    }

    public static GroupGeneralData template(final Long officeId, final Long centerId, final String accountNo, final String centerName, final Long staffId,
            final String staffName, final Collection<CenterData> centerOptions, final Collection<OfficeData> officeOptions,
            final Collection<StaffData> staffOptions, final Collection<ClientData> clientOptions,
            final Collection<CodeValueData> availableRoles) {

        final Collection<ClientData> clientMembers = null;
        final Collection<GroupRoleData> groupRoles = null;
        final Collection<CodeValueData> closureReasons = null;

        return new GroupGeneralData(null, accountNo , null, null, null, null, officeId, null, centerId, centerName, staffId, staffName, null, null,
                clientMembers, null, centerOptions, officeOptions, staffOptions, clientOptions, groupRoles, availableRoles, null, null, null,
                closureReasons, null);
    }

    public static GroupGeneralData withTemplate(final GroupGeneralData templatedGrouping, final GroupGeneralData grouping) {
        return new GroupGeneralData(grouping.id, grouping.accountNo, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, grouping.groupLevel, grouping.clientMembers, grouping.activeClientMembers, templatedGrouping.centerOptions, templatedGrouping.officeOptions,
                templatedGrouping.staffOptions, templatedGrouping.clientOptions, grouping.groupRoles, templatedGrouping.availableRoles,
                grouping.selectedRole, grouping.calendarsData, grouping.collectionMeetingCalendar, grouping.closureReasons,
                templatedGrouping.timeline);
    }

    public static GroupGeneralData withAssocations(final GroupGeneralData grouping, final Collection<ClientData> membersOfGroup,
            final Collection<ClientData> activeClientMembers, final Collection<GroupRoleData> groupRoles, final Collection<CalendarData> calendarsData,
            final CalendarData collectionMeetingCalendar) {
        return new GroupGeneralData(grouping.id, grouping.accountNo, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, grouping.groupLevel, membersOfGroup, activeClientMembers, grouping.centerOptions, grouping.officeOptions, grouping.staffOptions,
                grouping.clientOptions, groupRoles, grouping.availableRoles, grouping.selectedRole, calendarsData,
                collectionMeetingCalendar, grouping.closureReasons, grouping.timeline);
    }

    public static GroupGeneralData instance(final Long id, final String accountNo, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long centerId, final String centerName,
            final Long staffId, final String staffName, final String hierarchy, final String groupLevel, final GroupTimelineData timeline) {

        final Collection<ClientData> clientMembers = null;
        final Collection<ClientData> activeClientMembers = null;
        final Collection<CenterData> centerOptions = null;
        final Collection<OfficeData> officeOptions = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<ClientData> clientOptions = null;
        final Collection<GroupRoleData> groupRoles = null;
        final Collection<CodeValueData> availableRoles = null;
        final GroupRoleData role = null;
        final Collection<CalendarData> calendarsData = null;
        final CalendarData collectionMeetingCalendar = null;
        final Collection<CodeValueData> closureReasons = null;

        return new GroupGeneralData(id, accountNo, name, externalId, status, activationDate, officeId, officeName, centerId, centerName, staffId,
                staffName, hierarchy, groupLevel, clientMembers, activeClientMembers, centerOptions, officeOptions, staffOptions,
                clientOptions, groupRoles, availableRoles, role, calendarsData, collectionMeetingCalendar, closureReasons, timeline);
    }

    private GroupGeneralData(final Long id, final String accountNo, final String name, final String externalId, final EnumOptionData status,
            final LocalDate activationDate, final Long officeId, final String officeName, final Long centerId, final String centerName,
            final Long staffId, final String staffName, final String hierarchy, final String groupLevel, final Collection<ClientData> clientMembers,
            final Collection<ClientData> activeClientMembers, final Collection<CenterData> centerOptions,
            final Collection<OfficeData> officeOptions, final Collection<StaffData> staffOptions,
            final Collection<ClientData> clientOptions, final Collection<GroupRoleData> groupRoles,
            final Collection<CodeValueData> availableRoles, final GroupRoleData role,
            final Collection<CalendarData> calendarsData, final CalendarData collectionMeetingCalendar,
            final Collection<CodeValueData> closureReasons, final GroupTimelineData timeline) {
        this.id = id;
        this.accountNo = accountNo;
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
        this.groupLevel = groupLevel;

        // associations
        this.clientMembers = clientMembers;
        this.activeClientMembers = activeClientMembers;

        // template
        this.centerOptions = centerOptions;
        this.officeOptions = officeOptions;
        this.staffOptions = staffOptions;

        if (clientMembers != null && clientOptions != null) {
            clientOptions.removeAll(clientMembers);
        }
        this.clientOptions = clientOptions;
        this.groupRoles = groupRoles;
        this.availableRoles = availableRoles;
        this.selectedRole = role;
        this.calendarsData = calendarsData;
        this.collectionMeetingCalendar = collectionMeetingCalendar;
        this.closureReasons = closureReasons;
        this.timeline = timeline;
    }

    public Long getId() {
        return this.id;
    }
    
    public String getAccountNo(){
    	return this.accountNo;
    }

    public String getName() {
        return this.name;
    }

    public Long officeId() {
        return this.officeId;
    }

    public String getHierarchy() {
        return this.hierarchy;
    }

    public boolean isChildGroup() {
        return this.centerId == null ? false : true;
    }

    public Long getParentId() {
        return this.centerId;
    }

    public static GroupGeneralData updateSelectedRole(final GroupGeneralData grouping, final GroupRoleData selectedRole) {
        return new GroupGeneralData(grouping.id, grouping.accountNo, grouping.name, grouping.externalId, grouping.status, grouping.activationDate,
                grouping.officeId, grouping.officeName, grouping.centerId, grouping.centerName, grouping.staffId, grouping.staffName,
                grouping.hierarchy, grouping.groupLevel, grouping.clientMembers, grouping.activeClientMembers, grouping.centerOptions,
                grouping.officeOptions, grouping.staffOptions, grouping.clientOptions, grouping.groupRoles, grouping.availableRoles,
                selectedRole, grouping.calendarsData, grouping.collectionMeetingCalendar, grouping.closureReasons, null);
    }

    public static GroupGeneralData withClosureReasons(final Collection<CodeValueData> closureReasons) {
        final Long id = null;
        final String accountNo = null;
        final String name = null;
        final String externalId = null;
        final EnumOptionData status = null;
        final LocalDate activationDate = null;
        final Long officeId = null;
        final String officeName = null;
        final Long centerId = null;
        final String centerName = null;
        final Long staffId = null;
        final String staffName = null;
        final String hierarchy = null;
        final String groupLevel = null;
        final Collection<ClientData> clientMembers = null;
        final Collection<ClientData> activeClientMembers = null;
        final Collection<CenterData> centerOptions = null;
        final Collection<OfficeData> officeOptions = null;
        final Collection<StaffData> staffOptions = null;
        final Collection<ClientData> clientOptions = null;
        final Collection<GroupRoleData> groupRoles = null;
        final Collection<CodeValueData> availableRoles = null;
        final GroupRoleData role = null;
        final Collection<CalendarData> calendarsData = null;
        final CalendarData collectionMeetingCalendar = null;

        return new GroupGeneralData(id, accountNo, name, externalId, status, activationDate, officeId, officeName, centerId, centerName, staffId,
                staffName, hierarchy, groupLevel, clientMembers, activeClientMembers, centerOptions, officeOptions, staffOptions, clientOptions, groupRoles,
                availableRoles, role, calendarsData, collectionMeetingCalendar, closureReasons, null);
    }

    public Collection<ClientData> clientMembers() {
        return this.clientMembers;
    }
}