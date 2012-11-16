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

import org.mifosng.platform.api.commands.DepositProductCommand;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.DepositProductData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.loan.domain.PeriodFrequencyType;
import org.mifosng.platform.savingproduct.service.DepositProductReadPlatformService;
import org.mifosng.platform.savingproduct.service.DepositProductWritePlatformService;
import org.mifosng.platform.savingproduct.service.SavingsDepositEnumerations;
import org.mifosng.platform.security.PlatformSecurityContext;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositproducts")
@Component
@Scope("singleton")
public class DepositProductsApiResource {
	
	@Autowired
	private DepositProductReadPlatformService depositProductReadPlatformService;
	
	@Autowired
	private CurrencyReadPlatformService currencyReadPlatformService;
	
	@Autowired 
	private DepositProductWritePlatformService depositProductWritePlatformService;
	
	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;
	
	@Autowired
	private PortfolioApiJsonSerializerService apiJsonSerializerService;

	private final String entityType = "DEPOSITPRODUCT";
	@Autowired
	private PlatformSecurityContext context;
	
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createDepositProduct(final String jsonRequestBody){
		
		final DepositProductCommand command=this.apiDataConversionService.convertJsonToDepositProductCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier=this.depositProductWritePlatformService.createDepositProduct(command);
		
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@PUT
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateDepositProduct(@PathParam("productId") final Long productId, final String jsonRequestBody){
		
		final DepositProductCommand command=this.apiDataConversionService.convertJsonToDepositProductCommand(productId, jsonRequestBody);
		EntityIdentifier entityIdentifier=this.depositProductWritePlatformService.updateDepositProduct(command);
		return Response.ok().entity(entityIdentifier).build();
	}
	
	@DELETE
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response deleteDepositProduct(@PathParam("productId") final Long productId) {

		this.depositProductWritePlatformService.deleteDepositProduct(productId);

		return Response.ok(new EntityIdentifier(productId)).build();
	}
	
	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllDepositProducts(@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "externalId", "name", "description", "createdOn", "lastModifedOn",
				"currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance","tenureInMonths",
				"maturityDefaultInterestRate","maturityMinInterestRate","maturityMaxInterestRate",
				"interestCompoundedEvery", "interestCompoundedEveryPeriodType",
				"renewalAllowed","preClosureAllowed","preClosureInterestRate","interestCompoundingAllowed",
				"isLockinPeriodAllowed","lockinPeriod","lockinPeriodType"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		Collection<DepositProductData> products=this.depositProductReadPlatformService.retrieveAllDepositProducts();
		return this.apiJsonSerializerService.serializeDepositProductDataToJson(prettyPrint, responseParameters, products);
	}
	
	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveDepositProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "externalId", "name", "description", "createdOn", "lastModifedOn",
				"currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance","tenureInMonths",
				"maturityDefaultInterestRate","maturityMinInterestRate","maturityMaxInterestRate",
				"interestCompoundedEvery", "interestCompoundedEveryPeriodType",
				"renewalAllowed","preClosureAllowed","preClosureInterestRate","interestCompoundingAllowed",
				"isLockinPeriodAllowed","lockinPeriod","lockinPeriodType"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		
		DepositProductData productData = this.depositProductReadPlatformService.retrieveDepositProductData(productId);
		
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());
		if (template) {
			productData = handleTemplateRelatedData(responseParameters, productData);
		}
		
		return this.apiJsonSerializerService.serializeDepositProductDataToJson(prettyPrint, responseParameters, productData);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewDepositProductDetails(@Context final UriInfo uriInfo) {

    	context.authenticatedUser().validateHasReadPermission(entityType);
    	
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("currencyOptions", "interestCompoundedEveryPeriodTypeOptions", 
				"id", "externalId", "name", "description", "createdOn", "lastModifedOn",
				"currencyCode","digitsAfterDecimal","minimumBalance","maximumBalance","tenureInMonths",
				"maturityDefaultInterestRate","maturityMinInterestRate","maturityMaxInterestRate",
				"interestCompoundedEvery", "interestCompoundedEveryPeriodType",
				"renewalAllowed","preClosureAllowed","preClosureInterestRate",
				"isLockinPeriodAllowed","lockinPeriod","lockinPeriodType"));
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveNewDepositProductDetails();
		
		depositProduct = handleTemplateRelatedData(responseParameters, depositProduct);
		
		return this.apiJsonSerializerService.serializeDepositProductDataToJson(prettyPrint, responseParameters, depositProduct);
	}
	
	private DepositProductData handleTemplateRelatedData(
			final Set<String> responseParameters, 
			final DepositProductData productData) {
		
		responseParameters.addAll(Arrays.asList("currencyOptions", "interestCompoundedEveryPeriodTypeOptions"));
		List<CurrencyData> allowedCurrencies = this.currencyReadPlatformService.retrieveAllowedCurrencies();
		
		EnumOptionData monthly = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType.MONTHS);
		List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions = Arrays.asList(monthly);
		
		return new DepositProductData(productData, allowedCurrencies, interestCompoundedEveryPeriodTypeOptions);
	}
}