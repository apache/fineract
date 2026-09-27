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

import java.math.BigDecimal;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetProvisioningCriteriaCriteriaIdResponse;
import org.apache.fineract.client.models.PageLoanProductProvisioningEntryData;
import org.apache.fineract.client.models.PageProvisioningEntryData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansRequestCollateralData;
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
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class ProvisioningIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningIntegrationTest.class);
    private static final String NONE = "1";
    private static final int LOANPRODUCTS_SIZE = 2;
    private static final String LOAN_DATE = "20 September 2011";

    private FeignCollateralHelper collateralHelper;

    @BeforeEach
    public void setup() throws ParseException {
        this.collateralHelper = new FeignCollateralHelper(fineractClient());
        Assumptions.assumeTrue(!isAlreadyProvisioningEntriesCreated());
    }

    @Test
    public void testCreateProvisioningCriteria() {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper();
        ArrayList<Integer> loanProducts = new ArrayList<>(LOANPRODUCTS_SIZE);
        final Long clientID = createClient(ClientTestData.DEFAULT_ACTIVATION_DATE);
        Assertions.assertEquals(clientID, clientHelper.getClient(clientID).getId());

        for (int i = 0; i < LOANPRODUCTS_SIZE; i++) {
            final Long loanProductID = createLoanProduct(false, NONE);
            loanProducts.add(loanProductID.intValue());
            Assertions.assertNotNull(loanProductID);
            final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
            Assertions.assertNotNull(collateralId);
            final Long clientCollateralId = collateralHelper.createClientCollateral(clientID, collateralId).getResourceId();
            Assertions.assertNotNull(clientCollateralId);
            final Long loanID = applyForLoanApplication(clientID, loanProductID, "1,00,000.00", clientCollateralId);
            verifyLoanStatus(loanID, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);
            approveLoan(loanID, new PostLoansLoanIdRequest().approvedOnDate(LOAN_DATE).locale(LoanTestData.LOCALE)
                    .dateFormat(LoanTestData.DATETIME_PATTERN));
            verifyLoanStatus(loanID, LoanStatus.APPROVED);
            verifyLoanStatus(getLoanDetails(loanID), GetLoansLoanIdStatus::getWaitingForDisbursal);
            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            disburseLoan(loanID, LoanRequestBuilders
                    .disburseLoanWithNetDisbursalAmount(LOAN_DATE, getLoanDetails(loanID).getNetDisbursalAmount()).note("DISBURSE NOTE"));
            verifyLoanStatus(getLoanDetails(loanID), GetLoansLoanIdStatus::getActive);
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

    private Long createLoanProduct(final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
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
        return createLoanProduct(builder.buildRequest());
    }

    private Long applyForLoanApplication(final Long clientID, final Long loanProductID, String principal, Long clientCollateralId) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanApplication = LoanRequestBuilders
                .legacyIndividualApplication(clientID, loanProductID, principal, 4, BigDecimal.valueOf(2), LOAN_DATE)//
                .collateral(List.of(new PostLoansRequestCollateralData().clientCollateralId(clientCollateralId).quantity(BigDecimal.ONE)));
        return applyForLoan(loanApplication);
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
