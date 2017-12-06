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
package org.apache.fineract.portfolio.shareaccounts.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.Date;

import org.apache.fineract.accounting.glaccount.data.GLAccountData;
import org.apache.fineract.infrastructure.bulkimport.constants.TemplatePopulateImportConstants;
import org.apache.fineract.infrastructure.core.data.EnumOptionData;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.accountdetails.data.ShareAccountSummaryData;
import org.apache.fineract.portfolio.accounts.data.AccountData;
import org.apache.fineract.portfolio.charge.data.ChargeData;
import org.apache.fineract.portfolio.products.data.ProductData;
import org.apache.fineract.portfolio.savings.data.SavingsAccountData;
import org.apache.fineract.portfolio.shareproducts.data.ShareProductData;
import org.joda.time.LocalDate;

@SuppressWarnings("unused")
public class ShareAccountData implements AccountData {

    private Long id;

    private String accountNo;

    private String externalId;

    private String savingsAccountNumber;

    private Long clientId;

    private String clientName;

    private Long defaultShares ;
    
    private Long productId;

    private String productName;

    private ShareAccountStatusEnumData status;

    ShareAccountApplicationTimelineData timeline;

    private CurrencyData currency;

    private ShareAccountSummaryData summary;

    private Collection<ShareAccountTransactionData> purchasedShares;

    private Long savingsAccountId;

    private BigDecimal currentMarketPrice;

    private Integer lockinPeriod;

    private EnumOptionData lockPeriodTypeEnum;

    private Integer minimumActivePeriod;

    private EnumOptionData minimumActivePeriodTypeEnum;

    private Boolean allowDividendCalculationForInactiveClients;

    private Collection<ShareAccountChargeData> charges;

    private Collection<ShareAccountDividendData> dividends ;

    //import fields
    private Integer requestedShares;
    private LocalDate submittedDate;
    private Integer minimumActivePeriodFrequencyType;
    private Integer lockinPeriodFrequency;
    private Integer lockinPeriodFrequencyType;
    private LocalDate applicationDate;
    private String locale;
    private transient Integer rowIndex;
    private  String dateFormat;

    public static ShareAccountData importInstance(Long clientId,Long productId,Integer requestedShares,String externalId,
            LocalDate submittedOnDate , Integer minimumActivePeriodDays,Integer minimumActivePeriodFrequencyType,
            Integer lockinPeriodFrequency,Integer lockinPeriodFrequencyType,LocalDate applicationDate,
            Boolean allowDividendCalculationForInactiveClients, Collection<ShareAccountChargeData> charges,
            Long defaultSavingsAccountId,Integer rowIndex,String locale, String dateFormat){
        return new ShareAccountData(clientId,productId,requestedShares,externalId,submittedOnDate,minimumActivePeriodDays,
                minimumActivePeriodFrequencyType,lockinPeriodFrequency,lockinPeriodFrequencyType,applicationDate,allowDividendCalculationForInactiveClients,charges,
                defaultSavingsAccountId,rowIndex,locale,dateFormat);
    }
    private ShareAccountData(Long clientId,Long productId,Integer requestedShares,String externalId,
            LocalDate submittedDate , Integer minimumActivePeriod,Integer minimumActivePeriodFrequencyType,
            Integer lockinPeriodFrequency,Integer lockinPeriodFrequencyType,LocalDate applicationDate,
            Boolean allowDividendCalculationForInactiveClients, Collection<ShareAccountChargeData> charges,
            Long savingsAccountId,Integer rowIndex,String locale, String dateFormat) {

        this.clientId = clientId;
        this.productId = productId;
        this.requestedShares=requestedShares;
        this.externalId = externalId;
        this.submittedDate=submittedDate;
        this.minimumActivePeriod = minimumActivePeriod;
        this.minimumActivePeriodFrequencyType=minimumActivePeriodFrequencyType;
        this.lockinPeriodFrequency=lockinPeriodFrequency;
        this.lockinPeriodFrequencyType=lockinPeriodFrequencyType;
        this.applicationDate=applicationDate;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.dateFormat= dateFormat;
        this.locale=locale;
        this.charges = charges;
        this.savingsAccountId = savingsAccountId;
        this.rowIndex=rowIndex;
        this.clientName = null;
        this.savingsAccountNumber = null;
        this.defaultShares = null;
        this.id = null;
        this.accountNo = null;
        this.productName = null;
        this.status = null;
        this.timeline = null;
        this.currency = null;
        this.summary = null;
        this.purchasedShares = null;
        this.currentMarketPrice = null;
        this.lockinPeriod = null;
        this.lockPeriodTypeEnum = null;
        this.minimumActivePeriodTypeEnum = null;
        this.dividends = null;
        this.productOptions = null;
        this.chargeOptions = null;
        this.lockinPeriodFrequencyTypeOptions = null;
        this.minimumActivePeriodFrequencyTypeOptions = null;
        this.clientSavingsAccounts = null;
    }

