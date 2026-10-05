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

import java.util.List;
import org.apache.fineract.client.models.AccountingRuleData;
import org.apache.fineract.client.models.GetOfficesResponse;
import org.apache.fineract.client.models.PostAccountingRulesResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.common.OfficeHelper;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountRuleHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class AccountingRuleIntegrationTest extends FeignIntegrationTest {

    private FeignAccountHelper accountHelper;

    @BeforeAll
    public void setup() {
        accountHelper = new FeignAccountHelper(fineractClient());
    }

    @Test
    public void testAccountingRuleCreation() {
        // given
        final Account accountToCredit = accountHelper.createIncomeAccount();
        final Account accountToDebit = accountHelper.createExpenseAccount();
        final GetOfficesResponse headOffice = OfficeHelper.getHeadOffice();

        // when
        final PostAccountingRulesResponse accountingRule = AccountRuleHelper.createAccountRule(headOffice.getId(), accountToCredit,
                accountToDebit);
        final List<AccountingRuleData> accountingRules = AccountRuleHelper.getAccountingRules();
        // then
        assertNotNull(accountingRule);
        assertNotNull(accountingRule.getResourceId());
        assertNotNull(accountingRules);
        assertTrue(accountingRules.size() > 0);
    }
}
