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

import com.google.gson.JsonElement;
import io.swagger.annotations.*;
import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.commons.lang.StringUtils;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.calendar.service.CalendarUtils;
import org.apache.fineract.portfolio.client.data.ClientData;
import org.apache.fineract.portfolio.client.service.ClientReadPlatformService;
import org.apache.fineract.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.GroupRoleData;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupReadPlatformService;
import org.apache.fineract.portfolio.group.service.GroupRolesReadPlatformService;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;


@Path("/groups")
@Component
@Scope("singleton")
@Api(value = "Groups", description = "Groups are used to provide a distinctive banking distribution channel used in microfinances throughout the world. The Group is an administrative unit. It can contain as few as 5 people or as many as 40 depending on how its used.\n\n" + "\n\n" + "Different styles of group lending - Joint-Liability Group, Grameen Model (Center-Group), Self-Help Groups, Village/Communal Banks)")
public class GroupsApiResource {

    private final PlatformSecurityContext context;
    private final GroupReadPlatformService groupReadPlatformService;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ClientReadPlatformService clientReadPlatformService;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<GroupGeneralData> groupGeneralApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final GroupRolesReadPlatformService groupRolesReadPlatformService;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadPlatformService meetingReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public GroupsApiResource(final PlatformSecurityContext context, final GroupReadPlatformService groupReadPlatformService,
            final CenterReadPlatformService centerReadPlatformService, final ClientReadPlatformService clientReadPlatformService,
            final ToApiJsonSerializer<Object> toApiJsonSerializer,
            final ToApiJsonSerializer<GroupGeneralData> groupTopOfHierarchyApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService, final FromJsonHelper fromJsonHelper,
            final GroupRolesReadPlatformService groupRolesReadPlatformService,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final MeetingReadPlatformService meetingReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {

        this.context = context;
        this.groupReadPlatformService = groupReadPlatformService;
        this.centerReadPlatformService = centerReadPlatformService;
        this.clientReadPlatformService = clientReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupGeneralApiJsonSerializer = groupTopOfHierarchyApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
        this.groupRolesReadPlatformService = groupRolesReadPlatformService;
        this.accountDetailsReadPlatformService = accountDetailsReadPlatformService;
        this.calendarReadPlatformService = calendarReadPlatformService;
        this.meetingReadPlatformService = meetingReadPlatformService;
        this.entityDatatableChecksReadService = entityDatatableChecksReadService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Group Template", httpMethod = "GET", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n\n" + "\n\n" + "Field Defaults\n\n" + "Allowed Value Lists\n\n" + "Example Requests:\n\n" + "\n\n" + "groups/template\n\n" + "\n\n" + "groups/template?officeId=2\n\n" + "\n\n" + "groups/template?centerId=1\n\n" + "\n\n" + "groups/template?centerId=1&staffInSelectedOfficeOnly=true")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.GetGroupsTemplateResponse.class)})
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId,
            @QueryParam("center") @ApiParam(value = "center") final boolean isCenterGroup, @QueryParam("centerId") @ApiParam(value = "centerId") final Long centerId,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);

        if (is(commandParam, "close")) {
            final GroupGeneralData groupClosureTemplate = this.groupReadPlatformService.retrieveGroupWithClosureReasons();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.groupGeneralApiJsonSerializer.serialize(settings, groupClosureTemplate,
                    GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.GROUP.getName(), null);
        if (centerId != null) {
            final GroupGeneralData centerGroupTemplate = this.centerReadPlatformService.retrieveCenterGroupTemplate(centerId);
            centerGroupTemplate.setDatatables(datatableTemplates);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.groupGeneralApiJsonSerializer.serialize(settings, centerGroupTemplate,
                    GroupingTypesApiConstants.CENTER_GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final GroupGeneralData groupTemplate = this.groupReadPlatformService.retrieveTemplate(officeId, isCenterGroup,
                staffInSelectedOfficeOnly);
        groupTemplate.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupGeneralApiJsonSerializer.serialize(settings, groupTemplate,
                GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Groups", httpMethod = "GET", notes = "The default implementation of listing Groups returns 200 entries with support for pagination and sorting. Using the parameter limit with value -1 returns all entries.\n\n" + "Example Requests:\n\n" + "\n\n" + "groups\n\n" + "\n\n" + "groups?fields=name,officeName,joinedDate\n\n" + "\n\n" + "groups?offset=10&limit=50\n\n" + "\n\n" + "groups?orderBy=name&sortOrder=DESC")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.GetGroupsResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId, @QueryParam("staffId") @ApiParam(value = "staffId") final Long staffId,
            @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId, @QueryParam("name") @ApiParam(value = "name") final String name,
            @QueryParam("underHierarchy") @ApiParam(value = "underHierarchy") final String hierarchy, @QueryParam("paged") @ApiParam(value = "paged") final Boolean paged,
            @QueryParam("offset")@ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder,
            @QueryParam("orphansOnly") @ApiParam(value = "orphansOnly") final Boolean orphansOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        final SearchParameters searchParameters = SearchParameters.forGroups(sqlSearch, officeId, staffId, externalId, name, hierarchy,
                offset, limit, orderBy, sortOrder, orphansOnly);
        if (parameters.isPaged()) {
            final Page<GroupGeneralData> groups = this.groupReadPlatformService.retrievePagedAll(searchParameters, parameters);
            return this.toApiJsonSerializer.serialize(settings, groups, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<GroupGeneralData> groups = this.groupReadPlatformService.retrieveAll(searchParameters, parameters);
        return this.toApiJsonSerializer.serialize(settings, groups, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Group", httpMethod = "GET", notes = "Retrieve group information.\n\n" + "Example Requests:\n\n" + "\n\n" + "groups/1\n\n" + "\n\n" + "groups/1?associations=clientMembers")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.GetGroupsGroupIdResponse.class)})
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("groupId") @ApiParam(value = "groupId") final Long groupId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly,
            @QueryParam("roleId") @ApiParam(value = "roleId") final Long roleId) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.GROUP_RESOURCE_NAME);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());

        GroupGeneralData group = this.groupReadPlatformService.retrieveOne(groupId);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        // associations
        Collection<ClientData> membersOfGroup = null;
        Collection<ClientData> activeClientMembers = null;
        Collection<GroupRoleData> groupRoles = null;
        GroupRoleData selectedRole = null;
        Collection<CalendarData> calendars = null;
        CalendarData collectionMeetingCalendar = null;

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("clientMembers", "activeClientMembers",
                        "groupRoles", "calendars", "collectionMeetingCalendar"));
            }
            if (associationParameters.contains("clientMembers")) {
                membersOfGroup = this.clientReadPlatformService.retrieveClientMembersOfGroup(groupId);
                if (CollectionUtils.isEmpty(membersOfGroup)) {
                    membersOfGroup = null;
                }
            }
            if (associationParameters.contains("activeClientMembers")) {
                activeClientMembers = this.clientReadPlatformService.retrieveActiveClientMembersOfGroup(groupId);
                if (CollectionUtils.isEmpty(activeClientMembers)) {
                    activeClientMembers = null;
                }
            }
            if (associationParameters.contains("groupRoles")) {
                groupRoles = this.groupRolesReadPlatformService.retrieveGroupRoles(groupId);
                if (CollectionUtils.isEmpty(groupRoles)) {
                    groupRoles = null;
                }
            }
            if (associationParameters.contains("parentCalendars")) {
                final List<Integer> calendarTypeOptions = CalendarUtils.createIntegerListFromQueryParameter("all");
                calendars = this.calendarReadPlatformService.retrieveParentCalendarsByEntity(groupId, CalendarEntityType.GROUPS.getValue(),
                        calendarTypeOptions);
                if (CollectionUtils.isEmpty(calendars)) {
                    calendars = null;
                }
            }
            if (associationParameters.contains("collectionMeetingCalendar")) {
                if (group.isChildGroup()) {
                    collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(group.getParentId(),
                            CalendarEntityType.CENTERS.getValue());
                } else {
                    collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(groupId,
                            CalendarEntityType.GROUPS.getValue());
                }
                if (collectionMeetingCalendar != null) {
                    final boolean withHistory = true;
                    final LocalDate tillDate = null;
                    final Collection<LocalDate> recurringDates = this.calendarReadPlatformService.generateRecurringDates(
                            collectionMeetingCalendar, withHistory, tillDate);
                    final Collection<LocalDate> nextTenRecurringDates = this.calendarReadPlatformService
                            .generateNextTenRecurringDates(collectionMeetingCalendar);
                    final MeetingData lastMeeting = this.meetingReadPlatformService.retrieveLastMeeting(collectionMeetingCalendar
                            .getCalendarInstanceId());
                    final LocalDate recentEligibleMeetingDate = this.calendarReadPlatformService
                            .generateNextEligibleMeetingDateForCollection(collectionMeetingCalendar, lastMeeting);
                    collectionMeetingCalendar = CalendarData.withRecurringDates(collectionMeetingCalendar, recurringDates,
                            nextTenRecurringDates, recentEligibleMeetingDate);
                }
            }

            group = GroupGeneralData.withAssocations(group, membersOfGroup, activeClientMembers,
                    groupRoles, calendars, collectionMeetingCalendar);
        }

        if (roleId != null) {
            selectedRole = this.groupRolesReadPlatformService.retrieveGroupRole(groupId, roleId);
            if (selectedRole != null) {
                group = GroupGeneralData.updateSelectedRole(group, selectedRole);
            }
        }

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            final GroupGeneralData templateGroup = this.groupReadPlatformService.retrieveTemplate(group.officeId(), false,
                    staffInSelectedOfficeOnly);
            group = GroupGeneralData.withTemplate(templateGroup, group);
        }

        return this.groupGeneralApiJsonSerializer.serialize(settings, group, GroupingTypesApiConstants.GROUP_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Group", httpMethod = "POST", notes = "Creates a Group\n\n" + "Mandatory Fields: name, officeId, active, activationDate (if active=true)\n\n" + "Optional Fields: externalId, staffId, clientMembers")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = GroupsApiResourceSwagger.PostGroupsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.PostGroupsResponse.class)})
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createGroup() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{groupId}/command/unassign_staff")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Unassign a Staff", httpMethod = "POST", notes = "Allows you to unassign the Staff.\n\n" + "Mandatory Fields: staffId")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = GroupsApiResourceSwagger.PostGroupsGroupIdCommandUnassignStaffRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.PostGroupsGroupIdCommandUnassignStaffResponse.class )})
    public String unassignLoanOfficer(@PathParam("groupId") @ApiParam(value = "groupId") final Long groupId,@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .unassignGroupStaff(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Group", httpMethod = "PUT", notes = "Updates a Group")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = GroupsApiResourceSwagger.PutGroupsGroupIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.PutGroupsGroupIdResponse.class)})
    public String update(@PathParam("groupId")@ApiParam(value = "groupId") final Long groupId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateGroup(groupId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Group", httpMethod = "DELETE", notes = "A group can be deleted if it is in pending state and has no associations - clients, loans or savings")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.DeleteGroupsGroupIdResponse.class)})
    public String delete(@PathParam("groupId") @ApiParam(value = "groupId") final Long groupId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteGroup(groupId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{groupId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Activate a Group | Associate Clients | Disassociate Clients | Transfer Clients across groups | Generate Collection Sheet | Save Collection Sheet | Unassign a Staff | Assign a Staff | Close a Group | Unassign a Role | Update a Role", httpMethod = "POST", notes = "Activate a Group:\n\n" + "Groups can be created in a Pending state. This API exists to enable group activation.\n\n" + "\n\n" + "If the group happens to be already active this API will result in an error.\n\n" + "Mandatory Fields: activationDate\n\n" + "Associate Clients:\n\n" + "This API allows to associate existing clients to a group.\n\n" + "\n\n" + "The clients are listed from the office to which the group is associated.\n\n" + "\n\n" + "If client(s) is already associated with group then API will result in an error.\n\n" + "Mandatory Fields: clientMembers\n\n" + "Disassociate Clients:\n\n" + "This API allows to disassociate clients from a group.\n\n" + "\n\n" + "Disassociating a client with active joint liability group loans results in an error.\n\n" + "Mandatory Fields: clientMembers\n\n" + "Transfer Clients across groups:\n\n" + "This API allows to transfer clients from one group to another\n\n" + "Mandatory Fields: destinationGroupId and clients\n\n" + "Optional Fields: inheritDestinationGroupLoanOfficer (defaults to true) and transferActiveLoans (defaults to true)\n\n" + "Generate Collection Sheet:\n\n" + "This API retrieves repayment details of all jlg loans of all members of a group on a specified meeting date.\n\n" + "Mandatory Fields: calendarId and transactionDate\n\n" + "Save Collection Sheet:\n\n" + "This api allows the loan officer to perform bulk repayments of JLG loans for a group on its meeting date.\n\n" + "Mandatory Fields: calendarId, transactionDate, actualDisbursementDate\n\n" + "Optional Fields: clientsAttendance, bulkRepaymentTransaction, bulkDisbursementTransactions\n\n" + "Unassign a Staff:\n\n" + "Allows you to unassign the Staff.\n\n" + "Mandatory Fields: staffId\n\n" + "Assign a Staff:\n\n" + "Allows you to assign Staff to an existing Group.\n\n" + "\n\n" + "The selected Staff should be belong to the same office (or an office higher up in the hierarchy) as this group" + "Mandatory Fields: staffId\n\n" + "Optional Fields: inheritStaffForClientAccounts (Optional: Boolean if true all members of the group (i.e all clients with active loans and savings ) will inherit the staffId)\n\n" + "Close a Group:\n\n" + "This API exists to close a group. Groups can be closed if they don't have any non-closed clients/loans/savingsAccounts.\n\n" + "\n\n" + "If the group has any active clients/loans/savingsAccount, this API will result in an error." + "Assign a Role:\n\n" + "Allows you to assign a Role to an existing member of a group.\n\n" + "\n\n" + "We can define the different roles applicable to group members by adding code values to the pre-defined system code GROUPROLE. Example:Group leader etc.\n\n" + "Mandatory Fields: clientId, role\n\n" + "Unassign a Role:\n\n" + "Allows you to unassign Roles associated tp Group members.\n\n" + "Update a Role:\n\n" + "Allows you to update the member Role.\n\n" + "Mandatory Fields: role\n\n" + "Showing request/response for Transfer Clients across groups")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = GroupsApiResourceSwagger.PostGroupsGroupIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.PostGroupsGroupIdResponse.class)})
    public String activateOrGenerateCollectionSheet(@PathParam("groupId") @ApiParam(value = "groupId") final Long groupId,
            @QueryParam("command") @ApiParam(value = "command") final String commandParam, @QueryParam("roleId") @ApiParam(value = "roleId") final Long roleId, @ApiParam(hidden = true) final String apiRequestBodyAsJson,
            @Context final UriInfo uriInfo) {
        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "associateClients")) {
            final CommandWrapper commandRequest = builder.associateClientsToGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "disassociateClients")) {
            final CommandWrapper commandRequest = builder.disassociateClientsFromGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "generateCollectionSheet")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.generateGroupCollectionSheet(groupId,
                    query);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, collectionSheet, GroupingTypesApiConstants.COLLECTIONSHEET_DATA_PARAMETERS);
        } else if (is(commandParam, "saveCollectionSheet")) {
            final CommandWrapper commandRequest = builder.saveGroupCollectionSheet(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignStaff")) {
            final CommandWrapper commandRequest = builder.unassignGroupStaff(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "assignStaff")) {
            final CommandWrapper commandRequest = builder.assignGroupStaff(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "assignRole")) {
            final CommandWrapper commandRequest = builder.assignRole(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "unassignRole")) {
            final CommandWrapper commandRequest = builder.unassignRole(groupId, roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "updateRole")) {
            final CommandWrapper commandRequest = builder.updateRole(groupId, roleId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "transferClients")) {
            final CommandWrapper commandRequest = builder.transferClientsBetweenGroups(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeGroup(groupId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate", "generateCollectionSheet",
                    "saveCollectionSheet", "unassignStaff", "assignRole", "unassignRole", "updateassignRole" });
        }

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{groupId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Group accounts overview", httpMethod = "GET", notes = "Retrieves details of all Loan and Savings accounts associated with this group.\n\n" + "\n\n" + "Example Requests:\n\n" + "\n\n" + "groups/1/accounts\n\n" + "\n\n" + "\n\n" + "groups/1/accounts?fields=loanAccounts,savingsAccounts,memberLoanAccounts,\n\n" + "memberSavingsAccounts")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = GroupsApiResourceSwagger.GetGroupsGroupIdAccountsResponse.class)})
    public String retrieveAccounts(@PathParam("groupId") @ApiParam(value = "groupId") final Long groupId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission("GROUP");

        final AccountSummaryCollectionData groupAccount = this.accountDetailsReadPlatformService.retrieveGroupAccountDetails(groupId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts",
                "memberLoanAccounts", "memberSavingsAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getGroupsTemplate(@QueryParam("officeId")final Long officeId,
            @QueryParam("staffId")final Long staffId,@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.GROUPS.toString(),
                officeId, staffId,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postGroupTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.GROUPS.toString(),
                uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}