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
package org.apache.fineract.integrationtests.common.accounting;

import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import java.util.HashMap;
import org.apache.fineract.client.models.PostGLAccountsRequest;
import org.apache.fineract.client.models.PostGLAccountsResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;

@SuppressWarnings("rawtypes")
public class AccountHelper extends IntegrationTest {

    private static final String CREATE_GL_ACCOUNT_URL = "/fineract-provider/api/v1/glaccounts?" + Utils.TENANT_IDENTIFIER;
    private static final String GL_ACCOUNT_ID_RESPONSE = "resourceId";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public AccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Account createAssetAccount() {
        return this.createAssetAccount(null);
    }

    public Account createIncomeAccount() {
        return this.createIncomeAccount(null);
    }

    public Account createExpenseAccount() {
        return this.createExpenseAccount(null);
    }

    public Account createLiabilityAccount() {
        return this.createLiabilityAccount(null);
    }

    public Account createEquityAccount() {
        return this.createEquityAccount(null);
    }

    public Account createAssetAccount(String accountName) {
        final String assetAccountJSON = new GLAccountBuilder().withName(accountName).withAccountTypeAsAsset().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON,
                GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.ASSET);
    }

    public Account createIncomeAccount(String accountName) {
        final String assetAccountJSON = new GLAccountBuilder().withName(accountName).withAccountTypeAsIncome().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON,
                GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.INCOME);
    }

    public Account createExpenseAccount(String accountName) {
        final String assetAccountJSON = new GLAccountBuilder().withName(accountName).withAccountTypeAsExpense().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GL_ACCOUNT_URL, assetAccountJSON,
                GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.EXPENSE);
    }

    public Account createLiabilityAccount(String accountName) {
        final String liabilityAccountJSON = new GLAccountBuilder().withName(accountName).withAccountTypeAsLiability().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GL_ACCOUNT_URL, liabilityAccountJSON,
                GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.LIABILITY);
    }

    public Account createEquityAccount(String accountName) {
        final String equityAccountJSON = new GLAccountBuilder().withName(accountName).withAccountTypeAsAsEquity().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, CREATE_GL_ACCOUNT_URL, equityAccountJSON,
                GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.EQUITY);
    }

    public ArrayList getAccountingWithRunningBalances() {
        final String GET_RUNNING_BALANCE_URL = "/fineract-provider/api/v1/glaccounts?fetchRunningBalance=true";
        final ArrayList<HashMap> accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec,
                GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }

    public HashMap getAccountingWithRunningBalanceById(final String accountId) {
        final String GET_RUNNING_BALANCE_URL = "/fineract-provider/api/v1/glaccounts/" + accountId + "?fetchRunningBalance=true";
        final HashMap accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }

    public PostGLAccountsResponse createGLAccount(final PostGLAccountsRequest request) {
        return ok(fineract().glAccounts.createGLAccount1(request));
    }

}
