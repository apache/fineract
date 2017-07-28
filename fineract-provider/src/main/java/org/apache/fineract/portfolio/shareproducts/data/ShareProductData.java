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
package org.apache.fineract.portfolio.shareproducts.data;

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
import org.apache.fineract.portfolio.products.data.ProductData;

public class ShareProductData implements ProductData {

    private final Long id;
    private final String name;
    private final String shortName;
    private final String description;
    private final String externalId;
    private final CurrencyData currency;
    private final Long totalShares;
    private final Long totalSharesIssued;
    private final BigDecimal unitPrice;
    private final BigDecimal shareCapital;
    private final Long minimumShares;
    private final Long nominalShares;
    private final Long maximumShares;
    private final Collection<ShareProductMarketPriceData> marketPrice;
    private final Collection<ChargeData> charges;
    private final Boolean allowDividendCalculationForInactiveClients;

    private final Integer lockinPeriod;
    private final EnumOptionData lockPeriodTypeEnum;

    private final Integer minimumActivePeriod;
    private final EnumOptionData minimumActivePeriodForDividendsTypeEnum;

    // accounting
    private final EnumOptionData accountingRule;
    private final Map<String, Object> accountingMappings;
    private final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings;
    private final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings;

    private final Collection<CurrencyData> currencyOptions;
    private final Collection<ChargeData> chargeOptions;
    final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions;
    final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    private final Map<String, List<GLAccountData>> accountingMappingOptions;

