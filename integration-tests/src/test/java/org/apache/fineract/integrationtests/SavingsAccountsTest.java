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

import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Integration Test for /savingsaccounts API.
 * <p>
 * The three tests are one ordered chain over a single account, so this cannot extend {@code FeignSavingsTestBase}:
 * {@code FeignSavingsLifecycleExtension} rejects every submitted account after each test, which would take the account
 * away before the next test could approve it. It closes its own account instead.
 *
 * @author Danish Jamal
 *
 */
public class SavingsAccountsTest extends FeignIntegrationTest {

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignClientHelper clientHelper;

    // static: JUnit uses a new test instance per method, so instance fields do not carry between the ordered tests
    private static Long savingsId;

    private final String formattedDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

    @BeforeAll
    public static void setupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        clientHelper = new FeignClientHelper(client);
    }

    @AfterAll
    public static void closeAccount() {
        if (savingsId != null) {
            savingsHelper.closeSavings(savingsId, Utils.dateFormatter.format(Utils.getLocalDateOfTenant()), true);
        }
    }

    @Test
    @Order(1)
    void submitSavingsAccountsApplication() {
        // create a dedicated active client and savings product instead of relying on entities created by other test
        // classes in the same shard
        Long clientId = clientHelper.createClient();
        Long productId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.QUARTERLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                .getResourceId();

        savingsId = savingsHelper.submitApplication(clientId, productId, formattedDate).getSavingsId();

        assertThat(savingsId).isNotNull();
    }

    @Test
    @Order(2)
    void approveSavingsAccount() {
        assertThat(savingsHelper.approveSavings(savingsId, formattedDate)).isNotNull();
    }

    @Test
    @Order(3)
    void activateSavingsAccount() {
        assertThat(savingsHelper.activateSavings(savingsId, formattedDate)).isNotNull();
    }
}
