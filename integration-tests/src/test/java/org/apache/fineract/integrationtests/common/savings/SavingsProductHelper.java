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
package org.apache.fineract.integrationtests.common.savings;

import static org.junit.jupiter.api.Assertions.assertEquals;

import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PostSavingsProductsResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings("unused")
public class SavingsProductHelper extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsProductHelper.class);
    private static final String SAVINGS_PRODUCT_URL = "/fineract-provider/api/v1/savingsproducts";
    private static final String CREATE_SAVINGS_PRODUCT_URL = SAVINGS_PRODUCT_URL + "?" + Utils.TENANT_IDENTIFIER;
    private static final Gson GSON = new JSON().getGson();

    private static final String LOCALE = "en_GB";
    private static final String DIGITS_AFTER_DECIMAL = "4";
    private static final String IN_MULTIPLES_OF = "0";
    private static final String USD = "USD";
    private static final String DAYS = "0";
    private static final String WEEKS = "1";
    private static final String MONTHS = "2";
    private static final String YEARS = "3";
    private static final String DAILY = "1";
    private static final String MONTHLY = "4";
    private static final String QUARTERLY = "5";
    private static final String ANNUAL = "7";
    private static final String INTEREST_CALCULATION_USING_DAILY_BALANCE = "1";
    private static final String INTEREST_CALCULATION_USING_AVERAGE_DAILY_BALANCE = "2";
    private static final String DAYS_360 = "360";
    private static final String DAYS_365 = "365";
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";

    private String nameOfSavingsProduct = Utils.uniqueRandomStringGenerator("SAVINGS_PRODUCT_", 6);
    private String shortName = Utils.uniqueRandomStringGenerator("", 4);
    private String description = Utils.randomStringGenerator("", 20);
    private String interestCompoundingPeriodType = "4";
    private String interestPostingPeriodType = "4";
    private String interestCalculationType = INTEREST_CALCULATION_USING_DAILY_BALANCE;
    private String nominalAnnualInterestRate = "10.0";
    private String accountingRule = NONE;
    private String savingsReferenceAccountId = null;
    private String transfersInSuspenseAccountId = null;
    private String savingsControlAccountId = null;
    private String interestOnSavingsAccountId = null;
    private String incomeFromFeeAccountId = null;
    private String incomeFromPenaltyAccountId = null;
    private String overdraftPortfolioControlId = null;
    private String incomeFromInterestId = null;
    private String writeOffAccountId = null;
    private String minRequiredOpeningBalance = null;
    private String lockinPeriodFrequency = "0";
    private String withdrawalFeeForTransfers = "true";
    private String lockingPeriodFrequencyType = DAYS;
    private String currencyCode = USD;
    private final String interestCalculationDaysInYearType = DAYS_365;
    private Account[] accountList = null;
    private String minBalanceForInterestCalculation = null;
    private String allowOverdraft = "false";
    private String overdraftLimit = null;
    private String minRequiredBalance = null;
    private String enforceMinRequiredBalance = "false";
    private String lienAllowed = "false";
    private String maxAllowedLienLimit = null;
    private Boolean withHoldTax = false;
    private String taxGroupId = null;
    private boolean isDormancyTrackingActive = false;
    private String daysToInactive = null;
    private String daysToDormancy = null;
    private String daysToEscheat = null;
    private Boolean withgsimID = null;
    private Integer gsimID = null;
    private String nominalAnnualInterestRateOverdraft = null;

    public String build() {
        final HashMap<String, String> map = new HashMap<>();

        map.put("name", this.nameOfSavingsProduct);
        map.put("shortName", this.shortName);
        map.put("description", this.description);
        map.put("currencyCode", this.currencyCode);
        map.put("interestCalculationDaysInYearType", this.interestCalculationDaysInYearType);
        map.put("locale", LOCALE);
        map.put("digitsAfterDecimal", DIGITS_AFTER_DECIMAL);
        map.put("inMultiplesOf", IN_MULTIPLES_OF);
        map.put("interestCalculationType", this.interestCalculationType);
        map.put("nominalAnnualInterestRate", this.nominalAnnualInterestRate);
        map.put("interestCompoundingPeriodType", this.interestCompoundingPeriodType);
        map.put("interestPostingPeriodType", this.interestPostingPeriodType);
        map.put("accountingRule", this.accountingRule);
        map.put("savingsReferenceAccountId", this.savingsReferenceAccountId);
        map.put("transfersInSuspenseAccountId", this.transfersInSuspenseAccountId);
        map.put("savingsControlAccountId", this.savingsControlAccountId);
        map.put("interestOnSavingsAccountId", this.interestOnSavingsAccountId);
        map.put("incomeFromFeeAccountId", this.incomeFromFeeAccountId);
        map.put("incomeFromPenaltyAccountId", this.incomeFromPenaltyAccountId);
        map.put("overdraftPortfolioControlId", this.overdraftPortfolioControlId);
        map.put("incomeFromInterestId", this.incomeFromInterestId);
        map.put("writeOffAccountId", this.writeOffAccountId);
        map.put("minRequiredOpeningBalance", this.minRequiredOpeningBalance);
        map.put("lockinPeriodFrequency", this.lockinPeriodFrequency);
        map.put("lockinPeriodFrequencyType", this.lockingPeriodFrequencyType);
        map.put("withdrawalFeeForTransfers", this.withdrawalFeeForTransfers);
        map.put("minBalanceForInterestCalculation", minBalanceForInterestCalculation);
        map.put("allowOverdraft", this.allowOverdraft);
        map.put("overdraftLimit", this.overdraftLimit);
        map.put("minRequiredBalance", this.minRequiredBalance);
        map.put("enforceMinRequiredBalance", this.enforceMinRequiredBalance);
        map.put("lienAllowed", this.lienAllowed);
        map.put("maxAllowedLienLimit", this.maxAllowedLienLimit);
        map.put("withHoldTax", this.withHoldTax.toString());
        map.put("nominalAnnualInterestRateOverdraft", this.nominalAnnualInterestRateOverdraft);

        if (withHoldTax) {
            map.put("taxGroupId", taxGroupId);
        }
        if (this.accountingRule.equals(CASH_BASED)) {
            map.putAll(getAccountMappingForCashBased());
        }
        if (this.accountingRule.equals(ACCRUAL_PERIODIC)) {
            map.putAll(getAccountMappingForAccrualBased());
        }
        if (this.isDormancyTrackingActive) {
            map.put("isDormancyTrackingActive", Boolean.toString(this.isDormancyTrackingActive));
            map.put("daysToInactive", this.daysToInactive);
            map.put("daysToDormancy", this.daysToDormancy);
            map.put("daysToEscheat", this.daysToEscheat);

        }

        String savingsProductCreateJson = new Gson().toJson(map);
        LOG.info("{}", savingsProductCreateJson);
        return savingsProductCreateJson;
    }

    public SavingsProductHelper withSavingsName(final String savingsName) {
        this.nameOfSavingsProduct = savingsName;
        return this;
    }

    public SavingsProductHelper withInterestCompoundingPeriodTypeAsDaily() {
        this.interestCompoundingPeriodType = DAILY;
        return this;
    }

    public SavingsProductHelper withMinimumOpenningBalance(String minBalance) {
        this.minRequiredOpeningBalance = minBalance;
        return this;
    }

    public SavingsProductHelper withInterestCompoundingPeriodTypeAsMonthly() {
        this.interestCompoundingPeriodType = MONTHLY;
        return this;
    }

    public SavingsProductHelper withInterestPostingPeriodTypeAsMonthly() {
        this.interestPostingPeriodType = MONTHLY;
        return this;
    }

    public SavingsProductHelper withMinBalanceForInterestCalculation(final String amount) {
        this.minBalanceForInterestCalculation = amount;
        return this;
    }

    public SavingsProductHelper withInterestPostingPeriodTypeAsQuarterly() {
        this.interestPostingPeriodType = QUARTERLY;
        return this;
    }

    public SavingsProductHelper withInterestPostingPeriodTypeAsAnnual() {
        this.interestPostingPeriodType = ANNUAL;
        return this;
    }

    public SavingsProductHelper withInterestPostingPeriodTypeAsDaily() {
        this.interestPostingPeriodType = DAILY;
        return this;
    }

    public SavingsProductHelper withInterestCalculationPeriodTypeAsDailyBalance() {
        this.interestCalculationType = INTEREST_CALCULATION_USING_DAILY_BALANCE;
        return this;
    }

    public SavingsProductHelper withInterestCalculationPeriodTypeAsAverageDailyBalance() {
        this.interestCalculationType = INTEREST_CALCULATION_USING_AVERAGE_DAILY_BALANCE;
        return this;
    }

    public SavingsProductHelper withAccountingRuleAsNone() {
        this.accountingRule = NONE;
        return this;
    }

    public SavingsProductHelper withAccountingRuleAsAccrualBased(final Account[] account_list) {
        this.accountingRule = ACCRUAL_PERIODIC;
        this.accountList = account_list;
        return this;
    }

    public SavingsProductHelper withAccountingRuleAsCashBased(final Account[] account_list) {
        this.accountingRule = CASH_BASED;
        this.accountList = account_list;
        return this;
    }

    public SavingsProductHelper withMinRequiredBalance(final String minRequiredBalance) {
        this.minRequiredBalance = minRequiredBalance;
        return this;
    }

    public SavingsProductHelper withEnforceMinRequiredBalance(final String enforceMinRequiredBalance) {
        this.enforceMinRequiredBalance = enforceMinRequiredBalance;
        return this;
    }

    public SavingsProductHelper withLienAllowed(final String maxAllowedLienLimit) {
        this.lienAllowed = "true";
        this.maxAllowedLienLimit = maxAllowedLienLimit;
        return this;
    }

    public SavingsProductHelper withOverDraft(final String overdraftLimit) {
        this.allowOverdraft = "true";
        this.overdraftLimit = overdraftLimit;
        return this;
    }

    public SavingsProductHelper withOverDraftRate(final String overdraftLimit, String nominalAnnualInterestRateOverdraft) {
        this.allowOverdraft = "true";
        this.overdraftLimit = overdraftLimit;
        this.nominalAnnualInterestRateOverdraft = nominalAnnualInterestRateOverdraft;
        return this;
    }

    public SavingsProductHelper withWithHoldTax(final String taxGroupId) {
        if (taxGroupId != null) {
            this.withHoldTax = true;
            this.taxGroupId = taxGroupId;
        }
        return this;
    }

    public SavingsProductHelper withgsimID(final Integer gsimID) {
        if (withgsimID != null) {
            this.gsimID = gsimID;
        }
        return this;
    }

    public SavingsProductHelper withCurrencyCode(String currency) {
        this.currencyCode = currency;
        return this;
    }

    public SavingsProductHelper withNominalAnnualInterestRate(BigDecimal interestRate) {
        this.nominalAnnualInterestRate = interestRate.toString();
        return this;
    }

    private Map<String, String> getAccountMappingForCashBased() {
        final Map<String, String> map = new HashMap<>();
        if (accountList != null) {
            for (int i = 0; i < this.accountList.length; i++) {
                if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("savingsReferenceAccountId", ID);
                    map.put("overdraftPortfolioControlId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("savingsControlAccountId", ID);
                    map.put("transfersInSuspenseAccountId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("interestOnSavingsAccountId", ID);
                    map.put("writeOffAccountId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("incomeFromFeeAccountId", ID);
                    map.put("incomeFromPenaltyAccountId", ID);
                    map.put("incomeFromInterestId", ID);
                }
            }
        }
        return map;
    }

    private Map<String, String> getAccountMappingForAccrualBased() {
        final Map<String, String> map = new HashMap<>();
        if (accountList != null) {
            for (int i = 0; i < this.accountList.length; i++) {
                if (this.accountList[i].getAccountType().equals(Account.AccountType.ASSET)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("savingsReferenceAccountId", ID);
                    map.put("overdraftPortfolioControlId", ID);
                    map.put("feesReceivableAccountId", ID);
                    map.put("penaltiesReceivableAccountId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.LIABILITY)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("savingsControlAccountId", ID);
                    map.put("transfersInSuspenseAccountId", ID);
                    map.put("interestPayableAccountId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.EXPENSE)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("interestOnSavingsAccountId", ID);
                    map.put("writeOffAccountId", ID);
                }
                if (this.accountList[i].getAccountType().equals(Account.AccountType.INCOME)) {
                    final String ID = this.accountList[i].getAccountID().toString();
                    map.put("incomeFromFeeAccountId", ID);
                    map.put("incomeFromPenaltyAccountId", ID);
                    map.put("incomeFromInterestId", ID);
                }
            }
        }
        return map;
    }

    public static Integer createSavingsProduct(final String savingsProductJSON, final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec) {
        return Utils.performServerPost(requestSpec, responseSpec, CREATE_SAVINGS_PRODUCT_URL, savingsProductJSON, "resourceId");
    }

    public static void verifySavingsProductCreatedOnServer(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final Integer generatedProductID) {
        LOG.info("------------------------------CHECK CLIENT DETAILS------------------------------------\n");
        final String GET_SAVINGS_PRODUCT_URL = SAVINGS_PRODUCT_URL + "/" + generatedProductID + "?" + Utils.TENANT_IDENTIFIER;
        final Integer responseSavingsProductID = Utils.performServerGet(requestSpec, responseSpec, GET_SAVINGS_PRODUCT_URL, "id");
        assertEquals(generatedProductID, responseSavingsProductID, "ERROR IN CREATING THE Savings Product");
    }

    public SavingsProductHelper withDormancy() {
        this.isDormancyTrackingActive = true;
        this.daysToInactive = "30";
        this.daysToDormancy = "60";
        this.daysToEscheat = "90";
        return this;
    }

    public static GetSavingsProductsProductIdResponse getSavingsProductById(final RequestSpecification requestSpec,
            final ResponseSpecification responseSpec, final Integer productId) {
        LOG.info("-------------------- RETRIEVING SAVINGS DEPOSIT PRODUCT BY ID --------------------------");
        final String GET_PRODUCT_BY_ID_URL = SAVINGS_PRODUCT_URL + "/" + productId + "?" + Utils.TENANT_IDENTIFIER;
        final String response = Utils.performServerGet(requestSpec, responseSpec, GET_PRODUCT_BY_ID_URL);
        return GSON.fromJson(response, GetSavingsProductsProductIdResponse.class);
    }

    public PostSavingsProductsResponse createSavingsProduct(PostSavingsProductsRequest request) {
        return ok(fineract().savingsProducts.create13(request));
    }

    public GetSavingsProductsProductIdResponse getSavingsProduct(Long savingsProductId) {
        return ok(fineract().savingsProducts.retrieveOne27(savingsProductId));
    }

}
