package org.mifosng.platform.api;

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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DepositAccountData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.savingproduct.service.DepositAccountReadPlatformService;
import org.mifosng.platform.savingproduct.service.DepositAccountWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositaccounts")
@Component
@Scope("singleton")
public class DepositAccountsApiResource {
	
	@Autowired
	private DepositAccountReadPlatformService depositAccountReadPlatformService;
	
	@Autowired
	private CurrencyReadPlatformService currencyReadPlatformService;
	
	@Autowired
	private DepositAccountWritePlatformService depositAccountWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createDepositAccount(final String jsonRequestBody){
		
		final DepositAccountCommand command = this.apiDataConversionService.convertJsonToDepositAccountCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.depositAccountWritePlatformService.createDepositAccount(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@PUT
	@Path("{accountId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateSavingProduct(@PathParam("accountId") final Long accountId, final String jsonRequestBody){
		
		final DepositAccountCommand command = this.apiDataConversionService.convertJsonToDepositAccountCommand(accountId, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.depositAccountWritePlatformService.updateDepositAccount(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllDepositAccounts(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("createdOn", "lastModifedOn", 
						"id", "externalId", "clientId", "clientName", "productId", "productName", 
						"currency", "deposit", "maturityInterestRate", "tenureInMonths", "interestCompoundedEvery", "interestCompoundedEveryPeriodType",
						"renewalAllowed","preClosureAllowed","preClosureInterestRate"
						)
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<DepositAccountData> accounts = this.depositAccountReadPlatformService.retrieveAllDepositAccounts();
		
		return this.apiDataConversionService.convertDepositAccountDataToJson(prettyPrint, responseParameters, accounts.toArray(new DepositAccountData[accounts.size()]));
	}
	
	@GET
	@Path("{accountId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveDepositAccount(@PathParam("accountId") final Long accountId, @Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("createdOn", "lastModifedOn", 
						"id", "clientId", "clientName", "productId", "productName", 
						"currency", "deposit", "maturityInterestRate")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		DepositAccountData account = this.depositAccountReadPlatformService.retrieveDepositAccount(accountId);
		
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			responseParameters.addAll(Arrays.asList("currencyOptions"));
			List<CurrencyData> allowedCurrencies = this.currencyReadPlatformService.retrieveAllowedCurrencies();
			account = new DepositAccountData(account, allowedCurrencies);
		}
		
		return this.apiDataConversionService.convertDepositAccountDataToJson(prettyPrint, responseParameters, account);
	}
	
	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewDepositAccountDetails(@Context final UriInfo uriInfo) {
		
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("createdOn", "lastModifedOn", 
						"id", "clientId", "clientName", "productId", "productName", 
						"currency", "deposit", "maturityInterestRate", "currencyOptions")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		DepositAccountData account = this.depositAccountReadPlatformService.retrieveNewDepositAccountDetails();
		List<CurrencyData> allowedCurrencies = this.currencyReadPlatformService.retrieveAllowedCurrencies();
		account = new DepositAccountData(account, allowedCurrencies);
		
		return this.apiDataConversionService.convertDepositAccountDataToJson(prettyPrint, responseParameters, account);
	}
	
	@DELETE
	@Path("{accountId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteProduct(@PathParam("accountId") final Long accountId) {

		this.depositAccountWritePlatformService.deleteDepositAccount(accountId);

		return Response.ok(new EntityIdentifier(accountId)).build();
	}
}