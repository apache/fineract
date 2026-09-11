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
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.SavingsAccountSubStatusEnumData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.savings.base.BaseSavingsIntegrationTest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Validates the end-to-end execution of the Savings Close of Business (SAVINGS_COB) job triggered inline, mirroring the
 * way Loan COB is triggered through {@code POST /jobs/{jobName}/inline}.
 */
public class SavingsInlineCOBIntegrationTest extends BaseSavingsIntegrationTest {

    private static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        savingsAccountHelper = new SavingsAccountHelper(requestSpec, responseSpec);
        globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(false));
    }

    @Test
    public void inlineSavingsCOBMovesAccountToInactiveSubStatus() {
        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));

        final LocalDate activationDate = LocalDate.of(2024, 1, 1);
        // Business date on the day the account is funded; COB date 45 days later so the account is well past the
        // product's 30-day inactivity threshold and should be flagged inactive by
        // UpdateSavingsDormantStatusBusinessStep.
        BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, activationDate);
        BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, activationDate.minusDays(1));

        final Long clientId = createClient();
        Assertions.assertNotNull(clientId);

        final Long savingsProductId = createDormancyTrackingSavingsProduct();
        Assertions.assertNotNull(savingsProductId);

        final Long savingsId = savingsAccountHelper
                .applyForSavingsApplicationOnDate(clientId, savingsProductId, ACCOUNT_TYPE_INDIVIDUAL, "01 January 2024").getSavingsId();
        Assertions.assertNotNull(savingsId);

        savingsAccountHelper.approveSavingsOnDate(savingsId, "01 January 2024");
        savingsAccountHelper.activateSavingsAccount(savingsId, "01 January 2024");
        savingsAccountHelper.depositToSavingsAccount(savingsId, "5000", "01 January 2024");

        SavingsAccountSubStatusEnumData subStatus = savingsAccountHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getNone(), "Savings sub-status should be NONE right after activation");

        // Advance the COB date past the inactivity threshold and run the Savings COB inline for this account only.
        final LocalDate cobDate = activationDate.plusDays(45);
        BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, cobDate.plusDays(1));
        BusinessDateHelper.updateBusinessDate(BusinessDateType.COB_DATE, cobDate);

        executeInlineSavingsCOB(List.of(savingsId));

        subStatus = savingsAccountHelper.getSavingsSubStatus(savingsId);
        Assertions.assertTrue(subStatus.getInactive(), "Savings sub-status should be INACTIVE after inline Savings COB");
    }

    private Long createDormancyTrackingSavingsProduct() {
        // defaultSavingsProduct() already configures daily compounding, monthly posting and daily-balance calculation;
        // here we only add the dormancy thresholds the UpdateSavingsDormantStatusBusinessStep relies on.
        final PostSavingsProductsRequest request = SavingsRequestBuilders.defaultSavingsProduct() //
                .isDormancyTrackingActive(true) //
                .daysToInactive(30) //
                .daysToDormancy(60) //
                .daysToEscheat(90);
        return SavingsProductHelper.createSavingsProduct(request).getResourceId();
    }
}
