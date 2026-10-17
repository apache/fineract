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

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.Set;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeignOfficeSpecificLoanProductVisibilityTest extends FeignLoanTestBase {

    private static final long OFFICE_ACCESS_TO_LOAN_PRODUCTS_RELATION_ID = 1L;

    @Test
    void loanTemplateListsUnmappedProductsAndProductsMappedToTheClientOfficeOrItsAncestors() {
        Long regionOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long branchOfficeId = createOffice(regionOfficeId);
        Long siblingOfficeId = createOffice(regionOfficeId);
        Long branchClientId = createClient(branchOfficeId);

        Long unmappedProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long regionProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long branchProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long siblingProductId = createLoanProduct(onePeriod30DaysNoInterest());
        mapProductToOffice(regionOfficeId, regionProductId);
        mapProductToOffice(branchOfficeId, branchProductId);
        mapProductToOffice(siblingOfficeId, siblingProductId);

        try {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, true);

            Set<Long> productOptions = loanTemplateProductOptions(branchClientId);

            Assertions.assertThat(productOptions).contains(unmappedProductId, regionProductId, branchProductId);
            Assertions.assertThat(productOptions).doesNotContain(siblingProductId);
        } finally {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, false);
        }
    }

    @Test
    void loanTemplateListsEveryProductWhenOfficeSpecificProductsAreDisabled() {
        Long branchOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long siblingOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long branchClientId = createClient(branchOfficeId);
        Long siblingProductId = createLoanProduct(onePeriod30DaysNoInterest());
        mapProductToOffice(siblingOfficeId, siblingProductId);

        Assertions.assertThat(loanTemplateProductOptions(branchClientId)).contains(siblingProductId);
    }

    private Long createOffice(Long parentId) {
        return ok(() -> fineractClient().offices()
                .createOffice(new PostOfficesRequest().parentId(parentId).name(Utils.uniqueRandomStringGenerator("O_", 9))
                        .openingDate(LocalDate.of(2010, 1, 1)).dateFormat("yyyy-MM-dd").locale("en")))
                .getOfficeId();
    }

    private Long createClient(Long officeId) {
        return clientHelper.createClient(new PostClientsRequest().officeId(officeId).legalFormId(1L)
                .firstname(Utils.randomFirstNameGenerator()).lastname(Utils.randomLastNameGenerator()).active(true)
                .activationDate(ClientHelper.DEFAULT_DATE).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE))
                .getClientId();
    }

    private void mapProductToOffice(Long officeId, Long productId) {
        FeignRawHttpHelper.post("/entitytoentitymapping/" + OFFICE_ACCESS_TO_LOAN_PRODUCTS_RELATION_ID,
                "{\"fromId\": " + officeId + ", \"toId\": " + productId + "}");
    }

    private Set<Long> loanTemplateProductOptions(Long clientId) {
        String response = FeignRawHttpHelper.get("/loans/template?templateType=individual&clientId=" + clientId);
        Set<Long> productIds = new HashSet<>();
        for (JsonElement product : JsonParser.parseString(response).getAsJsonObject().getAsJsonArray("productOptions")) {
            productIds.add(product.getAsJsonObject().get("id").getAsLong());
        }
        return productIds;
    }
}
