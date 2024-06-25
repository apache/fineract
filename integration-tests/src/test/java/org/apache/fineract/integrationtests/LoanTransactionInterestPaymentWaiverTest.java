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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import org.apache.fineract.batch.command.internal.CreateTransactionLoanCommandStrategy;
import org.apache.fineract.batch.domain.BatchRequest;
import org.apache.fineract.batch.domain.BatchResponse;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.BusinessDateRequest;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesRequest;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BatchHelper;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CollateralManagementHelper;
import org.apache.fineract.integrationtests.common.LoanRescheduleRequestHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.system.CodeHelper;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.charge.domain.ChargePaymentMode;
import org.apache.fineract.portfolio.charge.domain.ChargeTimeType;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.apache.http.HttpStatus;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanTransactionInterestPaymentWaiverTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(AdvancedPaymentAllocationLoanRepaymentScheduleTest.class);
    private static final String DATETIME_PATTERN = "dd MMMM yyyy";
    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static BusinessDateHelper businessDateHelper;
    private static LoanTransactionHelper loanTransactionHelper;
    private static AccountHelper accountHelper;
    private static Integer commonLoanProductId;
    private static PostClientsResponse client;
    private static LoanRescheduleRequestHelper loanRescheduleRequestHelper;
    private static ChargesHelper chargesHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        businessDateHelper = new BusinessDateHelper();
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        loanRescheduleRequestHelper = new LoanRescheduleRequestHelper(requestSpec, responseSpec);
        chargesHelper = new ChargesHelper();

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        commonLoanProductId = createLoanProduct("500", "15", "4", true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    @Test
    public void testInterestPaymentWaiverTransactionForProgressiveLoan() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            Long repayment1TransactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023");

            assertNotNull(repayment1TransactionId);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, 0, 0, 0, 0.0, true, "01 February 2023", 750.0), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            Long repayment2TransactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "3 January 2023");

            assertNotNull(repayment2TransactionId);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "03 January 2023", 500.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0));

        });
    }

    @Test
    public void testInterestPaymentWaiverTransactionValidationErrorTests() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            // loan should be active
            assertThrows(Exception.class, () -> addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023"));

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // transaction cant be made before disbursement
            assertThrows(Exception.class, () -> addInterestPaymentWaiverForLoan(loanId, 250.0, "30 December 2022"));
        });
    }

    @Test
    public void testInterestPaymentWaiverTransactionForProgressiveLoanWithExternalTransactionId() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            String loanExternalIdStr = UUID.randomUUID().toString();
            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, loanExternalIdStr);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            // Check whether the provided external id was retrieved
            String transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse interestPaymentWaiverResultWithExternalId = loanTransactionHelper
                    .makeGoodwillCredit(loanExternalIdStr, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                            .transactionDate("03 January 2023").locale("en").transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, interestPaymentWaiverResultWithExternalId.getResourceExternalId());

            GetLoansLoanIdTransactionsTransactionIdResponse response = loanTransactionHelper.getLoanTransactionDetails(loanId,
                    transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr,
                    interestPaymentWaiverResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = loanTransactionHelper.getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
        });
    }

    @Test
    public void testInterestPaymentWaiverTransactionReversePaymentForProgressiveLoan() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            Long repayment1TransactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023");

            assertNotNull(repayment1TransactionId);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, 0, 0, 0, 0.0, true, "01 February 2023", 750.0), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.reverseRepayment(Math.toIntExact(loanId), Math.toIntExact(repayment1TransactionId), "2 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0, true));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, 0, 0, 0, 250.0, false, "01 February 2023", 750.0), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

        });
    }

    @Test
    public void testInterestPaymentWaiverTransactionChargeBackForProgressiveLoan() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            Long repayment1TransactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023");

            assertNotNull(repayment1TransactionId);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, 0, 0, 0, 0.0, true, "01 February 2023", 750.0), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            addChargebackForLoan(loanId, repayment1TransactionId, 250.0);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0),
                    transaction(250.0, "Chargeback", "15 January 2023", 1000.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(500.0, 0, 0, 0, 250.0, false, "01 February 2023", 750.0), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

        });
    }

    private Long applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(Long clientId, Long loanProductId,
            Integer numberOfRepayments, String loanDisbursementDate, double amount, String externalLoanId) {
        LOG.info("------------------------------APPLY AND APPROVE LOAN ---------------------------------------");
        PostLoansRequest applicationRequest = applyLoanRequestProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                loanProductId, externalLoanId, amount, numberOfRepayments, loanDisbursementDate);

        PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

        Long loanId = loanResponse.getLoanId();

        assertNotNull(loanId);

        loanTransactionHelper.approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(amount))
                .dateFormat(DATETIME_PATTERN).approvedOnDate(loanDisbursementDate).locale("en"));

        return loanId;
    }

    private PostLoansRequest applyLoanRequestProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(Long clientId, Long loanId,
            String loanExternalId, double amount, Integer numberOfRepayments, String loanDisbursementDate) {
        PostLoansRequest postLoansRequest = new PostLoansRequest().clientId(clientId).productId(loanId)
                .submittedOnDate(loanDisbursementDate).expectedDisbursementDate(loanDisbursementDate).dateFormat(DATETIME_PATTERN)
                .locale("en").loanType("individual")
                .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY).amortizationType(1)
                .interestRatePerPeriod(BigDecimal.ZERO).interestCalculationPeriodType(1).interestType(0)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(amount)).principal(BigDecimal.valueOf(amount))
                .loanTermFrequencyType(RepaymentFrequencyType.MONTHS).loanTermFrequency(numberOfRepayments)
                .repaymentFrequencyType(RepaymentFrequencyType.MONTHS).repaymentEvery(1).numberOfRepayments(numberOfRepayments);
        if (loanExternalId != null) {
            postLoansRequest.externalId(loanExternalId);
        }
        return postLoansRequest;
    }

    // PW UC3: Overpayment
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. Overpay 2nd installment
    @Test
    public void testInterestPaymentWaiverUC3() {
        runAt("15 February 2023", () -> {

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 January 2023").locale("en").transactionAmount(150.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 225.0, 275.0, 225.0, 275.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 25.0, 100.0, 25.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 150.0, 150.0, 0.0, 225.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("31 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 100.0, 400.0, 100.0, 400.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 25.0, 100.0, 25.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 100.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 February 2023").locale("en").transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, 25.0);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 25.0, 0.0);
            validateLoanTransaction(loanDetails, 4, 125.0, 100.0, 25.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());
        });
    }

    // PW UC12: Refund last installment
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. Interest Payment Waiver in advance
    // 4. Pay rest on time
    @Test
    public void testInterestPaymentWaiverUC12() {
        runAt("15 February 2023", () -> {

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("08 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("31 January 2023").locale("en").transactionAmount(50.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    // PW UC13: Refund last installment
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. InterestPaymentWaiver in advance
    // 4. Pay rest on time
    @Test
    public void testInterestPaymentWaiverUC13() {
        runAt("15 February 2023", () -> {

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("08 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("31 January 2023").locale("en").transactionAmount(50.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    // PW UC15: Interest Payment Waiver PD
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. Pay first installment on time - fails
    // 4. Refund (DP due and in advance)
    // 5. Pay rest on time
    @Test
    public void testInterestPaymentWaiverUC15() {
        runAt("15 February 2023", () -> {

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            loanTransactionHelper.reverseLoanTransaction(loanResponse.getLoanId(), loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("01 January 2023")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 500.0, 0.0, 500.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 300.0, 200.0, 300.0, 200.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 300.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("31 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 4, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 February 2023").locale("en").transactionAmount(50.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 5, 50.0, 50.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    // PW UC17c: Full refund with CBR
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan
    // 2. Pay down payment
    // 3. Pay first installment on time
    // 4. Full Goodwill credit
    // 5. CBR
    @Test
    public void testInterestPaymentWaiverUC17c() {
        runAt("15 February 2023", () -> {

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId,
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 January 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("08 January 2023").locale("en").transactionAmount(500.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, 125.0);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 500.0, 375.0, 125.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeCreditBalanceRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("09 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 0.0, 125.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
        });
    }

    /**
     * Tests successful run of batch Interest Payment Waiver for loans. 200(OK) status is returned for successful
     * responses. It first creates a new loan, approves and disburses the loan. Then a Interest Payment Waiver request
     * is made
     *
     * @see CreateTransactionLoanCommandStrategy
     */
    @Test
    public void shouldReturnOkStatusForBatchInterestPaymentWaiver() {

        final String loanProductJSON = new LoanProductTestBuilder() //
                .withPrincipal("1000.00") //
                .withNumberOfRepayments("24") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("2") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualPrincipalPayment() //
                .withInterestTypeAsDecliningBalance() //
                .currencyDetails("0", "100").build(null);

        final Integer clientID = ClientHelper.createClient(requestSpec, responseSpec);
        ClientHelper.verifyClientCreatedOnServer(requestSpec, responseSpec, clientID);

        final Integer collateralId = CollateralManagementHelper.createCollateralProduct(requestSpec, responseSpec);
        Assertions.assertNotNull(collateralId);
        final Integer clientCollateralId = CollateralManagementHelper.createClientCollateral(requestSpec, responseSpec, clientID.toString(),
                collateralId);
        Assertions.assertNotNull(clientCollateralId);

        final Integer productId = new LoanTransactionHelper(requestSpec, responseSpec).getLoanProductId(loanProductJSON);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long interestPaymentWaiverRequestId = disburseLoanRequestId + 1;

        // Create a createClient Request
        final BatchRequest br1 = BatchHelper.createActiveClientRequest(createActiveClientRequestId, "");

        // Create a ApplyLoan Request
        final BatchRequest br2 = BatchHelper.applyLoanRequest(applyLoanRequestId, createActiveClientRequestId, productId,
                clientCollateralId);

        // Create a approveLoan Request
        final BatchRequest br3 = BatchHelper.approveLoanRequest(approveLoanRequestId, applyLoanRequestId);

        // Create a disburseLoan Request
        final BatchRequest br4 = BatchHelper.disburseLoanRequest(disburseLoanRequestId, approveLoanRequestId);

        // Create a Interest Payment Waiver request.
        final BatchRequest br5 = BatchHelper.interestPaymentWaiverRequest(interestPaymentWaiverRequestId, disburseLoanRequestId, "500");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);

        final String batchRequestStr = BatchHelper.toJsonString(batchRequests);

        final List<BatchResponse> response = BatchHelper.postBatchRequestsWithoutEnclosingTransaction(requestSpec, responseSpec,
                batchRequestStr);

        Assertions.assertEquals(HttpStatus.SC_OK, (long) response.get(4).getStatusCode(), "Verify Status Code 200 for Goodwill credit");
    }

    // PW UC112: Advanced payment allocation, horizontal repayment processing
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan (1000)
    // 2. Add charge after maturity date
    // 3. Pay 1st installment
    // 4. Pay 2nd installment
    // 5. Add charge to 3rd installment
    // 6. Add charge to 4th installment
    // 7. Do Interest Payment Waiver (in advance payment)
    @Test
    public void testInterestPaymentWaiverUC112() {
        runAt("01 September 2023", () -> {

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();
            Integer localLoanProductId = createLoanProduct("1000", "15", "3", true, "25", false, LoanScheduleType.PROGRESSIVE,
                    LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), localLoanProductId,
                    BigDecimal.valueOf(1000.0), 45, 15, 3, BigDecimal.ZERO, "01 September 2023", "01 September 2023");

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 September 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));
            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "17 October 2023", "20"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1020.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("01 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 770.0, 250.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.09.16").dateFormat("yyyy.MM.dd").locale("en"));

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 520.0, 500.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "17 September 2023", "20"));
            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "16 October 2023", "20"));

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 September 2023").locale("en").transactionAmount(50.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 510.0, 550.0, 490.0, 510.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 10.0, 240.0, 0.0, 0.0, 0.0, 20.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 30.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0, 0.0, 0.0, 0.0,
                    0.0, 20.0, 0.0);

            validateLoanCharge(loanDetails, 0, LocalDate.of(2023, 9, 17), 20.0, 0.0, 20.0);
            validateLoanCharge(loanDetails, 1, LocalDate.of(2023, 10, 16), 20.0, 20.0, 0.0);
            validateLoanCharge(loanDetails, 2, LocalDate.of(2023, 10, 17), 20.0, 20.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());
        });
    }

    // PW UC113: Advanced payment allocation, vertical repayment processing
    // ADVANCED_PAYMENT_ALLOCATION_STRATEGY
    // 1. Disburse the loan (1000)
    // 2. Add charge after maturity date
    // 3. Pay 1st installment
    // 4. Pay 2nd installment
    // 5. Add charge to 3rd installment
    // 6. Add charge to 4th installment
    // 7. Do Interest Payment Waiver (in advance payment)
    @Test
    public void testInterestPaymentWaiverUC113() {
        runAt("01 September 2023", () -> {

            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();
            Integer localLoanProductId = createLoanProduct("1000", "15", "3", true, "25", false, LoanScheduleType.PROGRESSIVE,
                    LoanScheduleProcessingType.VERTICAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), localLoanProductId,
                    BigDecimal.valueOf(1000.0), 45, 15, 3, BigDecimal.ZERO, "01 September 2023", "01 September 2023",
                    LoanScheduleProcessingType.VERTICAL);

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("01 September 2023").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // Add Charge Penalty
            Integer penalty = ChargesHelper.createCharges(requestSpec, responseSpec,
                    ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, "20", true));
            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "17 October 2023", "20"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1020.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("01 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 770.0, 250.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            businessDateHelper.updateBusinessDate(new BusinessDateRequest().type(BusinessDateType.BUSINESS_DATE.getName())
                    .date("2023.09.16").dateFormat("yyyy.MM.dd").locale("en"));

            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 520.0, 500.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "17 September 2023", "20"));
            loanTransactionHelper.addChargesForLoan(loanResponse.getLoanId().intValue(),
                    LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(String.valueOf(penalty), "16 October 2023", "20"));

            loanTransactionHelper.makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 September 2023").locale("en").transactionAmount(50.0));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 510.0, 550.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 20.0, 10.0, 10.0, 0.0, 0.0,
                    0.0, 10.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 20.0, 20.0, 0.0, 0.0, 0.0,
                    0.0, 20.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0, 0.0, 0.0, 0.0,
                    0.0, 20.0, 0.0);
            validateLoanCharge(loanDetails, 0, LocalDate.of(2023, 9, 17), 20.0, 10.0, 10.0);
            validateLoanCharge(loanDetails, 1, LocalDate.of(2023, 10, 16), 20.0, 20.0, 0.0);
            validateLoanCharge(loanDetails, 2, LocalDate.of(2023, 10, 17), 20.0, 20.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());
        });
    }

    // Create Loan with Interest for Accural accounting
    // Approve and disburse loan
    // charge penalty with due date as 1st installment
    // charge fee with due date as 1st installment
    // charge penalty with due date as 3rd installment
    // charge fee with due date as 2nd installment
    // pay 1st-3rd installment on time by Interest Payment Waiver
    // pay 4th (last) installment on time with overpayment by Interest Payment Waiver
    // reverse 4 - 1 installment Payment Waiver transactions
    @Test
    public void testAccounting() {
        runAt("15 May 2023", () -> {

            final String disbursementDay = "01 January 2023";
            final String repaymentPeriod1DueDate = "01 February 2023";
            final String repaymentPeriod2DueDate = "01 March 2023";
            final String repaymentPeriod3DueDate = "01 April 2023";
            final String repaymentPeriod4DueDate = "01 May 2023";

            Long localLoanProductId = createLoanProductAccountingAccuralPeriodicWithInterest();
            final Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay);

            loanTransactionHelper.approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId, 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId, 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId, 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId, 60.0, repaymentPeriod3DueDate);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 1260.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 0.0, 250.0, 40.0, 0.0, 40.0, 30.0, 0.0, 30.0, 20.0,
                    0.0, 20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // transaction 1
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate)
                            .locale("en").transactionAmount(340.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 920.0, 340.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr1.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(30, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(40, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"));

            verifyJournalEntries(loanId, journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(30, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(40, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"));

            // transaction 2
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate)
                            .locale("en").transactionAmount(320.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 600.0, 660.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr2.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"));

            verifyJournalEntries(loanId, journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(30, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(40, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"));

            // transaction 3
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate)
                            .locale("en").transactionAmount(330.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 270.0, 990.0, 250.0, 750.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr3.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(60, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(330.0, interestIncomeAccount, "DEBIT"));

            verifyJournalEntries(loanId, journalEntry(1000.0, fundSource, "CREDIT"), //
                    journalEntry(1000.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(30, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(40, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(60, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(330.0, interestIncomeAccount, "DEBIT"));

            // transaction 4 + overpayment
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate)
                            .locale("en").transactionAmount(350.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 1260.0, 0.0, 1000.0, 80.0);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            assertFalse(loanDetails.getStatus().getActive());

            // Because of closing and other reason we skip validating loan's journal entries

            verifyTRJournalEntries(interestPaymentWaiverTr4.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(80, overpaymentAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(350.0, interestIncomeAccount, "DEBIT"));

            // reverse transaction 4
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr4.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 270.0, 990.0, 250.0, 750.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr4Reverse.getResourceId(), journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(80, overpaymentAccount, "DEBIT"), //
                    journalEntry(20, interestReceivableAccount, "DEBIT"), //
                    journalEntry(350.0, interestIncomeAccount, "CREDIT"), //
                    journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(80, overpaymentAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(350.0, interestIncomeAccount, "DEBIT"));

            // reverse transaction 3
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr3.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 600.0, 660.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr3Reverse.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(60, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(330.0, interestIncomeAccount, "DEBIT"), //

                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(60, feeReceivableAccount, "DEBIT"), //
                    journalEntry(20, interestReceivableAccount, "DEBIT"), //
                    journalEntry(330.0, interestIncomeAccount, "CREDIT"));

            // reverse transaction 2
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr2.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 920.0, 340.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr2Reverse.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(50, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"), //

                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(50, penaltyReceivableAccount, "DEBIT"), //
                    journalEntry(20, interestReceivableAccount, "DEBIT"), //
                    journalEntry(320.0, interestIncomeAccount, "CREDIT"));

            // reverse transaction 1
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 1260.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 0.0, 250.0, 40.0, 0.0, 40.0, 30.0, 0.0, 30.0, 20.0,
                    0.0, 20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr1Reverse.getResourceId(), journalEntry(250.0, loansReceivableAccount, "CREDIT"), //
                    journalEntry(30, penaltyReceivableAccount, "CREDIT"), //
                    journalEntry(40, feeReceivableAccount, "CREDIT"), //
                    journalEntry(20, interestReceivableAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"), //

                    journalEntry(250.0, loansReceivableAccount, "DEBIT"), //
                    journalEntry(30, penaltyReceivableAccount, "DEBIT"), //
                    journalEntry(40, feeReceivableAccount, "DEBIT"), //
                    journalEntry(20, interestReceivableAccount, "DEBIT"), //
                    journalEntry(340.0, interestIncomeAccount, "CREDIT"));
        });
    }

    // Create Loan with Interest for Accural accounting
    // Approve and disburse loan
    // charge penalty with due date as 1st installment
    // charge fee with due date as 1st installment
    // charge penalty with due date as 3rd installment
    // charge fee with due date as 2nd installment
    // Charge-OFF loan
    // pay 1st-3rd installment on time by Interest Payment Waiver
    // pay 4th (last) installment on time with overpayment by Interest Payment Waiver
    // reverse 4 - 1 Interest Payment Waiver transactions
    @Test
    public void testInterestPaymentWaiverTransactionAccountingAccuralForInterestPenaltyFeeOverpaymentChargeOFFLoan() {
        runAt("15 May 2023", () -> {

            final String disbursementDay = "01 January 2023";
            final String repaymentPeriod1DueDate = "01 February 2023";
            final String repaymentPeriod2DueDate = "01 March 2023";
            final String repaymentPeriod3DueDate = "01 April 2023";
            final String repaymentPeriod4DueDate = "01 May 2023";

            Long localLoanProductId = createLoanProductAccountingAccuralPeriodicWithInterest();
            final Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay);

            loanTransactionHelper.approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate(disbursementDay).locale("en"));

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay)
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId, 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId, 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId, 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId, 60.0, repaymentPeriod3DueDate);

            // Charge-OFF loan
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Integer chargeOffReasonId = CodeHelper.createChargeOffCodeValue(requestSpec, responseSpec, randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse chargeOffTransaction = loanTransactionHelper.chargeOffLoan(loanId,
                    new PostLoansLoanIdTransactionsRequest().transactionDate("2 January 2023").locale("en").dateFormat("dd MMMM yyyy")
                            .externalId(transactionExternalId).chargeOffReasonId((long) chargeOffReasonId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 1260.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 0.0, 250.0, 40.0, 0.0, 40.0, 30.0, 0.0, 30.0, 20.0,
                    0.0, 20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // transaction 1
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate)
                            .locale("en").transactionAmount(340.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 920.0, 340.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr1.getResourceId(), journalEntry(340.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"));

            // transaction 2
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate)
                            .locale("en").transactionAmount(320.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 600.0, 660.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr2.getResourceId(), journalEntry(320.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"));

            // transaction 3
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate)
                            .locale("en").transactionAmount(330.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 270.0, 990.0, 250.0, 750.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr3.getResourceId(), journalEntry(330.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(330.0, interestIncomeAccount, "DEBIT"));

            // transaction 4 + overpayment
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4 = loanTransactionHelper.makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate)
                            .locale("en").transactionAmount(350.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 1260.0, 0.0, 1000.0, 80.0);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            assertFalse(loanDetails.getStatus().getActive());

            // Because of closing and other reason we skip validating loan's journal entries

            verifyTRJournalEntries(interestPaymentWaiverTr4.getResourceId(), journalEntry(270.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(80, overpaymentAccount, "CREDIT"), //
                    journalEntry(350.0, interestIncomeAccount, "DEBIT"));

            // reverse transaction 4
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr4.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 270.0, 990.0, 250.0, 750.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 250.0, 0.0, 60.0, 60.0, 0.0, 0.0, 0.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr4Reverse.getResourceId(),
                    journalEntry(270.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(80, overpaymentAccount, "CREDIT"), //
                    journalEntry(350.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(270.0, interestIncomeChargeOffAccount, "DEBIT"), //
                    journalEntry(80, overpaymentAccount, "DEBIT"), //
                    journalEntry(350.0, interestIncomeAccount, "CREDIT"));

            // reverse transaction 3
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr3.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 600.0, 660.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 250.0, 0.0, 0.0, 0.0, 0.0, 50.0, 50.0, 0.0, 20.0, 20.0,
                    0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr3Reverse.getResourceId(),
                    journalEntry(330.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(330.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(330.0, interestIncomeChargeOffAccount, "DEBIT"), //
                    journalEntry(330.0, interestIncomeAccount, "CREDIT"));

            // reverse transaction 2
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr2.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 920.0, 340.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 250.0, 0.0, 40.0, 40.0, 0.0, 30.0, 30.0, 0.0, 20.0,
                    20.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr2Reverse.getResourceId(),
                    journalEntry(320.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(320.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(320.0, interestIncomeChargeOffAccount, "DEBIT"), //
                    journalEntry(320.0, interestIncomeAccount, "CREDIT"));

            // reverse transaction 1
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1Reverse = loanTransactionHelper.reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 1260.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 250.0, 0.0, 250.0, 40.0, 0.0, 40.0, 30.0, 0.0, 30.0, 20.0,
                    0.0, 20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 50.0, 0.0, 50.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 250.0, 0.0, 250.0, 60.0, 0.0, 60.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 250.0, 0.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0,
                    20.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTRJournalEntries(interestPaymentWaiverTr1Reverse.getResourceId(),
                    journalEntry(340.0, interestIncomeChargeOffAccount, "CREDIT"), //
                    journalEntry(340.0, interestIncomeAccount, "DEBIT"), //
                    journalEntry(340.0, interestIncomeChargeOffAccount, "DEBIT"), //
                    journalEntry(340.0, interestIncomeAccount, "CREDIT"));
        });
    }

    @Test
    public void testInterestPaymentWaiverAdjustTransaction() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            PostLoanProductsResponse loanProductResponse = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                            .loanScheduleType(LoanScheduleType.PROGRESSIVE.toString()));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId,
                    loanProductResponse.getResourceId(), numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            loanTransactionHelper.disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // loan should be active
            Long transactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0));

            loanTransactionHelper.adjustLoanTransaction(loanId, transactionId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .transactionAmount(200.0).dateFormat(DATETIME_PATTERN).transactionDate("3 January 2023").locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0, true),
                    transaction(200.0, "Interest Payment Waiver", "03 January 2023", 800.0, 200.0, 0.0, 0.0, 0, 0.0, 0.0));
        });
    }

    private void chargeFee(Long loanId, Double amount, String dueDate) {
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
    }

    private Long createLoanProductAccountingAccuralPeriodicWithInterest() {
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
                .fundSourceAccountId(fundSource.getAccountID().longValue())//
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

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, LoanScheduleType loanScheduleType,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        AdvancedPaymentData goodwillCreditAllocation = createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT");
        AdvancedPaymentData interestPaymentWaiver = createPaymentAllocation("INTEREST_PAYMENT_WAIVER", "LAST_INSTALLMENT");
        AdvancedPaymentData merchantIssuedRefundAllocation = createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION");
        AdvancedPaymentData payoutRefundAllocation = createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT");
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(downPaymentEnabled, downPaymentPercentage, autoPayForDownPayment).withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation, goodwillCreditAllocation, merchantIssuedRefundAllocation,
                        payoutRefundAllocation, interestPaymentWaiver)
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withInterestTypeAsDecliningBalance().withMultiDisburse()
                .withDisallowExpectedDisbursements(true).withLoanScheduleType(loanScheduleType)
                .withLoanScheduleProcessingType(loanScheduleProcessingType).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").build(null);
        return loanTransactionHelper.getLoanProductId(loanProductJSON);
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

    private static AdvancedPaymentData createPaymentAllocation(String transactionType, String futureInstallmentAllocationRule) {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType(transactionType);
        advancedPaymentData.setFutureInstallmentAllocationRule(futureInstallmentAllocationRule);

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String transactionProcessorCode,
            String loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanTransactionHelper.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(transactionProcessorCode).locale("en").submittedOnDate(submittedOnDate)
                .amortizationType(1).interestRatePerPeriod(interestRate).interestCalculationPeriodType(1).interestType(0)
                .repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery).repaymentFrequencyType(0)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency).loanTermFrequencyType(0).principal(principal)
                .loanType("individual").loanScheduleProcessingType(loanScheduleProcessingType)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, LoanScheduleProcessingType.HORIZONTAL);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, LoanScheduleProcessingType loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate,
                AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY, loanScheduleProcessingType.name());
    }

    private static void validateLoanTransaction(GetLoansLoanIdResponse loanDetails, int index, double transactionAmount,
            double principalPortion, double overPaidPortion, double loanBalance) {
        assertEquals(transactionAmount, loanDetails.getTransactions().get(index).getAmount());
        assertEquals(principalPortion, loanDetails.getTransactions().get(index).getPrincipalPortion());
        assertEquals(overPaidPortion, loanDetails.getTransactions().get(index).getOverpaymentPortion());
        assertEquals(loanBalance, loanDetails.getTransactions().get(index).getOutstandingLoanBalance());
    }

    private void validateLoanCharge(GetLoansLoanIdResponse loanDetails, int index, LocalDate dueDate, double charged, double paid,
            double outstanding) {
        GetLoansLoanIdLoanChargeData chargeData = loanDetails.getCharges().get(index);
        assertEquals(dueDate, chargeData.getDueDate());
        assertEquals(charged, chargeData.getAmount());
        assertEquals(paid, chargeData.getAmountPaid());
        assertEquals(outstanding, chargeData.getAmountOutstanding());
    }

}
