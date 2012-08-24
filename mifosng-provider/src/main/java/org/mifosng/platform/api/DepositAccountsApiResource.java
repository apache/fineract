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
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.apache.commons.lang.StringUtils;
import org.mifosng.platform.api.commands.DepositAccountCommand;
import org.mifosng.platform.api.commands.DepositStateTransitionCommand;
import org.mifosng.platform.api.commands.UndoStateTransitionCommand;
import org.mifosng.platform.api.data.DepositAccountData;
import org.mifosng.platform.api.data.DepositProductLookup;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.exceptions.UnrecognizedQueryParamException;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.saving.service.DepositAccountReadPlatformService;
import org.mifosng.platform.saving.service.DepositAccountWritePlatformService;
import org.mifosng.platform.savingproduct.service.DepositProductReadPlatformService;
import org.mifosng.platform.savingproduct.service.SavingsDepositEnumerations;
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
	private DepositProductReadPlatformService depositProductReadPlatformService;
	
	@Autowired
	private DepositAccountWritePlatformService depositAccountWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;
	
	@Autowired
	private ApiJsonSerializerService apiJsonSerializerService;

	private static final Set<String> typicalResponseParameters = new HashSet<String>(
			Arrays.asList("id", "externalId", "clientId", "clientName",
					"productId", "productName", "status", "currency",
					"deposit", "maturityInterestRate", "tenureInMonths",
					"interestCompoundedEvery",
					"interestCompoundedEveryPeriodType", "renewalAllowed",
					"preClosureAllowed", "preClosureInterestRate"));
	
	
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

		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<DepositAccountData> accounts = this.depositAccountReadPlatformService.retrieveAllDepositAccounts();
		
		return this.apiJsonSerializerService.serializeDepositAccountDataToJson(prettyPrint, responseParameters, accounts);
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
		
		DepositAccountData account = this.depositAccountReadPlatformService.retrieveDepositAccount(accountId);
		
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			account = handleTemplateRelatedData(responseParameters, account);
		}
		
		return this.apiJsonSerializerService.serializeDepositAccountDataToJson(prettyPrint, responseParameters, account);
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
		
		DepositAccountData account = this.depositAccountReadPlatformService.retrieveNewDepositAccountDetails(clientId,productId);
		
		account = handleTemplateRelatedData(responseParameters, account);
		
		return this.apiJsonSerializerService.serializeDepositAccountDataToJson(prettyPrint, responseParameters, account);
	}
	
	@DELETE
	@Path("{accountId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteProduct(@PathParam("accountId") final Long accountId) {

		this.depositAccountWritePlatformService.deleteDepositAccount(accountId);

		return Response.ok(new EntityIdentifier(accountId)).build();
	}
	
	private DepositAccountData handleTemplateRelatedData(
			final Set<String> responseParameters, 
			final DepositAccountData account) {
		
		responseParameters.addAll(Arrays.asList("interestCompoundedEveryPeriodTypeOptions", "productOptions"));
		
		Collection<DepositProductLookup> productOptions = depositProductReadPlatformService.retrieveAllDepositProductsForLookup();
		
		EnumOptionData monthly = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType.MONTHS);
		List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions = Arrays.asList(monthly);
		
		return new DepositAccountData(account, interestCompoundedEveryPeriodTypeOptions, productOptions);
	}
	
	@POST
	@Path("{accountId}")
	@Consumes({ MediaType.APPLICATION_JSON })
	@Produces({ MediaType.APPLICATION_JSON })
	public Response SubmitDepositApplication(@PathParam("accountId") final Long accountId,
			@QueryParam("command") final String commandParam, final String jsonRequestBody) {
		
		DepositStateTransitionCommand command=apiDataConversionService.convertJsonToDepositStateTransitionCommand(accountId, jsonRequestBody);
		
		Response response=null;
		
		if (is(commandParam, "approve")) {

			EntityIdentifier identifier = this.depositAccountWritePlatformService.approveDepositApplication(command);
			response = Response.ok().entity(identifier).build();
		} 
		
		else if (is(commandParam, "reject")) {
			
			EntityIdentifier identifier = this.depositAccountWritePlatformService.rejectDepositApplication(command);
			response = Response.ok().entity(identifier).build();
		
		}  else if (is(commandParam, "withdrewbyclient")) {

			EntityIdentifier identifier = this.depositAccountWritePlatformService.withdrawDepositApplication(command);
			response = Response.ok().entity(identifier).build();
			
		}
		
		UndoStateTransitionCommand undoCommand = new UndoStateTransitionCommand(accountId);
		
		if (is(commandParam, "undoapproval")) {
			EntityIdentifier identifier = this.depositAccountWritePlatformService.undoDepositApproval(undoCommand);
			response = Response.ok().entity(identifier).build();
		}
		
		if (response == null) {
			throw new UnrecognizedQueryParamException("command", commandParam);
		}
		
		return response;
	}
	
	private boolean is(final String commandParam, final String commandValue) {
		return StringUtils.isNotBlank(commandParam)
				&& commandParam.trim().equalsIgnoreCase(commandValue);
	}
}