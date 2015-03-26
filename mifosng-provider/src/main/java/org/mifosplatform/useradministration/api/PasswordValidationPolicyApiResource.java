/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.api;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.PasswordValidationPolicyData;
import org.mifosplatform.useradministration.data.RoleData;
import org.mifosplatform.useradministration.data.RolePermissionsData;
import org.mifosplatform.useradministration.service.PasswordValidationPolicyReadPlatformService;
import org.mifosplatform.useradministration.service.PermissionReadPlatformService;
import org.mifosplatform.useradministration.service.RoleReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.Set;

@Path("/passwordValidationPolicy")
@Component
@Scope("singleton")
public class PasswordValidationPolicyApiResource {

    /**
     * The set of parameters that are supported in response for {@link org.mifosplatform.useradministration.data.RoleData}
     */
    private final Set<String> RESPONSE_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "description",
            "active"));


    private final String resourceNameForPermissions = "Password_Validation_Policy";

    private final PlatformSecurityContext context;
    private final PasswordValidationPolicyReadPlatformService passwordValidationPolicyReadPlatformService;
    private final DefaultToApiJsonSerializer<PasswordValidationPolicyData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public PasswordValidationPolicyApiResource(final PlatformSecurityContext context, final PasswordValidationPolicyReadPlatformService readPlatformService,
                                               final DefaultToApiJsonSerializer<PasswordValidationPolicyData> toApiJsonSerializer,
                                               final ApiRequestParameterHelper apiRequestParameterHelper,
                                               final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.passwordValidationPolicyReadPlatformService = readPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllPolicy(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final Collection<PasswordValidationPolicyData> validationPolicies = this.passwordValidationPolicyReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize( settings,validationPolicies ,this.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Path("{validationPolicyId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String activate(@PathParam("validationPolicyId") final Long validationPolicyId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .activatePasswordValidationPolicy(validationPolicyId) //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}