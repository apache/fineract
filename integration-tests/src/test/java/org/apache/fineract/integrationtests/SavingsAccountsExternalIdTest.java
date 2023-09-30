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

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.fineract.client.models.DeleteSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.GetSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import retrofit2.Response;

public class SavingsAccountsExternalIdTest extends IntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsAccountsExternalIdTest.class);
    public static final String EXTERNAL_ID = UUID.randomUUID().toString();
    private final String dateFormat = "dd MMMM yyyy";
    private final String locale = "en";
    private final String formattedDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5).format(DateTimeFormatter.ofPattern(dateFormat));

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
        request.setExternalId(EXTERNAL_ID);

        Response<PostSavingsAccountsResponse> response = okR(fineract().savingsAccounts.submitApplication2(request));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(2)
    void updateSavingsAccountWithExternalId() {
        LOG.info("------------------------------ UPDATING SAVINGS ACCOUNT ---------------------------------------");
        PutSavingsAccountsAccountIdRequest request = new PutSavingsAccountsAccountIdRequest();
        request.setLocale(locale);
        request.setNominalAnnualInterestRate(5.999);
        Response<PutSavingsAccountsAccountIdResponse> response = okR(fineract().savingsAccounts.update21(EXTERNAL_ID, request, ""));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(3)
    void approveSavingsAccount() {
        LOG.info("------------------------------ APPROVING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setApprovedOnDate(formattedDate);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineract().savingsAccounts.handleCommands7(EXTERNAL_ID, request, "approve"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(4)
    void retrieveSavingsAccountWithExternalId() {
        LOG.info("------------------------------ RETRIEVING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<GetSavingsAccountsAccountIdResponse> response = okR(fineract().savingsAccounts.retrieveOne26(EXTERNAL_ID, false, "all"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getStatus().getCode()).isEqualTo("savingsAccountStatusType.approved");
        assertThat(response.body().getNominalAnnualInterestRate()).isEqualTo(5.999);
    }

    @Test
    @Order(5)
    void undoApprovalSavingsAccountWithExternalId() {
        LOG.info("------------------------------ UNDO APPROVAL SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineract().savingsAccounts.handleCommands7(EXTERNAL_ID, request, "undoapproval"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(6)
    void retrieveSavingsAccountWithExternalIdSecondTime() {
        LOG.info("------------------------------ RETRIEVING SAVINGS ACCOUNT - SECOND TIME ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<GetSavingsAccountsAccountIdResponse> response = okR(fineract().savingsAccounts.retrieveOne26(EXTERNAL_ID, false, "all"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
        assertThat(response.body().getStatus().getCode()).isEqualTo("savingsAccountStatusType.submitted.and.pending.approval");
    }

    @Test
    @Order(7)
    void deleteSavingsAccountWithExternalId() {
        LOG.info("------------------------------ DELETING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<DeleteSavingsAccountsAccountIdResponse> response = okR(fineract().savingsAccounts.delete20(EXTERNAL_ID));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body()).isNotNull();
    }

    @Test
    @Order(8)
    void retrieveSavingsAccountWithExternalIdThirdTime() {
        LOG.info("------------------------------ RETRIEVING SAVINGS ACCOUNT - THIRD TIME ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<GetSavingsAccountsAccountIdResponse> response = Calls
                .executeU(fineract().savingsAccounts.retrieveOne26(EXTERNAL_ID, false, "all"));

        assertThat(response.raw().code()).isEqualTo(404);
    }
}
