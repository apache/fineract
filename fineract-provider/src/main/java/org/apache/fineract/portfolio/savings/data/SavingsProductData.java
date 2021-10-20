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
package org.apache.fineract.portfolio.savings.data;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.common.AccountingRuleType;
import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.accounting.producttoaccountmapping.data.ChargeToGLAccountMapper;
import org.apache.fineract.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.paymenttype.data.PaymentTypeData;
import org.apache.fineract.portfolio.tax.data.TaxGroupData;

/**
 * Immutable data object represent a savings product.
 */
public final class SavingsProductData implements Serializable {

    private final Long id;
    private final String name;
    private final String shortName;
    private final String description;
    private final CurrencyData currency;
    private final BigDecimal nominalAnnualInterestRate;
    private final EnumOptionData interestCompoundingPeriodType;
    private final EnumOptionData interestPostingPeriodType;
    private final EnumOptionData interestCalculationType;
    private final EnumOptionData interestCalculationDaysInYearType;
    private final BigDecimal minRequiredOpeningBalance;
    private final Integer lockinPeriodFrequency;
    private final EnumOptionData lockinPeriodFrequencyType;
    private final boolean withdrawalFeeForTransfers;
    private final boolean allowOverdraft;
    private final BigDecimal overdraftLimit;
    private final BigDecimal minRequiredBalance;
    private final boolean enforceMinRequiredBalance;
    private final BigDecimal minBalanceForInterestCalculation;
    private final BigDecimal nominalAnnualInterestRateOverdraft;
    private final BigDecimal minOverdraftForInterestCalculation;
    private final boolean withHoldTax;
    private final TaxGroupData taxGroup;
    private String depositAccountType = null;
    private final String accountMappingForPayment;

    // accounting
    private final EnumOptionData accountingRule;
    private final Map<String, Object> accountingMappings;
    private final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings;
    private final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings;

    // charges
    private final Collection<ChargeData> charges;

    // template
    private final Collection<CurrencyData> currencyOptions;
    private final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions;
    private final Collection<EnumOptionData> interestPostingPeriodTypeOptions;
    private final Collection<EnumOptionData> interestCalculationTypeOptions;
    private final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions;
    private final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    private final Collection<EnumOptionData> withdrawalFeeTypeOptions;
    private final Collection<PaymentTypeData> paymentTypeOptions;
    private final Collection<EnumOptionData> accountingRuleOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    private final Collection<ChargeData> chargeOptions;
    private final Collection<ChargeData> penaltyOptions;
    private final Collection<TaxGroupData> taxGroupOptions;
    private final Boolean isDormancyTrackingActive;
    private final Long daysToInactive;
    private final Long daysToDormancy;
    private final Long daysToEscheat;

