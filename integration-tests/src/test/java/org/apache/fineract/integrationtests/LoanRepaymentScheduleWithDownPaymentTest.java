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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

public class LoanRepaymentScheduleWithDownPaymentTest {

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private ClientHelper clientHelper;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.clientHelper = new ClientHelper(this.requestSpec, this.responseSpec);
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
        assertEquals(expectedRepaymentAmount, loanDetails.getSummary().getTotalOutstanding());
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
                .anyMatch(period -> expectedFirstDownPaymentAmount.equals(period.getTotalPaidForPeriod()) //
                        && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream() //
                .anyMatch(period -> expectedSecondDownPaymentAmount.equals(period.getTotalPaidForPeriod())
                        && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
        assertTrue(periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(period.getTotalDueForPeriod())
                && expectedRepaymentDueDate.equals(period.getDueDate())));
        assertEquals(expectedRepaymentAmount, loanDetails.getSummary().getTotalOutstanding());
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
                "25", enableAutoRepaymentForDownPayment, true);

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
}
