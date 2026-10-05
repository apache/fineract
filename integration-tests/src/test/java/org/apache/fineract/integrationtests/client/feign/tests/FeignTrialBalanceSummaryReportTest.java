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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.BusinessStepRequest;
import org.apache.fineract.client.models.ExternalAssetOwnerRequest;
import org.apache.fineract.client.models.GetFinancialActivityAccountsResponse;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostFinancialActivityAccountsRequest;
import org.apache.fineract.client.models.PostInitiateTransferResponse;
import org.apache.fineract.client.models.PostLoanOriginatorsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGlobalConfigurationHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.MethodOrderer;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.TestMethodOrder;

@Slf4j
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
@Order(1)
public class FeignTrialBalanceSummaryReportTest extends FeignIntegrationTest {

    private static final String REPORT_NAME = "Trial Balance Summary Report with Asset Owner";
    private static final BigDecimal LOAN_PRINCIPAL = BigDecimal.valueOf(15000.0);
    private static final String RETAINED_EARNING_JOB_NAME = "Retained Earning Job";
    private static final String RETAINED_EARNINGS_ACCOUNT_NAME = "Retained Earnings Prior Year";
    private static final String INCOME_EXPENSE_GL_ACCOUNTS = "income-expense-gl-accounts";
    private static final String RETAINED_GL_ACCOUNT = "retained-gl-account";
    private static final String RETAINED_EARNING_REPORT_NAME = "retained-earning-used-by-report-name";
    private static final String OFFICE_ID = "office-id";
    private static final String LAST_DAY_OF_FINANCIAL_YEAR = "last-day-of-financial-year";
    private static final String LAST_MONTH_OF_FINANCIAL_YEAR = "last-month-of-financial-year";

    private FeignAccountHelper accountHelper;
    private FeignGlobalConfigurationHelper globalConfigHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private FeignLoanHelper loanHelper;
    private FeignTransactionHelper transactionHelper;
    private FeignClientHelper clientHelper;
    private FeignSchedulerHelper schedulerHelper;
    private FeignChargesHelper chargesHelper;
    private FeignOfficeHelper officeHelper;
    private static FeignLoanOriginatorHelper originatorHelper;

    private Account assetAccount;
    private Account feePenaltyAccount;
    private Account transferAccount;
    private Account expenseAccount;
    private Account incomeAccount;
    private Account overpaymentAccount;
    private String todaysDate;
    private List<BusinessStep> originalBusinessSteps;
    private List<GetFinancialActivityAccountsResponse> originalFinancialMappings;

    @BeforeAll
    public void setup() {
        accountHelper = new FeignAccountHelper(fineractClient());
        globalConfigHelper = new FeignGlobalConfigurationHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        loanHelper = new FeignLoanHelper(fineractClient());
        transactionHelper = new FeignTransactionHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        schedulerHelper = new FeignSchedulerHelper(fineractClient());
        originatorHelper = new FeignLoanOriginatorHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
        officeHelper = new FeignOfficeHelper(fineractClient());

        todaysDate = Utils.getLocalDateOfTenant().toString();

        originalBusinessSteps = ok(
                () -> fineractClient().businessStepConfiguration().retrieveAllConfiguredBusinessStep("LOAN_CLOSE_OF_BUSINESS"))
                .getBusinessSteps();
        originalFinancialMappings = ok(() -> fineractClient().mappingFinancialActivitiesToAccounts().retrieveAll());

        configureBusinessSteps();

        assetAccount = accountHelper.createAssetAccount("TrialBal");
        feePenaltyAccount = accountHelper.createAssetAccount("TrialBalFP");
        transferAccount = accountHelper.createAssetAccount("TrialBalTR");
        expenseAccount = accountHelper.createExpenseAccount("TrialBalEXP");
        incomeAccount = accountHelper.createIncomeAccount("TrialBalINC");
        overpaymentAccount = accountHelper.createLiabilityAccount("TrialBalOP");

        setupFinancialActivityMapping();
    }

