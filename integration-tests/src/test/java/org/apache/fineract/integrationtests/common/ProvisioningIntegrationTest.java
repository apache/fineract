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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import org.apache.fineract.client.models.GetProvisioningCriteriaCriteriaIdResponse;
import org.apache.fineract.client.models.PageLoanProductProvisioningEntryData;
import org.apache.fineract.client.models.PageProvisioningEntryData;
import org.apache.fineract.client.models.PostProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PostProvisioningCriteriaResponse;
import org.apache.fineract.client.models.PostProvisioningEntriesResponse;
import org.apache.fineract.client.models.ProvisionEntryRequest;
import org.apache.fineract.client.models.ProvisioningCategoryData;
import org.apache.fineract.client.models.ProvisioningCriteriaDefinitionData;
import org.apache.fineract.client.models.ProvisioningEntryData;
import org.apache.fineract.client.models.PutProvisioningCriteriaRequest;
import org.apache.fineract.client.models.PutProvisioningCriteriaResponse;
import org.apache.fineract.client.models.PutProvisioningEntriesRequest;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class ProvisioningIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningIntegrationTest.class);
    private static final String NONE = "1";
    private static final int LOANPRODUCTS_SIZE = 2;

    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private AccountHelper accountHelper;
    private LoanTransactionHelper loanTransactionHelper;

    @BeforeEach
    public void setup() throws ParseException {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        Assumptions.assumeTrue(!isAlreadyProvisioningEntriesCreated());
    }

    @Test
    public void testCreateProvisioningCriteria() {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();
        ArrayList<Integer> loanProducts = new ArrayList<>(LOANPRODUCTS_SIZE);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        for (int i = 0; i < LOANPRODUCTS_SIZE; i++) {
            final Integer loanProductID = createLoanProduct(false, NONE);
            loanProducts.add(loanProductID);
            Assertions.assertNotNull(loanProductID);
            List<HashMap> collaterals = new ArrayList<>();
            final Integer collateralId = CollateralManagementHelper.createCollateralProduct(this.requestSpec, this.responseSpec);
            Assertions.assertNotNull(collateralId);
            final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(this.requestSpec, this.responseSpec,
                    String.valueOf(clientID), collateralId);
            Assertions.assertNotNull(clientCollateralId);
            addCollaterals(collaterals, clientCollateralId, BigDecimal.valueOf(1));
            final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00", collaterals);
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
            loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
            loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount("20 September 2011", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
            Assertions.assertNotNull(loanID);
        }

        List<ProvisioningCategoryData> categories = transactionHelper.retrieveAllProvisioningCategories();
        Assertions.assertTrue(categories.size() > 0);
        Account liability = accountHelper.createLiabilityAccount();
        Account expense = accountHelper.createExpenseAccount();
        PostProvisioningCriteriaRequest criteriaRequest = ProvisioningHelper.buildProvisioningCriteriaRequest(loanProducts, categories,
                liability, expense);
        PostProvisioningCriteriaResponse createdCriteria = transactionHelper.createProvisioningCriteria(criteriaRequest);
        Assertions.assertNotNull(createdCriteria.getResourceId());
        Long criteriaId = createdCriteria.getResourceId();

        GetProvisioningCriteriaCriteriaIdResponse newCriteria = transactionHelper.retrieveProvisioningCriteria(criteriaId);
        validateProvisioningCriteria(criteriaRequest, newCriteria);

        PutProvisioningCriteriaRequest updateRequest = ProvisioningHelper.buildUpdateProvisioningCriteriaRequest(loanProducts, categories,
                liability, expense, newCriteria.getDefinitions());
        PutProvisioningCriteriaResponse updatedCriteriaResponse = transactionHelper.updateProvisioningCriteria(criteriaId, updateRequest);
        GetProvisioningCriteriaCriteriaIdResponse updatedCriteria = transactionHelper
                .retrieveProvisioningCriteria(updatedCriteriaResponse.getResourceId());
        validateProvisioningCriteria(updateRequest, updatedCriteria);

        transactionHelper.deleteProvisioningCriteria(criteriaId);

        categories = transactionHelper.retrieveAllProvisioningCategories();
        liability = accountHelper.createLiabilityAccount();
        expense = accountHelper.createExpenseAccount();
        criteriaRequest = ProvisioningHelper.buildProvisioningCriteriaRequest(loanProducts, categories, liability, expense);
        createdCriteria = transactionHelper.createProvisioningCriteria(criteriaRequest);
        Assertions.assertNotNull(createdCriteria.getResourceId());
        criteriaId = createdCriteria.getResourceId();

        ProvisionEntryRequest provisioningEntryRequest = ProvisioningHelper.createProvisioningEntryRequest();
        PostProvisioningEntriesResponse createdEntry = transactionHelper.createProvisioningEntries(provisioningEntryRequest);
        Long provisioningEntryId = createdEntry.getResourceId();
        Assertions.assertNotNull(provisioningEntryId);

        transactionHelper.updateProvisioningEntry("recreateprovisioningentry", provisioningEntryId, new PutProvisioningEntriesRequest());
        transactionHelper.updateProvisioningEntry("createjournalentry", provisioningEntryId, new PutProvisioningEntriesRequest());
        ProvisioningEntryData entry = transactionHelper.retrieveProvisioningEntry(provisioningEntryId);
        Assertions.assertTrue(entry.getJournalEntry());
        PageLoanProductProvisioningEntryData provisioningEntry = transactionHelper.retrieveProvisioningEntries(provisioningEntryId);
        Assertions.assertTrue(provisioningEntry.getPageItems().size() > 0);
    }

    private HashMap<String, String> collaterals(Integer collateralId, BigDecimal quantity) {
        HashMap<String, String> collateral = new HashMap<String, String>(2);
        collateral.put("clientCollateralId", collateralId.toString());
        collateral.put("quantity", quantity.toString());
        return collateral;
    }

    private void addCollaterals(List<HashMap> collaterals, Integer collateralId, BigDecimal quantity) {
        collaterals.add(collaterals(collateralId, quantity));
    }

    private void validateProvisioningCriteria(PostProvisioningCriteriaRequest request, GetProvisioningCriteriaCriteriaIdResponse response) {
        Assertions.assertEquals(request.getCriteriaName(), response.getCriteriaName());
        Assertions.assertEquals(request.getLoanProducts().size(), response.getLoanProducts().size());
        List<ProvisioningCriteriaDefinitionData> requestDefinitions = request.getDefinitions();
        List<ProvisioningCriteriaDefinitionData> responseDefinitions = response.getDefinitions();
        Assertions.assertEquals(requestDefinitions.size(), responseDefinitions.size());
        for (ProvisioningCriteriaDefinitionData requestDef : requestDefinitions) {
            boolean found = responseDefinitions.stream().anyMatch(d -> d.getCategoryId().equals(requestDef.getCategoryId()));
            if (!found) {
                Assertions.fail("No Category found with Id:" + requestDef.getCategoryId());
            }
        }
    }

    private void validateProvisioningCriteria(PutProvisioningCriteriaRequest request, GetProvisioningCriteriaCriteriaIdResponse response) {
        Assertions.assertEquals(request.getCriteriaName(), response.getCriteriaName());
        Assertions.assertEquals(request.getLoanProducts().size(), response.getLoanProducts().size());
        List<ProvisioningCriteriaDefinitionData> requestDefinitions = request.getDefinitions();
        List<ProvisioningCriteriaDefinitionData> responseDefinitions = response.getDefinitions();
        Assertions.assertEquals(requestDefinitions.size(), responseDefinitions.size());
        for (ProvisioningCriteriaDefinitionData requestDef : requestDefinitions) {
            boolean found = responseDefinitions.stream().anyMatch(d -> d.getCategoryId().equals(requestDef.getCategoryId()));
            if (!found) {
                Assertions.fail("No Category found with Id:" + requestDef.getCategoryId());
            }
        }
    }

    private Integer createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal("1,00,000.00") //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);

        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges,
            final String savingsId, String principal, List<HashMap> collaterals) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate("20 September 2011") //
                .withSubmittedOnDate("20 September 2011") //
                .withCollaterals(collaterals).withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private boolean isAlreadyProvisioningEntriesCreated() throws ParseException {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();
        PageProvisioningEntryData entries = transactionHelper.retrieveAllProvisioningEntries();

        boolean provisioningetryAlreadyCreated = false;

        for (ProvisioningEntryData item : entries.getPageItems()) {
            if (item.getCreatedDate().equals(Utils.getLocalDateOfTenant())) {
                provisioningetryAlreadyCreated = true;
                break;
            }
        }

        return provisioningetryAlreadyCreated;
    }
}
