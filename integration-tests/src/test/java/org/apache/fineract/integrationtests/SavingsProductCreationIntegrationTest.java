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
package org.apache.fineract.integrationtests;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import org.apache.fineract.client.models.GetSavingsProductsProductIdResponse;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class SavingsProductCreationIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsProductCreationIntegrationTest.class);
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private AccountHelper accountHelper;
    private SavingsAccountHelper savingsAccountHelper;
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void testStandardSavingsProductCreation_DoesNotAllowOverdraft() {
        // --- ARRANGE ---
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(MINIMUM_OPENING_BALANCE,
                assetAccount, incomeAccount, expenseAccount, liabilityAccount);
        final GetSavingsProductsProductIdResponse savingsProductsResponse = SavingsProductHelper.getSavingsProductById(requestSpec,
                responseSpec, savingsProductID);
        Assertions.assertNotNull(savingsProductsResponse);
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings());
        Assertions.assertNull(savingsProductsResponse.getAccountingMappings().getInterestReceivableAccount());
    }

    @Test
    public void testSavingsProductWithOverdraftCreation_AllowsOverdraft() {
        // --- ARRANGE ---
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOverdraftAllowed(
                interestReceivableAccount.getAccountID().toString(), MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);
        final GetSavingsProductsProductIdResponse savingsProductsResponse = SavingsProductHelper.getSavingsProductById(requestSpec,
                responseSpec, savingsProductID);
        Assertions.assertNotNull(savingsProductsResponse);
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getInterestReceivableAccount());

        Assertions.assertEquals(interestReceivableAccount.getAccountID(),
                savingsProductsResponse.getAccountingMappings().getInterestReceivableAccount().getId().intValue());

    }

    @Test
    public void testSavingsProductWithOverdraftUpdate_AllowsOverdraft() {
        // --- ARRANGE ---
        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account interestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();

        final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOverdraftAllowed(
                interestReceivableAccount.getAccountID().toString(), MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount, expenseAccount,
                liabilityAccount);
        final GetSavingsProductsProductIdResponse savingsProductsResponse = SavingsProductHelper.getSavingsProductById(requestSpec,
                responseSpec, savingsProductID);

        Assertions.assertNotNull(savingsProductsResponse);
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings());
        Assertions.assertNotNull(savingsProductsResponse.getAccountingMappings().getInterestReceivableAccount());

        Assertions.assertEquals(interestReceivableAccount.getAccountID(),
                savingsProductsResponse.getAccountingMappings().getInterestReceivableAccount().getId().intValue());

        final Account newInterestReceivableAccount = accountHelper.createAssetAccount("interestReceivableAccount");

        final Integer savingsProductIDupdate = updateSavingsProductWithAccrualAccountingWithOverdraftAllowed(savingsProductID,
                newInterestReceivableAccount.getAccountID().toString(), MINIMUM_OPENING_BALANCE, assetAccount, incomeAccount,
                expenseAccount, liabilityAccount);

        final GetSavingsProductsProductIdResponse savingsProductsResponseUpdate = SavingsProductHelper.getSavingsProductById(requestSpec,
                responseSpec, savingsProductIDupdate);

        Assertions.assertNotNull(savingsProductsResponseUpdate);
        Assertions.assertNotNull(savingsProductsResponseUpdate.getAccountingMappings());
        Assertions.assertNotNull(savingsProductsResponseUpdate.getAccountingMappings().getInterestReceivableAccount());

        Assertions.assertEquals(newInterestReceivableAccount.getAccountID(),
                savingsProductsResponseUpdate.getAccountingMappings().getInterestReceivableAccount().getId().intValue());
        Assertions.assertNotEquals(interestReceivableAccount.getAccountID(),
                savingsProductsResponseUpdate.getAccountingMappings().getInterestReceivableAccount().getId().intValue());

    }

    public static Integer createSavingsProductWithAccrualAccountingWithOverdraftAllowed(final String interestReceivableAccount,
            final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITH OVERDRAFT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withOverDraft("100000").withAccountInterestReceivables(interestReceivableAccount)
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsAccrualBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    public static Integer createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(final String minOpenningBalance,
            final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITHOUT OVERDRAFT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsAccrualBased(accounts).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    public static Integer updateSavingsProductWithAccrualAccountingWithOverdraftAllowed(final Integer productId,
            final String interestReceivableAccount, final String minOpenningBalance, final Account... accounts) {
        LOG.info("------------------------------UPDATE SAVINGS PRODUCT ACCOUNT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestPostingPeriodTypeAsQuarterly() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withOverDraft("100000").withAccountInterestReceivables(interestReceivableAccount)
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsAccrualBased(accounts).build();
        return SavingsProductHelper.updateSavingsProduct(savingsProductJSON, requestSpec, responseSpec, productId);
    }

}
