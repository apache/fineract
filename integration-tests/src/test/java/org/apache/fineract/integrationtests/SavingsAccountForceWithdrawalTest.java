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
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class SavingsAccountForceWithdrawalTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testForceWithdrawal() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT,
                new PutGlobalConfigurationsRequest().enabled(true));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT,
                new PutGlobalConfigurationsRequest().value(5000L).enabled(true));

        GlobalConfigurationPropertyData config = globalConfigurationHelper
                .getGlobalConfigurationByName(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT);
        Assertions.assertEquals(5000L, config.getValue());

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final Integer savingsProductId = createSavingsProductDailyPosting();
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplication(clientID, savingsProductId, "INDIVIDUAL");
        this.savingsAccountHelper.approveSavings(savingsId);
        this.savingsAccountHelper.activateSavings(savingsId);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", "04 March 2013", null);

        PostSavingsAccountTransactionsRequest request = new PostSavingsAccountTransactionsRequest() //
                .locale("en") //
                .dateFormat("dd MMMM yyyy") //
                .transactionDate("05 March 2013") //
                .transactionAmount(java.math.BigDecimal.valueOf(200.0)) //
                .paymentTypeId(1);

        retrofit2.Response<PostSavingsAccountTransactionsResponse> response = this.savingsAccountHelper
                .forceWithdrawalFromSavingsAccount(savingsId.longValue(), request);

        Assertions.assertTrue(response.isSuccessful(), () -> "Force withdrawal failed with body: " + getErrorBody(response));
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private String getErrorBody(retrofit2.Response<?> response) {
        try {
            return response.errorBody() != null ? response.errorBody().string() : "No error body";
        } catch (Exception e) {
            return "Failed to read error body: " + e.getMessage();
        }
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT,
                new PutGlobalConfigurationsRequest().enabled(false));
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.FORCE_WITHDRAWAL_ON_SAVINGS_ACCOUNT_LIMIT,
                new PutGlobalConfigurationsRequest().value(0L).enabled(false));
    }
}