    private ShareProductData(final Long id, final String name, final String shortName, final String description, final String externalId,
            final CurrencyData currency, final Long totalShares, final Long totalSharesIssued, final BigDecimal unitPrice,
            final BigDecimal shareCapital, final Long minimumShares, final Long nominaltShares, final Long maximumShares,
            Collection<ShareProductMarketPriceData> marketPrice, final Collection<ChargeData> charges,
            final Boolean allowDividendCalculationForInactiveClients, final Integer lockinPeriod, final EnumOptionData lockPeriodEnum,
            final Integer minimumActivePeriod, final EnumOptionData minimumActivePeriodForDividendsTypeEnum, EnumOptionData accountingRule,
            Map<String, Object> accountingMappings, Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            Collection<ChargeToGLAccountMapper> feeToGLAccountMappings, final Collection<CurrencyData> currencyOptions,
            final Collection<ChargeData> chargeOptions, final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, Map<String, List<GLAccountData>> accountingMappingOptions) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.description = description;
        this.externalId = externalId;
        this.currency = currency;
        this.totalShares = totalShares;
        this.totalSharesIssued = totalSharesIssued;
        this.unitPrice = unitPrice;
        this.shareCapital = shareCapital;
        this.minimumShares = minimumShares;
        this.nominalShares = nominaltShares;
        this.maximumShares = maximumShares;
        this.marketPrice = marketPrice;
        this.charges = charges;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.lockinPeriod = lockinPeriod;
        this.lockPeriodTypeEnum = lockPeriodEnum;
        this.minimumActivePeriod = minimumActivePeriod;
        this.minimumActivePeriodForDividendsTypeEnum = minimumActivePeriodForDividendsTypeEnum;
        this.accountingMappings = accountingMappings;
        this.accountingRule = accountingRule;
        this.paymentChannelToFundSourceMappings = paymentChannelToFundSourceMappings;
        this.feeToGLAccountMappings = feeToGLAccountMappings;
        this.currencyOptions = currencyOptions;
        this.chargeOptions = chargeOptions;
        this.minimumActivePeriodFrequencyTypeOptions = minimumActivePeriodFrequencyTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.accountingMappingOptions = accountingMappingOptions;
    }

    public static ShareProductData data(final Long id, final String name, final String shortName, final String description,
            final String externalId, final CurrencyData currency, final Long totalShares, final Long totalSharesIssued,
            final BigDecimal unitPrice, final BigDecimal shareCapital, final Long minimumShares, final Long nominaltShares,
            final Long maximumShares, Collection<ShareProductMarketPriceData> marketPrice, final Collection<ChargeData> charges,

            final Boolean allowDividendCalculationForInactiveClients, final Integer lockinPeriod, final EnumOptionData lockPeriodTypeEnum,
            final Integer minimumActivePeriod, final EnumOptionData minimumActivePeriodForDividendsEnum, EnumOptionData accountingRule) {

        final Map<String, Object> accountingMappings = null;
        final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings = null;
        final Map<String, List<GLAccountData>> accountingMappingOptions = null;
        final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings = null;

        final Collection<CurrencyData> currencyOptions = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        return new ShareProductData(id, name, shortName, description, externalId, currency, totalShares, totalSharesIssued, unitPrice,
                shareCapital, minimumShares, nominaltShares, maximumShares, marketPrice, charges,
                allowDividendCalculationForInactiveClients, lockinPeriod, lockPeriodTypeEnum, minimumActivePeriod,
                minimumActivePeriodForDividendsEnum, accountingRule, accountingMappings, paymentChannelToFundSourceMappings,
                feeToGLAccountMappings, currencyOptions, chargeOptions, minimumActivePeriodFrequencyTypeOptions,
                lockinPeriodFrequencyTypeOptions, accountingMappingOptions);

    }

    private ShareProductData(final Collection<CurrencyData> currencyOptions, final Collection<ChargeData> chargeOptions,
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions) {
        this.id = null;
        this.name = null;
        this.shortName = null;
        this.description = null;
        this.externalId = null;
        this.currency = null;
        this.totalShares = null;
        this.totalSharesIssued = null;
        this.unitPrice = null;
        this.shareCapital = null;
        this.minimumShares = null;
        this.nominalShares = null;
        this.maximumShares = null;
        this.marketPrice = null;
        this.charges = null;
        this.allowDividendCalculationForInactiveClients = null;
        this.lockPeriodTypeEnum = null;
        this.minimumActivePeriodForDividendsTypeEnum = null;
        this.accountingRule = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
        this.feeToGLAccountMappings = null;
        this.currencyOptions = currencyOptions;
        this.chargeOptions = chargeOptions;
        this.minimumActivePeriodFrequencyTypeOptions = minimumActivePeriodFrequencyTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.lockinPeriod = null;
        this.minimumActivePeriod = null;
        this.accountingMappingOptions = accountingMappingOptions;
    }

    public static ShareProductData template(final Collection<CurrencyData> currencyOptions, final Collection<ChargeData> chargeOptions,
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, Map<String, List<GLAccountData>> accountingMappingOptions) {
        return new ShareProductData(currencyOptions, chargeOptions, minimumActivePeriodFrequencyTypeOptions,
                lockinPeriodFrequencyTypeOptions, accountingMappingOptions);
    }

    public static ShareProductData template(final ShareProductData data, final Collection<CurrencyData> currencyOptions,
            final Collection<ChargeData> chargeOptions, final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions,
            final Map<String, List<GLAccountData>> accountingMappingOptions) {

        return new ShareProductData(data.id, data.name, data.shortName, data.description, data.externalId, data.currency,
                data.totalShares, data.totalSharesIssued, data.unitPrice, data.shareCapital, data.minimumShares, data.nominalShares,
                data.maximumShares, data.marketPrice, data.charges, data.allowDividendCalculationForInactiveClients, data.lockinPeriod,
                data.lockPeriodTypeEnum, data.minimumActivePeriod, data.minimumActivePeriodForDividendsTypeEnum, data.accountingRule,
                data.accountingMappings, data.paymentChannelToFundSourceMappings, data.feeToGLAccountMappings, currencyOptions,
                chargeOptions, minimumActivePeriodFrequencyTypeOptions, lockinPeriodFrequencyTypeOptions, accountingMappingOptions);
    }

    public static ShareProductData withAccountingDetails(final ShareProductData data, final Map<String, Object> accountingMappings,
            final Collection<PaymentTypeToGLAccountMapper> paymentChannelToFundSourceMappings,
            final Collection<ChargeToGLAccountMapper> feeToGLAccountMappings) {
        return new ShareProductData(data.id, data.name, data.shortName, data.description, data.externalId, data.currency,
                data.totalShares, data.totalSharesIssued, data.unitPrice, data.shareCapital, data.minimumShares, data.nominalShares,
                data.maximumShares, data.marketPrice, data.charges, data.allowDividendCalculationForInactiveClients, data.lockinPeriod,
                data.lockPeriodTypeEnum, data.minimumActivePeriod, data.minimumActivePeriodForDividendsTypeEnum, data.accountingRule,
                accountingMappings, paymentChannelToFundSourceMappings, feeToGLAccountMappings, data.currencyOptions, data.chargeOptions,
                data.minimumActivePeriodFrequencyTypeOptions, data.lockinPeriodFrequencyTypeOptions, data.accountingMappingOptions);
    }

    private ShareProductData(final Long id, final String name, final String shortName, final Long totalShares) {
        this.id = id;
        this.name = name;
        this.shortName = shortName;
        this.description = null;
        this.externalId = null;
        this.currency = null;
        this.totalShares = totalShares;
        this.totalSharesIssued = null;
        this.unitPrice = null;
        this.shareCapital = null;
        this.minimumShares = null;
        this.nominalShares = null;
        this.maximumShares = null;
        this.marketPrice = null;
        this.charges = null;
        this.allowDividendCalculationForInactiveClients = null;
        this.lockPeriodTypeEnum = null;
        this.minimumActivePeriodForDividendsTypeEnum = null;
        this.currencyOptions = null;
        this.chargeOptions = null;
        this.minimumActivePeriodFrequencyTypeOptions = null;
        this.lockinPeriodFrequencyTypeOptions = null;
        this.lockinPeriod = null;
        this.minimumActivePeriod = null;
        this.accountingRule = null;
        this.accountingMappings = null;
        this.paymentChannelToFundSourceMappings = null;
        this.feeToGLAccountMappings = null;
        this.accountingMappingOptions = null;
    }

    public static ShareProductData generic(final Long id, final String name, final String shortName, final Long totalShares) {
        return new ShareProductData(id, name, shortName, totalShares);
    }

    public static ShareProductData lookup(final Long id, final String name) {
        final String shortName = null;
        final Long totalShares = null;
        return new ShareProductData(id, name, shortName, totalShares);

    }

    public Long getId() {
        return this.id;
    }

    public String getName() {
        return this.name;
    }

    public String getShortName() {
        return this.shortName;
    }

    public String getDescription() {
        return this.description;
    }

    public String getExternalId() {
        return this.externalId;
    }

    public CurrencyData getCurrency() {
        return this.currency;
    }

    public Long getTotalShares() {
        return this.totalShares;
    }

    public Long getTotalSharesIssued() {
        return this.totalSharesIssued;
    }

    public BigDecimal getUnitPrice() {
        return this.unitPrice;
    }

    public BigDecimal getShareCapital() {
        return this.shareCapital;
    }

    public Long getMinimumShares() {
        return this.minimumShares;
    }

    public Long getNominaltShares() {
        return this.nominalShares;
    }

    public Long getMaximumShares() {
        return this.maximumShares;
    }

    public Collection<ShareProductMarketPriceData> getMarketPrice() {
        return this.marketPrice;
    }

    public Boolean getAllowDividendCalculationForInactiveClients() {
        return this.allowDividendCalculationForInactiveClients;
    }

    public EnumOptionData getLockPeriod() {
        return this.lockPeriodTypeEnum;
    }

    public EnumOptionData getMinimumActivePeriodForDividends() {
        return this.minimumActivePeriodForDividendsTypeEnum;
    }

    public Collection<CurrencyData> getCurrencyOptions() {
        return currencyOptions;
    }

    public Collection<ChargeData> getChargeOptions() {
        return chargeOptions;
    }

    public Integer getMinimumActivePeriod() {
        return this.minimumActivePeriod;
    }

    public boolean hasAccountingEnabled() {
        return this.accountingRule.getId() > AccountingRuleType.NONE.getValue();
    }

    public int accountingRuleTypeId() {
        return this.accountingRule.getId().intValue();
    }

    public EnumOptionData getAccountingRule() {
        return this.accountingRule;
    }
}