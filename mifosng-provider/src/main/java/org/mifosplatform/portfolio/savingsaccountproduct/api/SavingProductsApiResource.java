/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savingsaccountproduct.api;

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

import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiRequestParameterHelper;
import org.mifosplatform.infrastructure.core.data.CommandProcessingResult;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.mifosplatform.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.mifosplatform.infrastructure.security.service.PlatformSecurityContext;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.organisation.monetary.service.CurrencyReadPlatformService;
import org.mifosplatform.portfolio.savingsaccountproduct.data.SavingProductData;
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

@Path("/savingproducts")
@Component
@Scope("singleton")
public class SavingProductsApiResource {

    private final Set<String> SAVINGS_PRODUCT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("currencyOptions", "id", "createdOn",
            "lastModifedOn", "locale", "name", "description", "currencyCode", "digitsAfterDecimal", "interstRate", "minInterestRate",
            "maxInterestRate", "savingsDepositAmount", "savingProductType", "tenureType", "tenure", "frequency", "interestType",
            "interestCalculationMethod", "minimumBalanceForWithdrawal", "isPartialDepositAllowed", "isLockinPeriodAllowed", "lockinPeriod",
            "lockinPeriodType", "currencyOptions", "savingsProductTypeOptions", "tenureTypeOptions", "depositEvery",
            "savingFrequencyOptions", "savingsInterestTypeOptions", "lockinPeriodTypeOptions", "interestCalculationOptions"));

    private final SavingProductReadPlatformService savingProductReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final String entityType = "SAVINGSPRODUCT";
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingProductData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public SavingProductsApiResource(final SavingProductReadPlatformService savingProductReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingProductData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.savingProductReadPlatformService = savingProductReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createSavingProduct(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateSavingProduct(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllSavingProducts(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        Collection<SavingProductData> products = this.savingProductReadPlatformService.retrieveAllSavingProducts();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        return this.toApiJsonSerializer.serialize(settings, products, SAVINGS_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveSavingProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        SavingProductData savingProduct = this.savingProductReadPlatformService.retrieveSavingProduct(productId);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (settings.isTemplate()) {
            savingProduct = handleTemplateRelatedData(savingProduct);
        }

        return this.toApiJsonSerializer.serialize(settings, savingProduct, SAVINGS_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewSavingProductDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        SavingProductData savingProduct = this.savingProductReadPlatformService.retrieveNewSavingProductDetails();

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        savingProduct = handleTemplateRelatedData(savingProduct);

        return this.toApiJsonSerializer.serialize(settings, savingProduct, SAVINGS_PRODUCT_DATA_PARAMETERS);
    }

    private SavingProductData handleTemplateRelatedData(final SavingProductData savingProduct) {

        Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        EnumOptionData reccuring = SavingProductEnumerations.savingProductType(SavingProductType.RECURRING);
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

        return new SavingProductData(savingProduct, currencyOptions, savingsProductTypeOptions, tenureTypeOptions, savingFrequencyOptions,
                savingsInterestTypeOptions, lockinPeriodTypeOptions, interestCalculationOptions);
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteProduct(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingProduct(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}