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

import com.google.gson.Gson;
import com.google.gson.JsonElement;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.ConstraintViolation;
import jakarta.validation.ConstraintViolationException;
import jakarta.validation.Validator;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DELETE;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import jakarta.ws.rs.core.UriInfo;
import java.io.InputStream;
import java.time.LocalDate;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import org.apache.commons.lang3.StringUtils;
import org.apache.fineract.command.core.Command;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.core.annotation.AlternativeOperationId;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.api.DateParam;
import org.apache.fineract.infrastructure.core.api.JsonQuery;
import org.apache.fineract.infrastructure.core.data.DateFormat;
import org.apache.fineract.infrastructure.core.data.PaginationParameters;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.FromJsonHelper;
import org.apache.fineract.infrastructure.core.serialization.ToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.Page;
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.dataqueries.data.DatatableData;
import org.apache.fineract.infrastructure.dataqueries.data.EntityTables;
import org.apache.fineract.infrastructure.dataqueries.data.StatusEnum;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksReadService;
import org.apache.fineract.infrastructure.security.service.SqlValidator;
import org.apache.fineract.portfolio.accountdetails.data.AccountSummaryCollectionData;
import org.apache.fineract.portfolio.accountdetails.service.AccountDetailsReadPlatformService;
import org.apache.fineract.portfolio.calendar.data.CalendarData;
import org.apache.fineract.portfolio.calendar.domain.CalendarEntityType;
import org.apache.fineract.portfolio.calendar.service.CalendarReadPlatformService;
import org.apache.fineract.portfolio.collectionsheet.data.JLGCollectionSheetData;
import org.apache.fineract.portfolio.collectionsheet.service.CollectionSheetReadPlatformService;
import org.apache.fineract.portfolio.group.command.CenterActivateCommand;
import org.apache.fineract.portfolio.group.command.CenterAssociateGroupsCommand;
import org.apache.fineract.portfolio.group.command.CenterCloseCommand;
import org.apache.fineract.portfolio.group.command.CenterCreateCommand;
import org.apache.fineract.portfolio.group.command.CenterDeleteCommand;
import org.apache.fineract.portfolio.group.command.CenterDisassociateGroupsCommand;
import org.apache.fineract.portfolio.group.command.CenterSaveCollectionSheetCommand;
import org.apache.fineract.portfolio.group.command.CenterUpdateCommand;
import org.apache.fineract.portfolio.group.command.CenterUploadCommand;
import org.apache.fineract.portfolio.group.data.CenterCommandRequest;
import org.apache.fineract.portfolio.group.data.CenterCommandResponse;
import org.apache.fineract.portfolio.group.data.CenterCreateRequest;
import org.apache.fineract.portfolio.group.data.CenterCreateResponse;
import org.apache.fineract.portfolio.group.data.CenterData;
import org.apache.fineract.portfolio.group.data.CenterDeleteRequest;
import org.apache.fineract.portfolio.group.data.CenterDeleteResponse;
import org.apache.fineract.portfolio.group.data.CenterUpdateRequest;
import org.apache.fineract.portfolio.group.data.CenterUpdateResponse;
import org.apache.fineract.portfolio.group.data.CenterUploadRequest;
import org.apache.fineract.portfolio.group.data.CenterUploadResponse;
import org.apache.fineract.portfolio.group.data.CentersPageResponse;
import org.apache.fineract.portfolio.group.data.GroupGeneralData;
import org.apache.fineract.portfolio.group.data.StaffCenterData;
import org.apache.fineract.portfolio.group.mapping.CenterCommandRequestMapper;
import org.apache.fineract.portfolio.group.service.CenterReadPlatformService;
import org.apache.fineract.portfolio.meeting.data.MeetingData;
import org.apache.fineract.portfolio.meeting.service.MeetingReadService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/centers")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Centers", description = "Centers along with Groups are used to provided a distinctive banking distribution channel used in microfinance. Its common in areas such as Southern Asia to use Centers and Group as administrative units in grameen style lending. Typically groups will contain one to five people and centers themselves will be made of anywhere between 2-10 groups.")
@RequiredArgsConstructor
public class CentersApiResource {

