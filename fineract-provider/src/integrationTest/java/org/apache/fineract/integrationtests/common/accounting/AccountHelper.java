/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.mifosplatform.integrationtests.common.accounting;

import java.util.ArrayList;
import java.util.HashMap;

import org.mifosplatform.integrationtests.common.Utils;

import com.jayway.restassured.specification.RequestSpecification;
import com.jayway.restassured.specification.ResponseSpecification;

@SuppressWarnings("rawtypes")
public class AccountHelper {

    private final String CREATE_GL_ACCOUNT_URL = "/mifosng-provider/api/v1/glaccounts?" + Utils.TENANT_IDENTIFIER;
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
        final String GET_RUNNING_BALANCE_URL = "/mifosng-provider/api/v1/glaccounts?fetchRunningBalance=true";
        final ArrayList<HashMap> accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }
    
    public HashMap getAccountingWithRunningBalanceById(final String accountId) {
        final String GET_RUNNING_BALANCE_URL = "/mifosng-provider/api/v1/glaccounts/" + accountId + "?fetchRunningBalance=true";
        final HashMap accountRunningBalance = Utils.performServerGet(this.requestSpec, this.responseSpec, GET_RUNNING_BALANCE_URL, "");
        return accountRunningBalance;
    }

}
