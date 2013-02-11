package org.mifosplatform.portfolio.savingsdepositproduct.api;

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
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResultBuilder;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccount.PortfolioApiDataConversionService;
import org.mifosplatform.portfolio.savingsaccount.PortfolioApiJsonSerializerService;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingsDepositEnumerations;
import org.mifosplatform.portfolio.savingsdepositproduct.command.DepositProductCommand;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductReadPlatformService;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductWritePlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositproducts")
@Component
@Scope("singleton")
public class DepositProductsApiResource {

    private final String entityType = "DEPOSITPRODUCT";

    private final PlatformSecurityContext context;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final DepositProductWritePlatformService depositProductWritePlatformService;
    private final PortfolioApiDataConversionService apiDataConversionService;
    private final PortfolioApiJsonSerializerService apiJsonSerializerService;
    private final DefaultToApiJsonSerializer<DepositProductData> toApiJsonSerializer;

    @Autowired
    public DepositProductsApiResource(final PlatformSecurityContext context,
            final DepositProductReadPlatformService depositProductReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService,
            final DepositProductWritePlatformService depositProductWritePlatformService,
            final PortfolioApiDataConversionService apiDataConversionService,
            final PortfolioApiJsonSerializerService apiJsonSerializerService,
            final DefaultToApiJsonSerializer<DepositProductData> toApiJsonSerializer) {
        this.context = context;
        this.depositProductReadPlatformService = depositProductReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.depositProductWritePlatformService = depositProductWritePlatformService;
        this.apiDataConversionService = apiDataConversionService;
        this.apiJsonSerializerService = apiJsonSerializerService;
        this.toApiJsonSerializer = toApiJsonSerializer;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDepositProduct(final String jsonRequestBody) {

        final DepositProductCommand command = this.apiDataConversionService.convertJsonToDepositProductCommand(null, jsonRequestBody);

        final CommandProcessingResult result = this.depositProductWritePlatformService.createDepositProduct(command);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateDepositProduct(@PathParam("productId") final Long productId, final String jsonRequestBody) {

        final DepositProductCommand command = this.apiDataConversionService.convertJsonToDepositProductCommand(productId, jsonRequestBody);
        final CommandProcessingResult result = this.depositProductWritePlatformService.updateDepositProduct(command);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDepositProduct(@PathParam("productId") final Long productId) {

        this.depositProductWritePlatformService.deleteDepositProduct(productId);

        final CommandProcessingResult result = new CommandProcessingResultBuilder().withEntityId(productId).build();

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositProducts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "externalId", "name", "description", "createdOn",
                "lastModifedOn", "currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths",
                "maturityDefaultInterestRate", "maturityMinInterestRate", "maturityMaxInterestRate", "interestCompoundedEvery",
                "interestCompoundedEveryPeriodType", "renewalAllowed", "preClosureAllowed", "preClosureInterestRate",
                "interestCompoundingAllowed", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "currency"));

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        Collection<DepositProductData> products = this.depositProductReadPlatformService.retrieveAllDepositProducts();
        return this.apiJsonSerializerService.serializeDepositProductDataToJson(prettyPrint, responseParameters, products);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("id", "externalId", "name", "description", "createdOn",
                "lastModifedOn", "currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths",
                "maturityDefaultInterestRate", "maturityMinInterestRate", "maturityMaxInterestRate", "interestCompoundedEvery",
                "interestCompoundedEveryPeriodType", "renewalAllowed", "preClosureAllowed", "preClosureInterestRate",
                "interestCompoundingAllowed", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "currency"));

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
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewDepositProductDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Set<String> typicalResponseParameters = new HashSet<String>(Arrays.asList("currencyOptions",
                "interestCompoundedEveryPeriodTypeOptions", "id", "externalId", "name", "description", "createdOn", "lastModifedOn",
                "currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths", "maturityDefaultInterestRate",
                "maturityMinInterestRate", "maturityMaxInterestRate", "interestCompoundedEvery", "interestCompoundedEveryPeriodType",
                "renewalAllowed", "preClosureAllowed", "preClosureInterestRate", "isLockinPeriodAllowed", "lockinPeriod",
                "lockinPeriodType", "currency"));

        Set<String> responseParameters = ApiParameterHelper.extractFieldsForResponseIfProvided(uriInfo.getQueryParameters());
        if (responseParameters.isEmpty()) {
            responseParameters.addAll(typicalResponseParameters);
        }
        boolean prettyPrint = ApiParameterHelper.prettyPrint(uriInfo.getQueryParameters());

        DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveNewDepositProductDetails();

        depositProduct = handleTemplateRelatedData(responseParameters, depositProduct);

        return this.apiJsonSerializerService.serializeDepositProductDataToJson(prettyPrint, responseParameters, depositProduct);
    }

    private DepositProductData handleTemplateRelatedData(final Set<String> responseParameters, final DepositProductData productData) {

        responseParameters.addAll(Arrays.asList("currencyOptions", "interestCompoundedEveryPeriodTypeOptions"));
        Collection<CurrencyData> allowedCurrencies = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        EnumOptionData monthly = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType.MONTHS);
        List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions = Arrays.asList(monthly);

        return new DepositProductData(productData, allowedCurrencies, interestCompoundedEveryPeriodTypeOptions);
    }
}