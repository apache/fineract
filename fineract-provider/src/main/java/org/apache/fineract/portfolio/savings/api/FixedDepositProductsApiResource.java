/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.portfolio.savings.api;

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

import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.common.AccountingEnumerations;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.interestratechart.data.InterestRateChartData;
import org.apache.fineract.portfolio.interestratechart.service.InterestRateChartReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.savings.DepositAccountType;
import org.apache.fineract.portfolio.savings.DepositsApiConstants;
import org.apache.fineract.portfolio.savings.SavingsCompoundingInterestPeriodType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationDaysInYearType;
import org.apache.fineract.portfolio.savings.SavingsInterestCalculationType;
import org.apache.fineract.portfolio.savings.SavingsPostingInterestPeriodType;
import org.apache.fineract.portfolio.savings.data.FixedDepositProductData;
import org.apache.fineract.portfolio.savings.service.DepositProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.DepositsDropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsDropdownReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsEnumerations;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;
import org.apache.fineract.portfolio.tax.service.TaxReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;

@Path("/fixeddepositproducts")
@Component
@Scope("singleton")
public class FixedDepositProductsApiResource {

    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<FixedDepositProductData> toApiJsonSerializer;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final InterestRateChartReadPlatformService chartReadPlatformService;
    private final InterestRateChartReadPlatformService interestRateChartReadPlatformService;
    private final DepositsDropdownReadPlatformService depositsDropdownReadPlatformService;
    private final DropdownReadPlatformService dropdownReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final TaxReadPlatformService taxReadPlatformService;

    @Autowired
    public FixedDepositProductsApiResource(final DepositProductReadPlatformService depositProductReadPlatformService,
            final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<FixedDepositProductData> toApiJsonSerializer,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final InterestRateChartReadPlatformService chartReadPlatformService,
            final InterestRateChartReadPlatformService interestRateChartReadPlatformService,
            final DepositsDropdownReadPlatformService depositsDropdownReadPlatformService,
            final DropdownReadPlatformService dropdownReadPlatformService,
            final PaymentTypeReadPlatformService paymentTypeReadPlatformService, final TaxReadPlatformService taxReadPlatformService) {
        this.depositProductReadPlatformService = depositProductReadPlatformService;
        this.savingsDropdownReadPlatformService = savingsDropdownReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.context = context;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.chartReadPlatformService = chartReadPlatformService;
        this.interestRateChartReadPlatformService = interestRateChartReadPlatformService;
        this.depositsDropdownReadPlatformService = depositsDropdownReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
        this.taxReadPlatformService = taxReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String create(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createFixedDepositProduct().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String update(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateFixedDepositProduct(productId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<FixedDepositProductData> products = (Collection) this.depositProductReadPlatformService
                .retrieveAll(DepositAccountType.FIXED_DEPOSIT);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, products, DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveOne(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);

        FixedDepositProductData fixedDepositProductData = (FixedDepositProductData) this.depositProductReadPlatformService.retrieveOne(
                DepositAccountType.FIXED_DEPOSIT, productId);

        final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);
        fixedDepositProductData = FixedDepositProductData.withCharges(fixedDepositProductData, charges);

        final Collection<InterestRateChartData> charts = this.chartReadPlatformService.retrieveAllWithSlabsWithTemplate(productId);
        fixedDepositProductData = FixedDepositProductData.withInterestChart(fixedDepositProductData, charts);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (fixedDepositProductData.hasAccountingEnabled()) {
            final Map<String, Object> accountingMappings = this.accountMappingReadPlatformService
                    .fetchAccountMappingDetailsForSavingsProduct(productId, fixedDepositProductData.accountingRuleTypeId());
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchFeeToIncomeAccountMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForSavingsProduct(productId);
            fixedDepositProductData = FixedDepositProductData.withAccountingDetails(fixedDepositProductData, accountingMappings,
                    paymentChannelToFundSourceMappings, feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            fixedDepositProductData = handleTemplateRelatedData(fixedDepositProductData);
        }

        return this.toApiJsonSerializer.serialize(settings, fixedDepositProductData,
                DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESOURCE_NAME);

        final FixedDepositProductData fixedDepositProduct = handleTemplateRelatedData(null);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, fixedDepositProduct,
                DepositsApiConstants.FIXED_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    private FixedDepositProductData handleTemplateRelatedData(final FixedDepositProductData savingsProduct) {

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

        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = this.savingsDropdownReadPlatformService
                .retrieveCompoundingInterestPeriodTypeOptions();

        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = this.savingsDropdownReadPlatformService
                .retrieveInterestPostingPeriodTypeOptions();

        final Collection<EnumOptionData> interestCalculationTypeOptions = this.savingsDropdownReadPlatformService
                .retrieveInterestCalculationTypeOptions();

        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = this.savingsDropdownReadPlatformService
                .retrieveInterestCalculationDaysInYearTypeOptions();

        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = this.savingsDropdownReadPlatformService
                .retrieveLockinPeriodFrequencyTypeOptions();

        final Collection<EnumOptionData> withdrawalFeeTypeOptions = this.savingsDropdownReadPlatformService
                .retrievewithdrawalFeeTypeOptions();

        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        final Collection<EnumOptionData> accountingRuleOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountingRuleTypeOptions();

        final Map<String, List<GLAccountData>> accountingMappingOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForSavingsProducts();

        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = this.depositsDropdownReadPlatformService
                .retrievePreClosurePenalInterestOnTypeOptions();

        final Collection<EnumOptionData> periodFrequencyTypeOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();

        // charges
        final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);
        chargeOptions = CollectionUtils.isEmpty(chargeOptions) ? null : chargeOptions;

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveSavingsApplicablePenalties();
        penaltyOptions = CollectionUtils.isEmpty(penaltyOptions) ? null : penaltyOptions;

        final Collection<TaxGroupData> taxGroupOptions = this.taxReadPlatformService.retrieveTaxGroupsForLookUp();

        // interest rate chart template
        final InterestRateChartData chartTemplate = this.interestRateChartReadPlatformService.template();

        FixedDepositProductData fixedDepositProductToReturn = null;
        if (savingsProduct != null) {
            fixedDepositProductToReturn = FixedDepositProductData.withTemplate(savingsProduct, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions, chartTemplate,
                    preClosurePenalInterestOnTypeOptions, periodFrequencyTypeOptions, taxGroupOptions);
        } else {
            fixedDepositProductToReturn = FixedDepositProductData.template(currency, interestCompoundingPeriodType,
                    interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, accountingRule, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions, chartTemplate,
                    preClosurePenalInterestOnTypeOptions, periodFrequencyTypeOptions, taxGroupOptions);
        }

        return fixedDepositProductToReturn;
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String delete(@PathParam("productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteFixedDepositProduct(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}