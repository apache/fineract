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
package org.apache.fineract.integrationtests.client.feign.helpers;

import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.GetGLAccountsResponse;
import org.apache.fineract.client.models.GlobalConfigurationPropertyData;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;

/**
 * Runs the trial balance summary reports and configures the year-end retained earning job that reads them back. Report
 * rows are compared as canonical strings, see {@link #reportRows} and {@link #reportRow}.
 */
public class FeignTrialBalanceReportHelper {

    public static final String LOAN_REPORT_NAME = "Trial Balance Summary Report with Asset Owner";
    public static final String WORKING_CAPITAL_REPORT_NAME = "Trial Balance Summary Report for Working Capital Loans";
    public static final String RETAINED_EARNING_JOB_NAME = "Retained Earning Job";

    private static final String RETAINED_EARNINGS_ACCOUNT_NAME = "Retained Earnings Prior Year";
    private static final String INCOME_EXPENSE_GL_ACCOUNTS = "income-expense-gl-accounts";
    private static final String RETAINED_GL_ACCOUNT = "retained-gl-account";
    private static final String RETAINED_EARNING_REPORT_NAME = "retained-earning-used-by-report-name";
    private static final String RETAINED_EARNING_WC_REPORT_NAME = "retained-earning-wc-used-by-report-name";
    private static final String OFFICE_ID = "office-id";
    private static final String LAST_DAY_OF_FINANCIAL_YEAR = "last-day-of-financial-year";
    private static final String LAST_MONTH_OF_FINANCIAL_YEAR = "last-month-of-financial-year";
    private static final List<String> STRING_CONFIGS = List.of(INCOME_EXPENSE_GL_ACCOUNTS, RETAINED_GL_ACCOUNT,
            RETAINED_EARNING_REPORT_NAME, RETAINED_EARNING_WC_REPORT_NAME);
    private static final List<String> NUMERIC_CONFIGS = List.of(OFFICE_ID, LAST_DAY_OF_FINANCIAL_YEAR, LAST_MONTH_OF_FINANCIAL_YEAR);

    private final FeignReportHelper reportHelper;
    private final FeignAccountHelper accountHelper;
    private final FeignGlobalConfigurationHelper globalConfigurationHelper;

    public FeignTrialBalanceReportHelper(FineractFeignClient fineractClient) {
        this.reportHelper = new FeignReportHelper(fineractClient);
        this.accountHelper = new FeignAccountHelper(fineractClient);
        this.globalConfigurationHelper = new FeignGlobalConfigurationHelper(fineractClient);
    }

    public RunReportsResponse runReport(String reportName, String endDate, Long officeId) {
        return reportHelper.runReport(reportName, Map.of("R_endDate", endDate, "R_officeId", String.valueOf(officeId)));
    }

    /**
     * Returns the GL code of the "Retained Earnings Prior Year" account the reports look up by name, creating it when
     * it does not exist yet.
     */
    public String findOrCreateRetainedEarningsAccount() {
        List<GetGLAccountsResponse> existingAccounts = accountHelper.findGLAccountsByName(RETAINED_EARNINGS_ACCOUNT_NAME);
        assertTrue(existingAccounts.size() <= 1,
                "The report needs a unique '" + RETAINED_EARNINGS_ACCOUNT_NAME + "' account: " + existingAccounts);
        if (existingAccounts.isEmpty()) {
            return accountHelper.createEquityAccountWithExactName(RETAINED_EARNINGS_ACCOUNT_NAME).getGlCode();
        }
        return existingAccounts.get(0).getGlCode();
    }

    /**
     * Configures the retained earning job to close the given income/expense GL accounts of one office into the retained
     * earnings account at the end of the calendar year, runs the action and restores the previous configuration.
     */
    public void runWithRetainedEarningConfiguration(String incomeExpenseGlAccounts, String retainedEarningsGlCode, Long officeId,
            Runnable action) {
        Map<String, GlobalConfigurationPropertyData> originalConfigs = snapshotRetainedEarningConfigs();
        try {
            globalConfigurationHelper.updateConfigurationStringValue(INCOME_EXPENSE_GL_ACCOUNTS, incomeExpenseGlAccounts);
            globalConfigurationHelper.updateConfigurationStringValue(RETAINED_GL_ACCOUNT, retainedEarningsGlCode);
            globalConfigurationHelper.updateConfigurationStringValue(RETAINED_EARNING_REPORT_NAME, LOAN_REPORT_NAME);
            globalConfigurationHelper.updateConfigurationStringValue(RETAINED_EARNING_WC_REPORT_NAME, WORKING_CAPITAL_REPORT_NAME);
            globalConfigurationHelper.updateGlobalConfigurationInternal(OFFICE_ID, officeId);
            globalConfigurationHelper.updateGlobalConfigurationInternal(LAST_DAY_OF_FINANCIAL_YEAR, 31L);
            globalConfigurationHelper.updateGlobalConfigurationInternal(LAST_MONTH_OF_FINANCIAL_YEAR, 12L);
            action.run();
        } finally {
            restoreRetainedEarningConfigs(originalConfigs);
        }
    }

