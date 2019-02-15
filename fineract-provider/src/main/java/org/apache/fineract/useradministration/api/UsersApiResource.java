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
package org.apache.fineract.useradministration.api;

import java.io.InputStream;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

import javax.ws.rs.*;
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
import org.apache.fineract.infrastructure.bulkimport.constants.UserConstants;
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
import org.apache.fineract.useradministration.data.AppUserData;
import org.apache.fineract.useradministration.service.AppUserReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/users")
@Component
@Scope("singleton")
@Api(value = "Users", description = "An API capability to support administration of application users.")
public class UsersApiResource {

    /**
     * The set of parameters that are supported in response for
     * {@link AppUserData}.
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "officeId", "officeName", "username",
            "firstname", "lastname", "email", "allowedOffices", "availableRoles", "selectedRoles", "staff"));

    private final String resourceNameForPermissions = "USER";

    private final PlatformSecurityContext context;
    private final AppUserReadPlatformService readPlatformService;
    private final OfficeReadPlatformService officeReadPlatformService;
    private final DefaultToApiJsonSerializer<AppUserData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService;
    private final BulkImportWorkbookService bulkImportWorkbookService;

    @Autowired
    public UsersApiResource(final PlatformSecurityContext context, final AppUserReadPlatformService readPlatformService,
            final OfficeReadPlatformService officeReadPlatformService, final DefaultToApiJsonSerializer<AppUserData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final BulkImportWorkbookPopulatorService bulkImportWorkbookPopulatorService,
            final BulkImportWorkbookService bulkImportWorkbookService) {
        this.context = context;
        this.readPlatformService = readPlatformService;
        this.officeReadPlatformService = officeReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.bulkImportWorkbookPopulatorService=bulkImportWorkbookPopulatorService;
        this.bulkImportWorkbookService=bulkImportWorkbookService;
    }

    @GET
    @ApiOperation(value = "Retrieve list of users", notes = "Example Requests:\n" + "\n" + "users\n" + "\n" + "\n" + "users?fields=id,username,email,officeName")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.GetUsersResponse.class, responseContainer = "List")})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<AppUserData> users = this.readPlatformService.retrieveAllUsers();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, users, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{userId}")
    @ApiOperation(value = "Retrieve a User", notes = "Example Requests:\n" + "\n" + "users/1\n" + "\n" + "\n" + "users/1?template=true\n" + "\n" + "\n" + "users/1?fields=username,officeName")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.GetUsersUserIdResponse.class)})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("userId") @ApiParam(value = "userId") final Long userId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions, userId);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AppUserData user = this.readPlatformService.retrieveUser(userId);
        if (settings.isTemplate()) {
            final Collection<OfficeData> offices = this.officeReadPlatformService.retrieveAllOfficesForDropdown();
            user = AppUserData.template(user, offices);
        }

        return this.toApiJsonSerializer.serialize(settings, user, this.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @ApiOperation(value = "Retrieve User Details Template", notes = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n" + "\n" + "Field Defaults\n" + "Allowed Value Lists\n" + "Example Request:\n" + "\n" + "users/template")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.GetUsersTemplateResponse.class)})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final AppUserData user = this.readPlatformService.retrieveNewUserDetails();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, user, this.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @ApiOperation(value = "Create a User", notes = "Adds new application user.\n" + "\n" + "Note: Password information is not required (or processed). Password details at present are auto-generated and then sent to the email account given (which is why it can take a few seconds to complete).\n" + "\n" + "Mandatory Fields: \n" + "username, firstname, lastname, email, officeId, roles, sendPasswordToEmail\n" + "\n" + "Optional Fields: \n" + "staffId,passwordNeverExpires,isSelfServiceUser,clients")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = UsersApiResourceSwagger.PostUsersRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.PostUsersResponse.class)})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createUser() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{userId}")
    @ApiOperation(value = "Update a User", notes = "When updating a password you must provide the repeatPassword parameter also.")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = UsersApiResourceSwagger.PutUsersUserIdRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.PutUsersUserIdResponse.class)})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("userId") @ApiParam(value = "userId") final Long userId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateUser(userId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{userId}")
    @ApiOperation(value = "Delete a User", notes = "Removes the user and the associated roles and permissions.")
    @ApiResponses({@ApiResponse(code = 200, message = "", response = UsersApiResourceSwagger.DeleteUsersUserIdResponse.class)})
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("userId") @ApiParam(value = "userId") final Long userId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteUser(userId) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("downloadtemplate")
    @Produces("application/vnd.ms-excel")
    public Response getUserTemplate(@QueryParam("officeId")final Long officeId,@QueryParam("staffId")final Long staffId,
            @QueryParam("dateFormat") final String dateFormat) {
        return bulkImportWorkbookPopulatorService.getTemplate(GlobalEntityType.USERS.toString(), officeId, staffId,dateFormat);
    }

    @POST
    @Path("uploadtemplate")
    @Consumes(MediaType.MULTIPART_FORM_DATA)
    public String postUsersTemplate(@FormDataParam("file") InputStream uploadedInputStream,
            @FormDataParam("file") FormDataContentDisposition fileDetail,
            @FormDataParam("locale") final String locale, @FormDataParam("dateFormat") final String dateFormat){
        final Long importDocumentId = this. bulkImportWorkbookService.importWorkbook(GlobalEntityType.USERS.toString(), uploadedInputStream,fileDetail,
                locale,dateFormat);
        return this.toApiJsonSerializer.serialize(importDocumentId);
    }
}