/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.api;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

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

import org.mifosplatform.accounting.common.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
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
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.charge.service.ChargeReadPlatformService;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.mifosplatform.portfolio.savings.SavingsApiConstants;
import org.mifosplatform.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.mifosplatform.portfolio.savings.SavingsInterestCalculationType;
import org.mifosplatform.portfolio.savings.SavingsPostingInterestPeriodType;
import org.mifosplatform.portfolio.savings.data.SavingsProductData;
import org.mifosplatform.portfolio.savings.service.SavingsDropdownReadPlatformService;
import org.mifosplatform.portfolio.savings.service.SavingsEnumerations;
import org.mifosplatform.portfolio.savings.service.SavingsProductReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/savingsproducts")
@Component
@Scope("singleton")
public class SavingsProductsApiResource {

    private final SavingsProductReadPlatformService savingProductReadPlatformService;
    private final SavingsDropdownReadPlatformService dropdownReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<SavingsProductData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;

    @Autowired
    public SavingsProductsApiResource(final SavingsProductReadPlatformService savingProductReadPlatformService,
            final SavingsDropdownReadPlatformService dropdownReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<SavingsProductData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.savingProductReadPlatformService = savingProductReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createSavingProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateSavingProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        final Collection<SavingsProductData> products = this.savingProductReadPlatformService.retrieveAll();

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, products, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        SavingsProductData savingProductData = this.savingProductReadPlatformService.retrieveOne(productId);

        final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);

        savingProductData = SavingsProductData.withCharges(savingProductData, charges);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (savingProductData.hasAccountingEnabled()) {
            final Map<String, Object> accountingMappings = this.accountMappingReadPlatformService
                    .fetchAccountMappingDetailsForSavingsProduct(productId, savingProductData.accountingRuleTypeId());
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchFeeToIncomeAccountMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForSavingsProduct(productId);
            savingProductData = SavingsProductData.withAccountingDetails(savingProductData, accountingMappings,
                    paymentChannelToFundSourceMappings, feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            savingProductData = handleTemplateRelatedData(savingProductData);
        }

        return this.toApiJsonSerializer
                .serialize(settings, savingProductData, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(SavingsApiConstants.SAVINGS_PRODUCT_RESOURCE_NAME);

        final SavingsProductData savingProduct = handleTemplateRelatedData(null);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, savingProduct, SavingsApiConstants.SAVINGS_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    private SavingsProductData handleTemplateRelatedData(final SavingsProductData savingsProduct) {

        final EnumOptionData interestCompoundingPeriodType = SavingsEnumerations
                .compoundingInterestPeriodType(SavingsCompoundingInterestPeriodType.DAILY);

        final EnumOptionData interestPostingPeriodType = SavingsEnumerations
                .interestPostingPeriodType(SavingsPostingInterestPeriodType.MONTHLY);

        final EnumOptionData interestCalculationType = SavingsEnumerations
                .interestCalculationType(SavingsInterestCalculationType.DAILY_BALANCE);

        final EnumOptionData interestCalculationDaysInYearType = SavingsEnumerations
                .interestCalculationDaysInYearType(SavingsInterestCalculationDaysInYearType.DAYS_365);

        final EnumOptionData accountingRule = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);

        CurrencyData currency = CurrencyData.blank();
        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        if (currencyOptions.size() == 1) {
            currency = new ArrayList<>(currencyOptions).get(0);
        }

        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveCompoundingInterestPeriodTypeOptions();

        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestPostingPeriodTypeOptions();

        final Collection<EnumOptionData> interestCalculationTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestCalculationTypeOptions();

        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestCalculationDaysInYearTypeOptions();

        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.dropdownReadPlatformService
                .retrieveLockinPeriodFrequencyTypeOptions();

        final Collection<EnumOptionData> withdrawalFeeTypeOptions = this.dropdownReadPlatformService.retrievewithdrawalFeeTypeOptions();

        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        final Collection<EnumOptionData> accountingRuleOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountingRuleTypeOptions();
        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForSavingsProducts();

        // charges
        final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);
        chargeOptions = CollectionUtils.isEmpty(chargeOptions) ? null : chargeOptions;

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveSavingsApplicablePenalties();
        penaltyOptions = CollectionUtils.isEmpty(penaltyOptions) ? null : penaltyOptions;

        SavingsProductData savingsProductToReturn = null;
        if (savingsProduct != null) {
            savingsProductToReturn = SavingsProductData.withTemplate(savingsProduct, currencyOptions, interestCompoundingPeriodTypeOptions,
                    interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                    lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                    accountingMappingOptions, chargeOptions, penaltyOptions);
        } else {
            savingsProductToReturn = SavingsProductData.template(currency, interestCompoundingPeriodType, interestPostingPeriodType,
                    interestCalculationType, interestCalculationDaysInYearType, accountingRule, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions);
        }

        return savingsProductToReturn;
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteSavingProduct(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}