    @AfterAll
    public void tearDown() {
        executeVoid(() -> fineractClient().businessStepConfiguration().updateJobBusinessStepConfig("LOAN_CLOSE_OF_BUSINESS",
                new BusinessStepRequest().businessSteps(originalBusinessSteps)));
        List<GetFinancialActivityAccountsResponse> currentMappings = ok(
                () -> fineractClient().mappingFinancialActivitiesToAccounts().retrieveAll());
        for (GetFinancialActivityAccountsResponse mapping : currentMappings) {
            executeVoid(() -> fineractClient().mappingFinancialActivitiesToAccounts()
                    .deleteGLAccountMappingFinancialActivityAccount(mapping.getId()));
        }
        for (GetFinancialActivityAccountsResponse mapping : originalFinancialMappings) {
            ok(() -> fineractClient().mappingFinancialActivitiesToAccounts()
                    .createGLAccountMappingFinancialActivityAccount(new PostFinancialActivityAccountsRequest()
                            .financialActivityId(Long.valueOf(mapping.getFinancialActivityData().getId()))
                            .glAccountId(mapping.getGlAccountData().getId())));
        }
        schedulerHelper.startScheduler();
    }

    @Test
    @Order(1)
    public void testReportReturnsExpectedColumns() {
        RunReportsResponse response = runReport("2020-01-01");

        assertNotNull(response);
        assertNotNull(response.getColumnHeaders());
        assertFalse(response.getColumnHeaders().isEmpty());

        List<String> expectedColumns = List.of("postingdate", "product", "glacct", "description", "assetowner", "beginningbalance",
                "debitmovement", "creditmovement", "endingbalance", "originator_external_ids");
        List<String> actualColumns = response.getColumnHeaders().stream().map(ResultsetColumnHeaderData::getColumnName).toList();
        Assertions.assertEquals(expectedColumns, actualColumns);
    }

    @Test
    @Order(2)
    public void testExternalAssetOwnerEntriesAppearInReport() {
        runWithBusinessDate("2020-03-02", () -> {
            Long clientId = createClient("01 March 2020");
            Long loanId = createAndDisburseLoan(clientId, "01 March 2020", "02 March 2020", null, null);

            String ownerExternalId = UUID.randomUUID().toString();
            PostInitiateTransferResponse saleResponse = ok(() -> fineractClient().externalAssetOwners().transferRequestWithLoanId(loanId,
                    new ExternalAssetOwnerRequest().settlementDate("2020-03-02").dateFormat("yyyy-MM-dd").locale(LoanTestData.LOCALE)
                            .transferExternalId(UUID.randomUUID().toString()).ownerExternalId(ownerExternalId).purchasePriceRatio("1.0"),
                    "sale"));
            assertNotNull(saleResponse);

            advanceBusinessDateAndRunCob("2020-03-03");

            RunReportsResponse report = runReport("2020-03-02");
            assertNotNull(report.getData());

            int assetOwnerColIdx = findColumnIndex(report, "assetowner");
            boolean hasExternalOwnerEntry = report.getData().stream()
                    .anyMatch(row -> ownerExternalId.equals(String.valueOf(row.getRow().get(assetOwnerColIdx))));

            assertTrue(hasExternalOwnerEntry,
                    "Report must contain entries for external asset owner '" + ownerExternalId + "'. Actual rows: " + report.getData());
        });
    }

