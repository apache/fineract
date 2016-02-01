/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.accounts.api;

import java.util.Collection;
import java.util.HashSet;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
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
import org.mifosplatform.portfolio.accounts.constants.AccountsApiConstants;
import org.mifosplatform.portfolio.accounts.data.AccountData;
import org.mifosplatform.portfolio.accounts.service.AccountReadPlatformService;
import org.mifosplatform.portfolio.accounts.service.AccountsCommandsService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;


@Path("/accounts/{type}")
@Component
@Scope("singleton")
public class AccountsApiResource {

    private final ApplicationContext applicationContext ;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer;
    private final PlatformSecurityContext platformSecurityContext;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final DefaultToApiJsonSerializer<Object> toApiObjectJsonSerializer ;
    
    @Autowired
    public AccountsApiResource(final ApplicationContext applicationContext,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final DefaultToApiJsonSerializer<AccountData> toApiJsonSerializer,
            final PlatformSecurityContext platformSecurityContext,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final DefaultToApiJsonSerializer<Object> toApiObjectJsonSerializer) {
        this.applicationContext = applicationContext ;
        this.apiRequestParameterHelper = apiRequestParameterHelper ;
        this.toApiJsonSerializer = toApiJsonSerializer ;
        this.platformSecurityContext = platformSecurityContext ; 
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService ;
        this.toApiObjectJsonSerializer = toApiObjectJsonSerializer ;
    }
    
    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAccount(@PathParam("accountId") final Long accountId, @PathParam("type") final String accountType,
            @Context final UriInfo uriInfo) {
        String serviceName = accountType+AccountsApiConstants.READPLATFORM_NAME ;
        AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName) ;
        AccountData data = service.retrieveOne(accountId) ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams());
    }
    
    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllAccounts(@PathParam("type") final String accountType, @Context final UriInfo uriInfo) {
        String serviceName = accountType+AccountsApiConstants.READPLATFORM_NAME ;
        AccountReadPlatformService service = (AccountReadPlatformService) this.applicationContext.getBean(serviceName) ;
        Collection<AccountData> data = service.retrieveAll() ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, data, service.getResponseDataParams()); 
    }
    
    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createAccount(@PathParam("type") final String accountType, final String apiRequestBodyAsJson) {
        CommandWrapper commandWrapper = null;
        this.platformSecurityContext.authenticatedUser();
        commandWrapper = new CommandWrapperBuilder().createAccount(accountType).withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult commandProcessingResult = this.commandsSourceWritePlatformService.logCommandSource(commandWrapper);
        return this.toApiJsonSerializer.serialize(commandProcessingResult);
    }
    
    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String handleCommands(@PathParam("type") final String accountType, @PathParam("accountId") final Long accountId, @QueryParam("command") final String commandParam,
            @Context final UriInfo uriInfo, final String apiRequestBodyAsJson) {
        String serviceName = accountType.toUpperCase()+AccountsApiConstants.ACCOUNT_COMMANDSERVICE ;
        AccountsCommandsService service = (AccountsCommandsService) this.applicationContext.getBean(serviceName) ;
        final Object obj = service.handleCommand(accountId, commandParam, apiRequestBodyAsJson) ;
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiObjectJsonSerializer.serialize(settings, obj, new HashSet<String>());
    }
    
    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateAccount(@PathParam("type") final String accountType, @PathParam("accountId") final Long accountId, final String apiRequestBodyAsJson) {
        this.platformSecurityContext.authenticatedUser();
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateAccount(accountType, accountId)
                .withJson(apiRequestBodyAsJson).build();
        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        return this.toApiJsonSerializer.serialize(result);
    }
}
