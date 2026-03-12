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

import static org.assertj.core.api.Assertions.assertThat;

import java.time.LocalDate;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.UUID;
import org.apache.fineract.client.models.DeleteSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PutSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.SavingsAccountData;
import org.apache.fineract.client.util.Calls;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;
import org.junit.jupiter.api.extension.ExtendWith;
import retrofit2.Response;

@ExtendWith({ SavingsTestLifecycleExtension.class })
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
public class SavingsAccountsExternalIdTest extends IntegrationTest {

    private static final String EXTERNAL_ID = UUID.randomUUID().toString();
    private final String dateFormat = "dd MMMM yyyy";
    private final String locale = "en";
    private final String formattedDate = LocalDate.now(ZoneId.systemDefault()).minusDays(5).format(DateTimeFormatter.ofPattern(dateFormat));

    @Test
    @Order(1)
    void submitSavingsAccountsApplication() {
        PostSavingsAccountsRequest request = new PostSavingsAccountsRequest();
        request.setClientId(1L);
        request.setProductId(1L);
        request.setLocale(locale);
        request.setDateFormat(dateFormat);
        request.setSubmittedOnDate(formattedDate);
        request.setExternalId(EXTERNAL_ID);

        Response<PostSavingsAccountsResponse> response = okR(fineractClient().savingsAccounts.submitSavingsApplication(request));
        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    @Order(2)
    void updateSavingsAccountWithExternalId() {
        PutSavingsAccountsAccountIdRequest request = new PutSavingsAccountsAccountIdRequest();
        request.setLocale(locale);
        request.setNominalAnnualInterestRate(5.999);
        Response<PutSavingsAccountsAccountIdResponse> response = okR(
                fineractClient().savingsAccounts.updateSavingsAccountByExternalId(EXTERNAL_ID, request, ""));

        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    @Order(3)
    void approveSavingsAccount() {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.setApprovedOnDate(formattedDate);
        request.setLocale(locale);
        request.setDateFormat(dateFormat);
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineractClient().savingsAccounts.handleCommandsSavingsAccountByExternalId(EXTERNAL_ID, request, "approve"));

        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    @Order(4)
    void retrieveSavingsAccountWithExternalId() {
        LOG.info("------------------------------ RETRIEVING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<SavingsAccountData> response = okR(
                fineractClient().savingsAccounts.retrieveSavingsAccountByExternalId(EXTERNAL_ID, false, null, "all"));

        assertThat(response.isSuccessful()).isTrue();
        assertThat(response.body().getStatus().getCode()).isEqualTo("savingsAccountStatusType.approved");
    }

    @Test
    @Order(5)
    void undoApprovalSavingsAccountWithExternalId() {
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        Response<PostSavingsAccountsAccountIdResponse> response = okR(
                fineractClient().savingsAccounts.handleCommandsSavingsAccountByExternalId(EXTERNAL_ID, request, "undoapproval"));

        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    @Order(6)
    void retrieveSavingsAccountWithExternalIdSecondTime() {
        LOG.info("------------------------------ RETRIEVING SAVINGS ACCOUNT - SECOND TIME ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<SavingsAccountData> response = okR(
                fineractClient().savingsAccounts.retrieveSavingsAccountByExternalId(EXTERNAL_ID, false, null, "all"));

        assertThat(response.isSuccessful()).isTrue();
    }

    @Test
    @Order(7)
    void deleteSavingsAccountWithExternalId() {
        LOG.info("------------------------------ DELETING SAVINGS ACCOUNT ---------------------------------------");
        PostSavingsAccountsAccountIdRequest request = new PostSavingsAccountsAccountIdRequest();
        request.dateFormat(dateFormat);
        request.setLocale(locale);
        request.setActivatedOnDate(formattedDate);
        Response<DeleteSavingsAccountsAccountIdResponse> response = okR(
                fineractClient().savingsAccounts.deleteSavingsAccountByExternalId(EXTERNAL_ID));

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
        Response<SavingsAccountData> response = Calls
                .executeU(fineractClient().savingsAccounts.retrieveSavingsAccountByExternalId(EXTERNAL_ID, false, null, "all"));

        assertThat(response.raw().code()).isEqualTo(404);
    }
}