    public Integer getRowIndex() {
        return rowIndex;
    }
    
    // Data for template
    private Collection<ProductData> productOptions;
    private Collection<ChargeData> chargeOptions;
    private Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions;
    private Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions;
    private Collection<SavingsAccountData> clientSavingsAccounts;

    public ShareAccountData(final Long id, final String accountNo, final String externalId, final Long savingsAccountId,
            final String savingsAccountNumber, final Long clientId, final String clientName, final Long productId,
            final String productName, final ShareAccountStatusEnumData status, final ShareAccountApplicationTimelineData timeline,
            final CurrencyData currency, final ShareAccountSummaryData summaryData, final Collection<ShareAccountChargeData> charges,
            final Collection<ShareAccountTransactionData> purchasedSharesData, final Integer lockinPeriod, final EnumOptionData lockPeriodTypeEnum,
            final Integer minimumActivePeriod, final EnumOptionData minimumActivePeriodTypeEnum, Boolean allowdividendsforinactiveclients) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.savingsAccountId = savingsAccountId;
        this.savingsAccountNumber = savingsAccountNumber;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.status = status;
        this.timeline = timeline;
        this.currency = currency;
        this.summary = summaryData;
        this.charges = charges;
        this.purchasedShares = purchasedSharesData;
        this.lockinPeriod = lockinPeriod;
        this.lockPeriodTypeEnum = lockPeriodTypeEnum;
        this.minimumActivePeriod = minimumActivePeriod;
        this.minimumActivePeriodTypeEnum = minimumActivePeriodTypeEnum;
        this.allowDividendCalculationForInactiveClients = allowdividendsforinactiveclients;
    }

    public ShareAccountData(final Long clientId, final String clientName, final Collection<ProductData> productOptions,
            final Collection<ChargeData> chargeOptions) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.productOptions = productOptions;
        this.chargeOptions = chargeOptions;
    }

    public ShareAccountData(final Long clientId, final String clientName, final CurrencyData currency,
            final Collection<ShareAccountChargeData> charges, final BigDecimal currentMarketPrice,
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, final Collection<SavingsAccountData> clientSavingsAccounts,
            final Long defaultShares) {
        this.clientId = clientId;
        this.clientName = clientName;
        this.charges = charges;
        this.currency = currency;
        this.currentMarketPrice = currentMarketPrice;
        // template
        this.minimumActivePeriodFrequencyTypeOptions = minimumActivePeriodFrequencyTypeOptions;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.clientSavingsAccounts = clientSavingsAccounts;
        this.defaultShares = defaultShares ;
    }

    private ShareAccountData(final Long id, final String accountNo, final String externalId, final Long savingsAccountId,
            final String savingsAccountNumber, final Long clientId, final String clientName, final Long productId,
            final String productName, final ShareAccountStatusEnumData status, final ShareAccountApplicationTimelineData timeline,
            final CurrencyData currency, final ShareAccountSummaryData summaryData, final Collection<ShareAccountChargeData> charges,
            final Collection<ShareAccountTransactionData> purchasedSharesData, final Integer lockinPeriod, final EnumOptionData lockPeriodTypeEnum,
            final Integer minimumActivePeriod, final EnumOptionData minimumActivePeriodTypeEnum,
            final Boolean allowDividendCalculationForInactiveClients, final Collection<ProductData> productOptions,
            final Collection<ChargeData> chargeOptions, final Collection<SavingsAccountData> clientSavingsAccounts,
            final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions,
            final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOption, final BigDecimal currenMarketPrice) {
        this.id = id;
        this.accountNo = accountNo;
        this.externalId = externalId;
        this.savingsAccountId = savingsAccountId;
        this.savingsAccountNumber = savingsAccountNumber;
        this.clientId = clientId;
        this.clientName = clientName;
        this.productId = productId;
        this.productName = productName;
        this.status = status;
        this.timeline = timeline;
        this.currency = currency;
        this.summary = summaryData;
        this.charges = charges;
        this.purchasedShares = purchasedSharesData;
        this.lockinPeriod = lockinPeriod;
        this.lockPeriodTypeEnum = lockPeriodTypeEnum;
        this.minimumActivePeriod = minimumActivePeriod;
        this.minimumActivePeriodTypeEnum = minimumActivePeriodTypeEnum;
        this.allowDividendCalculationForInactiveClients = allowDividendCalculationForInactiveClients;
        this.productOptions = productOptions;
        this.chargeOptions = chargeOptions;
        this.clientSavingsAccounts = clientSavingsAccounts;
        this.lockinPeriodFrequencyTypeOptions = lockinPeriodFrequencyTypeOptions;
        this.minimumActivePeriodFrequencyTypeOptions = minimumActivePeriodFrequencyTypeOption;
        this.currentMarketPrice = currenMarketPrice;
    }

    public Long getProductId() {
        return this.productId;
    }

    public Long getClientId() {
        return this.clientId;
    }

    public static ShareAccountData template(ShareAccountData data, Collection<ProductData> productOptions,
            Collection<ChargeData> chargeOptions, Collection<SavingsAccountData> clientSavingsAccounts,
            Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions, Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOptions) {
        return new ShareAccountData(data.id, data.accountNo, data.externalId, data.savingsAccountId, data.savingsAccountNumber,
                data.clientId, data.clientName, data.productId, data.productName, data.status, data.timeline, data.currency, data.summary,
                data.charges, data.purchasedShares, data.lockinPeriod, data.lockPeriodTypeEnum, data.minimumActivePeriod,
                data.minimumActivePeriodTypeEnum, data.allowDividendCalculationForInactiveClients, productOptions, chargeOptions,
                clientSavingsAccounts, lockinPeriodFrequencyTypeOptions, minimumActivePeriodFrequencyTypeOptions, data.currentMarketPrice);
    }

    public static ShareAccountData lookup(final Long id, final String accountNo, final Long clientId, final String clientName) {
        String externalId = null;
        ShareAccountSummaryData summaryData = null;
        Collection<ShareAccountTransactionData> purchasedSharesData = null;
        GLAccountData suspenseAccount = null;
        Integer lockinPeriod = null;
        EnumOptionData lockPeriodTypeEnum = null;
        Integer minimumActivePeriod = null;
        EnumOptionData minimumActivePeriodTypeEnum = null;
        Collection<ShareAccountChargeData> charges = null;
        ShareAccountStatusEnumData status = null;
        ShareAccountApplicationTimelineData timeline = null;
        CurrencyData currency = null;
        Boolean allowDividendCalculationForInactiveClients = null;
        final Long savingsAccountId = null;
        final String savingsAccountNumber = null;
        final String productName = null;
        final Collection<ChargeData> chargeOptions = null;
        final Collection<SavingsAccountData> clientSavingsAccounts = null;
        final Collection<EnumOptionData> lockinPeriodFrequencyTypeOptions = null;
        final Collection<EnumOptionData> minimumActivePeriodFrequencyTypeOption = null;
        final BigDecimal currenMarketPrice = null;
        final Long productId = null;
        final Collection<ProductData> productOptions = null;

        return new ShareAccountData(id, accountNo, externalId, savingsAccountId, savingsAccountNumber, clientId, clientName, productId,
                productName, status, timeline, currency, summaryData, charges, purchasedSharesData, lockinPeriod, lockPeriodTypeEnum,
                minimumActivePeriod, minimumActivePeriodTypeEnum, allowDividendCalculationForInactiveClients, productOptions,
                chargeOptions, clientSavingsAccounts, lockinPeriodFrequencyTypeOptions, minimumActivePeriodFrequencyTypeOption,
                currenMarketPrice);
    }

    public Long getId() {
        return this.id;
    }

    public Collection<ShareAccountTransactionData> getPurchasedShares() {
        return this.purchasedShares;
    }

    public void setCurrentMarketPrice(final BigDecimal currentMarketPrice) {
        this.currentMarketPrice = currentMarketPrice;
    }
    
    public void setDividends(Collection<ShareAccountDividendData> dividends) {
        this.dividends = dividends ;
    }
}
