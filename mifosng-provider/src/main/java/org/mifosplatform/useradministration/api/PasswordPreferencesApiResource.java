/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.useradministration.api;

import java.util.Collection;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.useradministration.data.PasswordValidationPolicyData;
import org.mifosplatform.useradministration.service.PasswordValidationPolicyReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/" + PasswordPreferencesApiConstants.RESOURCE_NAME)
@Component
@Scope("singleton")
public class PasswordPreferencesApiResource {

    private final PlatformSecurityContext context;
    private final PasswordValidationPolicyReadPlatformService passwordValidationPolicyReadPlatformService;
    private final DefaultToApiJsonSerializer<PasswordValidationPolicyData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public PasswordPreferencesApiResource(final PlatformSecurityContext context,
            final PasswordValidationPolicyReadPlatformService readPlatformService,
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
    public String retrieve(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(PasswordPreferencesApiConstants.ENTITY_NAME);

        final PasswordValidationPolicyData passwordValidationPolicyData = this.passwordValidationPolicyReadPlatformService
                .retrieveActiveValidationPolicy();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, passwordValidationPolicyData,
                PasswordPreferencesApiConstants.RESPONSE_DATA_PARAMETERS);
    }

    @PUT
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updatePasswordPreferences() //
                .withJson(apiRequestBodyAsJson) //
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Path("/template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String template(@Context final UriInfo uriInfo) {
        this.context.authenticatedUser().validateHasReadPermission(PasswordPreferencesApiConstants.ENTITY_NAME);

        final Collection<PasswordValidationPolicyData> validationPolicies = this.passwordValidationPolicyReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, validationPolicies, PasswordPreferencesApiConstants.RESPONSE_DATA_PARAMETERS);
    }

}