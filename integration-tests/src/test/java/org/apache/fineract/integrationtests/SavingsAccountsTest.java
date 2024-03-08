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
import java.time.LocalDate;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.GetSavingsAccountsResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostClientsRequest;
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
public class SavingsAccountsTest extends IntegrationTest {

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


        PostSavingsAccountsRequest request = buildPostSavingsAccountRequest(1);


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
    void getSavingsAccountsByBirthday() {
        LOG.info("------------------------------ GET SAVINGS ACCOUNT BY BIRTH DAY ---------------------------------------");
        int year = 1994, month = 12, day = 2;

        PostClientsRequest clientsRequest = new PostClientsRequest();
        clientsRequest.setDateOfBirth(LocalDate.of(year, month, day));
        clientsRequest.setFirstname("Alan");
        clientsRequest.setLastname("Wake");
        clientsRequest.setOfficeId(1);
        clientsRequest.setActive(true);
        clientsRequest.setActivationDate("04 March 2009");
        Response<PostClientsResponse> clientResponse = okR(
                fineract().clients.create6(clientsRequest)
        );

        assertThat(clientResponse.isSuccessful()).isTrue();
        long clientId = clientResponse.body().getClientId();
        assertThat(clientId).isNotNull();

        PostSavingsAccountsRequest postSavingsRequest = buildPostSavingsAccountRequest((int) clientId);

        Response<PostSavingsAccountsResponse> postSavingsResponse = okR(
                fineract().savingsAccounts.submitApplication2(postSavingsRequest)
        );

        assertThat(postSavingsResponse.isSuccessful()).isTrue();

        Response<GetSavingsAccountsResponse> getSavingsResponse = okR(
                fineract().savingsAccounts.retrieveAll33(
                        null,
                        null,
                        null,
                        null,
                        null,
                        null,
                        String.format("%d-0%d", month, day))
        );

        assertThat(getSavingsResponse.isSuccessful()).isTrue();

        GetSavingsAccountsResponse body = getSavingsResponse.body();

        assertThat(body).isNotNull();
        assertThat(body.getTotalFilteredRecords()).isEqualTo(1);
    }

    PostSavingsAccountsRequest buildPostSavingsAccountRequest(int clientId) {
        PostSavingsAccountsRequest request = new PostSavingsAccountsRequest();
        request.setClientId(clientId);
        request.setProductId(1);
        request.setLocale(locale);
        request.setDateFormat(dateFormat);
        request.submittedOnDate(formattedDate);
        return request;
    }

}
