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
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.DefaultValue;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.Response;
import java.io.InputStream;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Supplier;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.command.core.CommandDispatcher;
import org.apache.fineract.infrastructure.bulkimport.data.GlobalEntityType;
import org.apache.fineract.infrastructure.bulkimport.service.BulkImportWorkbookPopulatorService;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.apache.fineract.organisation.staff.command.StaffCreateCommand;
import org.apache.fineract.organisation.staff.command.StaffUpdateCommand;
import org.apache.fineract.organisation.staff.command.StaffUploadCommand;
import org.apache.fineract.organisation.staff.data.StaffCreateRequest;
import org.apache.fineract.organisation.staff.data.StaffCreateResponse;
import org.apache.fineract.organisation.staff.data.StaffData;
import org.apache.fineract.organisation.staff.data.StaffUpdateRequest;
import org.apache.fineract.organisation.staff.data.StaffUpdateResponse;
import org.apache.fineract.organisation.staff.data.StaffUploadRequest;
import org.apache.fineract.organisation.staff.service.StaffReadService;
import org.glassfish.jersey.media.multipart.FormDataContentDisposition;
import org.glassfish.jersey.media.multipart.FormDataParam;
import org.springframework.stereotype.Component;

@Slf4j
@Path("/v1/staff")
@Produces({ MediaType.APPLICATION_JSON })
@Component
@Tag(name = "Staff", description = "Allows you to model staff members. At present the key role of significance is whether this staff member is a loan officer or not.")
@RequiredArgsConstructor
public class StaffApiResource {

    private final StaffReadService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final CommandDispatcher dispatcher;

    @GET
    @Operation(summary = "Retrieve Staff", operationId = "retrieveAllStaff", description = """
            Returns the list of staff members.

            Example Requests:

            - /staff
            - /staff?status=ACTIVE
            - /staff?status=INACTIVE
            - /staff?status=ALL

            By default it Returns all the ACTIVE Staff. Otherwise a status can be provided like e.g. status=INACTIVE,
            then it returns all INACTIVE staff or status=ALL returns both ACTIVE and INACTIVE staff.
            """)
    public List<StaffData> retrieveAll(@QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @DefaultValue("false") @QueryParam("staffInOfficeHierarchy") @Parameter(description = "staffInOfficeHierarchy") final boolean staffInOfficeHierarchy,
            @DefaultValue("false") @QueryParam("loanOfficersOnly") @Parameter(description = "loanOfficersOnly") final boolean loanOfficersOnly,
            @DefaultValue("active") @QueryParam("status") @Parameter(description = "status") final String status) {
        return staffInOfficeHierarchy ? readPlatformService.retrieveAllStaffInOfficeAndItsParentOfficeHierarchy(officeId, loanOfficersOnly)
                : readPlatformService.retrieveAllStaff(officeId, loanOfficersOnly, Optional.ofNullable(status).orElse("active"));
    }

    @GET
    @Path("{staffId}")
    @Operation(summary = "Retrieve a Staff Member", operationId = "retrieveOneStaff", description = """
            Returns the details of a Staff Member.

            Example Requests:

            - /staff/1
            """)
    public StaffData retrieveOne(@PathParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @DefaultValue("false") @QueryParam("template") @Parameter(description = "template", hidden = true) boolean template) {
        StaffData staff = readPlatformService.retrieveStaff(staffId);

        if (template) {
            final Collection<OfficeData> allowedOffices = officeReadPlatformService.retrieveAllOfficesForDropdown();

            staff = StaffData.builder().id(staff.getId()).firstname(staff.getFirstname()).lastname(staff.getLastname())
                    .displayName(staff.getDisplayName()).officeId(staff.getOfficeId()).officeName(staff.getOfficeName())
                    .isLoanOfficer(staff.getIsLoanOfficer()).externalId(staff.getExternalId()).mobileNo(staff.getMobileNo())
                    .isActive(staff.getIsActive()).joiningDate(staff.getJoiningDate()).allowedOffices(allowedOffices).build();
        }

        return staff;
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    @Operation(summary = "Download bulk import template")
    public Response getTemplate(@QueryParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @QueryParam("dateFormat") @Parameter(description = "dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.STAFF.toString(), officeId, null, dateFormat);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a staff member", operationId = "createStaff", description = """
            Creates a staff member.

            Mandatory fields:

            - officeId
            - firstname
            - lastname

            Optional fields:

            - isLoanOfficer
            - isActive
            """)
    public StaffCreateResponse createStaff(@RequestBody(required = true) @Valid StaffCreateRequest request) {
        final var command = new StaffCreateCommand();

        command.setPayload(request);

        final Supplier<StaffCreateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @PUT
    @Path("{staffId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Staff Member", description = "Updates the details of a staff member.")
    public StaffUpdateResponse updateStaff(@PathParam("staffId") @Parameter(description = "staffId") final Long staffId,
            @RequestBody(required = true) @Valid StaffUpdateRequest request) {
        request.setId(staffId);

        final var command = new StaffUpdateCommand();

        command.setPayload(request);

        final Supplier<StaffUpdateResponse> response = dispatcher.dispatch(command);

        return response.get();
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    @Produces({ MediaType.WILDCARD })
    @RequestBody(description = "Upload staff template", content = {
            @Content(mediaType = MediaType.MULTIPART_FORM_DATA, schema = @Schema(implementation = StaffUploadRequest.class)) })
    public Long postTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final var command = new StaffUploadCommand();

        command.setPayload(StaffUploadRequest.builder().uploadedInputStream(uploadedInputStream).fileDetail(fileDetail).locale(locale)
                .dateFormat(dateFormat).build());

        final Supplier<StaffUpdateResponse> response = dispatcher.dispatch(command);

        // TODO: return the whole body, a number is not a valid JSON element!
        return response.get().getResourceId();
    }
}
