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

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.lang.reflect.Type;
import java.util.ArrayList;
import org.apache.fineract.client.models.GetAccountRulesResponse;
import org.apache.fineract.client.models.PostAccountingRulesResponse;
import org.apache.fineract.client.util.JSON;
import org.apache.fineract.integrationtests.common.Utils;

public class AccountRuleHelper {

    private static final Gson GSON = new JSON().getGson();

    private static final String ACCOUNTINGRULES_URL = "/fineract-provider/api/v1/accountingrules?" + Utils.TENANT_IDENTIFIER;

    private final RequestSpecification requestSpec;
    private final ResponseSpecification responseSpec;

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public AccountRuleHelper(final RequestSpecification requestSpec, final ResponseSpecification responseSpec) {
        this.requestSpec = requestSpec;
        this.responseSpec = responseSpec;
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public ArrayList<GetAccountRulesResponse> getAccountingRules() {
        final String response = Utils.performServerGet(this.requestSpec, this.responseSpec, ACCOUNTINGRULES_URL);
        Type accountRuleListType = new TypeToken<ArrayList<GetAccountRulesResponse>>() {}.getType();
        return GSON.fromJson(response, accountRuleListType);
    }

    // TODO: Rewrite to use fineract-client instead!
    // Example: org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper.disburseLoan(java.lang.Long,
    // org.apache.fineract.client.models.PostLoansLoanIdRequest)
    @Deprecated(forRemoval = true)
    public PostAccountingRulesResponse createAccountRule(final Long officeId, final Account accountToCredit, final Account accountToDebit) {
        final String assetAccountJSON = new AccountingRuleBuilder()
                .withGLAccounts(accountToCredit.getAccountID(), accountToDebit.getAccountID()).withOffice(officeId).build();
        final String response = Utils.performServerPost(requestSpec, responseSpec, ACCOUNTINGRULES_URL, assetAccountJSON);
        return GSON.fromJson(response, PostAccountingRulesResponse.class);
    }

}
