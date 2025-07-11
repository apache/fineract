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
package org.apache.fineract.test.stepdef.loan;

import static org.assertj.core.api.Assertions.assertThat;
import static org.awaitility.Awaitility.await;

import io.cucumber.datatable.DataTable;
import io.cucumber.java.en.And;
import io.cucumber.java.en.Then;
import io.cucumber.java.en.When;
import java.io.IOException;
import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessDateResponse;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.InlineJobResponse;
import org.apache.fineract.client.models.IsCatchUpRunningDTO;
import org.apache.fineract.client.models.OldestCOBProcessedLoanDTO;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostClientsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.services.BusinessDateManagementApi;
import org.apache.fineract.client.services.ClientApi;
import org.apache.fineract.client.services.InlineJobApi;
import org.apache.fineract.client.services.LoanCobCatchUpApi;
import org.apache.fineract.client.services.LoansApi;
import org.apache.fineract.test.data.loanproduct.DefaultLoanProduct;
import org.apache.fineract.test.data.loanproduct.LoanProductResolver;
import org.apache.fineract.test.factory.ClientRequestFactory;
import org.apache.fineract.test.factory.LoanRequestFactory;
import org.apache.fineract.test.helper.BusinessDateHelper;
import org.apache.fineract.test.helper.ErrorHelper;
import org.apache.fineract.test.stepdef.AbstractStepDef;
import org.apache.fineract.test.support.TestContextKey;
import retrofit2.Response;

@Slf4j
@RequiredArgsConstructor
public class LoanPerformanceStepDef extends AbstractStepDef {

    private static final String DATE_FORMAT = "dd MMMM yyyy";
    private static final DateTimeFormatter FORMATTER = DateTimeFormatter.ofPattern(DATE_FORMAT);

    private final LoansApi loansApi;
    private final LoanProductResolver loanProductResolver;
    private final LoanRequestFactory loanRequestFactory;
    private final ClientApi clientApi;
    private final ClientRequestFactory clientRequestFactory;
    private final InlineJobApi inlineJobApi;
    private final LoanCobCatchUpApi loanCobCatchUpApi;
    private final BusinessDateManagementApi businessDateApi;

    private final List<Long> performanceTestLoanIds = new ArrayList<>();
    private final Map<String, Long> performanceMetricsMap = new HashMap<>();

    @When("Admin creates performance test data with {int} progressive loans with the following configuration:")
    public void createPerformanceTestLoans(final int numberOfLoans, final DataTable table) throws IOException {
        log.info("Creating {} progressive loans for performance testing", numberOfLoans);

        final List<Map<String, String>> rows = table.asMaps(String.class, String.class);
        final Map<String, String> loanConfig = rows.get(0);

        final String loanProductName = loanConfig.get("loanProductName");
        final String submittedOnDate = loanConfig.get("submittedOnDate");
        final String principal = loanConfig.get("principal");
        final String interestRate = loanConfig.get("interestRate");
        final String repaymentEvery = loanConfig.get("repaymentEvery");
        final String numberOfRepayments = loanConfig.get("numberOfRepayments");

        final DefaultLoanProduct loanProductEnum = DefaultLoanProduct.valueOf(loanProductName);
        final Long loanProductId = loanProductResolver.resolve(loanProductEnum);

        Response<PostClientsResponse> clientResponse = testContext().get(TestContextKey.CLIENT_CREATE_RESPONSE);
        if (clientResponse == null) {
            log.info("Creating a client for performance test loans");
            final PostClientsRequest clientsRequest = clientRequestFactory.defaultClientCreationRequest();
            clientResponse = clientApi.create6(clientsRequest).execute();
            ErrorHelper.checkSuccessfulApiCall(clientResponse);
            testContext().set(TestContextKey.CLIENT_CREATE_RESPONSE, clientResponse);
        }
        final Long clientId = clientResponse.body().getClientId();

        final long startTime = System.currentTimeMillis();

        for (int i = 0; i < numberOfLoans; i++) {
            final PostLoansRequest loanRequest = loanRequestFactory.defaultProgressiveLoansRequest(clientId).productId(loanProductId)
                    .submittedOnDate(submittedOnDate).expectedDisbursementDate(submittedOnDate)
                    .principal(BigDecimal.valueOf(Double.parseDouble(principal)))
                    .interestRatePerPeriod(BigDecimal.valueOf(Double.parseDouble(interestRate)))
                    .numberOfRepayments(Integer.parseInt(numberOfRepayments)).repaymentEvery(Integer.parseInt(repaymentEvery))
                    .repaymentFrequencyType(2).loanTermFrequency(Integer.parseInt(numberOfRepayments)).loanTermFrequencyType(2)
                    .locale("en");

            final Response<PostLoansResponse> submitResponse = loansApi.calculateLoanScheduleOrSubmitLoanApplication(loanRequest, null)
                    .execute();
            ErrorHelper.checkSuccessfulApiCall(submitResponse);

            final Long loanId = submitResponse.body().getLoanId();
            performanceTestLoanIds.add(loanId);

            if ((i + 1) % 100 == 0) {
                log.info("Created {} out of {} loans", i + 1, numberOfLoans);
            }
        }

        final long endTime = System.currentTimeMillis();
        log.info("Successfully created {} progressive loans in {} ms", numberOfLoans, (endTime - startTime));

        if (!performanceTestLoanIds.isEmpty()) {
            final PostLoansResponse firstLoan = new PostLoansResponse();
            firstLoan.setLoanId(performanceTestLoanIds.get(0));
            testContext().set(TestContextKey.LOAN_CREATE_RESPONSE, firstLoan);
        }
    }