    @Test
    @Order(3)
    public void testBalanceFormulaIsConsistent() {
        runWithBusinessDate("2020-06-01", () -> {
            Long clientId = createClient("01 June 2020");
            Long chargeId = createFlatFeeCharge(500.0);
            Long loanId = createAndDisburseLoan(clientId, "01 June 2020", "01 June 2020", chargeId, null);

            String productName = loanHelper.getLoanDetails(loanId).getLoanProductName();

            transactionHelper.addRepayment(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("01 June 2020")
                    .transactionAmount(1500.0).locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            RunReportsResponse report = runReport("2020-06-01");
            assertNotNull(report.getData());

            int productIdx = findColumnIndex(report, "product");
            int assetOwnerIdx = findColumnIndex(report, "assetowner");
            int beginBalIdx = findColumnIndex(report, "beginningbalance");
            int debitIdx = findColumnIndex(report, "debitmovement");
            int creditIdx = findColumnIndex(report, "creditmovement");
            int endingBalIdx = findColumnIndex(report, "endingbalance");
            int glAcctIdx = findColumnIndex(report, "glacct");

            List<ResultsetRowData> productRows = report.getData().stream()
                    .filter(row -> productName.equals(String.valueOf(row.getRow().get(productIdx)))).toList();

            assertFalse(productRows.isEmpty(), "Report must contain entries for product '" + productName + "'.");

            boolean allSelfOwned = productRows.stream().allMatch(row -> "self".equals(String.valueOf(row.getRow().get(assetOwnerIdx))));
            assertTrue(allSelfOwned, "All entries should be 'self' when no external asset owner transfer exists.");

            assertTrue(productRows.size() >= 2, "Report must contain at least 2 GL account rows for a product with charges.");

            String assetGlCode = accountHelper.getGlCode(assetAccount);
            String feeGlCode = accountHelper.getGlCode(feePenaltyAccount);
            boolean hasAssetRow = productRows.stream().anyMatch(row -> assetGlCode.equals(String.valueOf(row.getRow().get(glAcctIdx))));
            boolean hasFeeRow = productRows.stream().anyMatch(row -> feeGlCode.equals(String.valueOf(row.getRow().get(glAcctIdx))));
            assertTrue(hasAssetRow, "Report must contain asset account row.");
            assertTrue(hasFeeRow, "Report must contain fee/penalty receivable row.");

            for (ResultsetRowData row : productRows) {
                BigDecimal beginBal = parseBigDecimal(row.getRow().get(beginBalIdx));
                BigDecimal debit = parseBigDecimal(row.getRow().get(debitIdx));
                BigDecimal credit = parseBigDecimal(row.getRow().get(creditIdx));
                BigDecimal endBal = parseBigDecimal(row.getRow().get(endingBalIdx));

                Assertions.assertEquals(0, BigDecimal.ZERO.compareTo(beginBal),
                        "Beginning balance should be 0 when no prior year data exists. Row: " + row.getRow());

                BigDecimal expectedEndBal = beginBal.add(debit).add(credit);
                Assertions.assertEquals(0, expectedEndBal.compareTo(endBal),
                        "Ending balance must equal beginning + debit + credit. Expected: " + expectedEndBal + ", Actual: " + endBal);
            }
        });
    }

    @Test
    @Order(4)
    public void testNoRetainedEarningsWithoutAnnualSummary() {
        RunReportsResponse report = runReport("2020-06-01");
        assertNotNull(report);
        assertNotNull(report.getData());

        int glAcctIdx = findColumnIndex(report, "glacct");
        boolean hasRetainedEarningsRow = report.getData().stream()
                .anyMatch(row -> "320000".equals(String.valueOf(row.getRow().get(glAcctIdx))));
        assertFalse(hasRetainedEarningsRow, "Report should not contain Retained Earnings rows when no annual summary data exists.");
    }

    @Test
    @Order(5)
    public void testOriginatorExternalIdsEntriesAppearInReport() {
        runWithBusinessDate("2020-03-02", () -> {
            Long clientId = createClient("01 March 2020");
            String originatorExternalId = UUID.randomUUID().toString();
            Long loanId = createAndDisburseLoan(clientId, "01 March 2020", "02 March 2020", null, originatorExternalId);

            advanceBusinessDateAndRunCob("2020-03-03");

            RunReportsResponse report = runReport("2020-03-02");
            assertNotNull(report.getData());

            int originatorIdColIdx = findColumnIndex(report, "originator_external_ids");
            boolean hasOriginatorIdEntry = report.getData().stream()
                    .anyMatch(row -> originatorExternalId.equals(String.valueOf(row.getRow().get(originatorIdColIdx))));

            assertTrue(hasOriginatorIdEntry, "Report must contain entries for originator external ids '" + originatorExternalId
                    + "'. Actual rows: " + report.getData());
        });
    }

    /**
     * Needs a database where FY2022 has not been closed: the job persists only when no
     * acc_gl_journal_entry_annual_summary rows exist for year_end_date 2022-12-31 in any office.
     */
    @Test
    @Order(6)
    public void testRetainedEarningJobClosesIncomePerOriginator() {
        Map<String, GlobalConfigurationPropertyData> originalConfigs = snapshotRetainedEarningConfigs();
        try {
            List<GetGLAccountsResponse> existingRetainedEarningsAccounts = accountHelper
                    .findGLAccountsByName(RETAINED_EARNINGS_ACCOUNT_NAME);
            assertTrue(existingRetainedEarningsAccounts.size() <= 1,
                    "The report needs a unique '" + RETAINED_EARNINGS_ACCOUNT_NAME + "' account: " + existingRetainedEarningsAccounts);
            String retainedEarningsGlCode;
            if (existingRetainedEarningsAccounts.isEmpty()) {
                retainedEarningsGlCode = accountHelper.createEquityAccountWithExactName(RETAINED_EARNINGS_ACCOUNT_NAME).getGlCode();
            } else {
                retainedEarningsGlCode = existingRetainedEarningsAccounts.get(0).getGlCode();
            }

            Long officeId = officeHelper.createOffice(LocalDate.of(2022, 1, 1)).getOfficeId();
            Account feeIncomeAccount = accountHelper.createIncomeAccount("TrialBalREFEE");
            String feeIncomeGlCode = accountHelper.getGlCode(feeIncomeAccount);

            globalConfigHelper.updateConfigurationStringValue(INCOME_EXPENSE_GL_ACCOUNTS, feeIncomeGlCode);
            globalConfigHelper.updateConfigurationStringValue(RETAINED_GL_ACCOUNT, retainedEarningsGlCode);
            globalConfigHelper.updateConfigurationStringValue(RETAINED_EARNING_REPORT_NAME, REPORT_NAME);
            globalConfigHelper.updateGlobalConfigurationInternal(OFFICE_ID, officeId);
            globalConfigHelper.updateGlobalConfigurationInternal(LAST_DAY_OF_FINANCIAL_YEAR, 31L);
            globalConfigHelper.updateGlobalConfigurationInternal(LAST_MONTH_OF_FINANCIAL_YEAR, 12L);

            String originatorX = UUID.randomUUID().toString();

            runWithBusinessDate("2022-11-01", () -> {
                Long clientId = createClient("01 November 2022", officeId);
                Long productId = createCashBasedZeroInterestLoanProduct(feeIncomeAccount);
                String productName = loanHelper.retrieveLoanProduct(productId).getName();

                Long loanA = createAndDisburseLoanWithFee(clientId, productId, "01 November 2022", 500.0, originatorX);
                Long loanB = createAndDisburseLoanWithFee(clientId, productId, "01 November 2022", 300.0, null);
                repay(loanA, 500.0, "01 November 2022");
                repay(loanB, 300.0, "01 November 2022");

                RunReportsResponse preClose = runReport("2022-12-31", officeId);
                assertEquals(
                        List.of(reportRow("", "-300.00", "0.00", "0.00", "-300.00"),
                                reportRow(originatorX, "-500.00", "0.00", "0.00", "-500.00")).stream().sorted().toList(),
                        reportRows(preClose, feeIncomeGlCode, productName),
                        "Pre-close fee income rows per originator bucket for office " + officeId);

                businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2023-01-02");
                schedulerHelper.executeAndAwaitJob(RETAINED_EARNING_JOB_NAME);

                RunReportsResponse postClose = runReport("2023-01-01", officeId);
                assertAll(
                        () -> assertEquals(List.of(), reportRows(postClose, feeIncomeGlCode, productName),
                                "Post-close fee income must be fully closed out in every originator bucket"),
                        () -> assertEquals(
                                List.of(reportRow("", "-300.00", "0.00", "0.00", "-300.00"),
                                        reportRow(originatorX, "-500.00", "0.00", "0.00", "-500.00")).stream().sorted().toList(),
                                reportRows(postClose, retainedEarningsGlCode, productName),
                                "Post-close retained earnings must be carried per originator bucket"));
            });
        } finally {
            restoreRetainedEarningConfigs(originalConfigs);
        }
    }

    private void configureBusinessSteps() {
        List<String> stepNames = List.of("APPLY_CHARGE_TO_OVERDUE_LOANS", "LOAN_DELINQUENCY_CLASSIFICATION", "CHECK_LOAN_REPAYMENT_DUE",
                "CHECK_LOAN_REPAYMENT_OVERDUE", "UPDATE_LOAN_ARREARS_AGING", "ADD_PERIODIC_ACCRUAL_ENTRIES",
                "EXTERNAL_ASSET_OWNER_TRANSFER");
        List<BusinessStep> steps = IntStream.range(0, stepNames.size())
                .mapToObj(i -> new BusinessStep().stepName(stepNames.get(i)).order((long) (i + 1))).collect(Collectors.toList());
        executeVoid(() -> fineractClient().businessStepConfiguration().updateJobBusinessStepConfig("LOAN_CLOSE_OF_BUSINESS",
                new BusinessStepRequest().businessSteps(steps)));
    }

    private void setupFinancialActivityMapping() {
        List<GetFinancialActivityAccountsResponse> mappings = ok(
                () -> fineractClient().mappingFinancialActivitiesToAccounts().retrieveAll());
        for (GetFinancialActivityAccountsResponse mapping : mappings) {
            executeVoid(() -> fineractClient().mappingFinancialActivitiesToAccounts()
                    .deleteGLAccountMappingFinancialActivityAccount(mapping.getId()));
        }
        ok(() -> fineractClient().mappingFinancialActivitiesToAccounts().createGLAccountMappingFinancialActivityAccount(
                new PostFinancialActivityAccountsRequest().financialActivityId(100L).glAccountId((long) transferAccount.getAccountID())));
    }

    private void runWithBusinessDate(String date, Runnable action) {
        try {
            globalConfigHelper.updateConfigurationByName("enable-business-date", true);
            globalConfigHelper.updateConfigurationByName("enable-auto-generated-external-id", true);
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", date);
            action.run();
        } finally {
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", todaysDate);
            globalConfigHelper.updateConfigurationByName("enable-business-date", false);
            globalConfigHelper.updateConfigurationByName("enable-auto-generated-external-id", false);
        }
    }

    private void advanceBusinessDateAndRunCob(String date) {
        businessDateHelper.updateBusinessDate("BUSINESS_DATE", date);
        schedulerHelper.executeAndAwaitJob("Loan COB");
    }

    private RunReportsResponse runReport(String endDate) {
        return ok(() -> fineractClient().runReports().runReportGetData(REPORT_NAME, Map.of("R_endDate", endDate, "R_officeId", "1")));
    }

    private RunReportsResponse runReport(String endDate, Long officeId) {
        return ok(() -> fineractClient().runReports().runReportGetData(REPORT_NAME,
                Map.of("R_endDate", endDate, "R_officeId", String.valueOf(officeId))));
    }

    private List<String> reportRows(RunReportsResponse report, String glCode, String productName) {
        int glAcctIdx = findColumnIndex(report, "glacct");
        int productIdx = findColumnIndex(report, "product");
        int assetOwnerIdx = findColumnIndex(report, "assetowner");
        int originatorIdx = findColumnIndex(report, "originator_external_ids");
        int beginBalIdx = findColumnIndex(report, "beginningbalance");
        int debitIdx = findColumnIndex(report, "debitmovement");
        int creditIdx = findColumnIndex(report, "creditmovement");
        int endingBalIdx = findColumnIndex(report, "endingbalance");
        return report.getData().stream().map(ResultsetRowData::getRow)
                .filter(row -> glCode.equals(String.valueOf(row.get(glAcctIdx))) && productName.equals(String.valueOf(row.get(productIdx))))
                .map(row -> "originator=\"" + row.get(originatorIdx) + "\" assetowner=" + row.get(assetOwnerIdx) + " beginningbalance="
                        + money(row.get(beginBalIdx)) + " debitmovement=" + money(row.get(debitIdx)) + " creditmovement="
                        + money(row.get(creditIdx)) + " endingbalance=" + money(row.get(endingBalIdx)))
                .sorted().toList();
    }

    private String reportRow(String originator, String beginningBalance, String debitMovement, String creditMovement,
            String endingBalance) {
        return "originator=\"" + originator + "\" assetowner=self beginningbalance=" + beginningBalance + " debitmovement=" + debitMovement
                + " creditmovement=" + creditMovement + " endingbalance=" + endingBalance;
    }

    private String money(Object value) {
        return parseBigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }

    private Map<String, GlobalConfigurationPropertyData> snapshotRetainedEarningConfigs() {
        Map<String, GlobalConfigurationPropertyData> snapshot = new LinkedHashMap<>();
        for (String name : List.of(INCOME_EXPENSE_GL_ACCOUNTS, RETAINED_GL_ACCOUNT, RETAINED_EARNING_REPORT_NAME, OFFICE_ID,
                LAST_DAY_OF_FINANCIAL_YEAR, LAST_MONTH_OF_FINANCIAL_YEAR)) {
            snapshot.put(name, globalConfigHelper.getGlobalConfigurationByName(name));
        }
        return snapshot;
    }

    private void restoreRetainedEarningConfigs(Map<String, GlobalConfigurationPropertyData> snapshot) {
        for (String name : List.of(INCOME_EXPENSE_GL_ACCOUNTS, RETAINED_GL_ACCOUNT, RETAINED_EARNING_REPORT_NAME)) {
            String original = snapshot.get(name).getStringValue();
            // the configuration API rejects a blank stringValue, so a blank default cannot be written back
            if (original != null && !original.isBlank()) {
                globalConfigHelper.updateConfigurationStringValue(name, original);
            }
        }
        for (String name : List.of(OFFICE_ID, LAST_DAY_OF_FINANCIAL_YEAR, LAST_MONTH_OF_FINANCIAL_YEAR)) {
            globalConfigHelper.updateGlobalConfigurationInternal(name, snapshot.get(name).getValue());
        }
    }

    private void repay(Long loanId, double amount, String date) {
        transactionHelper.addRepayment(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate(date).transactionAmount(amount)
                .locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
    }

    private Long createAndDisburseLoanWithFee(Long clientId, Long productId, String date, double feeAmount, String originatorExternalId) {
        BigDecimal principal = BigDecimal.valueOf(1000.0);
        Long loanId = loanHelper.applyForLoan(new PostLoansRequest()//
                .clientId(clientId)//
                .productId(productId)//
                .loanType("individual")//
                .submittedOnDate(date)//
                .expectedDisbursementDate(date)//
                .principal(principal)//
                .loanTermFrequency(1)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.ZERO)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)).getLoanId();

        Long chargeId = createFlatFeeCharge(feeAmount);
        ok(() -> fineractClient().loanCharges().createOrPayLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId)
                .amount(feeAmount).dueDate(date).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE), (String) null));

        if (originatorExternalId != null) {
            createAndAttachOriginator(loanId, originatorExternalId);
        }

        loanHelper.approveLoan(loanId, new PostLoansLoanIdRequest()//
                .approvedLoanAmount(principal)//
                .approvedOnDate(date)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        loanHelper.disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(date)//
                .transactionAmount(principal)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));
        return loanId;
    }

    private Long createCashBasedZeroInterestLoanProduct(Account feeIncomeAccount) {
        return loanHelper.createLoanProduct(new PostLoanProductsRequest()//
                .name("TrialBal RE Product " + System.currentTimeMillis())//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(1000.0)//
                .numberOfRepayments(1)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2L)//
                .interestRatePerPeriod(0.0)//
                .interestRateFrequencyType(2)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .daysInYearType(365)//
                .daysInMonthType(30)//
                .isInterestRecalculationEnabled(false)//
                .accountingRule(2)//
                .fundSourceAccountId((long) assetAccount.getAccountID())//
                .loanPortfolioAccountId((long) assetAccount.getAccountID())//
                .transfersInSuspenseAccountId((long) assetAccount.getAccountID())//
                .interestOnLoanAccountId((long) incomeAccount.getAccountID())//
                .incomeFromFeeAccountId((long) feeIncomeAccount.getAccountID())//
                .incomeFromPenaltyAccountId((long) incomeAccount.getAccountID())//
                .incomeFromRecoveryAccountId((long) incomeAccount.getAccountID())//
                .writeOffAccountId((long) expenseAccount.getAccountID())//
                .overpaymentLiabilityAccountId((long) overpaymentAccount.getAccountID())//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)).getResourceId();
    }

    private int findColumnIndex(RunReportsResponse report, String columnName) {
        List<ResultsetColumnHeaderData> headers = report.getColumnHeaders();
        for (int i = 0; i < headers.size(); i++) {
            if (columnName.equals(headers.get(i).getColumnName())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found. Available: "
                + headers.stream().map(ResultsetColumnHeaderData::getColumnName).toList());
    }

    private BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        String str = String.valueOf(value);
        if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(str);
    }

    private Long createClient(String activationDate) {
        return createClient(activationDate, 1L);
    }

    private Long createClient(String activationDate, Long officeId) {
        return clientHelper.createClient(new PostClientsRequest()//
                .officeId(officeId)//
                .legalFormId(1L)//
                .firstname(Utils.randomFirstNameGenerator())//
                .lastname(Utils.randomLastNameGenerator())//
                .externalId(Utils.randomStringGenerator("EXT_", 7))//
                .active(true)//
                .activationDate(activationDate)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)//
                .locale(LoanTestData.LOCALE)).getClientId();
    }

    private Long createAndDisburseLoan(Long clientId, String submitDate, String disburseDate, Long chargeId, String originatorExternalId) {
        Long loanProductId = createLoanProduct();
        assertNotNull(loanProductId);

        PostLoansRequest loanRequest = new PostLoansRequest()//
                .clientId(clientId)//
                .productId(loanProductId)//
                .loanType("individual")//
                .submittedOnDate(submitDate)//
                .expectedDisbursementDate(submitDate)//
                .principal(LOAN_PRINCIPAL)//
                .loanTermFrequency(4)//
                .loanTermFrequencyType(2)//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2)//
                .interestRatePerPeriod(BigDecimal.valueOf(2.0))//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN);

        Long loanId = loanHelper.applyForLoan(loanRequest).getLoanId();
        assertNotNull(loanId);

        if (chargeId != null) {
            ok(() -> fineractClient().loanCharges().createOrPayLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId)
                    .amount(500.0).dueDate(disburseDate).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE),
                    (String) null));
        }

        if (originatorExternalId != null) {
            createAndAttachOriginator(loanId, originatorExternalId);
        }

        loanHelper.approveLoan(loanId, new PostLoansLoanIdRequest()//
                .approvedLoanAmount(LOAN_PRINCIPAL)//
                .approvedOnDate(submitDate)//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));

        var loanDetails = loanHelper.getLoanDetails(loanId);
        loanHelper.disburseLoan(loanId, new PostLoansLoanIdRequest()//
                .actualDisbursementDate(disburseDate)//
                .transactionAmount(loanDetails.getNetDisbursalAmount())//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN));

        return loanId;
    }

    private void createAndAttachOriginator(Long loanId, String originatorExternalId) {
        Long originatorId = originatorHelper.createOriginator(new PostLoanOriginatorsRequest().externalId(originatorExternalId));
        assertNotNull(originatorId);
        originatorHelper.attachOriginatorToLoan(loanId, originatorId);
    }

    private Long createFlatFeeCharge(double amount) {
        return chargesHelper.createLoanSpecifiedDueDateCharge(amount).getResourceId();
    }

    private Long createLoanProduct() {
        return loanHelper.createLoanProduct(new PostLoanProductsRequest()//
                .name("TrialBal Product " + System.currentTimeMillis())//
                .shortName(Utils.uniqueRandomStringGenerator("", 4))//
                .currencyCode("USD")//
                .digitsAfterDecimal(2)//
                .inMultiplesOf(1)//
                .principal(LOAN_PRINCIPAL.doubleValue())//
                .numberOfRepayments(4)//
                .repaymentEvery(1)//
                .repaymentFrequencyType(2L)//
                .interestRatePerPeriod(1.0)//
                .interestRateFrequencyType(2)//
                .amortizationType(1)//
                .interestType(0)//
                .interestCalculationPeriodType(1)//
                .transactionProcessingStrategyCode("mifos-standard-strategy")//
                .daysInYearType(365)//
                .daysInMonthType(30)//
                .isInterestRecalculationEnabled(false)//
                .accountingRule(3)//
                .loanPortfolioAccountId((long) assetAccount.getAccountID())//
                .transfersInSuspenseAccountId((long) assetAccount.getAccountID())//
                .interestOnLoanAccountId((long) incomeAccount.getAccountID())//
                .incomeFromFeeAccountId((long) incomeAccount.getAccountID())//
                .incomeFromPenaltyAccountId((long) incomeAccount.getAccountID())//
                .writeOffAccountId((long) expenseAccount.getAccountID())//
                .overpaymentLiabilityAccountId((long) overpaymentAccount.getAccountID())//
                .receivableInterestAccountId((long) feePenaltyAccount.getAccountID())//
                .receivableFeeAccountId((long) feePenaltyAccount.getAccountID())//
                .receivablePenaltyAccountId((long) feePenaltyAccount.getAccountID())//
                .fundSourceAccountId((long) assetAccount.getAccountID())//
                .incomeFromRecoveryAccountId((long) incomeAccount.getAccountID())//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)).getResourceId();
    }
}
