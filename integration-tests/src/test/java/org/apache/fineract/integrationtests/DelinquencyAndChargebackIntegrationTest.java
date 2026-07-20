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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class DelinquencyAndChargebackIntegrationTest extends FeignLoanTestBase {

    private static final String principalAmount = "1200.00";
    private static final Double doubleZERO = Double.valueOf("0.00");
    private static final DateTimeFormatter BUSINESS_DATE_FORMAT = DateTimeFormatter.ofPattern(LoanTestData.DATETIME_PATTERN);

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void testLoanClassificationStepAsPartOfCOB(String strategyCode, boolean advancedAllocation) {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getDateAsLocalDate("01 April 2012");
            LocalDate businessDate = todaysDate.minusMonths(3);
            log.info("Current Business date {}", businessDate);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final Long clientId = createClient("01 January 2012");
            final Long loanProductId = createLoanProduct(strategyCode, advancedAllocation, delinquencyBucketId);

            String operationDate = formatBusinessDate(businessDate);
            log.info("Operation date  {}", businessDate);

            final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, "12", strategyCode);

            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            String amountVal = "100.00";
            operationDate = formatBusinessDate(businessDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                    Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            Long transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = formatBusinessDate(businessDate);
            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, "0.00", "1000.00", 0, doubleZERO);

            businessDate = businessDate.plusDays(21);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            applyChargebackTransaction(loanId, transactionId, amountVal, 0);
            reviewLoanTransactionRelations(loanId, transactionId, 1);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1100.00", 0, Double.valueOf("0.00"));

            businessDate = businessDate.plusDays(14);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            executeInlineCOB(loanId);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1100.00", 14, Double.valueOf("200.00"));

            businessDate = todaysDate.plusDays(4);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = formatBusinessDate(businessDate);
            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "1000.00", 4, Double.valueOf("100.00"));

            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, 50.0);
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);
            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "950.00", 4, Double.valueOf("50.00"));
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void testLoanClassificationStepAsPartOfCOBRepeated(String strategyCode, boolean advancedAllocation) {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            List<LocalDate> expectedDates = new ArrayList<>();

            LocalDate businessDate = LocalDate.parse("2022-01-01", DateUtils.DEFAULT_DATE_FORMATTER);
            log.info("Current Business date {}", businessDate);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);

            final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
            final Long clientId = createClient("01 January 2012");
            final Long loanProductId = createLoanProduct(strategyCode, advancedAllocation, delinquencyBucketId);

            String operationDate = formatBusinessDate(businessDate);
            log.info("Operation date  {}", businessDate);

            final Long loanId = createLoanAccount(clientId, loanProductId, operationDate, "3", strategyCode);

            businessDate = businessDate.plusMonths(1);
            expectedDates.add(businessDate);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            String amountVal = "400.00";
            operationDate = formatBusinessDate(businessDate);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                    Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            Long transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            businessDate = businessDate.plusMonths(1);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            operationDate = formatBusinessDate(businessDate);
            expectedDates.add(businessDate);
            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, "0.00", "400.00", 0, doubleZERO);

            businessDate = businessDate.plusDays(15);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            applyChargebackTransaction(loanId, transactionId, amountVal, 0);
            reviewLoanTransactionRelations(loanId, transactionId, 1);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "800.00", 0, Double.valueOf("0.00"));

            businessDate = businessDate.plusDays(23);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            executeInlineCOB(loanId);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "800.00", 23, Double.valueOf("800.00"));

            businessDate = LocalDate.parse("2022-03-20", DateUtils.DEFAULT_DATE_FORMATTER);
            expectedDates.add(businessDate);
            operationDate = formatBusinessDate(businessDate);
            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0);

            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanAccount(getLoansLoanIdResponse, amountVal, "400.00", 7, Double.valueOf("400.00"));

            loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate, Double.valueOf(amountVal));
            assertNotNull(loanIdTransactionsResponse);
            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertEquals(Long.valueOf(LoanStatus.CLOSED_OBLIGATIONS_MET.getValue()), getLoansLoanIdResponse.getStatus().getId());
            log.info("Loan id {} with status {}", loanId, getLoansLoanIdResponse.getStatus().getCode());

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
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long createLoanProduct(String strategyCode, boolean advancedAllocation, Long delinquencyBucketId) {
        PostLoanProductsRequest product = fourPeriod1MonthWithoutInterest(strategyCode).delinquencyBucketId(delinquencyBucketId)
                .principal(Double.valueOf(principalAmount)).numberOfRepayments(12);
        if (advancedAllocation) {
            product.addPaymentAllocationItem(LoanRequestBuilders.paymentAllocation("REPAYMENT", "NEXT_INSTALLMENT", "PAST_DUE_PENALTY",
                    "PAST_DUE_FEE", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL", "DUE_PENALTY", "DUE_FEE", "DUE_INTEREST", "DUE_PRINCIPAL",
                    "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_INTEREST"));
        }
        return createLoanProduct(product);
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String operationDate, final String periods,
            String repaymentStrategy) {
        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, operationDate, Double.valueOf(principalAmount),
                Integer.valueOf(periods),
                req -> req.repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .loanTermFrequency(Integer.valueOf(periods)).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .interestRatePerPeriod(BigDecimal.ZERO).interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                        .expectedDisbursementDate(operationDate).submittedOnDate(operationDate)
                        .transactionProcessingStrategyCode(repaymentStrategy));
        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(Double.valueOf(principalAmount), operationDate));
        disburseLoan(loanId, operationDate, Double.valueOf(principalAmount));
        return loanId;
    }

    private DelinquencyRangeData validateLoanAccount(GetLoansLoanIdResponse getLoansLoanIdResponse, final String adjustments,
            final String outstanding, Integer pastDueDays, Double delinquentAmount) {
        assertNotNull(getLoansLoanIdResponse);
        final DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();

        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
        if (delinquencyRange != null) {
            log.info("Loan Delinquency Range is {}", delinquencyRange.getClassification());
        }
        evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(adjustments));
        DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, pastDueDays, delinquentAmount);
        validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(outstanding));

        return delinquencyRange;
    }

    private static String formatBusinessDate(LocalDate date) {
        return date.format(BUSINESS_DATE_FORMAT);
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", DEFAULT_STRATEGY), false),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY", ADVANCED_PAYMENT_ALLOCATION_STRATEGY), true));
    }

}
