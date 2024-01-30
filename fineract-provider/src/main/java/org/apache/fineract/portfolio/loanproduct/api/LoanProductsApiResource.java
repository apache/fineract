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
package org.apache.fineract.portfolio.loanproduct.api;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.media.ArraySchema;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.parameters.RequestBody;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.ws.rs.Consumes;
import jakarta.ws.rs.GET;
import jakarta.ws.rs.POST;
import jakarta.ws.rs.PUT;
import jakarta.ws.rs.Path;
import jakarta.ws.rs.PathParam;
import jakarta.ws.rs.Produces;
import jakarta.ws.rs.QueryParam;
import jakarta.ws.rs.core.Context;
import jakarta.ws.rs.core.MediaType;
import jakarta.ws.rs.core.UriInfo;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.accounting.common.AccountingDropdownReadPlatformService;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.apache.fineract.commands.domain.CommandWrapper;
import org.apache.fineract.commands.service.CommandWrapperBuilder;
import org.apache.fineract.commands.service.PortfolioCommandSourceWritePlatformService;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.ApiParameterHelper;
import org.apache.fineract.infrastructure.core.api.ApiRequestParameterHelper;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.infrastructure.core.domain.ExternalId;
import org.apache.fineract.infrastructure.core.serialization.ApiRequestJsonSerializationSettings;
import org.apache.fineract.infrastructure.core.serialization.DefaultToApiJsonSerializer;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.organisation.monetary.service.CurrencyReadPlatformService;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.common.service.DropdownReadPlatformService;
import org.apache.fineract.portfolio.delinquency.data.DelinquencyBucketData;
import org.apache.fineract.portfolio.delinquency.service.DelinquencyReadPlatformService;
import org.apache.fineract.portfolio.floatingrates.data.FloatingRateData;
import org.apache.fineract.portfolio.floatingrates.service.FloatingRatesReadPlatformService;
import org.apache.fineract.portfolio.fund.data.FundData;
import org.apache.fineract.portfolio.fund.service.FundReadPlatformService;
import org.apache.fineract.portfolio.loanaccount.api.LoanApiConstants;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.LoanProductConstants;
import org.apache.fineract.portfolio.loanproduct.data.LoanProductData;
import org.apache.fineract.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.apache.fineract.portfolio.loanproduct.domain.AllocationType;
import org.apache.fineract.portfolio.loanproduct.domain.CreditAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.FutureInstallmentAllocationRule;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationTransactionType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.apache.fineract.portfolio.loanproduct.exception.LoanProductNotFoundException;
import org.apache.fineract.portfolio.loanproduct.productmix.data.ProductMixData;
import org.apache.fineract.portfolio.loanproduct.productmix.service.ProductMixReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.apache.fineract.portfolio.rate.data.RateData;
import org.apache.fineract.portfolio.rate.service.RateReadService;
import org.springframework.stereotype.Component;

@Path("/v1/loanproducts")
@Component
@Tag(name = "Loan Products", description = "A Loan product is a template that is used when creating a loan. Much of the template definition can be overridden during loan creation.")
@RequiredArgsConstructor
public class LoanProductsApiResource {

