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

import java.text.SimpleDateFormat;
import java.util.Date;
import org.apache.fineract.client.models.GetSavingsAccountsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

/**
 * Integration Test for /savingsaccounts API.
 *
 * @author Danish Jamal
 *
 */
class SavingsAccountsTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountsTest.class);
    private final String dateFormat = "dd MMMM yyyy";
    private final String locale = "en";
    private final SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd MMM yyyy");
    private final String formattedDate = simpleDateFormat.format(new Date());
    private int savingId = 1;

    @Test
    @Order(1)
    void submitSavingsAccountsApplication() {
        LOG.info("------------------------------ CREATING NEW SAVINGS ACCOUNT APPLICATION ---------------------------------------");
        PostSavingsAccountsRequest request = new PostSavingsAccountsRequest();
        request.setClientId(1);
        request.setProductId(1);
        request.setLocale(locale);
        request.setDateFormat(dateFormat);
        request.submittedOnDate(formattedDate);

        Response<PostSavingsAccountsResponse> response = okR(fineract().savingsAccounts.submitApplication2(request));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        savingId = response.body().getSavingsId();
    }

    @Test
    @Order(2)
    void approveSavingsAccount() {
        LOG.info("------------------------------ APPROVING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setApprovedOnDate(formattedDate);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineract().savingsAccounts.handleCommands6((long) savingId, request, "approve"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(3)
    void activateSavingsAccount() {
        LOG.info("------------------------------ ACTIVATING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineract().savingsAccounts.handleCommands6((long) savingId, request, "activate"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(4)
    void retrieveSavingsAccountsByBirthday_Success() {
        LOG.info("------------------------------ RETRIEVE SAVINGS ACCOUNTS BY BIRTHDAY -----------------------------");

        // This test assumes test data already exists
        int month = 4;
        int day = 3;

        Response<GetSavingsAccountsResponse> response = okR(
                fineract().savingsAccounts.retrieveAll33(null, null, 0, 10, null, null, month, day));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getPageItems()).isNotEmpty();

        response.body().getPageItems().forEach(account -> {
            LOG.info("Savings Account ID: {}, Client: {}", account.getId(), account.getClientName());
        });
    }

    @Test
    @Order(4)
    void retrieveSavingsAccountsByBirthday_EmptySet() {
        LOG.info("------------------------------ RETRIEVE SAVINGS ACCOUNTS BY BIRTHDAY -----------------------------");

        // Data does not exist for these dates
        int month = 13;
        int day = 32;

        Response<GetSavingsAccountsResponse> response = okR(
                fineract().savingsAccounts.retrieveAll33(null, null, 0, 10, null, null, month, day));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getPageItems()).isEmpty();
    }
}
