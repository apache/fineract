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
package org.apache.fineract.integrationtests.common;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.CommandProcessingResult;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaResponse;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

/**
 * Covers {@code DELETE /provisioningcategory/{id}} — see FINERACT-2653. The endpoint was broken in several ways
 * (create-validation run on a body-less DELETE, a transient entity with a null id, and an in-use check querying a
 * non-existent table); these tests exercise both the success and the in-use-rejected paths end to end.
 */
public class ProvisioningCategoryDeleteIntegrationTest {

    private AccountHelper accountHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        var requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        var responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
    }

    @Test
    public void testDeleteUnusedProvisioningCategorySucceeds() {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();

        final String categoryName = Utils.randomStringGenerator("PROV_CAT_", 6);
        CommandProcessingResult createResult = transactionHelper.createProvisioningCategory("{\"categoryname\":\"" + categoryName + "\"}");
        assertNotNull(createResult.getResourceId());
        final Long categoryId = createResult.getResourceId();

        // An unused category must delete cleanly.
        CommandProcessingResult deleteResult = transactionHelper.deleteProvisioningCategory(categoryId);
        Assertions.assertEquals(categoryId, deleteResult.getResourceId());

        List<ProvisioningCategoryData> categories = transactionHelper.retrieveAllProvisioningCategories();
        for (ProvisioningCategoryData category : categories) {
            Assertions.assertNotEquals(categoryId, category.getId());
        }
    }

    @Test
    public void testDeleteProvisioningCategoryInUseIsRejected() {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();

        final String categoryName = Utils.randomStringGenerator("PROV_CAT_", 6);
        CommandProcessingResult createResult = transactionHelper.createProvisioningCategory("{\"categoryname\":\"" + categoryName + "\"}");
        assertNotNull(createResult.getResourceId());
        final Long categoryId = createResult.getResourceId();

        final Integer loanProductId = createLoanProduct();
        assertNotNull(loanProductId);
        final List<Integer> loanProducts = new ArrayList<>();
        loanProducts.add(loanProductId);
        final Account liability = accountHelper.createLiabilityAccount();
        final Account expense = accountHelper.createExpenseAccount();

        // Reference only the fresh category under test.
        List<ProvisioningCategoryData> allCategories = transactionHelper.retrieveAllProvisioningCategories();
        List<ProvisioningCategoryData> categoriesUnderTest = allCategories.stream().filter(c -> categoryId.equals(c.getId())).toList();

        PostProvisioningCriteriaRequest criteriaRequest = ProvisioningHelper.buildProvisioningCriteriaRequest(loanProducts,
                categoriesUnderTest, liability, expense);
        PostProvisioningCriteriaResponse criteriaResponse = transactionHelper.createProvisioningCriteria(criteriaRequest);
        assertNotNull(criteriaResponse.getResourceId());

        // Delete must be rejected with the domain-rule violation (HTTP 403).
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> transactionHelper.deleteProvisioningCategory(categoryId));
        Assertions.assertEquals(403, exception.getStatus());
        Assertions.assertTrue(exception.getDeveloperMessage().contains("cannot be deleted, it is already used in loan product"));

        // Once the referencing criteria is removed, the category can be deleted.
        transactionHelper.deleteProvisioningCriteria(criteriaResponse.getResourceId());
        CommandProcessingResult deleteResult = transactionHelper.deleteProvisioningCategory(categoryId);
        Assertions.assertEquals(categoryId, deleteResult.getResourceId());
    }

    private Integer createLoanProduct() {
        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("100000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(false) //
                .withAccountingRuleAsNone() //
                .build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }
}
