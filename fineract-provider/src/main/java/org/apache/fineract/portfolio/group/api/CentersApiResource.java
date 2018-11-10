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
import java.util.*;

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
import org.apache.fineract.accounting.journalentry.api.DateParam;
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
import org.apache.fineract.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.StaffCenterData;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.apache.fineract.portfolio.meeting.service.MeetingReadPlatformService;
import org.joda.time.LocalDate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/centers")
@Component
@Scope("singleton")
@Api(value = "Centers", description = "Centers along with Groups are used to provided a distinctive banking distribution channel used in microfinance. Its common in areas such as Southern Asia to use Centers and Group as administrative units in grameen style lending. Typically groups will contain one to five people and centers themselves will be made of anywhere between 2-10 groups.")
public class CentersApiResource {

    private final PlatformSecurityContext context;
    private final CenterReadPlatformService centerReadPlatformService;
    private final ToApiJsonSerializer<CenterData> centerApiJsonSerializer;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadPlatformService meetingReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @Autowired
    public CentersApiResource(final PlatformSecurityContext context, final CenterReadPlatformService centerReadPlatformService,
            final ToApiJsonSerializer<CenterData> centerApiJsonSerializer, final ToApiJsonSerializer<Object> toApiJsonSerializer,
            final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final CollectionSheetReadPlatformService collectionSheetReadPlatformService, final FromJsonHelper fromJsonHelper,
            final AccountDetailsReadPlatformService accountDetailsReadPlatformService,
            final CalendarReadPlatformService calendarReadPlatformService, final MeetingReadPlatformService meetingReadPlatformService,
            final EntityDatatableChecksReadService entityDatatableChecksReadService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.centerReadPlatformService = centerReadPlatformService;
        this.centerApiJsonSerializer = centerApiJsonSerializer;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.groupSummaryToApiJsonSerializer = groupSummaryToApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.collectionSheetReadPlatformService = collectionSheetReadPlatformService;
        this.fromJsonHelper = fromJsonHelper;
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
    @ApiOperation(value = "Retrieve a Center Template", httpMethod = "GET", notes = "Retrieves a Center Template\n\n" + "Example Requests:\n\n" + "\n\n" + "centers/template\n\n" + "\n\n" + "centers/template?officeId=2")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.GetCentersTemplateResponse.class)})
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
            @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        if (is(commandParam, "close")) {
            final CenterData centerClosureTemplate = this.centerReadPlatformService.retrieveCenterWithClosureReasons();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.centerApiJsonSerializer.serialize(settings, centerClosureTemplate,
                    GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final CenterData template = this.centerReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly);
        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService
                .retrieveTemplates(StatusEnum.CREATE.getCode().longValue(), EntityTables.GROUP.getName(), null);
        template.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.centerApiJsonSerializer.serialize(settings, template, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Centers", httpMethod = "GET", notes = "The default implementation supports pagination and sorting with the default pagination size set to 200 records. The parameter limit with value -1 will return all entries.\n\n" + "Example Requests:\n\n" + "\n\n" + "centers\n\n" + "\n\n" + "centers?fields=name,officeName,joinedDate\n\n" + "\n\n" + "centers?offset=10&limit=50\n\n" + "\n\n" + "centers?orderBy=name&sortOrder=DESC")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.GetCentersResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId, @QueryParam("staffId") @ApiParam(value = "staffId") final Long staffId,
            @QueryParam("externalId") @ApiParam(value = "externalId") final String externalId, @QueryParam("name") @ApiParam(value = "name") final String name,
            @QueryParam("underHierarchy") @ApiParam(value = "underHierarchy") final String hierarchy, @QueryParam("paged") @ApiParam(value = "paged") final Boolean paged,
            @QueryParam("offset") @ApiParam(value = "offset") final Integer offset, @QueryParam("limit") @ApiParam(value = "limit") final Integer limit,
            @QueryParam("orderBy") @ApiParam(value = "orderBy") final String orderBy, @QueryParam("sortOrder") @ApiParam(value = "sortOrder") final String sortOrder,
            @QueryParam("meetingDate") @ApiParam(value = "meetingDate") final DateParam meetingDateParam, @QueryParam("dateFormat") @ApiParam(value = "dateFormat") final String dateFormat,
            @QueryParam("locale") @ApiParam(value = "locale") final String locale) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (meetingDateParam != null && officeId != null) {
            Date meetingDate = meetingDateParam.getDate("meetingDate", dateFormat, locale);
            Collection<StaffCenterData> staffCenterDataArray = this.centerReadPlatformService.retriveAllCentersByMeetingDate(officeId,
                    meetingDate, staffId);
            return this.toApiJsonSerializer.serialize(settings, staffCenterDataArray,
                    GroupingTypesApiConstants.STAFF_CENTER_RESPONSE_DATA_PARAMETERS);
        }
        final PaginationParameters parameters = PaginationParameters.instance(paged, offset, limit, orderBy, sortOrder);
        final Boolean isOrphansOnly = false;
        final SearchParameters searchParameters = SearchParameters.forGroups(sqlSearch, officeId, staffId, externalId, name, hierarchy,
                offset, limit, orderBy, sortOrder, isOrphansOnly);
        if (parameters.isPaged()) {
            final Page<CenterData> centers = this.centerReadPlatformService.retrievePagedAll(searchParameters, parameters);
            return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<CenterData> centers = this.centerReadPlatformService.retrieveAll(searchParameters, parameters);
        return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Center", httpMethod = "GET", notes = "Retrieves a Center\n\n" + "Example Requests:\n\n" + "\n\n" + "centers/1\n\n" + "\n\n" + "centers/1?associations=groupMembers")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.GetCentersCenterIdResponse.class)})
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("centerId") @ApiParam(value = "centerId") final Long centerId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @ApiParam(value = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        CalendarData collectionMeetingCalendar = null;
        Collection<GroupGeneralData> groups = null;
        CenterData center = this.centerReadPlatformService.retrieveOne(centerId);

        final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
        if (template) {
            final CenterData templateCenter = this.centerReadPlatformService.retrieveTemplate(center.officeId(), staffInSelectedOfficeOnly);
            center = CenterData.withTemplate(templateCenter, center);
        }

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("groupMembers")) {
                groups = this.centerReadPlatformService.retrieveAssociatedGroups(centerId);
            }

            if (associationParameters.contains("collectionMeetingCalendar")) {
                collectionMeetingCalendar = this.calendarReadPlatformService.retrieveCollctionCalendarByEntity(centerId,
                        CalendarEntityType.CENTERS.getValue());
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

            center = CenterData.withAssociations(center, groups, collectionMeetingCalendar);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.centerApiJsonSerializer.serialize(settings, center, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a Center", httpMethod = "POST", notes = "Creates a Center\n\n" + "Mandatory Fields: name, officeId, active, activationDate (if active=true)\n\n" + "Optional Fields: externalId, staffId, groupMembers")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CentersApiResourceSwagger.PostCentersRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.PostCentersResponse.class)})
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createCenter() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Center", httpMethod = "PUT", notes = "Updates a Center")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CentersApiResourceSwagger.PutCentersCenterIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.PutCentersCenterIdResponse.class)})
    public String update(@PathParam("centerId") @ApiParam(value = "centerId") final Long centerId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateCenter(centerId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Center", httpMethod = "DELETE", notes = "A Center can be deleted if it is in pending state and has no association - groups, loans or savings")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.DeleteCentersCenterIdResponse.class)})
    public String delete(@PathParam("centerId") @ApiParam(value = "centerId") final Long centerId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteCenter(centerId) //
                .build(); //
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }

    @POST
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Activate a Center | Generate Collection Sheet | Save Collection Sheet | Close a Center | Associate Groups | Disassociate Groups", httpMethod = "POST", notes = "Activate a Center:\n\n" + "Centers can be created in a Pending state. This API exists to enable center activation. If the center happens to be already active, this API will result in an error.\n\n" + "Close a Center:\n\n" + "Centers can be closed if they don't have any non-closed groups or saving accounts. If the Center has any active groups or savings accounts, this API will result in an error.\n\n" + "Associate Groups:\n\n" + "This API allows associating existing groups to a center. The groups are listed from the office to which the center is associated. If group(s) is already associated with a center, this API will result in an error.\n\n" + "Disassociate Groups:\n\n" + "This API allows to disassociate groups from a center.\n\n" + "Generate Collection Sheet:\n\n" + "This Api retrieves repayment details of all jlg loans under a center as on a specified meeting date.\n\n" + "Save Collection Sheet:\n\n" + "This Api allows the loan officer to perform bulk repayments of JLG loans for a center on a given meeting date.\n\n" + "Showing Request/Response for Close a Center")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = CentersApiResourceSwagger.PostCentersCenterIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.PostCentersCenterIdResponse.class)})
    public String activate(@PathParam("centerId") @ApiParam(value = "centerId") final Long centerId, @QueryParam("command") @ApiParam(value = "command") final String commandParam,
           @ApiParam(hidden = true) final String apiRequestBodyAsJson, @Context final UriInfo uriInfo) {

        final CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);

        CommandProcessingResult result = null;
        if (is(commandParam, "activate")) {
            final CommandWrapper commandRequest = builder.activateCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "generateCollectionSheet")) {
            final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
            final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.generateCenterCollectionSheet(centerId,
                    query);
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.toApiJsonSerializer.serialize(settings, collectionSheet, GroupingTypesApiConstants.COLLECTIONSHEET_DATA_PARAMETERS);
        } else if (is(commandParam, "saveCollectionSheet")) {
            final CommandWrapper commandRequest = builder.saveCenterCollectionSheet(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "close")) {
            final CommandWrapper commandRequest = builder.closeCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "associateGroups")) {
            final CommandWrapper commandRequest = builder.associateGroupsToCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else if (is(commandParam, "disassociateGroups")) {
            final CommandWrapper commandRequest = builder.disassociateGroupsFromCenter(centerId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
            return this.toApiJsonSerializer.serialize(result);
        } else {
            throw new UnrecognizedQueryParamException("command", commandParam, new Object[] { "activate", "generateCollectionSheet",
                    "saveCollectionSheet", "close", "associateGroups", "disassociateGroups" });
        }

    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @GET
    @Path("{centerId}/accounts")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Center accounts overview", httpMethod = "GET", notes = "An example of how a savings summary for a Center can be provided. This is requested in a specific use case of the reference application.\n\n" + "It is quite reasonable to add resources like this to simplify User Interface development.\n\n" + "\n\n" + "Example Requests:\n\n" + "\n\n" + "centers/9/accounts")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = CentersApiResourceSwagger.GetCentersCenterIdAccountsResponse.class)})
    public String retrieveGroupAccount(@PathParam("centerId") @ApiParam(value = "centerId") final Long centerId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(GroupingTypesApiConstants.CENTER_RESOURCE_NAME);

        final AccountSummaryCollectionData groupAccount = this.accountDetailsReadPlatformService.retrieveGroupAccountDetails(centerId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(Arrays.asList("loanAccounts", "savingsAccounts",
                "memberLoanAccounts", "memberSavingsAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getCentersTemplate(@QueryParam("officeId")final Long officeId,@QueryParam("staffId")final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.CENTERS.toString(), officeId,staffId,dateFormat);
    }
    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postCentersTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId=this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.CENTERS.toString(), uploadedInputStream,fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}