/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsdepositaccount.api;

import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
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
import javax.ws.rs.core.Response.ResponseBuilder;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingsDepositEnumerations;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountData;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositAccountsForLookup;
import org.mifosplatform.portfolio.savingsdepositaccount.data.DepositPermissionData;
import org.mifosplatform.portfolio.savingsdepositaccount.service.DepositAccountReadPlatformService;
import org.mifosplatform.portfolio.savingsdepositaccount.service.DepositAccountWritePlatformService;
import org.mifosplatform.portfolio.savingsdepositaccount.service.GeneratePDF;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductLookup;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositaccounts")
@Component
@Scope("singleton")
public class DepositAccountsApiResource {
	
	private static final Set<String> SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("productOptions",
            "interestCompoundedEveryPeriodTypeOptions", "id", "externalId", "clientId", "clientName", "productId", "productName", "status",
            "currency", "deposit", "maturityInterestRate", "tenureInMonths", "interestCompoundedEvery",
            "interestCompoundedEveryPeriodType", "renewalAllowed", "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate",
            "rejectedonDate", "closedonDate", "transactions", "permissions", "isInterestWithdrawable", "interestPaid",
            "interestCompoundingAllowed", "availableInterestForWithdrawal", "availableWithdrawalAmount", "todaysDate",
            "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "printFDdetailsLocation", "availableInterest",
            "interestPostedAmount", "lastInterestPostedDate", "nextInterestPostedDate", "fatherName", "address", "imageKey"));

    private final DepositAccountReadPlatformService depositAccountReadPlatformService;

    private final DepositProductReadPlatformService depositProductReadPlatformService;

    private final DepositAccountWritePlatformService depositAccountWritePlatformService;

    private final String entityType = "DEPOSITACCOUNT";
    
    private final PlatformSecurityContext context;
    
    private final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer;
    
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public DepositAccountsApiResource(final DepositAccountReadPlatformService depositAccountReadPlatformService,
    		final DepositProductReadPlatformService depositProductReadPlatformService,
    		final DepositAccountWritePlatformService depositAccountWritePlatformService,
    		final PlatformSecurityContext context, 
    		final DefaultToApiJsonSerializer<DepositAccountData> toApiJsonSerializer,
    		final ApiRequestParameterHelper apiRequestParameterHelper,
    		final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService ) {
    	this.depositAccountReadPlatformService = depositAccountReadPlatformService;
    	this.depositProductReadPlatformService = depositProductReadPlatformService;
    	this.depositAccountWritePlatformService = depositAccountWritePlatformService;
    	this.context = context;
    	this.toApiJsonSerializer = toApiJsonSerializer;
    	this.apiRequestParameterHelper = apiRequestParameterHelper;
    	this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
	}

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDepositAccount(final String apiRequestBodyAsJson) {

    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createDepositAccount().withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateDepositAccount(@PathParam("accountId") final Long accountId, final String apiRequestBodyAsJson) {

    	final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDepositAccount(accountId).withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Collection<DepositAccountData> accounts = this.depositAccountReadPlatformService.retrieveAllDepositAccounts();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        return this.toApiJsonSerializer.serialize(settings, accounts, SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);
        
        DepositPermissionData permissions = null;

        DepositAccountData account = this.depositAccountReadPlatformService.retrieveDepositAccount(accountId);
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            account = handleTemplateRelatedData(account);
        }

        Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
            	 permissions = this.depositAccountReadPlatformService.retrieveDepositAccountsPermissions(account);
                 account = new DepositAccountData(account, permissions, account.getTransactions());
            }
        }

        return this.toApiJsonSerializer.serialize(settings, account, SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewDepositAccountDetails(@QueryParam("clientId") final Long clientId,
            @QueryParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        DepositAccountData account = this.depositAccountReadPlatformService.retrieveNewDepositAccountDetails(clientId, productId);

        account = handleTemplateRelatedData(account);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        return this.toApiJsonSerializer.serialize(settings, account, SAVINGS_DEPOSIT_ACCOUNT_DATA_PARAMETERS);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteProduct(@PathParam("accountId") final Long accountId) {

    	final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDepositAccount(accountId).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private DepositAccountData handleTemplateRelatedData(final DepositAccountData account) {

        Collection<DepositProductLookup> productOptions = depositProductReadPlatformService.retrieveAllDepositProductsForLookup();

        EnumOptionData monthly = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType.MONTHS);
        List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions = Arrays.asList(monthly);

        return new DepositAccountData(account, interestCompoundedEveryPeriodTypeOptions, productOptions);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String SubmitDepositApplication(@PathParam("accountId") final Long accountId,
            @QueryParam("command") final String commandParam, final String apiRequestBodyAsJson) {
    	
    	CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
    	CommandProcessingResult result = null;

        if (is(commandParam, "approve")) {
        	final CommandWrapper commandRequest = builder.approveDepositApplication(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrawal")) {
        	final CommandWrapper commandRequest = builder.withdrawDepositAmount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "interestwithdraw")) {
        	final CommandWrapper commandRequest = builder.withdrawInterestDepositAmount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "renew")) {
        	final CommandWrapper commandRequest = builder.renewDepositAccount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reject")) {
        	final CommandWrapper commandRequest = builder.rejectDepositAccount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrewbyclient")) {
        	final CommandWrapper commandRequest = builder.withdrawDepositApplication(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
        	final CommandWrapper commandRequest = builder.undoDepositApplicationApproval(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else { throw new UnrecognizedQueryParamException("command", commandParam); }

        return this.toApiJsonSerializer.serialize(result);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @POST
    @Path("postinterest")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String postInterest() {
        Collection<DepositAccountsForLookup> accounts = this.depositAccountReadPlatformService.retrieveDepositAccountForLookup();
        CommandProcessingResult entityIdentifier = this.depositAccountWritePlatformService.postInterestToDepositAccount(accounts);
        return this.toApiJsonSerializer.serialize(entityIdentifier);

    }

    @SuppressWarnings("null")
	@GET
    @Path("{accountId}/print")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response printDepositAccount(@PathParam("accountId") final Long accountId) {

        DepositAccountData account = this.depositAccountReadPlatformService.retrieveDepositAccount(accountId);
        String printFileName = null;
        if (account != null) printFileName = new GeneratePDF(account).generatePDF();
        account.setPrintFDdetailsLocation(printFileName);

        File file = new File(printFileName);
        ResponseBuilder response = Response.ok(file);
        response.header("Content-Disposition", "attachment; filename=\"" + printFileName + "\"");
        response.header("Content-Type", "applicatio/pdf");
        return response.build();
    }
}