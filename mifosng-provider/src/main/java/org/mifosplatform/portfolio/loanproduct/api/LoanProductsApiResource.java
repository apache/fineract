/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.api;

import java.util.Arrays;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.POST;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Context;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.UriInfo;

import org.mifosplatform.accounting.common.AccountingDropdownReadPlatformService;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.service.ProductToGLAccountMappingReadPlatformService;
import org.mifosplatform.commands.domain.CommandWrapper;
import org.mifosplatform.commands.service.CommandWrapperBuilder;
import org.mifosplatform.commands.service.PortfolioCommandSourceWritePlatformService;
import org.mifosplatform.infrastructure.core.api.ApiParameterHelper;
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
import org.mifosplatform.portfolio.common.service.DropdownReadPlatformService;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.fund.service.FundReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.data.LoanProductData;
import org.mifosplatform.portfolio.loanproduct.data.TransactionProcessingStrategyData;
import org.mifosplatform.portfolio.loanproduct.productmix.data.ProductMixData;
import org.mifosplatform.portfolio.loanproduct.productmix.service.ProductMixReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanDropdownReadPlatformService;
import org.mifosplatform.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.mifosplatform.portfolio.paymenttype.service.PaymentTypeReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

@Path("/loanproducts")
@Component
@Scope("singleton")
public class LoanProductsApiResource {

    private final Set<String> LOAN_PRODUCT_DATA_PARAMETERS = new HashSet<>(Arrays.asList("id", "name", "shortName", "description",
            "fundId", "fundName", "includeInBorrowerCycle", "currency", "principal", "minPrincipal", "maxPrincipal", "numberOfRepayments",
            "minNumberOfRepayments", "maxNumberOfRepayments", "repaymentEvery", "repaymentFrequencyType", "graceOnPrincipalPayment",
            "graceOnInterestPayment", "graceOnInterestCharged", "interestRatePerPeriod", "minInterestRatePerPeriod",
            "maxInterestRatePerPeriod", "interestRateFrequencyType", "annualInterestRate", "amortizationType", "interestType",
            "interestCalculationPeriodType", "inArrearsTolerance", "transactionProcessingStrategyId", "transactionProcessingStrategyName",
            "charges", "accountingRule", "externalId", "accountingMappings", "paymentChannelToFundSourceMappings", "fundOptions",
            "paymentTypeOptions", "currencyOptions", "repaymentFrequencyTypeOptions", "interestRateFrequencyTypeOptions",
            "amortizationTypeOptions", "interestTypeOptions", "interestCalculationPeriodTypeOptions",
            "transactionProcessingStrategyOptions", "chargeOptions", "accountingOptions", "accountingRuleOptions",
            "accountingMappingOptions"));

    private final Set<String> PRODUCT_MIX_DATA_PARAMETERS = new HashSet<>(Arrays.asList("productId", "productName", "restrictedProducts",
            "allowedProducts", "productOptions"));

    private final String resourceNameForPermissions = "LOANPRODUCT";

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

    @Autowired
    public LoanProductsApiResource(final PlatformSecurityContext context, final LoanProductReadPlatformService readPlatformService,
            final ChargeReadPlatformService chargeReadPlatformService, final CurrencyReadPlatformService currencyReadPlatformService,
            final FundReadPlatformService fundReadPlatformService, final LoanDropdownReadPlatformService dropdownReadPlatformService,
            final DefaultToApiJsonSerializer<LoanProductData> toApiJsonSerializer,
            final ApiRequestParameterHelper apiRequestParameterHelper,
            final PortfolioCommandSourceWritePlatformService commandsSourceWritePlatformService,
            final ProductToGLAccountMappingReadPlatformService accountMappingReadPlatformService,
            final AccountingDropdownReadPlatformService accountingDropdownReadPlatformService,
            final DefaultToApiJsonSerializer<ProductMixData> productMixDataApiJsonSerializer,
            final ProductMixReadPlatformService productMixReadPlatformService,
            final DropdownReadPlatformService commonDropdownReadPlatformService,
            PaymentTypeReadPlatformService paymentTypeReadPlatformService) {
        this.context = context;
        this.loanProductReadPlatformService = readPlatformService;
        this.chargeReadPlatformService = chargeReadPlatformService;
        this.currencyReadPlatformService = currencyReadPlatformService;
        this.fundReadPlatformService = fundReadPlatformService;
        this.dropdownReadPlatformService = dropdownReadPlatformService;
        this.toApiJsonSerializer = toApiJsonSerializer;
        this.apiRequestParameterHelper = apiRequestParameterHelper;
        this.commandsSourceWritePlatformService = commandsSourceWritePlatformService;
        this.accountMappingReadPlatformService = accountMappingReadPlatformService;
        this.accountingDropdownReadPlatformService = accountingDropdownReadPlatformService;
        this.productMixDataApiJsonSerializer = productMixDataApiJsonSerializer;
        this.productMixReadPlatformService = productMixReadPlatformService;
        this.commonDropdownReadPlatformService = commonDropdownReadPlatformService;
        this.paymentTypeReadPlatformService = paymentTypeReadPlatformService;
    }

