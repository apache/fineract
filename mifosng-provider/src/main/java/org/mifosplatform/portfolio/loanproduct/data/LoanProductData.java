/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.loanproduct.data;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.joda.time.LocalDate;
import org.mifosplatform.accounting.common.AccountingEnumerations;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.calendar.data.CalendarData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.common.domain.DaysInMonthType;
import org.mifosplatform.portfolio.common.domain.DaysInYearType;
import org.mifosplatform.portfolio.common.domain.PeriodFrequencyType;
import org.mifosplatform.portfolio.common.service.CommonEnumerations;
import org.mifosplatform.portfolio.fund.data.FundData;
import org.mifosplatform.portfolio.loanaccount.data.LoanInterestRecalculationData;
import org.mifosplatform.portfolio.loanproduct.domain.AmortizationMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestCalculationPeriodMethod;
import org.mifosplatform.portfolio.loanproduct.domain.InterestMethod;
import org.mifosplatform.portfolio.loanproduct.domain.LoanProductConfigurableAttributes;
import org.mifosplatform.portfolio.loanproduct.service.LoanEnumerations;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;
import org.springframework.util.CollectionUtils;

/**
 * Immutable data object to represent loan products.
 */
public class LoanProductData {

    private final Long id;
    private final String name;
    private final String shortName;
    private final String description;
    private final Long fundId;
    private final String fundName;
    private final boolean includeInBorrowerCycle;
    private final boolean useBorrowerCycle;
    private final LocalDate startDate;
    private final LocalDate closeDate;
    private final String status;
    private final String externalId;
    // terms
    private final CurrencyData currency;
    private final BigDecimal principal;
    private final BigDecimal minPrincipal;
    private final BigDecimal maxPrincipal;
    private final Integer numberOfRepayments;
    private final Integer minNumberOfRepayments;
    private final Integer maxNumberOfRepayments;
    private final Integer repaymentEvery;
    private final EnumOptionData repaymentFrequencyType;
    private final BigDecimal interestRatePerPeriod;
    private final BigDecimal minInterestRatePerPeriod;
    private final BigDecimal maxInterestRatePerPeriod;
    private final EnumOptionData interestRateFrequencyType;
    private final BigDecimal annualInterestRate;

    // settings
    private final EnumOptionData amortizationType;
    private final EnumOptionData interestType;
    private final EnumOptionData interestCalculationPeriodType;
    private final BigDecimal inArrearsTolerance;
    private final Long transactionProcessingStrategyId;
    private final String transactionProcessingStrategyName;
    private final Integer graceOnPrincipalPayment;
    private final Integer graceOnInterestPayment;
    private final Integer graceOnInterestCharged;
    private final Integer graceOnArrearsAgeing;
    private final Integer overdueDaysForNPA;
    private final EnumOptionData daysInMonthType;
    private final EnumOptionData daysInYearType;
    private final boolean isInterestRecalculationEnabled;
    private final LoanProductInterestRecalculationData interestRecalculationData;
    private final Integer minimumDaysBetweenDisbursalAndFirstRepayment;
    private final boolean canDefineInstallmentAmount;
    private final Integer installmentAmountInMultiplesOf;

    // charges
    private final Collection<ChargeData> charges;

    private final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle;
    private final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle;
    // accounting
    private final EnumOptionData accountingRule;
    private Map<String, Object> accountingMappings;
    private Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    private Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

    // template related
    private final Collection<FundData> fundOptions;
    @SuppressWarnings("unused")
    private final Collection<PaymentTypeData> paymentTypeOptions;
    @SuppressWarnings("unused")
    private final Collection<CurrencyData> currencyOptions;
    private final List<EnumOptionData> repaymentFrequencyTypeOptions;
    private final List<EnumOptionData> interestRateFrequencyTypeOptions;
    private final List<EnumOptionData> amortizationTypeOptions;
    private final List<EnumOptionData> interestTypeOptions;
    private final List<EnumOptionData> interestCalculationPeriodTypeOptions;
    private final Collection<TransactionProcessingStrategyData> transactionProcessingStrategyOptions;
    private final Collection<ChargeData> chargeOptions;
    @SuppressWarnings("unused")
    private final Collection<ChargeData> penaltyOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> accountingRuleOptions;
    @SuppressWarnings("unused")
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> valueConditionTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInMonthTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> daysInYearTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationCompoundingTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> rescheduleStrategyTypeOptions;
    @SuppressWarnings("unused")
    private final List<EnumOptionData> preClosureInterestCalculationStrategyOptions;

