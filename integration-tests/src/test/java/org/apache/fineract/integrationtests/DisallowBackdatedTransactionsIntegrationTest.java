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
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Integration tests for the {@code disallow-backdated-transactions} global configuration (FINERACT-1950).
 */
@SuppressWarnings({ "rawtypes", "unchecked" })
public class DisallowBackdatedTransactionsIntegrationTest {

    private static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private SavingsAccountHelper savingsAccountHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void backdatedTransactionsAreRejectedOnlyWhileConfigurationEnabled() {
        final DateTimeFormatter dateFormatter = DateTimeFormatter.ofPattern(CommonConstants.DATE_FORMAT, Locale.US);
        // the allowed window is anchored to the tenant's business date, so "today" must use the tenant timezone
        final LocalDate today = Utils.getLocalDateOfTenant();
        final String backdatedDate = dateFormatter.format(today.minusMonths(2));
        final String currentDate = dateFormatter.format(today);
        // opened six months ago rather than on the 2013 helper defaults: the server wide "Post Interest For Savings"
        // job replays the whole life of every active account, so a decade old account slows down unrelated tests
        final String openedOnDate = dateFormatter.format(today.minusMonths(6));

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsQuarterly().withInterestCalculationPeriodTypeAsDailyBalance()
                .withMinimumOpenningBalance("100").build();
        final Integer savingsProductID = SavingsProductHelper.createSavingsProduct(savingsProductJSON, this.requestSpec, this.responseSpec);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, openedOnDate);
        this.savingsAccountHelper.approveSavingsOnDate(savingsId, openedOnDate);
        this.savingsAccountHelper.activateSavings(savingsId, openedOnDate);

        this.globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            // backdated deposit is rejected while the configuration is enabled
            final ResponseSpecification domainRuleErrorSpec = new ResponseSpecBuilder().expectStatusCode(403).build();
            final SavingsAccountHelper rejectedHelper = new SavingsAccountHelper(this.requestSpec, domainRuleErrorSpec);
            final List<HashMap> error = (List<HashMap>) rejectedHelper.depositToSavingsAccount(savingsId, "100", backdatedDate,
                    CommonConstants.RESPONSE_ERROR);
            assertEquals("error.msg.transaction.backdated.not.allowed", error.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

            // current-date deposit is still allowed
            assertNotNull(this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", currentDate, "resourceId"));

            // a tolerance window (in days) allows backdating within it
            this.globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                    new PutGlobalConfigurationsRequest().enabled(true).value(90L));
            assertNotNull(this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", backdatedDate, "resourceId"));
        } finally {
            this.globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.DISALLOW_BACKDATED_TRANSACTIONS,
                    new PutGlobalConfigurationsRequest().enabled(false).value(0L));
        }

        // with the configuration disabled again, backdated deposits pass
        assertNotNull(this.savingsAccountHelper.depositToSavingsAccount(savingsId, "100", backdatedDate, "resourceId"));

        this.savingsAccountHelper.closeSavingsAccountOnDate(savingsId, "true", currentDate);
    }
}