    @POST
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String createLoanProduct(final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().createLoanProduct().withJson(apiRequestBodyAsJson).build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
    }

    @GET
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveAllLoanProducts(@Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final Set<String> associationParameters = ApiParameterHelper.extractAssociationsForResponseIfProvided(uriInfo.getQueryParameters());
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (!associationParameters.isEmpty()) {
            if (associationParameters.contains("productMixes")) {
                this.context.authenticatedUser().validateHasReadPermission("PRODUCTMIX");
                final Collection<ProductMixData> productMixes = this.productMixReadPlatformService.retrieveAllProductMixes();
                return this.productMixDataApiJsonSerializer.serialize(settings, productMixes, this.PRODUCT_MIX_DATA_PARAMETERS);
            }
        }

        final Collection<LoanProductData> products = this.loanProductReadPlatformService.retrieveAllLoanProducts();

        return this.toApiJsonSerializer.serialize(settings, products, this.LOAN_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("template")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveTemplate(@Context final UriInfo uriInfo, @QueryParam("isProductMixTemplate") final boolean isProductMixTemplate) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);
        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        if (isProductMixTemplate) {
            this.context.authenticatedUser().validateHasReadPermission("PRODUCTMIX");

            final Collection<LoanProductData> productOptions = this.loanProductReadPlatformService.retrieveAvailableLoanProductsForMix();
            final ProductMixData productMixData = ProductMixData.template(productOptions);
            return this.productMixDataApiJsonSerializer.serialize(settings, productMixData, this.PRODUCT_MIX_DATA_PARAMETERS);
        }

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveNewLoanProductDetails();
        loanProduct = handleTemplate(loanProduct);

        return this.toApiJsonSerializer.serialize(settings, loanProduct, this.LOAN_PRODUCT_DATA_PARAMETERS);
    }

    @GET
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String retrieveLoanProductDetails(@PathParam("productId") final Long productId, @Context final UriInfo uriInfo) {

        this.context.authenticatedUser().validateHasReadPermission(this.resourceNameForPermissions);

        final ApiRequestJsonSerializationSettings settings = this.apiRequestParameterHelper.process(uriInfo.getQueryParameters());

        LoanProductData loanProduct = this.loanProductReadPlatformService.retrieveLoanProduct(productId);

        Map<String, Object> accountingMappings = null;
        Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = null;
        Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings = null;
        if (loanProduct.hasAccountingEnabled()) {
            accountingMappings = this.accountMappingReadPlatformService.fetchAccountMappingDetailsForLoanProduct(productId, loanProduct
                    .accountingRuleType().getId().intValue());
            paymentChannelToFundSourceMappings = this.accountMappingReadPlatformService
                    .fetchPaymentTypeToFundSourceMappingsForLoanProduct(productId);
            feeToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchFeeToIncomeOrLiabilityAccountMappingsForLoanProduct(productId);
            penaltyToGLAccountMappings = this.accountMappingReadPlatformService
                    .fetchPenaltyToIncomeAccountMappingsForLoanProduct(productId);
            loanProduct = LoanProductData.withAccountingDetails(loanProduct, accountingMappings, paymentChannelToFundSourceMappings,
                    feeToGLAccountMappings, penaltyToGLAccountMappings);
        }

        if (settings.isTemplate()) {
            loanProduct = handleTemplate(loanProduct);
        }
        return this.toApiJsonSerializer.serialize(settings, loanProduct, this.LOAN_PRODUCT_DATA_PARAMETERS);
    }

    @PUT
    @Path("{productId}")
    @Consumes({ MediaType.APPLICATION_JSON })
    @Produces({ MediaType.APPLICATION_JSON })
    public String updateLoanProduct(@PathParam("productId") final Long productId, final String apiRequestBodyAsJson) {

        final CommandWrapper commandRequest = new CommandWrapperBuilder().updateLoanProduct(productId).withJson(apiRequestBodyAsJson)
                .build();

        final CommandProcessingResult result = this.commandsSourceWritePlatformService.logCommandSource(commandRequest);

        return this.toApiJsonSerializer.serialize(result);
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
        final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions = this.dropdownReadPlatformService
                .retreiveTransactionProcessingStrategies();

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
        final List<EnumOptionData> preCloseInterestCalculationStrategyOptions = dropdownReadPlatformService
                .retrivePreCloseInterestCalculationStrategyOptions();

        return new LoanProductData(productData, chargeOptions, penaltyOptions, paymentTypeOptions, currencyOptions,
                amortizationTypeOptions, interestTypeOptions, interestCalculationPeriodTypeOptions, repaymentFrequencyTypeOptions,
                interestRateFrequencyTypeOptions, fundOptions, transactionProcessingStrategyOptions, accountOptions,
                accountingRuleTypeOptions, loanCycleValueConditionTypeOptions, daysInMonthTypeOptions, daysInYearTypeOptions,
                interestRecalculationCompoundingTypeOptions, rescheduleStrategyTypeOptions, interestRecalculationFrequencyTypeOptions,
                preCloseInterestCalculationStrategyOptions);
    }

}