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

import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;
import org.mifosplatform.portfolio.interestratechart.data.InterestRateChartData;
import org.mifosplatform.portfolio.paymenttype.data.PaymentTypeData;

/**
 * Immutable data object represents a partial data of Deposit Proucts.
 */
public class DepositProductData {

    protected final Long id;
    protected final String name;
    protected final String shortName;
    protected final String description;
    protected final CurrencyData currency;
    protected final BigDecimal nominalAnnualInterestRate;
    protected final EnumOptionData interestCompoundingPeriodType;
    protected final EnumOptionData interestPostingPeriodType;
    protected final EnumOptionData interestCalculationType;
    protected final EnumOptionData interestCalculationDaysInYearType;
    // protected final BigDecimal minRequiredOpeningBalance;
    protected final Integer lockinPeriodFrequency;
    protected final EnumOptionData lockinPeriodFrequencyType;
    // protected final boolean withdrawalFeeForTransfers;
    protected final BigDecimal minBalanceForInterestCalculation;

    // accounting
    protected final EnumOptionData accountingRule;
    protected final Map<String, Object> accountingMappings;
    protected final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    protected final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    protected final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

    // charges
    protected final Collection<ChargeData> charges;

    // interest rate charts
    protected final Collection<InterestRateChartData> interestRateCharts;
    protected final InterestRateChartData activeChart;

    // template
    protected final Collection<CurrencyData> currencyOptions;
    protected final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions;
    protected final Collection<EnumOptionData> interestPostingPeriodTypeOptions;
    protected final Collection<EnumOptionData> interestCalculationTypeOptions;
    protected final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions;
    protected final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    protected final Collection<EnumOptionData> withdrawalFeeTypeOptions;
    protected final Collection<PaymentTypeData> paymentTypeOptions;
    protected final Collection<EnumOptionData> accountingRuleOptions;
    protected final Map<String, List<GLAccountData>> accountingMappingOptions;
    protected final Collection<ChargeData> chargeOptions;
    protected final Collection<ChargeData> penaltyOptions;
    protected final InterestRateChartData chartTemplate;

