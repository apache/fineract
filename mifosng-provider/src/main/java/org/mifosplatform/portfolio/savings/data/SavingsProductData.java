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

import org.joda.time.MonthDay;
import org.mifosplatform.accounting.common.AccountingRuleType;
import org.mifosplatform.accounting.glaccount.data.GLAccountData;
import org.mifosplatform.accounting.producttoaccountmapping.data.PaymentTypeToGLAccountMapper;
import org.mifosplatform.infrastructure.codes.data.CodeValueData;
import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;
import org.mifosplatform.portfolio.charge.data.ChargeData;

/**
 * Immutable data object represent a savings product.
 */
public class SavingsProductData {

    private final Long id;
    private final String name;
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
    private final BigDecimal withdrawalFeeAmount;
    private final EnumOptionData withdrawalFeeType;
    private final boolean withdrawalFeeForTransfers;
    private final BigDecimal annualFeeAmount;
    private final MonthDay annualFeeOnMonthDay;

    // accounting
    private final EnumOptionData accountingRule;
    private final Map<String, Object> accountingMappings;
    private final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;

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
    private final Collection<CodeValueData> paymentTypeOptions;
    private final Collection<EnumOptionData> accountingRuleOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;
    private final Collection<ChargeData> chargeOptions;
    private final Collection<ChargeData> penaltyOptions;
    
    public static SavingsProductData template(final CurrencyData currency, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final EnumOptionData accountingRule,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<CodeValueData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions, final Collection<ChargeData> penaltyOptions) {

        final Long id = null;
        final String name = null;
        final String description = null;
        final BigDecimal nominalAnnualInterestRate = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final BigDecimal withdrawalFeeAmount = null;
        final EnumOptionData withdrawalFeeType = null;
        final boolean withdrawalFeeForTransfers = false;
        final BigDecimal annualFeeAmount = null;
        final MonthDay annualFeeMonthDay = null;
        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        final Collection<ChargeData> charges = null;

        return new SavingsProductData(id, name, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType,withdrawalFeeForTransfers,
                accountingRule, annualFeeAmount, annualFeeMonthDay, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions);
    }

    public static SavingsProductData withCharges(final SavingsProductData product, final Collection<ChargeData> charges) {
        return new SavingsProductData(product.id, product.name, product.description, product.currency, product.nominalAnnualInterestRate,
                product.interestCompoundingPeriodType, product.interestPostingPeriodType, product.interestCalculationType,
                product.interestCalculationDaysInYearType, product.minRequiredOpeningBalance, product.lockinPeriodFrequency,
                product.lockinPeriodFrequencyType, product.withdrawalFeeAmount, product.withdrawalFeeType,
                product.withdrawalFeeForTransfers, product.accountingRule, product.annualFeeAmount, product.annualFeeOnMonthDay,
                product.accountingMappings, product.paymentChannelToFundSourceMappings, product.currencyOptions,
                product.interestCompoundingPeriodTypeOptions, product.interestPostingPeriodTypeOptions,
                product.interestCalculationTypeOptions, product.interestCalculationDaysInYearTypeOptions,
                product.lockinPeriodFrequencyTypeOptions, product.withdrawalFeeTypeOptions, product.paymentTypeOptions,
                product.accountingRuleOptions, product.accountingMappingOptions, charges, product.chargeOptions, product.penaltyOptions);
    }
    
    /**
     * Returns a {@link SavingsProductData} that contains and exist
     * {@link SavingsProductData} data with further template data for dropdowns.
     */
    public static SavingsProductData withTemplate(final SavingsProductData existingProduct, final Collection<CurrencyData> currencyOptions,
            final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<CodeValueData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> chargeOptions, final Collection<ChargeData> penaltyOptions) {

        return new SavingsProductData(existingProduct.id, existingProduct.name, existingProduct.description, existingProduct.currency,
                existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.minRequiredOpeningBalance,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType, existingProduct.withdrawalFeeAmount,
                existingProduct.withdrawalFeeType,existingProduct.withdrawalFeeForTransfers, existingProduct.accountingRule, existingProduct.annualFeeAmount,
                existingProduct.annualFeeOnMonthDay, existingProduct.accountingMappings,
                existingProduct.paymentChannelToFundSourceMappings, currencyOptions, interestCompoundingPeriodTypeOptions,
                interestPostingPeriodTypeOptions, interestCalculationTypeOptions, interestCalculationDaysInYearTypeOptions,
                lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions, accountingRuleOptions,
                accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions);
    }

