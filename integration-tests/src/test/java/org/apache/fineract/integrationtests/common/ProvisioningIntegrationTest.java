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

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.path.json.JsonPath;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningHelper;
import org.apache.fineract.integrationtests.common.provisioning.ProvisioningTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Assumptions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
public class ProvisioningIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ProvisioningIntegrationTest.class);
    private static final String NONE = "1";
    private static final int LOANPRODUCTS_SIZE = 10;

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
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        Assumptions.assumeTrue(!isAlreadyProvisioningEntriesCreated());
    }

    @Test
    public void testCreateProvisioningCriteria() {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper(requestSpec, responseSpec);
        ArrayList<Integer> loanProducts = new ArrayList<>(LOANPRODUCTS_SIZE);
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);
        for (int i = 0; i < LOANPRODUCTS_SIZE; i++) {
            final Integer loanProductID = createLoanProduct(false, NONE);
            loanProducts.add(loanProductID);
            Assertions.assertNotNull(loanProductID);
            final Integer loanID = applyForLoanApplication(clientID, loanProductID, null, null, "1,00,000.00");
            HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
            LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);
            loanStatusHashMap = this.loanTransactionHelper.approveLoan("20 September 2011", loanID);
            LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
            LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);
            LOG.info("-------------------------------DISBURSE LOAN-------------------------------------------");
            String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
            loanStatusHashMap = this.loanTransactionHelper.disburseLoan("20 September 2011", loanID,
                    JsonPath.from(loanDetails).get("netDisbursalAmount").toString());
            LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
            Assertions.assertNotNull(loanID);
        }

        ArrayList categories = transactionHelper.retrieveAllProvisioningCategories();
        Assertions.assertTrue(categories.size() > 0);
        Account liability = accountHelper.createLiabilityAccount();
        Account expense = accountHelper.createExpenseAccount();
        Map requestCriteria = ProvisioningHelper.createProvisioingCriteriaJson(loanProducts, categories, liability, expense);
        String provisioningCriteriaCreateJson = new Gson().toJson(requestCriteria);
        Integer criteriaId = transactionHelper.createProvisioningCriteria(provisioningCriteriaCreateJson);
        Assertions.assertNotNull(criteriaId);

        Map newCriteria = transactionHelper.retrieveProvisioningCriteria(criteriaId);
        validateProvisioningCriteria(requestCriteria, newCriteria);

        ArrayList definitions = (ArrayList) newCriteria.get("definitions");
        for (int i = 0; i < definitions.size(); i++) {
            Map criteriadefinition = (Map) definitions.get(i);
            criteriadefinition.put("provisioningPercentage", Float.valueOf((float) 20.0));
        }
        newCriteria.put("locale", "en");
        String updateCriteriaString = new Gson().toJson(newCriteria);
        Integer criteriaId1 = transactionHelper.updateProvisioningCriteria(criteriaId, updateCriteriaString);
        Map updatedCriteria = transactionHelper.retrieveProvisioningCriteria(criteriaId1);
        validateProvisioningCriteria(newCriteria, updatedCriteria);

        transactionHelper.deleteProvisioningCriteria(criteriaId1);

        categories = transactionHelper.retrieveAllProvisioningCategories();
        liability = accountHelper.createLiabilityAccount();
        expense = accountHelper.createExpenseAccount();
        requestCriteria = ProvisioningHelper.createProvisioingCriteriaJson(loanProducts, categories, liability, expense);
        provisioningCriteriaCreateJson = new Gson().toJson(requestCriteria);
        criteriaId = transactionHelper.createProvisioningCriteria(provisioningCriteriaCreateJson);
        Assertions.assertNotNull(criteriaId);

        String provisioningEntryJson = ProvisioningHelper.createProvisioningEntryJson();
        Integer provisioningEntryId = transactionHelper.createProvisioningEntries(provisioningEntryJson);
        Assertions.assertNotNull(provisioningEntryId);

        transactionHelper.updateProvisioningEntry("recreateprovisioningentry", provisioningEntryId, "");
        transactionHelper.updateProvisioningEntry("createjournalentry", provisioningEntryId, "");
        Map entry = transactionHelper.retrieveProvisioningEntry(provisioningEntryId);
        Assertions.assertTrue((Boolean) entry.get("journalEntry"));
        Map provisioningEntry = transactionHelper.retrieveProvisioningEntries(provisioningEntryId);
        Assertions.assertTrue(((ArrayList) provisioningEntry.get("pageItems")).size() > 0);
    }

    private void validateProvisioningCriteria(Map requestCriteria, Map newCriteria) {

        // criteria name validation
        String requestCriteriaName = (String) requestCriteria.get("criteriaName");
        String criteriaName = (String) newCriteria.get("criteriaName");
        Assertions.assertEquals(criteriaName, requestCriteriaName);

        // loan products validation
        ArrayList requestProducts = (ArrayList) requestCriteria.get("loanProducts");
        ArrayList products = (ArrayList) newCriteria.get("loanProducts");
        Assertions.assertEquals(products.size(), requestProducts.size());

        ArrayList requestedDefinitions = (ArrayList) requestCriteria.get("definitions");
        ArrayList newdefintions = (ArrayList) newCriteria.get("definitions");
        Assertions.assertEquals(newdefintions.size(), requestedDefinitions.size());
        for (int i = 0; i < newdefintions.size(); i++) {
            Map requestedMap = (Map) requestedDefinitions.get(i);
            Object requestedCategoryId = requestedMap.get("categoryId");
            boolean found = false;
            for (int j = 0; j < newdefintions.size(); j++) {
                Map newMap = (Map) newdefintions.get(j);
                Object newCategoryId = newMap.get("categoryId");
                if (requestedCategoryId.equals(newCategoryId)) {
                    found = true;
                    checkProperty("categoryId", requestedMap, newMap);
                    checkProperty("categoryName", requestedMap, newMap);
                    checkProperty("minAge", requestedMap, newMap);
                    checkProperty("maxAge", requestedMap, newMap);
                    checkProperty("provisioningPercentage", requestedMap, newMap);
                    checkProperty("liabilityAccount", requestedMap, newMap);
                    checkProperty("expenseAccount", requestedMap, newMap);
                    break; // internal loop
                }
            }
            if (!found) {
                Assertions.fail("No Category found with Id:" + requestedCategoryId);
            }
        }
    }

    private void checkProperty(String propertyName, Map requestMap, Map newMap) {
        Object requested = requestMap.get(propertyName);
        Object modified = newMap.get(propertyName);
        Assertions.assertEquals(requested, modified);
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
            final String savingsId, String principal) {
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
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private boolean isAlreadyProvisioningEntriesCreated() throws ParseException {
        ProvisioningTransactionHelper transactionHelper = new ProvisioningTransactionHelper(requestSpec, responseSpec);
        Map entries = transactionHelper.retrieveAllProvisioningEntries();
        ArrayList<Map> pageItems = (ArrayList) entries.get("pageItems");
        boolean provisioningetryAlreadyCreated = false;
        if (pageItems != null) {
            for (Map item : pageItems) {
                String date = (String) item.get("createdDate");
                DateFormat formatter = new SimpleDateFormat("MMM dd, yyyy");
                Date date1 = formatter.parse(date);
                DateFormat simple = new SimpleDateFormat("dd MMMM yyyy");
                String formattedString = simple
                        .format(Date.from(Utils.getLocalDateOfTenant().atStartOfDay(DateUtils.getDateTimeZoneOfTenant()).toInstant()));
                Date currentDate = simple.parse(formattedString);
                if (date1.getTime() == currentDate.getTime()) {
                    provisioningetryAlreadyCreated = true;
                    break;
                }
            }
        }
        return provisioningetryAlreadyCreated;
    }
}
