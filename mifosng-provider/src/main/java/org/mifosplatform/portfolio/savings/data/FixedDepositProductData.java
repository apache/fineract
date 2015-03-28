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
 * Immutable data object representing a Fixed Deposit product.
 */
public class FixedDepositProductData extends DepositProductData {

    // additional fields
    private boolean preClosurePenalApplicable;
    protected BigDecimal preClosurePenalInterest;
    protected EnumOptionData preClosurePenalInterestOnType;
    protected Integer minDepositTerm;
    protected Integer maxDepositTerm;
    private EnumOptionData minDepositTermType;
    private EnumOptionData maxDepositTermType;
    protected Integer inMultiplesOfDepositTerm;
    protected EnumOptionData inMultiplesOfDepositTermType;
    protected BigDecimal minDepositAmount;
    protected BigDecimal depositAmount;
    protected BigDecimal maxDepositAmount;

    private Collection<EnumOptionData> preClosurePenalInterestOnTypeOptions;
    private Collection<EnumOptionData> periodFrequencyTypeOptions;

    public static FixedDepositProductData template(final CurrencyData currency, final EnumOptionData interestCompoundingPeriodType,
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
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal minDepositAmount = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maxDepositAmount = null;

        return new FixedDepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, minBalanceForInterestCalculation, accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, minDepositAmount, depositAmount,
                maxDepositAmount, periodFrequencyTypeOptions);
    }

    public static FixedDepositProductData withCharges(final FixedDepositProductData existingProduct, final Collection<ChargeData> charges) {
        return new FixedDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
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
                existingProduct.minDepositAmount, existingProduct.depositAmount, existingProduct.maxDepositAmount,
                existingProduct.periodFrequencyTypeOptions);
    }

    /**
     * Returns a {@link FixedDepositProductData} that contains and exist
     * {@link FixedDepositProductData} data with further template data for
     * dropdowns.
     */
    public static FixedDepositProductData withTemplate(final FixedDepositProductData existingProduct,
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

        return new FixedDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
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
                existingProduct.inMultiplesOfDepositTermType, existingProduct.minDepositAmount, existingProduct.depositAmount,
                existingProduct.maxDepositAmount, periodFrequencyTypeOptions);
    }

    public static FixedDepositProductData withAccountingDetails(final FixedDepositProductData existingProduct,
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

        return new FixedDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
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
                existingProduct.minDepositAmount, existingProduct.depositAmount, existingProduct.maxDepositAmount,
                existingProduct.periodFrequencyTypeOptions);
    }

    public static FixedDepositProductData instance(final DepositProductData depositProductData, final boolean preClosurePenalApplicable,
            final BigDecimal preClosurePenalInterest, final EnumOptionData preClosurePenalInterestOnType, final Integer minDepositTerm,
            final Integer maxDepositTerm, final EnumOptionData minDepositTermType, final EnumOptionData maxDepositTermType,
            final Integer inMultiplesOfDepositTerm, final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal minDepositAmount,
            final BigDecimal depositAmount, final BigDecimal maxDepositAmount) {

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

        return new FixedDepositProductData(depositProductData.id, depositProductData.name, depositProductData.shortName,
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
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, minDepositAmount, depositAmount,
                maxDepositAmount, periodFrequencyTypeOptions);
    }

    public static FixedDepositProductData lookup(final Long id, final String name) {

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
        final Integer minDepositTerm = null;
        final Integer maxDepositTerm = null;
        final EnumOptionData minDepositTermType = null;
        final EnumOptionData maxDepositTermType = null;
        final Integer inMultiplesOfDepositTerm = null;
        final EnumOptionData inMultiplesOfDepositTermType = null;
        final BigDecimal minDepositAmount = null;
        final BigDecimal depositAmount = null;
        final BigDecimal maxDepositAmount = null;
        final Collection<EnumOptionData> periodFrequencyTypeOptions = null;

        return new FixedDepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate,
                interestCompoundingPeriodType, interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType,
                lockinPeriodFrequency, lockinPeriodFrequencyType, minBalanceForInterestCalculation, accountingType, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, preClosurePenalApplicable, preClosurePenalInterest,
                preClosurePenalInterestOnType, preClosurePenalInterestOnTypeOptions, minDepositTerm, maxDepositTerm, minDepositTermType,
                maxDepositTermType, inMultiplesOfDepositTerm, inMultiplesOfDepositTermType, minDepositAmount, depositAmount,
                maxDepositAmount, periodFrequencyTypeOptions);
    }

    public static FixedDepositProductData withInterestChart(final FixedDepositProductData existingProduct,
            final Collection<InterestRateChartData> interestRateCharts) {
        return new FixedDepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName,
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
                existingProduct.charges, existingProduct.chargeOptions, existingProduct.penaltyOptions,
                existingProduct.feeToIncomeAccountMappings, existingProduct.penaltyToIncomeAccountMappings, interestRateCharts,
                existingProduct.chartTemplate, existingProduct.preClosurePenalApplicable, existingProduct.preClosurePenalInterest,
                existingProduct.preClosurePenalInterestOnType, existingProduct.preClosurePenalInterestOnTypeOptions,
                existingProduct.minDepositTerm, existingProduct.maxDepositTerm, existingProduct.minDepositTermType,
                existingProduct.maxDepositTermType, existingProduct.inMultiplesOfDepositTerm, existingProduct.inMultiplesOfDepositTermType,
                existingProduct.minDepositAmount, existingProduct.depositAmount, existingProduct.maxDepositAmount,
                existingProduct.periodFrequencyTypeOptions);

    }

    private FixedDepositProductData(final Long id, final String name, final String shortName, final String description,
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
            final EnumOptionData inMultiplesOfDepositTermType, final BigDecimal minDepositAmount, final BigDecimal depositAmount,
            final BigDecimal maxDepositAmount, final Collection<EnumOptionData> periodFrequencyTypeOptions) {

        super(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency,
                lockinPeriodFrequencyType, accountingType, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, minBalanceForInterestCalculation);

        // fixed deposit additional fields
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

        // template
        this.preClosurePenalInterestOnTypeOptions = preClosurePenalInterestOnTypeOptions;
        this.periodFrequencyTypeOptions = periodFrequencyTypeOptions;
    }

}