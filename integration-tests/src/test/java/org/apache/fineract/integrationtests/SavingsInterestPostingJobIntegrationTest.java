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
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
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
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Order(2)
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

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.savingsAccountHelper = new SavingsAccountHelper(this.requestSpec, this.responseSpec);
        this.savingsProductHelper = new SavingsProductHelper();
        this.scheduleJobHelper = new SchedulerJobHelper(requestSpec);
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
        // client activation, savings activation and 1st transaction date
        final String startDate = "01 July 2022";
        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec, startDate);
        Assertions.assertNotNull(clientID);

        final Integer savingsId = createSavingsAccountDailyPostingOverdraft(clientID, startDate);

        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);

        this.scheduleJobHelper.executeAndAwaitJobByShortName(POST_INTEREST_FOR_SAVINGS_JOB_SHORT_NAME);
        this.savingsAccountHelper.withdrawalFromSavingsAccount(savingsId, "1000", startDate, CommonConstants.RESPONSE_RESOURCE_ID);
        Object transactionObj = this.savingsAccountHelper.getSavingsDetails(savingsId, "transactions");
        ArrayList<HashMap<String, Object>> transactions = (ArrayList<HashMap<String, Object>>) transactionObj;
        Integer dateCount = 0;
        for (HashMap<String, Object> transaction : transactions) {
            if (transaction.get("date").toString().equals("[2022, 7, 10]") && transaction.get("reversed").toString().equals("false")) {
                dateCount++;
            }
        }
        assertEquals(1, dateCount, "No Duplicate Overdraft Interest Posting");
    }

    @Test
    public void testSavingsDailyInterestPostingJob() {
        LocalDate today = Utils.getLocalDateOfTenant();
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, true);
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
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, false);
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

    // Reset configuration fields
    @AfterEach
    public void tearDown() {
        GlobalConfigurationHelper.resetAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
        GlobalConfigurationHelper.verifyAllDefaultGlobalConfigurations(this.requestSpec, this.responseSpec);
    }

}