    private Map<String, GlobalConfigurationPropertyData> snapshotRetainedEarningConfigs() {
        Map<String, GlobalConfigurationPropertyData> snapshot = new LinkedHashMap<>();
        Stream.concat(STRING_CONFIGS.stream(), NUMERIC_CONFIGS.stream())
                .forEach(name -> snapshot.put(name, globalConfigurationHelper.getGlobalConfigurationByName(name)));
        return snapshot;
    }

    private void restoreRetainedEarningConfigs(Map<String, GlobalConfigurationPropertyData> snapshot) {
        for (String name : STRING_CONFIGS) {
            String original = snapshot.get(name).getStringValue();
            // the configuration API rejects a blank stringValue, so a blank default cannot be written back
            if (original != null && !original.isBlank()) {
                globalConfigurationHelper.updateConfigurationStringValue(name, original);
            }
        }
        for (String name : NUMERIC_CONFIGS) {
            globalConfigurationHelper.updateGlobalConfigurationInternal(name, snapshot.get(name).getValue());
        }
    }

    public static List<List<Object>> rowsOfProduct(RunReportsResponse report, String productName) {
        int productIdx = findColumnIndex(report, "product");
        return report.getData().stream().map(ResultsetRowData::getRow)
                .filter(row -> productName.equals(String.valueOf(row.get(productIdx)))).toList();
    }

    /**
     * Returns the rows of one product and GL account, each as a canonical sorted string comparable with
     * {@link #reportRow}.
     */
    public static List<String> reportRows(RunReportsResponse report, String glCode, String productName) {
        int glAcctIdx = findColumnIndex(report, "glacct");
        int assetOwnerIdx = findColumnIndex(report, "assetowner");
        int originatorIdx = findColumnIndex(report, "originator_external_ids");
        int beginBalIdx = findColumnIndex(report, "beginningbalance");
        int debitIdx = findColumnIndex(report, "debitmovement");
        int creditIdx = findColumnIndex(report, "creditmovement");
        int endingBalIdx = findColumnIndex(report, "endingbalance");
        return rowsOfProduct(report, productName).stream().filter(row -> glCode.equals(String.valueOf(row.get(glAcctIdx))))
                .map(row -> "originator=\"" + row.get(originatorIdx) + "\" assetowner=" + row.get(assetOwnerIdx) + " beginningbalance="
                        + money(row.get(beginBalIdx)) + " debitmovement=" + money(row.get(debitIdx)) + " creditmovement="
                        + money(row.get(creditIdx)) + " endingbalance=" + money(row.get(endingBalIdx)))
                .sorted().toList();
    }

    /**
     * Builds the expected canonical string of a row owned by {@code self}; amounts are given with two decimals.
     */
    public static String reportRow(String originator, String beginningBalance, String debitMovement, String creditMovement,
            String endingBalance) {
        return "originator=\"" + originator + "\" assetowner=self beginningbalance=" + beginningBalance + " debitmovement=" + debitMovement
                + " creditmovement=" + creditMovement + " endingbalance=" + endingBalance;
    }

    public static int findColumnIndex(RunReportsResponse report, String columnName) {
        List<ResultsetColumnHeaderData> headers = report.getColumnHeaders();
        for (int i = 0; i < headers.size(); i++) {
            if (columnName.equals(headers.get(i).getColumnName())) {
                return i;
            }
        }
        throw new IllegalArgumentException("Column '" + columnName + "' not found. Available: "
                + headers.stream().map(ResultsetColumnHeaderData::getColumnName).toList());
    }

    public static BigDecimal parseBigDecimal(Object value) {
        if (value == null) {
            return BigDecimal.ZERO;
        }
        String str = String.valueOf(value);
        if (str.isEmpty() || "null".equalsIgnoreCase(str)) {
            return BigDecimal.ZERO;
        }
        return new BigDecimal(str);
    }

    private static String money(Object value) {
        return parseBigDecimal(value).setScale(2, RoundingMode.HALF_UP).toPlainString();
    }
}
