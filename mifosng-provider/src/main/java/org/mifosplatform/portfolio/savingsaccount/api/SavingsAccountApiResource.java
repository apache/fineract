package org.mifosplatform.portfolio.savingsaccount.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

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
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.api.PortfolioApiDataConversionService;
import org.mifosplatform.infrastructure.core.api.PortfolioApiJsonSerializerService;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.savingsaccount.command.CalculateSavingScheduleCommand;
import org.mifosplatform.portfolio.savingsaccount.command.SavingAccountCommand;
import org.mifosplatform.portfolio.savingsaccount.data.SavingAccountData;
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

@Path("/savingaccounts")
@Component
@Scope("singleton")
public class SavingsAccountApiResource {

    @Autowired
    private SavingAccountWritePlatformService savingAccountWritePlatformService;

    @Autowired
    private SavingAccountReadPlatformService savingAccountReadPlatformService;

    @Autowired
    private PortfolioApiDataConversionService apiDataConversionService;

    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;
    
    @Autowired 
    private SavingProductReadPlatformService savingProductReadPlatformService;
    
    @Autowired
    private CurrencyReadPlatformService currencyReadPlatformService;

    private final String entityType = "SAVINGSACCOUNT";

    @Autowired
    private PlatformSecurityContext context;

    @Autowired
    private CalculateSavingSchedule calculateSavingSchedule;

    @Autowired
    private ToApiJsonSerializer<SavingAccountData> toApiJsonSerializer;

    private static final Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "status", "externalId",
            "clientId", "clientName", "productId", "productName", "productType", "currencyData", "savingsDepostiAmountPerPeriod",
            "savingsFrequencyType", "totalDepositAmount", "reccuringInterestRate", "savingInterestRate", "interestType",
            "interestCalculationMethod", "tenure", "tenureType", "projectedCommencementDate", "actualCommencementDate", "maturesOnDate",
            "projectedInterestAccuredOnMaturity", "actualInterestAccured", "projectedMaturityAmount", "actualMaturityAmount",
            "preClosureAllowed", "preClosureInterestRate", "withdrawnonDate", "rejectedonDate", "closedonDate", "isLockinPeriodAllowed",
            "lockinPeriod", "lockinPeriodType"));

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSavingAccount(@QueryParam("command") final String commandParam, @Context final UriInfo uriInfo,
            final String jsonRequestBody) {

        final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(null, jsonRequestBody);

        if (is(commandParam, "calculateSavingSchedule")) {
            CalculateSavingScheduleCommand calculateSavingScheduleCommand = command.toCalculateSavingScheduleCommand();
            return calculateSavingSchedule(uriInfo, calculateSavingScheduleCommand);
        }

        CommandProcessingResult entityIdentifier = this.savingAccountWritePlatformService.createSavingAccount(command);

        return this.toApiJsonSerializer.serialize(entityIdentifier);
    }

    private String calculateSavingSchedule(final UriInfo uriInfo, final CalculateSavingScheduleCommand command) {

        SavingScheduleData savingScheduleData = this.calculateSavingSchedule.calculateSavingSchedule(command);
        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        return this.apiJsonSerializerService.serializeSavingScheduleDataToJson(prettyPrint, responseParameters, savingScheduleData);
    }

    @PUT
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public Response updateSavingAccount(@PathParam("accountId") final Long accountId, final String jsonRequestBody) {

        final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(accountId, jsonRequestBody);

        CommandProcessingResult entityIdentifier = this.savingAccountWritePlatformService.updateSavingAccount(command);

        return Response.ok().entity(entityIdentifier).build();
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<SavingAccountData> accounts = this.savingAccountReadPlatformService.retrieveAllSavingsAccounts();

        return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, accounts);
    }

    @GET
    @Path("{accountId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());

        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
		
		
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		SavingAccountData account = this.savingAccountReadPlatformService.retrieveSavingsAccount(accountId);
		
		return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, account);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewDepositAccountDetails(
			@QueryParam("clientId") final Long clientId,
			@QueryParam("productId") final Long productId,
			@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		SavingAccountData account = this.savingAccountReadPlatformService.retrieveNewSavingsAccountDetails(clientId, productId);
		account = handleTemplateRelatedData(responseParameters,account);
		
		return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, account);
	}
	
    private SavingAccountData handleTemplateRelatedData(Set<String> responseParameters, SavingAccountData account) {
    	responseParameters.addAll(Arrays.asList("productOptions", "currencyOptions", "savingsProductTypeOptions", "tenureTypeOptions",
                "savingFrequencyOptions", "savingsInterestTypeOptions", "lockinPeriodTypeOptions", "interestCalculationOptions"));
    	
    	 Collection<SavingProductLookup> productOptions = savingProductReadPlatformService.retrieveAllSavingProductsForLookup();
    	 List<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();

         EnumOptionData reccuring = SavingProductEnumerations.savingProductType(SavingProductType.RECCURING);
         EnumOptionData regular = SavingProductEnumerations.savingProductType(SavingProductType.REGULAR);
         List<EnumOptionData> savingsProductTypeOptions = Arrays.asList(reccuring, regular);

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
         EnumOptionData minbal = SavingProductEnumerations.savingInterestCalculationMethod(SavingInterestCalculationMethod.MINBAL);
         EnumOptionData monthlyCollection = SavingProductEnumerations
                 .savingInterestCalculationMethod(SavingInterestCalculationMethod.MONTHLYCOLLECTION);
         List<EnumOptionData> interestCalculationOptions = Arrays.asList(averagebal, minbal, monthlyCollection);
		return new SavingAccountData(account, productOptions, currencyOptions, savingsProductTypeOptions,tenureTypeOptions,savingFrequencyOptions,
				savingsInterestTypeOptions,lockinPeriodTypeOptions,interestCalculationOptions);
	}

	private boolean is(final String commandParam, final String commandValue) {
        return StringUtils.isNotBlank(commandParam) && commandParam.trim().equalsIgnoreCase(commandValue);
    }

}