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

import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.LOAN_REPORT_NAME;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.RETAINED_EARNING_JOB_NAME;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.WORKING_CAPITAL_REPORT_NAME;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.findColumnIndex;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.parseBigDecimal;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.reportRow;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.reportRows;
import static org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper.rowsOfProduct;
import static org.junit.jupiter.api.Assertions.assertAll;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.fineract.client.models.PostClientsRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanOriginatorHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignSchedulerHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTrialBalanceReportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.WorkingCapitalLoanOriginatorHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Order;
import org.junit.jupiter.api.Test;

/**
 * Verifies that the working capital loan trial balance report gives the same outcome as the regular loan "Trial Balance
 * Summary Report with Asset Owner", only for working capital loans and products.
 */
public class FeignWorkingCapitalTrialBalanceSummaryReportTest extends FeignWorkingCapitalTestBase {

    private static final String JOURNAL_ENTRY_AGGREGATION_JOB_NAME = "Journal Entry Aggregation";

    private FeignAccountHelper accountHelper;
    private FeignLoanHelper loanHelper;
    private FeignTransactionHelper transactionHelper;
    private FeignLoanOriginatorHelper loanOriginatorHelper;
    private FeignSchedulerHelper schedulerHelper;
    private FeignOfficeHelper officeHelper;
    private FeignChargesHelper chargesHelper;
    private WorkingCapitalLoanOriginatorHelper wcOriginatorHelper;
    private FeignTrialBalanceReportHelper trialBalanceHelper;

    private Account fundSourceAccount;
    private Account loanPortfolioAccount;
    private Account feesReceivableAccount;
    private Account penaltiesReceivableAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account liabilityAccount;
    private Account chargeOffExpenseAccount;
    private Account chargeOffIncomeAccount;

    @BeforeAll
    public void setup() {
        accountHelper = new FeignAccountHelper(fineractClient());
        loanHelper = new FeignLoanHelper(fineractClient());
        transactionHelper = new FeignTransactionHelper(fineractClient());
        loanOriginatorHelper = new FeignLoanOriginatorHelper(fineractClient());
        schedulerHelper = new FeignSchedulerHelper(fineractClient());
        officeHelper = new FeignOfficeHelper(fineractClient());
        chargesHelper = new FeignChargesHelper(fineractClient());
        wcOriginatorHelper = new WorkingCapitalLoanOriginatorHelper();
        trialBalanceHelper = new FeignTrialBalanceReportHelper(fineractClient());

        fundSourceAccount = accountHelper.createAssetAccount("WcTrialBalFund");
        loanPortfolioAccount = accountHelper.createAssetAccount("WcTrialBalPort");
        feesReceivableAccount = accountHelper.createAssetAccount("WcTrialBalFeeRcv");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("WcTrialBalPenRcv");
        incomeAccount = accountHelper.createIncomeAccount("WcTrialBalInc");
        expenseAccount = accountHelper.createExpenseAccount("WcTrialBalExp");
        liabilityAccount = accountHelper.createLiabilityAccount("WcTrialBalLia");
        chargeOffExpenseAccount = accountHelper.createExpenseAccount("WcTrialBalCOExp");
        chargeOffIncomeAccount = accountHelper.createIncomeAccount("WcTrialBalCOInc");
    }

    @Test
    @Order(1)
    public void testReportReturnsExpectedColumns() {
        RunReportsResponse response = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-01-01",
                FeignOfficeHelper.HEAD_OFFICE_ID);

