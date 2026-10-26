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

import static org.apache.fineract.client.feign.util.FeignCalls.fail;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import java.time.LocalDate;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoanProductsResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostOfficesRequest;
import org.apache.fineract.client.models.PostUsersRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignUserHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.Test;

public class FeignOfficeSpecificLoanProductVisibilityTest extends FeignLoanTestBase {

    private static final long OFFICE_ACCESS_TO_LOAN_PRODUCTS_RELATION_ID = 1L;
    private static final long OFFICE_ACCESS_TO_CHARGES_RELATION_ID = 3L;
    private static final long SUPER_USER_ROLE_ID = 1L;
    private static final String USER_PASSWORD = "QwE!5rTy#9uP0";
    private static final int CHARGE_APPLIES_TO_SAVINGS = 2;
    private static final int CHARGE_APPLIES_TO_CLIENT = 3;

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

    @Test
    void loanProductListingFollowsTheSameRuleForBranchAndHeadOfficeUsers() {
        Long regionOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long branchOfficeId = createOffice(regionOfficeId);
        Long siblingOfficeId = createOffice(regionOfficeId);
        FineractFeignClient branchUser = createUser(branchOfficeId);

        Long unmappedProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long regionProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long branchProductId = createLoanProduct(onePeriod30DaysNoInterest());
        Long siblingProductId = createLoanProduct(onePeriod30DaysNoInterest());
        mapProductToOffice(regionOfficeId, regionProductId);
        mapProductToOffice(branchOfficeId, branchProductId);
        mapProductToOffice(siblingOfficeId, siblingProductId);

        try {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, true);

            Set<Long> branchUserProducts = loanProductIds(branchUser);
            Assertions.assertThat(branchUserProducts).contains(unmappedProductId, regionProductId, branchProductId);
            Assertions.assertThat(branchUserProducts).doesNotContain(siblingProductId);

            Assertions.assertThat(loanProductIds(fineractClient())).contains(unmappedProductId, regionProductId, branchProductId,
                    siblingProductId);
        } finally {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, false);
        }
    }

    @Test
    void chargesFollowTheClientOfficeAndSingleChargeLookupKeepsTheOfficeFilter() {
        Long branchOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long siblingOfficeId = createOffice(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long branchClientId = createClient(branchOfficeId);
        FineractFeignClient branchUser = createUser(branchOfficeId);

        Long unmappedChargeId = createCharge(CHARGE_APPLIES_TO_CLIENT, true);
        Long branchChargeId = createCharge(CHARGE_APPLIES_TO_CLIENT, true);
        Long siblingChargeId = createCharge(CHARGE_APPLIES_TO_CLIENT, true);
        mapChargeToOffice(branchOfficeId, branchChargeId);
        mapChargeToOffice(siblingOfficeId, siblingChargeId);

        try {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, true);

            Set<Long> chargeOptions = ids(FeignRawHttpHelper.get("/clients/" + branchClientId + "/charges/template"), "chargeOptions");
            Assertions.assertThat(chargeOptions).contains(unmappedChargeId, branchChargeId);
            Assertions.assertThat(chargeOptions).doesNotContain(siblingChargeId);

            ok(() -> branchUser.charges().retrieveOneCharge(branchChargeId));
            CallFailedRuntimeException siblingLookup = fail(() -> branchUser.charges().retrieveOneCharge(siblingChargeId));
            Assertions.assertThat(siblingLookup.getStatus()).isEqualTo(404);
        } finally {
            globalConfigurationHelper.updateConfigurationByName(GlobalConfigurationConstants.OFFICE_SPECIFIC_PRODUCTS_ENABLED, false);
        }
    }

    @Test
    void savingsTemplateDoesNotOfferInactiveCharges() {
        Long clientId = createClient(FeignOfficeHelper.HEAD_OFFICE_ID);
        Long activeChargeId = createCharge(CHARGE_APPLIES_TO_SAVINGS, true);
        Long inactiveChargeId = createCharge(CHARGE_APPLIES_TO_SAVINGS, false);

        Set<Long> chargeOptions = ids(FeignRawHttpHelper.get("/savingsaccounts/template?clientId=" + clientId), "chargeOptions");

        Assertions.assertThat(chargeOptions).contains(activeChargeId);
        Assertions.assertThat(chargeOptions).doesNotContain(inactiveChargeId);
    }

    private FineractFeignClient createUser(Long officeId) {
        String username = Utils.uniqueRandomStringGenerator("OfficeUser", 6);
        FeignUserHelper.createUser(new PostUsersRequest().username(username).firstname(Utils.randomFirstNameGenerator())
                .lastname(Utils.randomLastNameGenerator()).email("officeuser@localhost").password(USER_PASSWORD)
                .repeatPassword(USER_PASSWORD).sendPasswordToEmail(false).officeId(officeId).roles(List.of(SUPER_USER_ROLE_ID)));
        return FineractFeignClientHelper.createNewFineractFeignClient(username, USER_PASSWORD);
    }

    private Long createCharge(int chargeAppliesTo, boolean active) {
        String response = FeignRawHttpHelper.post("/charges",
                "{\"name\": \"" + Utils.uniqueRandomStringGenerator("CH_", 9) + "\", \"currencyCode\": \"USD\", \"chargeAppliesTo\": "
                        + chargeAppliesTo
                        + ", \"chargeTimeType\": 2, \"chargeCalculationType\": 1, \"amount\": 10, \"penalty\": false, \"active\": " + active
                        + ", \"locale\": \"en\", \"monthDayFormat\": \"dd MMM\"}");
        return JsonParser.parseString(response).getAsJsonObject().get("resourceId").getAsLong();
    }

    private void mapChargeToOffice(Long officeId, Long chargeId) {
        FeignRawHttpHelper.post("/entitytoentitymapping/" + OFFICE_ACCESS_TO_CHARGES_RELATION_ID,
                "{\"fromId\": " + officeId + ", \"toId\": " + chargeId + "}");
    }

    private Set<Long> loanProductIds(FineractFeignClient client) {
        return ok(() -> client.loanProducts().retrieveAllLoanProducts()).stream().map(GetLoanProductsResponse::getId)
                .collect(Collectors.toSet());
    }

    private Set<Long> ids(String response, String arrayName) {
        Set<Long> ids = new HashSet<>();
        for (JsonElement element : JsonParser.parseString(response).getAsJsonObject().getAsJsonArray(arrayName)) {
            ids.add(element.getAsJsonObject().get("id").getAsLong());
        }
        return ids;
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
        return ids(FeignRawHttpHelper.get("/loans/template?templateType=individual&clientId=" + clientId), "productOptions");
    }
}