    @SuppressWarnings("unused")
    private final List<EnumOptionData> interestRecalculationFrequencyTypeOptions;

    private final Boolean multiDisburseLoan;
    private final Integer maxTrancheCount;
    private final BigDecimal outstandingLoanBalance;
    private final BigDecimal principalThresholdForLastInstallment;

    private final Boolean holdGuaranteeFunds;
    private final LoanProductGuaranteeData productGuaranteeData;
    private final Boolean accountMovesOutOfNPAOnlyOnArrearsCompletion;
    private LoanProductConfigurableAttributes allowAttributeOverrides;

    /**
     * Used when returning lookup information about loan product for dropdowns.
     */
    public static LoanProductData lookup(final Long id, final String name) {
        final String shortName = null;
        final String description = null;
        final CurrencyData currency = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;
        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final Boolean holdGuaranteeFunds = false;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, charges,
                accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes);

    }

    public static LoanProductData lookupWithCurrency(final Long id, final String name, final CurrencyData currency) {
        final String shortName = null;
        final String description = null;
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;
        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData repaymentFrequencyType = null;
        final EnumOptionData interestRateFrequencyType = null;
        final EnumOptionData amortizationType = null;
        final EnumOptionData interestType = null;
        final EnumOptionData interestCalculationPeriodType = null;
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;
        final Integer graceOnPrincipalPayment = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final EnumOptionData accountingType = null;
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;

        final Collection<LoanProductBorrowerCycleVariationData> principalVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariations = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations = new ArrayList<>(1);
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;

        final EnumOptionData daysInMonthType = null;
        final EnumOptionData daysInYearType = null;
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = null;
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, charges,
                accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId, principalVariations,
                interestRateVariations, numberOfRepaymentVariations, multiDisburseLoan, maxTrancheCount, outstandingLoanBalance,
                graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType, daysInYearType, isInterestRecalculationEnabled,
                interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment, holdGuaranteeFunds, productGuaranteeData,
                principalThresholdForLastInstallment, accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount,
                installmentAmountInMultiplesOf, loanProductConfigurableAttributes);

    }

    public static LoanProductData sensibleDefaultsForNewLoanProductCreation() {
        final Long id = null;
        final String name = null;
        final String shortName = null;
        final String description = null;
        final CurrencyData currency = CurrencyData.blank();
        final BigDecimal principal = null;
        final BigDecimal minPrincipal = null;
        final BigDecimal maxPrincipal = null;
        final BigDecimal tolerance = null;
        final Integer numberOfRepayments = null;
        final Integer minNumberOfRepayments = null;
        final Integer maxNumberOfRepayments = null;

        final Integer repaymentEvery = null;
        final BigDecimal interestRatePerPeriod = null;
        final BigDecimal minInterestRatePerPeriod = null;
        final BigDecimal maxInterestRatePerPeriod = null;
        final BigDecimal annualInterestRate = null;
        final EnumOptionData repaymentFrequencyType = LoanEnumerations.repaymentFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData interestRateFrequencyType = LoanEnumerations.interestRateFrequencyType(PeriodFrequencyType.MONTHS);
        final EnumOptionData amortizationType = LoanEnumerations.amortizationType(AmortizationMethod.EQUAL_INSTALLMENTS);
        final EnumOptionData interestType = LoanEnumerations.interestType(InterestMethod.DECLINING_BALANCE);
        final EnumOptionData interestCalculationPeriodType = LoanEnumerations
                .interestCalculationPeriodType(InterestCalculationPeriodMethod.SAME_AS_REPAYMENT_PERIOD);
        final Long fundId = null;
        final String fundName = null;
        final Long transactionProcessingStrategyId = null;
        final String transactionProcessingStrategyName = null;

        final Integer graceOnPrincipalPayment = null;
        final Integer graceOnInterestPayment = null;
        final Integer graceOnInterestCharged = null;
        final Integer graceOnArrearsAgeing = null;
        final Integer overdueDaysForNPA = null;

        final Collection<ChargeData> charges = null;
        final Collection<LoanProductBorrowerCycleVariationData> principalVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> interestRateVariationsForBorrowerCycle = new ArrayList<>(1);
        final Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariationsForBorrowerCycle = new ArrayList<>(1);

        final EnumOptionData accountingType = AccountingEnumerations.accountingRuleType(AccountingRuleType.NONE);
        final boolean includeInBorrowerCycle = false;
        final boolean useBorrowerCycle = false;
        final LocalDate startDate = null;
        final LocalDate closeDate = null;
        final String status = null;
        final String externalId = null;
        final Boolean multiDisburseLoan = null;
        final Integer maxTrancheCount = null;
        final BigDecimal outstandingLoanBalance = null;

        final EnumOptionData daysInMonthType = CommonEnumerations.daysInMonthType(DaysInMonthType.ACTUAL);
        final EnumOptionData daysInYearType = CommonEnumerations.daysInYearType(DaysInYearType.ACTUAL);
        final boolean isInterestRecalculationEnabled = false;
        final LoanProductInterestRecalculationData interestRecalculationData = LoanProductInterestRecalculationData
                .sensibleDefaultsForNewLoanProductCreation();
        final Integer minimumDaysBetweenDisbursalAndFirstRepayment = null;
        final Boolean holdGuaranteeFunds = false;
        final LoanProductGuaranteeData productGuaranteeData = null;
        final BigDecimal principalThresholdForLastInstallment = null;
        final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion = false;
        final boolean canDefineInstallmentAmount = false;
        final Integer installmentAmountInMultiplesOf = null;
        final LoanProductConfigurableAttributes loanProductConfigurableAttributes = null;

        return new LoanProductData(id, name, shortName, description, currency, principal, minPrincipal, maxPrincipal, tolerance,
                numberOfRepayments, minNumberOfRepayments, maxNumberOfRepayments, repaymentEvery, interestRatePerPeriod,
                minInterestRatePerPeriod, maxInterestRatePerPeriod, annualInterestRate, repaymentFrequencyType, interestRateFrequencyType,
                amortizationType, interestType, interestCalculationPeriodType, fundId, fundName, transactionProcessingStrategyId,
                transactionProcessingStrategyName, graceOnPrincipalPayment, graceOnInterestPayment, graceOnInterestCharged, charges,
                accountingType, includeInBorrowerCycle, useBorrowerCycle, startDate, closeDate, status, externalId,
                principalVariationsForBorrowerCycle, interestRateVariationsForBorrowerCycle, numberOfRepaymentVariationsForBorrowerCycle,
                multiDisburseLoan, maxTrancheCount, outstandingLoanBalance, graceOnArrearsAgeing, overdueDaysForNPA, daysInMonthType,
                daysInYearType, isInterestRecalculationEnabled, interestRecalculationData, minimumDaysBetweenDisbursalAndFirstRepayment,
                holdGuaranteeFunds, productGuaranteeData, principalThresholdForLastInstallment,
                accountMovesOutOfNPAOnlyOnArrearsCompletion, canDefineInstallmentAmount, installmentAmountInMultiplesOf, loanProductConfigurableAttributes);

    }

    public static LoanProductData withAccountingDetails(final LoanProductData productData, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings,
            final Collection<ChargeToGLAccountMapper> penaltyToGLAccountMappings) {
        productData.accountingMappings = accountingMappings;
        productData.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;
        productData.feeToIncomeAccountMappings = feeToGLAccountMappings;
        productData.penaltyToIncomeAccountMappings = penaltyToGLAccountMappings;
        return productData;
    }

    public LoanProductData(final Long id, final String name, final String shortName, final String description, final CurrencyData currency,
            final BigDecimal principal, final BigDecimal minPrincipal, final BigDecimal maxPrincipal, final BigDecimal tolerance,
            final Integer numberOfRepayments, final Integer minNumberOfRepayments, final Integer maxNumberOfRepayments,
            final Integer repaymentEvery, final BigDecimal interestRatePerPeriod, final BigDecimal minInterestRatePerPeriod,
            final BigDecimal maxInterestRatePerPeriod, final BigDecimal annualInterestRate, final EnumOptionData repaymentFrequencyType,
            final EnumOptionData interestRateFrequencyType, final EnumOptionData amortizationType, final EnumOptionData interestType,
            final EnumOptionData interestCalculationPeriodType, final Long fundId, final String fundName,
            final Long transactionProcessingStrategyId, final String transactionProcessingStrategyName,
            final Integer graceOnPrincipalPayment, final Integer graceOnInterestPayment, final Integer graceOnInterestCharged,
            final Collection<ChargeData> charges, final EnumOptionData accountingType, final boolean includeInBorrowerCycle,
            boolean useBorrowerCycle, final LocalDate startDate, final LocalDate closeDate, final String status, final String externalId,
            Collection<LoanProductBorrowerCycleVariationData> principalVariations,
            Collection<LoanProductBorrowerCycleVariationData> interestRateVariations,
            Collection<LoanProductBorrowerCycleVariationData> numberOfRepaymentVariations, Boolean multiDisburseLoan,
            Integer maxTrancheCount, BigDecimal outstandingLoanBalance, final Integer graceOnArrearsAgeing,
            final Integer overdueDaysForNPA, final EnumOptionData daysInMonthType, final EnumOptionData daysInYearType,
            final boolean isInterestRecalculationEnabled, final LoanProductInterestRecalculationData interestRecalculationData,
            final Integer minimumDaysBetweenDisbursalAndFirstRepayment, boolean holdGuaranteeFunds,
            final LoanProductGuaranteeData loanProductGuaranteeData, final BigDecimal principalThresholdForLastInstallment,
            final boolean accountMovesOutOfNPAOnlyOnArrearsCompletion, boolean canDefineInstallmentAmount,
            Integer installmentAmountInMultiplesOf, LoanProductConfigurableAttributes allowAttributeOverrides) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.currency = currency;
        this.principal = principal;
        this.minPrincipal = minPrincipal;
        this.maxPrincipal = maxPrincipal;
        this.inArrearsTolerance = tolerance;
        this.numberOfRepayments = numberOfRepayments;
        this.minNumberOfRepayments = minNumberOfRepayments;
        this.maxNumberOfRepayments = maxNumberOfRepayments;
        this.graceOnPrincipalPayment = graceOnPrincipalPayment;
        this.graceOnInterestPayment = graceOnInterestPayment;
        this.graceOnInterestCharged = graceOnInterestCharged;
        this.repaymentEvery = repaymentEvery;
        this.interestRatePerPeriod = interestRatePerPeriod;
        this.minInterestRatePerPeriod = minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = maxInterestRatePerPeriod;
        this.annualInterestRate = annualInterestRate;
        this.repaymentFrequencyType = repaymentFrequencyType;
        this.interestRateFrequencyType = interestRateFrequencyType;
        this.amortizationType = amortizationType;
        this.interestType = interestType;
        this.interestCalculationPeriodType = interestCalculationPeriodType;
        this.fundId = fundId;
        this.fundName = fundName;
        this.transactionProcessingStrategyId = transactionProcessingStrategyId;
        this.transactionProcessingStrategyName = transactionProcessingStrategyName;
        this.charges = charges;
        this.accountingRule = accountingType;
        this.includeInBorrowerCycle = includeInBorrowerCycle;
        this.useBorrowerCycle = useBorrowerCycle;
        this.startDate = startDate;
        this.closeDate = closeDate;
        this.status = status;
        this.externalId = externalId;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = minimumDaysBetweenDisbursalAndFirstRepayment;

        this.chargeOptions = null;
        this.penaltyOptions = null;
        this.paymentTypeOptions = null;
        this.currencyOptions = null;
        this.fundOptions = null;
        this.transactionProcessingStrategyOptions = null;
        this.amortizationTypeOptions = null;
        this.interestTypeOptions = null;
        this.interestCalculationPeriodTypeOptions = null;
        this.repaymentFrequencyTypeOptions = null;
        this.interestRateFrequencyTypeOptions = null;

        this.accountingMappingOptions = null;
        this.accountingRuleOptions = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
        this.feeToIncomeAccountMappings = null;
        this.penaltyToIncomeAccountMappings = null;
        this.valueConditionTypeOptions = null;
        this.principalVariationsForBorrowerCycle = principalVariations;
        this.interestRateVariationsForBorrowerCycle = interestRateVariations;
        this.numberOfRepaymentVariationsForBorrowerCycle = numberOfRepaymentVariations;
        this.multiDisburseLoan = multiDisburseLoan;
        this.outstandingLoanBalance = outstandingLoanBalance;
        this.maxTrancheCount = maxTrancheCount;
        this.graceOnArrearsAgeing = graceOnArrearsAgeing;
        this.overdueDaysForNPA = overdueDaysForNPA;
        this.daysInMonthType = daysInMonthType;
        this.daysInYearType = daysInYearType;
        this.isInterestRecalculationEnabled = isInterestRecalculationEnabled;
        this.interestRecalculationData = interestRecalculationData;
        this.holdGuaranteeFunds = holdGuaranteeFunds;
        this.productGuaranteeData = loanProductGuaranteeData;
        this.principalThresholdForLastInstallment = principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = accountMovesOutOfNPAOnlyOnArrearsCompletion;
        this.allowAttributeOverrides = allowAttributeOverrides;

        this.daysInMonthTypeOptions = null;
        this.daysInYearTypeOptions = null;
        this.interestRecalculationCompoundingTypeOptions = null;
        this.rescheduleStrategyTypeOptions = null;
        this.interestRecalculationFrequencyTypeOptions = null;

        this.canDefineInstallmentAmount = canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = null;

    }

    public LoanProductData(final LoanProductData productData, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final Collection<PaymentTypeData> paymentTypeOptions,
            final Collection<CurrencyData> currencyOptions, final List<EnumOptionData> amortizationTypeOptions,
            final List<EnumOptionData> interestTypeOptions, final List<EnumOptionData> interestCalculationPeriodTypeOptions,
            final List<EnumOptionData> repaymentFrequencyTypeOptions, final List<EnumOptionData> interestRateFrequencyTypeOptions,
            final Collection<FundData> fundOptions, final Collection<TransactionProcessingStrategyData> transactionStrategyOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final List<EnumOptionData> accountingRuleOptions,
            final List<EnumOptionData> valueConditionTypeOptions, final List<EnumOptionData> daysInMonthTypeOptions,
            final List<EnumOptionData> daysInYearTypeOptions, final List<EnumOptionData> interestRecalculationCompoundingTypeOptions,
            final List<EnumOptionData> rescheduleStrategyTypeOptions, final List<EnumOptionData> interestRecalculationFrequencyTypeOptions,
            final List<EnumOptionData> preCloseInterestCalculationStrategyOptions) {
        this.id = productData.id;
        this.name = productData.name;
        this.shortName = productData.shortName;
        this.description = productData.description;
        this.fundId = productData.fundId;
        this.fundName = productData.fundName;

        this.principal = productData.principal;
        this.minPrincipal = productData.minPrincipal;
        this.maxPrincipal = productData.maxPrincipal;
        this.inArrearsTolerance = productData.inArrearsTolerance;
        this.numberOfRepayments = productData.numberOfRepayments;
        this.minNumberOfRepayments = productData.minNumberOfRepayments;
        this.maxNumberOfRepayments = productData.maxNumberOfRepayments;
        this.repaymentEvery = productData.repaymentEvery;
        this.interestRatePerPeriod = productData.interestRatePerPeriod;
        this.minInterestRatePerPeriod = productData.minInterestRatePerPeriod;
        this.maxInterestRatePerPeriod = productData.maxInterestRatePerPeriod;
        this.annualInterestRate = productData.annualInterestRate;
        this.repaymentFrequencyType = productData.repaymentFrequencyType;
        this.interestRateFrequencyType = productData.interestRateFrequencyType;
        this.amortizationType = productData.amortizationType;
        this.interestType = productData.interestType;
        this.interestCalculationPeriodType = productData.interestCalculationPeriodType;
        this.startDate = productData.startDate;
        this.closeDate = productData.closeDate;
        this.status = productData.status;
        this.externalId = productData.externalId;

        this.charges = nullIfEmpty(productData.charges());
        this.principalVariationsForBorrowerCycle = productData.principalVariationsForBorrowerCycle;
        this.interestRateVariationsForBorrowerCycle = productData.interestRateVariationsForBorrowerCycle;
        this.numberOfRepaymentVariationsForBorrowerCycle = productData.numberOfRepaymentVariationsForBorrowerCycle;
        this.accountingRule = productData.accountingRule;
        this.accountingMappings = productData.accountingMappings;
        this.paymentChannelToFundSourceMappings = productData.paymentChannelToFundSourceMappings;
        this.feeToIncomeAccountMappings = productData.feeToIncomeAccountMappings;
        this.penaltyToIncomeAccountMappings = productData.penaltyToIncomeAccountMappings;

        this.chargeOptions = chargeOptions;
        this.penaltyOptions = penaltyOptions;
        this.paymentTypeOptions = paymentTypeOptions;
        this.currencyOptions = currencyOptions;
        this.currency = productData.currency;
        this.fundOptions = fundOptions;
        this.transactionProcessingStrategyOptions = transactionStrategyOptions;
        if (this.transactionProcessingStrategyOptions != null && this.transactionProcessingStrategyOptions.size() == 1) {
            final List<TransactionProcessingStrategyData> listOfOptions = new ArrayList<>(this.transactionProcessingStrategyOptions);

            this.transactionProcessingStrategyId = listOfOptions.get(0).id();
            this.transactionProcessingStrategyName = listOfOptions.get(0).name();
        } else {
            this.transactionProcessingStrategyId = productData.transactionProcessingStrategyId;
            this.transactionProcessingStrategyName = productData.transactionProcessingStrategyName;
        }

        this.graceOnPrincipalPayment = productData.graceOnPrincipalPayment;
        this.graceOnInterestPayment = productData.graceOnInterestPayment;
        this.graceOnInterestCharged = productData.graceOnInterestCharged;
        this.includeInBorrowerCycle = productData.includeInBorrowerCycle;
        this.useBorrowerCycle = productData.useBorrowerCycle;
        this.multiDisburseLoan = productData.multiDisburseLoan;
        this.maxTrancheCount = productData.maxTrancheCount;
        this.outstandingLoanBalance = productData.outstandingLoanBalance;
        this.minimumDaysBetweenDisbursalAndFirstRepayment = productData.minimumDaysBetweenDisbursalAndFirstRepayment;

        this.amortizationTypeOptions = amortizationTypeOptions;
        this.interestTypeOptions = interestTypeOptions;
        this.interestCalculationPeriodTypeOptions = interestCalculationPeriodTypeOptions;
        this.repaymentFrequencyTypeOptions = repaymentFrequencyTypeOptions;
        this.interestRateFrequencyTypeOptions = interestRateFrequencyTypeOptions;

        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;
        this.valueConditionTypeOptions = valueConditionTypeOptions;
        this.graceOnArrearsAgeing = productData.graceOnArrearsAgeing;
        this.overdueDaysForNPA = productData.overdueDaysForNPA;

        this.daysInMonthType = productData.daysInMonthType;
        this.daysInYearType = productData.daysInYearType;
        this.isInterestRecalculationEnabled = productData.isInterestRecalculationEnabled;
        this.interestRecalculationData = productData.interestRecalculationData;
        this.holdGuaranteeFunds = productData.holdGuaranteeFunds;
        this.productGuaranteeData = productData.productGuaranteeData;
        this.principalThresholdForLastInstallment = productData.principalThresholdForLastInstallment;
        this.accountMovesOutOfNPAOnlyOnArrearsCompletion = productData.accountMovesOutOfNPAOnlyOnArrearsCompletion;

        this.daysInMonthTypeOptions = daysInMonthTypeOptions;
        this.daysInYearTypeOptions = daysInYearTypeOptions;
        this.interestRecalculationCompoundingTypeOptions = interestRecalculationCompoundingTypeOptions;
        this.rescheduleStrategyTypeOptions = rescheduleStrategyTypeOptions;
        this.allowAttributeOverrides = productData.allowAttributeOverrides;

        if (CollectionUtils.isEmpty(interestRecalculationFrequencyTypeOptions)) {
            this.interestRecalculationFrequencyTypeOptions = null;
        } else {
            this.interestRecalculationFrequencyTypeOptions = interestRecalculationFrequencyTypeOptions;
        }

        this.canDefineInstallmentAmount = productData.canDefineInstallmentAmount;
        this.installmentAmountInMultiplesOf = productData.installmentAmountInMultiplesOf;
        this.preClosureInterestCalculationStrategyOptions = preCloseInterestCalculationStrategyOptions;
    }

    private Collection<ChargeData> nullIfEmpty(final Collection<ChargeData> charges) {
        Collection<ChargeData> chargesLocal = charges;
        if (charges == null || charges.isEmpty()) {
            chargesLocal = null;
        }
        return chargesLocal;
    }

    public Collection<ChargeData> charges() {
        Collection<ChargeData> chargesLocal = new ArrayList<>();
        if (this.charges != null) {
            chargesLocal = this.charges;
        }
        return chargesLocal;
    }

    public EnumOptionData accountingRuleType() {
        return this.accountingRule;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getDescription() {
        return this.description;
    }

    public Long getFundId() {
        return this.fundId;
    }

    public String getFundName() {
        return this.fundName;
    }

    public Long getTransactionProcessingStrategyId() {
        return this.transactionProcessingStrategyId;
    }

    public String getTransactionProcessingStrategyName() {
        return this.transactionProcessingStrategyName;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public BigDecimal getPrincipal() {
        return this.principal;
    }

    public BigDecimal getMinPrincipal() {
        return this.minPrincipal;
    }

    public BigDecimal getMaxPrincipal() {
        return this.maxPrincipal;
    }

    public BigDecimal getInArrearsTolerance() {
        return this.inArrearsTolerance;
    }

    public Integer getNumberOfRepayments() {
        return this.numberOfRepayments;
    }

    public Integer getRepaymentEvery() {
        return this.repaymentEvery;
    }

    public BigDecimal getInterestRatePerPeriod() {
        return this.interestRatePerPeriod;
    }

    public BigDecimal getAnnualInterestRate() {
        return this.annualInterestRate;
    }

    public EnumOptionData getRepaymentFrequencyType() {
        return this.repaymentFrequencyType;
    }

    public Integer getGraceOnPrincipalPayment() {
        return this.graceOnPrincipalPayment;
    }

    public Integer getGraceOnInterestPayment() {
        return this.graceOnInterestPayment;
    }

    public Integer getGraceOnInterestCharged() {
        return this.graceOnInterestCharged;
    }

    public EnumOptionData getInterestRateFrequencyType() {
        return this.interestRateFrequencyType;
    }

    public EnumOptionData getAmortizationType() {
        return this.amortizationType;
    }

    public EnumOptionData getInterestType() {
        return this.interestType;
    }

    public EnumOptionData getInterestCalculationPeriodType() {
        return this.interestCalculationPeriodType;
    }

    public Collection<FundData> getFundOptions() {
        return this.fundOptions;
    }

    public List<EnumOptionData> getAmortizationTypeOptions() {
        return this.amortizationTypeOptions;
    }

    public List<EnumOptionData> getInterestTypeOptions() {
        return this.interestTypeOptions;
    }

    public List<EnumOptionData> getInterestCalculationPeriodTypeOptions() {
        return this.interestCalculationPeriodTypeOptions;
    }

    public List<EnumOptionData> getRepaymentFrequencyTypeOptions() {
        return this.repaymentFrequencyTypeOptions;
    }

    public List<EnumOptionData> getInterestRateFrequencyTypeOptions() {
        return this.interestRateFrequencyTypeOptions;
    }

    public Collection<ChargeData> getChargeOptions() {
        return this.chargeOptions;
    }

    @Override
    public boolean equals(final Object obj) {
        final LoanProductData loanProductData = (LoanProductData) obj;
        return loanProductData.id.equals(this.id);
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public boolean useBorrowerCycle() {
        return this.useBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getPrincipalVariationsForBorrowerCycle() {
        return this.principalVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getInterestRateVariationsForBorrowerCycle() {
        return this.interestRateVariationsForBorrowerCycle;
    }

    public Collection<LoanProductBorrowerCycleVariationData> getNumberOfRepaymentVariationsForBorrowerCycle() {
        return this.numberOfRepaymentVariationsForBorrowerCycle;
    }

    public Boolean getMultiDisburseLoan() {
        return this.multiDisburseLoan;
    }

    public BigDecimal getOutstandingLoanBalance() {
        return this.outstandingLoanBalance;
    }

    public Integer getGraceOnArrearsAgeing() {
        return this.graceOnArrearsAgeing;
    }

    public EnumOptionData getDaysInMonthType() {
        return this.daysInMonthType;
    }

    public EnumOptionData getDaysInYearType() {
        return this.daysInYearType;
    }

    public boolean isInterestRecalculationEnabled() {
        return this.isInterestRecalculationEnabled;
    }

    public LoanProductInterestRecalculationData getInterestRecalculationData() {
        return this.interestRecalculationData;
    }

    public Collection<ChargeData> overdueFeeCharges() {
        Collection<ChargeData> overdueFeeCharges = new ArrayList<>();
        Collection<ChargeData> charges = charges();
        for (ChargeData chargeData : charges) {
            if (chargeData.isOverdueInstallmentCharge()) {
                overdueFeeCharges.add(chargeData);
            }
        }
        return overdueFeeCharges;
    }

    public LoanInterestRecalculationData toLoanInterestRecalculationData() {
        final Long id = null;
        final Long loanId = null;
        final CalendarData calendarData = null;
        return new LoanInterestRecalculationData(id, loanId, getInterestRecalculationCompoundingType(), getRescheduleStrategyType(),
                calendarData, getRecalculationRestFrequencyType(), getRecalculationRestFrequencyInterval(),
                getRecalculationRestFrequencyDate());
    }

    private EnumOptionData getRescheduleStrategyType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRescheduleStrategyType(); }
        return null;
    }

    private EnumOptionData getInterestRecalculationCompoundingType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getInterestRecalculationCompoundingType(); }
        return null;
    }

    private LocalDate getRecalculationRestFrequencyDate() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyDate(); }
        return null;
    }

    public EnumOptionData getRecalculationRestFrequencyType() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyType(); }
        return null;
    }

    public Integer getRecalculationRestFrequencyInterval() {
        if (isInterestRecalculationEnabled()) { return this.interestRecalculationData.getRecalculationRestFrequencyInterval(); }
        return null;
    }

    public boolean canDefineInstallmentAmount() {
        return this.canDefineInstallmentAmount;
    }
    
    public LoanProductConfigurableAttributes getloanProductConfigurableAttributes() {
        return this.allowAttributeOverrides;
    }

    public void setloanProductConfigurableAttributes(LoanProductConfigurableAttributes loanProductConfigurableAttributes) {
        this.allowAttributeOverrides = loanProductConfigurableAttributes;
    }
}