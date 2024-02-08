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
package org.apache.fineract.organisation.staff.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
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
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.UploadRequest;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Path("/v1/staff")
@Component
@Tag(name = "Staff", description = "Allows you to model staff members. At present the key role of significance is whether this staff member is a loan officer or not.")
@RequiredArgsConstructor
public class StaffApiResource {

    /**
     * The set of parameters that are supported in response for {@link StaffData}.
     */
    private static final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "firstname", "lastname", "displayName",
            "officeId", "officeName", "isLoanOfficer", "externalId", "mobileNo", "allowedOffices", "isActive", "joiningDate"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "STAFF";

    private final PlatformSecurityContext context;
    private final StaffReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<StaffData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Staff", description = "Returns the list of staff members.\n" + "\n" + "Example Requests:\n" + "\n"
            + "staff\n\n\n\n" + "\n" + "Retrieve a Staff by status\n" + "\n" + "Returns the details of a Staff based on status.\n" + "\n"
            + "By default it Returns all the ACTIVE Staff.\n" + "\n" + "If status=INACTIVE, then it returns all INACTIVE Staff.\n" + "\n"
            + "and for status=ALL, it Returns both ACTIVE and INACTIVE Staff.\n" + "\n" + "Example Requests:\n" + "\n"
            + "staff?status=active")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = StaffApiResourceSwagger.RetrieveOneResponse.class)))) })
    public String retrieveAll(@Context final UriInfo uriInfo,
            @QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInOfficeHierarchy") @Parameter(description = "staffInOfficeHierarchy") final boolean staffInOfficeHierarchy,
            @DefaultValue("false") @QueryParam("loanOfficersOnly") @Parameter(description = "loanOfficersOnly") final boolean loanOfficersOnly,
            @DefaultValue("active") @QueryParam("status") @Parameter(description = "status") final String status) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Collection<StaffData> staff;
        if (staffInOfficeHierarchy) {
            staff = readPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, loanOfficersOnly);
        } else {
            staff = readPlatformService.retrieveAllStaff(officeId, loanOfficersOnly, status);
        }
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return toApiJsonSerializer.serialize(settings, staff, RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a staff member", description = "Creates a staff member.\n" + "\n" + "Mandatory Fields: \n"
            + "officeId, firstname, lastname\n" + "\n" + "Optional Fields: \n" + "isLoanOfficer, isActive")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = StaffApiResourceSwagger.PostStaffRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StaffApiResourceSwagger.CreateStaffResponse.class))) })
    public String create(@Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().createStaff().withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Staff Member", description = "Returns the details of a Staff Member.\n" + "\n" + "Example Requests:\n"
            + "\n" + "staff/1")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StaffApiResourceSwagger.RetrieveOneResponse.class))) })
    public String retrieveOne(@PathParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        StaffData staff = readPlatformService.retrieveStaff(staffId);
        if (settings.isTemplate()) {
            final Collection<OfficeData> allowedOffices = officeReadPlatformService.retrieveAllOfficesForDropdown();
            staff = StaffData.templateData(staff, allowedOffices);
        }
        return toApiJsonSerializer.serialize(settings, staff, RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Staff Member", description = "Updates the details of a staff member.")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = StaffApiResourceSwagger.PutStaffRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = StaffApiResourceSwagger.UpdateStaffResponse.class))) })
    public String update(@PathParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateStaff(staffId).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getTemplate(@QueryParam("officeId") final Long officeId, @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.STAFF.toString(), officeId, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @RequestBody(description = "Upload staff template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = UploadRequest.class)) })
    public String postTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = bulkImportWorkbookService.importWorkbook(GlobalEntityType.STAFF.toString(), uploadedInputStream,
                fileDetail, locale, dateFormat);
        return toApiJsonSerializer.serialize(importDocumentId);
    }
}
