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
 * Immutable data object representing a savings account.
 */
@SuppressWarnings("unused")
public class SavingsAccountData {

    private final Long id;
    private final String accountNo;
    private final String externalId;
    private final Long groupId;
    private final String groupName;
    private final Long clientId;
    private final String clientName;
    private final Long savingsProductId;
    private final String savingsProductName;
    private final CurrencyData currency;
    private final BigDecimal interestRate;
    private final EnumOptionData interestRatePeriodFrequencyType;
    private final BigDecimal annualInterestRate;
    private final BigDecimal minRequiredOpeningBalance;
    private final Integer lockinPeriodFrequency;
    private final EnumOptionData lockinPeriodFrequencyType;

    // template
    private final Collection<SavingsProductData> productOptions;
    private final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions;
    private final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;

    public static SavingsAccountData instance(final Long id, final String accountNo, final String externalId, final Long groupId,
            final String groupName, final Long clientId, final String clientName, final Long productId, final String productName,
            final CurrencyData currency, final BigDecimal interestRate, final EnumOptionData interestRatePeriodFrequencyType,
            final BigDecimal annualInterestRate, final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType) {

        final Collection<SavingsProductData> productOptions = null;
        final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;

        return new SavingsAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                currency, interestRate, interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, productOptions, interestRatePeriodFrequencyTypeOptions,
                lockinPeriodFrequencyTypeOptions);
    }

    public static SavingsAccountData withTemplateOptions(final SavingsAccountData account,
            final Collection<SavingsProductData> productOptions, final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions) {

        return new SavingsAccountData(account.id, account.accountNo, account.externalId, account.groupId, account.groupName,
                account.clientId, account.clientName, account.savingsProductId, account.savingsProductName, account.currency,
                account.interestRate, account.interestRatePeriodFrequencyType, account.annualInterestRate,
                account.minRequiredOpeningBalance, account.lockinPeriodFrequency, account.lockinPeriodFrequencyType, productOptions,
                interestRatePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions);
    }

    public static SavingsAccountData withClientTemplate(final Long clientId, final String clientName, final Long groupId,
            final String groupName) {

        final Long id = null;
        final String accountNo = null;
        final String externalId = null;
        final Long productId = null;
        final String productName = null;
        final CurrencyData currency = null;
        final BigDecimal interestRate = null;
        final EnumOptionData interestRatePeriodFrequencyType = null;
        final BigDecimal annualInterestRate = null;
        final BigDecimal minRequiredOpeningBalance = null;
        final Integer lockinPeriodFrequency = null;
        final EnumOptionData lockinPeriodFrequencyType = null;
        final Collection<SavingsProductData> productOptions = null;
        final Collection<CurrencyData> currencyOptions = null;
        final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;

        return new SavingsAccountData(id, accountNo, externalId, groupId, groupName, clientId, clientName, productId, productName,
                currency, interestRate, interestRatePeriodFrequencyType, annualInterestRate, minRequiredOpeningBalance,
                lockinPeriodFrequency, lockinPeriodFrequencyType, productOptions, interestRatePeriodFrequencyTypeOptions,
                lockinPeriodFrequencyTypeOptions);
    }

    private SavingsAccountData(final Long id, final String accountNo, final String externalId, final Long groupId, final String groupName,
            final Long clientId, final String clientName, final Long productId, final String productName, final CurrencyData currency,
            final BigDecimal interestRate, final EnumOptionData interestRatePeriodFrequencyType, final BigDecimal annualInterestRate,
            final BigDecimal minRequiredOpeningBalance, final Integer lockinPeriodFrequency,
            final EnumOptionData lockinPeriodFrequencyType, final Collection<SavingsProductData> productOptions,
            final Collection<EnumOptionData> interestRatePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.groupId = groupId;
        this.groupName = groupName;
        this.clientId = clientId;
        this.clientName = clientName;
        this.savingsProductId = productId;
        this.savingsProductName = productName;
        this.currency = currency;
        this.interestRate = interestRate;
        this.interestRatePeriodFrequencyType = interestRatePeriodFrequencyType;
        this.annualInterestRate = annualInterestRate;
        this.minRequiredOpeningBalance = minRequiredOpeningBalance;
        this.lockinPeriodFrequency = lockinPeriodFrequency;
        this.lockinPeriodFrequencyType = lockinPeriodFrequencyType;

        this.productOptions = productOptions;
        this.interestRatePeriodFrequencyTypeOptions = interestRatePeriodFrequencyTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
    }
}