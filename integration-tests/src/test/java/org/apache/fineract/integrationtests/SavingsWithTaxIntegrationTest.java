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
package org.apache.fineract.integrationtests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.TaxComponentHelper;
import org.apache.fineract.integrationtests.common.TaxGroupHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * Integration tests for tax-related features in savings accounts.
 */
public class SavingsWithTaxIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsWithTaxIntegrationTest.class);
    private RequestSpecification requestSpec;
    private ResponseSpecification responseSpec;
    private SavingsAccountHelper savingsAccountHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;
    private SchedulerJobHelper schedulerJobHelper;
    private AccountHelper accountHelper;
    private SavingsProductHelper productHelper;
    private JournalEntryHelper journalEntryHelper;

    /**
     * Initial configuration before each test.
     */
    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.schedulerJobHelper = new SchedulerJobHelper(this.requestSpec);
        globalConfigurationHelper = new GlobalConfigurationHelper();
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    /**
     * This test verifies the complete flow of creating tax components, a tax group that contains them, and the
     * subsequent creation of a savings product that uses that tax group.
     */
    @Test
    public void shouldCreateTwoTaxComponentsAndATaxGroupWithBothSuccessfully() {
        // --- ARRANGE ---
        final String amount = "10000";
        final String jobName = "Post Interest For Savings";

        LOG.info("-------------------- STARTING TEST: shouldCreateTwoTaxComponentsAndATaxGroupWithBothSuccessfully --------------------");

        final Account assetAccount = this.accountHelper.createAssetAccount();
        final Account incomeAccount = this.accountHelper.createIncomeAccount();
        final Account expenseAccount = this.accountHelper.createExpenseAccount();
        final Account liabilityAccount = this.accountHelper.createLiabilityAccount();
        final Account savingsControlAccount = this.accountHelper.createLiabilityAccount("Savings Control");

        final String TAX_PERCENTAGE_1 = "10";
        final Integer taxComponentId1 = TaxComponentHelper.createTaxComponent(this.requestSpec, this.responseSpec, TAX_PERCENTAGE_1,
                liabilityAccount.getAccountID());
        assertNotNull(taxComponentId1, "The ID of the first tax component should not be null.");
        assertTrue(taxComponentId1 > 0, "The ID of the first tax component should be a positive number.");
        LOG.info("First tax component created successfully. ID: {}", taxComponentId1);

        final Account liabilityAccountdif = this.accountHelper.createLiabilityAccount();
        final String TAX_PERCENTAGE_2 = "15";
        final Integer taxComponentId2 = TaxComponentHelper.createTaxComponent(this.requestSpec, this.responseSpec, TAX_PERCENTAGE_2,
                liabilityAccountdif.getAccountID());
        assertNotNull(taxComponentId2, "The ID of the second tax component should not be null.");
        assertTrue(taxComponentId2 > 0, "The ID of the second tax component should be a positive number.");
        LOG.info("Second tax component created successfully. ID: {}", taxComponentId2);

        final List<Integer> taxComponentIds = Arrays.asList(taxComponentId1, taxComponentId2);
        final Integer taxGroupId = TaxGroupHelper.createTaxGroup(this.requestSpec, this.responseSpec, taxComponentIds);
        assertNotNull(taxGroupId, "The ID of the tax group should not be null.");
        assertTrue(taxGroupId > 0, "The ID of the tax group should be a positive number.");
        LOG.info("Tax group created successfully. ID: {}", taxGroupId);

        final Integer savingsProductID = createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(taxGroupId, liabilityAccount,
                expenseAccount, incomeAccount, assetAccount, savingsControlAccount);
        assertNotNull(savingsProductID, "The ID of the savings product should not be null.");
        assertTrue(savingsProductID > 0, "The ID of the savings product should be a positive number.");
        LOG.info("Savings product created successfully. ID: {}", savingsProductID);

        Integer clientId = ClientHelper.createClient(requestSpec, responseSpec, "01 January 2025");
        assertNotNull(clientId, "The client's ID should not be null.");
        assertTrue(clientId > 0, "The client's ID should be a positive number.");
        LOG.info("Client created successfully with ID: {}", clientId);
        final LocalDate startDate = LocalDate.of(2025, 2, 1);
        final String startDateString = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.US).format(startDate);
        final Integer savingsAccountId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientId, savingsProductID,
                SavingsAccountHelper.ACCOUNT_TYPE_INDIVIDUAL, startDateString);
        Assertions.assertNotNull(savingsAccountId, "Error while applying for the savings account.");

        this.savingsAccountHelper.approveSavingsOnDate(savingsAccountId, startDateString);
        this.savingsAccountHelper.activateSavings(savingsAccountId, startDateString);
        this.savingsAccountHelper.depositToSavingsAccount(savingsAccountId, amount, startDateString, CommonConstants.RESPONSE_RESOURCE_ID);

        globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                new PutGlobalConfigurationsRequest().enabled(true));

        LocalDate marchDate = LocalDate.of(2025, 3, 2);
        BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, marchDate);

        // --- ACT ---
        schedulerJobHelper.executeAndAwaitJob(jobName);

        // --- ASSERT ---
        List<HashMap> taxTransactions = getTaxTransactions(savingsAccountId);
        Assertions.assertFalse(taxTransactions.isEmpty(), "No tax transactions were found for overdraft.");

        Number firstTransactionIdNumber = (Number) taxTransactions.get(0).get("id");
        ArrayList<HashMap> journalEntries = journalEntryHelper.getJournalEntriesByTransactionId("S" + firstTransactionIdNumber.intValue());
        Assertions.assertFalse(journalEntries.isEmpty(), "No journal entries found for negative tax.");

        boolean debitFound = false;
        boolean creditFound = false;
        for (Map<String, Object> entry : journalEntries) {
            String entryType = (String) ((HashMap) entry.get("entryType")).get("value");
            Integer accountId = ((Number) entry.get("glAccountId")).intValue();
            if ("DEBIT".equals(entryType) && accountId.equals(expenseAccount.getAccountID())) {
                debitFound = true;
            }
            if ("CREDIT".equals(entryType) && accountId.equals(savingsControlAccount.getAccountID())) {
                creditFound = true;
            }
        }
        Assertions.assertTrue(debitFound, "DEBIT to Interest Receivable (Asset) Account not found for negative accrual.");
        Assertions.assertTrue(creditFound, "CREDIT to Overdraft Interest Income Account not found for negative accrual.");

        List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);

        boolean taxTransactionFound = false;
        boolean interestTransactionFound = false;

        for (HashMap transaction : allTransactions) {
            Map<String, Object> transactionType = (Map<String, Object>) transaction.get("transactionType");
            SavingsAccountTransactionType type = SavingsAccountTransactionType.fromInt(((Double) transactionType.get("id")).intValue());

            if (type.isWithHoldTax()) {
                taxTransactionFound = true;
            } else if (type.isInterestPosting()) {
                interestTransactionFound = true;
            }
        }
        assertTrue(taxTransactionFound, "A 'Withhold Tax' transaction was expected but not found.");
        assertTrue(interestTransactionFound, "An 'Interest Posting' transaction was expected but not found.");
        LOG.info("Interest and tax transactions validated successfully.");

    }

    public Integer createSavingsProductWithAccrualAccountingWithOutOverdraftAllowed(Integer taxGroupId, final Account... accounts) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT WITHOUT OVERDRAFT ---------------------------------------");
        this.productHelper = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsAnnually()
                .withInterestPostingPeriodTypeAsMonthly().withInterestCalculationPeriodTypeAsDailyBalance()
                .withAccountingRuleAsAccrualBased(accounts).withInterestPayableAccountId(accounts[1].getAccountID().toString())
                .withSavingsControlAccountId(accounts[4].getAccountID().toString()).withWithHoldTax(taxGroupId.toString());
        final String savingsProductJSON = this.productHelper.build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private List<HashMap> getTaxTransactions(Integer savingsAccountId) {
        List<HashMap> allTransactions = savingsAccountHelper.getSavingsTransactions(savingsAccountId);
        List<HashMap> taxTransactions = new ArrayList<>();
        for (HashMap transaction : allTransactions) {
            Map<String, Object> type = (Map<String, Object>) transaction.get("transactionType");
            if (type != null && Boolean.TRUE.equals(type.get("withholdTax"))) {
                taxTransactions.add(transaction);
            }
        }
        return taxTransactions;
    }

}
