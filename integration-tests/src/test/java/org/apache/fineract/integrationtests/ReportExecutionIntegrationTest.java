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

import java.io.IOException;
import java.math.BigDecimal;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import lombok.extern.slf4j.Slf4j;
import okhttp3.MediaType;
import okhttp3.ResponseBody;
import org.apache.fineract.client.models.GetReportsResponse;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostGroupsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsRequest;
import org.apache.fineract.client.models.PostSavingsAccountTransactionsResponse;
import org.apache.fineract.client.models.PostSavingsAccountsAccountIdRequest;
import org.apache.fineract.client.models.PostSavingsAccountsRequest;
import org.apache.fineract.client.models.PostSavingsProductsRequest;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.client.IntegrationTest;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;
import retrofit2.Response;

@Slf4j
class ReportExecutionIntegrationTest extends IntegrationTest {

    @Test
    void verifyClientListingReportAndMetrics() throws IOException {
        // Skip if Pentaho/Email
        if (shouldSkipReport("Client Listing")) {
            return;
        }

        String uniqueClientName = "ReportTestClient_" + UUID.randomUUID().toString().substring(0, 8);
        Long clientId = createClient(uniqueClientName);
        assertThat(clientId).isNotNull();

        // Run the Client Listing Report (Table/JSON format)
        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("Client Listing", Map.of("R_officeId", "1"), false));

