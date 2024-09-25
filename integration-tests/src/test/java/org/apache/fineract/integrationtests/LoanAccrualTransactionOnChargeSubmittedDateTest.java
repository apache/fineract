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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeFormatterBuilder;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.PeriodicAccrualAccountingHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanRescheduleRequestTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanAccrualTransactionOnChargeSubmittedDateTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;
    private DateTimeFormatter dateFormatter = new DateTimeFormatterBuilder().appendPattern("dd MMMM yyyy").toFormatter();
    private PeriodicAccrualAccountingHelper periodicAccrualAccountingHelper;
    private AccountHelper accountHelper;
    private LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private InlineLoanCOBHelper inlineLoanCOBHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.requestSpec.header("Fineract-Platform-TenantId", "default");
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
        this.periodicAccrualAccountingHelper = new PeriodicAccrualAccountingHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(this.requestSpec, this.responseSpec);
        this.inlineLoanCOBHelper = new InlineLoanCOBHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Accrual_Accounting_Api() {
        try {

            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            final String accrualRunTillDate = dateFormatter.format(currentDate);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 3, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Add Charge Fee
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 14);
            final String feeChargeAddedDate = dateFormatter.format(targetDate);
            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeChargeAddedDate, "10"));

            assertNotNull(feeLoanChargeId);

            // Run accrual for charge created date
            this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(accrualRunTillDate);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 10.0f, 10.0f, loanId);

            // Set business date
            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            final String nextAccrualRunDate = dateFormatter.format(futureDate);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate);

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("4 March 2023").locale("en")
                            .transactionAmount(100.0));

            // Add Charge
            Integer feeCharge_1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 21);
            final String feeChargeAddedDate_1 = dateFormatter.format(targetDate);
            Integer feeLoanChargeId_1 = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge_1), feeChargeAddedDate_1, "10"));

            assertNotNull(feeLoanChargeId_1);

            // Run accrual for charge created date
            this.periodicAccrualAccountingHelper.runPeriodicAccrualAccounting(nextAccrualRunDate);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(futureDate, 0.0f, 10.0f, 0.0f, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }

    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Add_Periodic_Accrual_Transactions_Job() {
        try {

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            final String accrualRunTillDate = dateFormatter.format(currentDate);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 3, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Add Charge Fee
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 14);
            final String feeChargeAddedDate = dateFormatter.format(targetDate);
            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeChargeAddedDate, "10"));

            assertNotNull(feeLoanChargeId);

            // Run periodic accrual job for business date
            final String jobName = "Add Periodic Accrual Transactions";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 10.0f, 10.0f, loanId);

            // Set business date
            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            final String nextAccrualRunDate = dateFormatter.format(futureDate);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate);

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("4 March 2023").locale("en")
                            .transactionAmount(100.0));

            // Add Charge
            Integer feeCharge_1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 21);
            final String feeChargeAddedDate_1 = dateFormatter.format(targetDate);
            Integer feeLoanChargeId_1 = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge_1), feeChargeAddedDate_1, "10"));

            assertNotNull(feeLoanChargeId_1);

            // Run periodic accrual job for business date
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(futureDate, 0.0f, 10.0f, 0.0f, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Loan_COB_AddPeriodicAccrualEntriesBusinessStep() {
        try {

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            final String accrualRunTillDate = dateFormatter.format(currentDate);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 3, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Add Charge Fee
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 14);
            final String feeChargeAddedDate = dateFormatter.format(targetDate);
            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeChargeAddedDate, "10"));

            assertNotNull(feeLoanChargeId);

            // Run cob job for business date + 1
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate.plusDays(1));

            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 10.0f, 10.0f, loanId);

            // Set business date
            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            final String nextAccrualRunDate = dateFormatter.format(futureDate);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate);

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("4 March 2023").locale("en")
                            .transactionAmount(100.0));

            // Add Charge
            Integer feeCharge_1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 21);
            final String feeChargeAddedDate_1 = dateFormatter.format(targetDate);
            Integer feeLoanChargeId_1 = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge_1), feeChargeAddedDate_1, "10"));

            assertNotNull(feeLoanChargeId_1);

            // Run cob job for business date + 1
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate.plusDays(1));
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(futureDate, 0.0f, 10.0f, 0.0f, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmittedTest_Add_Accrual_Transactions_Job() {
        try {

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            final String accrualRunTillDate = dateFormatter.format(currentDate);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccount(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 3, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Add Charge Fee
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 14);
            final String feeChargeAddedDate = dateFormatter.format(targetDate);
            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeChargeAddedDate, "10"));

            assertNotNull(feeLoanChargeId);

            // Run accrual entries job for business date
            final String jobName = "Add Accrual Transactions";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 10.0f, 10.0f, loanId);

            // Set business date
            LocalDate futureDate = LocalDate.of(2023, 3, 4);
            final String nextAccrualRunDate = dateFormatter.format(futureDate);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate);

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("4 March 2023").locale("en")
                            .transactionAmount(100.0));

            // Add Charge
            Integer feeCharge_1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            targetDate = LocalDate.of(2023, 3, 21);
            final String feeChargeAddedDate_1 = dateFormatter.format(targetDate);
            Integer feeLoanChargeId_1 = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge_1), feeChargeAddedDate_1, "10"));

            assertNotNull(feeLoanChargeId_1);

            // Run accrual entries job for business date
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(futureDate, 0.0f, 10.0f, 0.0f, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmitted_With_Multiple_Repayments_Test_Add_Periodic_Accrual_Transactions_Job() {
        try {

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);
            final String accrualRunTillDate = dateFormatter.format(currentDate);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductMultipleRepayments(
                    loanTransactionHelper, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccountMultipleRepayments(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            // Due for future date in one of the schedule
            LocalDate targetDate = LocalDate.of(2023, 3, 10);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Add Charge Penalty
            Integer penalty_1 = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            // Due for future date in different of the schedule
            targetDate = LocalDate.of(2023, 3, 17);
            final String penaltyChargeAddedDate = dateFormatter.format(targetDate);
            Integer penaltyLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty_1), penaltyChargeAddedDate, "10"));

            assertNotNull(penaltyLoanChargeId);

            // Run periodic accrual job for business date
            final String jobName = "Add Periodic Accrual Transactions";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify multiple accrual transactions are created on charge created date according to repayment schedule
            // to which charge due date falls
            checkAccrualTransactionsForMultipleRepaymentSchedulesChargeDueDate(currentDate, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    @Test
    public void loanAccrualTransactionOnChargeSubmitted_multiple_disbursement_reversal_test_Loan_COB() {
        try {

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 3, 3);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));
            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductMultipleDisbursements(
                    loanTransactionHelper, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 3, 9);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // Run cob job for business date + 1
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate.plusDays(1));

            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 0.0f, 10.0f, loanId);

            // Set business date
            LocalDate futureDate = LocalDate.of(2023, 3, 4);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, futureDate);

            loanTransactionHelper.disburseLoanWithTransactionAmount("04 March 2023", loanId, "300");

            // verify accrual transaction exists with same date,amount and is not reversed by regeneration of repayment
            // schedule
            checkAccrualTransaction(currentDate, 0.0f, 0.0f, 10.0f, loanId);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    @Test
    public void loanAccrualTransaction_ChargeSubmittedDate_AdjustRepaymentScheduleSnoozeFeeDueDateNewFeeAddTest() {
        try {

            // Accounts oof periodic accrual
            final Account assetAccount = this.accountHelper.createAssetAccount();
            final Account incomeAccount = this.accountHelper.createIncomeAccount();
            final Account expenseAccount = this.accountHelper.createExpenseAccount();
            final Account overpaymentAccount = this.accountHelper.createLiabilityAccount();

            // Set business date
            LocalDate currentDate = LocalDate.of(2023, 05, 19);

            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("submitted-date"));

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Client and Loan account creation

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductMultipleRepayments(
                    loanTransactionHelper, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            assertNotNull(getLoanProductsProductResponse);

            final Integer loanId = createLoanAccountAndDisburse(clientId, getLoanProductsProductResponse.getId(), loanExternalIdStr);

            // set business date as date when fee charge is added/submitted

            LocalDate chargeSubmittedDate = LocalDate.of(2023, 06, 12);

            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, chargeSubmittedDate);

            LocalDate chargeDueDate = LocalDate.of(2023, 07, 18);
            final String feeChargeDueDate = dateFormatter.format(chargeDueDate);

            // Add Fee
            Integer feeCharge = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", false));

            Integer feeLoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(feeCharge), feeChargeDueDate, "10"));

            assertNotNull(feeLoanChargeId);

            // adjust loan schedule
            final String requestJSON = new LoanRescheduleRequestTestBuilder().updateRescheduleFromDate("18 June 2023")
                    .updateAdjustedDueDate("18 July 2023").updateSubmittedOnDate("19 May 2023").updateGraceOnPrincipal(null)
                    .updateGraceOnInterest(null).updateExtraTerms(null).build(loanId.toString());
            final HashMap<String, String> map = new HashMap<>();
            map.put("locale", "en");
            map.put("dateFormat", "dd MMMM yyyy");
            map.put("approvedOnDate", "19 May 2023");
            final String aproveRequestJSON = new Gson().toJson(map);

            Integer loanRescheduleRequestId = this.loanRescheduleRequestHelper.createLoanRescheduleRequest(requestJSON);
            this.loanRescheduleRequestHelper.approveLoanRescheduleRequest(loanRescheduleRequestId, aproveRequestJSON);

            // run cob
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE,
                    chargeSubmittedDate.plusDays(1));

            // run inline cob for loan
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(chargeSubmittedDate, 0.0f, 10.0f, 0.0f, loanId);

            // update business date
            currentDate = LocalDate.of(2023, 07, 18);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);

            // make repayment
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("18 July 2023").locale("en")
                            .transactionAmount(1010.0));

            // update business date
            currentDate = LocalDate.of(2023, 07, 19);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate);

            // reverse repayment
            loanTransactionHelper.reverseRepayment(loanId, repaymentTransaction_1.getResourceId().intValue(), "19 July 2023");

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "10", true));

            LocalDate targetDate = LocalDate.of(2023, 07, 19);
            final String penaltyCharge1AddedDate = dateFormatter.format(targetDate);

            Integer penalty1LoanChargeId = this.loanTransactionHelper.addChargesForLoan(loanId,
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), penaltyCharge1AddedDate, "10"));

            assertNotNull(penalty1LoanChargeId);

            // update business date
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, currentDate.plusDays(1));

            // run inline cob for loan
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.longValue()));

            // verify accrual transaction created for charges create date
            checkAccrualTransaction(currentDate, 0.0f, 0.0f, 10.0f, loanId);

            // verify loan repayment schedules
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails((long) loanId);
            assertTrue(loanDetails.getStatus().getActive());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            assertNotNull(periods);

            verifyPeriodDates(periods);

        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.CHARGE_ACCRUAL_DATE,
                    new PutGlobalConfigurationsRequest().stringValue("due-date"));
        }
    }

    private void verifyPeriodDates(List<GetLoansLoanIdRepaymentPeriod> periods) {
        assertEquals(3, periods.size());
        assertEquals(LocalDate.of(2023, 05, 19), periods.get(1).getFromDate());
        assertEquals(LocalDate.of(2023, 07, 18), periods.get(1).getDueDate());
        assertEquals(LocalDate.of(2023, 07, 18), periods.get(2).getFromDate());
        assertEquals(LocalDate.of(2023, 07, 19), periods.get(2).getDueDate());
    }

    private void checkAccrualTransactionsForMultipleRepaymentSchedulesChargeDueDate(LocalDate transactionDate, Integer loanId) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanId);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isAccrualTransaction = (Boolean) transactionType.get("accrual");

            if (isAccrualTransaction) {
                ArrayList<Integer> accrualEntryDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate accrualEntryDate = LocalDate.of(accrualEntryDateAsArray.get(0), accrualEntryDateAsArray.get(1),
                        accrualEntryDateAsArray.get(2));

                if (DateUtils.isEqual(transactionDate, accrualEntryDate)) {
                    isTransactionFound = true;
                    verifyAmounts(0.0f, 0.0f, 10.0f, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))));
                }
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

    private void verifyAmounts(final Float expectedInterestPortion, final Float expectedFeePortion, final Float expectedPenaltyPortion,
            final Float actualInterestPortion, final Float actualFeePortion, final Float actualPenaltyPortion) {
        assertEquals(expectedInterestPortion, actualInterestPortion, "Mismatch in transaction amounts");
        assertEquals(expectedFeePortion, actualFeePortion, "Mismatch in transaction amounts");
        assertEquals(expectedPenaltyPortion, actualPenaltyPortion, "Mismatch in transaction amounts");
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 March 2023").withSubmittedOnDate("03 March 2023").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 March 2023", loanId, "1000");
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductMultipleRepayments(final LoanTransactionHelper loanTransactionHelper,
            final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentAfterEvery("1")
                .withNumberOfRepayments("1").withRepaymentTypeAsDays().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat()
                .withAccountingRulePeriodicAccrual(accounts).withDaysInMonth("30").withDaysInYear("365").withMoratorium("0", "0")
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private GetLoanProductsProductIdResponse createLoanProductMultipleDisbursements(final LoanTransactionHelper loanTransactionHelper,
            final Account... accounts) {

        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true).build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("10").withRepaymentEveryAfter("3").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private Integer createLoanAccountMultipleRepayments(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("10").withRepaymentEveryAfter("3").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 March 2023", loanId, "1000");
        return loanId;
    }

    private Integer createLoanAccountAndDisburse(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("19 May 2023")
                .withSubmittedOnDate("19 May 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("19 May 2023", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("19 May 2023", loanId, "1000");
        return loanId;
    }

    private void checkAccrualTransaction(final LocalDate transactionDate, final Float interestPortion, final Float feePortion,
            final Float penaltyPortion, final Integer loanID) {

        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(this.requestSpec,
                this.responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isAccrualTransaction = (Boolean) transactionType.get("accrual");

            if (isAccrualTransaction) {
                ArrayList<Integer> accrualEntryDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate accrualEntryDate = LocalDate.of(accrualEntryDateAsArray.get(0), accrualEntryDateAsArray.get(1),
                        accrualEntryDateAsArray.get(2));

                if (DateUtils.isEqual(transactionDate, accrualEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transactions.get(i).get("interestPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transactions.get(i).get("feeChargesPortion"))),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transactions.get(i).get("penaltyChargesPortion"))),
                            "Mismatch in transaction amounts");
                    break;
                }
            }
        }
        assertTrue(isTransactionFound, "No Accrual entries are posted");
    }

}
