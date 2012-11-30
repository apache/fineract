package org.mifosplatform.portfolio.loanproduct.api;

import java.util.Arrays;
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

import org.mifosng.platform.api.data.EntityIdentifier;
import org.mifosng.platform.api.data.EnumOptionData;
import org.mifosng.platform.api.infrastructure.PortfolioApiDataConversionService;
import org.mifosng.platform.api.infrastructure.PortfolioApiJsonSerializerService;
import org.mifosng.platform.api.infrastructure.PortfolioCommandSerializerService;
import org.mifosng.platform.infrastructure.api.ApiParameterHelper;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.configuration.data.CurrencyData;
import org.mifosplatform.infrastructure.configuration.service.CurrencyReadPlatformService;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.command.LoanProductCommand;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
public class LoanProductsApiResource {

    private final String resourceNameForPermissions = "LOANPRODUCT";

    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioCommandSerializerService commandSerializerService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;

    @Autowired
    public LoanProductsApiResource(final PlatformSecurityContext context, final LoanProductReadPlatformService readPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final CurrencyReadPlatformService currencyReadPlatformService,
            final FundReadPlatformService fundReadPlatformService, final LoanDropdownReadPlatformService dropdownReadPlatformService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioCommandSerializerService commandSerializerService,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService) {
        this.context = context;
        this.loanProductReadPlatformService = readPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.apiDataConversionService = apiDataConversionService;
        this.commandSerializerService = commandSerializerService;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createLoanProduct(final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                "CREATE_LOANPRODUCT");
        context.authenticatedUser().validateHasPermissionTo("CREATE_LOANPRODUCT", allowedPermissions);

        final LoanProductCommand command = this.apiDataConversionService.convertApiRequestJsonToLoanProductCommand(null,
                apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("CREATE", "loanproducts", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        final Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

        return this.apiJsonSerializerService.serializeLoanProductDataToJson(prettyPrint, responseParameters, products);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewLoanProductDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

        final Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        final boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
        loanProduct = handleTemplate(loanProduct);

        return this.apiJsonSerializerService.serializeLoanProductDataToJson(prettyPrint, responseParameters, loanProduct);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(resourceNameForPermissions);

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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanProduct(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final List<String> allowedPermissions = Arrays.asList("ALL_FUNCTIONS", "ORGANISATION_ADMINISTRATION_SUPER_USER",
                "UPDATE_LOANPRODUCT");
        context.authenticatedUser().validateHasPermissionTo("UPDATE_LOANPRODUCT", allowedPermissions);

        final LoanProductCommand command = this.apiDataConversionService.convertApiRequestJsonToLoanProductCommand(productId,
                apiRequestBodyAsJson);
        final String commandSerializedAsJson = this.commandSerializerService.serializeCommandToJson(command);

        final EntityIdentifier result = this.commandsSourceWritePlatformService.logCommandSource("UPDATE", "loanproducts", null,
                commandSerializedAsJson);

        return this.apiJsonSerializerService.serializeEntityIdentifier(result);
    }

    private LoanProductData handleTemplate(final LoanProductData productData) {

        final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableCharges(feeChargesOnly);
        chargeOptions.removeAll(productData.charges());

        final List<CurrencyData> currencyOptions = currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> amortizationTypeOptions = dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        final List<EnumOptionData> interestTypeOptions = dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        final List<EnumOptionData> interestCalculationPeriodTypeOptions = dropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final List<EnumOptionData> loanTermFrequencyTypeOptions = dropdownReadPlatformService.retrieveLoanTermFrequencyTypeOptions();
        final List<EnumOptionData> repaymentFrequencyTypeOptions = dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
        final List<EnumOptionData> interestRateFrequencyTypeOptions = dropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();

        final Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();

        return new LoanProductData(productData, chargeOptions, currencyOptions, amortizationTypeOptions, interestTypeOptions,
                interestCalculationPeriodTypeOptions, loanTermFrequencyTypeOptions, repaymentFrequencyTypeOptions,
                interestRateFrequencyTypeOptions, fundOptions, transactionProcessingStrategyOptions);
    }
}