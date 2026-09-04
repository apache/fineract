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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.ACCRUAL_PERIODIC;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestAccounts;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.TransactionExt;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@Slf4j
public class LoanTransactionChargebackTest extends FeignLoanTestBase {

    private final String amountVal = "1000";
    private LocalDate todaysDate;
    private String operationDate;
    private static Long clientId;

    @BeforeEach
    public void setup() {
        clientId = createClient("01 January 2012");
        this.todaysDate = Utils.getLocalDateOfTenant();
        this.operationDate = Utils.dateFormatter.format(this.todaysDate);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargeback(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, true, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();
        assertNotNull(transactionId);

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "1000.00", 0);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, amount.doubleValue());

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(getAccounts().getFundSource(), 1000.0), //
                debit(getAccounts().getLoansReceivableAccount(), 1000.0) //
        );

        // Try to reverse a Loan Transaction charge back
        CallFailedRuntimeException reverseChargebackException = assertThrows(CallFailedRuntimeException.class,
                () -> reverseLoanTransaction(loanId, chargebackTransactionId, operationDate));
        assertEquals(403, reverseChargebackException.getStatus());

        // Try to reverse a Loan Transaction repayment with linked transactions
        CallFailedRuntimeException reverseRepaymentException = assertThrows(CallFailedRuntimeException.class,
                () -> reverseLoanTransaction(loanId, transactionId, operationDate));
        assertEquals(403, reverseRepaymentException.getStatus());
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyAndAdjustLoanTransactionChargeback(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, false, strategyCode, advancedAllocation);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanTransactionResponse);
        final Long transactionId = loanTransactionResponse.getResourceId();

        final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "1000.00", 0);

        // Then
        CallFailedRuntimeException adjustException = assertThrows(CallFailedRuntimeException.class,
                () -> adjustLoanTransaction(loanId, chargebackTransactionId, operationDate));
        assertEquals(403, adjustException.getStatus());
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithAmountZero(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, false, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        applyChargebackTransactionWithExpectedStatus(loanId, transactionId, "0.00", 0, 400);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackInLongTermLoan(String strategyCode, boolean advancedAllocation) {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            LocalDate businessDate = LocalDate.of(2023, 1, 20);
            todaysDate = businessDate;
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            // Client and Loan account creation
            final Integer daysToSubtract = 1;
            final Integer numberOfRepayments = 3;
            final Long loanId = createAccounts(daysToSubtract, numberOfRepayments, false, strategyCode, advancedAllocation);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            final String baseAmount = "333.33";
            Float amount = Float.valueOf(baseAmount);
            final LocalDate transactionDate = this.todaysDate.minusMonths(numberOfRepayments - 1).plusDays(3);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                    amount.doubleValue());
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("666.67"));

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, amount.toString(), 0);
            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("666.67"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(amountVal));

            GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null && period.getPeriod() == 3) {
                    log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                            period.getTotalDueForPeriod());
                    assertEquals(Double.valueOf("666.67"), Utils.getDoubleValue(period.getTotalDueForPeriod()));
                }
            }

            evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(baseAmount));
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));
        } finally {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, todaysDate);
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackOverNoRepaymentType(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, false, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        List<GetLoansLoanIdTransactions> loanTransactions = getLoansLoanIdResponse.getTransactions();
        assertNotNull(loanTransactions);
        log.info("Loan Id {} with {} transactions", loanId, loanTransactions.size());
        assertEquals(1, loanTransactions.size());
        GetLoansLoanIdTransactions loanTransaction = loanTransactions.iterator().next();
        log.info("Try to apply the Charge back over transaction Id {} with type {}", loanTransaction.getId(),
                loanTransaction.getType().getCode());

        applyChargebackTransactionWithExpectedStatus(loanId, loanTransaction.getId(), amountVal, 0, 503);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackAfterMature(String strategyCode, boolean advancedAllocation) {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, todaysDate);
            log.info("Current Business date {}", todaysDate);

            // Client and Loan account creation
            final Long loanId = createAccounts(45, 1, false, strategyCode, advancedAllocation);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
            assertNotNull(delinquencyRange);
            log.info("Loan Delinquency Range is {}", delinquencyRange.getClassification());

            GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            assertEquals(2, getLoanRepaymentSchedule.getPeriods().size());

            Float amount = Float.valueOf(amountVal);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                    amount.doubleValue());
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");
            assertNotNull(getLoansLoanIdResponse.getTimeline());
            assertEquals(todaysDate, getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

            Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "500.00", 0);

            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("500.00"));

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("500.00"));

            assertNotNull(getLoansLoanIdResponse.getTimeline());
            assertEquals(getLoansLoanIdResponse.getTimeline().getExpectedMaturityDate(),
                    getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

            // N+1 Scenario

            getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            assertEquals(3, getLoanRepaymentSchedule.getPeriods().size());
            getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null && period.getPeriod() == 2) {
                    log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                            period.getTotalDueForPeriod());
                    assertEquals(Double.valueOf("500.00"), Utils.getDoubleValue(period.getPrincipalDue()));
                }
            }

            chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "300.00", 0);

            reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("800.00"));

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

            delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
            assertNull(delinquencyRange);
            log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));

            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("800.00"));

            // N+1 Scenario -- Remains the same periods number

            getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            assertEquals(3, getLoanRepaymentSchedule.getPeriods().size());
            getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null && period.getPeriod() == 2) {
                    log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                            period.getTotalDueForPeriod());
                    assertEquals(Double.valueOf("800.00"), Utils.getDoubleValue(period.getPrincipalDue()));
                }
            }

            // Move the Business date few days to get Collection data
            LocalDate businessDate = todaysDate.plusDays(4);
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = getLoanDetails(loanId);
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 4, Double.valueOf("800.00"));
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanActive(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, true, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "200.00", 0);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("100.00"));

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("100.00"));

        assertNotNull(getLoansLoanIdResponse.getTimeline());
        assertEquals(getLoansLoanIdResponse.getTimeline().getExpectedMaturityDate(),
                getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(getAccounts().getFundSource(), 200.0), //
                debit(getAccounts().getLoansReceivableAccount(), 100.0), //
                debit(getAccounts().getOverpaymentAccount(), 100.0) //
        );

        final DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanClose(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, false, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "100.00", 0);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToKeepAsLoanOverpaid(String strategyCode, boolean advancedAllocation) {
        // Client and Loan account creation
        final Long loanId = createAccounts(15, 1, true, strategyCode, advancedAllocation);

        GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                amount.doubleValue());
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
        final Long chargebackTransactionId = applyChargebackTransaction(loanId, transactionId, "50.00", 0);
        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = getLoanDetails(loanId);
        assertNotNull(getLoansLoanIdResponse);
        validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));

        validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(getAccounts().getFundSource(), 50.0), //
                debit(getAccounts().getOverpaymentAccount(), 50.0) //
        );
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyMultipleLoanTransactionChargeback(String strategyCode, boolean advancedAllocation) {
        try {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(BusinessDateType.BUSINESS_DATE, todaysDate);
            log.info("Current Business date {}", todaysDate);

            // Client and Loan account creation
            final Long loanId = createAccounts(15, 1, false, strategyCode, advancedAllocation);

            GetLoansLoanIdResponse getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);

            Float amount = Float.valueOf(amountVal);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = makeLoanRepayment(loanId, "Repayment", operationDate,
                    amount.doubleValue());
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();

            getLoansLoanIdResponse = getLoanDetails(loanId);
            assertNotNull(getLoansLoanIdResponse);
            validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

            // First round, empty array
            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

            applyChargebackTransaction(loanId, transactionId, "200.00", 0);

            Double expectedAmount = Double.valueOf("200.00");
            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);

            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));

            // Second round, array size equal to 1
            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));

            applyChargebackTransaction(loanId, transactionId, "300.00", 1);

            expectedAmount = Double.valueOf("500.00");
            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));

            // Third round, array size equal to 2
            reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));

            applyChargebackTransaction(loanId, transactionId, "500.00", 0);

            expectedAmount = Double.valueOf("1000.00");
            getLoansLoanIdResponse = getLoanDetails(loanId);
            validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);

            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Nested
    public class ProgressiveInterestBearingLoanWithInterestRecalculationTest {

        Long applyApproveDisburseLoan(Long loanProductId) {
            AtomicReference<Long> loanIdRef = new AtomicReference<>();
            runAt("1 January 2024", () -> {
                Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 100.0, 7.0, 6, null);
                loanIdRef.set(loanId);
                disburseLoan(loanId, BigDecimal.valueOf(100.0), "01 January 2024");
            });
            return loanIdRef.get();
        }

        List<CreditAllocationData> chargebackCreditAllocationOrders(List<String> allocationIds) {
            List<CreditAllocationOrder> creditAllocationOrders = new ArrayList<>(allocationIds.size());
            for (int i = 0; i < allocationIds.size(); i++) {
                String allocationId = allocationIds.get(i);
                creditAllocationOrders.add(new CreditAllocationOrder().order(i + 1).creditAllocationRule(allocationId));
            }
            return List.of(new CreditAllocationData().transactionType("CHARGEBACK").creditAllocationOrder(creditAllocationOrders));
        }

        @Nested
        public class WithoutChargebackAllocation {

            final Long loanProductWithoutChargebackAllocation = createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true)
                    .daysInYearType(DaysInYearType.DAYS_360).daysInMonthType(DaysInMonthType.DAYS_30));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    addChargebackForLoan(loanId, repaymentId, 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.53, 0.49, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(17.0, 0.10, "01 July 2024") //
                    ); //
                    Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                    reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation);
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    addChargebackForLoan(loanId, repaymentFebruaryRef.get(), 15.0);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(31.53, 0.48, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.99, 0.10, "01 July 2024") //
                    ); //
                    addChargebackForLoan(loanId, repaymentId, 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(48.44, 0.58, "01 April 2024"), //
                            unpaidInstallment(16.71, 0.30, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(17.10, 0.10, "01 July 2024") //
                    ); //
                    Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                    reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation);
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId.set(makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                });
                runAt("15 March 2024", () -> {
                    addChargebackForLoan(loanId, repaymentMarchId.get(), 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.57, 0.45, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.96, 0.10, "01 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "15 March 2024");
                    reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS7ChargebacksOnMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 July 2024", 17.00);
                    repaymentJulyRef.set(repayment.getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    addChargebackForLoan(loanId, repaymentJulyRef.get(), 17.00);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            installment(33.9, 0.10, 17.0, false, "01 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "01 July 2024");
                    reverseLoanTransaction(loanId, repaymentId, "01 July 2024");
                });

            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 July 2024", 17.00);
                    repaymentJulyRef.set(repayment.getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                });
                runAt("15 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJuneRef.get(), 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(17.01, 0.0, "15 July 2024") //
                    ); //
                });
                runAt("30 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJulyRef.get(), 17.00);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(34.01, 0.0, "30 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "30 July 2024");
                    reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }
        }

        @Nested
        public class WithChargebackAllocationPrincipalInterestFeesPenalties {

            final Long loanProductWithChargebackAllocationPrincipalInterestFeesPenalties = createLoanProduct(create4IProgressive()
                    .isInterestRecalculationEnabled(true).daysInYearType(DaysInYearType.DAYS_360).daysInMonthType(DaysInMonthType.DAYS_30)
                    .creditAllocation(chargebackCreditAllocationOrders(List.of("PRINCIPAL", "PENALTY", "FEE", "INTEREST"))));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationPrincipalInterestFeesPenalties);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    addChargebackForLoan(loanId, repaymentId, 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.04, 0.98, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(17.0, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Chargeback", "01 March 2024", 83.57, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                    reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationPrincipalInterestFeesPenalties);
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    runAt("1 March 2024", () -> {
                        Long repaymentMarchId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                        verifyRepaymentSchedule(loanId, //
                                installment(100.0, null, "01 January 2024"), //
                                fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                                fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                                unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                                unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                                unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                                unpaidInstallment(16.9, 0.10, "01 July 2024") //
                        ); //
                        verifyTransactions(loanId,
                                new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                        addChargebackForLoan(loanId, repaymentFebruaryRef.get(), 15.0);
                        verifyTransactions(loanId,
                                new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(15.00, "Chargeback", "01 March 2024", 82.05, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, false));
                        verifyRepaymentSchedule(loanId, //
                                installment(100.0, null, "01 January 2024"), //
                                fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                                fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                                unpaidInstallment(31.53, 0.48, "01 April 2024"), //
                                unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                                unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                                unpaidInstallment(16.99, 0.10, "01 July 2024") //
                        ); //

                        addChargebackForLoan(loanId, repaymentMarchId, 17.01);
                        verifyTransactions(loanId,
                                new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(15.00, "Chargeback", "01 March 2024", 82.05, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                                new TransactionExt(17.01, "Chargeback", "01 March 2024", 98.57, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                        verifyRepaymentSchedule(loanId, //
                                installment(100.0, null, "01 January 2024"), //
                                fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                                fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                                unpaidInstallment(47.96, 1.06, "01 April 2024"), //
                                unpaidInstallment(16.71, 0.30, "01 May 2024"), //
                                unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                                unpaidInstallment(17.09, 0.10, "01 July 2024") //
                        ); //
                        Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                        reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    });
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationPrincipalInterestFeesPenalties);
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId.set(makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
                });
                runAt("15 March 2024", () -> {
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    );
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    addChargebackForLoan(loanId, repaymentMarchId.get(), 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.09, 0.93, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.95, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Chargeback", "15 March 2024", 83.57, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "15 March 2024");
                    reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationPrincipalInterestFeesPenalties);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 July 2024", 17.00);
                    repaymentJulyRef.set(repayment.getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                });
                runAt("15 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJuneRef.get(), 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(16.81, 0.2, "15 July 2024") //
                    ); //
                });
                runAt("30 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJulyRef.get(), 17.00);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(33.71, 0.3, "30 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "30 July 2024");
                    reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }
        }

        @Nested
        public class WithChargebackAllocationInterestFeesPenaltiesPrincipal {

            final Long loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal = createLoanProduct(create4IProgressive()
                    .isInterestRecalculationEnabled(true).daysInYearType(DaysInYearType.DAYS_360).daysInMonthType(DaysInMonthType.DAYS_30)
                    .creditAllocation(chargebackCreditAllocationOrders(List.of("PENALTY", "FEE", "INTEREST", "PRINCIPAL"))));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    addChargebackForLoan(loanId, repaymentId, 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.04, 0.98, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(17.0, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Chargeback", "01 March 2024", 83.57, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                    reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal);
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    Long repaymentMarchId = makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    addChargebackForLoan(loanId, repaymentFebruaryRef.get(), 15.0);
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(15.00, "Chargeback", "01 March 2024", 81.47, 14.42, 0.58, 0.0, 0.0, 0.0, 0.0, false));
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(30.95, 1.06, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.99, 0.10, "01 July 2024") //
                    ); //

                    addChargebackForLoan(loanId, repaymentMarchId, 17.01);
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(15.00, "Chargeback", "01 March 2024", 81.47, 14.42, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Chargeback", "01 March 2024", 97.99, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(47.38, 1.64, "01 April 2024"), //
                            unpaidInstallment(16.71, 0.30, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(17.09, 0.10, "01 July 2024") //
                    ); //
                    Long prepayId = verifyPrepayAmountByRepayment(loanId, "1 March 2024");
                    reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal);
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId.set(makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
                });
                runAt("15 March 2024", () -> {
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(16.62, 0.39, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.9, 0.10, "01 July 2024") //
                    );
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    addChargebackForLoan(loanId, repaymentMarchId.get(), 17.01);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            unpaidInstallment(33.09, 0.93, "01 April 2024"), //
                            unpaidInstallment(16.72, 0.29, "01 May 2024"), //
                            unpaidInstallment(16.81, 0.20, "01 June 2024"), //
                            unpaidInstallment(16.95, 0.10, "01 July 2024") //
                    ); //
                    verifyTransactions(loanId,
                            new TransactionExt(100.0, "Disbursement", "01 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 February 2024", 83.57, 16.43, 0.58, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Repayment", "01 March 2024", 67.05, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false),
                            new TransactionExt(17.01, "Chargeback", "15 March 2024", 83.57, 16.52, 0.49, 0.0, 0.0, 0.0, 0.0, false));
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "15 March 2024");
                    reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 July 2024", 17.00);
                    repaymentJulyRef.set(repayment.getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                });
                runAt("15 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJuneRef.get(), 17.01);
                    // TODO verify TRANSACTIONS!!!!
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(16.81, 0.2, "15 July 2024") //
                    ); //
                });
                runAt("30 July 2024", () -> {
                    addChargebackForLoan(loanId, repaymentJulyRef.get(), 17.00);
                    // TODO verify TRANSACTIONS!!!!
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024"), //
                            unpaidInstallment(33.71, 0.3, "30 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "30 July 2024");
                    reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }

            @Test
            public void testS7ChargebacksOnMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal);
                runAt("1 February 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = makeLoanRepayment(loanId, "Repayment", "01 July 2024", 17.00);
                    repaymentJulyRef.set(repayment.getResourceId());
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            fullyRepaidInstallment(16.9, 0.10, "01 July 2024") //
                    ); //
                    addChargebackForLoan(loanId, repaymentJulyRef.get(), 17.00);
                    verifyRepaymentSchedule(loanId, //
                            installment(100.0, null, "01 January 2024"), //
                            fullyRepaidInstallment(16.43, 0.58, "01 February 2024"), //
                            fullyRepaidInstallment(16.52, 0.49, "01 March 2024"), //
                            fullyRepaidInstallment(16.62, 0.39, "01 April 2024"), //
                            fullyRepaidInstallment(16.72, 0.29, "01 May 2024"), //
                            fullyRepaidInstallment(16.81, 0.20, "01 June 2024"), //
                            installment(33.8, 0.20, 17.0, false, "01 July 2024") //
                    ); //
                    Long repaymentId = verifyPrepayAmountByRepayment(loanId, "01 July 2024");
                    reverseLoanTransaction(loanId, repaymentId, "01 July 2024");
                });
                runAt("2 July 2024", () -> {
                    executeInlineCOB(loanId);
                });

            }
        }
    }

    private Long applyChargebackTransactionWithExpectedStatus(Long loanId, Long transactionId, String amount, int paymentTypeIdx,
            int expectedStatus) {
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> applyChargebackTransaction(loanId, transactionId, amount, paymentTypeIdx));
        assertEquals(expectedStatus, exception.getStatus());
        return null;
    }

    private Long createAccounts(final Integer daysToSubtract, final Integer numberOfRepayments, final boolean withJournalEntries,
            String strategyCode, boolean advancedAllocation) {
        final Long delinquencyBucketId = DelinquencyBucketsHelper.createDefaultBucket();
        final Long loanProductId = createChargebackLoanProduct(strategyCode, advancedAllocation, delinquencyBucketId, withJournalEntries);
        final LocalDate transactionDate = this.todaysDate.minusDays(daysToSubtract + (30L * (numberOfRepayments - 1)));
        String operationDate = Utils.dateFormatter.format(transactionDate);
        return createLoanAccount(clientId, loanProductId, operationDate, amountVal, numberOfRepayments, strategyCode);
    }

    private Long createChargebackLoanProduct(String strategyCode, boolean advancedAllocation, Long delinquencyBucketId,
            boolean withJournalEntries) {
        LoanProductTestBuilder builder = advancedAllocation
                ? new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                        .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                        .addAdvancedPaymentAllocation(LoanRequestBuilders.defaultPaymentAllocation(), createRepaymentPaymentAllocation())
                : new LoanProductTestBuilder().withRepaymentStrategy(strategyCode);

        if (withJournalEntries) {
            LoanTestAccounts accounts = getAccounts();
            return createLoanProduct(builder
                    .withFullAccountingConfig(ACCRUAL_PERIODIC,
                            LoanProductTestBuilder.FullAccountingConfig.builder()
                                    .fundSourceAccountId(accounts.getFundSource().getAccountID().longValue())
                                    .loanPortfolioAccountId(accounts.getLoansReceivableAccount().getAccountID().longValue())
                                    .transfersInSuspenseAccountId(accounts.getSuspenseAccount().getAccountID().longValue())
                                    .interestOnLoanAccountId(accounts.getInterestIncomeAccount().getAccountID().longValue())
                                    .incomeFromFeeAccountId(accounts.getFeeIncomeAccount().getAccountID().longValue())
                                    .incomeFromPenaltyAccountId(accounts.getPenaltyIncomeAccount().getAccountID().longValue())
                                    .incomeFromRecoveryAccountId(accounts.getRecoveriesAccount().getAccountID().longValue())
                                    .writeOffAccountId(accounts.getWrittenOffAccount().getAccountID().longValue())
                                    .overpaymentLiabilityAccountId(accounts.getOverpaymentAccount().getAccountID().longValue())
                                    .receivableInterestAccountId(accounts.getInterestReceivableAccount().getAccountID().longValue())
                                    .receivableFeeAccountId(accounts.getInterestReceivableAccount().getAccountID().longValue())
                                    .receivablePenaltyAccountId(accounts.getInterestReceivableAccount().getAccountID().longValue()).build())
                    .buildRequest(null, delinquencyBucketId));
        }
        return createLoanProduct(builder.buildRequest(null, delinquencyBucketId));
    }

    private Long createLoanAccount(final Long clientId, final Long loanProductId, final String operationDate, final String principalAmount,
            final Integer numberOfRepayments, final String repaymentStrategy) {
        PostLoansRequest request = applyLoanRequest(clientId, loanProductId, operationDate, Double.valueOf(principalAmount),
                numberOfRepayments,
                req -> req.repaymentEvery(1).repaymentFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .loanTermFrequency(numberOfRepayments).loanTermFrequencyType(LoanTestData.RepaymentFrequencyType.MONTHS)
                        .interestRatePerPeriod(BigDecimal.ZERO).interestType(LoanTestData.InterestType.DECLINING_BALANCE)
                        .expectedDisbursementDate(operationDate).submittedOnDate(operationDate)
                        .transactionProcessingStrategyCode(repaymentStrategy));
        Long loanId = applyForLoan(request);
        approveLoan(loanId, approveLoanRequest(Double.valueOf(principalAmount), operationDate));
        disburseLoan(loanId, operationDate, Double.valueOf(principalAmount));
        return loanId;
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

    private static List<PaymentAllocationOrder> getPaymentAllocationOrder(PaymentAllocationType... paymentAllocationTypes) {
        java.util.concurrent.atomic.AtomicInteger order = new java.util.concurrent.atomic.AtomicInteger(1);
        return java.util.Arrays.stream(paymentAllocationTypes)
                .map(pat -> new PaymentAllocationOrder().paymentAllocationRule(pat.name()).order(order.getAndIncrement())).toList();
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", DEFAULT_STRATEGY), false),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY", ADVANCED_PAYMENT_ALLOCATION_STRATEGY), true));
    }
}
