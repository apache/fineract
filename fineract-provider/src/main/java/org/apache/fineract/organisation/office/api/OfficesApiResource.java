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
package org.apache.fineract.organisation.office.api;

import com.sun.jersey.core.header.FormDataContentDisposition;
import com.sun.jersey.multipart.FormDataParam;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
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
import org.apache.fineract.infrastructure.core.service.SearchParameters;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.office.data.OfficeData;
import org.apache.fineract.organisation.office.service.OfficeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/offices")
@Component
@Scope("singleton")
@Tag(name = "Offices", description = "Offices are used to model an MFIs structure. A hierarchical representation of offices is supported. There will always be at least one office (which represents the MFI or an MFIs head office). All subsequent offices added must have a parent office.")
public class OfficesApiResource {

    /**
     * The set of parameters that are supported in response for {@link OfficeData}.
     */
    private final Set<String> responseDataParameters = new HashSet<>(Arrays.asList("id", "name", "nameDecorated", "externalId",
            "openingDate", "hierarchy", "parentId", "parentName", "allowedParents"));

    private final String resourceNameForPermissions = "OFFICE";

    private final PlatformSecurityContext context;
    private final OfficeReadPlatformService readPlatformService;
    private final DefaultToApiJsonSerializer<OfficeData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookService bulkImportWorkbookService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;

    @Autowired
    public OfficesApiResource(final PlatformSecurityContext context, final OfficeReadPlatformService readPlatformService,
            final DefaultToApiJsonSerializer<OfficeData> toApiJsonSerializer, final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookService bulkImportWorkbookService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.bulkImportWorkbookService = bulkImportWorkbookService;
        this.bulkImportWorkbookPopulatorService = bulkImportWorkbookPopulatorService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Offices", description = "Example Requests:\n" + "\n" + "offices\n" + "\n" + "\n"
            + "offices?fields=id,name,openingDate")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = OfficesApiResourceSwagger.GetOfficesResponse.class)))) })
    public String retrieveOffices(@Context final UriInfo uriInfo,
            @DefaultValue("false") @QueryParam("includeAllOffices") @Parameter(description = "includeAllOffices") final boolean onlyManualEntries,
            @QueryParam("orderBy") @Parameter(description = "orderBy") final String orderBy,
            @QueryParam("sortOrder") @Parameter(description = "sortOrder") final String sortOrder) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final SearchParameters searchParameters = SearchParameters.forOffices(orderBy, sortOrder);

        final Collection<OfficeData> offices = this.readPlatformService.retrieveAllOffices(onlyManualEntries, searchParameters);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, offices, this.responseDataParameters);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Office Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "offices/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.GetOfficesTemplateResponse.class))) })
    public String retrieveOfficeTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        OfficeData office = this.readPlatformService.retrieveNewOfficeTemplate();

        final Collection<OfficeData> allowedParents = this.readPlatformService.retrieveAllOfficesForDropdown();
        office = OfficeData.appendedTemplate(office, allowedParents);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, office, this.responseDataParameters);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create an Office", description = "Mandatory Fields\n" + "name, openingDate, parentId")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.PostOfficesRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.PostOfficesResponse.class))) })
    public String createOffice(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createOffice() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve an Office", description = "Example Requests:\n" + "\n" + "offices/1\n" + "\n" + "\n"
            + "offices/1?template=true\n" + "\n" + "\n" + "offices/1?fields=id,name,parentName")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.GetOfficesResponse.class))) })
    public String retreiveOffice(@PathParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        OfficeData office = this.readPlatformService.retrieveOffice(officeId);
        if (settings.isTemplate()) {
            final Collection<OfficeData> allowedParents = this.readPlatformService.retrieveAllowedParents(officeId);
            office = OfficeData.appendedTemplate(office, allowedParents);
        }

        return this.toApiJsonSerializer.serialize(settings, office, this.responseDataParameters);
    }

    @PUT
    @Path("{officeId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update Office", description = "")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.PutOfficesOfficeIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = OfficesApiResourceSwagger.PutOfficesOfficeIdResponse.class))) })
    public String updateOffice(@PathParam("officeId") @Parameter(description = "officeId") final Long officeId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateOffice(officeId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getOfficeTemplate(@QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.OFFICES.toString(), null, null, dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postOfficeTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail, @FormDataParam("locale") final String locale,
            @FormDataParam("dateFormat") final String dateFormat) {
        final Long importDocumentId = this.bulkImportWorkbookService.importWorkbook(GlobalEntityType.OFFICES.toString(),
                uploadedInputStream, fileDetail, locale, dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}
