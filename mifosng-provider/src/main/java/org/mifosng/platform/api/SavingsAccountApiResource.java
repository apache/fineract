package org.mifosng.platform.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
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

import org.mifosng.platform.api.commands.SavingAccountCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.SavingAccountData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.saving.service.SavingAccountReadPlatformService;
import org.mifosng.platform.saving.service.SavingAccountWritePlatformService;
import org.mifosng.platform.savingproduct.service.SavingProductReadPlatformService;
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
	private SavingProductReadPlatformService savingProductReadPlatformService;
	
	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;
	
	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "status", "externalId", "clientId", "clientName", "productId", "productName", "productType", "currencyData", "savingsDepostiAmountPerPeriod", "savingsFrequencyType", 
					"totalDepositAmount", "reccuringInterestRate", "savingInterestRate", "interestType", "interestCalculationMethod", "tenure", "tenureType", "projectedCommencementDate", 
					"actualCommencementDate", "maturesOnDate", "projectedInterestAccuredOnMaturity", "actualInterestAccured", "projectedMaturityAmount", "actualMaturityAmount", "preClosureAllowed", 
					"preClosureInterestRate", "withdrawnonDate", "rejectedonDate", "closedonDate", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType"));
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createSavingAccount(final String jsonRequestBody){
		
		final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.savingAccountWritePlatformService.createSavingAccount(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@PUT
	@Path("{accountId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateSavingAccount(@PathParam("accountId") final Long accountId,final String jsonRequestBody){
		
		final SavingAccountCommand command = this.apiDataConversionService.convertJsonToSavingAccountCommand(accountId, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.savingAccountWritePlatformService.updateSavingAccount(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	 
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

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
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {
		
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
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		SavingAccountData account = this.savingAccountReadPlatformService.retrieveNewSavingsAccountDetails(clientId, productId);
		
		return this.apiJsonSerializerService.serializeSavingAccountsDataToJson(prettyPrint, responseParameters, account);
	}

}
