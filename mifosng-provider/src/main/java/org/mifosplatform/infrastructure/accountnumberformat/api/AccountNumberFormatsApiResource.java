/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.infrastructure.accountnumberformat.api;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.accountnumberformat.data.AccountNumberFormatData;
import org.mifosplatform.infrastructure.accountnumberformat.domain.EntityAccountType;
import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatConstants;
import org.mifosplatform.infrastructure.accountnumberformat.service.AccountNumberFormatReadPlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path(AccountNumberFormatConstants.resourceRelativeURL)
@Component
@Scope("singleton")
public class AccountNumberFormatsApiResource {

    private final PlatformSecurityContext context;
    private final AccountNumberFormatReadPlatformService accountNumberFormatReadPlatformService;
    private final ToApiJsonSerializer<AccountNumberFormatData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public AccountNumberFormatsApiResource(final PlatformSecurityContext context,
            final ToApiJsonSerializer<AccountNumberFormatData> toApiJsonSerializer,
            final AccountNumberFormatReadPlatformService accountNumberFormatReadPlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.accountNumberFormatReadPlatformService = accountNumberFormatReadPlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        EntityAccountType accountType = null;
        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService.retrieveTemplate(accountType);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
                AccountNumberFormatConstants.ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final List<AccountNumberFormatData> accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAllAccountNumberFormats();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
                AccountNumberFormatConstants.ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@Context final UriInfo uriInfo, @PathParam("accountNumberFormatId") final Long accountNumberFormatId) {

        this.context.authenticatedUser().validateHasReadPermission(AccountNumberFormatConstants.ENTITY_NAME);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        AccountNumberFormatData accountNumberFormatData = this.accountNumberFormatReadPlatformService
                .getAccountNumberFormat(accountNumberFormatId);
        if (settings.isTemplate()) {
            final AccountNumberFormatData templateData = this.accountNumberFormatReadPlatformService.retrieveTemplate(EntityAccountType
                    .fromInt(accountNumberFormatData.getAccountType().getId().intValue()));
            accountNumberFormatData.templateOnTop(templateData.getAccountTypeOptions(), templateData.getPrefixTypeOptions());
        }

        return this.toApiJsonSerializer.serialize(settings, accountNumberFormatData,
                AccountNumberFormatConstants.ACCOUNT_NUMBER_FORMAT_RESPONSE_DATA_PARAMETERS);
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .createAccountNumberFormat() //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("accountNumberFormatId") final Long accountNumberFormatId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .updateAccountNumberFormat(accountNumberFormatId) //
                .withJson(apiRequestBodyAsJson) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{accountNumberFormatId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("accountNumberFormatId") final Long accountNumberFormatId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder() //
                .deleteAccountNumberFormat(accountNumberFormatId) //
                .build(); //

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

}
