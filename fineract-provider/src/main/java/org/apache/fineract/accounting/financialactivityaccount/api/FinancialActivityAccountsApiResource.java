/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.accounting.financialactivityaccount.api;

import java.util.List;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.accounting.financialactivityaccount.data.FinancialActivityAccountData;
import org.mifosplatform.accounting.financialactivityaccount.service.FinancialActivityAccountReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/financialactivityaccounts")
@Component
@Scope("singleton")
public class FinancialActivityAccountsApiResource {

    private final FinancialActivityAccountReadPlatformService financialActivityAccountReadPlatformService;
    private final DefaultToApiJsonSerializer<FinancialActivityAccountData> apiJsonSerializerService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PlatformSecurityContext context;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public FinancialActivityAccountsApiResource(final PlatformSecurityContext context,
            final FinancialActivityAccountReadPlatformService officeToGLAccountMappingReadPlatformService,
            final DefaultToApiJsonSerializer<FinancialActivityAccountData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiJsonSerializerService = toApiJsonSerializer;
        this.financialActivityAccountReadPlatformService = officeToGLAccountMappingReadPlatformService;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);

        FinancialActivityAccountData financialActivityAccountData = this.financialActivityAccountReadPlatformService
                .getFinancialActivityAccountTemplate();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, financialActivityAccountData,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);
        final List<FinancialActivityAccountData> financialActivityAccounts = this.financialActivityAccountReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.apiJsonSerializerService.serialize(settings, financialActivityAccounts,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retreive(@PathParam("mappingId") final Long mappingId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(FinancialActivityAccountsConstants.resourceNameForPermission);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        FinancialActivityAccountData financialActivityAccountData = this.financialActivityAccountReadPlatformService.retrieve(mappingId);
        if (settings.isTemplate()) {
            financialActivityAccountData = this.financialActivityAccountReadPlatformService
                    .addTemplateDetails(financialActivityAccountData);
        }

        return this.apiJsonSerializerService.serialize(settings, financialActivityAccountData,
                FinancialActivityAccountsConstants.RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createGLAccount(final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createOfficeToGLAccountMapping().withJson(jsonRequestBody)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @PUT
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateGLAccount(@PathParam("mappingId") final Long mappingId, final String jsonRequestBody) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateOfficeToGLAccountMapping(mappingId)
                .withJson(jsonRequestBody).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }

    @DELETE
    @Path("{mappingId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteGLAccount(@PathParam("mappingId") final Long mappingId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteOfficeToGLAccountMapping(mappingId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.apiJsonSerializerService.serialize(result);
    }
}
