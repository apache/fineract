package org.mifosng.platform.api;

import java.util.Collection;
import java.util.List;
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
import javax.ws.rs.core.UriInfo;

import org.mifosng.platform.api.commands.LoanProductCommand;
import org.mifosng.platform.api.data.ChargeData;
import org.mifosng.platform.api.data.CurrencyData;
import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.data.FundData;
import org.mifosng.platform.api.data.LoanProductData;
import org.mifosng.platform.api.data.TransactionProcessingStrategyData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.charge.service.ChargeReadPlatformService;
import org.mifosng.platform.currency.service.CurrencyReadPlatformService;
import org.mifosng.platform.fund.service.FundReadPlatformService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosng.platform.loanproduct.service.LoanDropdownReadPlatformService;
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
    private ChargeReadPlatformService chargeReadPlatformService;

	@Autowired
	private PortfolioApiDataConversionService apiDataConversionService;
	
    @Autowired
    private PortfolioApiJsonSerializerService apiJsonSerializerService;
    
    @Autowired
    private CurrencyReadPlatformService currencyReadPlatformService;
    
    @Autowired
    private FundReadPlatformService fundReadPlatformService;
    
    @Autowired
    private LoanDropdownReadPlatformService dropdownReadPlatformService;
    
	@POST
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String createLoanProduct(final String jsonRequestBody) {
		
		final LoanProductCommand command = this.apiDataConversionService.convertJsonToLoanProductCommand(null, jsonRequestBody);
		
		final EntityIdentifier identifier = this.loanProductWritePlatformService.createLoanProduct(command);

		return apiJsonSerializerService.serializeEntityIdentifier(identifier);
	}

	@GET
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		final Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();
		
		return this.apiJsonSerializerService.serializeLoanProductDataToJson(prettyPrint, responseParameters, products);
	}

	@GET
	@Path("template")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveNewLoanProductDetails(@Context final UriInfo uriInfo) {
		
		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
		loanProduct = handleTemplate(loanProduct);
		
		return this.apiJsonSerializerService.serializeLoanProductDataToJson(prettyPrint, responseParameters, loanProduct);
	}

	@GET
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String retrieveLoanProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

		final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
		final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());
		final boolean template = ApiParameterHelper.template(uriInfo.getQueryParameters());

		LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);
		if (template) {
			loanProduct = handleTemplate(loanProduct);
		}
		
		return this.apiJsonSerializerService.serializeLoanProductDataToJson(prettyPrint, responseParameters, loanProduct);
	}
	
	@PUT
	@Path("{productId}")
	@Consumes({MediaType.APPLICATION_JSON})
	@Produces({MediaType.APPLICATION_JSON})
	public String updateLoanProduct(@PathParam("productId") final Long productId, final String jsonRequestBody) {
		
		final LoanProductCommand command = this.apiDataConversionService.convertJsonToLoanProductCommand(productId, jsonRequestBody);
		
		final EntityIdentifier identifier = this.loanProductWritePlatformService.updateLoanProduct(command);

		return apiJsonSerializerService.serializeEntityIdentifier(identifier);
	}
	
	private LoanProductData handleTemplate(final LoanProductData productData) {

		final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
        chargeOptions.removeAll(productData.charges());
        
        final List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        final List<EnumOptionData> interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        final List<EnumOptionData> interestCalculationPeriodTypeOptions = dropdownReadPlatformService.retrieveLoanInterestRateCalculatedInPeriodOptions();
        final List<EnumOptionData> loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
        final List<EnumOptionData> repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
        final List<EnumOptionData> interestRateFrequencyTypeOptions = dropdownReadPlatformService.retrieveInterestRateFrequencyTypeOptions();

        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService.retreiveTransactionProcessingStrategies();
        
        return new LoanProductData(productData, chargeOptions, currencyOptions, amortizationTypeOptions, interestTypeOptions, 
        		interestCalculationPeriodTypeOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions, interestRateFrequencyTypeOptions, fundOptions, transactionProcessingStrategyOptions);
	}
}