    public static DepositProductData template(final CurrencyData currency, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final EnumOptionData accountingRule,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final InterestRateChartData chartTemplate) {

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

        return new DepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency,
                lockinPeriodFrequencyType, accountingRule, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, minBalanceForInterestCalculation);
    }

    public static DepositProductData withCharges(final DepositProductData existingProduct, final Collection<ChargeData> charges) {
        return new DepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.lockinPeriodFrequency,
                existingProduct.lockinPeriodFrequencyType, existingProduct.accountingRule, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, existingProduct.currencyOptions,
                existingProduct.interestCompoundingPeriodTypeOptions, existingProduct.interestPostingPeriodTypeOptions,
                existingProduct.interestCalculationTypeOptions, existingProduct.interestCalculationDaysInYearTypeOptions,
                existingProduct.lockinPeriodFrequencyTypeOptions, existingProduct.withdrawalFeeTypeOptions,
                existingProduct.paymentTypeOptions, existingProduct.accountingRuleOptions, existingProduct.accountingMappingOptions,
                charges, existingProduct.chargeOptions, existingProduct.penaltyOptions, existingProduct.feeToIncomeAccountMappings,
                existingProduct.penaltyToIncomeAccountMappings, existingProduct.interestRateCharts, existingProduct.chartTemplate,
                existingProduct.minBalanceForInterestCalculation);
    }

    /**
     * Returns a {@link DepositProductData} that contains and exist
     * {@link DepositProductData} data with further template data for dropdowns.
     */
    public static DepositProductData withTemplate(final DepositProductData existingProduct, final Collection<CurrencyData> currencyOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final InterestRateChartData chartTemplate) {

        return new DepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.lockinPeriodFrequency,
                existingProduct.lockinPeriodFrequencyType, existingProduct.accountingRule, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions,
                existingProduct.feeToIncomeAccountMappings, existingProduct.penaltyToIncomeAccountMappings,
                existingProduct.interestRateCharts, chartTemplate, existingProduct.minBalanceForInterestCalculation);
    }

    public static DepositProductData withAccountingDetails(final DepositProductData existingProduct,
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

        return new DepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.lockinPeriodFrequency,
                existingProduct.lockinPeriodFrequencyType, existingProduct.accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, existingProduct.interestRateCharts, existingProduct.chartTemplate,
                existingProduct.minBalanceForInterestCalculation);
    }

    public static DepositProductData instance(final Long id, final String name, final String shortName, final String description,
            final CurrencyData currency, final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final EnumOptionData accountingType,
            final BigDecimal minBalanceForInterestCalculation) {

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

        return new DepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency,
                lockinPeriodFrequencyType, accountingType, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, minBalanceForInterestCalculation);
    }

    public static DepositProductData lookup(final Long id, final String name) {

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

        return new DepositProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, lockinPeriodFrequency,
                lockinPeriodFrequencyType, accountingType, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions, feeToIncomeAccountMappings,
                penaltyToIncomeAccountMappings, interestRateCharts, chartTemplate, minBalanceForInterestCalculation);
    }

    public static DepositProductData withInterestChart(final DepositProductData existingProduct,
            final Collection<InterestRateChartData> interestRateCharts) {
        return new DepositProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.lockinPeriodFrequency,
                existingProduct.lockinPeriodFrequencyType, existingProduct.accountingRule, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, existingProduct.currencyOptions,
                existingProduct.interestCompoundingPeriodTypeOptions, existingProduct.interestPostingPeriodTypeOptions,
                existingProduct.interestCalculationTypeOptions, existingProduct.interestCalculationDaysInYearTypeOptions,
                existingProduct.lockinPeriodFrequencyTypeOptions, existingProduct.withdrawalFeeTypeOptions,
                existingProduct.paymentTypeOptions, existingProduct.accountingRuleOptions, existingProduct.accountingMappingOptions,
                existingProduct.charges, existingProduct.chargeOptions, existingProduct.penaltyOptions,
                existingProduct.feeToIncomeAccountMappings, existingProduct.penaltyToIncomeAccountMappings, interestRateCharts,
                existingProduct.chartTemplate, existingProduct.minBalanceForInterestCalculation);
    }

    protected DepositProductData(final Long id, final String name, final String shortName, final String description,
            final CurrencyData currency, final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final EnumOptionData accountingType,
            final Map<String, Object> accountingMappings,
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
            final BigDecimal minBalanceForInterestCalculation) {

        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.currency = currency;
        this.nominalAnnualInterestRate = nominalAnnualInterestRate;
        this.interestCompoundingPeriodType = interestCompoundingPeriodType;
        this.interestPostingPeriodType = interestPostingPeriodType;
        this.interestCalculationType = interestCalculationType;
        this.interestCalculationDaysInYearType = interestCalculationDaysInYearType;
        this.accountingRule = accountingType;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;

        this.currencyOptions = currencyOptions;
        this.interestCompoundingPeriodTypeOptions = interestCompoundingPeriodTypeOptions;
        this.interestPostingPeriodTypeOptions = interestPostingPeriodTypeOptions;
        this.interestCalculationTypeOptions = interestCalculationTypeOptions;
        this.interestCalculationDaysInYearTypeOptions = interestCalculationDaysInYearTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.withdrawalFeeTypeOptions = withdrawalFeeTypeOptions;

        this.paymentTypeOptions = paymentTypeOptions;
        this.accountingMappingOptions = accountingMappingOptions;
        this.accountingRuleOptions = accountingRuleOptions;
        if (accountingMappings == null || accountingMappings.isEmpty()) {
            this.accountingMappings = null;
        } else {
            this.accountingMappings = accountingMappings;
        }
        this.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;

        this.charges = charges;// charges associated with Savings product
        this.chargeOptions = chargeOptions;// charges available for adding to
                                           // Savings product
        this.penaltyOptions = penaltyOptions;// penalties available for adding
                                             // to Savings product

        this.feeToIncomeAccountMappings = feeToIncomeAccountMappings;
        this.penaltyToIncomeAccountMappings = penaltyToIncomeAccountMappings;
        this.interestRateCharts = interestRateCharts;
        this.activeChart = activeChart(this.interestRateCharts);
        this.chartTemplate = chartTemplate;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
    }

    public static InterestRateChartData activeChart(Collection<InterestRateChartData> interestRateCharts) {
        InterestRateChartData activeChart = null;
        if (interestRateCharts != null) {
            for (InterestRateChartData chartData : interestRateCharts) {
                if (activeChart == null) {
                    activeChart = chartData;
                } else {
                    if (!activeChart.isFromDateAfter(chartData.endDate())) {
                        activeChart = chartData;
                    }
                }
            }
        }
        return activeChart;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public int accountingRuleTypeId() {
        return this.accountingRule.getId().intValue();
    }

    public String getName() {
        return this.name;
    }
}