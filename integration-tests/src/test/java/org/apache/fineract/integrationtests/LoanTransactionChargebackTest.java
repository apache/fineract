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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.CreditAllocationData;
import org.apache.fineract.client.models.CreditAllocationOrder;
import org.apache.fineract.client.models.DelinquencyBucketData;
import org.apache.fineract.client.models.DelinquencyRangeData;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
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
public class LoanTransactionChargebackTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpecErr400;
    private ResponseSpecification responseSpecErr403;
    private ResponseSpecification responseSpecErr503;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private final String amountVal = "1000";
    private LocalDate todaysDate;
    private String operationDate;
    private static Long clientId;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpecErr400 = new ResponseSpecBuilder().expectStatusCode(400).build();
        this.responseSpecErr403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.responseSpecErr503 = new ResponseSpecBuilder().expectStatusCode(503).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.journalEntryHelper = new JournalEntryHelper(requestSpec, responseSpec);
        this.accountHelper = new AccountHelper(requestSpec, responseSpec);
        PostClientsResponse client = new ClientHelper(requestSpec, responseSpec).createClient(ClientHelper.defaultClientCreationRequest());
        clientId = client.getResourceId();

        this.todaysDate = Utils.getLocalDateOfTenant();
        this.operationDate = Utils.dateFormatter.format(this.todaysDate);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargeback(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, true, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();
        assertNotNull(transactionId);

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "1000.00", 0,
                responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, amount.doubleValue());

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(fundSource, 1000.0), //
                debit(loansReceivableAccount, 1000.0) //
        );

        // Try to reverse a Loan Transaction charge back
        PostLoansLoanIdTransactionsResponse reverseTransactionResponse = loanTransactionHelper.reverseLoanTransaction(loanId,
                chargebackTransactionId, operationDate, responseSpecErr403);

        // Try to reverse a Loan Transaction repayment with linked transactions
        reverseTransactionResponse = loanTransactionHelper.reverseLoanTransaction(loanId, transactionId, operationDate, responseSpecErr503);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyAndAdjustLoanTransactionChargeback(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, false, loanProductTestBuilder);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanTransactionResponse);
        final Long transactionId = loanTransactionResponse.getResourceId();

        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "1000.00", 0,
                responseSpec);

        // Then
        loanTransactionHelper.adjustLoanTransaction(loanId, chargebackTransactionId, operationDate, responseSpecErr403);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithAmountZero(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, false, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf(amountVal);
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "0.00", 0, responseSpecErr400);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackInLongTermLoan(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            LocalDate businessDate = LocalDate.of(2023, 1, 20);
            todaysDate = businessDate;
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            // Client and Loan account creation
            final Integer daysToSubtract = 1;
            final Integer numberOfRepayments = 3;
            final Integer loanId = createAccounts(daysToSubtract, numberOfRepayments, false, loanProductTestBuilder);

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

            final String baseAmount = "333.33";
            Float amount = Float.valueOf(baseAmount);
            final LocalDate transactionDate = this.todaysDate.minusMonths(numberOfRepayments - 1).plusDays(3);
            String operationDate = Utils.dateFormatter.format(transactionDate);

            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                    loanId);
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();
            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("666.67"));

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, amount.toString(),
                    0, responseSpec);
            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("666.67"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("1000.00"));

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf(amountVal));

            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
            GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            for (GetLoansLoanIdRepaymentPeriod period : getLoanRepaymentSchedule.getPeriods()) {
                if (period.getPeriod() != null && period.getPeriod() == 3) {
                    log.info("Period number {} for due date {} and totalDueForPeriod {}", period.getPeriod(), period.getDueDate(),
                            period.getTotalDueForPeriod());
                    assertEquals(Double.valueOf("666.67"), Utils.getDoubleValue(period.getTotalDueForPeriod()));
                }
            }

            loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(baseAmount));
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));
        } finally {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackOverNoRepaymentType(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, false, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        List<GetLoansLoanIdTransactions> loanTransactions = getLoansLoanIdResponse.getTransactions();
        assertNotNull(loanTransactions);
        log.info("Loan Id {} with {} transactions", loanId, loanTransactions.size());
        assertEquals(1, loanTransactions.size());
        GetLoansLoanIdTransactions loanTransaction = loanTransactions.iterator().next();
        log.info("Try to apply the Charge back over transaction Id {} with type {}", loanTransaction.getId(),
                loanTransaction.getType().getCode());

        loanTransactionHelper.applyChargebackTransaction(loanId, loanTransaction.getId(), amountVal, 0, responseSpecErr503);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackAfterMature(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            log.info("Current Business date {}", todaysDate);

            // Client and Loan account creation
            final Integer loanId = createAccounts(45, 1, false, loanProductTestBuilder);

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

            DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
            assertNotNull(delinquencyRange);
            log.info("Loan Delinquency Range is {}", delinquencyRange.getClassification());

            GetLoansLoanIdRepaymentSchedule getLoanRepaymentSchedule = getLoansLoanIdResponse.getRepaymentSchedule();
            log.info("Loan with {} periods", getLoanRepaymentSchedule.getPeriods().size());
            assertEquals(2, getLoanRepaymentSchedule.getPeriods().size());

            Float amount = Float.valueOf(amountVal);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                    loanId);
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");
            assertNotNull(getLoansLoanIdResponse.getTimeline());
            assertEquals(todaysDate, getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

            Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "500.00", 0,
                    responseSpec);

            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("500.00"));

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("500.00"));

            assertNotNull(getLoansLoanIdResponse.getTimeline());
            assertEquals(getLoansLoanIdResponse.getTimeline().getExpectedMaturityDate(),
                    getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

            // N+1 Scenario
            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
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

            chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "300.00", 0, responseSpec);

            reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));
            reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("800.00"));

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

            delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
            assertNull(delinquencyRange);
            log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));

            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("800.00"));

            // N+1 Scenario -- Remains the same periods number
            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);
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
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, businessDate);
            log.info("Current Business date {}", businessDate);

            // Get loan details expecting to have a delinquency classification
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 4, Double.valueOf("800.00"));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanActive(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, true, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "200.00", 0,
                responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("100.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.active");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("100.00"));

        assertNotNull(getLoansLoanIdResponse.getTimeline());
        assertEquals(getLoansLoanIdResponse.getTimeline().getExpectedMaturityDate(),
                getLoansLoanIdResponse.getTimeline().getActualMaturityDate());

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(fundSource, 200.0), //
                debit(loansReceivableAccount, 100.0), //
                debit(overpaymentAccount, 100.0) //
        );

        final DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToLoanClose(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, false, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "100.00", 0,
                responseSpec);

        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackWithLoanOverpaidToKeepAsLoanOverpaid(LoanProductTestBuilder loanProductTestBuilder) {
        // Client and Loan account creation
        final Integer loanId = createAccounts(15, 1, true, loanProductTestBuilder);

        GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);

        loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

        Float amount = Float.valueOf("1100.00");
        PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                loanId);
        assertNotNull(loanIdTransactionsResponse);
        final Long transactionId = loanIdTransactionsResponse.getResourceId();

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

        DelinquencyRangeData delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));
        final Long chargebackTransactionId = loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "50.00", 0,
                responseSpec);
        reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));
        reviewLoanTransactionRelations(loanId, chargebackTransactionId, 0, Double.valueOf("0.00"));

        getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
        assertNotNull(getLoansLoanIdResponse);
        loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.overpaid");

        delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
        assertNull(delinquencyRange);
        log.info("Loan Delinquency Range is null {}", (delinquencyRange == null));

        loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, Double.valueOf("0.00"));

        verifyTRJournalEntries(chargebackTransactionId, //
                credit(fundSource, 50.0), //
                debit(overpaymentAccount, 50.0) //
        );
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyMultipleLoanTransactionChargeback(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
                    new PutGlobalConfigurationsRequest().enabled(true));
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            log.info("Current Business date {}", todaysDate);

            // Client and Loan account creation
            final Integer loanId = createAccounts(15, 1, false, loanProductTestBuilder);

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

            Float amount = Float.valueOf(amountVal);
            PostLoansLoanIdTransactionsResponse loanIdTransactionsResponse = loanTransactionHelper.makeLoanRepayment(operationDate, amount,
                    loanId);
            assertNotNull(loanIdTransactionsResponse);
            final Long transactionId = loanIdTransactionsResponse.getResourceId();

            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);
            loanTransactionHelper.validateLoanStatus(getLoansLoanIdResponse, "loanStatusType.closed.obligations.met");

            // First round, empty array
            reviewLoanTransactionRelations(loanId, transactionId, 0, Double.valueOf("0.00"));

            loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "200.00", 0, responseSpec);

            Double expectedAmount = Double.valueOf("200.00");
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);
            loanTransactionHelper.printDelinquencyData(getLoansLoanIdResponse);
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));

            // Second round, array size equal to 1
            reviewLoanTransactionRelations(loanId, transactionId, 1, Double.valueOf("0.00"));

            loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "300.00", 1, responseSpec);

            expectedAmount = Double.valueOf("500.00");
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));

            // Third round, array size equal to 2
            reviewLoanTransactionRelations(loanId, transactionId, 2, Double.valueOf("0.00"));

            loanTransactionHelper.applyChargebackTransaction(loanId, transactionId, "500.00", 0, responseSpec);

            expectedAmount = Double.valueOf("1000.00");
            getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            loanTransactionHelper.validateLoanPrincipalOustandingBalance(getLoansLoanIdResponse, expectedAmount);

            loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, expectedAmount);
            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.ENABLE_BUSINESS_DATE,
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

            final PostLoanProductsResponse loanProductWithoutChargebackAllocation = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true).daysInYearType(DaysInYearType.DAYS_360)
                            .daysInMonthType(DaysInMonthType.DAYS_30));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation.getResourceId());
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation.getResourceId());
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId
                            .set(loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS7ChargebacksOnMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 July 2024", 17.00);
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "01 July 2024");
                });

            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(loanProductWithoutChargebackAllocation.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 July 2024", 17.00);
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }
        }

        @Nested
        public class WithChargebackAllocationPrincipalInterestFeesPenalties {

            final PostLoanProductsResponse loanProductWithChargebackAllocationPrincipalInterestFeesPenalties = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true).daysInYearType(DaysInYearType.DAYS_360)
                            .daysInMonthType(DaysInMonthType.DAYS_30)
                            .creditAllocation(chargebackCreditAllocationOrders(List.of("PRINCIPAL", "PENALTY", "FEE", "INTEREST"))));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationPrincipalInterestFeesPenalties.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationPrincipalInterestFeesPenalties.getResourceId());
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    runAt("1 March 2024", () -> {
                        Long repaymentMarchId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01)
                                .getResourceId();
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
                        loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    });
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationPrincipalInterestFeesPenalties.getResourceId());
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId
                            .set(loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationPrincipalInterestFeesPenalties.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 July 2024", 17.00);
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }
        }

        @Nested
        public class WithChargebackAllocationInterestFeesPenaltiesPrincipal {

            final PostLoanProductsResponse loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal = loanProductHelper
                    .createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(true).daysInYearType(DaysInYearType.DAYS_360)
                            .daysInMonthType(DaysInMonthType.DAYS_30)
                            .creditAllocation(chargebackCreditAllocationOrders(List.of("PENALTY", "FEE", "INTEREST", "PRINCIPAL"))));

            @Test
            public void testS1FullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    Long repaymentId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId();
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                    GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
                    verifyLoanStatus(loanDetails, LoanStatus.ACTIVE);
                });
            }

            @Test
            public void testS2AndS3PartialChargebackThenFullChargebackBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal.getResourceId());
                AtomicReference<Long> repaymentFebruaryRef = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 February 2024", 17.01);
                    repaymentFebruaryRef.set(repayment.getResourceId());
                });
                runAt("1 March 2024", () -> {
                    Long repaymentMarchId = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01)
                            .getResourceId();
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, prepayId, "1 March 2024");
                });
            }

            @Test
            public void testS4FullChargebackMiddleOfRepaymentPeriodBeforeMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal.getResourceId());
                AtomicReference<Long> repaymentMarchId = new AtomicReference<>();
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    repaymentMarchId
                            .set(loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01).getResourceId());
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "15 March 2024");
                });
            }

            @Test
            public void testS5AndS6ChargebacksAfterMaturityDateVerifyNPlus1ThPeriod() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 July 2024", 17.00);
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "30 July 2024");
                });

            }

            @Test
            public void testS7ChargebacksOnMaturityDate() {
                final Long loanId = applyApproveDisburseLoan(
                        loanProductWithChargebackAllocationInterestFeesPenaltiesPrincipal.getResourceId());
                runAt("1 February 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 February 2024", 17.01);
                });
                runAt("1 March 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 March 2024", 17.01);
                });
                runAt("1 April 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 April 2024", 17.01);
                });
                runAt("1 May 2024", () -> {
                    loanTransactionHelper.makeLoanRepayment(loanId, "Repayment", "01 May 2024", 17.01);
                });
                AtomicReference<Long> repaymentJuneRef = new AtomicReference<>();
                runAt("1 June 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 June 2024", 17.01);
                    repaymentJuneRef.set(repayment.getResourceId());
                });
                AtomicReference<Long> repaymentJulyRef = new AtomicReference<>();
                runAt("1 July 2024", () -> {
                    PostLoansLoanIdTransactionsResponse repayment = loanTransactionHelper.makeLoanRepayment(loanId, "Repayment",
                            "01 July 2024", 17.00);
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
                    loanTransactionHelper.reverseLoanTransaction(loanId, repaymentId, "01 July 2024");
                });
                runAt("2 July 2024", () -> {
                    executeInlineCOB(loanId);
                });

            }
        }
    }

    private Integer createAccounts(final Integer daysToSubtract, final Integer numberOfRepayments, final boolean withJournalEntries,
            LoanProductTestBuilder loanProductTestBuilder) {
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final DelinquencyBucketData delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
                delinquencyBucketId);

        // Client and Loan account creation
        final Integer clientId = ClientHelper.createClient(this.requestSpec, this.responseSpec, "01 January 2012");
        final GetLoanProductsProductIdResponse getLoanProductsProductResponse = createLoanProduct(loanTransactionHelper,
                delinquencyBucketId, withJournalEntries, loanProductTestBuilder);
        assertNotNull(getLoanProductsProductResponse);
        log.info("Loan Product Bucket Name: {}", getLoanProductsProductResponse.getDelinquencyBucket().getName());
        assertEquals(getLoanProductsProductResponse.getDelinquencyBucket().getName(), delinquencyBucket.getName());

        // Older date to have more than one overdue installment
        final LocalDate transactionDate = this.todaysDate.minusDays(daysToSubtract + (30 * (numberOfRepayments - 1)));
        String operationDate = Utils.dateFormatter.format(transactionDate);

        return createLoanAccount(loanTransactionHelper, clientId.toString(), getLoanProductsProductResponse.getId().toString(),
                operationDate, amountVal, numberOfRepayments.toString(), loanProductTestBuilder.getTransactionProcessingStrategyCode());
    }

    private GetLoanProductsProductIdResponse createLoanProduct(final LoanTransactionHelper loanTransactionHelper,
            final Integer delinquencyBucketId, final boolean withJournalEntries, LoanProductTestBuilder loanProductTestBuilder) {
        final HashMap<String, Object> loanProductMap;
        if (withJournalEntries) {
            loanProductMap = loanProductTestBuilder
                    .withFullAccountingConfig(ACCRUAL_PERIODIC,
                            LoanProductTestBuilder.FullAccountingConfig.builder().fundSourceAccountId(fundSource.getAccountID().longValue())//
                                    .loanPortfolioAccountId(loansReceivableAccount.getAccountID().longValue())//
                                    .transfersInSuspenseAccountId(suspenseAccount.getAccountID().longValue())//
                                    .interestOnLoanAccountId(interestIncomeAccount.getAccountID().longValue())//
                                    .incomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                                    .incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue())//
                                    .incomeFromRecoveryAccountId(recoveriesAccount.getAccountID().longValue())//
                                    .writeOffAccountId(writtenOffAccount.getAccountID().longValue())//
                                    .overpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())//
                                    .receivableInterestAccountId(interestReceivableAccount.getAccountID().longValue())//
                                    .receivableFeeAccountId(interestReceivableAccount.getAccountID().longValue())//
                                    .receivablePenaltyAccountId(interestReceivableAccount.getAccountID().longValue()).build())
                    .build(null, delinquencyBucketId);
        } else {
            loanProductMap = loanProductTestBuilder.build(null, delinquencyBucketId);
        }
        final Integer loanProductId = loanTransactionHelper.getLoanProductId(Utils.convertToJson(loanProductMap));
        return loanTransactionHelper.getLoanProduct(loanProductId);
    }

    private Integer createLoanAccount(final LoanTransactionHelper loanTransactionHelper, final String clientId, final String loanProductId,
            final String operationDate, final String principalAmount, final String numberOfRepayments, final String repaymentStrategy) {
        final String loanApplicationJSON = new LoanApplicationTestBuilder().withPrincipal(principalAmount)
                .withLoanTermFrequency(numberOfRepayments).withLoanTermFrequencyAsMonths().withNumberOfRepayments(numberOfRepayments)
                .withRepaymentEveryAfter("1").withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("0") //
                .withExpectedDisbursementDate(operationDate) //
                .withInterestTypeAsDecliningBalance() //
                .withSubmittedOnDate(operationDate) //
                .withRepaymentStrategy(repaymentStrategy) //
                .build(clientId, loanProductId, null);
        final Integer loanId = loanTransactionHelper.getLoanId(loanApplicationJSON);
        loanTransactionHelper.approveLoan(operationDate, principalAmount, loanId, null);
        loanTransactionHelper.disburseLoanWithNetDisbursalAmount(operationDate, loanId, principalAmount);
        return loanId;
    }

    private void reviewLoanTransactionRelations(final Integer loanId, final Long transactionId, final Integer expectedSize,
            final Double outstandingBalance) {
        log.info("Loan Transaction Id: {} {}", loanId, transactionId);

        GetLoansLoanIdTransactionsTransactionIdResponse getLoansTransactionResponse = loanTransactionHelper.getLoanTransaction(loanId,
                transactionId.intValue());
        log.info("Loan with {} Chargeback Transactions and balance {}", getLoansTransactionResponse.getTransactionRelations().size(),
                getLoansTransactionResponse.getOutstandingLoanBalance());
        assertNotNull(getLoansTransactionResponse);
        assertNotNull(getLoansTransactionResponse.getTransactionRelations());
        assertEquals(expectedSize, getLoansTransactionResponse.getTransactionRelations().size());
        // Outstanding amount
        assertEquals(outstandingBalance, getLoansTransactionResponse.getOutstandingLoanBalance());
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

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))));
    }

}
