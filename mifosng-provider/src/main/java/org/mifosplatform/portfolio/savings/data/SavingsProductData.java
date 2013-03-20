/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.portfolio.savings.data;

import java.math.BigDecimal;
import java.util.Collection;

import org.mifosplatform.infrastructure.core.data.EnumOptionData;
import org.mifosplatform.organisation.monetary.data.CurrencyData;

/**
 * Immutable data object represent a savings product.
 */
@SuppressWarnings("unused")
public class SavingsProductData {

    private final Long id;
    private final String name;
    private final String description;
    private final CurrencyData currency;
    private final BigDecimal interestRate;
    private final EnumOptionData interestRatePeriodFrequencyType;
    private final BigDecimal minRequiredOpeningBalance;
    private final Integer lockinPeriodFrequency;
    private final EnumOptionData lockinPeriodFrequencyType;

    // template
    private final Collection<CurrencyData> currencyOptions;
    private final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions;
    private final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;

    public static SavingsProductData template(final CurrencyData currency, final EnumOptionData interestRatePeriodFrequencyType,
            final Collection<CurrencyData> currencyOptions, final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions) {

        final Long id = null;
        final String name = null;
        final String description = null;
        final Integer digitsAfterDecimal = null;
        final BigDecimal interestRate = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;

        return new SavingsProductData(id, name, description, currency, interestRate, interestRatePeriodFrequencyType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, currencyOptions,
                interestRatePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions);
    }

    /**
     * Returns a {@link SavingsProductData} that contains and exist
     * {@link SavingsProductData} data with further template data for dropdowns.
     */
    public static SavingsProductData withTemplate(final SavingsProductData existingProduct, final Collection<CurrencyData> currencyOptions,
            final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions) {

        return new SavingsProductData(existingProduct.id, existingProduct.name, existingProduct.description, existingProduct.currency,
                existingProduct.interestRate, existingProduct.interestRatePeriodFrequencyType, existingProduct.minRequiredOpeningBalance,
                existingProduct.lockinPeriodFrequency, existingProduct.lockinPeriodFrequencyType, currencyOptions,
                interestRatePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions);
    }

    public static SavingsProductData instance(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal interestRate, final EnumOptionData interestRatePeriodFrequencyType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency, final EnumOptionData lockinPeriodFrequencyType) {

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;

        return new SavingsProductData(id, name, description, currency, interestRate, interestRatePeriodFrequencyType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, currencyOptions,
                interestRatePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions);
    }

    public static SavingsProductData lookup(final Long id, final String name) {

        final CurrencyData currency = null;
        final String description = null;
        final BigDecimal interestRate = null;
        final EnumOptionData interestRatePeriodFrequencyType = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;

        return new SavingsProductData(id, name, description, currency, interestRate, interestRatePeriodFrequencyType,
                minRequiredOpeningBalance, lockinPeriodFrequency, lockinPeriodFrequencyType, currencyOptions,
                interestRatePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions);
    }

    private SavingsProductData(final Long id, final String name, final String description, final CurrencyData currency,
            final BigDecimal interestRate, final EnumOptionData interestRatePeriodFrequencyType,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final Collection<CurrencyData> currencyOptions,
            final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions) {
        this.id = id;
        this.name = name;
        this.description = description;
        this.currency = currency;
        this.interestRate = interestRate;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;

        this.currencyOptions = currencyOptions;
        this.interestRatePeriodFrequencyTypeOptions = interestRatePeriodFrequencyTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
    }
}