    private static final Set<String> LOAN_PRODUCT_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "shortName", "description",
            "fundId", "fundName", "includeInBorrowerCycle", "currency", "principal", "minPrincipal", "maxPrincipal", "numberOfRepayments",
            "minNumberOfRepayments", "maxNumberOfRepayments", "repaymentEvery", "repaymentFrequencyType", "graceOnPrincipalPayment",
            "recurringMoratoriumOnPrincipalPeriods", "graceOnInterestPayment", "graceOnInterestCharged", "interestRatePerPeriod",
            "minInterestRatePerPeriod", "maxInterestRatePerPeriod", "interestRateFrequencyType", "annualInterestRate", "amortizationType",
            "interestType", "interestCalculationPeriodType", LoanProductConstants.ALLOW_PARTIAL_PERIOD_INTEREST_CALCUALTION_PARAM_NAME,
            "inArrearsTolerance", "transactionProcessingStrategyCode", "transactionProcessingStrategyName", "charges", "accountingRule",
            "externalId", "accountingMappings", "paymentChannelToFundSourceMappings", "fundOptions", "paymentTypeOptions",
            "currencyOptions", "repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions", "amortizationTypeOptions",
            "interestTypeOptions", "interestCalculationPeriodTypeOptions", "transactionProcessingStrategyOptions", "chargeOptions",
            "accountingOptions", "accountingRuleOptions", "accountingMappingOptions", "floatingRateOptions",
            "isLinkedToFloatingInterestRates", "floatingRatesId", "interestRateDifferential", "minDifferentialLendingRate",
            "defaultDifferentialLendingRate", "maxDifferentialLendingRate", "isFloatingInterestRateCalculationAllowed",
            LoanProductConstants.CAN_USE_FOR_TOPUP, LoanProductConstants.IS_EQUAL_AMORTIZATION_PARAM, LoanProductConstants.RATES_PARAM_NAME,
            LoanApiConstants.fixedPrincipalPercentagePerInstallmentParamName, LoanProductConstants.DUE_DAYS_FOR_REPAYMENT_EVENT,
            LoanProductConstants.OVER_DUE_DAYS_FOR_REPAYMENT_EVENT, LoanProductConstants.ENABLE_DOWN_PAYMENT,
            LoanProductConstants.DISBURSED_AMOUNT_PERCENTAGE_DOWN_PAYMENT, LoanProductConstants.ENABLE_AUTO_REPAYMENT_DOWN_PAYMENT,
            LoanProductConstants.REPAYMENT_START_DATE_TYPE));

    private static final Set<String> PRODUCT_MIX_DATA_PARAMETERS = new HashSet<>(
            Arrays.asList("productId", "productName", "restrictedProducts", "allowedProducts", "productOptions"));

    private static final String RESOURCE_NAME_FOR_PERMISSIONS = "LOANPRODUCT";
    public static final String PRODUCTMIX = "PRODUCTMIX";
    public static final String PRODUCT_MIXES = "productMixes";

    private final PlatformSecurityContext context;
    private final LoanProductReadPlatformService loanProductReadPlatformService;
    private final ChargeReadPlatformService chargeReadPlatformService;
    private final CurrencyReadPlatformService currencyReadPlatformService;
    private final FundReadPlatformService fundReadPlatformService;
    private final DefaultToApiJsonSerializer<LoanProductData> toApiJsonSerializer;
    private final ApiRequestParameterHelper apiRequestParameterHelper;
    private final LoanDropdownReadPlatformService dropdownReadPlatformService;
    private final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService;
    private final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService;
    private final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService;
    private final DefaultToApiJsonSerializer<ProductMixData> productMixDataApiJsonSerializer;
    private final ProductMixReadPlatformService productMixReadPlatformService;
    private final DropdownReadPlatformService commonDropdownReadPlatformService;
    private final PaymentTypeReadPlatformService paymentTypeReadPlatformService;
    private final FloatingRatesReadPlatformService floatingRateReadPlatformService;
    private final RateReadService rateReadService;
    private final ConfigurationDomainService configurationDomainService;
    private final DelinquencyReadPlatformService delinquencyReadPlatformService;

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Create a Loan Product", description = "Depending of the Accounting Rule (accountingRule) selected, additional fields with details of the appropriate Ledger Account identifiers would need to be passed in.\n"
            + "\n" + "Refer MifosX Accounting Specs Draft for more details regarding the significance of the selected accounting rule\n\n"
            + "Mandatory Fields: name, shortName, currencyCode, digitsAfterDecimal, inMultiplesOf, principal, numberOfRepayments, repaymentEvery, repaymentFrequencyType, interestRatePerPeriod, interestRateFrequencyType, amortizationType, interestType, interestCalculationPeriodType, transactionProcessingStrategyCode, accountingRule, isInterestRecalculationEnabled, daysInYearType, daysInMonthType\n\n"
            + "Optional Fields: inArrearsTolerance, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, graceOnArrearsAgeing, charges, paymentChannelToFundSourceMappings, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, includeInBorrowerCycle, useBorrowerCycle,principalVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle, multiDisburseLoan,maxTrancheCount, outstandingLoanBalance,overdueDaysForNPA,holdGuaranteeFunds, principalThresholdForLastInstalment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount, installmentAmountInMultiplesOf, allowAttributeOverrides, allowPartialPeriodInterestCalcualtion,dueDaysForRepaymentEvent,overDueDaysForRepaymentEvent,enableDownPayment,disbursedAmountPercentageDownPayment,enableAutoRepaymentForDownPayment,repaymentStartDateType\n\n"
            + "Additional Mandatory Fields for Cash(2) based accounting: fundSourceAccountId, loanPortfolioAccountId, interestOnLoanAccountId, incomeFromFeeAccountId, incomeFromPenaltyAccountId, writeOffAccountId, transfersInSuspenseAccountId, overpaymentLiabilityAccountId\n\n"
            + "Additional Mandatory Fields for periodic (3) and upfront (4)accrual accounting: fundSourceAccountId, loanPortfolioAccountId, interestOnLoanAccountId, incomeFromFeeAccountId, incomeFromPenaltyAccountId, writeOffAccountId, receivableInterestAccountId, receivableFeeAccountId, receivablePenaltyAccountId, transfersInSuspenseAccountId, overpaymentLiabilityAccountId\n\n"
            + "Additional Mandatory Fields if interest recalculation is enabled(true): interestRecalculationCompoundingMethod, rescheduleStrategyMethod, recalculationRestFrequencyType\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true): isArrearsBasedOnOriginalSchedule, preClosureInterestCalculationStrategy\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and recalculationRestFrequencyType is not same as repayment period: recalculationRestFrequencyInterval, recalculationRestFrequencyDate\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and interestRecalculationCompoundingMethod is enabled: recalculationCompoundingFrequencyType\n\n"
            + "Additional Optional Fields if interest recalculation is enabled(true) and interestRecalculationCompoundingMethod is enabled and recalculationCompoundingFrequencyType is not same as repayment period: recalculationCompoundingFrequencyInterval, recalculationCompoundingFrequencyDate\n\n"
            + "Additional Mandatory Fields if Hold Guarantee funds is enabled(true): mandatoryGuarantee\n\n"
            + "Additional Optional Fields if Hold Guarantee funds is enabled(true): minimumGuaranteeFromOwnFunds,minimumGuaranteeFromGuarantor")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PostLoanProductsRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PostLoanProductsResponse.class))) })
    public String createLoanProduct(@Parameter(hidden = true) final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "List Loan Products", description = "Lists Loan Products\n\n" + "Example Requests:\n" + "\n" + "loanproducts\n"
            + "\n" + "\n" + "loanproducts?fields=name,description,interestRateFrequencyType,amortizationType")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(array = @ArraySchema(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsResponse.class)))) })
    public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (!associationParameters.isEmpty() && associationParameters.contains(PRODUCT_MIXES)) {
            this.context.authenticatedUser().validateHasReadPermission(PRODUCTMIX);
            final Collection<ProductMixData> productMixes = this.productMixReadPlatformService.retrieveAllProductMixes();
            return this.productMixDataApiJsonSerializer.serialize(settings, productMixes, PRODUCT_MIX_DATA_PARAMETERS);
        }

        final Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

        return this.toApiJsonSerializer.serialize(settings, products, LOAN_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve Loan Product Details Template", description = "This is a convenience resource. It can be useful when building maintenance user interface screens for client applications. The template data returned consists of any or all of:\n"
            + "\n" + "Field Defaults\n" + "Allowed description Lists\n" + "Example Request:\n" + "\n" + "loanproducts/template")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsTemplateResponse.class))) })
    public String retrieveTemplate(@Context final UriInfo uriInfo,
            @QueryParam("isProductMixTemplate") @Parameter(description = "isProductMixTemplate") final boolean isProductMixTemplate) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (isProductMixTemplate) {
            this.context.authenticatedUser().validateHasReadPermission(PRODUCTMIX);

            final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAvailableLoanProductsForMix();
            final ProductMixData productMixData = ProductMixData.template(productOptions);
            return this.productMixDataApiJsonSerializer.serialize(settings, productMixData, PRODUCT_MIX_DATA_PARAMETERS);
        }

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
        loanProduct = handleTemplate(loanProduct);

        return this.toApiJsonSerializer.serialize(settings, loanProduct, LOAN_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Product", description = "Retrieves a Loan Product\n\n" + "Example Requests:\n" + "\n"
            + "loanproducts/1\n" + "\n" + "\n" + "loanproducts/1?template=true\n" + "\n" + "\n"
            + "loanproducts/1?fields=name,description,numberOfRepayments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsProductIdResponse.class))) })
    public String retrieveLoanProductDetails(@PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        return getLoanProductDetails(productId, uriInfo);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Product", description = "Updates a Loan Product")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdResponse.class))) })
    public String updateLoanProduct(@PathParam("productId") @Parameter(description = "productId") final Long productId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        return getUpdateLoanProductResult(apiRequestBodyAsJson, productId);
    }

    @GET
    @Path("external-id/{externalProductId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Retrieve a Loan Product", description = "Retrieves a Loan Product\n\n" + "Example Requests:\n" + "\n"
            + "loanproducts/external-id/2075e308-d4a8-44d9-8203-f5a947b8c2f4\n" + "\n" + "\n"
            + "loanproducts/external-id/2075e308-d4a8-44d9-8203-f5a947b8c2f4?template=true\n" + "\n" + "\n"
            + "loanproducts/external-id/2075e308-d4a8-44d9-8203-f5a947b8c2f4?fields=name,description,numberOfRepayments")
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.GetLoanProductsProductIdResponse.class))) })
    public String retrieveLoanProductDetails(
            @PathParam("externalProductId") @Parameter(description = "externalProductId") final String externalProductId,
            @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(RESOURCE_NAME_FOR_PERMISSIONS);

        ExternalId externalId = ExternalIdFactory.produce(externalProductId);

        Long productId = resolveProductId(externalId);
        if (Objects.isNull(productId)) {
            throw new LoanProductNotFoundException(externalId);
        }

        return getLoanProductDetails(productId, uriInfo);
    }

    @PUT
    @Path("external-id/{externalProductId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    @Operation(summary = "Update a Loan Product", description = "Updates a Loan Product")
    @RequestBody(required = true, content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdRequest.class)))
    @ApiResponses({
            @ApiResponse(responseCode = "200", description = "OK", content = @Content(schema = @Schema(implementation = LoanProductsApiResourceSwagger.PutLoanProductsProductIdResponse.class))) })
    public String updateLoanProduct(
            @PathParam("externalProductId") @Parameter(description = "externalProductId") final String externalProductId,
            @Parameter(hidden = true) final String apiRequestBodyAsJson) {

        ExternalId externalId = ExternalIdFactory.produce(externalProductId);

        Long productId = resolveProductId(externalId);

        if (Objects.isNull(productId)) {
            throw new LoanProductNotFoundException(externalId);
        }

        return getUpdateLoanProductResult(apiRequestBodyAsJson, productId);
    }

    private String getUpdateLoanProductResult(String apiRequestBodyAsJson, Long productId) {
        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    private Long resolveProductId(ExternalId externalProductId) {
        return loanProductReadPlatformService.retrieveLoanProductByExternalId(externalProductId).getId();
    }

    private String getLoanProductDetails(Long productId, UriInfo uriInfo) {
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);

        Map<String, Object> accountingMappings;
        Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
        Collection<ChargeToGLAccountMapper> feeToGLAccountMappings;
        Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings;
        if (loanProduct.hasAccountingEnabled()) {
            accountingMappings = this.accountMappingReadPlatformService.fetchAccountMappingDetailsForLoanProduct(productId,
                    loanProduct.getAccountingRule().getId().intValue());
            paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForLoanProduct(productId);
            feeToGLAccountMappings = this.accountMappingReadPlatformService.fetchFeeToGLAccountMappingsForLoanProduct(productId);
            penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForLoanProduct(productId);
            loanProduct = LoanProductData.withAccountingDetails(loanProduct, accountingMappings, paymentChannelToFundSourceMappings,
                    feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            loanProduct = handleTemplate(loanProduct);
        }
        return this.toApiJsonSerializer.serialize(settings, loanProduct, LOAN_PRODUCT_DATA_PARAMETERS);
    }

    private LoanProductData handleTemplate(final LoanProductData productData) {

        Collection<ChargeData> chargeOptions = this.chargeReadPlatformService.retrieveLoanApplicableFees();
        if (chargeOptions.isEmpty()) {
            chargeOptions = null;
        }

        Collection<ChargeData> penaltyOptions = this.chargeReadPlatformService.retrieveLoanApplicablePenalties();
        if (penaltyOptions.isEmpty()) {
            penaltyOptions = null;
        }

        boolean isRatesEnabled = this.configurationDomainService.isSubRatesEnabled();
        Collection<RateData> rateOptions = this.rateReadService.retrieveLoanApplicableRates();
        if (rateOptions.isEmpty()) {
            rateOptions = null;
        }

        final Collection<CurrencyData> currencyOptions = this.currencyReadPlatformService.retrieveAllowedCurrencies();
        final List<EnumOptionData> amortizationTypeOptions = this.dropdownReadPlatformService.retrieveLoanAmortizationTypeOptions();
        final List<EnumOptionData> interestTypeOptions = this.dropdownReadPlatformService.retrieveLoanInterestTypeOptions();
        final List<EnumOptionData> interestCalculationPeriodTypeOptions = this.dropdownReadPlatformService
                .retrieveLoanInterestRateCalculatedInPeriodOptions();
        final List<EnumOptionData> repaymentFrequencyTypeOptions = this.dropdownReadPlatformService.retrieveRepaymentFrequencyTypeOptions();
        final List<EnumOptionData> interestRateFrequencyTypeOptions = this.dropdownReadPlatformService
                .retrieveInterestRateFrequencyTypeOptions();
        final Collection<PaymentTypeData> paymentTypeOptions = this.paymentTypeReadPlatformService.retrieveAllPaymentTypes();

        Collection<FundData> fundOptions = this.fundReadPlatformService.retrieveAllFunds();
        if (fundOptions.isEmpty()) {
            fundOptions = null;
        }

        Collection<DelinquencyBucketData> delinquencyBucketOptions = this.delinquencyReadPlatformService.retrieveAllDelinquencyBuckets();
        if (delinquencyBucketOptions.isEmpty()) {
            delinquencyBucketOptions = null;
        }

        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService
                .retrieveTransactionProcessingStrategies();

        final Map<String, List<GLAccountData>> accountOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountMappingOptionsForLoanProducts();

        final List<EnumOptionData> accountingRuleTypeOptions = this.accountingDropdownReadPlatformService
                .retrieveAccountingRuleTypeOptions();

        final List<EnumOptionData> loanCycleValueConditionTypeOptions = this.dropdownReadPlatformService
                .retrieveLoanCycleValueConditionTypeOptions();

        final List<EnumOptionData> daysInMonthTypeOptions = commonDropdownReadPlatformService.retrieveDaysInMonthTypeOptions();
        final List<EnumOptionData> daysInYearTypeOptions = commonDropdownReadPlatformService.retrieveDaysInYearTypeOptions();
        final List<EnumOptionData> interestRecalculationCompoundingTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationCompoundingTypeOptions();
        final List<EnumOptionData> rescheduleStrategyTypeOptions = dropdownReadPlatformService.retrieveRescheduleStrategyTypeOptions();
        final List<EnumOptionData> interestRecalculationFrequencyTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationFrequencyTypeOptions();
        final List<EnumOptionData> interestRecalculationNthDayTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationNthDayTypeOptions();
        final List<EnumOptionData> interestRecalculationDayOfWeekTypeOptions = dropdownReadPlatformService
                .retrieveInterestRecalculationDayOfWeekTypeOptions();
        final List<EnumOptionData> preCloseInterestCalculationStrategyOptions = dropdownReadPlatformService
                .retrievePreCloseInterestCalculationStrategyOptions();
        final List<FloatingRateData> floatingRateOptions = this.floatingRateReadPlatformService.retrieveLookupActive();
        final List<EnumOptionData> repaymentStartDateTypeOptions = dropdownReadPlatformService.retrieveRepaymentStartDateTypeOptions();
        final List<EnumOptionData> advancedPaymentAllocationTransactionTypes = PaymentAllocationTransactionType
                .getValuesAsEnumOptionDataList();
        final List<EnumOptionData> advancedPaymentAllocationFutureInstallmentAllocationRules = FutureInstallmentAllocationRule
                .getValuesAsEnumOptionDataList();
        final List<EnumOptionData> advancedPaymentAllocationTypes = PaymentAllocationType.getValuesAsEnumOptionDataList();
        final List<EnumOptionData> creditAllocationTransactionTypes = CreditAllocationTransactionType.getValuesAsEnumOptionDataList();
        final List<EnumOptionData> creditAllocationAllocationTypes = AllocationType.getValuesAsEnumOptionDataList();

        return new LoanProductData(productData, chargeOptions, penaltyOptions, paymentTypeOptions, currencyOptions, amortizationTypeOptions,
                interestTypeOptions, interestCalculationPeriodTypeOptions, repaymentFrequencyTypeOptions, interestRateFrequencyTypeOptions,
                fundOptions, transactionProcessingStrategyOptions, rateOptions, accountOptions, accountingRuleTypeOptions,
                loanCycleValueConditionTypeOptions, daysInMonthTypeOptions, daysInYearTypeOptions,
                interestRecalculationCompoundingTypeOptions, rescheduleStrategyTypeOptions, interestRecalculationFrequencyTypeOptions,
                preCloseInterestCalculationStrategyOptions, floatingRateOptions, interestRecalculationNthDayTypeOptions,
                interestRecalculationDayOfWeekTypeOptions, isRatesEnabled, delinquencyBucketOptions, repaymentStartDateTypeOptions,
                advancedPaymentAllocationTransactionTypes, advancedPaymentAllocationFutureInstallmentAllocationRules,
                advancedPaymentAllocationTypes, LoanScheduleType.getValuesAsEnumOptionDataList(),
                LoanScheduleProcessingType.getValuesAsEnumOptionDataList(), creditAllocationTransactionTypes,
                creditAllocationAllocationTypes);
    }

}
