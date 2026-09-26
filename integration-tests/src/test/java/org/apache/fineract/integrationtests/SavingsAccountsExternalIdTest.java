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

import java.math.BigDecimal;
import java.util.UUID;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSavingsProductHelper;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsTestData;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * The eight tests are one ordered chain over a single account, so this cannot extend {@code FeignSavingsTestBase}:
 * {@code FeignSavingsLifecycleExtension} rejects every submitted account after each test, which would take the account
 * away before the next test could act on it. The chain deletes the account itself, so nothing is left behind.
 */
public class SavingsAccountsExternalIdTest extends FeignIntegrationTest {

    private static FeignSavingsHelper savingsHelper;
    private static FeignSavingsProductHelper savingsProductHelper;
    private static FeignClientHelper clientHelper;

    @BeforeAll
    public static void setupHelpers() {
        FineractFeignClient client = FineractFeignClientHelper.getFineractFeignClient();
        savingsHelper = new FeignSavingsHelper(client);
        savingsProductHelper = new FeignSavingsProductHelper(client);
        clientHelper = new FeignClientHelper(client);
    }

    public static final String EXTERNAL_ID = UUID.randomUUID().toString();
    private static final BigDecimal UPDATED_INTEREST_RATE = BigDecimal.valueOf(5.999);

    private final String formattedDate = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());

    @Test
    @Order(1)
    void submitSavingsAccountsApplication() {
        Long clientId = clientHelper.createClient();
        // create a dedicated savings product instead of assuming product id 1 was created by another test class in the
        // same shard: shard membership is round-robin over every test class in the repository, so it changes whenever a
        // test is added anywhere
        Long productId = savingsProductHelper
                .createSavingsProduct(SavingsRequestBuilders.savingsProduct(SavingsTestData.InterestCompoundingPeriodType.DAILY,
                        SavingsTestData.InterestPostingPeriodType.QUARTERLY, SavingsTestData.InterestCalculationType.DAILY_BALANCE))
                .getResourceId();

        Long savingsId = savingsHelper
                .submitApplication(
                        SavingsRequestBuilders.submitSavingsApplication(clientId, productId, formattedDate).externalId(EXTERNAL_ID))
                .getSavingsId();

        assertThat(savingsId).isNotNull();
    }

    @Test
    @Order(2)
    void updateSavingsAccountWithExternalId() {
        PutSavingsAccountsAccountIdRequest request = new PutSavingsAccountsAccountIdRequest()//
                .locale(SavingsTestData.LOCALE)//
                .nominalAnnualInterestRate(UPDATED_INTEREST_RATE.doubleValue());

        assertThat(savingsHelper.updateSavingsByExternalId(EXTERNAL_ID, request)).isNotNull();
    }

    @Test
    @Order(3)
    void approveSavingsAccount() {
        assertThat(savingsHelper.approveSavingsByExternalId(EXTERNAL_ID, formattedDate)).isNotNull();
    }

    @Test
    @Order(4)
    void retrieveSavingsAccountWithExternalId() {
        SavingsAccountData savingsAccount = savingsHelper.getSavingsDetailsByExternalId(EXTERNAL_ID, "all");

        assertThat(savingsAccount).isNotNull();
        assertThat(savingsAccount.getStatus().getCode()).isEqualTo("savingsAccountStatusType.approved");
        assertThat(savingsAccount.getNominalAnnualInterestRate()).isEqualByComparingTo(UPDATED_INTEREST_RATE);
    }

    @Test
    @Order(5)
    void undoApprovalSavingsAccountWithExternalId() {
        assertThat(savingsHelper.undoApprovalByExternalId(EXTERNAL_ID)).isNotNull();
    }

    @Test
    @Order(6)
    void retrieveSavingsAccountWithExternalIdSecondTime() {
        SavingsAccountData savingsAccount = savingsHelper.getSavingsDetailsByExternalId(EXTERNAL_ID, "all");

        assertThat(savingsAccount).isNotNull();
        assertThat(savingsAccount.getStatus().getCode()).isEqualTo("savingsAccountStatusType.submitted.and.pending.approval");
    }

    @Test
    @Order(7)
    void deleteSavingsAccountWithExternalId() {
        assertThat(savingsHelper.deleteSavingsByExternalId(EXTERNAL_ID)).isNotNull();
    }

    @Test
    @Order(8)
    void retrieveSavingsAccountWithExternalIdThirdTime() {
        assertThat(savingsHelper.getSavingsDetailsByExternalIdExpectingError(EXTERNAL_ID).getStatus()).isEqualTo(404);
    }
}
