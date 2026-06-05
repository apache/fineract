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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaResponse;
import org.apache.fineract.client.models.PostProvisioningEntriesResponse;
import org.apache.fineract.client.models.ProvisionEntryRequest;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.client.models.ProvisioningEntryData;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningTransactionHelper;
import org.junit.jupiter.api.Test;

public class FeignProvisioningEntryTest extends FeignLoanTestBase {

    @Test
    public void testRetrieveProvisioningEntryWithNoActiveLoansDoesNotReturn500() {

        // Create a loan product to satisfy criteria validation,
        // but do NOT disburse any loans — so m_loanproduct_provisioning_entry
        // will be empty, which is the scenario that used to cause HTTP 500.
        Long loanProductId = createLoanProduct(onePeriod30DaysNoInterest());
        assertNotNull(loanProductId);

        ArrayList<Integer> loanProducts = new ArrayList<>();
        loanProducts.add(loanProductId.intValue());

        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();
        List<ProvisioningCategoryData> categories = transactionHelper.retrieveAllProvisioningCategories();

        Account liability = accountHelper.createLiabilityAccount("Liability_" + Utils.randomStringGenerator("", 5));
        Account expense = accountHelper.createExpenseAccount("Expense_" + Utils.randomStringGenerator("", 5));

        PostProvisioningCriteriaRequest criteriaRequest = ProvisioningHelper.buildProvisioningCriteriaRequest(loanProducts, categories,
                liability, expense);
        PostProvisioningCriteriaResponse createdCriteria = transactionHelper.createProvisioningCriteria(criteriaRequest);
        assertNotNull(createdCriteria.getResourceId());
        Long criteriaId = createdCriteria.getResourceId();

        // Create the provisioning entry
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        ProvisionEntryRequest request = new ProvisionEntryRequest().date(today).dateFormat("dd MMMM yyyy").locale("en")
                .createjournalentries(false);
        PostProvisioningEntriesResponse created = ok(() -> fineractClient().provisioningEntries().createProvisioningEntries(request));
        assertNotNull(created);
        assertNotNull(created.getResourceId());

        // This GET used to throw 500 when no loans were disbursed (empty join result).
        // The LEFT JOIN fix ensures it now returns 200 with totalReserved = null/0.
        ProvisioningEntryData entry = ok(
                () -> fineractClient().provisioningEntries().retrieveOneProvisioningEntry(created.getResourceId()));
        assertNotNull(entry);
        assertNotNull(entry.getId());

        // Cleanup
        transactionHelper.deleteProvisioningCriteria(criteriaId);
    }
}
