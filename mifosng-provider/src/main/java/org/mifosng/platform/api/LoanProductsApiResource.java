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
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.infrastructure.ApiDataConversionService;
import org.mifosng.platform.api.infrastructure.ApiParameterHelper;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.loanproduct.service.LoanProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
public class LoanProductsApiResource {

	@Autowired
	private LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	private LoanProductWritePlatformService loanProductWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response createLoanProduct(final String jsonRequestBody) {
		
		final LoanProductCommand command = this.apiDataConversionService.convertJsonToLoanProductCommand(null, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService.createLoanProduct(command);

		return Response.ok().entity(entityIdentifier).build();
	}

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "description", "fundId", "fundName", "principal", "inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod", "annualInterestRate", 
						"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
						"createdOn", "lastModifedOn")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();
		
		return this.apiDataConversionService.convertLoanProductDataToJson(prettyPrint, responseParameters, products.toArray(new LoanProductData[products.size()]));
	}

	/*
	 * see
	 * http://stackoverflow.com/questions/5250074/what-uri-can-be-used-to-request
	 * -a-default-resource
	 */
	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewLoanProductDetails(@Context final UriInfo uriInfo) {
		
		// TODO - KW - every addition of a parameter here requires same additon in supported params used in 'apiDataConversionService' method.
		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "description", "fundId", "fundName", "transactionProcessingStrategyId", "transactionProcessingStrategyName",  
						"principal", "inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod", "annualInterestRate", 
						"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
						"createdOn", "lastModifedOn","currencyOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", 
						"repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "transactionProcessingStrategyOptions")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
		
		return this.apiDataConversionService.convertLoanProductDataToJson(prettyPrint, responseParameters, loanProduct);
	}

	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveLoanProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

		Set<String> typicalResponseParameters = new HashSet<String>(
				Arrays.asList("id", "name", "description", "fundId", "fundName", 
						"transactionProcessingStrategyId", "transactionProcessingStrategyName",
						"principal", "inArrearsTolerance", "numberOfRepayments",
						"repaymentEvery", "interestRatePerPeriod", "annualInterestRate", 
						"repaymentFrequencyType", "interestRateFrequencyType", "amortizationType", "interestType", "interestCalculationPeriodType",
						"createdOn", "lastModifedOn")
		);
		
		Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		if (responseParameters.isEmpty()) {
			responseParameters.addAll(typicalResponseParameters);
		}
		boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
		if (template) {
			responseParameters.addAll(Arrays.asList("currencyOptions", "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions", 
					"repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "fundOptions", "transactionProcessingStrategyOptions"));
		}
		
		return this.apiDataConversionService.convertLoanProductDataToJson(prettyPrint, responseParameters, loanProduct);
	}

	@PUT
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public Response updateLoanProduct(@PathParam("productId") final Long productId, final String jsonRequestBody) {
		
		LoanProductCommand command = this.apiDataConversionService.convertJsonToLoanProductCommand(productId, jsonRequestBody);
		
		EntityIdentifier entityIdentifier = this.loanProductWritePlatformService.updateLoanProduct(command);

		return Response.ok().entity(entityIdentifier).build();
	}
}