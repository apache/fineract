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

import static org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.http.HttpStatus.SC_BAD_REQUEST;
import static org.apache.http.HttpStatus.SC_FORBIDDEN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;

import com.google.gson.Gson;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BatchRequest;
import org.apache.fineract.client.models.BatchResponse;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargeData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBatchHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCollateralHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.AmortizationType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearCustomStrategy;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.InterestCalculationPeriodType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class LoanTransactionInterestPaymentWaiverTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(LoanTransactionInterestPaymentWaiverTest.class);
    private static final Gson GSON = new Gson();
    private static final String HORIZONTAL = "HORIZONTAL";
    private static final String VERTICAL = "VERTICAL";
    private static Long commonLoanProductId;
    private static PostClientsResponse client;

    private final FeignBatchHelper batchHelper = new FeignBatchHelper(FineractFeignClientHelper.getFineractFeignClient());
    private final FeignCollateralHelper collateralHelper = new FeignCollateralHelper(FineractFeignClientHelper.getFineractFeignClient());

    @BeforeAll
    public static void setupTestData() {
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
    }

    private Long commonLoanProductId() {
        if (commonLoanProductId == null) {
            commonLoanProductId = createLoanProduct(500.0, 15, 4, true, "25", true, HORIZONTAL);
        }
        return commonLoanProductId;
    }

    @Test
    public void testInterestPaymentWaiverTransactionForProgressiveLoan() {
        runAt("15 January 2023", () -> {
            Integer numberOfRepayments = 4;
            double amount = 1000.0;
            String loanDisbursementDate = "1 January 2023";

            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();

            LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

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
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            // loan should be active - LoanDownPaymentTransactionValidator raises a data-validation error here
            // loan should be active
            CallFailedRuntimeException notActive = assertThrows(CallFailedRuntimeException.class,
                    () -> addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023"));
            assertEquals(SC_BAD_REQUEST, notActive.getStatus());
            assertTrue(notActive.getResponseBody().contains("error.msg.loan.must.be.active.fully.paid.or.overpaid"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // transaction cant be made before disbursement - InvalidLoanStateTransitionException maps to 403
            // transaction cant be made before disbursement
            CallFailedRuntimeException beforeDisbursement = assertThrows(CallFailedRuntimeException.class,
                    () -> addInterestPaymentWaiverForLoan(loanId, 250.0, "30 December 2022"));
            assertEquals(SC_FORBIDDEN, beforeDisbursement.getStatus());
            assertTrue(beforeDisbursement.getResponseBody().contains("cannot.be.before.disbursement.date"));
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
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            String loanExternalIdStr = UUID.randomUUID().toString();
            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, loanExternalIdStr);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0));

            // Check whether the provided external id was retrieved
            String transactionExternalIdStr = UUID.randomUUID().toString();
            final PostLoansLoanIdTransactionsResponse interestPaymentWaiverResultWithExternalId = makeGoodwillCredit(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("03 January 2023").locale("en")
                            .transactionAmount(5.0).externalId(transactionExternalIdStr));
            assertEquals(transactionExternalIdStr, interestPaymentWaiverResultWithExternalId.getResourceExternalId());

            GetLoansLoanIdTransactionsTransactionIdResponse response = getLoanTransactionDetails(loanId, transactionExternalIdStr);
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, interestPaymentWaiverResultWithExternalId.getResourceId());
            assertEquals(transactionExternalIdStr, response.getExternalId());
            response = getLoanTransactionDetails(loanExternalIdStr, transactionExternalIdStr);
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
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

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

            reverseRepayment(loanId, repayment1TransactionId, "2 January 2023");

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
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

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

        PostLoansResponse loanResponse = loanHelper.applyForLoan(applicationRequest);

        Long loanId = loanResponse.getLoanId();

        assertNotNull(loanId);

        approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(amount)).dateFormat(DATETIME_PATTERN)
                .approvedOnDate(loanDisbursementDate).locale("en"));

        return loanId;
    }

    private PostLoansRequest applyLoanRequestProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(Long clientId, Long loanId,
            String loanExternalId, double amount, Integer numberOfRepayments, String loanDisbursementDate) {
        PostLoansRequest postLoansRequest = new PostLoansRequest().clientId(clientId).productId(loanId)
                .submittedOnDate(loanDisbursementDate).expectedDisbursementDate(loanDisbursementDate).dateFormat(DATETIME_PATTERN)
                .locale("en").loanType("individual").transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .amortizationType(1).interestRatePerPeriod(BigDecimal.ZERO).interestCalculationPeriodType(1).interestType(0)
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

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId(),
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 January 2023").locale("en").transactionAmount(150.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 225.0, 275.0, 225.0, 275.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 25.0, 100.0, 25.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 150.0, 150.0, 0.0, 225.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("31 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 100.0, 400.0, 100.0, 400.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 25.0, 100.0, 25.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 100.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 February 2023").locale("en").transactionAmount(125.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId(),
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("08 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("31 January 2023").locale("en").transactionAmount(50.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId(),
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("08 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 January 2023").locale("en").transactionAmount(125.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("31 January 2023").locale("en").transactionAmount(50.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId(),
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            reverseLoanTransaction(loanResponse.getLoanId(), loanDetails.getTransactions().get(1).getId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("01 January 2023")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 500.0, 0.0, 500.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 January 2023").locale("en").transactionAmount(200.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 300.0, 200.0, 300.0, 200.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 200.0, 200.0, 0.0, 300.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 175.0, 325.0, 175.0, 325.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 3, 125.0, 125.0, 0.0, 175.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("31 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 50.0, 450.0, 50.0, 450.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 125.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 75.0, 50.0, 75.0, 0.0);
            validateLoanTransaction(loanDetails, 4, 125.0, 125.0, 0.0, 50.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 February 2023").locale("en").transactionAmount(50.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), commonLoanProductId(),
                    BigDecimal.valueOf(500.0), 45, 15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(500))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 January 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(500.00)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 375.0, 125.0, 375.0, 125.0, null);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 0.0, 125.0, 0.0, 0.0);
            validateLoanTransaction(loanDetails, 1, 125.0, 125.0, 0.0, 375.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("08 January 2023").locale("en").transactionAmount(500.0));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 500.0, 0.0, 500.0, 125.0);
            validateRepaymentPeriod(loanDetails, 1, 125.0, 125.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, 125.0, 125.0, 0.0, 125.0, 0.0);
            validateLoanTransaction(loanDetails, 2, 500.0, 375.0, 125.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeCreditBalanceRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("09 January 2023").locale("en").transactionAmount(125.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

        final Long clientId = createClient();
        assertNotNull(clientId);

        final Long collateralId = collateralHelper.createCollateralProduct().getResourceId();
        assertNotNull(collateralId);
        final Long clientCollateralId = collateralHelper.createClientCollateral(clientId, collateralId).getResourceId();
        assertNotNull(clientCollateralId);

        final Integer productId = getLoanProductId(loanProductJSON);

        final Long createActiveClientRequestId = 4730L;
        final Long applyLoanRequestId = createActiveClientRequestId + 1;
        final Long approveLoanRequestId = applyLoanRequestId + 1;
        final Long disburseLoanRequestId = approveLoanRequestId + 1;
        final Long interestPaymentWaiverRequestId = disburseLoanRequestId + 1;

        final String applyAndApproveDate = dateTimeFormatter.format(LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(10));
        final String disburseDate = dateTimeFormatter.format(LocalDate.now(Utils.getZoneIdOfTenant()).minusDays(8));
        final String waiverDate = dateTimeFormatter.format(LocalDate.now(Utils.getZoneIdOfTenant()));

        // Create a createClient Request
        final BatchRequest br1 = new BatchRequest().requestId(createActiveClientRequestId).relativeUrl("v1/clients").method("POST")
                .body("{ \"officeId\": 1, \"legalFormId\":1, \"firstname\": \"Petra\", \"lastname\": \"Yton\"," + "\"externalId\": \"\","
                        + "  \"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en\","
                        + "\"active\": true, \"activationDate\": \"04 March 2010\", \"submittedOnDate\": \"04 March 2010\"}");

        // Create a ApplyLoan Request
        final BatchRequest br2 = new BatchRequest().requestId(applyLoanRequestId).relativeUrl("v1/loans").method("POST")
                .reference(createActiveClientRequestId)
                .body("{\"dateFormat\": \"dd MMMM yyyy\", \"locale\": \"en_GB\", \"clientId\": \"$.clientId\"," + "\"productId\": "
                        + productId + ", \"principal\": \"10,000.00\", \"loanTermFrequency\": 10,"
                        + "\"loanTermFrequencyType\": 2, \"loanType\": \"individual\", \"numberOfRepayments\": 10,"
                        + "\"repaymentEvery\": 1, \"repaymentFrequencyType\": 2, \"interestRatePerPeriod\": 10,"
                        + "\"amortizationType\": 1, \"interestType\": 0, \"interestCalculationPeriodType\": 1,"
                        + "\"transactionProcessingStrategyCode\": \"mifos-standard-strategy\", \"expectedDisbursementDate\": \""
                        + applyAndApproveDate + "\"," + "\"collateral\": [{\"clientCollateralId\": \"" + clientCollateralId
                        + "\", \"quantity\": \"1\"}]," + "\"submittedOnDate\": \"" + applyAndApproveDate + "\"}");

        // Create a approveLoan Request
        final BatchRequest br3 = new BatchRequest().requestId(approveLoanRequestId).relativeUrl("v1/loans/$.loanId?command=approve")
                .reference(applyLoanRequestId).method("POST")
                .body("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"approvedOnDate\": \"" + applyAndApproveDate + "\","
                        + "\"note\": \"Loan approval note\", \"expectedDisbursementDate\": \"" + applyAndApproveDate + "\"}");

        // Create a disburseLoan Request
        final BatchRequest br4 = new BatchRequest().requestId(disburseLoanRequestId).relativeUrl("v1/loans/$.loanId?command=disburse")
                .reference(approveLoanRequestId).method("POST")
                .body("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", \"actualDisbursementDate\": \"" + disburseDate + "\"}");

        // Create a Interest Payment Waiver request.
        final BatchRequest br5 = new BatchRequest().requestId(interestPaymentWaiverRequestId)
                .relativeUrl("v1/loans/$.loanId/transactions?command=interestPaymentWaiver").reference(disburseLoanRequestId).method("POST")
                .body("{\"locale\": \"en\", \"dateFormat\": \"dd MMMM yyyy\", " + "\"transactionDate\": \"" + waiverDate
                        + "\",  \"transactionAmount\": 500, \"note\":null}");

        final List<BatchRequest> batchRequests = new ArrayList<>();

        batchRequests.add(br1);
        batchRequests.add(br2);
        batchRequests.add(br3);
        batchRequests.add(br4);
        batchRequests.add(br5);

        final List<BatchResponse> response = batchHelper.executeWithoutEnclosingTransaction(batchRequests);

        assertEquals(200, response.get(4).getStatusCode(), "Verify Status Code 200 for interest payment waiver");
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

            Long localLoanProductId = createLoanProduct(1000.0, 15, 3, true, "25", false, HORIZONTAL);
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), localLoanProductId,
                    BigDecimal.valueOf(1000.0), 45, 15, 3, BigDecimal.ZERO, "01 September 2023", "01 September 2023");

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 September 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // Add Charge Penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDatePenalty(20.0).getResourceId();
            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("17 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1020.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("01 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 770.0, 250.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            updateBusinessDate("16 September 2023");

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 520.0, 500.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("17 September 2023").dateFormat(DATETIME_PATTERN).locale("en"));
            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("16 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 September 2023").locale("en").transactionAmount(50.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            Long localLoanProductId = createLoanProduct(1000.0, 15, 3, true, "25", false, VERTICAL);
            final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), localLoanProductId,
                    BigDecimal.valueOf(1000.0), 45, 15, 3, BigDecimal.ZERO, "01 September 2023", "01 September 2023", VERTICAL);

            approveLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000))
                    .dateFormat(DATETIME_PATTERN).approvedOnDate("01 September 2023").locale("en"));

            disburseLoan(loanResponse.getLoanId(), new PostLoansLoanIdRequest().actualDisbursementDate("01 September 2023")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1000.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            // Add Charge Penalty
            Long penalty = chargesHelper.createLoanSpecifiedDueDatePenalty(20.0).getResourceId();
            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("17 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 1020.0, 0.0, 1000.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("01 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 770.0, 250.0, 750.0, 250.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            updateBusinessDate("16 September 2023");

            makeLoanRepayment(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 September 2023").locale("en").transactionAmount(250.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 520.0, 500.0, 500.0, 500.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 9, 1), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 9, 16), 250.0, 250.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 10, 1), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 10, 16), 250.0, 0.0, 250.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 5, LocalDate.of(2023, 10, 17), 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0, 0.0, 20.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("17 September 2023").dateFormat(DATETIME_PATTERN).locale("en"));
            addChargesForLoan(loanResponse.getLoanId(), new PostLoansLoanIdChargesRequest().chargeId(penalty).amount(20.0)
                    .dueDate("16 October 2023").dateFormat(DATETIME_PATTERN).locale("en"));

            makeInterestPaymentWaiver(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("16 September 2023").locale("en").transactionAmount(50.0));
            loanDetails = getLoanDetails(loanResponse.getLoanId());
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

            final Account fundSource = getAccounts().getFundSource();
            final Account loansReceivableAccount = getAccounts().getLoansReceivableAccount();
            final Account interestReceivableAccount = getAccounts().getInterestReceivableAccount();
            final Account feeReceivableAccount = getAccounts().getFeeReceivableAccount();
            final Account penaltyReceivableAccount = getAccounts().getPenaltyReceivableAccount();
            final Account interestIncomeAccount = getAccounts().getInterestIncomeAccount();
            final Account overpaymentAccount = getAccounts().getOverpaymentAccount();

            final String disbursementDay = "01 January 2023";
            final String repaymentPeriod1DueDate = "01 February 2023";
            final String repaymentPeriod2DueDate = "01 March 2023";
            final String repaymentPeriod3DueDate = "01 April 2023";
            final String repaymentPeriod4DueDate = "01 May 2023";

            Long localLoanProductId = createLoanProductAccountingAccuralPeriodicWithInterest();
            final Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay);

            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate(disbursementDay).locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay).dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId, 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId, 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId, 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId, 60.0, repaymentPeriod3DueDate);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate)
                            .locale("en").transactionAmount(340.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate)
                            .locale("en").transactionAmount(320.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate)
                            .locale("en").transactionAmount(330.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate)
                            .locale("en").transactionAmount(350.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr4.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr3.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr2.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
        runAt("2 January 2023", () -> {

            final Account interestIncomeAccount = getAccounts().getInterestIncomeAccount();
            final Account interestIncomeChargeOffAccount = getAccounts().getInterestIncomeChargeOffAccount();
            final Account overpaymentAccount = getAccounts().getOverpaymentAccount();

            final String disbursementDay = "01 January 2023";
            final String repaymentPeriod1DueDate = "01 February 2023";
            final String repaymentPeriod2DueDate = "01 March 2023";
            final String repaymentPeriod3DueDate = "01 April 2023";
            final String repaymentPeriod4DueDate = "01 May 2023";

            Long localLoanProductId = createLoanProductAccountingAccuralPeriodicWithInterest();
            final Long loanId = applyForLoanApplicationWithInterest(client.getClientId(), localLoanProductId, BigDecimal.valueOf(40000),
                    disbursementDay);

            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate(disbursementDay).locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDay).dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            chargePenalty(loanId, 30.0, repaymentPeriod1DueDate);
            chargePenalty(loanId, 50.0, repaymentPeriod2DueDate);
            chargeFee(loanId, 40.0, repaymentPeriod1DueDate);
            chargeFee(loanId, 60.0, repaymentPeriod3DueDate);

            // Charge-OFF loan
            String randomText = Utils.randomStringGenerator("en", 5) + Utils.randomNumberGenerator(6)
                    + Utils.randomStringGenerator("is", 5);
            Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(randomText, 1);
            String transactionExternalId = UUID.randomUUID().toString();
            chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("2 January 2023").locale("en")
                    .dateFormat("dd MMMM yyyy").externalId(transactionExternalId).chargeOffReasonId(chargeOffReasonId));

            updateBusinessDate("15 May 2023");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate)
                            .locale("en").transactionAmount(340.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate)
                            .locale("en").transactionAmount(320.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate)
                            .locale("en").transactionAmount(330.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4 = makeInterestPaymentWaiver(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate)
                            .locale("en").transactionAmount(350.0));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr4Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr4.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod4DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr3Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr3.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod3DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr2Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr2.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod2DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            PostLoansLoanIdTransactionsResponse interestPaymentWaiverTr1Reverse = reverseLoanTransaction(loanId,
                    interestPaymentWaiverTr1.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                            .dateFormat(DATETIME_PATTERN).transactionDate(repaymentPeriod1DueDate).transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
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
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .loanScheduleType("PROGRESSIVE"));

            Long loanId = applyAndApproveLoanProgressiveAdvancedPaymentAllocationStrategyMonthlyRepayments(clientId, loanProductId,
                    numberOfRepayments, loanDisbursementDate, amount, null);

            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "01 January 2023"), //
                    installment(250.0, false, "01 February 2023"), //
                    installment(250.0, false, "01 March 2023"), //
                    installment(250.0, false, "01 April 2023"), //
                    installment(250.0, false, "01 May 2023") //
            );

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("1 January 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en"));

            // loan should be active
            Long transactionId = addInterestPaymentWaiverForLoan(loanId, 250.0, "2 January 2023");

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0));

            transactionHelper.adjustLoanTransaction(loanId, transactionId, "3 January 2023", 200.0);

            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "01 January 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0),
                    transaction(250.0, "Interest Payment Waiver", "02 January 2023", 750.0, 250.0, 0.0, 0.0, 0, 0.0, 0.0, true),
                    transaction(200.0, "Interest Payment Waiver", "03 January 2023", 800.0, 200.0, 0.0, 0.0, 0, 0.0, 0.0));
        });
    }

    @Test
    public void testInterestPaymentWaiverBatchExternalIdOnChargedOffLoan() {
        Long[] loanIdContainer = new Long[1];
        String[] loanExternalIdContainer = new String[1];

        runAt("01 January 2025", () -> {
            PostLoanProductsRequest loanProductRequest = create4IProgressiveWithChargeOffBehaviour();
            Long loanProductId = createLoanProduct(loanProductRequest);
            assertNotNull(loanProductId);

            PostClientsResponse clientResponse = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
            Long clientId = clientResponse.getClientId();
            assertNotNull(clientId);

            String loanExternalId = UUID.randomUUID().toString();
            Long createdLoanId = applyAndApproveLoan(clientId, loanProductId, "01 January 2022", 1500.0, 3,
                    req -> req.numberOfRepayments(3).loanTermFrequency(3).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                            .repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                            .interestRatePerPeriod(BigDecimal.valueOf(9.99))
                            .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).externalId(loanExternalId)
                            .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
            disburseLoan(createdLoanId, BigDecimal.valueOf(1500.0), "01 January 2022");

            Long chargeOffTransactionId = chargeOffLoan(createdLoanId, "15 June 2022");
            assertNotNull(chargeOffTransactionId);

            loanIdContainer[0] = createdLoanId;
            loanExternalIdContainer[0] = loanExternalId;
        });

        Long loanId = loanIdContainer[0];
        String loanExternalId = loanExternalIdContainer[0];

        runAt("01 January 2025", () -> {
            String transactionExternalId = UUID.randomUUID().toString();
            LocalDate waiverDate = LocalDate.of(2022, 9, 24);
            BigDecimal waiverAmount = new BigDecimal("46.56");

            String waiverBodyJson = GSON.toJson(Map.of("transactionDate", waiverDate.toString(), "dateFormat", "yyyy-MM-dd", "locale",
                    "de_DE", "transactionAmount", waiverAmount.toString(), "externalId", transactionExternalId));

            BatchRequest waiverRequest = new BatchRequest();
            waiverRequest.setRequestId(1L);
            waiverRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions?command=interestPaymentWaiver");
            waiverRequest.setMethod("POST");
            waiverRequest.setBody(waiverBodyJson);

            BatchRequest getRequest = new BatchRequest();
            getRequest.setRequestId(2L);
            getRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions/external-id/$.resourceExternalId");
            getRequest.setMethod("GET");
            getRequest.setReference(1L);

            List<BatchRequest> batchRequests = new ArrayList<>();
            batchRequests.add(waiverRequest);
            batchRequests.add(getRequest);

            List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(2, responses.size());

            BatchResponse waiverResponse = responses.get(0);
            assertEquals(200, waiverResponse.getStatusCode());
            assertNotNull(waiverResponse.getBody());

            Map<String, Object> waiverResponseBody = GSON.fromJson(waiverResponse.getBody(), Map.class);
            Object resourceExternalId = waiverResponseBody.get("resourceExternalId");

            BatchResponse getResponse = responses.get(1);

            if (resourceExternalId == null) {
                fail("POST response missing resourceExternalId field. GET Response: " + getResponse.getBody());
            }

            if (getResponse.getStatusCode() != 200) {
                fail(String.format(
                        "GET transaction by external ID failed. Status: %d, Expected externalId: %s, "
                                + "Actual resourceExternalId: %s, GET Response: %s",
                        getResponse.getStatusCode(), transactionExternalId, resourceExternalId, getResponse.getBody()));
            }

            assertNotNull(getResponse.getBody());
            Map<String, Object> getResponseBody = GSON.fromJson(getResponse.getBody(), Map.class);
            Object retrievedExternalId = getResponseBody.get("externalId");
            assertEquals(transactionExternalId, retrievedExternalId);
        });
    }

    /**
     * Test case that reproduces backdated charge-off followed by backdated interest waiver.
     *
     * This is the CRITICAL scenario from production: "backbook migrations" where transactions are created TODAY but
     * with backdated transaction dates. This triggers reverse-replays and reprocessing that causes the external ID
     * clearing bug.
     *
     * Key difference from forward-dated scenario: - All transactions created in PRESENT (today) - But with PAST
     * transaction dates (backdated) - This triggers different reprocessing logic - Charge-off creates missing accruals
     * → config query → premature flush
     */
    @Test
    public void testInterestPaymentWaiverBackbookBatchExternalId() {
        runAt("01 January 2025", () -> {
            PostLoanProductsRequest loanProductRequest = create4IProgressiveWithChargeOffBehaviour();
            Long loanProductId = createLoanProduct(loanProductRequest);
            assertNotNull(loanProductId);

            PostClientsResponse clientResponse = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
            Long clientId = clientResponse.getClientId();
            assertNotNull(clientId);

            String loanExternalId = UUID.randomUUID().toString();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "18 January 2022", 431.98, 3,
                    req -> req.numberOfRepayments(3).loanTermFrequency(3).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                            .repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                            .interestRatePerPeriod(BigDecimal.valueOf(9.99))
                            .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).externalId(loanExternalId)
                            .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY));

            disburseLoan(loanId, BigDecimal.valueOf(431.98), "18 January 2022");

            makeLoanRepayment(loanId, "repayment", "28 February 2022", 19.83);
            PostLoansLoanIdTransactionsResponse txn2 = makeLoanRepayment(loanId, "repayment", "18 March 2022", 19.83);
            reverseRepayment(loanId, txn2.getResourceId(), "18 March 2022");

            Long chargeOffTxnId = chargeOffLoan(loanId, "16 September 2022");
            assertNotNull(chargeOffTxnId);

            String transactionExternalId = UUID.randomUUID().toString();
            LocalDate waiverDate = LocalDate.of(2022, 9, 24);
            BigDecimal waiverAmount = new BigDecimal("46.56");

            String waiverBodyJson = GSON.toJson(Map.of("transactionDate", waiverDate.toString(), "dateFormat", "yyyy-MM-dd", "locale",
                    "de_DE", "transactionAmount", waiverAmount.toString(), "externalId", transactionExternalId));

            BatchRequest waiverRequest = new BatchRequest();
            waiverRequest.setRequestId(1L);
            waiverRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions?command=interestPaymentWaiver");
            waiverRequest.setMethod("POST");
            waiverRequest.setBody(waiverBodyJson);

            BatchRequest getRequest = new BatchRequest();
            getRequest.setRequestId(2L);
            getRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions/external-id/$.resourceExternalId");
            getRequest.setMethod("GET");
            getRequest.setReference(1L);

            List<BatchRequest> batchRequests = new ArrayList<>();
            batchRequests.add(waiverRequest);
            batchRequests.add(getRequest);

            List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(2, responses.size());

            BatchResponse waiverResponse = responses.get(0);
            assertEquals(200, waiverResponse.getStatusCode());
            assertNotNull(waiverResponse.getBody());

            Map<String, Object> waiverResponseBody = GSON.fromJson(waiverResponse.getBody(), Map.class);
            Object resourceExternalId = waiverResponseBody.get("resourceExternalId");

            BatchResponse getResponse = responses.get(1);

            if (resourceExternalId == null) {
                fail("POST response missing resourceExternalId with backbook scenario. GET Response: " + getResponse.getBody());
            }

            if (getResponse.getStatusCode() != 200) {
                fail(String.format("GET failed. Status: %d, Expected externalId: %s, Actual resourceExternalId: %s, GET Response: %s",
                        getResponse.getStatusCode(), transactionExternalId, resourceExternalId, getResponse.getBody()));
            }

            assertNotNull(getResponse.getBody());
            Map<String, Object> getResponseBody = GSON.fromJson(getResponse.getBody(), Map.class);
            Object retrievedExternalId = getResponseBody.get("externalId");
            assertEquals(transactionExternalId, retrievedExternalId);
        });
    }

    @Test
    public void testInterestPaymentWaiverComplexTransactionHistoryBatchExternalId() {
        Long[] loanIdContainer = new Long[1];
        String[] loanExternalIdContainer = new String[1];

        runAt("18 January 2022", () -> {
            PostLoanProductsRequest loanProductRequest = create4IProgressiveWithChargeOffBehaviour();
            Long loanProductId = createLoanProduct(loanProductRequest);
            assertNotNull(loanProductId);

            PostClientsResponse clientResponse = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
            Long clientId = clientResponse.getClientId();
            assertNotNull(clientId);

            String loanExternalId = UUID.randomUUID().toString();

            Long createdLoanId = applyAndApproveLoan(clientId, loanProductId, "18 January 2022", 431.98, 3,
                    req -> req.numberOfRepayments(3).loanTermFrequency(3).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                            .repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                            .interestRatePerPeriod(BigDecimal.valueOf(9.99))
                            .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).externalId(loanExternalId)
                            .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY));
            disburseLoan(createdLoanId, BigDecimal.valueOf(431.98), "18 January 2022");
            loanIdContainer[0] = createdLoanId;
            loanExternalIdContainer[0] = loanExternalId;
        });

        Long loanId = loanIdContainer[0];
        String loanExternalId = loanExternalIdContainer[0];

        runAt("28 February 2022", () -> {
            makeLoanRepayment(loanId, "repayment", "28 February 2022", 19.83);
        });

        runAt("18 March 2022", () -> {
            PostLoansLoanIdTransactionsResponse txn = makeLoanRepayment(loanId, "repayment", "18 March 2022", 19.83);
            reverseRepayment(loanId, txn.getResourceId(), "18 March 2022");
        });

        runAt("31 March 2022", () -> {
            PostLoansLoanIdTransactionsResponse txn = makeLoanRepayment(loanId, "repayment", "31 March 2022", 19.83);
            reverseRepayment(loanId, txn.getResourceId(), "31 March 2022");
        });

        runAt("18 April 2022", () -> {
            PostLoansLoanIdTransactionsResponse txn = makeLoanRepayment(loanId, "repayment", "18 April 2022", 39.66);
            reverseRepayment(loanId, txn.getResourceId(), "18 April 2022");
        });

        runAt("16 September 2022", () -> {
            Long chargeOffTransactionId = chargeOffLoan(loanId, "16 September 2022");
            assertNotNull(chargeOffTransactionId);
        });

        runAt("24 September 2022", () -> {
            String transactionExternalId = UUID.randomUUID().toString();
            LocalDate waiverDate = LocalDate.of(2022, 9, 24);
            BigDecimal waiverAmount = new BigDecimal("46.56");

            String waiverBodyJson = GSON.toJson(Map.of("transactionDate", waiverDate.toString(), "dateFormat", "yyyy-MM-dd", "locale",
                    "de_DE", "transactionAmount", waiverAmount.toString(), "externalId", transactionExternalId));

            BatchRequest waiverRequest = new BatchRequest();
            waiverRequest.setRequestId(1L);
            waiverRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions?command=interestPaymentWaiver");
            waiverRequest.setMethod("POST");
            waiverRequest.setBody(waiverBodyJson);

            BatchRequest getRequest = new BatchRequest();
            getRequest.setRequestId(2L);
            getRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions/external-id/$.resourceExternalId");
            getRequest.setMethod("GET");
            getRequest.setReference(1L);

            List<BatchRequest> batchRequests = new ArrayList<>();
            batchRequests.add(waiverRequest);
            batchRequests.add(getRequest);

            List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            assertEquals(2, responses.size());

            BatchResponse waiverResponse = responses.get(0);
            assertEquals(200, waiverResponse.getStatusCode());
            assertNotNull(waiverResponse.getBody());

            Map<String, Object> waiverResponseBody = GSON.fromJson(waiverResponse.getBody(), Map.class);
            Object resourceExternalId = waiverResponseBody.get("resourceExternalId");

            BatchResponse getResponse = responses.get(1);

            if (resourceExternalId == null) {
                fail("POST response missing resourceExternalId with complex scenario. GET Response: " + getResponse.getBody());
            }

            if (getResponse.getStatusCode() != 200) {
                fail(String.format("GET failed. Status: %d, Expected externalId: %s, Actual resourceExternalId: %s, GET Response: %s",
                        getResponse.getStatusCode(), transactionExternalId, resourceExternalId, getResponse.getBody()));
            }

            assertNotNull(getResponse.getBody());
            Map<String, Object> getResponseBody = GSON.fromJson(getResponse.getBody(), Map.class);
            Object retrievedExternalId = getResponseBody.get("externalId");
            assertEquals(transactionExternalId, retrievedExternalId);
        });
    }

    @Test
    public void testInterestPaymentWaiverProductionScenarioBatchExternalId() {
        runAt("01 January 2025", () -> {
            PostLoanProductsRequest loanProductRequest = create4IProgressiveWithChargeOffBehaviour();
            Long loanProductId = createLoanProduct(loanProductRequest);
            assertNotNull(loanProductId);

            PostClientsResponse clientResponse = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
            Long clientId = clientResponse.getClientId();
            assertNotNull(clientId);

            String loanExternalId = UUID.randomUUID().toString();
            Long loanId = applyAndApproveLoan(clientId, loanProductId, "18 January 2022", 431.98, 3,
                    req -> req.numberOfRepayments(3).loanTermFrequency(3).loanTermFrequencyType(RepaymentFrequencyType.MONTHS)
                            .repaymentEvery(1).repaymentFrequencyType(RepaymentFrequencyType.MONTHS)
                            .interestRatePerPeriod(BigDecimal.valueOf(9.99))
                            .interestCalculationPeriodType(InterestCalculationPeriodType.DAILY).externalId(loanExternalId)
                            .transactionProcessingStrategyCode(ADVANCED_PAYMENT_ALLOCATION_STRATEGY));

            disburseLoan(loanId, BigDecimal.valueOf(431.98), "18 January 2022");

            makeLoanRepayment(loanId, "repayment", "28 February 2022", 19.83);

            PostLoansLoanIdTransactionsResponse txn2 = makeLoanRepayment(loanId, "repayment", "18 March 2022", 19.83);
            reverseRepayment(loanId, txn2.getResourceId(), "18 March 2022");

            PostLoansLoanIdTransactionsResponse txn3 = makeLoanRepayment(loanId, "repayment", "31 March 2022", 19.83);
            reverseRepayment(loanId, txn3.getResourceId(), "31 March 2022");

            PostLoansLoanIdTransactionsResponse txn4 = makeLoanRepayment(loanId, "repayment", "18 April 2022", 39.66);
            reverseRepayment(loanId, txn4.getResourceId(), "18 April 2022");

            PostLoansLoanIdTransactionsResponse txn5 = makeLoanRepayment(loanId, "repayment", "18 May 2022", 59.49);
            reverseRepayment(loanId, txn5.getResourceId(), "18 May 2022");

            PostLoansLoanIdTransactionsResponse txn6 = makeLoanRepayment(loanId, "repayment", "18 June 2022", 64.83);
            reverseRepayment(loanId, txn6.getResourceId(), "18 June 2022");

            PostLoansLoanIdTransactionsResponse txn7 = makeLoanRepayment(loanId, "repayment", "18 July 2022", 65.32);
            reverseRepayment(loanId, txn7.getResourceId(), "18 July 2022");

            PostLoansLoanIdTransactionsResponse txn8 = makeLoanRepayment(loanId, "repayment", "18 August 2022", 65.83);
            reverseRepayment(loanId, txn8.getResourceId(), "18 August 2022");

            Long chargeOffTxnId = chargeOffLoan(loanId, "16 September 2022");
            assertNotNull(chargeOffTxnId);

            String transactionExternalId = UUID.randomUUID().toString();
            LocalDate waiverDate = LocalDate.of(2022, 9, 24);
            String waiverAmount = "46,56";

            String waiverBodyJson = GSON.toJson(Map.of("transactionDate", waiverDate.toString(), "dateFormat", "yyyy-MM-dd", "locale",
                    "de_DE", "transactionAmount", waiverAmount, "externalId", transactionExternalId));

            BatchRequest waiverRequest = new BatchRequest();
            waiverRequest.setRequestId(1L);
            waiverRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions?command=interestPaymentWaiver");
            waiverRequest.setMethod("POST");
            waiverRequest.setBody(waiverBodyJson);

            BatchRequest getRequest = new BatchRequest();
            getRequest.setRequestId(2L);
            getRequest.setRelativeUrl("loans/external-id/" + loanExternalId + "/transactions/external-id/$.resourceExternalId");
            getRequest.setMethod("GET");
            getRequest.setReference(1L);

            List<BatchRequest> batchRequests = new ArrayList<>();
            batchRequests.add(waiverRequest);
            batchRequests.add(getRequest);

            List<BatchResponse> responses = batchHelper.executeEnclosingTransaction(batchRequests);

            if (responses.size() != 2) {
                fail("Batch API returned " + responses.size() + " responses instead of 2.");
            }

            assertEquals(2, responses.size());

            BatchResponse waiverResponse = responses.get(0);
            assertEquals(200, waiverResponse.getStatusCode());

            Map<String, Object> waiverResponseBody = GSON.fromJson(waiverResponse.getBody(), Map.class);
            Object resourceExternalId = waiverResponseBody.get("resourceExternalId");

            if (resourceExternalId == null) {
                fail("POST response missing resourceExternalId with production scenario.");
            }

            BatchResponse getResponse = responses.get(1);
            if (getResponse.getStatusCode() != 200) {
                fail(String.format("GET failed. Status: %d, Expected externalId: %s, Actual resourceExternalId: %s, GET Response: %s",
                        getResponse.getStatusCode(), transactionExternalId, resourceExternalId, getResponse.getBody()));
            }

            assertNotNull(getResponse.getBody());
            Map<String, Object> getResponseBody = GSON.fromJson(getResponse.getBody(), Map.class);
            Object retrievedExternalId = getResponseBody.get("externalId");
            assertEquals(transactionExternalId, retrievedExternalId);
        });
    }

    private PostLoanProductsRequest create4IProgressiveWithChargeOffBehaviour() {
        return create4IProgressive().principal(1500.0) // Production uses 1500, not 1000
                .minPrincipal(1.0) // Production min
                .maxPrincipal(10000.0) // Keep same
                .numberOfRepayments(3) // Production uses 3, not 4
                .minNumberOfRepayments(3) // Production min
                .maxNumberOfRepayments(24) // Production max
                .daysInMonthType(1) // ACTUAL, not 30 - matches production
                .daysInYearType(1) // ACTUAL, not 360 - matches production
                .enableAccrualActivityPosting(true) // CRITICAL: enables accrual transaction generation
                .chargeOffBehaviour("ZERO_INTEREST").enableInstallmentLevelDelinquency(true).interestRecognitionOnDisbursementDate(true)
                .daysInYearCustomStrategy(DaysInYearCustomStrategy.FEB_29_PERIOD_ONLY).disallowInterestCalculationOnPastDue(true)
                .supportedInterestRefundTypes(List.of("MERCHANT_ISSUED_REFUND", "PAYOUT_REFUND"))
                .paymentAllocation(List.of(createPaymentAllocation("DEFAULT", "NEXT_INSTALLMENT"),
                        createPaymentAllocation("REPAYMENT", "NEXT_INSTALLMENT"),
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "LAST_INSTALLMENT"),
                        createPaymentAllocation("PAYOUT_REFUND", "LAST_INSTALLMENT"),
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"),
                        createPaymentAllocation("INTEREST_PAYMENT_WAIVER", "NEXT_INSTALLMENT")));
    }

    private void chargeFee(Long loanId, Double amount, String dueDate) {
        PostChargesResponse feeCharge = chargesHelper.createLoanSpecifiedDueDateCharge(9.0);
        PostLoansLoanIdChargesResponse feeLoanChargeResult = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                .chargeId(feeCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en").amount(amount).dueDate(dueDate));
        assertNotNull(feeLoanChargeResult);
        assertNotNull(feeLoanChargeResult.getResourceId());
    }

    private void chargePenalty(Long loanId, Double amount, String dueDate) {
        PostChargesResponse penaltyCharge = chargesHelper.createLoanSpecifiedDueDatePenalty(10.0);
        PostLoansLoanIdChargesResponse penaltyLoanChargeResult = addChargesForLoan(loanId, new PostLoansLoanIdChargesRequest()
                .chargeId(penaltyCharge.getResourceId()).dateFormat(DATETIME_PATTERN).locale("en").amount(amount).dueDate(dueDate));
        assertNotNull(penaltyLoanChargeResult);
        assertNotNull(penaltyLoanChargeResult.getResourceId());
    }

    private Long createLoanProductAccountingAccuralPeriodicWithInterest() {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        String name = Utils.uniqueRandomStringGenerator("LOAN_PRODUCT_", 6);
        String shortName = Utils.uniqueRandomStringGenerator("", 4);
        return createLoanProduct(new PostLoanProductsRequest().name(name).shortName(shortName).description("Test loan description")
                .currencyCode("USD").digitsAfterDecimal(2).daysInYearType(1).daysInMonthType(1).interestRecalculationCompoundingMethod(0)
                .recalculationRestFrequencyType(1).rescheduleStrategyMethod(1).recalculationRestFrequencyInterval(0)
                .isInterestRecalculationEnabled(false).interestRateFrequencyType(2).locale("en_GB").numberOfRepayments(4)
                .repaymentFrequencyType(2L).interestRatePerPeriod(2.0).repaymentEvery(1).minPrincipal(100.0).principal(1000.0)
                .maxPrincipal(10000000.0).amortizationType(1).interestType(1).interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy")
                .transactionProcessingStrategyCode(DEFAULT_STRATEGY).accountingRule(3)
                .fundSourceAccountId(getAccounts().getFundSource().getAccountID().longValue())//
                .loanPortfolioAccountId(getAccounts().getLoansReceivableAccount().getAccountID().longValue())//
                .transfersInSuspenseAccountId(getAccounts().getSuspenseAccount().getAccountID().longValue())//
                .interestOnLoanAccountId(getAccounts().getInterestIncomeAccount().getAccountID().longValue())//
                .incomeFromFeeAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())//
                .incomeFromPenaltyAccountId(getAccounts().getFeeIncomeAccount().getAccountID().longValue())//
                .incomeFromRecoveryAccountId(getAccounts().getRecoveriesAccount().getAccountID().longValue())//
                .writeOffAccountId(getAccounts().getWrittenOffAccount().getAccountID().longValue())//
                .overpaymentLiabilityAccountId(getAccounts().getOverpaymentAccount().getAccountID().longValue())//
                .receivableInterestAccountId(getAccounts().getInterestReceivableAccount().getAccountID().longValue())//
                .receivableFeeAccountId(getAccounts().getFeeReceivableAccount().getAccountID().longValue())//
                .receivablePenaltyAccountId(getAccounts().getPenaltyReceivableAccount().getAccountID().longValue())//
                .goodwillCreditAccountId(getAccounts().getGoodwillExpenseAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditInterestAccountId(getAccounts().getGoodwillIncomeAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditFeesAccountId(getAccounts().getGoodwillIncomeAccount().getAccountID().longValue())//
                .incomeFromGoodwillCreditPenaltyAccountId(getAccounts().getGoodwillIncomeAccount().getAccountID().longValue())//
                .incomeFromChargeOffInterestAccountId(getAccounts().getInterestIncomeChargeOffAccount().getAccountID().longValue())//
                .incomeFromChargeOffFeesAccountId(getAccounts().getFeeChargeOffAccount().getAccountID().longValue())//
                .chargeOffExpenseAccountId(getAccounts().getChargeOffExpenseAccount().getAccountID().longValue())//
                .chargeOffFraudExpenseAccountId(getAccounts().getChargeOffFraudExpenseAccount().getAccountID().longValue())//
                .incomeFromChargeOffPenaltyAccountId(getAccounts().getPenaltyChargeOffAccount().getAccountID().longValue())//
        );
    }

    private Long applyForLoanApplicationWithInterest(final Long clientID, final Long loanProductID, BigDecimal principal,
            String applicationDisbursementDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest loanRequest = new PostLoansRequest() //
                .loanTermFrequency(4).locale("en_GB").loanTermFrequencyType(2).numberOfRepayments(4).repaymentFrequencyType(2)
                .interestRatePerPeriod(BigDecimal.valueOf(2)).repaymentEvery(1).principal(principal).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).dateFormat("dd MMMM yyyy").transactionProcessingStrategyCode(DEFAULT_STRATEGY)
                .loanType("individual").expectedDisbursementDate(applicationDisbursementDate).submittedOnDate(applicationDisbursementDate)
                .clientId(clientID).productId(loanProductID);
        return applyForLoan(loanRequest);
    }

    private Long createLoanProduct(final double principal, final int repaymentAfterEvery, final int numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, String loanScheduleProcessingType) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        return createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                .loanScheduleProcessingType(loanScheduleProcessingType).paymentAllocation(List.of(createDefaultPaymentAllocation(), //
                        createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT"), //
                        createPaymentAllocation("MERCHANT_ISSUED_REFUND", "REAMORTIZATION"), //
                        createPaymentAllocation("PAYOUT_REFUND", "NEXT_INSTALLMENT"), //
                        createPaymentAllocation("INTEREST_PAYMENT_WAIVER", "LAST_INSTALLMENT")))
                .principal(principal).minPrincipal(principal).repaymentEvery(repaymentAfterEvery).numberOfRepayments(numberOfRepayments)
                .enableDownPayment(downPaymentEnabled).disbursedAmountPercentageForDownPayment(new BigDecimal(downPaymentPercentage))
                .enableAutoRepaymentForDownPayment(autoPayForDownPayment).amortizationType(AmortizationType.EQUAL_PRINCIPAL)
                .daysInMonthType(DaysInMonthType.DAYS_30).daysInYearType(DaysInYearType.DAYS_365).graceOnPrincipalPayment(0)
                .graceOnInterestPayment(0));
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Long loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String transactionProcessorCode,
            String loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return loanHelper.applyForLoan(
                new PostLoansRequest().clientId(clientId).productId(loanProductId).expectedDisbursementDate(expectedDisbursementDate)
                        .dateFormat(DATETIME_PATTERN).transactionProcessingStrategyCode(transactionProcessorCode).locale("en")
                        .submittedOnDate(submittedOnDate).amortizationType(1).interestRatePerPeriod(interestRate)
                        .interestCalculationPeriodType(1).interestType(0).repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery)
                        .repaymentFrequencyType(0).numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency)
                        .loanTermFrequencyType(0).principal(principal).loanType("individual")
                        .loanScheduleProcessingType(loanScheduleProcessingType).maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Long loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate) {
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, HORIZONTAL);
    }

    private PostLoansResponse applyForLoanApplication(final Long clientId, final Long loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String loanScheduleProcessingType) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, ADVANCED_PAYMENT_ALLOCATION_STRATEGY, loanScheduleProcessingType);
    }

    private static void validateLoanTransaction(GetLoansLoanIdResponse loanDetails, int index, double transactionAmount,
            double principalPortion, double overPaidPortion, double loanBalance) {
        assertEquals(transactionAmount, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getAmount()));
        assertEquals(principalPortion, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getPrincipalPortion()));
        assertEquals(overPaidPortion, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getOverpaymentPortion()));
        assertEquals(loanBalance, Utils.getDoubleValue(loanDetails.getTransactions().get(index).getOutstandingLoanBalance()));
    }

    private void validateLoanCharge(GetLoansLoanIdResponse loanDetails, int index, LocalDate dueDate, double charged, double paid,
            double outstanding) {
        GetLoansLoanIdLoanChargeData chargeData = loanDetails.getCharges().get(index);
        assertEquals(dueDate, chargeData.getDueDate());
        assertEquals(charged, Utils.getDoubleValue(chargeData.getAmount()));
        assertEquals(paid, Utils.getDoubleValue(chargeData.getAmountPaid()));
        assertEquals(outstanding, Utils.getDoubleValue(chargeData.getAmountOutstanding()));
    }

}
