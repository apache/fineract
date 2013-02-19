/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccount.api;

import java.math.BigDecimal;
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
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.api.JsonQuery;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.exception.UnrecognizedQueryParamException;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.core.serialization.FromJsonHelper;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountForLookup;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountTransactionsData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingPermissionData;
import org.mifosplatform.portfolio.savingsaccount.data.SavingScheduleData;
import org.mifosplatform.portfolio.savingsaccount.service.CalculateSavingSchedule;
import org.mifosplatform.portfolio.savingsaccount.service.SavingAccountReadPlatformService;
import org.mifosplatform.portfolio.savingsaccount.service.SavingAccountWritePlatformService;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductLookup;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingInterestCalculationMethod;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingProductType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsInterestType;
import org.mifosplatform.portfolio.savingsaccountproduct.domain.SavingsLockinPeriodEnum;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductEnumerations;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingProductReadPlatformService;
import org.mifosplatform.portfolio.savingsdepositproduct.domain.TenureTypeEnum;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import com.google.gson.JsonElement;

@Path("/savingaccounts")
@Component
@Scope("singleton")
public class SavingsAccountApiResource {
	
	private final Set<String> SAVINGS_ACCOUNTS_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "status", "externalId",
            "clientId", "clientName", "productId", "productName", "productType", "currencyData", "savingsDepostiAmountPerPeriod",
            "savingsFrequencyType", "totalDepositAmount", "reccuringInterestRate", "savingInterestRate", "interestType",
            "interestCalculationMethod", "tenure", "tenureType", "projectedCommencementDate", "actualCommencementDate", "maturesOnDate",
            "projectedInterestAccuredOnMaturity", "actualInterestAccured", "projectedMaturityAmount", "actualMaturityAmount","outstandingAmount",
            "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate", "rejectedonDate", "closedonDate", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType", "productOptions", "currencyOptions", "savingsProductTypeOptions", "tenureTypeOptions",
            "savingFrequencyOptions", "savingsInterestTypeOptions", "lockinPeriodTypeOptions", "interestCalculationOptions","permissions","savingScheduleDatas",
            "dueAmount", "savingScheduleData", "transactions","interestPostEvery","interestPostFrequency"));

    private final SavingAccountWritePlatformService savingAccountWritePlatformService;
    private final SavingAccountReadPlatformService savingAccountReadPlatformService;
    private final SavingProductReadPlatformService savingProductReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final String entityType = "SAVINGSACCOUNT";
    private final PlatformSecurityContext context;
    private final CalculateSavingSchedule calculateSavingSchedule;
    private final ToApiJsonSerializer<SavingAccountData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final FromJsonHelper fromJsonHelper;
    private final DefaultToApiJsonSerializer<SavingScheduleData> savingScheduleToApiJsonSerializer;
    
    @Autowired
    public SavingsAccountApiResource(final SavingAccountWritePlatformService savingAccountWritePlatformService,
    		final SavingAccountReadPlatformService savingAccountReadPlatformService,
    		final SavingProductReadPlatformService savingProductReadPlatformService,
    		final CurrencyReadPlatformService currencyReadPlatformService,
    		final PlatformSecurityContext context,final CalculateSavingSchedule calculateSavingSchedule,
    		final ToApiJsonSerializer<SavingAccountData> toApiJsonSerializer,
    		final ApiRequestParameterHelper apiRequestParameterHelper,
    		final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
    		final FromJsonHelper fromJsonHelper,
    		final DefaultToApiJsonSerializer<SavingScheduleData> savingScheduleToApiJsonSerializer) {
    	this.savingAccountWritePlatformService = savingAccountWritePlatformService;
    	this.savingAccountReadPlatformService = savingAccountReadPlatformService;
    	this.savingProductReadPlatformService = savingProductReadPlatformService;
    	this.currencyReadPlatformService = currencyReadPlatformService;
    	this.context = context;
    	this.calculateSavingSchedule = calculateSavingSchedule;
    	this.toApiJsonSerializer = toApiJsonSerializer;
    	this.apiRequestParameterHelper = apiRequestParameterHelper;
    	this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    	this.fromJsonHelper = fromJsonHelper;
    	this.savingScheduleToApiJsonSerializer = savingScheduleToApiJsonSerializer;
	}

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSavingAccount(@QueryParam("command") final String commandParam, @Context final UriInfo uriInfo,
            final String apiRequestBodyAsJson) {
    	
    	if (is(commandParam, "calculateSavingSchedule")) {
    		
    		final JsonElement parsedQuery = this.fromJsonHelper.parse(apiRequestBodyAsJson);
            final JsonQuery query = JsonQuery.from(apiRequestBodyAsJson, parsedQuery, this.fromJsonHelper);
    		
            SavingScheduleData savingSchedule = this.calculateSavingSchedule.calculateSavingSchedule(query);
            
            final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
            
            return this.savingScheduleToApiJsonSerializer.serialize(settings, savingSchedule, new HashSet<String>());
            
        }
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingAccount().withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }


    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateSavingAccount(@PathParam("accountId") final Long accountId, final String apiRequestBodyAsJson) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingAccount(accountId).withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Collection<SavingAccountData> accounts = this.savingAccountReadPlatformService.retrieveAllSavingsAccounts();
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, accounts, SAVINGS_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        SavingPermissionData permissions = null;
        SavingScheduleData savingScheduleData = null;
        Collection<SavingAccountTransactionsData> transactions = null;

        SavingAccountData account = this.savingAccountReadPlatformService.retrieveSavingsAccount(accountId);
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            account = handleTemplateRelatedData(account);
        }

        Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("all")) {
                associationParameters.addAll(Arrays.asList("savingScheduleData", "transactions"));
            } 

            if (associationParameters.contains("savingScheduleData")) {
                CurrencyData currencyData = account.getCurrencyData();
                savingScheduleData = this.savingAccountReadPlatformService.retrieveSavingsAccountSchedule(accountId, currencyData);
                account = new SavingAccountData(account, savingScheduleData);
            }
            if (associationParameters.contains("transactions")) {
                transactions = this.savingAccountReadPlatformService.retrieveSavingsAccountTransactions(accountId);
                account = new SavingAccountData(account, transactions);
            }
            permissions = this.savingAccountReadPlatformService.retrieveSavingAccountPermissions(account);
            account = new SavingAccountData(account, permissions);
        }

        return this.toApiJsonSerializer.serialize(settings, account, SAVINGS_ACCOUNTS_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewDepositAccountDetails(@QueryParam("clientId") final Long clientId,
            @QueryParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        SavingAccountData account = this.savingAccountReadPlatformService.retrieveNewSavingsAccountDetails(clientId, productId);
        account = handleTemplateRelatedData(account);
        
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, account, SAVINGS_ACCOUNTS_DATA_PARAMETERS);
    }

    private SavingAccountData handleTemplateRelatedData(SavingAccountData account) {
    	
        Collection<SavingProductLookup> productOptions = savingProductReadPlatformService.retrieveAllSavingProductsForLookup();
        Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        EnumOptionData recurring = SavingProductEnumerations.savingProductType(SavingProductType.RECURRING);
        EnumOptionData regular = SavingProductEnumerations.savingProductType(SavingProductType.REGULAR);
        List<EnumOptionData> savingsProductTypeOptions = Arrays.asList(recurring, regular);

        EnumOptionData fixed = SavingProductEnumerations.tenureTypeEnum(TenureTypeEnum.FIXED_PERIOD);
        EnumOptionData perpetual = SavingProductEnumerations.tenureTypeEnum(TenureTypeEnum.PERPETUAL);
        List<EnumOptionData> tenureTypeOptions = Arrays.asList(fixed, perpetual);

        EnumOptionData monthly = SavingProductEnumerations.interestFrequencyType(SavingFrequencyType.MONTHLY);
        List<EnumOptionData> savingFrequencyOptions = Arrays.asList(monthly);

        EnumOptionData simple = SavingProductEnumerations.savingInterestType(SavingsInterestType.SIMPLE);
        EnumOptionData compounding = SavingProductEnumerations.savingInterestType(SavingsInterestType.COMPOUNDING);
        List<EnumOptionData> savingsInterestTypeOptions = Arrays.asList(simple, compounding);

        EnumOptionData months = SavingProductEnumerations.savingsLockinPeriod(SavingsLockinPeriodEnum.MONTHS);
        List<EnumOptionData> lockinPeriodTypeOptions = Arrays.asList(months);

        EnumOptionData averagebal = SavingProductEnumerations.savingInterestCalculationMethod(SavingInterestCalculationMethod.AVERAGEBAL);
        // EnumOptionData minbal =
        // SavingProductEnumerations.savingInterestCalculationMethod(SavingInterestCalculationMethod.MINBAL);
        EnumOptionData monthlyCollection = SavingProductEnumerations
                .savingInterestCalculationMethod(SavingInterestCalculationMethod.MONTHLYCOLLECTION);
        List<EnumOptionData> interestCalculationOptions = Arrays.asList(averagebal, monthlyCollection);
        BigDecimal dueAmount = this.savingAccountReadPlatformService.deriveSavingDueAmount(account);
        return new SavingAccountData(account, productOptions, currencyOptions, savingsProductTypeOptions, tenureTypeOptions,
                savingFrequencyOptions, savingsInterestTypeOptions, lockinPeriodTypeOptions, interestCalculationOptions, dueAmount);
    }

    private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

    @POST
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String savingStateTransitions(@PathParam("accountId") final Long accountId, @QueryParam("command") final String commandParam,
            final String apiRequestBodyAsJson) {
    	
    	CommandWrapperBuilder builder = new CommandWrapperBuilder().withJson(apiRequestBodyAsJson);
    	CommandProcessingResult result = null;
    	
        if (is(commandParam, "approve")) {
        	final CommandWrapper commandRequest = builder.approveSavingAccountApplication(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "depositmoney")) {
        	final CommandWrapper commandRequest = builder.depositOfSavingAmount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdraw")) {
        	final CommandWrapper commandRequest = builder.withdrawSavingAmount(accountId).build();
        	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "reject")) {
           	final CommandWrapper commandRequest = builder.rejectSavingApplication(accountId).build();
           	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "withdrewbyclient")) {
           	final CommandWrapper commandRequest = builder.withdrawSavingApplication(accountId).build();
           	result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else if (is(commandParam, "undoapproval")) {
            final CommandWrapper commandRequest = builder.undoApprovalOfSavingApplication(accountId).build();
            result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);
        } else { throw new UnrecognizedQueryParamException("command", commandParam); }
        
        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteSavingAccount(@PathParam("accountId") final Long accountId) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingAccount(accountId).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @POST
    @Path("postinterest")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String postInterest() {

        Collection<SavingAccountForLookup> savingAccounts = this.savingAccountReadPlatformService.retrieveSavingAccountsForLookUp();
        CommandProcessingResult entityIdentifier = this.savingAccountWritePlatformService.postInterest(savingAccounts);
        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

}