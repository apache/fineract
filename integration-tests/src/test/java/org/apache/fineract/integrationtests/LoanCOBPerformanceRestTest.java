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

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.integrationtests.common.BusinessStepHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.inlinecob.InlineLoanCOBHelper;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.TestInfo;
import org.junit.jupiter.api.TestInstance;
import org.junit.jupiter.api.TestInstance.Lifecycle;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/***
 * This test class is designed to measure the performance of the Loan Close of Business (COB) process in Fineract. It
 * creates a specified number of loans, runs the COB process, and measures the time taken for each step. The results are
 * printed in a consolidated report at the end of all tests.
 ***/
@Disabled("This test is disabled by default. To run it, please remove the @Disabled annotation.")
@TestInstance(Lifecycle.PER_CLASS)
public class LoanCOBPerformanceRestTest extends BaseLoanIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(LoanCOBPerformanceRestTest.class);

    private static ResponseSpecification responseSpec;
    private static RequestSpecification requestSpec;
    private static PostClientsResponse client;
    private static InlineLoanCOBHelper inlineLoanCOBHelper;
    private static BusinessStepHelper businessStepHelper;
    private Random random = new Random();

    // Store metrics for all test runs
    private Map<String, Map<String, Object>> allTestMetrics = new LinkedHashMap<>();

    @BeforeAll
    public static void setup() {
        Utils.initializeRESTAssured();
        requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        requestSpec.header("Fineract-Platform-TenantId", Utils.DEFAULT_TENANT);
        responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        ClientHelper clientHelper = new ClientHelper(requestSpec, responseSpec);
        client = clientHelper.createClient(ClientHelper.defaultClientCreationRequest());
        inlineLoanCOBHelper = new InlineLoanCOBHelper(requestSpec, responseSpec);
        businessStepHelper = new BusinessStepHelper();
        // setup COB Business Steps to prevent test failing due other integration test configurations
        businessStepHelper.updateSteps("LOAN_CLOSE_OF_BUSINESS", "APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION",
                "CHECK_LOAN_REPAYMENT_DUE", "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER", "CHECK_DUE_INSTALLMENTS", "ACCRUAL_ACTIVITY_POSTING", "LOAN_INTEREST_RECALCULATION");
    }

    @AfterAll
    public void printConsolidatedReport() {
        LOG.info("\n\n");
        LOG.info("========================================================================");
        LOG.info("                     CONSOLIDATED PERFORMANCE REPORT                     ");
        LOG.info("========================================================================");

        // Table header
        LOG.info(String.format("%-25s | %-20s | %-20s | %-20s", "Test Configuration", "Loan Creation Time", "First COB Run Time",
                "Second COB Run Time"));
        LOG.info("-------------------------------------------------------------------------");

        // Table rows
        for (Map.Entry<String, Map<String, Object>> entry : allTestMetrics.entrySet()) {
            String testName = entry.getKey();
            Map<String, Object> metrics = entry.getValue();

            int loanCount = (int) metrics.get("loanCount");
            long createTime = (long) metrics.get("loanCreationTimeMs");
            long firstCobTime = (long) metrics.get("firstCOBTimeMs");
            long secondCobTime = (long) metrics.get("secondCOBTimeMs");

            LOG.info(String.format("%-25s | %-20s | %-20s | %-20s", testName + " (" + loanCount + " loans)",
                    createTime + " ms (" + (createTime / loanCount) + " ms/loan)",
                    firstCobTime + " ms (" + (firstCobTime / loanCount) + " ms/loan)",
                    secondCobTime + " ms (" + (secondCobTime / loanCount) + " ms/loan)"));
        }

        LOG.info("========================================================================");

        // Add scaled performance analysis if there are multiple tests
        if (allTestMetrics.size() > 1) {
            LOG.info("\n");
            LOG.info("SCALING ANALYSIS");
            LOG.info("========================================================================");
            LOG.info("This analysis shows how performance scales with increasing loan counts");

            // Find the smallest and largest loan count tests
            int minLoans = Integer.MAX_VALUE;
            int maxLoans = 0;
            String minTestName = "";
            String maxTestName = "";

            for (Map.Entry<String, Map<String, Object>> entry : allTestMetrics.entrySet()) {
                int loanCount = (int) entry.getValue().get("loanCount");
                if (loanCount < minLoans) {
                    minLoans = loanCount;
                    minTestName = entry.getKey();
                }
                if (loanCount > maxLoans) {
                    maxLoans = loanCount;
                    maxTestName = entry.getKey();
                }
            }

            if (!minTestName.equals(maxTestName)) {
                Map<String, Object> minMetrics = allTestMetrics.get(minTestName);
                Map<String, Object> maxMetrics = allTestMetrics.get(maxTestName);

                double loanCountRatio = (double) maxLoans / minLoans;

                double createTimeRatio = (double) ((long) maxMetrics.get("loanCreationTimeMs"))
                        / ((long) minMetrics.get("loanCreationTimeMs"));

                double firstCOBRatio = (double) ((long) maxMetrics.get("firstCOBTimeMs")) / ((long) minMetrics.get("firstCOBTimeMs"));

                double secondCOBRatio = (double) ((long) maxMetrics.get("secondCOBTimeMs")) / ((long) minMetrics.get("secondCOBTimeMs"));

                LOG.info(String.format("Loan count increased by a factor of %.2f (from %d to %d)", loanCountRatio, minLoans, maxLoans));
                LOG.info(String.format("Loan creation time increased by a factor of %.2f (scaling efficiency: %.2f%%)", createTimeRatio,
                        (loanCountRatio / createTimeRatio) * 100));
                LOG.info(String.format("First COB run time increased by a factor of %.2f (scaling efficiency: %.2f%%)", firstCOBRatio,
                        (loanCountRatio / firstCOBRatio) * 100));
                LOG.info(String.format("Second COB run time increased by a factor of %.2f (scaling efficiency: %.2f%%)", secondCOBRatio,
                        (loanCountRatio / secondCOBRatio) * 100));
                LOG.info("------------------------------------------------------------------------");
                LOG.info("Note: Scaling efficiency > 100% indicates better than linear scaling");
                LOG.info("      Scaling efficiency < 100% indicates worse than linear scaling");
            }

            LOG.info("========================================================================");
        }
    }

    private Long createLoanProduct(String disbursementDate, Double amount, Double interestRate, Integer numberOfInstallments) {
        PostLoanProductsResponse loanProduct = loanProductHelper
                .createLoanProduct(create4IProgressive().recalculationRestFrequencyType(RecalculationRestFrequencyType.DAILY));

        Long loanId = applyAndApproveProgressiveLoan(client.getClientId(), loanProduct.getResourceId(), disbursementDate, amount,
                interestRate, numberOfInstallments, null);
        disburseLoan(loanId, BigDecimal.valueOf(amount), disbursementDate);
        return loanId;
    }

    public List<Long> createLoans(int numberOfLoans, String disbursementDate, Double amount, Double interestRate,
            Integer numberOfInstallments) {
        LOG.info("Creating {} loans...", numberOfLoans);
        long startTime = System.nanoTime();

        List<Long> loanIds = new ArrayList<>();
        for (int i = 0; i < numberOfLoans; i++) {
            Long loanId = createLoanProduct(disbursementDate, amount != null ? amount : getRandomAmount(),
                    interestRate != null ? interestRate : getRandomInterestRate(),
                    numberOfInstallments != null ? numberOfInstallments : getRandomNumberOfInstallments());
            loanIds.add(loanId);

            if ((i + 1) % 10 == 0) {
                LOG.info("Created {} loans so far...", (i + 1));
            }
        }

        long endTime = System.nanoTime();
        long durationMs = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
        LOG.info("Loan creation completed in {} ms ({} ms per loan)", durationMs, durationMs / numberOfLoans);

        return loanIds;
    }

    // random number from 3,4,6,12
    private Integer getRandomNumberOfInstallments() {
        int[] possibleValues = { 3, 4, 6, 12 };
        return possibleValues[random.nextInt(possibleValues.length)];
    }

    private Double getRandomAmount() {
        return random.nextInt(1, 100) * 100.0;
    }

    private Double getRandomInterestRate() {
        return (double) random.nextInt(1, 15);
    }

    @ParameterizedTest
    @ValueSource(ints = { 10, 50, 100 })
    public void testLoanCOBPerformanceWithDifferentLoansCount(int loanCount, TestInfo testInfo) {
        String testName = testInfo.getDisplayName();
        LOG.info("Starting test: {} with {} loans", testName, loanCount);

        AtomicReference<List<Long>> loanIds = new AtomicReference<>(new ArrayList<>());
        final Map<String, Object> metrics = new HashMap<>();
        metrics.put("loanCount", loanCount);

        // Create loans
        runAt("1 January 2023", () -> {
            long startTime = System.nanoTime();
            loanIds.set(createLoans(loanCount, "1 January 2023", null, null, null));
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            metrics.put("loanCreationTimeMs", duration);
            LOG.info("Created {} loans in {} ms", loanCount, duration);
        });

        // First COB run - January to February
        runAt("1 February 2023", () -> {
            LOG.info("Running first COB for {} loans...", loanCount);
            long startTime = System.nanoTime();
            inlineLoanCOBHelper.executeInlineCOB(loanIds.get());
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            metrics.put("firstCOBTimeMs", duration);
            LOG.info("First COB completed in {} ms ({} ms per loan)", duration, duration / loanCount);
        });

        // Second COB run - February to March
        runAt("1 March 2023", () -> {
            LOG.info("Running second COB for {} loans...", loanCount);
            long startTime = System.nanoTime();
            inlineLoanCOBHelper.executeInlineCOB(loanIds.get());
            long endTime = System.nanoTime();
            long duration = TimeUnit.NANOSECONDS.toMillis(endTime - startTime);
            metrics.put("secondCOBTimeMs", duration);
            LOG.info("Second COB completed in {} ms ({} ms per loan)", duration, duration / loanCount);
        });

        // Add metrics to the consolidated collection
        allTestMetrics.put(testName, metrics);

        // Print individual test summary
        LOG.info("Individual test complete. Summary for {} loans:", loanCount);
        LOG.info("----------------------------------------------------");
        LOG.info("Loan Creation Time: {} ms ({} ms/loan)", metrics.get("loanCreationTimeMs"),
                ((Long) metrics.get("loanCreationTimeMs")) / loanCount);
        LOG.info("First COB Run Time: {} ms ({} ms/loan)", metrics.get("firstCOBTimeMs"),
                ((Long) metrics.get("firstCOBTimeMs")) / loanCount);
        LOG.info("Second COB Run Time: {} ms ({} ms/loan)", metrics.get("secondCOBTimeMs"),
                ((Long) metrics.get("secondCOBTimeMs")) / loanCount);
        LOG.info("----------------------------------------------------\n");

        LOG.info("Full consolidated report will be printed after all tests complete.");
    }
}
