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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import org.apache.fineract.client.models.DelinquencyBucketResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdResponse;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleProcessingType;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

@Order(1)
public class LoanRepaymentScheduleWithDownPaymentTest extends FeignLoanTestBase {

    private void runSeptember2022DownPaymentTest(Runnable action) {
        runAt("05 September 2022", action);
    }

    private void runMarch2023DownPaymentTest(Runnable action) {
        runAt("03 March 2023", action);
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

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
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
        });
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndAutoRepaymentDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
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
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalPaidForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(summary.getTotalOutstanding()));
            assertEquals(expectedDownPaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductOneDisbursementAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

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
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1",
                    "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

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
                    .anyMatch(period -> expectedFirstDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedSecondDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndAutoRepaymentDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1",
                    "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
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
                    .anyMatch(period -> expectedFirstDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalPaidForPeriod())) //
                            && expectedFirstDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedSecondDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalPaidForPeriod()))
                            && expectedSecondDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(summary.getTotalOutstanding()));
            assertEquals(expectedTotalRepaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductOneDisbursementAndThreeRepaymentsAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3", "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
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

            assertEquals(expectedDownPaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));

            GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
            assertEquals(expectedDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnDisbursement,
                    Utils.getDoubleValue(firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
            assertEquals(expectedDownPaymentAmount, Utils.getDoubleValue(firstDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

            GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(2);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(firstRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnFirstRepayment,
                    Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(3);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(secondRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnSecondRepayment,
                    Utils.getDoubleValue(secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(4);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(thirdRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnThirdRepayment,
                    Utils.getDoubleValue(thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3",
                    "0");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
            GetLoansLoanIdSummary summary = loanDetails.getSummary();

            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

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

            assertEquals(expectedTotalRepaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));

            GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
            assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement,
                    Utils.getDoubleValue(firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
            assertEquals(expectedFirstDownPaymentAmount, Utils.getDoubleValue(firstDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

            GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
            assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement,
                    Utils.getDoubleValue(secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
            assertEquals(expectedSecondDownPaymentAmount, Utils.getDoubleValue(secondDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());

            GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(firstRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnFirstRepayment,
                    Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(secondRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnSecondRepayment,
                    Utils.getDoubleValue(secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(thirdRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnThirdRepayment,
                    Utils.getDoubleValue(thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
        });
    }

    @Test
    public void loanRepaymentScheduleWithChargeAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            final Double feeAmount = 10.00;
            final PostChargesResponse postChargesResponse = createCharge(feeAmount);
            assertNotNull(postChargesResponse);
            final Long loanChargeId = postChargesResponse.getResourceId();
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId.longValue(), loanChargeId,
                    "03 September 2022", feeAmount);
            assertNotNull(postLoansLoanIdChargesResponse);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

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
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate()) //
                            && Double.valueOf(0.00).equals(Utils.getDoubleValue(period.getFeeChargesDue()))));
            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedTotalDueForRepaymentInstallment.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getPrincipalDue())) //
                            && expectedRepaymentDueDate.equals(period.getDueDate()) //
                            && feeAmount.equals(Utils.getDoubleValue(period.getFeeChargesDue()))));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPaymentAndCharge() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3",
                    "0");

            final Double feeAmount = 10.00;
            final PostChargesResponse postChargesResponse = createCharge(feeAmount);
            assertNotNull(postChargesResponse);
            final Long loanChargeId = postChargesResponse.getResourceId();
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId.longValue(), loanChargeId,
                    "04 September 2022", feeAmount);
            assertNotNull(postLoansLoanIdChargesResponse);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
            GetLoansLoanIdSummary summary = loanDetails.getSummary();

            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

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

            assertEquals(expectedTotalRepaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));

            GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
            assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement,
                    Utils.getDoubleValue(firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
            assertEquals(expectedFirstDownPaymentAmount, Utils.getDoubleValue(firstDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());

            GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
            assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement,
                    Utils.getDoubleValue(secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
            assertEquals(expectedSecondDownPaymentAmount, Utils.getDoubleValue(secondDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());

            GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalDue()));
            assertEquals(expectedRepaymentTotalDueWithCharge, Utils.getDoubleValue(firstRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnFirstRepayment,
                    Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(secondRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnSecondRepayment,
                    Utils.getDoubleValue(secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(thirdRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnThirdRepayment,
                    Utils.getDoubleValue(thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
        });
    }

    @Test
    public void loanRepaymentScheduleWithChargeAndInterestAndDownPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "1");

            final Double feeAmount = 10.00;
            final PostChargesResponse postChargesResponse = createCharge(feeAmount);
            assertNotNull(postChargesResponse);
            final Long loanChargeId = postChargesResponse.getResourceId();
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId.longValue(), loanChargeId,
                    "03 September 2022", feeAmount);
            assertNotNull(postLoansLoanIdChargesResponse);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            Double expectedDownPaymentAmount = 250.00;
            LocalDate expectedDownPaymentDueDate = LocalDate.of(2022, 9, 3);
            Double expectedRepaymentAmount = 750.00;
            Double expectedTotalDueForRepaymentInstallment = 767.50;
            LocalDate expectedRepaymentDueDate = LocalDate.of(2022, 10, 3);

            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate()) //
                            && Double.valueOf(0.00).equals(Utils.getDoubleValue(period.getFeeChargesDue())) //
                            && Double.valueOf(0.00).equals(Utils.getDoubleValue(period.getInterestDue()))));
            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedTotalDueForRepaymentInstallment.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getPrincipalDue())) //
                            && expectedRepaymentDueDate.equals(period.getDueDate()) //
                            && feeAmount.equals(Utils.getDoubleValue(period.getFeeChargesDue())) //
                            && Double.valueOf(7.5).equals(Utils.getDoubleValue(period.getInterestDue()))));
        });
    }

    @Test
    public void loanRepaymentScheduleWithMultiDisbursementProductTwoDisbursementAndThreeRepaymentsAndDownPaymentAndChargeAndInterest() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, true);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseTwiceLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "3",
                    "1");

            final Double feeAmount = 10.00;
            final PostChargesResponse postChargesResponse = createCharge(feeAmount);
            assertNotNull(postChargesResponse);
            final Long loanChargeId = postChargesResponse.getResourceId();
            PostLoansLoanIdChargesResponse postLoansLoanIdChargesResponse = addLoanCharge(loanId.longValue(), loanChargeId,
                    "04 September 2022", feeAmount);
            assertNotNull(postLoansLoanIdChargesResponse);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
            GetLoansLoanIdSummary summary = loanDetails.getSummary();

            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            Double expectedOutstandingLoanBalanceOnFirstDisbursement = 700.00;
            Double expectedFirstDownPaymentAmount = 175.00;
            Double expectedDownPaymentInterest = 0.00;
            LocalDate expectedFirstDownPaymentDueDate = LocalDate.of(2022, 9, 3);
            Double expectedOutstandingLoanBalanceOnSecondDisbursement = 300.00;
            Double expectedSecondDownPaymentAmount = 75.00;
            LocalDate expectedSecondDownPaymentDueDate = LocalDate.of(2022, 9, 4);
            Double expectedRepaymentAmount = 250.00;
            Double expectedRepaymentAmountWithInterest = 255.0;
            Double expectedRepaymentAmountWithInterest2 = 252.5;
            Double expectedRepaymentInterest = 7.42;
            Double expectedRepaymentInterest2 = 5.0;
            Double expectedRepaymentInterest3 = 2.5;
            Double expectedRepaymentTotalDueWithChargeAndInterest = 267.42;
            LocalDate expectedFirstRepaymentDueDate = LocalDate.of(2022, 10, 3);
            Double outstandingBalanceOnFirstRepayment = 500.00;
            LocalDate expectedSecondRepaymentDueDate = LocalDate.of(2022, 11, 3);
            Double outstandingBalanceOnSecondRepayment = 250.00;
            LocalDate expectedThirdRepaymentDueDate = LocalDate.of(2022, 12, 3);
            Double outstandingBalanceOnThirdRepayment = 0.00;
            Double expectedTotalRepaymentAmount = expectedFirstDownPaymentAmount + expectedSecondDownPaymentAmount;

            assertEquals(expectedTotalRepaymentAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));

            GetLoansLoanIdRepaymentPeriod firstDisbursementPeriod = periods.get(0);
            assertEquals(expectedFirstDownPaymentDueDate, firstDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnFirstDisbursement,
                    Utils.getDoubleValue(firstDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod firstDownPaymentPeriod = periods.get(1);
            assertEquals(expectedFirstDownPaymentAmount, Utils.getDoubleValue(firstDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstDownPaymentDueDate, firstDownPaymentPeriod.getDueDate());
            assertEquals(expectedDownPaymentInterest, Utils.getDoubleValue(firstDownPaymentPeriod.getInterestDue()));

            GetLoansLoanIdRepaymentPeriod secondDisbursementPeriod = periods.get(2);
            assertEquals(expectedSecondDownPaymentDueDate, secondDisbursementPeriod.getDueDate());
            assertEquals(expectedOutstandingLoanBalanceOnSecondDisbursement,
                    Utils.getDoubleValue(secondDisbursementPeriod.getPrincipalLoanBalanceOutstanding()));

            GetLoansLoanIdRepaymentPeriod secondDownPaymentPeriod = periods.get(3);
            assertEquals(expectedSecondDownPaymentAmount, Utils.getDoubleValue(secondDownPaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondDownPaymentDueDate, secondDownPaymentPeriod.getDueDate());
            assertEquals(expectedDownPaymentInterest, Utils.getDoubleValue(secondDownPaymentPeriod.getInterestDue()));

            GetLoansLoanIdRepaymentPeriod firstRepaymentPeriod = periods.get(4);
            assertEquals(expectedRepaymentAmount, Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalDue()));
            assertEquals(expectedRepaymentTotalDueWithChargeAndInterest, Utils.getDoubleValue(firstRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedFirstRepaymentDueDate, firstRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnFirstRepayment,
                    Utils.getDoubleValue(firstRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
            assertEquals(expectedRepaymentInterest, Utils.getDoubleValue(firstRepaymentPeriod.getInterestDue()));

            GetLoansLoanIdRepaymentPeriod secondRepaymentPeriod = periods.get(5);
            assertEquals(expectedRepaymentAmountWithInterest, Utils.getDoubleValue(secondRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedSecondRepaymentDueDate, secondRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnSecondRepayment,
                    Utils.getDoubleValue(secondRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
            assertEquals(expectedRepaymentInterest2, Utils.getDoubleValue(secondRepaymentPeriod.getInterestDue()));

            GetLoansLoanIdRepaymentPeriod thirdRepaymentPeriod = periods.get(6);
            assertEquals(expectedRepaymentAmountWithInterest2, Utils.getDoubleValue(thirdRepaymentPeriod.getTotalDueForPeriod()));
            assertEquals(expectedThirdRepaymentDueDate, thirdRepaymentPeriod.getDueDate());
            assertEquals(outstandingBalanceOnThirdRepayment,
                    Utils.getDoubleValue(thirdRepaymentPeriod.getPrincipalLoanBalanceOutstanding()));
            assertEquals(expectedRepaymentInterest3, Utils.getDoubleValue(thirdRepaymentPeriod.getInterestDue()));
        });
    }

    @Test
    public void testDelinquencyRangeOnDownPaymentInstallment() {
        runAt("05 September 2022", () -> {
            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");

            schedulerHelper.executeAndAwaitJob("Loan COB");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

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
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertTrue(
                    periods.stream().anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalDueForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
            assertNotNull(loanDetails.getDelinquencyRange());
            assertEquals(2, loanDetails.getDelinquent().getDelinquentDays());
        });
    }

    @Test
    public void loanApplicationCreationWithLoanProductWithEnableDownPaymentConfiguration() {
        runSeptember2022DownPaymentTest(() -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            // Loan Product creation with down-payment configuration
            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        });
    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentConfigurationDoesNotChangeWithUpdateProductConfiguration() {
        runSeptember2022DownPaymentTest(() -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(12.5);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            // Loan Product creation with down-payment configuration
            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "12.5",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createApproveAndDisburseLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // Modify Loan Product to update enable down payment configuration
            PutLoanProductsProductIdResponse loanProductModifyResponse = updateLoanProductDownPaymentConfiguration(
                    getLoanProductsProductResponse.getId());
            assertNotNull(loanProductModifyResponse);

            // verify Loan product configuration change
            GetLoanProductsProductIdResponse getLoanProductsProductResponse_1 = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse_1);
            assertEquals(enableDownPayment, getLoanProductsProductResponse_1.getEnableDownPayment());
            assertEquals(0,
                    getLoanProductsProductResponse_1.getDisbursedAmountPercentageForDownPayment().compareTo(BigDecimal.valueOf(25.0)));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse_1.getEnableAutoRepaymentForDownPayment());

            // make repayment for loan
            final PostLoansLoanIdTransactionsResponse repaymentTransaction_1 = makeLoanRepayment(loanExternalIdStr,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("5 September 2022").locale("en")
                            .transactionAmount(100.0));

            loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan does not change
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

        });
    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndEnableAutoRepaymentForDownPaymentTest() {
        runAt("03 March 2023", () -> {
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            // Accounts of periodic accrual
            final Account assetAccount = getAccounts().getLoansReceivableAccount();
            final Account incomeAccount = getAccounts().getInterestIncomeAccount();
            final Account expenseAccount = getAccounts().getWrittenOffAccount();
            final Account overpaymentAccount = getAccounts().getOverpaymentAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId.longValue(), "03 March 2023", Double.parseDouble("1000"));

            loanDetails = getLoanDetails(loanId.longValue());
            // verify down-payment transaction created
            checkDownPaymentTransaction(disbursementDate, 250.0f, 0.0f, 0.0f, 0.0f, loanId);

            // verify journal entries for down-payment
            checkJournalEntryForAssetAccount(assetAccount, "03 March 2023",
                    journalEntry(250.0, assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                    journalEntry(250.0, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding()));
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(2).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(750.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());

            // second disbursement

            disbursementDate = LocalDate.of(2023, 3, 5);
            updateBusinessDate(DateUtils.format(disbursementDate, DATETIME_PATTERN));
            disburseLoanWithAmount(loanId.longValue(), "05 March 2023", Double.parseDouble("200"));
            checkDownPaymentTransaction(disbursementDate, 50.0f, 0.0f, 0.0f, 0.0f, loanId);

            loanDetails = getLoanDetails(loanId.longValue());
            // verify installment details
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(0).getDueDate());
            assertEquals(1000.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(0).getPrincipalLoanBalanceOutstanding()));
            assertEquals(1, loanDetails.getRepaymentSchedule().getPeriods().get(1).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 3), loanDetails.getRepaymentSchedule().getPeriods().get(1).getDueDate());
            assertEquals(250.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(1).getTotalInstallmentAmountForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(1).getDownPaymentPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(2).getDueDate());
            assertEquals(200.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(2).getPrincipalLoanBalanceOutstanding()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(2).getDownPaymentPeriod());
            assertEquals(2, loanDetails.getRepaymentSchedule().getPeriods().get(3).getPeriod());
            assertEquals(LocalDate.of(2023, 3, 5), loanDetails.getRepaymentSchedule().getPeriods().get(3).getDueDate());
            assertEquals(50.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(3).getTotalInstallmentAmountForPeriod()));
            assertEquals(true, loanDetails.getRepaymentSchedule().getPeriods().get(3).getDownPaymentPeriod());
            assertEquals(3, loanDetails.getRepaymentSchedule().getPeriods().get(4).getPeriod());
            assertEquals(LocalDate.of(2023, 4, 2), loanDetails.getRepaymentSchedule().getPeriods().get(4).getDueDate());
            assertEquals(900.0,
                    Utils.getDoubleValue(loanDetails.getRepaymentSchedule().getPeriods().get(4).getTotalInstallmentAmountForPeriod()));
            assertEquals(false, loanDetails.getRepaymentSchedule().getPeriods().get(4).getDownPaymentPeriod());

            // verify journal entries for down-payment
            checkJournalEntryForAssetAccount(assetAccount, "05 March 2023",
                    journalEntry(50.0, assetAccount, JournalEntry.TransactionType.CREDIT.name()),
                    journalEntry(50.0, assetAccount, JournalEntry.TransactionType.DEBIT.name()));

        });

    }

    @Test
    public void loanApplicationWithLoanProductWithEnableDownPaymentAndDisableAutoRepaymentForDownPaymentVerifyNoDownPaymentCreatedTest() {
        runAt("03 March 2023", () -> {
            // Accounts of periodic accrual
            final Account assetAccount = getAccounts().getLoansReceivableAccount();
            final Account incomeAccount = getAccounts().getInterestIncomeAccount();
            final Account expenseAccount = getAccounts().getWrittenOffAccount();
            final Account overpaymentAccount = getAccounts().getOverpaymentAccount();

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // Delinquency Bucket
            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            // Loan Product creation with down-payment configuration
            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment, assetAccount, incomeAccount, expenseAccount,
                    overpaymentAccount);

            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());

            // first disbursement
            disburseLoanWithAmount(loanId.longValue(), "03 March 2023", Double.parseDouble("1000"));

            // verify no down-payment transaction created
            checkNoDownPaymentTransaction(loanId.longValue());

        });

    }

    @Test
    public void loanProductAndLoanAccountCreationWithEnableDownPaymentAndDisableRepaymentScheduleExtensionConfigurationTest() {
        runMarch2023DownPaymentTest(() -> {

            // Loan ExternalId
            String loanExternalIdStr = UUID.randomUUID().toString();

            // down-payment configuration
            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = false;

            final Integer clientId = createClient().intValue();

            // Loan Product creation with down-payment configuration
            GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
                    enableDownPayment, "25", enableAutoRepaymentForDownPayment);
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createLoanAccountMultipleRepaymentsDisbursement(clientId, getLoanProductsProductResponse.getId(),
                    loanExternalIdStr);

            // Retrieve Loan with loanId

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());

            // verify down-payment details for Loan
            assertNotNull(loanDetails);
            assertEquals(enableDownPayment, loanDetails.getEnableDownPayment());
            assertEquals(0, loanDetails.getDisbursedAmountPercentageForDownPayment().compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, loanDetails.getEnableAutoRepaymentForDownPayment());
        });
    }

    @Test
    public void downPaymentOnOverpaidProgressiveLoan() {
        runAt("03 March 2023", () -> {
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            Long clientId = createClient();

            final PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .installmentAmountInMultiplesOf(null).enableDownPayment(true).enableAutoRepaymentForDownPayment(true)
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25));

            Long loanProductId = createLoanProduct(loanProductsRequest);

            String disbursementDateStr = DateUtils.format(disbursementDate, DATETIME_PATTERN);
            Long loanId = applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId).loanType("individual")
                    .locale("en").dateFormat(DATETIME_PATTERN).amortizationType(1).interestRatePerPeriod(BigDecimal.ZERO)
                    .interestCalculationPeriodType(1).interestType(0).maxOutstandingLoanBalance(BigDecimal.valueOf(35000))
                    .transactionProcessingStrategyCode(AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                    .loanScheduleProcessingType(LoanScheduleProcessingType.HORIZONTAL.name()).expectedDisbursementDate(disbursementDateStr)
                    .dateFormat(DATETIME_PATTERN).submittedOnDate(disbursementDateStr).repaymentFrequencyType(0).repaymentEvery(30)
                    .numberOfRepayments(1).loanTermFrequency(30).loanTermFrequencyType(0).principal(BigDecimal.valueOf(1000))
                    .loanType("individual").maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate(disbursementDateStr).locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDateStr).dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertTrue(loanDetails.getStatus().getActive());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(750.0, 0.0, 750.0, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            verifyJournalEntries(loanId, //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
            );

            makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("03 March 2023")
                    .locale("en").transactionAmount(800.0));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(750.0, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0) //
            );
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(50.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            // second disbursement
            disbursementDate = LocalDate.of(2023, 3, 5);
            updateBusinessDate(DateUtils.format(disbursementDate, DATETIME_PATTERN));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(20.00)).locale("en"));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(765.0, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0) //
            );
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(30.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(30.00)).locale("en"));
            loanDetails = getLoanDetails(loanId);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 0.0, true, "05 March 2023"), //
                    installment(787.5, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0) //
            );

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(null, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat("dd MMMM yyyy").transactionDate("05 March 2023").locale("en").transactionAmount(1.0));

            loanDetails = getLoanDetails(loanId);
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0), //
                    transaction(1.0, "Repayment", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0) //
            );
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(1.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(40.00)).locale("en"));
            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(40.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 0.0, true, "05 March 2023"), //
                    installment(10.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(817.5, 0.0, 30.0, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0), //
                    transaction(1.0, "Repayment", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0), //
                    transaction(40.0, "Disbursement", "05 March 2023", 39.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0), //
                    transaction(9.0, "Down Payment", "05 March 2023", 30.0, 9.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(30.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

            reverseLoanTransaction(loanId, repayment.getResourceId(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("05 March 2023").transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(40.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 0.0, true, "05 March 2023"), //
                    installment(10.0, 0.0, 1.0, false, "05 March 2023"), //
                    installment(817.5, 0.0, 30.0, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 20.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 30.0), //
                    transaction(1.0, "Repayment", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 1.0, true), //
                    transaction(40.0, "Disbursement", "05 March 2023", 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(9.0, "Down Payment", "05 March 2023", 31.0, 9.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(31.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
        });
    }

    @Test
    public void downPaymentOnOverpaidCumulativeLoan() {
        runAt("03 March 2023", () -> {
            LocalDate disbursementDate = LocalDate.of(2023, 3, 3);

            Long clientId = createClient();

            final PostLoanProductsRequest loanProductsRequest = createOnePeriod30DaysLongNoInterestPeriodicAccrualProductWithAdvancedPaymentAllocation()
                    .installmentAmountInMultiplesOf(null).enableDownPayment(true).enableAutoRepaymentForDownPayment(true)
                    .loanScheduleType(LoanScheduleType.CUMULATIVE.name()).paymentAllocation(null)
                    .transactionProcessingStrategyCode(
                            DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor.STRATEGY_CODE)
                    .disbursedAmountPercentageForDownPayment(BigDecimal.valueOf(25));

            Long loanProductId = createLoanProduct(loanProductsRequest);

            String disbursementDateStr = DateUtils.format(disbursementDate, DATETIME_PATTERN);
            Long loanId = applyForLoan(new PostLoansRequest().clientId(clientId).productId(loanProductId).loanType("individual")
                    .locale("en").dateFormat(DATETIME_PATTERN).amortizationType(1).interestRatePerPeriod(BigDecimal.ZERO)
                    .interestCalculationPeriodType(1).interestType(0).maxOutstandingLoanBalance(BigDecimal.valueOf(35000))
                    .transactionProcessingStrategyCode(
                            DuePenFeeIntPriInAdvancePriPenFeeIntLoanRepaymentScheduleTransactionProcessor.STRATEGY_CODE)
                    .expectedDisbursementDate(disbursementDateStr).dateFormat(DATETIME_PATTERN).submittedOnDate(disbursementDateStr)
                    .repaymentFrequencyType(0).repaymentEvery(30).numberOfRepayments(1).loanTermFrequency(30).loanTermFrequencyType(0)
                    .principal(BigDecimal.valueOf(1000)).loanType("individual").maxOutstandingLoanBalance(BigDecimal.valueOf(35000)));
            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate(disbursementDateStr).locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDateStr).dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(1000)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertTrue(loanDetails.getStatus().getActive());

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(750.0, 0.0, 750.0, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            verifyJournalEntries(loanId, //
                    journalEntry(1000.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.DEBIT.name()), //
                    journalEntry(1000.0, getAccounts().getFundSource(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()), //
                    journalEntry(250.0, getAccounts().getFundSource(), JournalEntry.TransactionType.DEBIT.name()) //
            );

            String repaymentExternalId = UUID.randomUUID().toString();
            PostLoansLoanIdTransactionsResponse initialRepayment = makeLoanRepayment(loanId,
                    new PostLoansLoanIdTransactionsRequest().dateFormat("dd MMMM yyyy").transactionDate("03 March 2023").locale("en")
                            .transactionAmount(800.0).externalId(repaymentExternalId));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(750.0, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 750.0, 0.0, 0.0, 0.0, 0.0, 50.0) //
            );
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(50.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            // second disbursement
            disbursementDate = LocalDate.of(2023, 3, 5);
            updateBusinessDate(DateUtils.format(disbursementDate, DATETIME_PATTERN));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(20.00)).locale("en"));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(765.0, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 770.0, 0.0, 0.0, 0.0, 0.0, 30.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );
            assertTrue(loanDetails.getStatus().getOverpaid());
            assertEquals(30.0, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(30.00)).locale("en"));
            loanDetails = getLoanDetails(loanId);

            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 0.0, true, "05 March 2023"), //
                    installment(787.5, 0.0, 0.0, true, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 800.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            assertTrue(loanDetails.getStatus().getClosedObligationsMet());
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
            assertEquals(null, Utils.getDoubleValue(loanDetails.getTotalOverpaid()));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("05 March 2023").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(40.00)).locale("en"));
            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(40.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 0.0, true, "05 March 2023"), //
                    installment(10.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(817.5, 0.0, 30.0, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 800.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(20.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(40.0, "Disbursement", "05 March 2023", 40.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Down Payment", "05 March 2023", 30.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(30.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));

            reverseLoanTransaction(loanId, repaymentExternalId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("05 March 2023").transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
            // Verify Repayment Schedule
            verifyRepaymentSchedule(loanId, //
                    installment(1000.0, null, "03 March 2023"), //
                    installment(250.0, 0.0, 0.0, true, "03 March 2023"), //
                    installment(20.0, null, "05 March 2023"), //
                    installment(30.0, null, "05 March 2023"), //
                    installment(40.0, null, "05 March 2023"), //
                    installment(5.0, 0.0, 0.0, true, "05 March 2023"), //
                    installment(7.5, 0.0, 2.5, false, "05 March 2023"), //
                    installment(10.0, 0.0, 10.0, false, "05 March 2023"), //
                    installment(817.5, 0.0, 817.5, false, "02 April 2023") //
            );
            // verify transactions
            verifyTransactions(loanId, //
                    transaction(1000.0, "Disbursement", "03 March 2023", 1000.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(250.0, "Down Payment", "03 March 2023", 750.0, 250.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(800.0, "Repayment", "03 March 2023", 0.0, 800.0, 0.0, 0.0, 0.0, 0.0, 0.0, true), //
                    transaction(20.0, "Disbursement", "05 March 2023", 770.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(30.0, "Disbursement", "05 March 2023", 800.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(40.0, "Disbursement", "05 March 2023", 840.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(10.0, "Down Payment", "05 March 2023", 830.0, 10.0, 0.0, 0.0, 0.0, 0.0, 0.0) //
            );

            assertTrue(loanDetails.getStatus().getActive());
            assertEquals(830.0, Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding()));
        });
    }

    @Test
    public void loanRepaymentScheduleWithSimpleDisbursementAndWithoutAutoPayment() {
        runSeptember2022DownPaymentTest(() -> {

            String loanExternalIdStr = UUID.randomUUID().toString();

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final DelinquencyBucketResponse delinquencyBucket = DelinquencyBucketsHelper.getBucket(delinquencyBucketId);

            Boolean enableDownPayment = true;
            BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25);
            Boolean enableAutoRepaymentForDownPayment = true;

            final Integer clientId = createClient().intValue();

            Integer loanProductId = createLoanProductWithDownPaymentConfiguration(delinquencyBucketId, enableDownPayment, "25",
                    enableAutoRepaymentForDownPayment, false);

            final GetLoanProductsProductIdResponse getLoanProductsProductResponse = retrieveLoanProduct(loanProductId.longValue());
            assertNotNull(getLoanProductsProductResponse);
            assertEquals(enableDownPayment, getLoanProductsProductResponse.getEnableDownPayment());
            assertEquals(0, getLoanProductsProductResponse.getDisbursedAmountPercentageForDownPayment()
                    .compareTo(disbursedAmountPercentageForDownPayment));
            assertEquals(enableAutoRepaymentForDownPayment, getLoanProductsProductResponse.getEnableAutoRepaymentForDownPayment());

            final Integer loanId = createAndApproveLoanAccount(clientId, loanProductId.longValue(), loanExternalIdStr, "1", "0");
            moveLoanState(
                    loanId.longValue(), new PostLoansLoanIdRequest().actualDisbursementDate("03 September 2022")
                            .transactionAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN).locale("en"),
                    "disburseWithoutAutoDownPayment");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId.longValue());
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
            Double expectedTotalOutstandingAmount = 1000.00;
            Double expectedTotalRepaymentTransactionAmount = 0.00;

            assertTrue(periods.stream() //
                    .anyMatch(period -> expectedDownPaymentAmount.equals(Utils.getDoubleValue(period.getTotalOutstandingForPeriod())) //
                            && expectedDownPaymentDueDate.equals(period.getDueDate())));
            assertEquals(expectedTotalOutstandingAmount, Utils.getDoubleValue(summary.getTotalOutstanding()));
            assertEquals(expectedTotalRepaymentTransactionAmount, Utils.getDoubleValue(summary.getTotalRepaymentTransaction()));
            assertTrue(periods.stream()
                    .anyMatch(period -> expectedRepaymentAmount.equals(Utils.getDoubleValue(period.getTotalOutstandingForPeriod()))
                            && expectedRepaymentDueDate.equals(period.getDueDate())));
        });
    }

    private void checkNoDownPaymentTransaction(final Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        if (loanDetails.getTransactions() != null) {
            for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
                assertFalse("Down Payment".equals(transaction.getType().getValue()), "Down Payment entries are posted");
            }
        }
    }

    private void checkDownPaymentTransaction(final LocalDate transactionDate, final Float principalPortion, final Float interestPortion,
            final Float feePortion, final Float penaltyPortion, final Integer loanID) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanID.longValue());
        assertNotNull(loanDetails.getTransactions());
        boolean isTransactionFound = false;
        for (GetLoansLoanIdTransactions transaction : loanDetails.getTransactions()) {
            if ("Down Payment".equals(transaction.getType().getValue())) {
                if (DateUtils.isEqual(transactionDate, transaction.getDate())) {
                    isTransactionFound = true;
                    assertEquals(principalPortion, Float.valueOf(String.valueOf(transaction.getPrincipalPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(interestPortion, Float.valueOf(String.valueOf(transaction.getInterestPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(feePortion, Float.valueOf(String.valueOf(transaction.getFeeChargesPortion())),
                            "Mismatch in transaction amounts");
                    assertEquals(penaltyPortion, Float.valueOf(String.valueOf(transaction.getPenaltyChargesPortion())),
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
                .withInterestRatePerPeriod("0").withInterestTypeAsDecliningBalance().withAmortizationTypeAsEqualPrincipalPayments()
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod().withExpectedDisbursementDate("03 March 2023")
                .withSubmittedOnDate("03 March 2023").withLoanType("individual").withExternalId(externalId)
                .build(clientID.toString(), loanProductID.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "03 March 2023"));
        return loanId.intValue();
    }

    private GetLoanProductsProductIdResponse createLoanProductWithDownPaymentConfigurationAndAccrualAccounting(Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment, final Account... accounts) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("1").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withAccountingRulePeriodicAccrual(accounts).withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30")
                .withDaysInYear("365").withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Long loanProductId = createLoanProductFromJson(loanProductJSON);
        return retrieveLoanProduct(loanProductId);
    }

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency("1")
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments("1").withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod("0").withInterestTypeAsFlatBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoanWithNetDisbursalAmount(loanId, "03 September 2022", "1000");
        return loanId.intValue();
    }

    private GetLoanProductsProductIdResponse createLoanProductWithEnableDownPaymentAndMultipleDisbursementsWithDisableRepaymentConfiguration(
            Boolean enableDownPayment, String disbursedAmountPercentageForDownPayment, boolean enableAutoRepaymentForDownPayment) {
        final String loanProductJSON = new LoanProductTestBuilder().withPrincipal("1000").withRepaymentTypeAsMonth()
                .withRepaymentAfterEvery("1").withNumberOfRepayments("3").withRepaymentTypeAsMonth().withinterestRatePerPeriod("0")
                .withInterestRateFrequencyTypeAsMonths().withAmortizationTypeAsEqualPrincipalPayment().withInterestTypeAsDecliningBalance()
                .withInterestCalculationPeriodTypeAsRepaymentPeriod(true).withDaysInMonth("30").withDaysInYear("365")
                .withMoratorium("0", "0").withMultiDisburse().withDisallowExpectedDisbursements(true)
                .withEnableDownPayment(enableDownPayment, disbursedAmountPercentageForDownPayment, enableAutoRepaymentForDownPayment)
                .build(null);
        final Long loanProductId = createLoanProductFromJson(loanProductJSON);
        return retrieveLoanProduct(loanProductId);
    }

    private Integer createLoanProductWithDownPaymentConfiguration(final Long delinquencyBucketId, Boolean enableDownPayment,
            String disbursedAmountPercentageForDownPayment, Boolean enableAutoRepaymentForDownPayment, boolean multiDisbursement) {
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
        return createLoanProductFromJson(Utils.convertToJson(loanProductMap)).intValue();
    }

    private Integer createAndApproveLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String numberOfRepayments, final String interestRate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency(numberOfRepayments)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate).withInterestTypeAsDecliningBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("03 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        return loanId.intValue();
    }

    private Integer createApproveAndDisburseLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String numberOfRepayments, final String interestRate) {

        Integer loanId = createAndApproveLoanAccount(clientID, loanProductID, externalId, numberOfRepayments, interestRate);
        disburseLoanWithAmount(loanId.longValue(), "03 September 2022", Double.parseDouble("1000"));
        return loanId;
    }

    private Integer createApproveAndDisburseTwiceLoanAccount(final Integer clientID, final Long loanProductID, final String externalId,
            final String numberOfRepayments, final String interestRate) {

        String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal("1000").withLoanTermFrequency(numberOfRepayments)
                .withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments).withRepaymentEveryAfter("1")
                .withRepaymentFrequencyTypeAsMonths().withInterestRatePerPeriod(interestRate).withInterestTypeAsDecliningBalance()
                .withAmortizationTypeAsEqualPrincipalPayments().withInterestCalculationPeriodTypeSameAsRepaymentPeriod()
                .withExpectedDisbursementDate("04 September 2022").withSubmittedOnDate("01 September 2022").withLoanType("individual")
                .withExternalId(externalId).build(clientID.toString(), loanProductID.toString(), null);

        final Long loanId = applyForLoanFromJson(loanApplicationJSON);
        approveLoan(loanId, approveLoanRequest(1000.0, "02 September 2022"));
        disburseLoanWithAmount(loanId, "03 September 2022", 700.0);
        disburseLoanWithAmount(loanId, "04 September 2022", 300.0);
        return loanId.intValue();
    }

    private PutLoanProductsProductIdResponse updateLoanProductDownPaymentConfiguration(Long id) {
        Boolean enableDownPayment = true;
        BigDecimal disbursedAmountPercentageForDownPayment = BigDecimal.valueOf(25.0);
        final PutLoanProductsProductIdRequest requestModifyLoan = new PutLoanProductsProductIdRequest().enableDownPayment(enableDownPayment)
                .disbursedAmountPercentageForDownPayment(disbursedAmountPercentageForDownPayment).locale("en");
        return updateLoanProduct(id, requestModifyLoan);
    }
}
