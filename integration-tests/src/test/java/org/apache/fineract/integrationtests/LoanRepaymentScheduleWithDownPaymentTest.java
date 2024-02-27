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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.DATETIME_PATTERN;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
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
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicInteger;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

@ExtendWith(LoanTestLifecycleExtension.class)
public class LoanRepaymentScheduleWithDownPaymentTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        clientHelper = new ClientHelper(requestSpec, responseSpec);
        accountHelper = new AccountHelper(requestSpec, responseSpec);
        journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndAutoRepaymentDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalPaidForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertEquals(expectedRepaymentAmount, summary.getTotalOutstanding());
        assertEquals(expectedDownPaymentAmount, summary.getTotalRepaymentTransaction());
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductOneDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedFirstDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedSecondDownPaymentAmount.equals(period.getTotalDueForPeriod())
                        && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndAutoRepaymentDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 750.00;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);
        Double expectedTotalRepaymentAmount = expectedFirstDownPaymentAmount + expectedSecondDownPaymentAmount;

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedFirstDownPaymentAmount.equals(period.getTotalPaidForPeriod()) //
                        && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedSecondDownPaymentAmount.equals(period.getTotalPaidForPeriod())
                        && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
        assertEquals(expectedRepaymentAmount, summary.getTotalOutstanding());
        assertEquals(expectedTotalRepaymentAmount, summary.getTotalRepaymentTransaction());
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductOneDisbursementAndThreeRepaymentsAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedOutstandingLoanBalanceOnDisbursement = 1000.00;
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 250.00;
        LocalDate expectedFirstRepaymentDueDate = LocalDate.of(2022, 10, 3);
        Double outstandingBalanceOnFirstRepayment = 500.00;
        LocalDate expectedSecondRepaymentDueDate = LocalDate.of(2022, 11, 3);
        Double outstandingBalanceOnSecondRepayment = 250.00;
        LocalDate expectedThirdRepaymentDueDate = LocalDate.of(2022, 12, 3);
        Double outstandingBalanceOnThirdRepayment = 0.00;

        assertEquals(expectedDownPaymentAmount, summary.getTotalRepaymentTransaction());

        GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
        assertEquals(expectedDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnDisbursement, firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
        assertEquals(expectedDownPaymentAmount, firstDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

        GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(2);
        assertEquals(expectedRepaymentAmount, firstRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnFirstRepayment, firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(3);
        assertEquals(expectedRepaymentAmount, secondRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnSecondRepayment, secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(4);
        assertEquals(expectedRepaymentAmount, thirdRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnThirdRepayment, thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3", "0");

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        loanTransactionHelper.printRepaymentSchedule(loanDetails);

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedOutstandingLoanBalanceOnFirstDisbursement = 700.00;
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedOutstandingLoanBalanceOnSecondDisbursement = 300.00;
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 250.00;
        LocalDate expectedFirstRepaymentDueDate = LocalDate.of(2022, 10, 3);
        Double outstandingBalanceOnFirstRepayment = 500.00;
        LocalDate expectedSecondRepaymentDueDate = LocalDate.of(2022, 11, 3);
        Double outstandingBalanceOnSecondRepayment = 250.00;
        LocalDate expectedThirdRepaymentDueDate = LocalDate.of(2022, 12, 3);
        Double outstandingBalanceOnThirdRepayment = 0.00;
        Double expectedTotalRepaymentAmount = expectedFirstDownPaymentAmount + expectedSecondDownPaymentAmount;

        assertEquals(expectedTotalRepaymentAmount, summary.getTotalRepaymentTransaction());

        GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
        assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement, firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
        assertEquals(expectedFirstDownPaymentAmount, firstDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

        GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
        assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement, secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
        assertEquals(expectedSecondDownPaymentAmount, secondDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());

        GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
        assertEquals(expectedRepaymentAmount, firstRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnFirstRepayment, firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
        assertEquals(expectedRepaymentAmount, secondRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnSecondRepayment, secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
        assertEquals(expectedRepaymentAmount, thirdRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnThirdRepayment, thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
    }

    @Test
    public void loanRepaymentScheduleWithChargeAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

        final Double feeAmount = 10.00;
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount.toString(),
                false);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), "03 September 2022",
                feeAmount.toString());
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        Double expectedTotalDueForRepaymentInstallment = 760.0;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate()) //
                        && Double.valueOf(0.00).equals(period.getFeeChargesDue())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedTotalDueForRepaymentInstallment.equals(period.getTotalDueForPeriod()) //
                        && expectedRepaymentAmount.equals(period.getPrincipalDue()) //
                        && expectedRepaymentDueDate.equals(period.getDueDate()) //
                        && feeAmount.equals(period.getFeeChargesDue())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPaymentAndCharge() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3", "0");

        final Double feeAmount = 10.00;
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount.toString(),
                false);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), "04 September 2022",
                feeAmount.toString());
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        loanTransactionHelper.printRepaymentSchedule(loanDetails);

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedOutstandingLoanBalanceOnFirstDisbursement = 700.00;
        Double expectedFirstDownPaymentAmount = 175.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedOutstandingLoanBalanceOnSecondDisbursement = 300.00;
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 250.00;
        Double expectedRepaymentTotalDueWithCharge = 260.0;
        LocalDate expectedFirstRepaymentDueDate = LocalDate.of(2022, 10, 3);
        Double outstandingBalanceOnFirstRepayment = 500.00;
        LocalDate expectedSecondRepaymentDueDate = LocalDate.of(2022, 11, 3);
        Double outstandingBalanceOnSecondRepayment = 250.00;
        LocalDate expectedThirdRepaymentDueDate = LocalDate.of(2022, 12, 3);
        Double outstandingBalanceOnThirdRepayment = 0.00;
        Double expectedTotalRepaymentAmount = expectedFirstDownPaymentAmount + expectedSecondDownPaymentAmount;

        assertEquals(expectedTotalRepaymentAmount, summary.getTotalRepaymentTransaction());

        GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
        assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement, firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
        assertEquals(expectedFirstDownPaymentAmount, firstDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

        GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
        assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement, secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
        assertEquals(expectedSecondDownPaymentAmount, secondDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());

        GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
        assertEquals(expectedRepaymentAmount, firstRepaymentPeriod.getPrincipalDue());
        assertEquals(expectedRepaymentTotalDueWithCharge, firstRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnFirstRepayment, firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
        assertEquals(expectedRepaymentAmount, secondRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnSecondRepayment, secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
        assertEquals(expectedRepaymentAmount, thirdRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnThirdRepayment, thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
    }

    @Test
    public void loanRepaymentScheduleWithChargeAndInterestAndDownPayment() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "1");

        final Double feeAmount = 10.00;
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount.toString(),
                false);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), "03 September 2022",
                feeAmount.toString());
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedDownPaymentAmount = 250.00;
        LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedRepaymentAmount = 750.00;
        Double expectedTotalDueForRepaymentInstallment = 770.0;
        LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

        assertTrue(periods.stream() //
                .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                        && expectedDownPaymentDueDate.equals(period.getDueDate()) //
                        && Double.valueOf(0.00).equals(period.getFeeChargesDue()) //
                        && Double.valueOf(0.00).equals(period.getInterestDue())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedTotalDueForRepaymentInstallment.equals(period.getTotalDueForPeriod()) //
                        && expectedRepaymentAmount.equals(period.getPrincipalDue()) //
                        && expectedRepaymentDueDate.equals(period.getDueDate()) //
                        && feeAmount.equals(period.getFeeChargesDue()) //
                        && Double.valueOf(10.0).equals(period.getInterestDue())));
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPaymentAndChargeAndInterest() {
        String loanExternalIdStr = UUID.randomUUID().toString();

        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = true;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, true);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3", "1");

        final Double feeAmount = 10.00;
        String payloadJSON = ChargesHelper.getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_FLAT, feeAmount.toString(),
                false);
        final PostChargesResponse postChargesResponse = ChargesHelper.createLoanCharge(requestSpec, responseSpec, payloadJSON);
        assertNotNull(postChargesResponse);
        final Long loanChargeId = postChargesResponse.getResourceId();

        payloadJSON = LoanTransactionHelper.getSpecifiedDueDateChargesForLoanAsJSON(loanChargeId.toString(), "04 September 2022",
                feeAmount.toString());
        PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = loanTransactionHelper.addChargeForLoan(loanId, payloadJSON,
                responseSpec);
        assertNotNull(postLoansLoanIdChargesResponse);

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
        GetLoansLoanIdSummary summary = loanDetails.getSummary();

        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        loanTransactionHelper.printRepaymentSchedule(loanDetails);

        List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
        Double expectedOutstandingLoanBalanceOnFirstDisbursement = 700.00;
        Double expectedFirstDownPaymentAmount = 175.00;
        Double expectedDownPaymentInterest = 0.00;
        LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
        Double expectedOutstandingLoanBalanceOnSecondDisbursement = 300.00;
        Double expectedSecondDownPaymentAmount = 75.00;
        LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
        Double expectedRepaymentAmount = 250.00;
        Double expectedRepaymentAmountWithInterest = 260.00;
        Double expectedRepaymentInterest = 10.0;
        Double expectedRepaymentTotalDueWithChargeAndInterest = 270.0;
        LocalDate expectedFirstRepaymentDueDate = LocalDate.of(2022, 10, 3);
        Double outstandingBalanceOnFirstRepayment = 500.00;
        LocalDate expectedSecondRepaymentDueDate = LocalDate.of(2022, 11, 3);
        Double outstandingBalanceOnSecondRepayment = 250.00;
        LocalDate expectedThirdRepaymentDueDate = LocalDate.of(2022, 12, 3);
        Double outstandingBalanceOnThirdRepayment = 0.00;
        Double expectedTotalRepaymentAmount = expectedFirstDownPaymentAmount + expectedSecondDownPaymentAmount;

        assertEquals(expectedTotalRepaymentAmount, summary.getTotalRepaymentTransaction());

        GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
        assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement, firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
        assertEquals(expectedFirstDownPaymentAmount, firstDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());
        assertEquals(expectedDownPaymentInterest, firstDownPaymentPeriod.getInterestDue());

        GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
        assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
        assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement, secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding());

        GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
        assertEquals(expectedSecondDownPaymentAmount, secondDownPaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());
        assertEquals(expectedDownPaymentInterest, secondDownPaymentPeriod.getInterestDue());

        GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
        assertEquals(expectedRepaymentAmount, firstRepaymentPeriod.getPrincipalDue());
        assertEquals(expectedRepaymentTotalDueWithChargeAndInterest, firstRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnFirstRepayment, firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
        assertEquals(expectedRepaymentInterest, firstRepaymentPeriod.getInterestDue());

        GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
        assertEquals(expectedRepaymentAmountWithInterest, secondRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnSecondRepayment, secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
        assertEquals(expectedRepaymentInterest, secondRepaymentPeriod.getInterestDue());

        GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
        assertEquals(expectedRepaymentAmountWithInterest, thirdRepaymentPeriod.getTotalDueForPeriod());
        assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
        assertEquals(outstandingBalanceOnThirdRepayment, thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding());
        assertEquals(expectedRepaymentInterest, thirdRepaymentPeriod.getInterestDue());
    }

    @Test
    public void testDelinquencyRangeOnDownPaymentInstallment() {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            LocalDate businessDate = LocalDate.of(2022, 9, 5);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId,
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            final String jobName = "Loan COB";
            final SchedulerJobHelper schedulerJobHelper = new SchedulerJobHelper(requestSpec);
            schedulerJobHelper.executeAndAwaitJob(jobName);

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            Double expectedDownPaymentAmount = 250.00;
            LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
            Double expectedRepaymentAmount = 750.00;
            LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedDownPaymentAmount.equals(period.getTotalDueForPeriod()) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                    && expectedRepaymentDueDate.equals(period.getDueDate())));
            assertNotNull(loanDetails.getDelinquencyRange());
            assertEquals(2, loanDetails.getDelinquent().getDelinquentDays());
        } finally {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    @Test
    public void loanApplicationCreationWithLoanProductWithEnableDownPaymentConfiguration() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "25", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentConfigurationDoesNotChangeWithUpdateProductConfiguration() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(12.5);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        Integer loanProductId = createLoanProductWithDownPaymentConfiguration(loanTransactionHelper, delinquencyBucketId, enableDownPayment,
                "12.5", enableAutoRepaymentForDownPayment, false);

        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        // Modify Loan Product to update enable down payment configuration
        PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProduct(loanTransactionHelper,
                getLoanProductsProductResponse.getId());
        assertNotNull(loanProductModifyResponse);

        // verify Loan product configuration change
        GetLoanProductsProductIdResponse getLoanProductsProductResponse_1 = loanTransactionHelper.getLoanProduct(loanProductId);
        assertNotNull(getLoanProductsProductResponse_1);
        assertEquals(enableDownPayment, getLoanProductsProductResponse_1.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse_1.getDisbursedAmountPercentageForDownPayment().compareTo(BigDecimal.valueOf(25.0)));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse_1.getEnableAutoRepaymentForDownPayment());

        // make repayment for loan
        final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = loanTransactionHelper.makeLoanRepayment(loanExternalIdStr,
                new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                        .transactionAmount(100.0));

        loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan does not change
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndEnableAutoRepaymentForDownPaymentTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Accounts oof periodic accrual
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    loanTransactionHelper, delinquencyBucketId, enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify journal entries for down-payment
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.CREDIT));
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.DEBIT));

            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding());
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(750.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());

            // second disbursement

            disbursementDate = LocalDate.of(2023, 3, 5);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("05 March 2023", loanId, "200");
            checkDownPaymentTransaction(disbursementDate, 50.0f, 0.0f, 0.0f, 0.0f, loanId);

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding());
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(200.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalLoanBalanceOutstanding());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(50.0, loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getDownPaymentPeriod());
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(900.0, loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getDownPaymentPeriod());

            // verify journal entries for down-payment
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "05 March 2023",
                    new JournalEntry(50, JournalEntry.TransactionType.CREDIT));
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "05 March 2023",
                    new JournalEntry(50, JournalEntry.TransactionType.DEBIT));

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentVerifyNoDownPaymentCreatedTest() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Accounts oof periodic accrual
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
            final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                    delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    loanTransactionHelper, delinquencyBucketId, enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount,
                    incomeAccount, expenseAccount, overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            // verify no down-payment transaction created
            checkNoDownPaymentTransaction(loanId);

        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }

    }

    @Test
    public void loanProductAndLoanAccountCreationWithEnableDownPaymentAndDisableRepaymentScheduleExtensionConfigurationTest() {
        // Loan ExternalId
        String loanExternalIdStr = UUID.randomUUID().toString();

        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
        Boolean enableAutoRepaymentForDownPayment = false;

        final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

        // Loan Product creation with down-payment configuration
        GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
                loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment);
        assertNotNull(getLoanProductsProductResponse);
        assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
        assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                .compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

        final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                loanExternalIdStr);

        // Retrieve Loan with loanId

        GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

        // verify down-payment details for Loan
        assertNotNull(loanDetails);
        assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
        assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
        assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());
    }

    @Test
    public void downPaymentOnOverpaidLoan() {
        try {

            // Set business date
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);

            // Accounts oof periodic accrual
            final Account assetAccount = accountHelper.createAssetAccount();
            final Account incomeAccount = accountHelper.createIncomeAccount();
            final Account expenseAccount = accountHelper.createExpenseAccount();
            final Account overpaymentAccount = accountHelper.createLiabilityAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createProgressiveLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    loanTransactionHelper, enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount, incomeAccount,
                    expenseAccount, overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountWithAdvancedPaymentAllocation(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            loanTransactionHelper.disburseLoanWithTransactionAmount("03 March 2023", loanId, "1000");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify journal entries for down-payment
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.CREDIT));
            journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    new JournalEntry(250, JournalEntry.TransactionType.DEBIT));

            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0, loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding());
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0, loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod());
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(750.0, loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod());
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());

            loanTransactionHelper.makeLoanRepayment((long) loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy")
                    .transactionDate("03 March 2023").locale("en").transactionAmount(800.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            // verify down-payment details for Loan
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(50.0, loanDetails.getTotalOverpaid());

            // second disbursement

            disbursementDate = LocalDate.of(2023, 3, 5);
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, disbursementDate);
            loanTransactionHelper.disburseLoanWithTransactionAmount("05 March 2023", loanId, "20");

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());

            assertTrue(loanDetails.getTransactions().get(0).getType().getDisbursement());
            assertEquals(1000.0, loanDetails.getTransactions().get(0).getAmount());
            assertEquals("loanTransactionType.downPayment", loanDetails.getTransactions().get(1).getType().getCode());
            assertEquals(250.0, loanDetails.getTransactions().get(1).getAmount());
            assertTrue(loanDetails.getTransactions().get(2).getType().getRepayment());
            assertEquals(800.0, loanDetails.getTransactions().get(2).getAmount());
            assertTrue(loanDetails.getTransactions().get(3).getType().getDisbursement());
            assertEquals(20.0, loanDetails.getTransactions().get(3).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(3).getOutstandingLoanBalance());
            assertEquals(4, loanDetails.getTransactions().size());

            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(30.0, loanDetails.getTotalOverpaid());

            loanTransactionHelper.disburseLoanWithTransactionAmount("05 March 2023", loanId, "30");
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(4).getType().getDisbursement());
            assertEquals(30.0, loanDetails.getTransactions().get(4).getAmount());
            assertEquals(0.0, loanDetails.getTransactions().get(4).getOutstandingLoanBalance());
            assertEquals(5, loanDetails.getTransactions().size());

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            assertEquals(0.0, loanDetails.getSummary().getTotalOutstanding());
            assertEquals(null, loanDetails.getTotalOverpaid());

            PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment((long) loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("05 March 2023").locale("en")
                            .transactionAmount(1.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(1.0, loanDetails.getTotalOverpaid());

            loanTransactionHelper.disburseLoanWithTransactionAmount("05 March 2023", loanId, "40");
            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(5).getType().getRepayment());
            assertEquals(1.0, loanDetails.getTransactions().get(5).getAmount());
            assertTrue(loanDetails.getTransactions().get(6).getType().getDisbursement());
            assertEquals(40.0, loanDetails.getTransactions().get(6).getAmount());
            assertEquals(39.0, loanDetails.getTransactions().get(6).getOutstandingLoanBalance());
            assertEquals("loanTransactionType.downPayment", loanDetails.getTransactions().get(7).getType().getCode());
            assertEquals(9.0, loanDetails.getTransactions().get(7).getAmount());
            assertEquals(30.0, loanDetails.getTransactions().get(7).getOutstandingLoanBalance());
            assertEquals(8, loanDetails.getTransactions().size());

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(30.0, loanDetails.getSummary().getTotalOutstanding());

            loanTransactionHelper.reverseLoanTransaction(repayment.getLoanId(), repayment.getResourceId(),
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("05 March 2023")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanId.longValue());
            assertTrue(loanDetails.getTransactions().get(5).getType().getRepayment());
            assertEquals(1.0, loanDetails.getTransactions().get(5).getAmount());
            assertTrue(loanDetails.getTransactions().get(5).getManuallyReversed());
            assertTrue(loanDetails.getTransactions().get(6).getType().getDisbursement());
            assertEquals(40.0, loanDetails.getTransactions().get(6).getAmount());
            assertEquals(40.0, loanDetails.getTransactions().get(6).getOutstandingLoanBalance());
            assertEquals("loanTransactionType.downPayment", loanDetails.getTransactions().get(7).getType().getCode());
            assertEquals(9.0, loanDetails.getTransactions().get(7).getAmount());
            assertEquals(31.0, loanDetails.getTransactions().get(7).getOutstandingLoanBalance());
            assertEquals(8, loanDetails.getTransactions().size());

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(31.0, loanDetails.getSummary().getTotalOutstanding());
        } finally {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private void checkNoDownPaymentTransaction(final Integer loanID) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(requestSpec, responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isDownPaymentTransaction = (Boolean) transactionType.get("downPayment");

            if (isDownPaymentTransaction) {
                isTransactionFound = true;
                break;
            }
        }
        assertFalse(isTransactionFound, "Down Payment entries are posted");
    }

    private void checkDownPaymentTransaction(final LocalDate transactionDate, final Float principalPortion, final Float interestPortion,
            final Float feePortion, final Float penaltyPortion, final Integer loanID) {
        ArrayList<HashMap> transactions = (ArrayList<HashMap>) loanTransactionHelper.getLoanTransactions(requestSpec, responseSpec, loanID);
        boolean isTransactionFound = false;
        for (int i = 0; i < transactions.size(); i++) {
            HashMap transactionType = (HashMap) transactions.get(i).get("type");
            boolean isDownPaymentTransaction = (Boolean) transactionType.get("downPayment");

            if (isDownPaymentTransaction) {
                ArrayList<Integer> downPaymentDateAsArray = (ArrayList<Integer>) transactions.get(i).get("date");
                LocalDate downPaymentEntryDate = LocalDate.of(downPaymentDateAsArray.get(0), downPaymentDateAsArray.get(1),
                        downPaymentDateAsArray.get(2));

                if (DateUtils.isEqual(transactionDate, downPaymentEntryDate)) {
                    isTransactionFound = true;
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transactions.get(i).get("principalPortion"))),
                            "Mismatch in transaction amounts");
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
        assertTrue(isTransactionFound, "No Down Payment entries are posted");
    }

    private Integer createLoanAccountMultipleRepaymentsDisbursement(final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
            LoanTransactionHelper loanTransactionHelper, Integer delinquencyBucketId, Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment, final Account... accounts) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccountWithAdvancedPaymentAllocation(final Integer clientID, final Long loanProductID,
            final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("30")
                .withRepaymentStrategy(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                .withLoanTermFrequencyAsDays().withNumberOfRepayments("1").withRepaymentEveryAfter("30").withRepaymentFrequencyTypeAsDays()
                .withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("03 March 2023", "1000", loanId, null);
        return loanId;
    }

    private GetLoanProductsProductIdResponse createProgressiveLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
            LoanTransactionHelper loanTransactionHelper, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            boolean enableAutoRepaymentForDownPayment, final Account... accounts) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withLoanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL).withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation()).withRepaymentAfterEvery("1").withNumberOfRepayments("1")
                .withRepaymentTypeAsMonth().withinterestRatePerPeriod("0").withInterestRateFrequencyTypeAsMonths()
                .withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        AtomicInteger integer = new AtomicInteger(1);
        return Arrays.stream(paymentAllocationTypes).map(pat -> {
            PaymentAllocationOrder paymentAllocationOrder = new PaymentAllocationOrder();
            paymentAllocationOrder.setPaymentAllocationRule(pat.name());
            paymentAllocationOrder.setOrder(integer.getAndIncrement());
            return paymentAllocationOrder;
        }).toList();
    }

    private AdvancedPaymentData createDefaultPaymentAllocation() {
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

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
            LoanTransactionHelper loanTransactionHelper, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            boolean enableAutoRepaymentForDownPayment) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(loanProductJSON);
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanProductWithDownPaymentConfiguration(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment,
            Boolean enableAutoRepaymentForDownPayment, boolean multiDisbursement) {
        HashMap<String, Object> loanProductMap;
        if (multiDisbursement) {
            loanProductMap = new LoanProductTestBuilder().withAmortizationTypeAsEqualInstallments() //
                    .withInterestTypeAsDecliningBalance().withMoratorium("", "").withInterestCalculationPeriodTypeAsRepaymentPeriod(true)
                    .withInterestTypeAsDecliningBalance() //
                    .withMultiDisburse() //
                    .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment) //
                    .withDisallowExpectedDisbursements(true) //
                    .build(null, delinquencyBucketId);
        } else {
            loanProductMap = new LoanProductTestBuilder() //
                    .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment) //
                    .build(null, delinquencyBucketId);
        }
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanProductId;
    }

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String numberOfRepayments, final String interestRate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency(numberOfRepayments)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "1000");
        return loanId;
    }

    private Integer createApproveAndDisburseTwiceLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String numberOfRepayments, final String interestRate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency(numberOfRepayments)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate).withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("04 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan("02 September 2022", "1000", loanId, null);
        loanTransactionHelper.disburseLoanWithTransactionAmount("03 September 2022", loanId, "700");
        loanTransactionHelper.disburseLoanWithTransactionAmount("04 September 2022", loanId, "300");
        return loanId;
    }

    private PutLoanProductsProductIdResponse updateLoanProduct(LoanTransactionHelper loanTransactionHelper, Long id) {
        // down-payment configuration
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25.0);
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest().enableDownPayment(enableDownPayment)
                .disbursedAmountPercentageForDownPayment(disbursedAmountPercentageForDownPayment).locale("en");
        return loanTransactionHelper.updateLoanProduct(id, requestModifyLoan);
    }
}
