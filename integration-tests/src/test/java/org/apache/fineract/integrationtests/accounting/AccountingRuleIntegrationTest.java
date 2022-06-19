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
package org.apache.fineract.integrationtests.accounting;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.util.ArrayList;
import org.apache.fineract.client.models.GetAccountRulesResponse;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostAccountingRulesResponse;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.AccountRuleHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class AccountingRuleIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;

    private AccountHelper accountHelper;
    private AccountRuleHelper accountRuleHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        accountRuleHelper = new AccountRuleHelper(requestSpec, responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
    }

    @Test
    public void testAccountingRuleCreation() {
        // given
        final Account accountToCredit = accountHelper.createIncomeAccount();
        final Account accountToDebit = accountHelper.createExpenseAccount();
        final GetOfficesResponse headOffice = OfficeHelper.getHeadOffice(requestSpec, responseSpec);

        // when
        final PostAccountingRulesResponse accountingRule = accountRuleHelper.createAccountRule(headOffice.getId(), accountToCredit,
                accountToDebit);
        final ArrayList<GetAccountRulesResponse> accountingRules = accountRuleHelper.getAccountingRules();

        // then
        assertNotNull(accountingRule);
        assertNotNull(accountingRule.getResourceId());
        assertNotNull(accountingRules);
        assertTrue(accountingRules.size() > 0);
    }
}