    public static SavingsProductData template(final CurrencyData currency, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final EnumOptionData accountingRule,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, final Collection<TaxGroupData> taxGroupOptions,
            final String accountMappingForPayment) {

        final Long id = null;
        final String name = null;
        final String shortName = null;
        final String description = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final boolean withdrawalFeeForTransfers = false;
        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        final Collection<ChargeData> charges = null;
        final Collection<ChargeToGLAccountMapper> feeToIncomeAccountMappings = null;
        final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings = null;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;
        final BigDecimal minRequiredBalance = null;
        final boolean enforceMinRequiredBalance = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final BigDecimal nominalAnnualInterestRateOverdraft = null;
        final BigDecimal minOverdraftForInterestCalculation = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;
        final Boolean isDormancyTrackingActive = false;
        final Long daysToInactive = null;
        final Long daysToDormancy = null;
        final Long daysToEscheat = null;

        return new SavingsProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, accountingRule, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, charges, chargeOptions,
                penaltyOptions, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, nominalAnnualInterestRateOverdraft,
                minOverdraftForInterestCalculation, withHoldTax, taxGroup, taxGroupOptions, isDormancyTrackingActive, daysToInactive,
                daysToDormancy, daysToEscheat, accountMappingForPayment);
    }

    public static SavingsProductData withCharges(final SavingsProductData product, final Collection<ChargeData> charges) {
        return new SavingsProductData(product.id, product.name, product.shortName, product.description, product.currency,
                product.nominalAnnualInterestRate, product.interestCompoundingPeriodType, product.interestPostingPeriodType,
                product.interestCalculationType, product.interestCalculationDaysInYearType, product.minRequiredOpeningBalance,
                product.lockinPeriodFrequency, product.lockinPeriodFrequencyType, product.withdrawalFeeForTransfers, product.accountingRule,
                product.accountingMappings, product.paymentChannelToFundSourceMappings, product.currencyOptions,
                product.interestCompoundingPeriodTypeOptions, product.interestPostingPeriodTypeOptions,
                product.interestCalculationTypeOptions, product.interestCalculationDaysInYearTypeOptions,
                product.lockinPeriodFrequencyTypeOptions, product.withdrawalFeeTypeOptions, product.paymentTypeOptions,
                product.accountingRuleOptions, product.accountingMappingOptions, charges, product.chargeOptions, product.penaltyOptions,
                product.feeToIncomeAccountMappings, product.penaltyToIncomeAccountMappings, product.allowOverdraft, product.overdraftLimit,
                product.minRequiredBalance, product.enforceMinRequiredBalance, product.minBalanceForInterestCalculation,
                product.nominalAnnualInterestRateOverdraft, product.minOverdraftForInterestCalculation, product.withHoldTax,
                product.taxGroup, product.taxGroupOptions, product.isDormancyTrackingActive, product.daysToInactive, product.daysToDormancy,
                product.daysToEscheat, product.accountMappingForPayment);
    }

    /**
     * Returns a {@link SavingsProductData} that contains and exist {@link SavingsProductData} data with further
     * template data for dropdowns.
     *
     * @param taxGroupOptions
     *            TODO
     * @param accountMapping
     */
    public static SavingsProductData withTemplate(final SavingsProductData existingProduct, final Collection<CurrencyData> currencyOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<PaymentTypeData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions,
            final Collection<ChargeData> penaltyOptions, Collection<TaxGroupData> taxGroupOptions, final String accountMappingForPayment) {

        return new SavingsProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.minRequiredOpeningBalance,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType, existingProduct.withdrawalFeeForTransfers,
                existingProduct.accountingRule, existingProduct.accountingMappings, existingProduct.paymentChannelToFundSourceMappings,
                currencyOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions,
                existingProduct.feeToIncomeAccountMappings, existingProduct.penaltyToIncomeAccountMappings, existingProduct.allowOverdraft,
                existingProduct.overdraftLimit, existingProduct.minRequiredBalance, existingProduct.enforceMinRequiredBalance,
                existingProduct.minBalanceForInterestCalculation, existingProduct.nominalAnnualInterestRateOverdraft,
                existingProduct.minOverdraftForInterestCalculation, existingProduct.withHoldTax, existingProduct.taxGroup, taxGroupOptions,
                existingProduct.isDormancyTrackingActive, existingProduct.daysToInactive, existingProduct.daysToDormancy,
                existingProduct.daysToEscheat, existingProduct.accountMappingForPayment);
    }

    public static SavingsProductData withAccountingDetails(final SavingsProductData existingProduct,
            final Map<String, Object> accountingMappings, final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
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
        final String accountMappingForPayment = null;

        return new SavingsProductData(existingProduct.id, existingProduct.name, existingProduct.shortName, existingProduct.description,
                existingProduct.currency, existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.minRequiredOpeningBalance,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType, existingProduct.withdrawalFeeForTransfers,
                existingProduct.accountingRule, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions,
                feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, existingProduct.allowOverdraft, existingProduct.overdraftLimit,
                existingProduct.minRequiredBalance, existingProduct.enforceMinRequiredBalance,
                existingProduct.minBalanceForInterestCalculation, existingProduct.nominalAnnualInterestRateOverdraft,
                existingProduct.minOverdraftForInterestCalculation, existingProduct.withHoldTax, existingProduct.taxGroup,
                existingProduct.taxGroupOptions, existingProduct.isDormancyTrackingActive, existingProduct.daysToInactive,
                existingProduct.daysToDormancy, existingProduct.daysToEscheat, existingProduct.accountMappingForPayment);
    }

    public static SavingsProductData instance(final Long id, final String name, final String shortName, final String description,
            final CurrencyData currency, final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
            final EnumOptionData accountingType, final boolean allowOverdraft, final BigDecimal overdraftLimit,
            final BigDecimal minRequiredBalance, final boolean enforceMinRequiredBalance, final BigDecimal minBalanceForInterestCalculation,
            final BigDecimal nominalAnnualInterestRateOverdraft, final BigDecimal minOverdraftForInterestCalculation,
            final boolean withHoldTax, final TaxGroupData taxGroup, final Boolean isDormancyTrackingActive, final Long daysToInactive,
            final Long daysToDormancy, final Long daysToEscheat) {

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
        final Collection<TaxGroupData> taxGroupOptions = null;
        final String accountMappingForPayment = null;

        return new SavingsProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, accountingType, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, charges, chargeOptions,
                penaltyOptions, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, nominalAnnualInterestRateOverdraft,
                minOverdraftForInterestCalculation, withHoldTax, taxGroup, taxGroupOptions, isDormancyTrackingActive, daysToInactive,
                daysToDormancy, daysToEscheat, accountMappingForPayment);
    }

    public static SavingsProductData lookup(final Long id, final String name) {

        final String shortName = null;
        final CurrencyData currency = null;
        final String description = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final EnumOptionData interestCompoundingPeriodType = null;
        final EnumOptionData interestPostingPeriodType = null;
        final EnumOptionData interestCalculationType = null;
        final EnumOptionData interestCalculationDaysInYearType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final boolean withdrawalFeeForTransfers = false;
        final EnumOptionData accountingType = null;
        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        final boolean allowOverdraft = false;
        final BigDecimal overdraftLimit = null;
        final BigDecimal nominalAnnualInterestRateOverdraft = null;
        final BigDecimal minOverdraftForInterestCalculation = null;
        final BigDecimal minRequiredBalance = null;
        final boolean enforceMinRequiredBalance = false;
        final BigDecimal minBalanceForInterestCalculation = null;
        final boolean withHoldTax = false;
        final TaxGroupData taxGroup = null;

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
        final Collection<TaxGroupData> taxGroupOptions = null;
        final Boolean isDormancyTrackingActive = null;
        final Long daysToInactive = null;
        final Long daysToDormancy = null;
        final Long daysToEscheat = null;
        final String accountMappingForPayment = null;

        return new SavingsProductData(id, name, shortName, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeForTransfers, accountingType, accountingMappings,
                paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions,
                interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions,
                withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions, accountingMappingOptions, charges, chargeOptions,
                penaltyOptions, feeToIncomeAccountMappings, penaltyToIncomeAccountMappings, allowOverdraft, overdraftLimit,
                minRequiredBalance, enforceMinRequiredBalance, minBalanceForInterestCalculation, nominalAnnualInterestRateOverdraft,
                minOverdraftForInterestCalculation, withHoldTax, taxGroup, taxGroupOptions, isDormancyTrackingActive, daysToInactive,
                daysToDormancy, daysToEscheat, accountMappingForPayment);
    }

    private SavingsProductData(final Long id, final String name, final String shortName, final String description,
            final CurrencyData currency, final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType, final boolean withdrawalFeeForTransfers,
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
            final Collection<ChargeToGLAccountMapper> penaltyToIncomeAccountMappings, final boolean allowOverdraft,
            final BigDecimal overdraftLimit, final BigDecimal minRequiredBalance, final boolean enforceMinRequiredBalance,
            final BigDecimal minBalanceForInterestCalculation, final BigDecimal nominalAnnualInterestRateOverdraft,
            final BigDecimal minOverdraftForInterestCalculation, final boolean withHoldTax, final TaxGroupData taxGroup,
            final Collection<TaxGroupData> taxGroupOptions, final Boolean isDormancyTrackingActive, final Long daysToInactive,
            final Long daysToDormancy, final Long daysToEscheat, final String accountMappingForPayment) {
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
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;

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
        this.allowOverdraft = allowOverdraft;
        this.overdraftLimit = overdraftLimit;
        this.minRequiredBalance = minRequiredBalance;
        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        this.minBalanceForInterestCalculation = minBalanceForInterestCalculation;
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        this.minOverdraftForInterestCalculation = minOverdraftForInterestCalculation;
        this.taxGroup = taxGroup;
        this.withHoldTax = withHoldTax;
        this.taxGroupOptions = taxGroupOptions;
        this.isDormancyTrackingActive = isDormancyTrackingActive;
        this.daysToInactive = daysToInactive;
        this.daysToDormancy = daysToDormancy;
        this.daysToEscheat = daysToEscheat;
        this.accountMappingForPayment = accountMappingForPayment;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public int accountingRuleTypeId() {
        return this.accountingRule.getId().intValue();
    }

    @Override
    public boolean equals(final Object obj) {
        if (!(obj instanceof SavingsProductData)) {
            return false;
        }
        final SavingsProductData productData = (SavingsProductData) obj;
        return productData.id.compareTo(this.id) == 0;
    }

    @Override
    public int hashCode() {
        return this.id.hashCode();
    }

    public String getName() {
        return this.name;
    }

    public String getDepositAccountType() {
        return depositAccountType;
    }

    public void setDepositAccountType(String depositAccountType) {
        this.depositAccountType = depositAccountType;
    }

    public BigDecimal getNominalAnnualInterestRate() {
        return nominalAnnualInterestRate;
    }

    public CurrencyData getCurrency() {
        return currency;
    }

    public Integer getLockinPeriodFrequency() {
        return lockinPeriodFrequency;
    }

    public EnumOptionData getLockinPeriodFrequencyType() {
        return lockinPeriodFrequencyType;
    }

    public BigDecimal getOverdraftLimit() {
        return overdraftLimit;
    }

    public BigDecimal getMinRequiredOpeningBalance() {
        return minRequiredOpeningBalance;
    }

    public EnumOptionData getInterestCompoundingPeriodType() {
        return interestCompoundingPeriodType;
    }

    public EnumOptionData getInterestPostingPeriodType() {
        return interestPostingPeriodType;
    }

    public EnumOptionData getInterestCalculationType() {
        return interestCalculationType;
    }

    public EnumOptionData getInterestCalculationDaysInYearType() {
        return interestCalculationDaysInYearType;
    }

    public boolean isAllowOverdraft() {
        return allowOverdraft;
    }

    public BigDecimal getMinRequiredBalance() {
        return minRequiredBalance;
    }

    public Long getId() {
        return id;
    }

    public boolean isWithdrawalFeeForTransfers() {
        return withdrawalFeeForTransfers;
    }
}
