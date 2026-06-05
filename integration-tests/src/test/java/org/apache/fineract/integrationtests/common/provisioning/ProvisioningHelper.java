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
package org.apache.fineract.integrationtests.common.provisioning;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;

import java.math.BigDecimal;
import java.security.SecureRandom;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.client.models.LoanProductData;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaResponse;
import org.apache.fineract.client.models.ProvisionEntryRequest;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.client.models.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.client.models.PutProvisioningCriteriaRequest;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;

public final class ProvisioningHelper {

    private static final SecureRandom rand = new SecureRandom();

    private ProvisioningHelper() {}

    public static PostProvisioningCriteriaRequest buildProvisioningCriteriaRequest(List<Integer> loanProductIds,
            List<ProvisioningCategoryData> categories, Account liability, Account expense) {
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        String formattedDate = simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        String criteriaName = "General Provisioning Criteria" + formattedDate + rand.nextLong();

        List<ProvisioningCriteriaDefinitionData> definitions = buildDefinitions(categories, liability, expense);

        return new PostProvisioningCriteriaRequest().criteriaName(criteriaName).loanProducts(buildLoanProducts(loanProductIds))
                .definitions(definitions);
    }

    public static PutProvisioningCriteriaRequest buildUpdateProvisioningCriteriaRequest(List<Integer> loanProductIds,
            List<ProvisioningCategoryData> categories, Account liability, Account expense,
            List<ProvisioningCriteriaDefinitionData> existingDefinitions) {
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        String formattedDate = simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        String criteriaName = "General Provisioning Criteria" + formattedDate + rand.nextLong();

        List<ProvisioningCriteriaDefinitionData> definitions = buildDefinitions(categories, liability, expense);
        // The server requires each definition's existing id to be present in the update payload, so it can
        // match and update the correct row (see ProvisioningCriteria#update). Propagate ids by matching on
        // categoryId, since the new definitions are built fresh and don't carry the persisted id.
        for (ProvisioningCriteriaDefinitionData definition : definitions) {
            existingDefinitions.stream().filter(existing -> existing.getCategoryId().equals(definition.getCategoryId())).findFirst()
                    .ifPresent(existing -> definition.id(existing.getId()));
        }

        return new PutProvisioningCriteriaRequest().criteriaName(criteriaName).loanProducts(buildLoanProducts(loanProductIds))
                .definitions(definitions);
    }

    private static List<LoanProductData> buildLoanProducts(List<Integer> loanProductIds) {
        List<LoanProductData> list = new ArrayList<>();
        for (Integer id : loanProductIds) {
            LoanProductData product = new LoanProductData();
            product.setId(id.longValue());
            list.add(product);
        }
        return list;
    }

    private static List<ProvisioningCriteriaDefinitionData> buildDefinitions(List<ProvisioningCategoryData> categories, Account liability,
            Account expense) {
        List<ProvisioningCriteriaDefinitionData> definitions = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            ProvisioningCategoryData category = categories.get(i);
            definitions.add(new ProvisioningCriteriaDefinitionData().categoryId(category.getId()).categoryName(category.getCategoryName())
                    .minAge((long) ((i * 30) + 1)).maxAge(i == categories.size() - 1 ? 90000L : (long) ((i + 1) * 30))
                    .provisioningPercentage(BigDecimal.valueOf((i + 1) * 5.5)).liabilityAccount(liability.getAccountID().longValue())
                    .expenseAccount(expense.getAccountID().longValue()));
        }
        return definitions;
    }

    public static PostProvisioningCriteriaResponse createProvisioningCriteria(List<Integer> loanProductIds,
            List<ProvisioningCategoryData> categories, Account liability, Account expense) {
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        String formattedDate = simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant()));
        String criteriaName = "General Provisioning Criteria" + formattedDate + rand.nextLong();

        List<ProvisioningCriteriaDefinitionData> definitions = new ArrayList<>();
        for (int i = 0; i < categories.size(); i++) {
            ProvisioningCategoryData category = categories.get(i);
            definitions.add(new ProvisioningCriteriaDefinitionData().categoryId(category.getId()).categoryName(category.getCategoryName())
                    .minAge((long) ((i * 30) + 1)).maxAge(i == categories.size() - 1 ? 90000L : (long) ((i + 1) * 30))
                    .provisioningPercentage(BigDecimal.valueOf((i + 1) * 5.5)).liabilityAccount(liability.getAccountID().longValue())
                    .expenseAccount(expense.getAccountID().longValue()));
        }

        final PostProvisioningCriteriaRequest request = new PostProvisioningCriteriaRequest().criteriaName(criteriaName)
                .loanProducts(buildLoanProducts(loanProductIds)).definitions(definitions);

        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().provisioningCriteria().createProvisioningCriteria(request));
    }

    public static ProvisionEntryRequest createProvisioningEntryRequest() {
        return createProvisioningEntryRequest(false);
    }

    public static ProvisionEntryRequest createProvisioningEntryRequestWithJournalsEnabled() {
        return createProvisioningEntryRequest(true);
    }

    private static ProvisionEntryRequest createProvisioningEntryRequest(boolean createJournalEntries) {
        DateFormat simple = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        return new ProvisionEntryRequest().createjournalentries(createJournalEntries).locale("en").dateFormat("dd MMMM yyyy")
                .date(simple.format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(Utils.getZoneIdOfTenant()).toInstant())));
    }
}
