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
package org.apache.fineract.integrationtests.common.shares;

import com.google.gson.Gson;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.Assertions;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ShareProductHelper {

    private static final Logger LOG = LoggerFactory.getLogger(ShareProductHelper.class);
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "4";
    private static final String IN_MULTIPLES_OF = "0";
    private static final String USD = "USD";

    private String productName = Utils.uniqueRandomStringGenerator("SHARE_PRODUCT_", 6);
    private String shortName = Utils.uniqueRandomStringGenerator("", 4);
    private String description = Utils.randomStringGenerator("", 20);
    private String totalShares = "10000";
    private final String currencyCode = USD;
    private String sharesIssued = "10000";
    private String unitPrice = "2.0";
    private String minimumShares = "10";
    private String nominalShares = "20";
    private String maximumShares = "3000";
    private String allowDividendCalculationForInactiveClients = "true";
    private String lockinPeriodFrequency = "1";
    private String lockinPeriodFrequencyType = "0";
    private String accountingRule = NONE;

    String minimumActivePeriodForDividends = "1";
    String minimumactiveperiodFrequencyType = "0";

    private List<Map<String, String>> charges = null;
    private List<Map<String, String>> marketPrices = null;
    private Account[] accountList = null;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public String build() {
        final HashMap<String, Object> map = new HashMap<>();
        map.put("name", this.productName);
        map.put("shortName", this.shortName);
        map.put("description", this.description);
        map.put("currencyCode", this.currencyCode);
        map.put("locale", LOCALE);
        map.put("digitsAfterDecimal", DIGITS_AFTER_DECIMAL);
        map.put("inMultiplesOf", IN_MULTIPLES_OF);
        map.put("totalShares", this.totalShares);
        map.put("sharesIssued", this.sharesIssued);
        map.put("unitPrice", this.unitPrice);
        map.put("minimumShares", this.minimumShares);
        map.put("nominalShares", this.nominalShares);
        map.put("maximumShares", this.maximumShares);
        map.put("allowDividendCalculationForInactiveClients", this.allowDividendCalculationForInactiveClients);
        map.put("accountingRule", this.accountingRule);
        map.put("minimumActivePeriodForDividends", this.minimumActivePeriodForDividends);
        map.put("minimumactiveperiodFrequencyType", this.minimumactiveperiodFrequencyType);
        map.put("lockinPeriodFrequency", this.lockinPeriodFrequency);
        map.put("lockinPeriodFrequencyType", this.lockinPeriodFrequencyType);

        if (charges != null) {
            map.put("chargesSelected", charges);
        }

        if (marketPrices != null) {
            map.put("marketPricePeriods", marketPrices);
        }

        if (this.accountingRule.equals(CASH_BASED)) {
            map.putAll(getAccountMappingForCashBased());
        }

        String shareProductCreateJson = new Gson().toJson(map);
        LOG.info("{}", shareProductCreateJson);
        return shareProductCreateJson;
    }

    public ShareProductHelper withCashBasedAccounting() {
        this.accountingRule = CASH_BASED;
        return this;
    }

    public ShareProductHelper withCashBasedAccounting(final Account[] account_list) {
        this.accountingRule = CASH_BASED;
        this.accountList = account_list;
        return this;
    }

    public ShareProductHelper withMarketPrice() {
        this.marketPrices = new ArrayList<>();
        LocalDate currentDate = Utils.getLocalDateOfTenant();
        String[] prices = { "3.0", "4.0", "5.0", "6.0", "7.0" };
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        for (int i = 0; i < prices.length; i++) {
            currentDate = currentDate.plusMonths(2);
            Map<String, String> marketPrice = new HashMap<>();
            marketPrice.put("fromDate", simple.format(currentDate));
            marketPrice.put("shareValue", prices[i]);
            this.marketPrices.add(marketPrice);
        }
        return this;
    }

    public ShareProductHelper withCharges(final ArrayList<Long> charges) {
        if (charges != null && !charges.isEmpty()) {
            this.charges = new ArrayList<>();
            for (Long chargeId : charges) {
                Map<String, String> charge = new HashMap<>();
                charge.put("id", String.valueOf(chargeId.longValue()));
                this.charges.add(charge);
            }
        }
        return this;
    }

    @SuppressWarnings("unchecked")
    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public void verifyShareProduct(Map<String, Object> shareProductData) {
        String productName = (String) shareProductData.get("name");
        Assertions.assertEquals(this.productName, productName);
        String shortName = (String) shareProductData.get("shortName");
        Assertions.assertEquals(this.shortName, shortName);

        String description = (String) shareProductData.get("description");
        Assertions.assertEquals(this.description, description);

        Map<String, String> currency = (Map<String, String>) shareProductData.get("currency");
        String currencyCode = currency.get("code");
        Assertions.assertEquals(this.currencyCode, currencyCode);

        String digitsAfterDecimal = String.valueOf(currency.get("decimalPlaces"));
        Assertions.assertEquals(DIGITS_AFTER_DECIMAL, digitsAfterDecimal);

        String inMultiplesOf = String.valueOf(currency.get("inMultiplesOf"));
        Assertions.assertEquals(IN_MULTIPLES_OF, inMultiplesOf);

        String totalShares = String.valueOf(shareProductData.get("totalShares"));
        Assertions.assertEquals(this.totalShares, totalShares);

        String sharesIssued = String.valueOf(shareProductData.get("totalSharesIssued"));
        Assertions.assertEquals(this.sharesIssued, sharesIssued);

        String unitPrice = String.valueOf(shareProductData.get("unitPrice"));
        Assertions.assertEquals(this.unitPrice, unitPrice);

        String minimumShares = String.valueOf(shareProductData.get("minimumShares"));
        Assertions.assertEquals(this.minimumShares, minimumShares);

        String nominalShares = String.valueOf(shareProductData.get("nominalShares"));
        Assertions.assertEquals(this.nominalShares, nominalShares);

        String maximumShares = String.valueOf(shareProductData.get("maximumShares"));
        Assertions.assertEquals(this.maximumShares, maximumShares);

        String allowDividendCalculationForInactiveClients = String
                .valueOf(shareProductData.get("allowDividendCalculationForInactiveClients"));
        Assertions.assertEquals(this.allowDividendCalculationForInactiveClients, allowDividendCalculationForInactiveClients);

        Map<String, Object> accountingRuleMap = (Map<String, Object>) shareProductData.get("accountingRule");
        String accountingRule = String.valueOf(accountingRuleMap.get("id"));
        Assertions.assertEquals(this.accountingRule, accountingRule);

        String minimumActivePeriodForDividends = String.valueOf(shareProductData.get("minimumActivePeriod"));
        Assertions.assertEquals(this.minimumActivePeriodForDividends, minimumActivePeriodForDividends);

        Map<String, Object> minimumActivePeriodType = (Map<String, Object>) shareProductData.get("minimumActivePeriodForDividendsTypeEnum");
        String minimumactiveperiodFrequencyType = String.valueOf(minimumActivePeriodType.get("id"));
        Assertions.assertEquals(this.minimumactiveperiodFrequencyType, minimumactiveperiodFrequencyType);

        String lockinPeriodFrequency = String.valueOf(shareProductData.get("lockinPeriod"));
        Assertions.assertEquals(this.lockinPeriodFrequency, lockinPeriodFrequency);

        Map<String, Object> lockinPeriodType = (Map<String, Object>) shareProductData.get("lockPeriodTypeEnum");
        String lockinPeriodFrequencyType = String.valueOf(lockinPeriodType.get("id"));
        Assertions.assertEquals(this.lockinPeriodFrequencyType, lockinPeriodFrequencyType);

        // ArrayList<Map<String, String>> charges = (ArrayList<Map<String,
        // String>>)shareProductData.get("chargesSelected") ;

        // ArrayList<Map<String, String>> marketPrices = (ArrayList<Map<String,
        // String>>)shareProductData.get("marketPricePeriods") ;

    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    private Map<String, String> getAccountMappingForCashBased() {
        final Map<String, String> map = new HashMap<>();
        if (accountList != null) {
            for (int i = 0; i < this.accountList.length; i++) {
                if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("shareReferenceId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("shareSuspenseId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.EQUITY)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("shareEquityId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("incomeFromFeeAccountId", ID);
                }
            }
        }
        return map;
    }
}