        List<String> expectedColumns = List.of("postingdate", "product", "glacct", "description", "assetowner", "beginningbalance",
                "debitmovement", "creditmovement", "endingbalance", "originator_external_ids");
        List<String> actualColumns = response.getColumnHeaders().stream().map(ResultsetColumnHeaderData::getColumnName).toList();
        assertEquals(expectedColumns, actualColumns);
    }

    @Test
    @Order(2)
    public void testOpeningBalanceMovementAndOfficeFiltering() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2030, 1, 1)).getOfficeId();
        runAt("2030-03-01", () -> {
            Long clientId = createClientInOffice("01 March 2030", officeId);
            Long productId = createWorkingCapitalProduct(incomeAccount);
            String productName = productName(productId);
            Long loanId = createApproveAndDisburseWcLoan(clientId, productId, BigDecimal.valueOf(9000), "01 March 2030");

            setBusinessDate("2030-03-02");
            makeWcRepayment(loanId, BigDecimal.valueOf(1000), "02 March 2030");

            RunReportsResponse disbursementDay = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-03-01", officeId);
            assertAll(
                    () -> assertEquals(List.of(reportRow("", "0.00", "9000.00", "0.00", "9000.00")),
                            reportRows(disbursementDay, glCode(loanPortfolioAccount), productName)),
                    () -> assertEquals(List.of(reportRow("", "0.00", "0.00", "-9000.00", "-9000.00")),
                            reportRows(disbursementDay, glCode(fundSourceAccount), productName)));

            RunReportsResponse repaymentDay = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-03-02", officeId);
            assertAll(
                    () -> assertEquals(List.of(reportRow("", "9000.00", "0.00", "-1000.00", "8000.00")),
                            reportRows(repaymentDay, glCode(loanPortfolioAccount), productName)),
                    () -> assertEquals(List.of(reportRow("", "-9000.00", "1000.00", "0.00", "-8000.00")),
                            reportRows(repaymentDay, glCode(fundSourceAccount), productName)),
                    () -> assertBalanceFormula(repaymentDay, productName));

            assertTrue(
                    rowsOfProduct(trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-03-02", FeignOfficeHelper.HEAD_OFFICE_ID),
                            productName).isEmpty(),
                    "Working capital loan entries of another office must not show up");
            assertTrue(trialBalanceHelper.runReport(LOAN_REPORT_NAME, "2030-03-02", officeId).getData().isEmpty(),
                    "Working capital loan entries must not show up in the regular loan report");
        });
    }

    @Test
    @Order(3)
    public void testOriginatorExternalIdsAreSortedAndCommaSeparated() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2030, 1, 1)).getOfficeId();
        runAt("2030-04-01", () -> {
            Long clientId = createClientInOffice("01 April 2030", officeId);
            Long productId = createWorkingCapitalProduct(incomeAccount);
            String productName = productName(productId);
            String firstOriginator = "B-" + UUID.randomUUID();
            String secondOriginator = "A-" + UUID.randomUUID();
            createApproveAndDisburseWcLoan(clientId, productId, BigDecimal.valueOf(1000), "01 April 2030",
                    List.of(createWcOriginator(firstOriginator), createWcOriginator(secondOriginator)));

            String expectedOriginators = secondOriginator + ", " + firstOriginator;
            RunReportsResponse report = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-04-01", officeId);
            assertEquals(List.of(reportRow(expectedOriginators, "0.00", "1000.00", "0.00", "1000.00")),
                    reportRows(report, glCode(loanPortfolioAccount), productName));
        });
    }

    @Test
    @Order(4)
    public void testWorkingCapitalReportMatchesRegularLoanReportForMatchingTransactions() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2030, 1, 1)).getOfficeId();
        Account feeIncomeAccount = accountHelper.createIncomeAccount("WcTrialBalSbsFee");
        runAt("2030-05-01", () -> {
            Long clientId = createClientInOffice("01 May 2030", officeId);
            Long originatorId = createWcOriginator(UUID.randomUUID().toString());

            Long wcProductId = createWorkingCapitalProduct(feeIncomeAccount);
            String wcProductName = productName(wcProductId);
            Long wcLoanId = createApproveAndDisburseWcLoan(clientId, wcProductId, BigDecimal.valueOf(1000), "01 May 2030",
                    List.of(originatorId));
            addFee(wcLoanId, 100, "01 May 2030");

            Long loanProductId = createAccrualLoanProduct(feeIncomeAccount);
            String loanProductName = loanHelper.retrieveLoanProduct(loanProductId).getName();
            Long loanId = createAndDisburseLoan(clientId, loanProductId, "01 May 2030", List.of(originatorId));
            addLoanFee(loanId, 100, "01 May 2030");

            // accrues the fee on its due date, then repays the fee and part of the principal
            setBusinessDate("2030-05-02");
            runInlineWcCob(wcLoanId);
            transactionHelper.executeInlineCOB(loanId);
            makeWcRepayment(wcLoanId, BigDecimal.valueOf(400), "02 May 2030");
            transactionHelper.addRepayment(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("02 May 2030")
                    .transactionAmount(400.0).locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            setBusinessDate("2030-05-03");
            wcLoanHelper.chargeOff(wcLoanId, WorkingCapitalLoanRequestBuilders.chargeOff("03 May 2030", null));
            transactionHelper.chargeOffLoan(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("03 May 2030")
                    .locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));

            assertEquals(List.of(reportRow(originatorExternalIdOf(originatorId), "0.00", "700.00", "0.00", "700.00")),
                    reportRows(trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-05-03", officeId),
                            glCode(chargeOffExpenseAccount), wcProductName),
                    "The charge-off books the outstanding principal as expense");

            List<Account> comparedAccounts = List.of(loanPortfolioAccount, fundSourceAccount, feesReceivableAccount, feeIncomeAccount,
                    chargeOffExpenseAccount, chargeOffIncomeAccount);
            for (String endDate : List.of("2030-05-01", "2030-05-02", "2030-05-03", "2030-05-04")) {
                RunReportsResponse wcReport = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, endDate, officeId);
                RunReportsResponse loanReport = trialBalanceHelper.runReport(LOAN_REPORT_NAME, endDate, officeId);
                for (Account account : comparedAccounts) {
                    assertEquals(reportRows(loanReport, glCode(account), loanProductName),
                            reportRows(wcReport, glCode(account), wcProductName),
                            "Working capital and regular loan rows must match for GL " + glCode(account) + " on " + endDate);
                }
                assertEquals(List.of(wcProductName), productsOf(wcReport), "Only working capital loan rows belong to the WC report");
                assertEquals(List.of(loanProductName), productsOf(loanReport), "Only regular loan rows belong to the regular loan report");
            }
            for (Account account : List.of(loanPortfolioAccount, fundSourceAccount, feeIncomeAccount, chargeOffExpenseAccount)) {
                assertFalse(reportRows(trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-05-03", officeId), glCode(account),
                        wcProductName).isEmpty(), "Working capital report must contain GL " + glCode(account));
            }
        });
    }

    /**
     * Closes FY2030 for a working capital loan product and a regular loan product with matching transactions in the
     * same office, so both reports must carry the same retained earnings into 2031. Needs a database where FY2030 has
     * not been closed: the job persists only when no acc_gl_journal_entry_annual_summary rows exist for year_end_date
     * 2030-12-31 in any office.
     */
    @Test
    @Order(5)
    public void testRetainedEarningJobClosesWorkingCapitalIncomePerOriginator() {
        String retainedEarningsGlCode = trialBalanceHelper.findOrCreateRetainedEarningsAccount();
        Long officeId = officeHelper.createOffice(LocalDate.of(2030, 1, 1)).getOfficeId();
        Account feeIncomeAccount = accountHelper.createIncomeAccount("WcTrialBalREFee");
        String feeIncomeGlCode = glCode(feeIncomeAccount);
        String originatorX = UUID.randomUUID().toString();

        trialBalanceHelper.runWithRetainedEarningConfiguration(feeIncomeGlCode, retainedEarningsGlCode, officeId,
                () -> runAt("2030-11-01", () -> {
                    Long clientId = createClientInOffice("01 November 2030", officeId);
                    Long originatorId = createWcOriginator(originatorX);

                    Long wcProductId = createWorkingCapitalProduct(feeIncomeAccount);
                    String wcProductName = productName(wcProductId);
                    Long wcLoanA = createApproveAndDisburseWcLoan(clientId, wcProductId, BigDecimal.valueOf(1000), "01 November 2030",
                            List.of(originatorId));
                    addFee(wcLoanA, 500, "01 November 2030");
                    Long wcLoanB = createApproveAndDisburseWcLoan(clientId, wcProductId, BigDecimal.valueOf(1000), "01 November 2030",
                            List.of());
                    addFee(wcLoanB, 300, "01 November 2030");

                    Long loanProductId = createAccrualLoanProduct(feeIncomeAccount);
                    String loanProductName = loanHelper.retrieveLoanProduct(loanProductId).getName();
                    Long loanA = createAndDisburseLoan(clientId, loanProductId, "01 November 2030", List.of(originatorId));
                    addLoanFee(loanA, 500, "01 November 2030");
                    Long loanB = createAndDisburseLoan(clientId, loanProductId, "01 November 2030", List.of());
                    addLoanFee(loanB, 300, "01 November 2030");

                    setBusinessDate("2030-11-02");
                    runInlineWcCob(wcLoanA);
                    runInlineWcCob(wcLoanB);
                    transactionHelper.executeInlineCOB(List.of(loanA, loanB));

                    List<String> expectedBuckets = Stream.of(reportRow("", "-300.00", "0.00", "0.00", "-300.00"),
                            reportRow(originatorX, "-500.00", "0.00", "0.00", "-500.00")).sorted().toList();

                    RunReportsResponse preClose = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2030-12-31", officeId);
                    RunReportsResponse loanPreClose = trialBalanceHelper.runReport(LOAN_REPORT_NAME, "2030-12-31", officeId);
                    assertAll(
                            () -> assertEquals(expectedBuckets, reportRows(preClose, feeIncomeGlCode, wcProductName),
                                    "Pre-close fee income rows per originator bucket for office " + officeId),
                            () -> assertEquals(reportRows(loanPreClose, feeIncomeGlCode, loanProductName),
                                    reportRows(preClose, feeIncomeGlCode, wcProductName),
                                    "Pre-close fee income rows must match the regular loan report"));

                    setBusinessDate("2031-01-02");
                    schedulerHelper.executeAndAwaitJob(RETAINED_EARNING_JOB_NAME);

                    RunReportsResponse postClose = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2031-01-01", officeId);
                    RunReportsResponse loanPostClose = trialBalanceHelper.runReport(LOAN_REPORT_NAME, "2031-01-01", officeId);
                    assertAll(
                            () -> assertEquals(List.of(), reportRows(postClose, feeIncomeGlCode, wcProductName),
                                    "Post-close fee income must be fully closed out in every originator bucket"),
                            () -> assertEquals(expectedBuckets, reportRows(postClose, retainedEarningsGlCode, wcProductName),
                                    "Post-close retained earnings must be carried per originator bucket"),
                            () -> assertEquals(reportRows(loanPostClose, feeIncomeGlCode, loanProductName),
                                    reportRows(postClose, feeIncomeGlCode, wcProductName),
                                    "Post-close fee income rows must match the regular loan report"),
                            () -> assertEquals(reportRows(loanPostClose, retainedEarningsGlCode, loanProductName),
                                    reportRows(postClose, retainedEarningsGlCode, wcProductName),
                                    "Post-close retained earnings rows must match the regular loan report"),
                            () -> assertEquals(List.of(wcProductName), productsOf(postClose),
                                    "Regular loan year-end rows must not show up in the WC report"),
                            () -> assertEquals(List.of(loanProductName), productsOf(loanPostClose),
                                    "Working capital year-end rows must not show up in the regular loan report, even when a WC product id"
                                            + " matches a loan product id"));
                }));
    }

    /**
     * Runs the aggregation job, which advances the tenant-wide aggregation tracking date. The dates are later than
     * every other report date in this class so that the tracking date does not change their opening balances, and the
     * test needs a database where the aggregation has not already been tracked past them.
     */
    @Test
    @Order(6)
    public void testReportIsSameBeforeAndAfterJournalEntryAggregation() {
        Long officeId = officeHelper.createOffice(LocalDate.of(2031, 1, 1)).getOfficeId();
        runAt("2031-06-01", () -> {
            Long clientId = createClientInOffice("01 June 2031", officeId);
            Long productId = createWorkingCapitalProduct(incomeAccount);
            String productName = productName(productId);
            String originatorExternalId = UUID.randomUUID().toString();
            Long loanId = createApproveAndDisburseWcLoan(clientId, productId, BigDecimal.valueOf(10000), "01 June 2031",
                    List.of(createWcOriginator(originatorExternalId)));

            setBusinessDate("2031-06-02");
            makeWcRepayment(loanId, BigDecimal.valueOf(1000), "02 June 2031");
            setBusinessDate("2031-06-04");
            makeWcRepayment(loanId, BigDecimal.valueOf(500), "04 June 2031");

            List<String> endDates = List.of("2031-06-02", "2031-06-04", "2031-06-05");
            Map<String, List<String>> before = productRowsByDate(endDates, officeId, productName);
            assertEquals(List.of(reportRow(originatorExternalId, "9000.00", "0.00", "-500.00", "8500.00")),
                    reportRows(trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, "2031-06-04", officeId),
                            glCode(loanPortfolioAccount), productName));

            // aggregates every journal entry up to 2031-06-03, so the later reports read their opening balance from it
            schedulerHelper.executeAndAwaitJob(JOURNAL_ENTRY_AGGREGATION_JOB_NAME);

            assertEquals(before, productRowsByDate(endDates, officeId, productName));
        });
    }

    private Map<String, List<String>> productRowsByDate(List<String> endDates, Long officeId, String productName) {
        Map<String, List<String>> rowsByDate = new LinkedHashMap<>();
        for (String endDate : endDates) {
            RunReportsResponse report = trialBalanceHelper.runReport(WORKING_CAPITAL_REPORT_NAME, endDate, officeId);
            for (Account account : List.of(loanPortfolioAccount, fundSourceAccount)) {
                rowsByDate.put(endDate + " " + glCode(account), reportRows(report, glCode(account), productName));
            }
        }
        return rowsByDate;
    }

    private void assertBalanceFormula(RunReportsResponse report, String productName) {
        int assetOwnerIdx = findColumnIndex(report, "assetowner");
        int beginBalIdx = findColumnIndex(report, "beginningbalance");
        int debitIdx = findColumnIndex(report, "debitmovement");
        int creditIdx = findColumnIndex(report, "creditmovement");
        int endingBalIdx = findColumnIndex(report, "endingbalance");
        List<List<Object>> rows = rowsOfProduct(report, productName);
        assertFalse(rows.isEmpty(), "Report must contain entries for product '" + productName + "'.");
        for (List<Object> row : rows) {
            assertEquals("self", String.valueOf(row.get(assetOwnerIdx)), "Working capital rows are always owned by 'self': " + row);
            BigDecimal expectedEndingBalance = parseBigDecimal(row.get(beginBalIdx)).add(parseBigDecimal(row.get(debitIdx)))
                    .add(parseBigDecimal(row.get(creditIdx)));
            assertEquals(0, expectedEndingBalance.compareTo(parseBigDecimal(row.get(endingBalIdx))),
                    "Ending balance must equal beginning + debit + credit: " + row);
        }
    }

    private List<String> productsOf(RunReportsResponse report) {
        int productIdx = findColumnIndex(report, "product");
        return report.getData().stream().map(row -> String.valueOf(row.getRow().get(productIdx))).distinct().sorted().toList();
    }

    private String glCode(Account account) {
        return accountHelper.getGlCode(account);
    }

    private Long createClientInOffice(String activationDate, Long officeId) {
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

    private String originatorExternalIdOf(Long originatorId) {
        return loanOriginatorHelper.getOriginatorById(originatorId).getExternalId();
    }

    private Long createWcOriginator(String originatorExternalId) {
        return wcOriginatorHelper.createOriginator(originatorExternalId, "Originator " + originatorExternalId);
    }

    /**
     * Originators can only be attached before the loan is approved.
     */
    private Long createApproveAndDisburseWcLoan(Long clientId, Long productId, BigDecimal principal, String date,
            List<Long> originatorIds) {
        Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal,
                DEFAULT_PERIOD_PAYMENT_RATE, date, date));
        originatorIds.forEach(originatorId -> wcOriginatorHelper.attachOriginatorToWorkingCapitalLoan(loanId, originatorId));
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private void addFee(Long loanId, double amount, String dueDate) {
        Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, amount));
        wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private String productName(Long workingCapitalProductId) {
        return productHelper.retrieveWorkingCapitalLoanProductById(workingCapitalProductId).getName();
    }

    private Long createWorkingCapitalProduct(Account feeIncomeAccount) {
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder()//
                .withName("WC TrialBal " + Utils.uniqueRandomStringGenerator("", 8))//
                .withShortName(Utils.uniqueRandomStringGenerator("", 4))//
                .withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)//
                .withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())//
                .withLoanPortfolioAccountId(loanPortfolioAccount.getAccountID().longValue())//
                .withTransfersInSuspenseAccountId(loanPortfolioAccount.getAccountID().longValue())//
                .withIncomeFromDiscountFeeAccountId(incomeAccount.getAccountID().longValue())//
                .withReceivableFeeAccountId(feesReceivableAccount.getAccountID().longValue())//
                .withReceivablePenaltyAccountId(penaltiesReceivableAccount.getAccountID().longValue())//
                .withIncomeFromFeeAccountId(feeIncomeAccount.getAccountID().longValue())//
                .withIncomeFromPenaltyAccountId(incomeAccount.getAccountID().longValue())//
                .withIncomeFromRecoveryAccountId(incomeAccount.getAccountID().longValue())//
                .withWriteOffAccountId(expenseAccount.getAccountID().longValue())//
                .withOverpaymentLiabilityAccountId(liabilityAccount.getAccountID().longValue())//
                .withDeferredIncomeLiabilityAccountId(liabilityAccount.getAccountID().longValue())//
                .withChargeOffExpenseAccountId(chargeOffExpenseAccount.getAccountID().longValue())//
                .withIncomeFromChargeOffFeesAccountId(chargeOffIncomeAccount.getAccountID().longValue())//
                .withIncomeFromChargeOffPenaltyAccountId(chargeOffIncomeAccount.getAccountID().longValue())//
                .build()).getResourceId();
    }

    private void addLoanFee(Long loanId, double amount, String dueDate) {
        Long chargeId = chargesHelper.createLoanSpecifiedDueDateCharge(amount).getResourceId();
        ok(() -> fineractClient().loanCharges().createOrPayLoanCharge(loanId, new PostLoansLoanIdChargesRequest().chargeId(chargeId)
                .amount(amount).dueDate(dueDate).dateFormat(LoanTestData.DATETIME_PATTERN).locale(LoanTestData.LOCALE), (String) null));
    }

    private Long createAccrualLoanProduct(Account feeIncomeAccount) {
        return loanHelper.createLoanProduct(new PostLoanProductsRequest()//
                .name("WC TrialBal Loan " + Utils.uniqueRandomStringGenerator("", 8))//
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
                .accountingRule(3)//
                .fundSourceAccountId((long) fundSourceAccount.getAccountID())//
                .loanPortfolioAccountId((long) loanPortfolioAccount.getAccountID())//
                .transfersInSuspenseAccountId((long) loanPortfolioAccount.getAccountID())//
                .receivableInterestAccountId((long) feesReceivableAccount.getAccountID())//
                .receivableFeeAccountId((long) feesReceivableAccount.getAccountID())//
                .receivablePenaltyAccountId((long) penaltiesReceivableAccount.getAccountID())//
                .interestOnLoanAccountId((long) incomeAccount.getAccountID())//
                .incomeFromFeeAccountId((long) feeIncomeAccount.getAccountID())//
                .incomeFromPenaltyAccountId((long) incomeAccount.getAccountID())//
                .incomeFromRecoveryAccountId((long) incomeAccount.getAccountID())//
                .writeOffAccountId((long) expenseAccount.getAccountID())//
                .overpaymentLiabilityAccountId((long) liabilityAccount.getAccountID())//
                .chargeOffExpenseAccountId((long) chargeOffExpenseAccount.getAccountID())//
                .chargeOffFraudExpenseAccountId((long) chargeOffExpenseAccount.getAccountID())//
                .incomeFromChargeOffInterestAccountId((long) chargeOffIncomeAccount.getAccountID())//
                .incomeFromChargeOffFeesAccountId((long) chargeOffIncomeAccount.getAccountID())//
                .incomeFromChargeOffPenaltyAccountId((long) chargeOffIncomeAccount.getAccountID())//
                .locale(LoanTestData.LOCALE)//
                .dateFormat(LoanTestData.DATETIME_PATTERN)).getResourceId();
    }

    private Long createAndDisburseLoan(Long clientId, Long productId, String date, List<Long> originatorIds) {
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
        originatorIds.forEach(originatorId -> loanOriginatorHelper.attachOriginatorToLoan(loanId, originatorId));
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
}
