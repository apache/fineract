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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.HashMap;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.PaymentTypeHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Client Savings Integration Test for checking Savings Application.
 */
@SuppressWarnings({ "rawtypes" })
@Order(2)
public class SavingsAccountRecalculateBalanceTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountRecalculateBalanceTest.class);
    public static final String DEPOSIT_AMOUNT = "2000";
    public static final String WITHDRAW_AMOUNT = "1000";
    public static final String WITHDRAW_AMOUNT_ADJUSTED = "500";
    public static final String MINIMUM_OPENING_BALANCE = "1000.0";
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String DATE_FORMAT = "dd MMMM yyyy";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private SavingsProductHelper savingsProductHelper;
    private SchedulerJobHelper scheduleJobHelper;
    private PaymentTypeHelper paymentTypeHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.paymentTypeHelper = new PaymentTypeHelper();
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testSavingsAccountDepositAfterNegativeHoldAmount() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, "0", null, false, true, false, null);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        float balance = 0F;
        float transactionAmount = 100F;
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId,
                String.valueOf(transactionAmount), SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);
        balance = balance + transactionAmount;
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after deposit");
        Integer withdrawalTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId,
                String.valueOf(transactionAmount), SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(withdrawalTransactionId);
        balance = balance - transactionAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after withdrawal");

        float holdAmount = 50F;
        Integer holdTransactionId = (Integer) this.savingsAccountHelper.holdAmountInSavingsAccount(savingsId, String.valueOf(holdAmount),
                false, SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(holdTransactionId);
        balance = balance - holdAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after hold amount");
        this.savingsAccountHelper.releaseAmount(savingsId, holdTransactionId);
        balance = balance + holdAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after release amount");

        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, String.valueOf(transactionAmount),
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);
        balance = balance + transactionAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after hold-release-deposit");
    }

    @Test
    public void testSavingsAccountDepositAfterNegativeHoldAmountNoInterest() {
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        final Integer savingsProductID = createSavingsProduct(this.requestSpec, this.responseSpec, "0", null, false, true, false,
                BigDecimal.ZERO);
        Assertions.assertNotNull(savingsProductID);

        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductID, ACCOUNT_TYPE_INDIVIDUAL);
        this.savingsAccountHelper.approveSavings(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.activateSavings(savingsId);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);

        float balance = 0F;
        float transactionAmount = 100F;
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId,
                String.valueOf(transactionAmount), SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);
        balance = balance + transactionAmount;
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after deposit");
        HashMap depositTransaction = savingsAccountHelper.getTransactionDetails(savingsId, depositTransactionId);
        assertEquals(balance, depositTransaction.get("runningBalance"), "Verifying Running Balance of deposit");
        Integer withdrawalTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId,
                String.valueOf(transactionAmount), SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(withdrawalTransactionId);
        balance = balance - transactionAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after withdrawal");
        HashMap withdrawalTransaction = savingsAccountHelper.getTransactionDetails(savingsId, withdrawalTransactionId);
        assertEquals(balance, withdrawalTransaction.get("runningBalance"), "Verifying Running Balance of withdraw");

        float holdAmount = 50F;
        Integer holdTransactionId = (Integer) this.savingsAccountHelper.holdAmountInSavingsAccount(savingsId, String.valueOf(holdAmount),
                false, SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(holdTransactionId);
        balance = balance - holdAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after hold amount");
        Integer releaseTransactionId = this.savingsAccountHelper.releaseAmount(savingsId, holdTransactionId);
        Assertions.assertNotNull(releaseTransactionId);
        balance = balance + holdAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after release amount");

        depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, String.valueOf(transactionAmount),
                SavingsAccountHelper.TRANSACTION_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        Assertions.assertNotNull(depositTransactionId);
        balance = balance + transactionAmount;
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        assertEquals(balance, summary.get("availableBalance"), "Verifying Balance after hold-release-deposit");
        depositTransaction = savingsAccountHelper.getTransactionDetails(savingsId, depositTransactionId);
        // this is a backdated transaction and so listed before the release transaction
        assertEquals(balance - holdAmount, depositTransaction.get("runningBalance"),
                "Verifying Running Balance of deposit negative balance");

        HashMap releaseTransaction = savingsAccountHelper.getTransactionDetails(savingsId, releaseTransactionId);
        assertFalse((Boolean) releaseTransaction.get("reversed"), "Verifying release transaction with overdraft is not reversed");
        assertEquals(balance, releaseTransaction.get("runningBalance"), "Verifying Running Balance");
    }

    // LienAtProductLevel
    private Integer createSavingsProduct(final RequestSpecification requestSpec, final ResponseSpecification responseSpec,
            final String minOpenningBalance, String minBalanceForInterestCalculation, final boolean enforceMinRequiredBalance,
            final boolean allowOverDraft, final boolean lienAllowed, BigDecimal interestRate) {

        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITH LIEN---------------------------------------");
        SavingsProductHelper savingsProductHelper = new SavingsProductHelper();
        if (lienAllowed) {
            final String maxAllowedLienLimit = "2000.0";
            savingsProductHelper.withLienAllowed(maxAllowedLienLimit);
        }
        if (enforceMinRequiredBalance) {
            final String minRequiredBalance = "100.0";
            savingsProductHelper.withMinRequiredBalance(minRequiredBalance);
            savingsProductHelper.withEnforceMinRequiredBalance("true");
        }
        if (allowOverDraft) {
            final String overDraftLimit = "500.0";
            savingsProductHelper.withOverDraft(overDraftLimit);
        }
        if (interestRate != null) {
            savingsProductHelper.withNominalAnnualInterestRate(interestRate);
        }
        final String savingsProductJSON = savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                .withMinBalanceForInterestCalculation(minBalanceForInterestCalculation).withMinimumOpenningBalance(minOpenningBalance)
                .build();

        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }
}
