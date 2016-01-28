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

import java.util.ArrayList;
import java.util.HashMap;

import org.apache.fineract.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class AccountHelper {

    private final String CREATE_GL_ACCOUNT_URL = "/fineract-provider/api/v1/glaccounts?" + Utils.TENANT_IDENTIFIER;
    private final String GL_ACCOUNT_ID_RESPONSE = "resourceId";

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    public AccountHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    public Account createAssetAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsAsset().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.ASSET);
    }

    public Account createIncomeAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsIncome().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.INCOME);
    }

    public Account createExpenseAccount() {
        final String assetAccountJSON = new GLAccountBuilder().withAccountTypeAsExpense().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                assetAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.EXPENSE);
    }

    public Account createLiabilityAccount() {
        final String liabilityAccountJSON = new GLAccountBuilder().withAccountTypeAsLiability().build();
        final Integer accountID = Utils.performServerPost(this.requestSpec, this.responseSpec, this.CREATE_GL_ACCOUNT_URL,
                liabilityAccountJSON, this.GL_ACCOUNT_ID_RESPONSE);
        return new Account(accountID, Account.AccountType.LIABILITY);
    }
    
    public ArrayList getAccountingWithRunningBalances() {
        final String GET_RUNNING_BALANCE_URL = "/fineract-provider/api/v1/glaccounts?fetchRunningBalance=true";
        final ArrayList<HashMap> accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }
    
    public HashMap getAccountingWithRunningBalanceById(final String accountId) {
        final String GET_RUNNING_BALANCE_URL = "/fineract-provider/api/v1/glaccounts/" + accountId + "?fetchRunningBalance=true";
        final HashMap accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }

}