    private final CenterReadPlatformService centerReadPlatformService;
    private final ToApiJsonSerializer<CenterData> centerApiJsonSerializer;
    private final ToApiJsonSerializer<Object> toApiJsonSerializer;
    private final ToApiJsonSerializer<AccountSummaryCollectionData> groupSummaryToApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final CollectionSheetReadPlatformService collectionSheetReadPlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final AccountDetailsReadPlatformService accountDetailsReadPlatformService;
    private final CalendarReadPlatformService calendarReadPlatformService;
    private final MeetingReadService meetingReadPlatformService;
    private final EntityDatatableChecksReadService entityDatatableChecksReadService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final SqlValidator sqlValidator;
    private final CommandDispatcher dispatcher;
    private final Validator validator;
    private final CenterCommandRequestMapper mapper;

    @GET
    @Path("template")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Center Template", operationId = "retrieveTemplateCenter", description = """
            Retrieves a Center Template

            Example Requests:



            centers/template



            centers/template?officeId=2""")
    @AlternativeOperationId("retrieveTemplate_6")

    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CenterData.class)))
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @QueryParam("command") @Parameter(description = "command") final String commandParam,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

        if (is(commandParam, "close")) {
            final CenterData centerClosureTemplate = this.centerReadPlatformService.retrieveCenterWithClosureReasons();
            final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            return this.centerApiJsonSerializer.serialize(settings, centerClosureTemplate,
                    GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final CenterData template = this.centerReadPlatformService.retrieveTemplate(officeId, staffInSelectedOfficeOnly);
        final List<DatatableData> datatableTemplates = this.entityDatatableChecksReadService.retrieveTemplates(StatusEnum.CREATE.getValue(),
                EntityTables.GROUP.getName(), null);
        template.setDatatables(datatableTemplates);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.centerApiJsonSerializer.serialize(settings, template, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Centers", operationId = "retrieveAllCenters", description = """
            The default implementation supports pagination and sorting with the default pagination size set to 200 records. The parameter limit with description -1 will return all entries.

            Example Requests:



            centers



            centers?fields=name,officeName,joinedDate



            centers?offset=10&limit=50



            centers?orderBy=name&sortOrder=DESC""")
    @AlternativeOperationId("retrieveAll_23")

    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CentersPageResponse.class)))
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @QueryParam("externalId") @Parameter(description = "externalId") final String externalId,
            @QueryParam("name") @Parameter(description = "name") final String name,
            @QueryParam("underHierarchy") @Parameter(description = "underHierarchy") final String hierarchy,
            @QueryParam("paged") @Parameter(description = "paged") final Boolean paged,
            @QueryParam("offset") @Parameter(description = "offset") final Integer offset,
            @QueryParam("limit") @Parameter(description = "limit") final Integer limit,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder,
            @QueryParam("meetingDate") @Parameter(description = "meetingDate") final DateParam meetingDateParam,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat,
            @QueryParam("locale") @Parameter(description = "locale") final String locale) {

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        if (meetingDateParam != null && officeId != null) {
            LocalDate meetingDate = meetingDateParam.getDate("meetingDate", new DateFormat(dateFormat), locale);
            Collection<StaffCenterData> staffCenterDataArray = this.centerReadPlatformService.retriveAllCentersByMeetingDate(officeId,
                    meetingDate, staffId);
            return this.toApiJsonSerializer.serialize(settings, staffCenterDataArray,
                    GroupingTypesApiConstants.STAFF_CENTER_RESPONSE_DATA_PARAMETERS);
        }
        sqlValidator.validate(orderBy);
        sqlValidator.validate(sortOrder);
        sqlValidator.validate(externalId);
        sqlValidator.validate(hierarchy);
        final PaginationParameters parameters = PaginationParameters.builder().paged(Boolean.TRUE.equals(paged)).limit(limit).offset(offset)
                .orderBy(orderBy).sortOrder(sortOrder).build();
        final SearchParameters searchParameters = SearchParameters.builder().limit(limit).officeId(officeId).externalId(externalId)
                .name(name).hierarchy(hierarchy).offset(offset).orderBy(orderBy).sortOrder(sortOrder).staffId(staffId).build();
        if (parameters.isPaged()) {
            final Page<CenterData> centers = this.centerReadPlatformService.retrievePagedAll(searchParameters, parameters);
            return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
        }

        final Collection<CenterData> centers = this.centerReadPlatformService.retrieveAll(searchParameters, parameters);
        return this.toApiJsonSerializer.serialize(settings, centers, GroupingTypesApiConstants.CENTER_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{centerId}")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Center", operationId = "retrieveOneCenter", description = """
            Retrieves a Center

