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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Collectors;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionAccrualActivityPostingTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionAccrualActivityPostingTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static ChargesHelper chargesHelper;
    private static InlineLoanCOBHelper inlineLoanCOBHelper;
    private static BusinessStepHelper businessStepHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        chargesHelper = new ChargesHelper();
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
        businessStepHelper = new BusinessStepHelper();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "ACCRUAL_ACTIVITY_POSTING");
    }

    // Create Loan with Interest and enabled Accrual Activity Posting
    // Approve and disburse loan
    // charge penalty with due date as 1st installment
    // charge fee with due date as 1st installment
    // charge penalty with due date as 3rd installment
    // charge fee with due date as 2nd installment
    // set business day to the day before closing day of 1st installment, run COB for loan, verify no Accrual Activity
    // posted
    // set business day to the closing day of 1st installment, run COB for loan, verify Accrual Activity posted
    // set business day to the day after closing day of 1st installment, run COB for loan, verify no Accrual Activity
    // posted
    @Test
    public void testAccrualActivityPosting() {
        final String disbursementDay = "01 January 2023";
        final String repaymentPeriod1DueDate = "01 February 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String repaymentPeriod1OneDayBeforeCloseDate = "01 February 2023";
        final String repaymentPeriod1OneDayAfterCloseDate = "03 February 2023";
        final String repaymentPeriod2DueDate = "01 March 2023";
        final String repaymentPeriod3DueDate = "01 April 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {

            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicWithInterest();
            loanId.set(applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay));

            LOG.info("Test Loan Product Id {1} http://localhost:4200/#/products/loan-products/{}/general", localLoanProductId);
            LOG.info("Test Client Id {1} http://localhost:4200/#/clients/{}", client.getClientId());
            LOG.info("Test Loan Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(), loanId);

            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId.get(), 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId.get(), 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId.get(), 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId.get(), 60.0, repaymentPeriod3DueDate);

        });
        runAt(repaymentPeriod1OneDayBeforeCloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0));
        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0),
                    transaction(70.65, "Accrual", "01 February 2023", 0, 0, 0.65, 40, 30, 0.0, 0.0),
                    transaction(90.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 40.0, 30.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1OneDayAfterCloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(19.35, "Accrual", "31 January 2023", 0, 0, 19.35, 0, 0, 0.0, 0.0),
                    transaction(70.65, "Accrual", "01 February 2023", 0, 0, 0.65, 40, 30, 0.0, 0.0),
                    transaction(90.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 40.0, 30.0, 0.0, 0.0),
                    transaction(0.71, "Accrual", "02 February 2023", 0, 0, 0.71, 0, 0, 0.0, 0.0));

        });
    }

    // Create Loan with Interest and enabled Accrual Activity Posting
    // Approve and disburse loan
    // make partial repayment before first installment day
    // run COB on closing day of first installment
    // verify that the Accrual Activity transaction is created
    // make repayment before the first repayment
    // verify that Accrual Activity transaction is NOT modified/reversed/replayed
    // charge backdated penalty before first installment due date
    // verify that the Accrual Activity transaction is reverse replayed
    // verify that the Accrual Activity holds the correct portions
    // charge backdated penalty before first installment due date
    // verify that the Accrual Activity transaction is reverse replayed
    // verify that the Accrual Activity holds the correct portions

    @Test
    public void testAccrualActivityPostingReverseReplay() {
        final String disbursementDay = "01 January 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String repaymentPeriod1OneDayAfterCloseDate = "03 February 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {

            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicWithInterest();
            loanId.set(applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay));

            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            addRepaymentForLoan(loanId.get(), 50.0, "10 January 2023");
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 970, 30, 20, 0, 0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 20, 0, 0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 970, 30, 20, 0, 0, 0.0, 0.0),
                    transaction(20.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 0.0, 0.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1OneDayAfterCloseDate, () -> {

            addRepaymentForLoan(loanId.get(), 200.0, "8 January 2023");

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 20, 0, 0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 770, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(200.0, "Repayment", "08 January 2023", 820, 180, 20, 0, 0, 0.0, 0.0),
                    transaction(20.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 0.0, 0.0, 0.0, 0.0));

            chargePenalty(loanId.get(), 33.0, "01 February 2023");

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 20, 0, 0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 803, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(200.0, "Repayment", "08 January 2023", 853, 147, 20, 0, 33, 0.0, 0.0),
                    transaction(53.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 0.0, 33.0, 0.0, 0.0));

            chargeFee(loanId.get(), 12.0, "01 February 2023");

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 20, 0, 0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 815, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(200.0, "Repayment", "08 January 2023", 865, 135, 20, 12, 33, 0.0, 0.0),
                    transaction(65.0, "Accrual Activity", "01 February 2023", 0, 0, 20.0, 12.0, 33.0, 0.0, 0.0));

        });
    }

    // Create Loan with Advanced Payment Allocation and enabled Accrual Activity Posting
    // Approve and disburse loan
    // charge penalty for 1st installment
    // make partial repayment before first installment day
    // run COB on closing day of first installment
    // verify that the Accrual Activity transaction is created
    // make repayment before the first repayment
    // verify that Accrual Activity transaction is NOT modified/reversed/replayed
    // charge backdated penalty before first installment due date
    // verify that the Accrual Activity transaction is reverse replayed
    // verify that the Accrual Activity holds the correct portions
    // charge backdated penalty before first installment due date
    // verify that the Accrual Activity transaction is reverse replayed
    // verify that the Accrual Activity holds the correct portions
    @ParameterizedTest
    @CsvSource({ "29 January 2023,30 January 2023,31 January 2023", "31 January 2023,30 January 2023,29 January 2023",
            "31 January 2023,31 January 2023,31 January 2023", "01 February 2023,01 February 2023,01 February 2023" })
    public void testAccrualActivityPostingReverseReplayAdvancedPaymentAllocation(final String chargeDueDate1st,
            final String chargeDueDate2st, final String chargeDueDate3st) {
        final String disbursementDay = "01 January 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String repaymentPeriod1OneDayAfterCloseDate = "03 February 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {

            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation();
            loanId.set(applyForLoanApplicationAdvancedPaymentAllocation(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay));

            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            LOG.info("Test Loan Product Id {1} http://localhost:4200/#/products/loan-products/{}/general", localLoanProductId);
            LOG.info("Test Client Id {1} http://localhost:4200/#/clients/{}", client.getClientId());
            LOG.info("Test Loan Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(), loanId);

            chargePenalty(loanId.get(), 20.0, chargeDueDate1st);

            addRepaymentForLoan(loanId.get(), 50.0, "10 January 2023");
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 970, 30, 0, 0, 20, 0.0, 0.0));

        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 950, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(20.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 0.0, 20.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1OneDayAfterCloseDate, () -> {

            addRepaymentForLoan(loanId.get(), 220.0, "8 January 2023");

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 730, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(220.0, "Repayment", "08 January 2023", 780, 220, 0, 0, 0, 0.0, 0.0),
                    transaction(20.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 0.0, 20.0, 0.0, 0.0));

            chargePenalty(loanId.get(), 33.0, chargeDueDate2st);

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 730, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(220.0, "Repayment", "08 January 2023", 780, 220, 0, 0, 0, 0.0, 0.0),
                    transaction(53.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 0.0, 53.0, 0.0, 0.0));

            chargeFee(loanId.get(), 12.0, chargeDueDate3st);

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(50.0, "Repayment", "10 January 2023", 730, 50, 0, 0, 0, 0.0, 0.0),
                    transaction(220.0, "Repayment", "08 January 2023", 780, 220, 0, 0, 0, 0.0, 0.0),
                    transaction(65.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 12.0, 53.0, 0.0, 0.0));

        });
    }

    // Create Loan with Advanced Payment Allocation and enabled Accrual Activity Posting
    // Approve and disburse loan
    // charge penalty for 1st installment
    // run COB on closing day of first installment
    // verify that the Accrual Activity transaction is created
    // charge backdated fee before first installment due date
    // verify that the Accrual Activity transaction is reverse replayed
    // verify that the Accrual Activity holds the correct portions
    @ParameterizedTest
    @CsvSource({ "29 January 2023,30 January 2023,31 January 2023", "31 January 2023,30 January 2023,29 January 2023",
            "31 January 2023,31 January 2023,31 January 2023", "01 February 2023,01 February 2023,01 February 2023" })
    public void testAccrualActivityPostingReverseReplayAdvancedPaymentAllocationBasicFlow(final String chargeDueDate1st,
            final String chargeDueDate2st, final String chargeDueDate3st) {
        final String disbursementDay = "01 January 2023";
        final String repaymentPeriod1CloseDate = "02 February 2023";
        final String repaymentPeriod1OneDayAfterCloseDate = "03 February 2023";
        final String creationBusinessDay = "15 May 2023";
        AtomicReference<Long> loanId = new AtomicReference<>();
        runAt(creationBusinessDay, () -> {

            Long localLoanProductId = createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation();
            loanId.set(applyForLoanApplicationAdvancedPaymentAllocation(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay));

            loanTransactionHelper.approveLoan(loanId.get(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId.get(), new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            LOG.info("Test Loan Product Id {1} http://localhost:4200/#/products/loan-products/{}/general", localLoanProductId);
            LOG.info("Test Client Id {1} http://localhost:4200/#/clients/{}", client.getClientId());
            LOG.info("Test Loan Id {2} http://localhost:4200/#/clients/{}/loans-accounts/{}/transactions", client.getClientId(), loanId);

            chargePenalty(loanId.get(), 20.0, chargeDueDate1st);

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1CloseDate, () -> {
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId.get()));
            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(20.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 0.0, 20.0, 0.0, 0.0));

        });
        runAt(repaymentPeriod1OneDayAfterCloseDate, () -> {
            chargePenalty(loanId.get(), 33.0, chargeDueDate2st);

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(53.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 0.0, 53.0, 0.0, 0.0));

            chargeFee(loanId.get(), 12.0, chargeDueDate3st);

            verifyTransactions(loanId.get(), //
                    transaction(1000.0, "Disbursement", disbursementDay, 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(20.0, "Accrual", "01 February 2023", 0, 0, 0, 0, 20, 0.0, 0.0),
                    transaction(65.0, "Accrual Activity", "01 February 2023", 0, 0, 0.0, 12.0, 53.0, 0.0, 0.0));

        });
    }

    private void chargeFee(Long loanId, Double amount, String dueDate) {
        LOG.info("----------------------------------CHARGE FEE---------------------------------");
        LOG.info("FEE amount {} dueDate {}", amount, dueDate);
        PostChargesResponse feeCharge = chargesHelper.createCharges(new PostChargesRequest().penalty(false).amount(9.0)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.randomStringGenerator("FEE_" + Calendar.getInstance().getTimeInMillis(), 5)).chargeAppliesTo(1).locale("en")
                .active(true));
        PostLoansLoanIdChargesResponse feeLoanChargeResult = loanTransactionHelper.addChargesForLoan(loanId,
                new PostLoansLoanIdChargesRequest().chargeId(feeCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                        .amount(amount).dueDate(dueDate));
        assertNotNull(feeLoanChargeResult);
        assertNotNull(feeLoanChargeResult.getResourceId());
    }

    private void chargePenalty(Long loanId, Double amount, String dueDate) {
        LOG.info("----------------------------------CHARGE PENALTY---------------------------------");
        LOG.info("PENALTY amount {} dueDate {}", amount, dueDate);
        PostChargesResponse penaltyCharge = chargesHelper.createCharges(new PostChargesRequest().penalty(true).amount(10.0)
                .chargeCalculationType(ChargeCalculationType.FLAT.getValue()).chargeTimeType(ChargeTimeType.SPECIFIED_DUE_DATE.getValue())
                .chargePaymentMode(ChargePaymentMode.REGULAR.getValue()).currencyCode("USD")
                .name(Utils.randomStringGenerator("PENALTY_" + Calendar.getInstance().getTimeInMillis(), 5)).chargeAppliesTo(1).locale("en")
                .active(true));
        PostLoansLoanIdChargesResponse penaltyLoanChargeResult = loanTransactionHelper.addChargesForLoan(loanId,
                new PostLoansLoanIdChargesRequest().chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en")
                        .amount(amount).dueDate(dueDate));
        assertNotNull(penaltyLoanChargeResult);
        assertNotNull(penaltyLoanChargeResult.getResourceId());
        LOG.info("----------------------------------CHARGE PENALTY---------------------------------");
    }

    private Long createLoanProductAccountingAccrualPeriodicWithInterest() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        return loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .description("Test loan description").currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1)
                .interestRecalculationCompoundingMethod(0).recalculationRestFrequencyType(1).rescheduleStrategyMethod(1)
                .recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false).interestRateFrequencyType(2).locale("en_GB")
                .numberOfRepayments(4).repaymentFrequencyType(2L).interestRatePerPeriod(2.0).repaymentEvery(1).minPrincipal(100.0)
                .principal(1000.0).maxPrincipal(10000000.0).amortizationType(1).interestType(1).interestCalculationPeriodType(1)
                .dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY).accountingRule(3)
                .enableAccrualActivityPosting(true).fundSourceAccountId(fundSource.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//
        ).getResourceId();
    }

    private static Long applyForLoanApplicationWithInterest(final Long clientID, final Long loanProductID, BigDecimal principal,
            String applicationDisbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate)
                .clientId(clientID).productId(loanProductID);
        return loanTransactionHelper.applyLoan(loanRequest).getLoanId();
    }

    private Long createLoanProductAccountingAccrualPeriodicAdvancedPaymentAllocation() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        return loanTransactionHelper.createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName)
                .description("Test loan description").currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1)
                .recalculationRestFrequencyType(1).rescheduleStrategyMethod(1).loanScheduleType(LoanScheduleType.PROGRESSIVE.name())
                .recalculationRestFrequencyInterval(0).isInterestRecalculationEnabled(false).locale("en_GB").numberOfRepayments(4)
                .repaymentFrequencyType(2L).repaymentEvery(1).minPrincipal(100.0).principal(1000.0).maxPrincipal(10000000.0)
                .amortizationType(1).interestType(0).interestRatePerPeriod(0.0).interestRateFrequencyType(1)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).paymentAllocation(List.of(defaultAllocation))
                .accountingRule(3).enableAccrualActivityPosting(true).fundSourceAccountId(fundSource.getAccountID().longValue())//
                .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromPenaltyAccountId(feeIncomeAccount.getAccountID().longValue())//
                .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                .receivableFeeAccountId(feeReceivableAccount.getAccountID().longValue())//
                .receivablePenaltyAccountId(penaltyReceivableAccount.getAccountID().longValue())//
                .goodwillCreditAccountId(goodwillExpenseAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(goodwillIncomeAccount.getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(interestIncomeChargeOffAccount.getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(feeChargeOffAccount.getAccountID().longValue())//
                .chargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(chargeOffFraudExpenseAccount.getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(penaltyChargeOffAccount.getAccountID().longValue())//
        ).getResourceId();
    }

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).collect(Collectors.toList());
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static Long applyForLoanApplicationAdvancedPaymentAllocation(final Long clientID, final Long loanProductID,
            BigDecimal principal, String applicationDisbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .repaymentEvery(1).principal(principal).amortizationType(1).interestType(0).interestRatePerPeriod(BigDecimal.ZERO)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY).loanType("individual")
                .expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate).clientId(clientID)
                .productId(loanProductID);
        return loanTransactionHelper.applyLoan(loanRequest).getLoanId();
    }

}