    public static SavingsProductData withAccountingDetails(final SavingsProductData existingProduct,
            final Map<String, Object> accountingMappings, final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings) {

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;

        return new SavingsProductData(existingProduct.id, existingProduct.name, existingProduct.description, existingProduct.currency,
                existingProduct.nominalAnnualInterestRate, existingProduct.interestCompoundingPeriodType,
                existingProduct.interestPostingPeriodType, existingProduct.interestCalculationType,
                existingProduct.interestCalculationDaysInYearType, existingProduct.minRequiredOpeningBalance,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType, existingProduct.withdrawalFeeAmount,
                existingProduct.withdrawalFeeType,existingProduct.withdrawalFeeForTransfers, existingProduct.accountingRule, existingProduct.annualFeeAmount,
                existingProduct.annualFeeOnMonthDay, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, existingProduct.charges, chargeOptions, penaltyOptions);
    }

    public static SavingsProductData instance(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType, final BigDecimal withdrawalFeeAmount,
            final EnumOptionData withdrawalFeeType, boolean withdrawalFeeForTransfers, final BigDecimal annualFeeAmount,
            final MonthDay annualFeeOnMonthDay, final EnumOptionData accountingType) {

        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;
        final Collection<ChargeData> charges = null;

        return new SavingsProductData(id, name, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType,withdrawalFeeForTransfers, accountingType,
                annualFeeAmount, annualFeeOnMonthDay, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions);
    }

    public static SavingsProductData lookup(final Long id, final String name) {

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
        final BigDecimal withdrawalFeeAmount = null;
        final EnumOptionData withdrawalFeeType = null;
        final boolean withdrawalFeeForTransfers = false;
        final EnumOptionData accountingType = null;
        final BigDecimal annualFeeAmount = null;
        final MonthDay annualFeeMonthDay = null;
        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestPostingPeriodTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationTypeOptions = null;
        final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> withdrawalFeeTypeOptions = null;
        final Collection<CodeValueData> paymentTypeOptions = null;
        final Collection<EnumOptionData> accountingRuleOptions = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeData> charges = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<ChargeData> penaltyOptions = null;

        return new SavingsProductData(id, name, description, currency, nominalAnnualInterestRate, interestCompoundingPeriodType,
                interestPostingPeriodType, interestCalculationType, interestCalculationDaysInYearType, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, withdrawalFeeAmount, withdrawalFeeType,withdrawalFeeForTransfers, accountingType, 
                annualFeeAmount, annualFeeMonthDay, accountingMappings, paymentChannelToFundSourceMappings, currencyOptions,
                interestCompoundingPeriodTypeOptions, interestPostingPeriodTypeOptions, interestCalculationTypeOptions,
                interestCalculationDaysInYearTypeOptions, lockinPeriodFrequencyTypeOptions, withdrawalFeeTypeOptions, paymentTypeOptions,
                accountingRuleOptions, accountingMappingOptions, charges, chargeOptions, penaltyOptions);
    }

    private SavingsProductData(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal nominalAnnualInterestRate, final EnumOptionData interestCompoundingPeriodType,
            final EnumOptionData interestPostingPeriodType, final EnumOptionData interestCalculationType,
            final EnumOptionData interestCalculationDaysInYearType, final BigDecimal minRequiredOpeningBalance,
            final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType, final BigDecimal withdrawalFeeAmount,
            final EnumOptionData withdrawalFeeType,final boolean withdrawalFeeForTransfers, final EnumOptionData accountingType, 
            final BigDecimal annualFeeAmount, final MonthDay annualFeeOnMonthDay, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestCompoundingPeriodTypeOptions,
            final Collection<EnumOptionData> interestPostingPeriodTypeOptions,
            final Collection<EnumOptionData> interestCalculationTypeOptions,
            final Collection<EnumOptionData> interestCalculationDaysInYearTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<EnumOptionData> withdrawalFeeTypeOptions,
            final Collection<CodeValueData> paymentTypeOptions, final Collection<EnumOptionData> accountingRuleOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions, final Collection<ChargeData> charges, final Collection<ChargeData> chargeOptions, final Collection<ChargeData> penaltyOptions) {
        this.id = id;
        this.name = name;
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
        this.withdrawalFeeAmount = withdrawalFeeAmount;
        this.withdrawalFeeType = withdrawalFeeType;
        this.withdrawalFeeForTransfers = withdrawalFeeForTransfers;
        this.annualFeeAmount = annualFeeAmount;
        this.annualFeeOnMonthDay = annualFeeOnMonthDay;

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
        
        this.charges = charges;//charges associated with Savings product
        this.chargeOptions = chargeOptions;//charges available for adding to Savings product
        this.penaltyOptions = penaltyOptions;//penalties available for adding to Savings product
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public int accountingRuleTypeId() {
        return this.accountingRule.getId().intValue();
    }
}