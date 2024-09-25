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
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unused", "unchecked" })
public class FlexibleSavingsInterestPostingIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(FlexibleSavingsInterestPostingIntegrationTest.class);
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

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
    public void testSavingsInterestPostingAtPeriodEnd() {
        // client activation, savings activation and 1st transaction date
        final String startDate = "01 December 2013";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        // Configuring global config flags
        configureInterestPosting(true, 4L);

        final Integer savingsId = createSavingsAccount(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        /***
         * Perform Post interest transaction and verify the posted transaction date
         */
        this.savingsAccountHelper.postInterestForSavings(savingsId);
        HashMap accountDetails = this.savingsAccountHelper.getSavingsDetails(savingsId);
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) accountDetails.get("transactions");
        HashMap<String, Object> interestPostingTransaction = transactions.get(transactions.size() - 2);
        for (Map.Entry<String, Object> entry : interestPostingTransaction.entrySet()) {
            LOG.info("{} - {}", entry.getKey(), entry.getValue().toString());
        }
        // 1st Dec 13 to 31st March 14 - 365 days, daily compounding using daily
        // balance
        // 33.7016 obtained from formula in excel provided by Subramanya
        assertEquals("33.7016", interestPostingTransaction.get("amount").toString(), "Equality check for interest posted amount");
        assertEquals("[2014, 3, 31]", interestPostingTransaction.get("date").toString(), "Date check for Interest Posting transaction");

    }

    private Integer createSavingsAccount(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProduct();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private void configureInterestPosting(final Boolean periodEndEnable, final Long financialYearBeginningMonth) {
        // Updating flag for interest posting at period end
        String periodEndConfigName = GlobalConfigurationConstants.SAVINGS_INTEREST_POSTING_CURRENT_PERIOD_END;
        globalConfigurationHelper.updateGlobalConfiguration(periodEndConfigName,
                new PutGlobalConfigurationsRequest().enabled(periodEndEnable));

        // Updating value for financial year beginning month
        String financialYearBeginningConfigName = GlobalConfigurationConstants.FINANCIAL_YEAR_BEGINNING_MONTH;
        globalConfigurationHelper.updateGlobalConfiguration(financialYearBeginningConfigName,
                new PutGlobalConfigurationsRequest().value(financialYearBeginningMonth));
    }

    private Integer createSavingsProduct() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsAnnual().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

}
