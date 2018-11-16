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

import io.swagger.annotations.*;
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
import org.apache.fineract.portfolio.savings.*;
import org.apache.fineract.portfolio.savings.data.RecurringDepositProductData;
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

import javax.ws.rs.*;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

@Path("/recurringdepositproducts")
@Component
@Scope("singleton")
@Api(value = "Recurring Deposit Product", description = "Recurring Deposits are a special kind of Term Deposits offered by MFI's. The Recurring Deposit Products (aka RD) product offerings are modeled using this API.\n" + "\n" + "Recurring Deposits help people with regular incomes to deposit a fixed amount every month (specified recurring frequency) into their Recurring Deposit account.\n" + "\n" + "When creating recurring deposit accounts, the details from the recurring deposit product are used to auto fill details of the recurring deposit account application process.")
public class RecurringDepositProductsApiResource {

    private final DepositProductReadPlatformService depositProductReadPlatformService;
    private final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final PlatformSecurityContext context;
    private final DefaultToApiJsonSerializer<RecurringDepositProductData> toApiJsonSerializer;
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
    public RecurringDepositProductsApiResource(final DepositProductReadPlatformService depositProductReadPlatformService,
            final SavingsDropdownReadPlatformService savingsDropdownReadPlatformService,
            final CurrencyReadPlatformService currencyReadPlatformService, final PlatformSecurityContext context,
            final DefaultToApiJsonSerializer<RecurringDepositProductData> toApiJsonSerializer,
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
    @ApiOperation(value = "Create a Recurring Deposit Product", httpMethod = "POST", notes = "Creates a Recurring Deposit Product\n\n" + "Mandatory Fields: name, shortName, description, currencyCode, digitsAfterDecimal,inMultiplesOf, interestCompoundingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minDepositTerm, minDepositTermTypeId, recurringDepositFrequency, recurringDepositFrequencyTypeId, accountingRule, depositAmount\n\n" + "Mandatory Fields for Cash based accounting (accountingRule = 2): savingsReferenceAccountId, savingsControlAccountId, interestOnSavingsAccountId, incomeFromFeeAccountId, transfersInSuspenseAccountId, incomeFromPenaltyAccountId\n\n" + "Optional Fields: lockinPeriodFrequency, lockinPeriodFrequencyType, maxDepositTerm, maxDepositTermTypeId, inMultiplesOfDepositTerm, inMultiplesOfDepositTermTypeId, preClosurePenalApplicable, preClosurePenalInterest, preClosurePenalInterestOnTypeId, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, charges, charts, minDepositAmount, maxDepositAmount, withHoldTax, taxGroupId")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = RecurringDepositProductsApiResourceSwagger.PostRecurringDepositProductsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = RecurringDepositProductsApiResourceSwagger.PostRecurringDepositProductsResponse.class)})
    public String create(@ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createRecurringDepositProduct().withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Update a Recurring Deposit Product", httpMethod = "PUT", notes = "Updates a Recurring Deposit Product")
    @ApiImplicitParams({@ApiImplicitParam(value = "body", required = true, paramType = "body", dataType = "body", format = "body", dataTypeClass = RecurringDepositProductsApiResourceSwagger.PutRecurringDepositProductsRequest.class)})
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = RecurringDepositProductsApiResourceSwagger.PutRecurringDepositProductsResponse.class)})
    public String update(@PathParam("productId") @ApiParam(value = "productId") final Long productId, @ApiParam(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateRecurringDepositProduct(productId)
                .withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "List Recuring Deposit Products", httpMethod = "GET", notes = "Lists Recuring Deposit Products\n\n" + "Example Requests:\n" + "\n" + "recurringdepositproducts\n" + "\n" + "\n" + "recurringdepositproducts?fields=name")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", responseContainer = "List", response = RecurringDepositProductsApiResourceSwagger.GetRecurringDepositProductsResponse.class)})
    public String retrieveAll(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        @SuppressWarnings({ "unchecked", "rawtypes" })
        final Collection<RecurringDepositProductData> products = (Collection) this.depositProductReadPlatformService
                .retrieveAll(DepositAccountType.RECURRING_DEPOSIT);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, products,
                DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Retrieve a Recurring Deposit Product", httpMethod = "GET", notes = "Retrieves a Recurring Deposit Product\n\n" + "Example Requests:\n" + "\n" + "recurringdepositproducts/1\n" + "\n" + "\n" + "recurringdepositproducts/1?template=true\n" + "\n" + "\n" + "recurringdepositproducts/1?fields=name,description")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = RecurringDepositProductsApiResourceSwagger.GetRecurringDepositProductsProductIdResponse.class)})
    public String retrieveOne(@PathParam("productId") @ApiParam(value = "productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        RecurringDepositProductData recurringDepositProductData = (RecurringDepositProductData) this.depositProductReadPlatformService
                .retrieveOne(DepositAccountType.RECURRING_DEPOSIT, productId);

        final Collection<ChargeData> charges = this.chargeReadPlatformService.retrieveSavingsProductCharges(productId);
        recurringDepositProductData = RecurringDepositProductData.withCharges(recurringDepositProductData, charges);

        final Collection<InterestRateChartData> charts = this.chartReadPlatformService.retrieveAllWithSlabsWithTemplate(productId);
        recurringDepositProductData = RecurringDepositProductData.withInterestChart(recurringDepositProductData, charts);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (recurringDepositProductData.hasAccountingEnabled()) {
            final Map<String, Object> accountingMappings = this.accountMappingReadPlatformService
                    .fetchAccountMappingDetailsForSavingsProduct(productId, recurringDepositProductData.accountingRuleTypeId());
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchFeeToIncomeAccountMappingsForSavingsProduct(productId);
            Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForSavingsProduct(productId);
            recurringDepositProductData = RecurringDepositProductData.withAccountingDetails(recurringDepositProductData,
                    accountingMappings, paymentChannelToFundSourceMappings, feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            recurringDepositProductData = handleTemplateRelatedData(recurringDepositProductData);
        }

        return this.toApiJsonSerializer.serialize(settings, recurringDepositProductData,
                DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESOURCE_NAME);

        final RecurringDepositProductData recurringDepositProduct = handleTemplateRelatedData(null);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());
        return this.toApiJsonSerializer.serialize(settings, recurringDepositProduct,
                DepositsApiConstants.RECURRING_DEPOSIT_PRODUCT_RESPONSE_DATA_PARAMETERS);
    }

    private RecurringDepositProductData handleTemplateRelatedData(final RecurringDepositProductData savingsProduct) {

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

        // charges
        final boolean feeChargesOnly = true;
        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveSavingsProductApplicableCharges(feeChargesOnly);
        chargeOptions = CollectionUtils.isEmpty(chargeOptions) ? null : chargeOptions;

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveSavingsApplicablePenalties();
        penaltyOptions = CollectionUtils.isEmpty(penaltyOptions) ? null : penaltyOptions;

        final Collection<EnumOptionData> periodFrequencyTypeOptions = this.dropdownReadPlatformService.retrievePeriodFrequencyTypeOptions();

        // interest rate chart template
        final InterestRateChartData chartTemplate = this.interestRateChartReadPlatformService.template();
        final Collection<TaxGroupData> taxGroupOptions = this.taxReadPlatformService.retrieveTaxGroupsForLookUp();

        RecurringDepositProductData recurringDepositProductToReturn = null;
        if (savingsProduct != null) {
            recurringDepositProductToReturn = RecurringDepositProductData.withTemplate(savingsProduct, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions, chartTemplate,
                    preClosurePenalInterestOnTypeOptions, periodFrequencyTypeOptions, taxGroupOptions);
        } else {
            recurringDepositProductToReturn = RecurringDepositProductData.template(currency, interestCompoundingPeriodType,
                    interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, accountingRule, currencyOptions,
                    interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                    interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions,
                    paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, chargeOptions, penaltyOptions, chartTemplate,
                    preClosurePenalInterestOnTypeOptions, periodFrequencyTypeOptions, taxGroupOptions);
        }

        return recurringDepositProductToReturn;
    }

    @DELETE
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @ApiOperation(value = "Delete a Recurring Deposit Product", httpMethod = "DELETE", notes = "Deletes a Recurring Deposit Product")
    @ApiResponses({@ApiResponse(code = 200, message = "OK", response = RecurringDepositProductsApiResourceSwagger.DeleteRecurringDepositProductsProductIdResponse.class)})
    public String delete(@PathParam("productId") @ApiParam(value = "productId") final Long productId) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().deleteRecurringDepositProduct(productId).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);

    }
}