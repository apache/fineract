/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
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
import org.mifosplatform.portfolio.loanproduct.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.savingsaccountproduct.service.SavingsDepositEnumerations;
import org.mifosplatform.portfolio.savingsdepositproduct.data.DepositProductData;
import org.mifosplatform.portfolio.savingsdepositproduct.service.DepositProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/depositproducts")
@Component
@Scope("singleton")
public class DepositProductsApiResource {
	
	private final Set<String> DEPOSIT_PRODUCT_DATA_PARAMETERS = new HashSet<String>(Arrays.asList("id", "externalId", "name", "description", "createdOn", "lastModifedOn",
			"currencyCode", "digitsAfterDecimal", "minimumBalance", "maximumBalance", "tenureInMonths", "maturityDefaultInterestRate", "maturityMinInterestRate",
			"maturityMaxInterestRate", "interestCompoundedEvery", "interestCompoundedEveryPeriodType", "renewalAllowed", "preClosureAllowed", "preClosureInterestRate",
            "interestCompoundingAllowed", "isLockinPeriodAllowed", "lockinPeriod", "lockinPeriodType", "currency","currencyOptions", "interestCompoundedEveryPeriodTypeOptions"));

    private final String entityType = "DEPOSITPRODUCT";

    private final PlatformSecurityContext context;
    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final DefaultToApiJsonSerializer<DepositProductData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;

    @Autowired
    public DepositProductsApiResource(final PlatformSecurityContext context,
            final DepositProductReadPlatformService depositProductReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService,
            final DefaultToApiJsonSerializer<DepositProductData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper) {
        this.context = context;
        this.depositProductReadPlatformService = depositProductReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createDepositProduct(final String apiRequestBodyAsJson) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().createDepositProduct().withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateDepositProduct(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().updateDepositProduct(productId).withJson(apiRequestBodyAsJson).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String deleteDepositProduct(@PathParam("productId") final Long productId) {
    	
    	final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteDepositProduct(productId).build();
    	
    	final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllDepositProducts(@Context final UriInfo uriInfo) {
        context.authenticatedUser().validateHasReadPermission(entityType);
        Collection<DepositProductData> products = this.depositProductReadPlatformService.retrieveAllDepositProducts();
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, products, DEPOSIT_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveDepositProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);
        DepositProductData productData = this.depositProductReadPlatformService.retrieveDepositProductData(productId);
        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        
        if (settings.isTemplate()) {
            productData = handleTemplateRelatedData(productData);
        }
        
        return this.toApiJsonSerializer.serialize(settings, productData, DEPOSIT_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveNewDepositProductDetails(@Context final UriInfo uriInfo) {

        context.authenticatedUser().validateHasReadPermission(entityType);

        DepositProductData depositProduct = this.depositProductReadPlatformService.retrieveNewDepositProductDetails();

        depositProduct = handleTemplateRelatedData(depositProduct);

        final ApiRequestJsonSerializationSettings settings = apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, depositProduct, DEPOSIT_PRODUCT_DATA_PARAMETERS);
    }

    private DepositProductData handleTemplateRelatedData(final DepositProductData productData) {

        Collection<CurrencyData> allowedCurrencies = this.currencyReadPlatformService.retrieveAllowedCurrencies();

        EnumOptionData monthly = SavingsDepositEnumerations.interestCompoundingPeriodType(PeriodFrequencyType.MONTHS);
        List<EnumOptionData> interestCompoundedEveryPeriodTypeOptions = Arrays.asList(monthly);

        return new DepositProductData(productData, allowedCurrencies, interestCompoundedEveryPeriodTypeOptions);
    }
}