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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsAccountHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsProductHelper;
import org.apache.fineract.integrationtests.common.savings.SavingsStatusChecker;
import org.apache.fineract.integrationtests.common.savings.SavingsTestLifecycleExtension;
import org.apache.fineract.portfolio.savings.SavingsAccountTransactionType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(2)
@ExtendWith({ SavingsTestLifecycleExtension.class })
public class SavingsInterestPostingJobIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(SavingsInterestPostingJobIntegrationTest.class);
    public static final String ACCOUNT_TYPE_INDIVIDUAL = "INDIVIDUAL";
    public static final String POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME = "SA_PINT";

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private SavingsProductHelper savingsProductHelper;
    private SavingsAccountHelper savingsAccountHelper;
    private SchedulerJobHelper scheduleJobHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private GlobalConfigurationHelper globalConfigurationHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.scheduleJobHelper = new SchedulerJobHelper(requestSpec);
        this.globalConfigurationHelper = new GlobalConfigurationHelper();
    }

    @Test
    public void testSavingsBalanceCheckAfterDailyInterestPostingJob() {
        // client activation, savings activation and 1st transaction date
        final String startDate = "10 April 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        /***
         * Runs Post interest posting job and verify the new account created with accounting configuration set as none
         * is picked up by job
         */
        this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
        Object transactionObj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) transactionObj;
        HashMap<String, Object> interestPostingTransaction = transactions.get(transactions.size() - 48);
        for (Map.Entry<String, Object> entry : interestPostingTransaction.entrySet()) {
            LOG.info("{} - {}", entry.getKey(), entry.getValue().toString());
        }
        assertEquals("10129.582", interestPostingTransaction.get("runningBalance").toString(), "Equality check for Balance");
    }

    @Test
    public void testSavingsDailyInterestPostingJobWithAccountingNone() {
        final String startDate = "10 April 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);

        final Integer savingsId = createSavingsAccountDailyPostingWithAccounting(clientID, startDate);

        Integer transactionId = (Integer) this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate,
                CommonConstants.RESPONSE_RESOURCE_ID);
        ArrayList<HashMap> journalEntries = this.journalEntryHelper.getJournalEntriesByTransactionId(String.valueOf(transactionId));
        assertEquals(0, journalEntries.size());
    }

    @Test
    public void testDuplicateOverdraftInterestPostingJob() {
        final String startDate = "01 July 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPostingOverdraft(clientID, startDate);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        assertNoDuplicateOverdraftInterestPostings(savingsId);
    }

    @Test
    public void testPostInterestJobRunTwiceSameDayNoDuplicate() {
        final LocalDate businessDate = LocalDate.of(2022, 4, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            // First run of Post Interest job
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            // Second run of Post Interest job on the same business day — should NOT create duplicates
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            int postingCount = countTotalUnreversedPostings(savingsId, SavingsAccountTransactionType.INTEREST_POSTING.getValue());
            LOG.info("Interest posting count after running job twice: {}", postingCount);
            assertTrue(postingCount > 0, "Should have at least one interest posting");

            assertNoDuplicateInterestPostings(savingsId);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testSavingsDailyInterestPostingJob() {
        LocalDate today = Utils.getLocalDateOfTenant();
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, today);
            // client activation, savings activation and 1st transaction date
            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);

            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            /***
             * Runs Post interest posting job and verify the new account created with accounting configuration set as
             * none is picked up by job
             */
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            Object transactionObj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
            ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) transactionObj;
            HashMap<String, Object> interestPostingTransaction = transactions.get(transactions.size() - 3);
            for (Map.Entry<String, Object> entry : interestPostingTransaction.entrySet()) {
                LOG.info("{} - {}", entry.getKey(), entry.getValue().toString());
            }
            assertEquals("2.7405", interestPostingTransaction.get("amount").toString(), "Equality check for interest posted amount");
            assertEquals("[2022, 4, 12]", interestPostingTransaction.get("date").toString(), "Date check for Interest Posting transaction");
            List<Integer> submittedOnDateStringList = (List<Integer>) interestPostingTransaction.get("submittedOnDate");
            LocalDate submittedOnDate = submittedOnDateStringList.stream().collect(
                    Collectors.collectingAndThen(Collectors.toList(), list -> LocalDate.of(list.get(0), list.get(1), list.get(2))));
            assertTrue(DateUtils.isEqual(submittedOnDate, today), "Submitted On Date check for Interest Posting transaction");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }

    }

    @Test
    public void testSavingsDailyOverdraftInterestPostingJob() {
        // client activation, savings activation and 1st transaction date
        final String startDate = "10 April 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPostingOverdraft(clientID, startDate);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        // Runs Post interest posting job and verify the new account created with Overdraft is posting negative interest
        this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
        Object transactionObj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) transactionObj;
        HashMap<String, Object> interestPostingTransaction = transactions.get(transactions.size() - 2);
        for (Map.Entry<String, Object> entry : interestPostingTransaction.entrySet()) {
            LOG.info("{} - {}", entry.getKey(), entry.getValue().toString());
        }
        assertEquals("2.7397", interestPostingTransaction.get("amount").toString(), "Equality check for overdatft interest posted amount");
        assertEquals("[2022, 4, 11]", interestPostingTransaction.get("date").toString(),
                "Date check for overdraft Interest Posting transaction");

    }

    @Test
    public void testAccountBalanceWithWithdrawalFeeAfterInterestPostingJob() {
        final String startDate = "21 June 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPostingWithCharge(clientID, startDate);
        this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
        Float balance = Float.parseFloat("800.0");
        assertEquals(balance, summary.get("accountBalance"), "Verifying account balance is 800");

        this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
        Object transactionObj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) transactionObj;
        HashMap<String, Object> interestPostingTransaction = transactions.get(transactions.size() - 5);
        for (Map.Entry<String, Object> entry : interestPostingTransaction.entrySet()) {
            LOG.info("{} - {}", entry.getKey(), entry.getValue().toString());
        }
        assertEquals("800.4384", interestPostingTransaction.get("runningBalance").toString(), "Equality check for Balance");
    }

    @Test
    public void testMultipleAccountsPostInterestJobRunTwiceNoDuplicate() {
        final LocalDate businessDate = LocalDate.of(2022, 4, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "10 April 2022";
            final Integer savingsProductID = createSavingsProductDailyPosting();
            Assertions.assertNotNull(savingsProductID);

            List<Integer> savingsIds = new ArrayList<>();
            for (int i = 0; i < 3; i++) {
                final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
                Assertions.assertNotNull(clientID);
                final Integer savingsId = createSavingsAccountForProduct(clientID, savingsProductID, startDate);
                this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
                savingsIds.add(savingsId);
            }

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            for (Integer savingsId : savingsIds) {
                int postingCount = countTotalUnreversedPostings(savingsId, SavingsAccountTransactionType.INTEREST_POSTING.getValue());
                assertTrue(postingCount > 0, "Account " + savingsId + " should have at least one interest posting");
                assertNoDuplicateInterestPostings(savingsId);
            }
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testOverdraftInterestPostingJobRunTwiceNoDuplicate() {
        final LocalDate businessDate = LocalDate.of(2022, 4, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPostingOverdraft(clientID, startDate);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            int postingCount = countTotalUnreversedPostings(savingsId, SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue());
            assertTrue(postingCount > 0, "Should have at least one overdraft interest posting");
            assertNoDuplicateOverdraftInterestPostings(savingsId);

            HashMap summary = this.savingsAccountHelper.getSavingsSummary(savingsId);
            Float accountBalance = Float.parseFloat(summary.get("accountBalance").toString());
            LOG.info("Overdraft account balance after running job twice: {}", accountBalance);
            assertTrue(accountBalance < 0, "Overdraft account balance should be negative");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testPostInterestJobRunThreeTimesNoDuplicate() {
        final LocalDate businessDate = LocalDate.of(2022, 4, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            // Run job three times
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            HashMap summaryAfterFirst = this.savingsAccountHelper.getSavingsSummary(savingsId);
            Float balanceAfterFirst = Float.parseFloat(summaryAfterFirst.get("accountBalance").toString());

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            HashMap summaryAfterSecond = this.savingsAccountHelper.getSavingsSummary(savingsId);
            Float balanceAfterSecond = Float.parseFloat(summaryAfterSecond.get("accountBalance").toString());

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            HashMap summaryAfterThird = this.savingsAccountHelper.getSavingsSummary(savingsId);
            Float balanceAfterThird = Float.parseFloat(summaryAfterThird.get("accountBalance").toString());

            assertNoDuplicateInterestPostings(savingsId);
            assertEquals(balanceAfterFirst, balanceAfterSecond, 0.001f, "Balance should not change between first and second run");
            assertEquals(balanceAfterSecond, balanceAfterThird, 0.001f, "Balance should not change between second and third run");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testPostInterestAcrossMultipleDaysNoDuplicate() {
        final LocalDate businessDateDay1 = LocalDate.of(2022, 4, 12);
        final LocalDate businessDateDay2 = LocalDate.of(2022, 4, 13);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDateDay1);

            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            // Day 1: run job twice
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            Map<String, Integer> postingsAfterDay1 = countUnreversedPostingsByDate(savingsId,
                    SavingsAccountTransactionType.INTEREST_POSTING.getValue());
            int postingDatesAfterDay1 = postingsAfterDay1.size();
            assertTrue(postingDatesAfterDay1 > 0, "Should have at least one interest posting date after day 1");

            // Day 2: advance business date, run job twice
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDateDay2);

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            Map<String, Integer> postingsAfterDay2 = countUnreversedPostingsByDate(savingsId,
                    SavingsAccountTransactionType.INTEREST_POSTING.getValue());
            int postingDatesAfterDay2 = postingsAfterDay2.size();
            assertTrue(postingDatesAfterDay2 > postingDatesAfterDay1, "Should have more posting dates after day 2 than day 1");

            // No date should have duplicates
            assertNoDuplicateInterestPostings(savingsId);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testPostInterestWithChargeJobRunTwiceNoDuplicate() {
        final LocalDate businessDate = LocalDate.of(2022, 7, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "21 June 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPostingWithCharge(clientID, startDate);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
            this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "100", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            int postingCount = countTotalUnreversedPostings(savingsId, SavingsAccountTransactionType.INTEREST_POSTING.getValue());
            assertTrue(postingCount > 0, "Should have at least one interest posting");
            assertNoDuplicateInterestPostings(savingsId);
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void testPostInterestBalanceConsistencyAfterRepeatedRuns() {
        final LocalDate businessDate = LocalDate.of(2022, 4, 12);
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final String startDate = "10 April 2022";
            final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
            Assertions.assertNotNull(clientID);

            final Integer savingsId = createSavingsAccountDailyPosting(clientID, startDate);
            this.savingsAccountHelper.depositToSavingsAccount(savingsId, "10000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

            // Run job once and capture balance
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            HashMap summaryAfterFirstRun = this.savingsAccountHelper.getSavingsSummary(savingsId);
            float balanceAfterFirstRun = ((Number) summaryAfterFirstRun.get("accountBalance")).floatValue();
            float totalInterestAfterFirstRun = ((Number) summaryAfterFirstRun.get("totalInterestPosted")).floatValue();
            LOG.info("Balance after first run: {}, totalInterestPosted: {}", balanceAfterFirstRun, totalInterestAfterFirstRun);

            // Run job again and capture balance
            this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);

            HashMap summaryAfterSecondRun = this.savingsAccountHelper.getSavingsSummary(savingsId);
            float balanceAfterSecondRun = ((Number) summaryAfterSecondRun.get("accountBalance")).floatValue();
            float totalInterestAfterSecondRun = ((Number) summaryAfterSecondRun.get("totalInterestPosted")).floatValue();
            LOG.info("Balance after second run: {}, totalInterestPosted: {}", balanceAfterSecondRun, totalInterestAfterSecondRun);

            assertEquals(balanceAfterFirstRun, balanceAfterSecondRun, 0.001f, "Account balance must not change on repeated job run");
            assertEquals(totalInterestAfterFirstRun, totalInterestAfterSecondRun, 0.001f,
                    "Total interest posted must not change on repeated job run");
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Integer createSavingsAccountForProduct(final Integer clientID, final Integer savingsProductID, final String startDate) {
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsAccountDailyPosting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsAccountDailyPostingWithAccounting(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProduct("1000");
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsAccountDailyPostingWithCharge(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPosting();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);

        final Integer withdrawalChargeId = ChargesHelper.createCharges(this.requestSpec, this.responseSpec,
                ChargesHelper.getSavingsWithdrawalFeeJSON());
        Assertions.assertNotNull(withdrawalChargeId);

        this.savingsAccountHelper.addChargesForSavings(savingsId, withdrawalChargeId, false);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsAccountDailyPostingOverdraft(final Integer clientID, final String startDate) {
        final Integer savingsProductID = createSavingsProductDailyPostingOverdraft();
        Assertions.assertNotNull(savingsProductID);
        final Integer savingsId = this.savingsAccountHelper.applyForSavingsApplicationOnDate(clientID, savingsProductID,
                ACCOUNT_TYPE_INDIVIDUAL, startDate);
        Assertions.assertNotNull(savingsId);
        HashMap savingsStatusHashMap = this.savingsAccountHelper.approveSavingsOnDate(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsApproved(savingsStatusHashMap);
        savingsStatusHashMap = this.savingsAccountHelper.activateSavingsAccount(savingsId, startDate);
        SavingsStatusChecker.verifySavingsIsActive(savingsStatusHashMap);
        return savingsId;
    }

    private Integer createSavingsProductDailyPosting() {
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    private Integer createSavingsProductDailyPostingOverdraft() {
        final String overDraftLimit = "10000.0";
        final String nominalAnnualInterestRateOverdraft = "10";
        final String savingsProductJSON = this.savingsProductHelper.withInterestCompoundingPeriodTypeAsDaily()
                .withInterestPostingPeriodTypeAsDaily().withInterestCalculationPeriodTypeAsDailyBalance()
                .withOverDraftRate(overDraftLimit, nominalAnnualInterestRateOverdraft).build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    // Accounting None
    public static Integer createSavingsProduct(final String minOpenningBalance) {
        LOG.info("------------------------------CREATING NEW SAVINGS PRODUCT ---------------------------------------");
        final String savingsProductJSON = new SavingsProductHelper().withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestCompoundingPeriodTypeAsDaily() //
                .withInterestCalculationPeriodTypeAsDailyBalance() //
                .withMinimumOpenningBalance(minOpenningBalance).withAccountingRuleAsNone().build();
        return SavingsProductHelper.createSavingsProduct(savingsProductJSON, requestSpec, responseSpec);
    }

    @SuppressWarnings("unchecked")
    private Map<String, Integer> countUnreversedPostingsByDate(Integer savingsId, int transactionTypeId) {
        Object obj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) obj;
        Map<String, Integer> postingsByDate = new HashMap<>();
        for (HashMap<String, Object> transaction : transactions) {
            int typeId = ((Number) ((Map<String, Object>) transaction.get("transactionType")).get("id")).intValue();
            boolean reversed = Boolean.parseBoolean(transaction.get("reversed").toString());
            if (typeId == transactionTypeId && !reversed) {
                postingsByDate.merge(transaction.get("date").toString(), 1, Integer::sum);
            }
        }
        return postingsByDate;
    }

    private void assertNoDuplicateInterestPostings(Integer savingsId) {
        Map<String, Integer> postingsByDate = countUnreversedPostingsByDate(savingsId,
                SavingsAccountTransactionType.INTEREST_POSTING.getValue());
        for (Map.Entry<String, Integer> entry : postingsByDate.entrySet()) {
            assertEquals(1, entry.getValue().intValue(), "Duplicate interest posting detected for date " + entry.getKey());
        }
    }

    private void assertNoDuplicateOverdraftInterestPostings(Integer savingsId) {
        Map<String, Integer> postingsByDate = countUnreversedPostingsByDate(savingsId,
                SavingsAccountTransactionType.OVERDRAFT_INTEREST.getValue());
        for (Map.Entry<String, Integer> entry : postingsByDate.entrySet()) {
            assertEquals(1, entry.getValue().intValue(), "Duplicate overdraft interest posting detected for date " + entry.getKey());
        }
    }

    private int countTotalUnreversedPostings(Integer savingsId, int transactionTypeId) {
        return countUnreversedPostingsByDate(savingsId, transactionTypeId).values().stream().mapToInt(Integer::intValue).sum();
    }

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        globalConfigurationHelper.resetAllDefaultGlobalConfigurations();
        globalConfigurationHelper.verifyAllDefaultGlobalConfigurations();
    }

}
