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
package org.apache.fineract.portfolio.group.api;

public final class GroupingTypesApiConstants {

    private GroupingTypesApiConstants() {

    }

    public static final String CENTER_RESOURCE_NAME = "center";
    public static final String GROUP_RESOURCE_NAME = "group";
    public static final String COMMUNAL_BANK_RESOURCE_NAME = "communalbank";

    // group roles
    public static final String GROUP_ROLE_RESOURCE_NAME = "grouprole";
    public static final String GROUP_ROLE_NAME = "GROUPROLE";

    public static final String GROUP_CLOSURE_REASON = "GroupClosureReason";
    public static final String CENTER_CLOSURE_REASON = "CenterClosureReason";

    public static final String roleParamName = "role";
    public static final String groupIdParamName = "groupId";
    public static final String clientIdParamName = "clientId";
    public static final String groupRolesParamName = "groupRoles";
    public static final String accountNoParamName = "accountNo";

    // general
    public static final String localeParamName = "locale";
    public static final String dateFormatParamName = "dateFormat";

    // center parameters
    public static final String idParamName = "id";
    public static final String nameParamName = "name";
    public static final String externalIdParamName = "externalId";
    public static final String officeIdParamName = "officeId";
    public static final String staffIdParamName = "staffId";
    public static final String activeParamName = "active";
    public static final String activationDateParamName = "activationDate";
    public static final String groupMembersParamName = "groupMembers";

    public static final String submittedOnDateParamName = "submittedOnDate";
    public static final String inheritStaffForClientAccounts = "inheritStaffForClientAccounts";

    // group parameters
    public static final String centerIdParamName = "centerId";
    public static final String clientMembersParamName = "clientMembers";

    // response parameters
    public static final String statusParamName = "status";
    public static final String hierarchyParamName = "hierarchy";
    public static final String officeNameParamName = "officeName";
    public static final String staffNameParamName = "staffName";
    public static final String officeOptionsParamName = "officeOptions";
    public static final String staffOptionsParamName = "staffOptions";
    public static final String clientOptionsParamName = "clientOptions";
    public static final String collectionMeetingCalendar = "collectionMeetingCalendar";
    public static final String timeLine = "timeline";
    public static final String closureReasons = "closureReasons";
    public static final String totalCollected = "totalCollected";
    public static final String totalOverdue = "totalOverdue";
    public static final String totaldue = "totaldue";
    public static final String installmentDue = "installmentDue";

    // group close parameters
    public static final String closureDateParamName = "closureDate";
    public static final String closureReasonIdParamName = "closureReasonId";

    // staff centres parameters
    public static final String meetingFallCenters = "meetingFallCenters";

    public static final String datatables = "datatables";

}
