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
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetDelinquencyBucketsResponse;
import org.apache.fineract.client.models.GetDelinquencyRangesResponse;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentSchedule;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.GlobalConfigurationHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.common.products.DelinquencyBucketsHelper;
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
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
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
                    assertEquals(Double.valueOf("666.67"), period.getTotalDueForPeriod());
                }
            }

            loanTransactionHelper.evaluateLoanSummaryAdjustments(getLoansLoanIdResponse, Double.valueOf(baseAmount));
            DelinquencyBucketsHelper.evaluateLoanCollectionData(getLoansLoanIdResponse, 0, Double.valueOf("0.00"));
        } finally {
            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
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
        assertEquals(2, loanTransactions.size());
        GetLoansLoanIdTransactions loanTransaction = loanTransactions.iterator().next();
        log.info("Try to apply the Charge back over transaction Id {} with type {}", loanTransaction.getId(),
                loanTransaction.getType().getCode());

        loanTransactionHelper.applyChargebackTransaction(loanId, loanTransaction.getId(), amountVal, 0, responseSpecErr503);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void applyLoanTransactionChargebackAfterMature(LoanProductTestBuilder loanProductTestBuilder) {
        try {
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);

            final LocalDate todaysDate = Utils.getLocalDateOfTenant();
            BusinessDateHelper.updateBusinessDate(requestSpec, responseSpec, BusinessDateType.BUSINESS_DATE, todaysDate);
            log.info("Current Business date {}", todaysDate);

            // Client and Loan account creation
            final Integer loanId = createAccounts(45, 1, false, loanProductTestBuilder);

            GetLoansLoanIdResponse getLoansLoanIdResponse = loanTransactionHelper.getLoan(requestSpec, responseSpec, loanId);
            assertNotNull(getLoansLoanIdResponse);

            loanTransactionHelper.printRepaymentSchedule(getLoansLoanIdResponse);

            GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
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
                    assertEquals(Double.valueOf("500.00"), period.getPrincipalDue());
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
                    assertEquals(Double.valueOf("800.00"), period.getPrincipalDue());
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
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
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

        final GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
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

        GetDelinquencyRangesResponse delinquencyRange = getLoansLoanIdResponse.getDelinquencyRange();
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
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.TRUE);
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
            GlobalConfigurationHelper.updateIsBusinessDateEnabled(requestSpec, responseSpec, Boolean.FALSE);
        }
    }

    private Integer createAccounts(final Integer daysToSubtract, final Integer numberOfRepayments, final boolean withJournalEntries,
            LoanProductTestBuilder loanProductTestBuilder) {
        // Delinquency Bucket
        final Integer delinquencyBucketId = DelinquencyBucketsHelper.createDelinquencyBucket(requestSpec, responseSpec);
        final GetDelinquencyBucketsResponse delinquencyBucket = DelinquencyBucketsHelper.getDelinquencyBucket(requestSpec, responseSpec,
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

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))));
    }

}