    @And("Admin bulk approves all performance test loans on {string} with expected disbursement date on {string}")
    public void bulkApprovePerformanceTestLoans(final String approveDate, final String expectedDisbursementDate) throws IOException {
        log.info("Bulk approving {} performance test loans", performanceTestLoanIds.size());

        final long startTime = System.currentTimeMillis();
        int approvedCount = 0;

        for (final Long loanId : performanceTestLoanIds) {
            final PostLoansLoanIdRequest approveRequest = new PostLoansLoanIdRequest().approvedOnDate(approveDate)
                    .approvedLoanAmount(BigDecimal.valueOf(1000.0)).expectedDisbursementDate(expectedDisbursementDate).locale("en")
                    .dateFormat(DATE_FORMAT);

            final Response<PostLoansLoanIdResponse> approveResponse = loansApi.stateTransitions(loanId, approveRequest, "approve")
                    .execute();
            ErrorHelper.checkSuccessfulApiCall(approveResponse);

            approvedCount++;
            if (approvedCount % 100 == 0) {
                log.info("Approved {} out of {} loans", approvedCount, performanceTestLoanIds.size());
            }
        }

        final long endTime = System.currentTimeMillis();
        log.info("Successfully approved {} loans in {} ms", approvedCount, (endTime - startTime));
    }

    @And("Admin bulk disburses all performance test loans on {string} with full principal amount")
    public void bulkDisbursePerformanceTestLoans(final String disbursementDate) throws IOException {
        log.info("Bulk disbursing {} performance test loans", performanceTestLoanIds.size());

        final long startTime = System.currentTimeMillis();
        int disbursedCount = 0;

        for (final Long loanId : performanceTestLoanIds) {
            final PostLoansLoanIdRequest disburseRequest = new PostLoansLoanIdRequest().actualDisbursementDate(disbursementDate)
                    .transactionAmount(BigDecimal.valueOf(1000.0)).locale("en").dateFormat(DATE_FORMAT);

            final Response<PostLoansLoanIdResponse> disburseResponse = loansApi.stateTransitions(loanId, disburseRequest, "disburse")
                    .execute();
            ErrorHelper.checkSuccessfulApiCall(disburseResponse);

            disbursedCount++;
            if (disbursedCount % 100 == 0) {
                log.info("Disbursed {} out of {} loans", disbursedCount, performanceTestLoanIds.size());
            }
        }

        final long endTime = System.currentTimeMillis();
        log.info("Successfully disbursed {} loans in {} ms", disbursedCount, (endTime - startTime));
    }

