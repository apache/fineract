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
package org.apache.fineract.test.stepdef.saving;

import io.cucumber.java.en.And;
import java.io.IOException;
import java.math.BigDecimal;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdResponse;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsAccountsResponse;
import org.apache.fineract.client.services.SavingsAccountApi;
import org.apache.fineract.client.services.SavingsAccountTransactionsApi;
import org.apache.fineract.test.factory.SavingsAccountRequestFactory;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import org.springframework.beans.factory.annotation.Autowired;
import retrofit2.Response;

public class SavingsAccountStepDef extends AbstractStepDef {

    @Autowired
    private SavingsAccountTransactionsApi savingsAccountTransactionsApi;

    @Autowired
    private SavingsAccountApi savingsAccountApi;

    @And("Client creates a new EUR savings account with {string} submitted on date")
    public void createSavingsAccountEUR(String submittedOnDate) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.body().getClientId();

        PostSavingsAccountsRequest createSavingsAccountRequest = SavingsAccountRequestFactory.defaultEURSavingsAccountRequest()
                .clientId((int) clientId).submittedOnDate(submittedOnDate);

        Response<PostSavingsAccountsResponse> createSavingsAccountResponse = savingsAccountApi
                .submitApplication2(createSavingsAccountRequest).execute();
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE, createSavingsAccountResponse);
    }

    @And("Client creates a new USD savings account with {string} submitted on date")
    public void createSavingsAccountUSD(String submittedOnDate) throws IOException {
        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        long clientId = clientResponse.body().getClientId();

        PostSavingsAccountsRequest createSavingsAccountRequest = SavingsAccountRequestFactory.defaultUSDSavingsAccountRequest()
                .clientId((int) clientId).submittedOnDate(submittedOnDate);

        Response<PostSavingsAccountsResponse> createSavingsAccountResponse = savingsAccountApi
                .submitApplication2(createSavingsAccountRequest).execute();
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE, createSavingsAccountResponse);
    }

    @And("Approve EUR savings account on {string} date")
    public void approveEurSavingsAccount(String approvedOnDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountsAccountIdRequest approveSavingsAccountRequest = SavingsAccountRequestFactory.defaultApproveRequest()
                .approvedOnDate(approvedOnDate);

        Response<PostSavingsAccountsAccountIdResponse> approveSavingsAccountResponse = savingsAccountApi
                .handleCommands6(savingsAccountID, approveSavingsAccountRequest, "approve").execute();
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_APPROVE_RESPONSE, approveSavingsAccountResponse);
    }

    @And("Approve USD savings account on {string} date")
    public void approveUsdSavingsAccount(String approvedOnDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountsAccountIdRequest approveSavingsAccountRequest = SavingsAccountRequestFactory.defaultApproveRequest()
                .approvedOnDate(approvedOnDate);

        Response<PostSavingsAccountsAccountIdResponse> approveSavingsAccountResponse = savingsAccountApi
                .handleCommands6(savingsAccountID, approveSavingsAccountRequest, "approve").execute();
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_APPROVE_RESPONSE, approveSavingsAccountResponse);
    }

    @And("Activate EUR savings account on {string} date")
    public void activateSavingsAccount(String activatedOnDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountsAccountIdRequest activateSavingsAccountRequest = SavingsAccountRequestFactory.defaultActivateRequest()
                .activatedOnDate(activatedOnDate);

        Response<PostSavingsAccountsAccountIdResponse> activateSavingsAccountResponse = savingsAccountApi
                .handleCommands6(savingsAccountID, activateSavingsAccountRequest, "activate").execute();
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE, activateSavingsAccountResponse);
    }

    @And("Activate USD savings account on {string} date")
    public void activateUsdSavingsAccount(String activatedOnDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountsAccountIdRequest activateSavingsAccountRequest = SavingsAccountRequestFactory.defaultActivateRequest()
                .activatedOnDate(activatedOnDate);

        Response<PostSavingsAccountsAccountIdResponse> activateSavingsAccountResponse = savingsAccountApi
                .handleCommands6(savingsAccountID, activateSavingsAccountRequest, "activate").execute();
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_ACTIVATED_RESPONSE, activateSavingsAccountResponse);
    }

    @And("Client successfully deposits {double} EUR to the savings account on {string} date")
    public void createEurDeposit(double depositAmount, String depositDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountTransactionsRequest depositRequest = SavingsAccountRequestFactory.defaultDepositRequest()
                .transactionDate(depositDate).transactionAmount(BigDecimal.valueOf(depositAmount));

        Response<PostSavingsAccountTransactionsResponse> depositResponse = savingsAccountTransactionsApi
                .transaction2(savingsAccountID, depositRequest, "deposit").execute();
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE, depositResponse);
    }

    @And("Client successfully deposits {double} USD to the savings account on {string} date")
    public void createUsdDeposit(double depositAmount, String depositDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountTransactionsRequest depositRequest = SavingsAccountRequestFactory.defaultDepositRequest()
                .transactionDate(depositDate).transactionAmount(BigDecimal.valueOf(depositAmount));

        Response<PostSavingsAccountTransactionsResponse> depositResponse = savingsAccountTransactionsApi
                .transaction2(savingsAccountID, depositRequest, "deposit").execute();
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_DEPOSIT_RESPONSE, depositResponse);
    }

    @And("Client successfully withdraw {double} EUR from the savings account on {string} date")
    public void createEurWithdraw(double withdrawAmount, String transcationDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.EUR_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountTransactionsRequest withdrawRequest = SavingsAccountRequestFactory.defaultWithdrawRequest()
                .transactionDate(transcationDate).transactionAmount(BigDecimal.valueOf(withdrawAmount));

        Response<PostSavingsAccountTransactionsResponse> withdrawalResponse = savingsAccountTransactionsApi
                .transaction2(savingsAccountID, withdrawRequest, "withdrawal").execute();
        testContext().set(TestContextKey.EUR_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE, withdrawalResponse);
    }

    @And("Client successfully withdraw {double} USD from the savings account on {string} date")
    public void createUsdWithdraw(double withdrawAmount, String transcationDate) throws IOException {
        Response<PostSavingsAccountsResponse> savingsAccountResponse = testContext()
                .get(TestContextKey.USD_SAVINGS_ACCOUNT_CREATE_RESPONSE);
        long savingsAccountID = savingsAccountResponse.body().getSavingsId();

        PostSavingsAccountTransactionsRequest withdrawRequest = SavingsAccountRequestFactory.defaultWithdrawRequest()
                .transactionDate(transcationDate).transactionAmount(BigDecimal.valueOf(withdrawAmount));

        Response<PostSavingsAccountTransactionsResponse> withdrawalResponse = savingsAccountTransactionsApi
                .transaction2(savingsAccountID, withdrawRequest, "withdrawal").execute();
        testContext().set(TestContextKey.USD_SAVINGS_ACCOUNT_WITHDRAW_RESPONSE, withdrawalResponse);
    }
}
