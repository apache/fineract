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
import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class DelinquencyAndChargebackIntegrationTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private static final String principalAmount = "1200.00";
    private static final Double doubleZERO = Double.valueOf("0.00");

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();

        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();

        loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void testLoanClassificationStepAsPartOfCOB(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            final LocalDate todaysDate = Utils.getDateAsLocalDate("01 April 2012");
            LocalDate businessDate = todaysDate.minusMonths(3);
            log.info("Current Business date {}", businessDate);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // Client and Loan account creation
            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                    Math.toIntExact(delinquencyBucket.getId()), loanProductTestBuilder);
            assertNotNull(getLoanProductsProductResponse);

            // Older date to have more than one overdue installment
            String operationDate = Utils.dateFormatter.format(businessDate);
            log.info("Operation date  {}", businessDate);

            // Create Loan Account
            final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                    getLoanProductsProductResponse.getId().toString(), operationDate, "12");

            // Move the Business date 1 month to apply the first repayment
            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            String amountVal = "100.00";
            Float transactionAmount = Float.valueOf(amountVal);
            operationDate = Utils.dateFormatter.format(businessDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate,
                    transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            Long transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Move the Business date 1 month more to apply the second repayment
            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = Utils.dateFormatter.format(businessDate);
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Get loan details expecting to have not a delinquency classification and 1,000 as Outstanding
            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, "0.00", "1000.00", 0, doubleZERO);

            // Move the Business date n days to apply the chargeback for the previous repayment
            businessDate = businessDate.plusDays(21);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Apply the Chargeback transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, amountVal, 0,
                    responseSpec);
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 1);

            // Validate the account expecting to have an adjustment for 100.00 and Outstanding 1,100
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            // Past Due Days in Zero because the Charge back transaction exists and It was done with the current date
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1100.00", 0, Double.valueOf("0.00"));

            // Move the Business date n days to run the COB
            businessDate = businessDate.plusDays(14);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Run the Loan COB Job
            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1100.00", 14, Double.valueOf("200.00"));

            // Move the Business date few days to apply the repayment for Chargeback
            businessDate = todaysDate.plusDays(4);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = Utils.dateFormatter.format(businessDate);
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1000.00", 4, Double.valueOf("100.00"));

            // Apply a partial repayment
            operationDate = Utils.dateFormatter.format(businessDate);
            transactionAmount = Float.valueOf("50.00");
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);
            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "950.00", 4, Double.valueOf("50.00"));
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void testLoanClassificationStepAsPartOfCOBRepeated(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            List<LocalDate> expectedDates = new ArrayList();

            LocalDate businessDate = LocalDate.parse("2022-01-01", DateUtils.DEFAULT_DATE_FORMATTER);
            log.info("Current Business date {}", businessDate);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);

            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // Client and Loan account creation
            final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                    Math.toIntExact(delinquencyBucket.getId()), loanProductTestBuilder);
            assertNotNull(getLoanProductsProductResponse);

            // Older date to have more than one overdue installment
            String operationDate = Utils.dateFormatter.format(businessDate);
            log.info("Operation date  {}", businessDate);

            // Create Loan Account
            final Integer loanId = createLoanAccount(loanTransactionHelper, clientId.toString(),
                    getLoanProductsProductResponse.getId().toString(), operationDate, "3");

            // Move the Business date 1 month to apply the first repayment
            businessDate = businessDate.plusMonths(1);
            expectedDates.add(businessDate);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            String amountVal = "400.00";
            Float transactionAmount = Float.valueOf(amountVal);
            operationDate = Utils.dateFormatter.format(businessDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate,
                    transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            Long transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Move the Business date 1 month more to apply the second repayment
            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = Utils.dateFormatter.format(businessDate);
            expectedDates.add(businessDate);
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Get loan details expecting to have not a delinquency classification and 1,000 as Outstanding
            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, "0.00", "400.00", 0, doubleZERO);

            // Move the Business date n days to apply the chargeback for the previous repayment
            businessDate = businessDate.plusDays(15);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Apply the Chargeback transaction
            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, amountVal, 0,
                    responseSpec);
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 1);

            // Validate the account expecting to have an adjustment for 100.00 and Outstanding 1,100
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            // Past Due Days in Zero because the Charge back transaction exists and It was done with the current date
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "800.00", 0, Double.valueOf("0.00"));

            // Move the Business date n days to run the COB
            businessDate = businessDate.plusDays(23);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Run the Loan COB Job
            final String jobName = "Loan COB";
            schedulerJobHelper.executeAndAwaitJob(jobName);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "800.00", 23, Double.valueOf("800.00"));

            // Move the Business date few days to apply the repayment for Chargeback
            businessDate = LocalDate.parse("2022-03-20", DateUtils.DEFAULT_DATE_FORMATTER);
            expectedDates.add(businessDate);
            operationDate = Utils.dateFormatter.format(businessDate);
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            loanTransactionHelper.reviewLoanTransactionRelations(loanId, transactionId, 0);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "400.00", 7, Double.valueOf("400.00"));

            // Pay the Loan to get this as Closed
            loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, transactionAmount, loanId);
            assertNotNull(loanIdTransactionsResponse);
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertEquals(LoanStatus.CLOSED_OBLIGATIONS_MET.getValue(), getLoansLoanIdResponse.getStatus().getId());
            log.info("Loan id {} with status {}", loanId, getLoansLoanIdResponse.getStatus().getCode());

            // Evaluate Installments
            GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            assertNotNull(getLoanRepaymentSchedule);
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());

            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null) {
                    log.info("Period number {} completed on date {}", period.getPeriod(), period.getObligationsMetOnDate());
                    assertNotNull(period.getObligationsMetOnDate());
                    assertEquals(expectedDates.get(period.getPeriod() - 1), period.getObligationsMetOnDate());
                    assertTrue(period.getComplete());
                }
            }
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, LoanProductTestBuilder loanProductTestBuilder) {
        final HashMap<String, Object> loanProductMap = loanProductTestBuilder.build(null, delinquencyBucketId);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String periods) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount).withLoanTermFrequency(periods)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(periods).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);
        return loanId;
    }

    private GetDelinquencyRangesResponse validateLoanAccount(GetLoansLoanIdResponse getLoansLoanIdResponse, final String adjustments,
            final String outstanding, Integer pastDueDays, Double delinquentAmount) {
        assertNotNull(getLoansLoanIdResponse);
        final GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();

        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
        if (delinquencyRange != null) {
            log.info("Loan Delinquency Range is {}", delinquencyRange.getClassification());
        }
        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
        loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(adjustments));
        DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, pastDueDays, delinquentAmount);

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(outstanding));

        return delinquencyRange;
    }

    private static AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
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
        }).toList();
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))));
    }

}
