package org.mifosng.platform.api;

import java.util.Collection;

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
import org.mifosng.platform.api.infrastructure.ApiJSONFormattingService;
import org.mifosng.platform.loanproduct.service.LoanProductReadPlatformService;
import org.mifosng.platform.loanproduct.service.LoanProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
public class LoanProductsApiResource {

	private String defaultFieldList = "principal,inArrearsTolerance,interestRatePerPeriod,annualInterestRate,repaymentFrequencyType,interestRateFrequencyType,amortizationType,interestType,interestCalculationPeriodType,fundId";
	private String allowedFieldList = "currencyOptions,amortizationTypeOptions,interestTypeOptions,interestCalculationPeriodTypeOptions,repaymentFrequencyTypeOptions,interestRateFrequencyTypeOptions,fundOptions";
	private String filterName = "myFilter";

	@Autowired
	private LoanProductReadPlatformService loanProductReadPlatformService;

	@Autowired
	private LoanProductWritePlatformService loanProductWritePlatformService;

	@Autowired
	private ApiDataConversionService apiDataConversionService;

	@Autowired
	private ApiJSONFormattingService jsonFormattingService;

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

		Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(products, filterName,
				allowedFieldList, selectedFields, uriInfo.getQueryParameters());
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

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();

		String selectedFields = defaultFieldList + "," + allowedFieldList;
		return this.jsonFormattingService.convertRequest(loanProduct,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
	}

	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveLoanProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);

		String selectedFields = "";
		return this.jsonFormattingService.convertRequest(loanProduct,
				filterName, allowedFieldList, selectedFields,
				uriInfo.getQueryParameters());
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