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

import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD;
import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.RepaymentFrequencyType.DAYS;
import static org.apache.fineract.integrationtests.BaseLoanIntegrationTest.RepaymentFrequencyType.MONTHS;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostChargesResponse;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.SchedulerJobHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.Assertions;
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
        requestSpec.header("Fineract-Platform-TenantId", Utils.DEFAULT_TENANT);
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

    private void logLoanDetails(GetLoansLoanIdResponse loanDetails) {
        log.info("index, dueDate, principal, fee, penalty, interest");
        Assertions.assertNotNull(loanDetails.getRepaymentSchedule());
        Assertions.assertNotNull(loanDetails.getRepaymentSchedule().getPeriods());
        loanDetails.getRepaymentSchedule().getPeriods()
                .forEach(period -> log.info("{}, \"{}\", {}, {}, {}, {}", period.getPeriod(),
                        DateTimeFormatter.ofPattern(DATETIME_PATTERN, Locale.ENGLISH).format(Objects.requireNonNull(period.getDueDate())),
                        period.getPrincipalDue(), period.getFeeChargesDue(), period.getPenaltyChargesDue(), period.getInterestDue()));
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStepDaily() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });

        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.05, 0.0, 0.0, 50.79);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2025.55, 0.0, 0.0, 16.88);
        });
        runAt("20 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 16.97);

        });
        runAt("1 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

        });
        runAt("20 April 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2074.49, 0.0, 0.0, 48.56);

        });
        payoffOnDateAndVerifyStatus("1 February 2023", loanIdRef.get());
    }

    private void payoffOnDateAndVerifyStatus(final String date, final Long loanId) {
        runAt(date, () -> {
            HashMap prepayAmount = loanTransactionHelper.getPrepayAmount(requestSpec, responseSpec, loanId.intValue());
            Assertions.assertNotNull(prepayAmount);
            Float amount = (Float) prepayAmount.get("amount");
            PostLoansLoanIdTransactionsResponse response = loanTransactionHelper.makeLoanRepayment(date, amount, loanId.intValue());
            Assertions.assertNotNull(response);
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            Assertions.assertNotNull(loanDetails);
            Assertions.assertNotNull(loanDetails.getStatus());
            log.info("Loan status {}", loanDetails.getStatus().getId());
            Assertions.assertTrue(Stream.of(600).anyMatch(v -> loanDetails.getStatus().getId().intValue() == v));
        });
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStepLatePayPayOnDuePayLatePayOnDateDaily() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });
        runAt("20 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 16.97);

        });
        runAt("1 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

            loanTransactionHelper.makeLoanRepayment("20 February 2023", 2041.84f, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("01 March 2023", 2041.84f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 16.97);
        });
        runAt("10 April 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 21.99);

            loanTransactionHelper.makeLoanRepayment("10 April 2023", 2041.84f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyPaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7, 2041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 21.99);
        });
        runAt("20 April 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1980.46, 0.0, 0.0, 61.38);
            validateFullyPaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.14, 0.0, 0.0, 33.7, 2041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2036.23, 0.0, 0.0, 21.99);
        });
        payoffOnDateAndVerifyStatus("20 April 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStepLatePartialRepaymentDailyInterestCalculation() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(create4IProgressive().recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });
        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            loanTransactionHelper.makeLoanRepayment("01 February 2023", 2041.84f, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });
        runAt("10 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2003.41, 0.0, 0.0, 38.43);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2029.79, 0.0, 0.0, 16.91);

            loanTransactionHelper.makeLoanRepayment("10 March 2023", 500.00f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1991.63, 500.0, 1491.63, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 50.21,
                    0, 50.21, 0, 500.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2003.41, 0.0, 0.0, 38.43);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2029.79, 0.0, 0.0, 16.91);

            loanTransactionHelper.makeLoanRepayment("10 March 2023", 541.84f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1991.63, 1041.84, 949.79, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 50.21,
                    0, 50.21, 0, 1041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2003.41, 0.0, 0.0, 38.43);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2029.79, 0.0, 0.0, 16.91);
        });
        runAt("20 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2023, 3, 1), 1991.63, 1041.84, 949.79, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 50.21,
                    0, 50.21, 0, 1041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2000.85, 0.0, 0.0, 40.99);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2032.35, 0.0, 0.0, 16.94);

            loanTransactionHelper.makeLoanRepayment("20 March 2023", 1000f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21, 2041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2000.85, 0.0, 0.0, 40.99);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2032.35, 0.0, 0.0, 16.94);
        });
        payoffOnDateAndVerifyStatus("1 April 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStep() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive());

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });

        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("20 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2074.49, 0.0, 0.0, 17.29);
        });
        payoffOnDateAndVerifyStatus("1 February 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStepLatePaidPaidOnTimeLatePaidPayoffOnTime() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive());

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });

        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("15 February 2023", () -> {
            Long loanId = loanIdRef.get();
            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

            loanTransactionHelper.makeLoanRepayment("15 February 2023", 500.0F, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1975.17, 500, 1475.17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 66.67, 0,
                    66.67, 0, 500);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

            loanTransactionHelper.makeLoanRepayment("15 February 2023", 500f, loanId.intValue());

            loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1975.17, 1000, 975.17, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 66.67, 0,
                    66.67, 0, 1000);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

        });
        runAt("20 February 2023", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("20 February 2023", 1041.84f, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);

        });
        runAt("1 March 2023", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("01 March 2023", 2041.84f, loanId.intValue());

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("2 April 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1975.17, 1975.17, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 66.67,
                    66.67, 0, 0, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 33.75);
        });
        runAt("1 May 2023", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("15 April 2023", 2041.84f, loanId.intValue());
            loanTransactionHelper.makeLoanRepayment("01 May 2023", 2075.32f, loanId.intValue());

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2023, 2, 1), 1975.17, 1975.17, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 66.67,
                    66.67, 0, 0, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateRepaymentPeriod(loanDetails, 3, LocalDate.of(2023, 4, 1), 2008.09, 2008.09, 0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 33.75,
                    33.75, 0, 0, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 33.75);
        });
    }

    @Test
    public void verifyEarlyLateRepaymentOnProgressiveLoanNextInstallmentAllocationRepayLessThenEmi() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive() //
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.52, 0.0, 0.0, 0.49);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.90, 0.0, 0.0, 0.10);

        });
        runAt("15 February 2024", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            loanTransactionHelper.makeLoanRepayment("15 February 2024", 15.0F, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 16.43, 15.0, 1.43, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.58, 0,
                    0.58, 0.0, 15.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.48, 0.0, 0.0, 0.53);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.94, 0.0, 0.0, 0.10);
        });
    }

    @Test
    public void verifyChargeCreationAfterMaturityDateOnInterestBearingProgressiveLoan() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive() //
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                    .currencyCode("USD"));

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.52, 0.0, 0.0, 0.49);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.90, 0.0, 0.0, 0.10);

        });
        runAt("10 July 2024", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            // create charge
            PostChargesResponse chargeResult = createCharge(10.0);
            Assertions.assertNotNull(chargeResult);
            Long chargeId = chargeResult.getResourceId();
            Assertions.assertNotNull(chargeId);

            // add charge after maturity
            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "15 July 2024", 10.0);
            Assertions.assertNotNull(loanChargeResult.getResourceId());

            // verify N+1 installment in schedule
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 17.85, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 7, "15 July 2024", 0.0, 10.0, 0.0, 0.0);

            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(300, loanDetails.getStatus().getId());
        });
        runAt("15 July 2024", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("15 July 2024", 113.48F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 17.85, 0.0, 0.0, 0.58, 18.43);
            validateFullyPaidRepaymentPeriod(loanDetails, 7, "15 July 2024", 0.0, 10.0, 0.0, 0.0);

            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
        runAt("16 July 2024", () -> {
            Long loanId = loanIdRef.get();

            // create charge
            PostChargesResponse chargeResult = createCharge(10.0);
            Assertions.assertNotNull(chargeResult);
            Long chargeId = chargeResult.getResourceId();
            Assertions.assertNotNull(chargeId);

            // add charge after maturity
            PostLoansLoanIdChargesResponse loanChargeResult = addLoanCharge(loanId, chargeId, "20 July 2024", 15.0);
            Assertions.assertNotNull(loanChargeResult.getResourceId());

            // verify N+1 installment in schedule
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 17.85, 0.0, 0.0, 0.58, 18.43);
            validateRepaymentPeriod(loanDetails, 7, LocalDate.of(2024, 7, 20), 0.0, 0.0, 0.0, 25.0, 10.0, 15.0, 0.0, 0.0, 0.0, 0.0, 0.0,
                    0.0, 0.0, 0.0);

            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(300, loanDetails.getStatus().getId());
        });
        runAt("20 July 2024", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("20 July 2024", 15.0F, loanId.intValue());
            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyPaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 17.85, 0.0, 0.0, 0.58, 18.43);
            validateFullyPaidRepaymentPeriod(loanDetails, 7, "20 July 2024", 0.0, 25.0, 0.0, 0.0);

            Assertions.assertNotNull(loanDetails.getStatus());
            Assertions.assertEquals(600, loanDetails.getStatus().getId());
        });
    }

    @Test
    public void verifyEarlyLateRepaymentOnProgressiveLoanNextInstallmentAllocationRepayEmi() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive() //
                    .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.52, 0.0, 0.0, 0.49);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.90, 0.0, 0.0, 0.10);

        });
        runAt("15 February 2024", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            loanTransactionHelper.makeLoanRepayment("15 February 2024", 17.01F, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58, 17.01);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.48, 0.0, 0.0, 0.53);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.94, 0.0, 0.0, 0.10);
        });
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanCOBStepOnePaid() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive());

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });

        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            loanTransactionHelper.makeLoanRepayment("01 February 2023", 2041.84f, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);
        });
        runAt("1 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("20 March 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        payoffOnDateAndVerifyStatus("1 March 2023", loanIdRef.get());
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
        payoffOnDateAndVerifyStatus("1 March 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnProgressiveLoanJob() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive());

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });

        runAt("1 February 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);
        });
        runAt("2 February 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("20 February 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        runAt("2 March 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2074.49, 0.0, 0.0, 17.29);
        });
        payoffOnDateAndVerifyStatus("1 February 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnCumulativeLoanJob() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(10.0).isInterestRecalculationEnabled(true)//
                            .maxPrincipal(10000.0) //
                            .minNumberOfRepayments(1) //
                            .rescheduleStrategyMethod(1) //
                            .recalculationRestFrequencyType(MONTHS) //
                            .recalculationRestFrequencyInterval(1) //
                            .recalculationCompoundingFrequencyType(MONTHS) //
                            .recalculationCompoundingFrequencyInterval(30) //
                            .interestRecalculationCompoundingMethod(1)); //

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
        payoffOnDateAndVerifyStatus("1 March 2023", loanIdRef.get());

    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOn4IProgressiveLoanMultiOverdueSingleRepayment() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper.createLoanProduct(create4IProgressive());

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 10.0,
                    4, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1991.63, 0.0, 0.0, 50.21);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.23, 0.0, 0.0, 33.61);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2024.97, 0.0, 0.0, 16.87);

        });
        runAt("1 March 2023", () -> {
            Long loanId = loanIdRef.get();

            loanTransactionHelper.makeLoanRepayment("01 March 2023", 4083.68f, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyPaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1975.17, 0.0, 0.0, 66.67, 2041.84);
            validateFullyPaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1975.17, 0.0, 0.0, 66.67);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2008.09, 0.0, 0.0, 33.75);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2041.57, 0.0, 0.0, 17.01);
        });
        payoffOnDateAndVerifyStatus("1 April 2023", loanIdRef.get());

    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnCumulativeLoanJobSameAsRepaymentPeriod() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(10.0).isInterestRecalculationEnabled(true)//
                            .maxPrincipal(10000.0) //
                            .minNumberOfRepayments(1) //
                            .maxNumberOfRepayments(10) //
                            .rescheduleStrategyMethod(1) //
                            .daysInYearType(360) //
                            .daysInMonthType(30) //
                            .recalculationRestFrequencyType(SAME_AS_REPAYMENT_PERIOD) //
                            .recalculationRestFrequencyInterval(1) //
                            .recalculationCompoundingFrequencyType(1) //
                            .recalculationCompoundingFrequencyInterval(1) //
                            .interestRecalculationCompoundingMethod(0)); //

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 2,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(4)//
                            .loanTermFrequencyType(MONTHS)//
                            .numberOfRepayments(4).interestRatePerPeriod(BigDecimal.valueOf(10.0)).interestCalculationPeriodType(DAYS)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1896.4, 0.0, 0.0, 627.6);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2086.04, 0.0, 0.0, 437.96);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2293.56, 0.0, 0.0, 229.36);
        });
        runAt("8 February 2023", () -> {
            Long loanId = loanIdRef.get();

            schedulerJobHelper.executeAndAwaitJob("Update Loan Arrears Ageing");
            schedulerJobHelper.executeAndAwaitJob("Recalculate Interest For Loans");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2068.8, 0.0, 0.0, 455.2);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2483.2, 0.0, 0.0, 248.32);
        });
        payoffOnDateAndVerifyStatus("1 May 2023", loanIdRef.get());
    }

    @Test
    public void verifyLoanInstallmentRecalculatedIfThereIsOverdueInstallmentOnCumulativeLoanCOBSameAsRepaymentPeriod() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2023", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper
                    .createLoanProduct(createOnePeriod30DaysPeriodicAccrualProduct(10.0).isInterestRecalculationEnabled(true)//
                            .maxPrincipal(10000.0) //
                            .minNumberOfRepayments(1) //
                            .maxNumberOfRepayments(10) //
                            .rescheduleStrategyMethod(1) //
                            .daysInYearType(360) //
                            .daysInMonthType(30) //
                            .recalculationRestFrequencyType(SAME_AS_REPAYMENT_PERIOD) //
                            .recalculationRestFrequencyInterval(1) //
                            .recalculationCompoundingFrequencyType(1) //
                            .recalculationCompoundingFrequencyInterval(1) //
                            .interestRecalculationCompoundingMethod(0)); //

            Long loanId = applyAndApproveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2023", 8000.0, 2,
                    postLoansRequest -> postLoansRequest.loanTermFrequency(4)//
                            .loanTermFrequencyType(MONTHS)//
                            .numberOfRepayments(4).interestRatePerPeriod(BigDecimal.valueOf(10.0)).interestCalculationPeriodType(DAYS)//
                            .repaymentEvery(1)//
                            .repaymentFrequencyType(MONTHS)//
                            .principal(BigDecimal.valueOf(8000.0)).maxOutstandingLoanBalance(BigDecimal.valueOf(100000.0)));
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(8000), "1 January 2023");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1896.4, 0.0, 0.0, 627.6);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2086.04, 0.0, 0.0, 437.96);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2293.56, 0.0, 0.0, 229.36);
        });
        runAt("8 February 2023", () -> {
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2023", 1724.0, 0.0, 0.0, 800.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2023", 2068.8, 0.0, 0.0, 455.2);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2023", 2483.2, 0.0, 0.0, 248.32);
        });
        payoffOnDateAndVerifyStatus("1 May 2023", loanIdRef.get());
    }

    @Test
    public void verifyEarlyLateRepaymentOnProgressiveLoanThePastDueHasNoEffect() {
        AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            PostLoanProductsResponse loanProduct = loanProductHelper //
                    .createLoanProduct(create4IProgressive() //
                            .recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY) //
                            .disallowInterestCalculationOnPastDue(true) //
            );

            Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), "1 January 2024", 100.0, 7.0, 6,
                    null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateFullyUnpaidRepaymentPeriod(loanDetails, 1, "01 February 2024", 16.43, 0.0, 0.0, 0.58);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.52, 0.0, 0.0, 0.49);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.90, 0.0, 0.0, 0.10);

        });
        runAt("15 February 2024", () -> { // we have past due and should not count extra interest
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
            loanTransactionHelper.makeLoanRepayment("15 February 2024", 15.0F, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 16.43, 15.0, 1.43, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.58, 0,
                    0.58, 0.0, 15.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 2, "01 March 2024", 16.52, 0.0, 0.0, 0.49);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.62, 0.0, 0.0, 0.39);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.90, 0.0, 0.0, 0.10);
        });
        runAt("15 February 2024", () -> { // we turn from past due into early repayment and should have less interest
            Long loanId = loanIdRef.get();

            inlineLoanCOBHelper.executeInlineCOB(List.of(loanId));
            loanTransactionHelper.makeLoanRepayment("15 February 2024", 19.02F, loanId.intValue());

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanId);
            logLoanDetails(loanDetails);

            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 1), 16.43, 16.43, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.58, 0.58,
                    0.0, 0.0, 17.01);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 16.77, 16.77, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.24, 0.24,
                    0.0, 17.01, 0.0);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 3, "01 April 2024", 16.42, 0.0, 0.0, 0.59);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 4, "01 May 2024", 16.72, 0.0, 0.0, 0.29);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 5, "01 June 2024", 16.81, 0.0, 0.0, 0.20);
            validateFullyUnpaidRepaymentPeriod(loanDetails, 6, "01 July 2024", 16.85, 0.0, 0.0, 0.10);
        });
    }

}