    @Then("Admin verifies that all performance test loans have status {string}")
    public void verifyAllPerformanceTestLoansStatus(final String expectedStatus) throws IOException {
        log.info("Verifying status of {} performance test loans", performanceTestLoanIds.size());

        int verifiedCount = 0;
        for (final Long loanId : performanceTestLoanIds) {
            final Response<GetLoansLoanIdResponse> loanResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
            ErrorHelper.checkSuccessfulApiCall(loanResponse);

            final boolean isExpectedStatus;
            switch (expectedStatus.toUpperCase()) {
                case "ACTIVE":
                    isExpectedStatus = loanResponse.body().getStatus().getActive();
                break;
                case "PENDING":
                    isExpectedStatus = loanResponse.body().getStatus().getPendingApproval();
                break;
                case "APPROVED":
                    isExpectedStatus = loanResponse.body().getStatus().getWaitingForDisbursal();
                break;
                case "CLOSED":
                    isExpectedStatus = loanResponse.body().getStatus().getClosedObligationsMet();
                break;
                default:
                    throw new IllegalArgumentException("Unsupported status: " + expectedStatus);
            }

            assertThat(isExpectedStatus).as("Loan %d should have status %s", loanId, expectedStatus).isTrue();

            verifiedCount++;
            if (verifiedCount % 200 == 0) {
                log.info("Verified {} out of {} loans", verifiedCount, performanceTestLoanIds.size());
            }
        }

        log.info("Successfully verified status of {} loans", verifiedCount);
    }

    @Then("Admin checks that all performance test loans have last closed business date as {string}")
    public void verifyAllPerformanceTestLoansLastCOBDate(final String expectedDate) throws IOException {
        log.info("Verifying last closed business date of {} performance test loans", performanceTestLoanIds.size());

        final LocalDate expectedLocalDate = expectedDate.equals("null") ? null : LocalDate.parse(expectedDate, FORMATTER);
        int verifiedCount = 0;

        for (final Long loanId : performanceTestLoanIds) {
            final Response<GetLoansLoanIdResponse> loanResponse = loansApi.retrieveLoan(loanId, false, "", "", "").execute();
            ErrorHelper.checkSuccessfulApiCall(loanResponse);

            final LocalDate actualDate = loanResponse.body().getLastClosedBusinessDate();
            if (expectedDate.equals("null")) {
                assertThat(actualDate).as("Loan %d last closed business date should be null", loanId).isNull();
            } else {
                assertThat(actualDate).as("Loan %d last closed business date should be %s", loanId, expectedDate)
                        .isEqualTo(expectedLocalDate);
            }

            verifiedCount++;
            if (verifiedCount % 200 == 0) {
                log.info("Verified {} out of {} loans", verifiedCount, performanceTestLoanIds.size());
            }
        }

        log.info("Successfully verified last closed business date of {} loans", verifiedCount);
    }

    @When("Admin runs inline COB job for loans")
    public void runInlineCOB() throws IOException {
        for (final Long loanId : performanceTestLoanIds) {
            final InlineJobRequest inlineJobRequest = new InlineJobRequest().addLoanIdsItem(loanId);
            final Response<InlineJobResponse> inlineJobResponse = inlineJobApi.executeInlineJob("LOAN_COB", inlineJobRequest).execute();
            ErrorHelper.checkSuccessfulApiCall(inlineJobResponse);
        }

        log.info("Inlined COB job for {} loans executed", performanceTestLoanIds.size());
    }

    @And("Admin records performance metrics before action as {string}")
    public void recordPerformanceMetricsBefore(String metricsKey) {
        performanceMetricsMap.put(metricsKey, System.currentTimeMillis());
        log.info("Recorded performance metrics before action: {}", metricsKey);
    }