            Example Requests:



            centers/1



            centers/1?associations=groupMembers""")
    @AlternativeOperationId("retrieveOne_14")

    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CenterData.class)))
    public String retrieveOne(@Context final UriInfo uriInfo,
            @PathParam("centerId") @Parameter(description = "centerId") final Long centerId,
            @DefaultValue("false") @QueryParam("staffInSelectedOfficeOnly") @Parameter(description = "staffInSelectedOfficeOnly") final boolean staffInSelectedOfficeOnly) {

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
                    final Collection<LocalDate> recurringDates = this.calendarReadPlatformService
                            .generateRecurringDates(collectionMeetingCalendar, withHistory, tillDate);
                    final Collection<LocalDate> nextTenRecurringDates = this.calendarReadPlatformService
                            .generateNextTenRecurringDates(collectionMeetingCalendar);
                    final MeetingData lastMeeting = this.meetingReadPlatformService
                            .retrieveLastMeeting(collectionMeetingCalendar.getCalendarInstanceId());
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
    @Operation(summary = "Create a Center", operationId = "createCenter", description = """
            Creates a Center

            Mandatory Fields: name, officeId, active, activationDate (if active=true)

            Optional Fields: externalId, staffId, groupMembers""")
    @AlternativeOperationId("create_7")
    public CenterCreateResponse create(@RequestBody(required = true) final CenterCreateRequest request) {
        return dispatch(new CenterCreateCommand(), validate(request));
    }

    @PUT
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Center", operationId = "updateCenter", description = "Updates a Center")
    @AlternativeOperationId("update_12")
    public CenterUpdateResponse update(@PathParam("centerId") @Parameter(description = "centerId") final Long centerId,
            @RequestBody(required = true) final CenterUpdateRequest request) {
        request.setId(centerId);
        return dispatch(new CenterUpdateCommand(), validate(request));
    }

    @DELETE
    @Path("{centerId}")
    @Operation(summary = "Delete a Center", operationId = "deleteCenter", description = "A Center can be deleted if it is in pending state and has no association - groups, loans or savings")
    @AlternativeOperationId("delete_10")
    public CenterDeleteResponse delete(@PathParam("centerId") @Parameter(description = "centerId") final Long centerId) {
        return dispatch(new CenterDeleteCommand(), CenterDeleteRequest.builder().id(centerId).build());
    }

    @POST
    @Path("{centerId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Activate a Center | Generate Collection Sheet | Save Collection Sheet | Close a Center | Associate Groups | Disassociate Groups", operationId = "handleCommandsCenter", description = """
            Activate a Center:

            Centers can be created in a Pending state. This API exists to enable center activation. If the center happens to be already active, this API will result in an error.

            Close a Center:

            Centers can be closed if they don't have any non-closed groups or saving accounts. If the Center has any active groups or savings accounts, this API will result in an error.

            Associate Groups:

            This API allows associating existing groups to a center. The groups are listed from the office to which the center is associated. If group(s) is already associated with a center, this API will result in an error.

            Disassociate Groups:

            This API allows to disassociate groups from a center.

            Generate Collection Sheet:

            This Api retrieves repayment details of all jlg loans under a center as on a specified meeting date.

            Save Collection Sheet:

            This Api allows the loan officer to perform bulk repayments of JLG loans for a center on a given meeting date.

