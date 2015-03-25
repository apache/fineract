/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;

/**
 * Immutable data object represent a Recurring Deposit product.
 */
public class RecurringDepositProductData extends DepositProductData {

    private boolean preClosurePenalApplicable;
    private BigDecimal preClosurePenalInterest;
    private EnumOptionData preClosurePenalInterestOnType;
    private Integer minDepositTerm;
    private Integer maxDepositTerm;
    private EnumOptionData minDepositTermType;
    private EnumOptionData maxDepositTermType;
    private Integer inMultiplesOfDepositTerm;
    private EnumOptionData inMultiplesOfDepositTermType;
    private BigDecimal minDepositAmount;
    private BigDecimal depositAmount;
    private BigDecimal maxDepositAmount;
    private boolean isMandatoryDeposit;
    private boolean allowWithdrawal;
    private boolean adjustAdvanceTowardsFuturePayments;

    private Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions;
    private Collection<EnumOptionData> periodFrequencyTypeOptions;

    public static RecurringDepositProductData template(final CurrencyData currency, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final EnumOptionData accountingRule,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final InterestRateChartData chartTemplate,
            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Collection<EnumOptionData> periodFrequencyTypeOptions) {

        final Long id = null;
        final String name = null;
        final String shortName = null;
        final String description = null;
        final BigDecimal nominalAnnualInterestRate = null;

        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final BigDecimal minBalanceForInterestCalculation = null;

        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        final Collection<ChargeData> charges = null;
        final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings = null;
        final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = null;
        final Collection<InterestRateChartData> interestRateCharts = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final boolean isMandatoryDeposit = false;
        final boolean allowWithdrawal = false;
        final boolean adjustAdvanceTowardsFuturePayments = false;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal minDepositAmount = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maxDepositAmount = null;

        return new RecurringDepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, minBalanceForInterestCalculation, accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments, periodFrequencyTypeOptions, minDepositAmount, depositAmount, maxDepositAmount);
    }

    public static RecurringDepositProductData withCharges(final RecurringDepositProductData existingProduct,
            final Collection<ChargeData> charges) {
        return new RecurringDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
                existingProduct.description, existingProduct.currency, existingProduct.nominalAnnualInterestRate,
                existingProduct.interestCompoundingPeriodType, existingProduct.interestPostingPeriodType,
                existingProduct.interestCalculationType, existingProduct.interestCalculationDaysInYearType,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType,
                existingProduct.minBalanceForInterestCalculation, existingProduct.accountingRule, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, existingProduct.currencyOptions,
                existingProduct.interestCompoundingPeriodTypeOptions, existingProduct.interestPostingPeriodTypeOptions,
                existingProduct.interestCalculationTypeOptions, existingProduct.interestCalculationDaysInYearTypeOptions,
                existingProduct.lockinPeriodFrequencyTypeOptions, existingProduct.withdrawalFeeTypeOptions,
                existingProduct.paymentTypeOptions, existingProduct.accountingRuleOptions, existingProduct.accountingMappingOptions,
                charges, existingProduct.chargeOptions, existingProduct.penaltyOptions, existingProduct.feeToIncomeAccountMappings,
                existingProduct.penaltyToIncomeAccountMappings, existingProduct.interestRateCharts, existingProduct.chartTemplate,
                existingProduct.preClosurePenalApplicable, existingProduct.preClosurePenalInterest,
                existingProduct.preClosurePenalInterestOnType, existingProduct.preClosurePenalInterestOnTypeOptions,
                existingProduct.minDepositTerm, existingProduct.maxDepositTerm, existingProduct.minDepositTermType,
                existingProduct.maxDepositTermType, existingProduct.inMultiplesOfDepositTerm, existingProduct.inMultiplesOfDepositTermType,
                existingProduct.isMandatoryDeposit, existingProduct.allowWithdrawal, existingProduct.adjustAdvanceTowardsFuturePayments,
                existingProduct.periodFrequencyTypeOptions, existingProduct.minDepositAmount, existingProduct.depositAmount,
                existingProduct.maxDepositAmount);
    }

    /**
     * Returns a {@link RecurringDepositProductData} that contains and exist
     * {@link RecurringDepositProductData} data with further template data for
     * dropdowns.
     */
    public static RecurringDepositProductData withTemplate(final RecurringDepositProductData existingProduct,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final InterestRateChartData chartTemplate,
            final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Collection<EnumOptionData> periodFrequencyTypeOptions) {

        return new RecurringDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
                existingProduct.description, existingProduct.currency, existingProduct.nominalAnnualInterestRate,
                existingProduct.interestCompoundingPeriodType, existingProduct.interestPostingPeriodType,
                existingProduct.interestCalculationType, existingProduct.interestCalculationDaysInYearType,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType,
                existingProduct.minBalanceForInterestCalculation, existingProduct.accountingRule, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions,
                existingProduct.feeToIncomeAccountMappings, existingProduct.penaltyToIncomeAccountMappings,
                existingProduct.interestRateCharts, chartTemplate, existingProduct.preClosurePenalApplicable,
                existingProduct.preClosurePenalInterest, existingProduct.preClosurePenalInterestOnType,
                preClosurePenalInterestOnTypeOptions, existingProduct.minDepositTerm, existingProduct.maxDepositTerm,
                existingProduct.minDepositTermType, existingProduct.maxDepositTermType, existingProduct.inMultiplesOfDepositTerm,
                existingProduct.inMultiplesOfDepositTermType, existingProduct.isMandatoryDeposit, existingProduct.allowWithdrawal,
                existingProduct.adjustAdvanceTowardsFuturePayments, periodFrequencyTypeOptions, existingProduct.minDepositAmount,
                existingProduct.depositAmount, existingProduct.maxDepositAmount);

    }

    public static RecurringDepositProductData withAccountingDetails(final RecurringDepositProductData existingProduct,
            final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings,
            final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings) {

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;

        return new RecurringDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
                existingProduct.description, existingProduct.currency, existingProduct.nominalAnnualInterestRate,
                existingProduct.interestCompoundingPeriodType, existingProduct.interestPostingPeriodType,
                existingProduct.interestCalculationType, existingProduct.interestCalculationDaysInYearType,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType,
                existingProduct.minBalanceForInterestCalculation, existingProduct.accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, existingProduct.interestRateCharts, existingProduct.chartTemplate,
                existingProduct.preClosurePenalApplicable, existingProduct.preClosurePenalInterest,
                existingProduct.preClosurePenalInterestOnType, existingProduct.preClosurePenalInterestOnTypeOptions,
                existingProduct.minDepositTerm, existingProduct.maxDepositTerm, existingProduct.minDepositTermType,
                existingProduct.maxDepositTermType, existingProduct.inMultiplesOfDepositTerm, existingProduct.inMultiplesOfDepositTermType,
                existingProduct.isMandatoryDeposit, existingProduct.allowWithdrawal, existingProduct.adjustAdvanceTowardsFuturePayments,
                existingProduct.periodFrequencyTypeOptions, existingProduct.minDepositAmount, existingProduct.depositAmount,
                existingProduct.maxDepositAmount);
    }

    public static RecurringDepositProductData instance(final DepositProductData depositProductData,
            final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final EnumOptionData preClosurePenalInterestOnType, final Integer minDepositTerm, final Integer maxDepositTerm,
            final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType, final Integer inMultiplesOfDepositTerm,
            final EnumOptionData inMultiplesOfDepositTermType, final boolean isMandatoryDeposit, boolean allowWithdrawal,
            boolean adjustAdvanceTowardsFuturePayments, final BigDecimal minDepositAmount, final BigDecimal depositAmount,
            final BigDecimal maxDepositAmount) {

        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;
        final Collection<ChargeData> charges = null;
        final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings = null;
        final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = null;
        final Collection<InterestRateChartData> interestRateCharts = null;
        final InterestRateChartData chartTemplate = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        return new RecurringDepositProductData(depositProductData.id, depositProductData.name, depositProductData.shortName,
                depositProductData.description, depositProductData.currency, depositProductData.nominalAnnualInterestRate,
                depositProductData.interestCompoundingPeriodType, depositProductData.interestPostingPeriodType,
                depositProductData.interestCalculationType, depositProductData.interestCalculationDaysInYearType,
                depositProductData.lockinPeriodFrequency, depositProductData.lockinPeriodFrequencyType,
                depositProductData.minBalanceForInterestCalculation, depositProductData.accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments, periodFrequencyTypeOptions, minDepositAmount, depositAmount, maxDepositAmount);
    }

    public static RecurringDepositProductData lookup(final Long id, final String name) {

        final String shortName = null;
        final CurrencyData currency = null;
        final String description = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestCompoundingPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;

        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final BigDecimal minBalanceForInterestCalculation = null;

        final EnumOptionData accountingType = null;
        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<PaymentTypeData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;
        final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings = null;
        final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = null;
        final Collection<InterestRateChartData> interestRateCharts = null;
        final InterestRateChartData chartTemplate = null;
        final boolean preClosurePenalApplicable = false;
        final BigDecimal preClosurePenalInterest = null;
        final EnumOptionData preClosurePenalInterestOnType = null;
        final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions = null;
        final boolean isMandatoryDeposit = false;
        final boolean allowWithdrawal = false;
        final boolean adjustAdvanceTowardsFuturePayments = false;
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;
        final BigDecimal minDepositAmount = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maxDepositAmount = null;

        return new RecurringDepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, minBalanceForInterestCalculation, accountingType, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, isMandatoryDeposit, allowWithdrawal,
                adjustAdvanceTowardsFuturePayments, periodFrequencyTypeOptions, minDepositAmount, depositAmount, maxDepositAmount);
    }

    public static RecurringDepositProductData withInterestChart(final RecurringDepositProductData product,
            final Collection<InterestRateChartData> interestRateCharts) {
        return new RecurringDepositProductData(product.id, product.name, product.shortName, product.description, product.currency,
                product.nominalAnnualInterestRate, product.interestCompoundingPeriodType, product.interestPostingPeriodType,
                product.interestCalculationType, product.interestCalculationDaysInYearType, product.lockinPeriodFrequency,
                product.lockinPeriodFrequencyType, product.minBalanceForInterestCalculation, product.accountingRule,
                product.accountingMappings, product.paymentChannelToFundSourceMappings, product.currencyOptions,
                product.interestCompoundingPeriodTypeOptions, product.interestPostingPeriodTypeOptions,
                product.interestCalculationTypeOptions, product.interestCalculationDaysInYearTypeOptions,
                product.lockinPeriodFrequencyTypeOptions, product.withdrawalFeeTypeOptions, product.paymentTypeOptions,
                product.accountingRuleOptions, product.accountingMappingOptions, product.charges, product.chargeOptions,
                product.penaltyOptions, product.feeToIncomeAccountMappings, product.penaltyToIncomeAccountMappings, interestRateCharts,
                product.chartTemplate, product.preClosurePenalApplicable, product.preClosurePenalInterest,
                product.preClosurePenalInterestOnType, product.preClosurePenalInterestOnTypeOptions, product.minDepositTerm,
                product.maxDepositTerm, product.minDepositTermType, product.maxDepositTermType, product.inMultiplesOfDepositTerm,
                product.inMultiplesOfDepositTermType, product.isMandatoryDeposit, product.allowWithdrawal,
                product.adjustAdvanceTowardsFuturePayments, product.periodFrequencyTypeOptions, product.minDepositAmount,
                product.depositAmount, product.maxDepositAmount);

    }

    private RecurringDepositProductData(final Long id, final String name, final String shortName, final String description,
            final CurrencyData currency, final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final BigDecimal minBalanceForInterestCalculation,
            final EnumOptionData accountingType, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> charges,
            final Collection<ChargeData> chargeOptions, final Collection<ChargeData> penaltyOptions,
            final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings,
            final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings,
            final Collection<InterestRateChartData> interestRateCharts, final InterestRateChartData chartTemplate,
            final boolean preClosurePenalApplicable, final BigDecimal preClosurePenalInterest,
            final EnumOptionData preClosurePenalInterestOnType, final Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions,
            final Integer minDepositTerm, final Integer maxDepositTerm, final EnumOptionData minDepositTermType,
            final EnumOptionData maxDepositTermType, final Integer inMultiplesOfDepositTerm,
            final EnumOptionData inMultiplesOfDepositTermType, final boolean isMandatoryDeposit, boolean allowWithdrawal,
            boolean adjustAdvanceTowardsFuturePayments, final Collection<EnumOptionData> periodFrequencyTypeOptions,
            final BigDecimal minDepositAmount, final BigDecimal depositAmount, final BigDecimal maxDepositAmount) {

        super(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency,
                lockinPeriodFrequencyType, accountingType, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, minBalanceForInterestCalculation);

        this.preClosurePenalApplicable = preClosurePenalApplicable;
        this.preClosurePenalInterest = preClosurePenalInterest;
        this.preClosurePenalInterestOnType = preClosurePenalInterestOnType;
        this.minDepositTerm = minDepositTerm;
        this.maxDepositTerm = maxDepositTerm;
        this.minDepositTermType = minDepositTermType;
        this.maxDepositTermType = maxDepositTermType;
        this.inMultiplesOfDepositTerm = inMultiplesOfDepositTerm;
        this.inMultiplesOfDepositTermType = inMultiplesOfDepositTermType;
        this.minDepositAmount = minDepositAmount;
        this.depositAmount = depositAmount;
        this.maxDepositAmount = maxDepositAmount;
        this.isMandatoryDeposit = isMandatoryDeposit;
        this.allowWithdrawal = allowWithdrawal;
        this.adjustAdvanceTowardsFuturePayments = adjustAdvanceTowardsFuturePayments;

        // template data
        this.preClosurePenalInterestOnTypeOptions = preClosurePenalInterestOnTypeOptions;
        this.periodFrequencyTypeOptions = periodFrequencyTypeOptions;
    }

}