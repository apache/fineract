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
package org.apache.fineract.portfolio.shares.data;

import java.math.BigDecimal;
import java.util.List;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.products.data.ProductData;

public class ShareProductData implements ProductData{

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
    private final GLAccountData suspenseAccount;
    private final GLAccountData equityAccount;
    private final Long minimumShares;
    private final Long nominaltShares;
    private final Long maximumShares;
    private final List<ShareMarketPriceData> marketPrice;
    private final List<ChargeData> charges;
    private Boolean allowDividendCalculationForInactiveClients;
    private final EnumOptionData lockPeriod;
    private final EnumOptionData minimumActivePeriodForDividends;

    public ShareProductData(final Long id, final String name, final String shortName, final String description, final String externalId,
            final CurrencyData currency, final Long totalShares, final Long totalSharesIssued, final BigDecimal unitPrice,
            final BigDecimal shareCapital, final GLAccountData suspenseAccount, final GLAccountData equityAccount,
            final Long minimumShares, final Long nominaltShares, final Long maximumShares, List<ShareMarketPriceData> marketPrice,
            final List<ChargeData> charges, final Boolean allowDividendCalculationForInactiveClients, final EnumOptionData lockPeriod,
            final EnumOptionData minimumActivePeriodForDividends) {
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
        this.suspenseAccount = suspenseAccount;
        this.equityAccount = equityAccount;
        this.minimumShares = minimumShares;
        this.nominaltShares = nominaltShares;
        this.maximumShares = maximumShares;
        this.marketPrice = marketPrice;
        this.charges = charges;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.lockPeriod = lockPeriod;
        this.minimumActivePeriodForDividends = minimumActivePeriodForDividends;
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

    public GLAccountData getSuspenseAccount() {
        return this.suspenseAccount;
    }

    public GLAccountData getEquityAccount() {
        return this.equityAccount;
    }

    public Long getMinimumShares() {
        return this.minimumShares;
    }

    public Long getNominaltShares() {
        return this.nominaltShares;
    }

    public Long getMaximumShares() {
        return this.maximumShares;
    }

    public List<ShareMarketPriceData> getMarketPrice() {
        return this.marketPrice;
    }

    public List<ChargeData> getCharges() {
        return this.charges;
    }

    public Boolean getAllowDividendCalculationForInactiveClients() {
        return this.allowDividendCalculationForInactiveClients;
    }

    public EnumOptionData getLockPeriod() {
        return this.lockPeriod;
    }

    public EnumOptionData getMinimumActivePeriodForDividends() {
        return this.minimumActivePeriodForDividends;
    }
}