            Showing Request/Response for Close a Center""")
    @AlternativeOperationId("activate_2")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = CenterCommandRequest.class)))
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = CenterCommandResponse.class)))
    public Object activate(@PathParam("centerId") @Parameter(description = "centerId") final Long centerId,
            @QueryParam("command") @Parameter(description = "command") final String commandParam, final CenterCommandRequest request,
            @Context final UriInfo uriInfo) {
        final CenterCommandRequest body = request == null ? new CenterCommandRequest() : request;
        return switch (CenterCommand.from(commandParam)) {
            case ACTIVATE -> dispatch(new CenterActivateCommand(), validate(mapper.toActivate(body, centerId)));
            case GENERATE_COLLECTION_SHEET -> generateCollectionSheet(centerId, body, uriInfo);
            case SAVE_COLLECTION_SHEET ->
                dispatch(new CenterSaveCollectionSheetCommand(), validate(mapper.toSaveCollectionSheet(body, centerId)));
            case CLOSE -> dispatch(new CenterCloseCommand(), validate(mapper.toClose(body, centerId)));
            case ASSOCIATE_GROUPS -> dispatch(new CenterAssociateGroupsCommand(), validate(mapper.toAssociateGroups(body, centerId)));
            case DISASSOCIATE_GROUPS ->
                dispatch(new CenterDisassociateGroupsCommand(), validate(mapper.toDisassociateGroups(body, centerId)));
        };
    }

    private String generateCollectionSheet(final Long centerId, final CenterCommandRequest body, final UriInfo uriInfo) {
        final String json = new Gson().toJson(body);
        final JsonElement parsed = fromJsonHelper.parse(json);
        final JLGCollectionSheetData collectionSheet = this.collectionSheetReadPlatformService.generateCenterCollectionSheet(centerId,
                JsonQuery.from(json, parsed, fromJsonHelper));
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, collectionSheet, GroupingTypesApiConstants.COLLECTIONSHEET_DATA_PARAMETERS);
    }

    @GET
    @Path("{centerId}/accounts")
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Center accounts overview", operationId = "retrieveGroupAccountsCenter", description = """
            An example of how a savings summary for a Center can be provided. This is requested in a specific use case of the reference application.

            It is quite reasonable to add resources like this to simplify User Interface development.



            Example Requests:



            centers/9/accounts""")
    @AlternativeOperationId("retrieveGroupAccount")
    @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = AccountSummaryCollectionData.class)))
    public String retrieveGroupAccount(@PathParam("centerId") @Parameter(description = "centerId") final Long centerId,
            @Context final UriInfo uriInfo) {

        final AccountSummaryCollectionData groupAccount = this.accountDetailsReadPlatformService.retrieveGroupAccountDetails(centerId);

        final Set<String> GROUP_ACCOUNTS_DATA_PARAMETERS = new HashSet<>(
                Arrays.asList("loanAccounts", "savingsAccounts", "memberLoanAccounts", "memberSavingsAccounts"));

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.groupSummaryToApiJsonSerializer.serialize(settings, groupAccount, GROUP_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    @Operation(summary = "Download Centers Bulk Template", operationId = "getBulkTemplateCenter")
    @AlternativeOperationId("getCentersTemplate")
    public Response getCentersTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("staffId") final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.CENTERS.toString(), officeId, staffId, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.WILDCARD })
    @Operation(summary = "Upload Centers Bulk Template", operationId = "postBulkTemplateCenter")
    @AlternativeOperationId("postCentersTemplate")
    @RequestBody(description = "Upload centers template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public Long postCentersTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final CenterUploadRequest request = CenterUploadRequest.builder().uploadedInputStream(uploadedInputStream).fileDetail(fileDetail)
                .locale(locale).dateFormat(dateFormat).build();
        final CenterUploadResponse response = dispatch(new CenterUploadCommand(), request);
        return response.getResourceId();
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    private <T> T validate(final T request) {
        final Set<ConstraintViolation<T>> violations = validator.validate(request);
        if (!violations.isEmpty()) {
            throw new ConstraintViolationException(violations);
        }
        return request;
    }

    private <RequestT, ResponseT> ResponseT dispatch(final Command<RequestT> command, final RequestT payload) {
        command.setPayload(payload);
        final Supplier<ResponseT> response = dispatcher.dispatch(command);
        return response.get();
    }
}