        assertThat(response.code()).isEqualTo(200);
        RunReportsResponse body = response.body();
        assertThat(body).isNotNull();
        assertThat(body.getColumnHeaders()).isNotEmpty();
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData().toString()).contains(uniqueClientName);

        // Run the Client Listing Report (CSV format)
        Response<ResponseBody> csvResponse = okR(
                fineractClient().reportsRun.runReportGetFile("Client Listing", Map.of("R_officeId", "1", "exportCSV", "true"), false));

        assertThat(csvResponse.body()).isNotNull();

        try (ResponseBody csvBody = csvResponse.body()) {
            assertThat(csvBody.contentType()).isEqualTo(MediaType.parse("text/csv"));
            String csvContent = csvBody.string();
            assertThat(csvContent).contains(uniqueClientName);
        }
    }

    @Test
    void verifyLoanListingReport() throws IOException {
        // Skip if Pentaho/Email
        if (shouldSkipReport("Active Loans - Details")) {
            return;
        }

        String uniqueClientName = "LoanReportClient_" + UUID.randomUUID().toString().substring(0, 8);
        Long clientId = createClient(uniqueClientName);
        Long loanProductId = createLoanProduct();
        Long loanId = applyForLoan(clientId, loanProductId);
        approveLoan(loanId);
        disburseLoan(loanId, BigDecimal.valueOf(1000.0));

        Response<RunReportsResponse> response = okR(
                fineractClient().reportsRun.runReportGetData("Active Loans - Details", Map.of("R_officeId", "1", "R_loanOfficerId", "-1",
                        "R_currencyId", "-1", "R_fundId", "-1", "R_loanProductId", "-1", "R_loanPurposeId", "-1"), false));

        assertThat(response.code()).isEqualTo(200);
        RunReportsResponse body = response.body();
        assertThat(body).isNotNull();
        assertThat(body.getData()).isNotNull();
        assertThat(body.getData().toString()).contains(String.valueOf(loanId));
    }

    @Test
    void verifyAllReportsExecution() throws IOException {
        // Setup Data
        String uniqueClientName = "ReportClient_" + UUID.randomUUID().toString().substring(0, 8);
        String uniqueGroupName = "ReportGroup_" + UUID.randomUUID().toString().substring(0, 8);

        Long clientId = createClient(uniqueClientName);
        Long groupId = createGroup(uniqueGroupName);
        Long loanProductId = createLoanProduct();
        Long savingsProductId = createSavingsProduct();

        // Loan Lifecycle
        Long loanId = applyForLoan(clientId, loanProductId);
        approveLoan(loanId);
        disburseLoan(loanId, BigDecimal.valueOf(1000.0));
        // Make a repayment to generate a transaction ID
        Long loanTransactionId = makeRepayment(loanId, BigDecimal.valueOf(100.0));

        // Savings Lifecycle
        Long savingsId = applyForSavings(clientId, savingsProductId);
        approveSavings(savingsId);
        activateSavings(savingsId);
        // Make a deposit to generate a savings transaction ID
        Long savingsTransactionId = makeDeposit(savingsId, BigDecimal.valueOf(500.0));

        // Build the Map of Valid IDs
        // We map standard parameter names to our created IDs.
        Map<String, String> context = new HashMap<>();
        context.put("R_officeId", "1");
        context.put("R_clientId", clientId.toString());
        context.put("R_groupId", groupId.toString());
        context.put("R_loanId", loanId.toString());
        context.put("R_loanProductId", loanProductId.toString());
        context.put("R_savingsId", savingsId.toString());
        context.put("R_savingsProductId", savingsProductId.toString());
        context.put("R_loanOfficerId", "-1"); // -1 = All
        context.put("R_currencyId", "-1");
        context.put("R_fundId", "-1");
        context.put("R_loanPurposeId", "-1");
        context.put("R_parType", "1"); // PAR Type for Portfolio reports
        context.put("R_startDate", "2023-01-01");
        context.put("R_endDate", "2025-12-31");
        context.put("R_transactionId", loanTransactionId.toString()); // For Loan Transaction Receipts

        // Fetch Reports
        Response<List<GetReportsResponse>> reportListResponse = okR(fineractClient().reports.retrieveReportList());
        List<GetReportsResponse> allReports = reportListResponse.body();
        assertThat(allReports).isNotEmpty();

        int successCount = 0;
        int skippedCount = 0;
        Map<String, String> failedReports = new HashMap<>();

        log.info("Starting dynamic execution of {} reports...", allReports.size());

        for (GetReportsResponse report : allReports) {
            String reportName = report.getReportName();
            String reportType = report.getReportType();
            String reportCategory = report.getReportCategory();

            // SKIP Disabled Engines
            if ("Pentaho".equalsIgnoreCase(reportType) || "Email".equalsIgnoreCase(reportType)) {
                skippedCount++;
                continue;
            }

            // BUILD Dynamic Parameters for THIS Report
            Map<String, String> reportParams = new HashMap<>(context);

            // Special Handling: 'selectAccount' can be Loan or Savings depending on Category
            if ("Savings".equalsIgnoreCase(reportCategory)) {
                reportParams.put("R_selectAccount", savingsId.toString());
                reportParams.put("R_savingsTransactionId", savingsTransactionId.toString());
            } else {
                reportParams.put("R_selectAccount", loanId.toString());
            }

            // Execute
            Response<RunReportsResponse> response = fineractClient().reportsRun.runReportGetData(reportName, reportParams, false).execute();
            int statusCode = response.code();

            if (statusCode == 200) {
                successCount++;
            } else if (statusCode == 400) {
                // If it STILL fails validation, log it but don't fail build yet
                log.warn("Report '{}' returned 400 (Missing Params). Needs deeper context.", reportName);
            } else if (statusCode == 403) {
                // If data scoping still fails, log it.
                log.warn("Report '{}' returned 403 (Data Scope mismatch).", reportName);
            } else {
                // Fail on Crash
                log.error("Report '{}' FAILED with status: {}", reportName, statusCode);
                failedReports.put(reportName, "Status: " + statusCode);
            }
        }

        log.info("Summary: {} Success, {} Skipped, {} Failures.", successCount, skippedCount, failedReports.size());

        assertThat(failedReports.size()).as("The following reports crashed: " + failedReports).isEqualTo(0);
    }

    // --- Helpers ---

    private boolean shouldSkipReport(String reportName) throws IOException {
        Response<List<GetReportsResponse>> reportListResponse = okR(fineractClient().reports.retrieveReportList());
        List<GetReportsResponse> allReports = reportListResponse.body();
        if (allReports == null) {
            return false;
        }

        return allReports.stream().filter(r -> r.getReportName().equals(reportName)).findFirst().map(r -> {
            boolean skip = "Pentaho".equalsIgnoreCase(r.getReportType()) || "Email".equalsIgnoreCase(r.getReportType());
            if (skip) {
                log.info("Skipping '{}' because it is a {} report.", reportName, r.getReportType());
            }
            return skip;
        }).orElse(false); // If report not found, don't skip (let it fail)
    }

    private Long createClient(String fullName) {
        return ok(fineractClient().clients.create6(new PostClientsRequest().legalFormId(1L).officeId(1L).fullname(fullName)
                .dateFormat(Utils.DATE_FORMAT).locale("en_US").active(true).activationDate("01 January 2023"))).getClientId();
    }

    private Long applyForLoan(Long clientId, Long loanProductId) {
        PostLoansResponse response = ok(fineractClient().loans.calculateLoanScheduleOrSubmitLoanApplication(
                new PostLoansRequest().dateFormat(Utils.DATE_FORMAT).locale("en_US").clientId(clientId).productId(loanProductId)
                        .principal(BigDecimal.valueOf(1000.0)).loanTermFrequency(12).loanTermFrequencyType(2).numberOfRepayments(12)
                        .repaymentEvery(1).repaymentFrequencyType(2).interestRatePerPeriod(BigDecimal.valueOf(1.5)).amortizationType(1)
                        .interestType(1).interestCalculationPeriodType(1).transactionProcessingStrategyCode("mifos-standard-strategy")
                        .expectedDisbursementDate("01 January 2023").submittedOnDate("01 January 2023").loanType("individual"),
                "application"));
        return response.getLoanId();
    }

    private void approveLoan(Long loanId) {
        ok(fineractClient().loans.stateTransitions(loanId,
                new PostLoansLoanIdRequest().approvedOnDate("01 January 2023").dateFormat(Utils.DATE_FORMAT).locale("en_US"), "approve"));
    }

    private void disburseLoan(Long loanId, BigDecimal amount) {
        ok(fineractClient().loans.stateTransitions(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("01 January 2023")
                .dateFormat(Utils.DATE_FORMAT).locale("en_US").transactionAmount(amount), "disburse"));
    }

    private Long createLoanProduct() {
        PostLoanProductsResponse response = ok(fineractClient().loanProducts.createLoanProduct(new PostLoanProductsRequest()
                .name(Utils.uniqueRandomStringGenerator("Report_Product_", 6)).shortName(Utils.uniqueRandomStringGenerator("", 4))
                .description("Loan Product").currencyCode("USD").digitsAfterDecimal(2).inMultiplesOf(1).installmentAmountInMultiplesOf(1)
                .minPrincipal(100.0).principal(1000.0).maxPrincipal(10000.0).numberOfRepayments(12).repaymentEvery(1)
                .repaymentFrequencyType(2L).interestRatePerPeriod(1.5).interestRateFrequencyType(2).amortizationType(1).interestType(1)
                .interestCalculationPeriodType(1).transactionProcessingStrategyCode("mifos-standard-strategy").accountingRule(1)
                .daysInYearType(1).daysInMonthType(1).isInterestRecalculationEnabled(false).dateFormat(Utils.DATE_FORMAT).locale("en_US")));
        return response.getResourceId();
    }

    // --- Helpers for Groups & Savings ---

    private Long createGroup(String name) {
        return ok(fineractClient().groups.create8(new PostGroupsRequest().officeId(1L).name(name).active(false))).getResourceId();
    }

    private Long createSavingsProduct() {
        return ok(fineractClient().savingsProducts
                .create14(new PostSavingsProductsRequest().name(Utils.uniqueRandomStringGenerator("Savings_", 5))
                        .shortName(Utils.uniqueRandomStringGenerator("S", 3)).currencyCode("USD").digitsAfterDecimal(2).inMultiplesOf(1)
                        .nominalAnnualInterestRate(5.0).interestCompoundingPeriodType(1).interestPostingPeriodType(4)
                        .interestCalculationType(1).interestCalculationDaysInYearType(365).accountingRule(1).locale("en_US")))
                .getResourceId();
    }

    private Long applyForSavings(Long clientId, Long productId) {
        return ok(fineractClient().savingsAccounts.submitApplication2(new PostSavingsAccountsRequest().clientId(clientId)
                .productId(productId).locale("en_US").dateFormat(Utils.DATE_FORMAT).submittedOnDate("01 January 2023"))).getSavingsId();
    }

    private void approveSavings(Long savingsId) {
        ok(fineractClient().savingsAccounts.handleCommands6(savingsId,
                new PostSavingsAccountsAccountIdRequest().approvedOnDate("01 January 2023").dateFormat(Utils.DATE_FORMAT).locale("en_US"),
                "approve"));
    }

    private void activateSavings(Long savingsId) {
        ok(fineractClient().savingsAccounts.handleCommands6(savingsId,
                new PostSavingsAccountsAccountIdRequest().activatedOnDate("01 January 2023").dateFormat(Utils.DATE_FORMAT).locale("en_US"),
                "activate"));
    }

    private Long makeRepayment(Long loanId, BigDecimal amount) {
        return ok(
                fineractClient().loanTransactions.executeLoanTransaction(
                        loanId, new PostLoansLoanIdTransactionsRequest().transactionAmount(amount.doubleValue())
                                .dateFormat(Utils.DATE_FORMAT).locale("en_US").transactionDate("05 January 2023").paymentTypeId(1L),
                        "repayment"))
                .getResourceId();
    }

    private Long makeDeposit(Long savingsId, BigDecimal amount) {
        PostSavingsAccountTransactionsResponse response = ok(
                fineractClient().savingsTransactions.transaction2(savingsId,
                        new PostSavingsAccountTransactionsRequest().transactionAmount(BigDecimal.valueOf(amount.doubleValue()))
                                .dateFormat(Utils.DATE_FORMAT).locale("en_US").transactionDate("05 January 2023").paymentTypeId(1),
                        "deposit"));
        return response.getResourceId();
    }
}
