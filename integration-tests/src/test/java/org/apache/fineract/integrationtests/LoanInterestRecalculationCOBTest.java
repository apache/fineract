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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.RepaymentFrequencyType.DAYS;
import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.RepaymentFrequencyType.MONTHS;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

@Slf4j
public class LoanInterestRecalculationCOBTest extends BaseLoanIntegrationTest {

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static LoanTransactionHelper loanTransactionHelper;
    private static PostClientsResponse client;
    private static InlineLoanCOBHelper inlineLoanCOBHelper;
    private static BusinessStepHelper businessStepHelper;
    private static SchedulerJobHelper schedulerJobHelper;

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", "default");
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        loanTransactionHelper = new LoanTransactionHelper(requestSpec, responseSpec);
        schedulerJobHelper = new SchedulerJobHelper(requestSpec);
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
        businessStepHelper = new BusinessStepHelper();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS", "ACCRUAL_ACTIVITY_POSTING", "LOAN_INTEREST_RECALCULATION");
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnProgressiveLoanCOB() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(
                    createOnePeriod30DaysPeriodicAccrualProductWithAdvancedPaymentAllocationAndInterestRecalculation(16.0, 4)
                            .maxInterestRatePerPeriod(120.0).maxPrincipal(10000.0));

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 4,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(4)//
                            .loanTermFrequencyType(MONTHS)//
                            .interestRatePerPeriod(BigDecimal.valueOf(120.0)).interestCalculationPeriodType(DAYS)//
                            .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            loanDetails.getRepaymentSchedule().getPeriods().forEach(p -> log.info("validateRepaymentPeriod before: {} {} {}", p.getPeriod(),
                    p.getPrincipalOriginalDue(), p.getInterestOriginalDue()));//

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1700.66, 0.0, 0.0, 815.34);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1936.12, 0.0, 0.0, 579.88);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 2071.31, 0.0, 0.0, 444.69);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 2291.91, 0.0, 0.0, 226.05);
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            loanDetails.getRepaymentSchedule().getPeriods().forEach(p -> log.info("validateRepaymentPeriod after: {} {} {}", p.getPeriod(),
                    p.getPrincipalOriginalDue(), p.getInterestOriginalDue()));//

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1700.66, 0.0, 0.0, 815.34);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1936.12, 0.0, 0.0, 579.88);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 2071.31, 0.0, 0.0, 444.69);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 2291.91, 0.0, 0.0, 226.05);
        });

    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnCumulativeLoanCOBStep() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(10.0)
                    .isInterestRecalculationEnabled(true)//
                    .maxPrincipal(10000.0).minNumberOfRepayments(1).rescheduleStrategyMethod(1).recalculationRestFrequencyType(MONTHS)
                    .recalculationRestFrequencyInterval(1).recalculationCompoundingFrequencyType(MONTHS)
                    .recalculationCompoundingFrequencyInterval(30).interestRecalculationCompoundingMethod(1));

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 2,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(2)//
                            .loanTermFrequencyType(MONTHS)//
                            .interestRatePerPeriod(BigDecimal.valueOf(10.0)).interestCalculationPeriodType(DAYS)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 3783.06, 0.0, 3783.06, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 817.94,
                    0.0, 817.94, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 4216.94, 0.0, 4216.94, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 385.53,
                    0.0, 385.53, 0.0, 0.0);
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 3783.06, 0.0, 3783.06, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 817.94,
                    0.0, 817.94, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 4216.94, 0.0, 4216.94, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 403.23,
                    0.0, 403.23, 0.0, 0.0);
        });

    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnProgressiveLoanJob() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(
                    createOnePeriod30DaysPeriodicAccrualProductWithAdvancedPaymentAllocationAndInterestRecalculation(16.0, 4)
                            .maxInterestRatePerPeriod(120.0).maxPrincipal(10000.0));

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 4,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(4)//
                            .loanTermFrequencyType(MONTHS)//
                            .interestRatePerPeriod(BigDecimal.valueOf(120.0)).interestCalculationPeriodType(DAYS)//
                            .transactionProcessingStrategyCode(LoanProductTestBuilder.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            loanDetails.getRepaymentSchedule().getPeriods().forEach(p -> log.info("validateRepaymentPeriod before: {} {} {}", p.getPeriod(),
                    p.getPrincipalOriginalDue(), p.getInterestOriginalDue()));//

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1700.66, 0.0, 0.0, 815.34);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1936.12, 0.0, 0.0, 579.88);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 2071.31, 0.0, 0.0, 444.69);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 2291.91, 0.0, 0.0, 226.05);
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            loanDetails.getRepaymentSchedule().getPeriods().forEach(p -> log.info("validateRepaymentPeriod after: {} {} {}", p.getPeriod(),
                    p.getPrincipalOriginalDue(), p.getInterestOriginalDue()));//

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1700.66, 0.0, 0.0, 815.34);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1936.12, 0.0, 0.0, 579.88);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 2071.31, 0.0, 0.0, 444.69);
            validateRepaymentPeriod(loanDetails, 4, LocalDate.of(2023, 5, 1), 2291.91, 0.0, 0.0, 226.05);
        });

    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnCumulativeLoanJob() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(10.0)
                    .isInterestRecalculationEnabled(true)//
                    .maxPrincipal(10000.0).minNumberOfRepayments(1).rescheduleStrategyMethod(1).recalculationRestFrequencyType(MONTHS)
                    .recalculationRestFrequencyInterval(1).recalculationCompoundingFrequencyType(MONTHS)
                    .recalculationCompoundingFrequencyInterval(30).interestRecalculationCompoundingMethod(1));

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 2,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(2)//
                            .loanTermFrequencyType(MONTHS)//
                            .interestRatePerPeriod(BigDecimal.valueOf(10.0)).interestCalculationPeriodType(DAYS)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 3783.06, 0.0, 3783.06, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 817.94,
                    0.0, 817.94, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 4216.94, 0.0, 4216.94, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 385.53,
                    0.0, 385.53, 0.0, 0.0);
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 3783.06, 0.0, 3783.06, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 817.94,
                    0.0, 817.94, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 4216.94, 0.0, 4216.94, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 403.23,
                    0.0, 403.23, 0.0, 0.0);
        });

    }

}
