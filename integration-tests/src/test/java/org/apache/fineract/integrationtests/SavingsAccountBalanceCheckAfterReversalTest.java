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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountBalanceCheckAfterReversalTest {

    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String START_DATE = "10 April 2022";
    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper scheduleJobHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.scheduleJobHelper = new SchedulerJobHelper(requestSpec);
    }

    @SuppressWarnings("unchecked")
    @Test
    public void testSavingsBalanceAfterWithdrawal() {
        SavingsAccountHelper savingsAccountHelperValidationError = new SavingsAccountHelper(this.requestSpec,
                new ResponseSpecBuilder().build());
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, START_DATE);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPosting(clientID);
        Integer depositTransactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", START_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.reverseSavingsAccountTransaction(savingsId, depositTransactionId);
        HashMap reversedDepositTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, depositTransactionId);
        Assertions.assertTrue((Boolean) reversedDepositTransaction.get("reversed"));
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = Float.parseFloat("0.0");
        assertEquals(balance, summary.get("accountBalance"), "Verifying opening Balance is 0");
        List<HashMap> error = (List<HashMap>) savingsAccountHelperValidationError.withdrawalFromSavingsAccount(savingsId, "100", START_DATE,
                CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.savingsaccount.transaction.insufficient.account.balance",
                error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    @Test
    public void testSavingsBalanceWithOverDraftAfterWithdrawal() {
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, START_DATE);
        Assertions.assertNotNull(clientID);
        final Integer savingsId = createSavingsAccountDailyPostingWithOverDraft(clientID);
        Integer withdrawalTransactionId = (Integer) this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "1000", START_DATE,
                CommonConstants.RESPONSE_RESOURCE_ID);
        this.savingsAccountHelper.reverseSavingsAccountTransaction(savingsId, withdrawalTransactionId);
        HashMap reversedWithdrawalTransaction = this.savingsAccountHelper.getSavingsTransaction(savingsId, withdrawalTransactionId);
        Assertions.assertTrue((Boolean) reversedWithdrawalTransaction.get("reversed"));
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = Float.parseFloat("0.0");
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance is 0");
        Integer withdrawalAfterReversalTransactionId = (Integer) savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "500",
                START_DATE, CommonConstants.RESPONSE_RESOURCE_ID);
        summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        balance = Float.parseFloat("-500.0");
        assertEquals(balance, summary.get("accountBalance"), "Verifying Balance is -500");
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, START_DATE);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, START_DATE);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, START_DATE);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsAccountDailyPostingWithOverDraft(final Integer clientID) {
        final Integer savingsProductID = createSavingsProductDailyPostingWithOverDraft();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, START_DATE);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, START_DATE);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, START_DATE);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createSavingsProductDailyPostingWithOverDraft() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().withOverDraft("10000").build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

}