    @And("Admin records performance metrics after action as {string}")
    public void recordPerformanceMetricsAfter(final String metricsKey) {
        final Long metricsEnd = System.currentTimeMillis();

        final Long metricsStart = performanceMetricsMap.get(metricsKey);
        if (metricsStart != null) {
            final long duration = metricsEnd - metricsStart;
            log.info("Performance evaluation {}:", metricsKey);
            log.info("- Duration: {} ms ({} seconds)", duration, duration / 1000);
        }
    }

    @And("Admin is waiting for COB catch-up job to finish")
    public void waitingForCobCatchUpJobToFinish() {
        log.info("Waiting for COB catch-up job to finish...");
        await().atMost(Duration.ofMinutes(2000)).pollInterval(Duration.ofSeconds(5)).pollDelay(Duration.ofSeconds(5)).until(() -> {
            final Response<IsCatchUpRunningDTO> statusResponse = loanCobCatchUpApi.isCatchUpRunning().execute();
            ErrorHelper.checkSuccessfulApiCall(statusResponse);
            if (statusResponse.body() != null && Boolean.FALSE.equals(statusResponse.body().getCatchUpRunning())) {
                final Response<BusinessDateResponse> businessDateResponse = businessDateApi.getBusinessDate(BusinessDateHelper.COB)
                        .execute();
                ErrorHelper.checkSuccessfulApiCall(businessDateResponse);
                final LocalDate currentBusinessDate = businessDateResponse.body().getDate();
                final Response<OldestCOBProcessedLoanDTO> catchUpResponse = loanCobCatchUpApi.getOldestCOBProcessedLoan().execute();
                ErrorHelper.checkSuccessfulApiCall(catchUpResponse);
                final LocalDate lastClosedDate = catchUpResponse.body().getCobBusinessDate();
                return !lastClosedDate.isBefore(currentBusinessDate);
            }
            return false;
        });
        log.info("COB catch-up job finished");
    }

    @And("Admin evaluates transaction count for all performance test loans as {string}")
    public void evaluateTransactionCount(final String metricsKey) throws IOException {
        log.info("Evaluating transaction count for {} performance test loans", performanceTestLoanIds.size());

        long totalTransactions = 0;
        long totalAccrualTransactions = 0;
        long totalExecutionTimeMs = 0;

        for (int i = 0; i < performanceTestLoanIds.size(); i++) {
            final Long loanId = performanceTestLoanIds.get(i);

            final long startTime = System.currentTimeMillis();
            final Response<GetLoansLoanIdResponse> loanResponse = loansApi.retrieveLoan(loanId, false, "transactions", "", "").execute();
            final long executionTime = System.currentTimeMillis() - startTime;
            totalExecutionTimeMs += executionTime;

            ErrorHelper.checkSuccessfulApiCall(loanResponse);

            final List<GetLoansLoanIdTransactions> transactions = loanResponse.body().getTransactions();
            if (transactions != null) {
                totalTransactions += transactions.size();

                final long accrualCount = transactions.stream()
                        .filter(t -> t.getType() != null && t.getType().getAccrual() != null && t.getType().getAccrual()).count();
                totalAccrualTransactions += accrualCount;
            }

            if ((i + 1) % 100 == 0) {
                log.info("Evaluated transactions for {} out of {} loans", i + 1, performanceTestLoanIds.size());
            }
        }

        final double avgTransactionsPerLoan = (double) totalTransactions / performanceTestLoanIds.size();
        final double avgAccrualTransactionsPerLoan = (double) totalAccrualTransactions / performanceTestLoanIds.size();
        final double avgExecutionTimeMs = (double) totalExecutionTimeMs / performanceTestLoanIds.size();

        log.info("Transaction count evaluation for {}:", metricsKey);
        log.info("- Total loans: {}", performanceTestLoanIds.size());
        log.info("- Total transactions: {}", totalTransactions);
        log.info("- Total accrual transactions: {}", totalAccrualTransactions);
        log.info("- Average transactions per loan: {}", avgTransactionsPerLoan);
        log.info("- Average accrual transactions per loan: {}", avgAccrualTransactionsPerLoan);
        log.info("- Average retrieveLoan API execution time: {} ms", avgExecutionTimeMs);
    }

}
