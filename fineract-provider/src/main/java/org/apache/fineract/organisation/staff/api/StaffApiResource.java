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

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.Consumes;
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

import io.swagger.annotations.*;
import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.service.StaffReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/staff")
@Component
@Scope("singleton")
@Api(value = "Staff", description = "Allows you to model staff members. At present the key role of significance is whether this staff member is a loan officer or not.")
public class StaffApiResource {

    /**
     * The set of parameters that are supported in response for
     * {@link StaffData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "firstname", "lastname", "displayName",
            "officeId", "officeName", "isLoanOfficer", "externalId", "mobileNo", "allowedOffices", "isActive", "joiningDate"));

    private final String resourceNameForPermissions = "STAFF";

    private final PlatformSecurityContext context;
    private final StaffReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<StaffData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;


    @Autowired
    public StaffApiResource(final PlatformSecurityContext context, final StaffReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final DefaultToApiJsonSerializer<StaffData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve Staff", notes = "Returns the list of staff members.\n" + "\n" + "Example Requests:\n" + "\n" + "staff\n\n\n\n" + "\n" + "Retrieve a Staff by status\n" + "\n" + "Returns the details of a Staff based on status.\n" + "\n" + "By default it Returns all the ACTIVE Staff.\n" + "\n" + "If status=INACTIVE, then it returns all INACTIVE Staff.\n" + "\n" + "and for status=ALL, it Returns both ACTIVE and INACTIVE Staff.\n" + "\n" + "Example Requests:\n" + "\n" + "staff?status=active")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = StaffApiResourceSwagger.GetStaffResponse.class, responseContainer = "List"), @ApiResponse(code = 200, message = "GET https://DomainName/api/v1/staff?status={ACTIVE|INACTIVE|ALL}", response = StaffApiResourceSwagger.GetStaffResponse.class)})
    public String retrieveStaff(@Context final UriInfo uriInfo, @QueryParam("sqlSearch") @ApiParam(value = "sqlSearch") final String sqlSearch,
            @QueryParam("officeId") @ApiParam(value = "officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInOfficeHierarchy") @ApiParam(value = "staffInOfficeHierarchy") final boolean staffInOfficeHierarchy,
            @DefaultValue("false") @QueryParam("loanOfficersOnly") @ApiParam(value = "loanOfficersOnly") final boolean loanOfficersOnly,
            @DefaultValue("active") @QueryParam("status") @ApiParam(value = "status") final String status) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<StaffData> staff;
        if (staffInOfficeHierarchy) {
            staff = this.readPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, loanOfficersOnly);
        } else {
            staff = this.readPlatformService.retrieveAllStaff(sqlSearch, officeId, loanOfficersOnly, status);
        }

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, staff, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Create a staff member", notes = "Creates a staff member.\n" + "\n" + "Mandatory Fields: \n" + "officeId, firstname, lastname\n" + "\n" + "Optional Fields: \n" + "isLoanOfficer, isActive")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = StaffApiResourceSwagger.PostStaffRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = StaffApiResourceSwagger.PostStaffResponse.class)})
    public String createStaff(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createStaff().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Staff Member", notes = "Returns the details of a Staff Member.\n" + "\n" + "Example Requests:\n" + "\n" + "staff/1")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = StaffApiResourceSwagger.GetStaffResponse.class)})
    public String retreiveStaff(@PathParam("staffId") @ApiParam(value = "staffId") final Long staffId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        StaffData staff = this.readPlatformService.retrieveStaff(staffId);
        if (settings.isTemplate()) {
            final Collection<OfficeData> allowedOffices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            staff = StaffData.templateData(staff, allowedOffices);
        }
        return this.toApiJsonSerializer.serialize(settings, staff, this.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Staff Member", notes = "Updates the details of a staff member.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = StaffApiResourceSwagger.PutStaffRequest.class )})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = StaffApiResourceSwagger.PutStaffResponse.class)})
    public String updateStaff(@PathParam("staffId") @ApiParam(value = "staffId") final Long staffId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateStaff(staffId).withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getStaffTemplate(@QueryParam("officeId")final Long officeId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.STAFF.toString(), officeId,null,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postStaffTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.STAFF.toString(), uploadedInputStream,
                fileDetail,locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}