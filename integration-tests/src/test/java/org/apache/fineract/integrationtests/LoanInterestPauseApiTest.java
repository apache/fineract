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
import java.util.Arrays;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@Slf4j
@ExtendWith({ LoanTestLifecycleExtension.class })
public class LoanInterestPauseApiTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanInterestPauseApiTest.class);

    private static RequestSpecification REQUEST_SPEC;
    private static ResponseSpecification RESPONSE_SPEC;
    private static ResponseSpecification RESPONSE_SPEC_403;
    private static LoanTransactionHelper LOAN_TRANSACTIONAL_HELPER;
    private static LoanTransactionHelper LOAN_TRANSACTION_HELPER_403;
    private static AccountHelper accountHelper;
    private static PostClientsResponse client;
    private static Integer loanProductId;
    private static Long loanId;
    private static Long nonExistLoanId = 99999L;
    private static String externalId;
    private static String nonExistExternalId = "7c4fb86f-a778-4d02-b7a8-ec3ec98941fa";

    @BeforeAll
    public static void setupTests() {
        Utils.initializeRESTAssured();
        REQUEST_SPEC = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        REQUEST_SPEC.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        RESPONSE_SPEC = new ResponseSpecBuilder().expectStatusCode(200).build();
        RESPONSE_SPEC_403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        LOAN_TRANSACTIONAL_HELPER = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC);
        LOAN_TRANSACTION_HELPER_403 = new LoanTransactionHelper(REQUEST_SPEC, RESPONSE_SPEC_403);
        accountHelper = new AccountHelper(REQUEST_SPEC, RESPONSE_SPEC);
        ClientHelper clientHelper = new ClientHelper(REQUEST_SPEC, RESPONSE_SPEC);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());

        final Account assetAccount = accountHelper.createAssetAccount();
        final Account incomeAccount = accountHelper.createIncomeAccount();
        final Account expenseAccount = accountHelper.createExpenseAccount();
        final Account overpaymentAccount = accountHelper.createLiabilityAccount();

        externalId = UUID.randomUUID().toString();

        loanProductId = createLoanProduct("500", "15", "4", true, "25", true, LoanScheduleType.PROGRESSIVE,
                LoanScheduleProcessingType.HORIZONTAL, assetAccount, incomeAccount, expenseAccount, overpaymentAccount);

        final PostLoansResponse loanResponse = applyForLoanApplication(client.getClientId(), loanProductId, BigDecimal.valueOf(500.0), 45,
                15, 3, BigDecimal.ZERO, "01 January 2023", "01 January 2023", externalId);

        loanId = loanResponse.getLoanId();

        Assertions.assertNotNull(loanProductId, "Loan Product ID should not be null after creation");
        Assertions.assertNotNull(loanId, "Loan ID should not be null after creation");
        Assertions.assertNotNull(externalId, "External Loan ID should not be null after creation");
    }

    @Test
    public void testCreateInterestPauseByLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTIONAL_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-02-05",
                "yyyy-MM-dd", "en", loanId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateBeforeStartDate_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2024-12-05", "2024-12-01", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testCreateInterestPauseByLoanId_startDateBeforeLoanStart_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2022-12-01", "2024-12-05", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testCreateInterestPauseByLoanId_endDateAfterLoanMaturity_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByLoanId("2024-12-01", "2025-12-05", "yyyy-MM-dd", "en", loanId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.after.loan.maturity.date"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_noPauses_shouldReturnEmpty() {
        String response = LOAN_TRANSACTIONAL_HELPER.retrieveInterestPauseByLoanId(nonExistLoanId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.contains("id"));
        Assertions.assertFalse(response.contains("startDate"));
        Assertions.assertFalse(response.contains("endDate"));
    }

    @Test
    public void testRetrieveInterestPausesByLoanId_shouldReturnData() {
        LOAN_TRANSACTIONAL_HELPER.createInterestPauseByLoanId("2023-01-01", "2023-02-05", "yyyy-MM-dd", "en", loanId);

        String response = LOAN_TRANSACTIONAL_HELPER.retrieveInterestPauseByLoanId(loanId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("2023-01-01"));
        Assertions.assertTrue(response.contains("2023-02-05"));
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_validRequest_shouldSucceed() {
        PostLoansLoanIdTransactionsResponse response = LOAN_TRANSACTIONAL_HELPER.createInterestPauseByExternalId("2023-01-01", "2023-02-05",
                "yyyy-MM-dd", "en", externalId);

        Assertions.assertNotNull(response);
        Assertions.assertNotNull(response.getResourceId());
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateBeforeStartDate_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2024-12-05", "2024-12-01", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.before.start.date"),
                    "Response should contain the validation error message for end date before start date");
        }
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_startDateBeforeLoanStart_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2022-12-01", "2024-12-05", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.start.date.before.loan.start.date"),
                    "Response should contain the validation error message for start date before loan start date");
        }
    }

    @Test
    public void testCreateInterestPauseByExternalLoanId_endDateAfterLoanMaturity_shouldFail() {
        try {
            LOAN_TRANSACTION_HELPER_403.createInterestPauseByExternalId("2024-12-01", "2025-12-05", "yyyy-MM-dd", "en", externalId);
        } catch (Exception e) {
            String responseBody = e.getMessage();
            Assertions.assertNotNull(responseBody, "Response body should not be null");
            Assertions.assertTrue(responseBody.contains("interest.pause.end.date.after.loan.maturity.date"),
                    "Response should contain the validation error message for end date after loan maturity date");
        }
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_noPauses_shouldReturnEmpty() {
        String response = LOAN_TRANSACTIONAL_HELPER.retrieveInterestPauseByExternalId(nonExistExternalId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertFalse(response.contains("id"));
        Assertions.assertFalse(response.contains("startDate"));
        Assertions.assertFalse(response.contains("endDate"));
    }

    @Test
    public void testRetrieveInterestPausesByExternalLoanId_shouldReturnData() {
        LOAN_TRANSACTIONAL_HELPER.createInterestPauseByExternalId("2023-01-01", "2023-02-05", "yyyy-MM-dd", "en", externalId);

        String response = LOAN_TRANSACTIONAL_HELPER.retrieveInterestPauseByExternalId(externalId);

        Assertions.assertNotNull(response, "Response should not be null");
        Assertions.assertTrue(response.contains("2023-01-01"));
        Assertions.assertTrue(response.contains("2023-02-05"));
    }

    private static Integer createLoanProduct(final String principal, final String repaymentAfterEvery, final String numberOfRepayments,
            boolean downPaymentEnabled, String downPaymentPercentage, boolean autoPayForDownPayment, LoanScheduleType loanScheduleType,
            LoanScheduleProcessingType loanScheduleProcessingType, final Account... accounts) {
        AdvancedPaymentData defaultAllocation = createDefaultPaymentAllocation();
        final String loanProductJSON = new LoanProductTestBuilder().withMinPrincipal(principal).withPrincipal(principal)
                .withRepaymentTypeAsDays().withRepaymentAfterEvery(repaymentAfterEvery).withNumberOfRepayments(numberOfRepayments)
                .withEnableDownPayment(downPaymentEnabled, downPaymentPercentage, autoPayForDownPayment).withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths()
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsFlat().withAccountingRulePeriodicAccrual(accounts)
                .addAdvancedPaymentAllocation(defaultAllocation).withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                .withInterestTypeAsDecliningBalance().withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withLoanScheduleType(loanScheduleType).withLoanScheduleProcessingType(loanScheduleProcessingType).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").build(null);
        return LOAN_TRANSACTIONAL_HELPER.getLoanProductId(loanProductJSON);
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

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).collect(Collectors.toList());
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, final String externalId) {
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate, LoanScheduleProcessingType.HORIZONTAL, externalId);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, LoanScheduleProcessingType loanScheduleProcessingType,
            final String externalId) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return applyForLoanApplication(clientId, loanProductId, principal, loanTermFrequency, repaymentAfterEvery, numberOfRepayments,
                interestRate, expectedDisbursementDate, submittedOnDate,
                AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY, loanScheduleProcessingType.name(),
                externalId);
    }

    private static PostLoansResponse applyForLoanApplication(final Long clientId, final Integer loanProductId, final BigDecimal principal,
            final int loanTermFrequency, final int repaymentAfterEvery, final int numberOfRepayments, final BigDecimal interestRate,
            final String expectedDisbursementDate, final String submittedOnDate, String transactionProcessorCode,
            String loanScheduleProcessingType, final String externalId) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        return LOAN_TRANSACTIONAL_HELPER.applyLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId.longValue())
                .expectedDisbursementDate(expectedDisbursementDate).dateFormat(DATETIME_PATTERN)
                .transactionProcessingStrategyCode(transactionProcessorCode).locale("en").submittedOnDate(submittedOnDate)
                .amortizationType(1).interestRatePerPeriod(interestRate).interestCalculationPeriodType(1).interestType(0)
                .repaymentFrequencyType(0).repaymentEvery(repaymentAfterEvery).repaymentFrequencyType(0)
                .numberOfRepayments(numberOfRepayments).loanTermFrequency(loanTermFrequency).loanTermFrequencyType(0).principal(principal)
                .loanType("individual").loanScheduleProcessingType(loanScheduleProcessingType).externalId(externalId)
                .maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
    }
}
