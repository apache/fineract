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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.migration.domain.LoanMigrationStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class MigrationLoanApiTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanInterestPauseApiTest.class);

    private static RequestSpecification REQUEST_SPEC;
    private static ResponseSpecification RESPONSE_SPEC;
    private static ResponseSpecification RESPONSE_SPEC_403;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER_403;
    private static AccountHelper ACCOUNT_HELPER;
    private static final Integer nonExistLoanId = 99999;
    private static String externalId;
    private static final String nonExistExternalId = "7c4fb86f-a778-4d02-b7a8-ec3ec98941fa";
    private Integer clientId;
    private Integer loanProductId;
    private Integer loanId;
    private final String loanPrincipalAmount = "10000.00";
    private final String numberOfRepayments = "12";
    private final String interestRatePerPeriod = "18";
    private final String dateString = "01 January 2023";

    @BeforeEach
    public void initialize() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        RESPONSE_SPEC_403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        LOAN_TRANSACTION_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
        LOAN_TRANSACTION_HELPER_403 = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC_403);
        ACCOUNT_HELPER = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);

        externalId = UUID.randomUUID().toString();

        createRequiredEntities();

        Assertions.assertNotNull(loanProductId, "Loan Product ID should not be null after creation");
        Assertions.assertNotNull(loanId, "Loan ID should not be null after creation");
        Assertions.assertNotNull(externalId, "External Loan ID should not be null after creation");
    }

    /**
     * Creates the client, loan product, and loan entities
     **/
    private void createRequiredEntities() {
        this.createClientEntity();
        this.createLoanProductEntity();
        this.createLoanEntity();
    }

    @Test
    public void testCreateMigrateByLoanId_validRequest_shouldCreateSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER
                .createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrationByExternalLoanId_validRequest_shouldCreateSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER
                .createMigrationByExternalId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, externalId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrateByLoanId_validRequest_shouldUpdateSucceed() {
        LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER
                .createMigrationByLoanId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrationByExternalLoanId_validRequest_shouldUpdateSucceed() {
        LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER
                .createMigrationByLoanId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrateByLoanId_validRequest_shouldUpdateSucceed2() {
        LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_FAILED,
                loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrationByExternalLoanId_validRequest_shouldUpdateSucceed2() {
        LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_FAILED,
                loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
        Assertions.assertEquals(response.getSubResourceId(), loanId.longValue());
        Assertions.assertEquals(response.getSubResourceExternalId(), externalId);
    }

    @Test
    public void testCreateMigrationByLoanId_FirstTheStatusMustBeInProgress_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.not.started"),
                    "Migration must be started with MIGRATION_IN_PROGRESS status first");
        }
    }

    @Test
    public void testCreateMigrationByLoanId_FirstTheStatusMustBeInProgress_shouldFail2() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_FAILED, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.not.started"),
                    "Migration must be started with MIGRATION_IN_PROGRESS status first");
        }
    }

    @Test
    public void testCreateMigrationByExternalId_FirstTheStatusMustBeInProgress_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.not.started"),
                    "Migration must be started with MIGRATION_IN_PROGRESS status first");
        }
    }

    @Test
    public void testCreateMigrationByExternalId_FirstTheStatusMustBeInProgress_shouldFail2() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_FAILED, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.not.started"),
                    "Migration must be started with MIGRATION_IN_PROGRESS status first");
        }
    }

    @Test
    public void testCreateMigrationByLoanId_WeCannotMarkLoanAsInProgressTwice_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);
            LOAN_TRANSACTION_HELPER_403.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.already.in.progress"),
                    "Migration for loan " + loanId + " already in progress");
        }
    }

    @Test
    public void testCreateMigrationByExternalId_WeCannotMarkLoanAsInProgressTwice_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, externalId);
            LOAN_TRANSACTION_HELPER_403.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("migration.already.in.progress"),
                    "Migration for loan " + loanId + " already in progress");
        }
    }

    @Test
    public void testCreateMigrationByLoanId_InvalidStatusTransition_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("invalid.status.transition"), "Cannot transition from");
        }
    }

    @Test
    public void testCreateMigrationByLoanId_InvalidStatusTransition_shouldFail2() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_FAILED, loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("invalid.status.transition"), "Cannot transition from");
        }
    }

    @Test
    public void testCreateMigrationByExternalId_InvalidStatusTransition_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_SUCCESSFUL, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("invalid.status.transition"), "Cannot transition from");
        }
    }

    @Test
    public void testCreateMigrationByExternal_InvalidStatusTransition_shouldFail2() {
        try {
            LOAN_TRANSACTION_HELPER_403.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_FAILED, externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("invalid.status.transition"), "Cannot transition from");
        }
    }

    @Test
    public void testRetrieveMigrationByLoanId_shouldReturnData() {
        LOAN_TRANSACTION_HELPER.createMigrationByLoanId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, loanId);

        String response = LOAN_TRANSACTION_HELPER.retrieveLoanMigrationByLoanId(loanId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("MIGRATION_IN_PROGRESS"));
    }

    @Test
    public void testRetrieveMigrationByExternalId_shouldReturnData() {
        LOAN_TRANSACTION_HELPER.createMigrationByExternalId(LoanMigrationStatus.MIGRATION_IN_PROGRESS, externalId);

        String response = LOAN_TRANSACTION_HELPER.retrieveLoanMigrationByExternalId(externalId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("MIGRATION_IN_PROGRESS"));
    }

    @Test
    public void testRetrieveMigrationByLoanId_noMigrations_shouldReturnEmpty() {
        try {
            LOAN_TRANSACTION_HELPER_403.retrieveLoanMigrationByLoanId(nonExistLoanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.loan.migration.not.found"), "Migration for Loan not found");
        }
    }

    @Test
    public void testRetrieveMigrationByExternalId_noMigrations_shouldReturnEmpty() {
        try {
            LOAN_TRANSACTION_HELPER_403.retrieveLoanMigrationByExternalId(nonExistExternalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("error.msg.loan.migration.not.found"), "Migration for Loan not found");
        }
    }

    /**
     * create a new client
     **/
    private void createClientEntity() {
        this.clientId = ClientHelper.createClient(REQUEST_SPEC, RESPONSE_SPEC);

        ClientHelper.verifyClientCreatedOnServer(REQUEST_SPEC, RESPONSE_SPEC, clientId);
    }

    /**
     * create a new loan product
     **/
    private void createLoanProductEntity() {
        LOG.info("---------------------------------CREATING LOAN PRODUCT------------------------------------------");

        final String interestRecalculationCompoundingMethod = LoanProductTestBuilder.RECALCULATION_COMPOUNDING_METHOD_NONE;
        final String rescheduleStrategyMethod = LoanProductTestBuilder.RECALCULATION_STRATEGY_ADJUST_LAST_UNPAID_PERIOD;
        final String preCloseInterestCalculationStrategy = LoanProductTestBuilder.INTEREST_APPLICABLE_STRATEGY_ON_PRE_CLOSE_DATE;

        final Account assetAccount = ACCOUNT_HELPER.createAssetAccount();
        final Account incomeAccount = ACCOUNT_HELPER.createIncomeAccount();
        final Account expenseAccount = ACCOUNT_HELPER.createExpenseAccount();
        final Account overpaymentAccount = ACCOUNT_HELPER.createLiabilityAccount();

        String futureInstallmentAllocationRule = "NEXT_INSTALLMENT";
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation(futureInstallmentAllocationRule);
        String loanProductJSON = new LoanProductTestBuilder().withPrincipal(loanPrincipalAmount).withNumberOfRepayments(numberOfRepayments)
                .withRepaymentAfterEvery("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod(interestRatePerPeriod)
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualInstallments().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount })
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).addAdvancedPaymentAllocation(defaultAllocation)
                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE).withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL)
                .withMultiDisburse().withDisallowExpectedDisbursements(true).withInterestRecalculationDetails(
                        interestRecalculationCompoundingMethod, rescheduleStrategyMethod, preCloseInterestCalculationStrategy)
                .build();

        loanProductId = LOAN_TRANSACTION_HELPER.getLoanProductId(loanProductJSON);
        LOG.info("Successfully created loan product  (ID:{}) ", loanProductId);
    }

    /**
     * submit a new loan application, approve and disburse the loan
     **/
    private void createLoanEntity() {
        LOG.info("---------------------------------NEW LOAN APPLICATION------------------------------------------");

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(loanPrincipalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsDays().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsDays().withInterestRatePerPeriod(interestRatePerPeriod)
                .withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate(dateString)
                .withSubmittedOnDate(dateString).withLoanType("individual").withExternalId(externalId)
                .withRepaymentStrategy("advanced-payment-allocation-strategy").build(clientId.toString(), loanProductId.toString(), null);

        loanId = LOAN_TRANSACTION_HELPER.getLoanId(loanApplicationJSON);

        LOG.info("Sucessfully created loan (ID: {} )", loanId);

        approveLoanApplication();
        disburseLoan();
    }

    /**
     * approve the loan application
     **/
    private void approveLoanApplication() {

        if (loanId != null) {
            LOAN_TRANSACTION_HELPER.approveLoan(dateString, loanId);
            LOG.info("Successfully approved loan (ID: {} )", loanId);
        }
    }

    /**
     * disburse the newly created loan
     **/
    private void disburseLoan() {

        if (loanId != null) {
            LOAN_TRANSACTION_HELPER.disburseLoan(externalId, new PostLoansLoanIdRequest().actualDisbursementDate(dateString)
                    .transactionAmount(new BigDecimal(loanPrincipalAmount)).locale("en").dateFormat("dd MMMM yyyy"));
            LOG.info("Successfully disbursed loan (ID: {} )", loanId);
        }
    }
}
