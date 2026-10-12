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
package org.apache.fineract.infrastructure.jobs.service.retainedearning;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.accounting.retainedearning.domain.AccountGLJournalEntryAnnualSummaryRepository;
import org.apache.fineract.infrastructure.core.service.ExternalIdFactory;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.data.AccountGLJournalEntryAnnualSummaryData;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.helper.DataParser;
import org.apache.fineract.infrastructure.jobs.service.retainedearning.services.RetainedEarningDataServiceImpl;
import org.apache.fineract.infrastructure.report.service.ReportingProcessService;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

/**
 * End-to-end scenario test that validates retained earning calculation against the sample report from the Confluence
 * product requirements page.
 *
 * Sample data structure from "DE PayIn30 TB 1-1-24.csv": postingdate, product, glacct, description, assetowner,
 * beginningbalance, debitmovement, creditmovement, endingbalance
 *
 * The test simulates: 1. Trial balance data for 12/31/2023 with mixed account types 2. Income/expense accounts
 * (400000-899999 range) that need year-end closing 3. Balance sheet accounts (outside range) that should be untouched
 * 4. Expected retained earning calculation at GL 320000
 *
 * Requirements verified: - Income/expense accounts ending balances sum to retained earnings - Retained earnings GL
 * account = 320000 - Balance sheet accounts (e.g., 112601) are excluded from processing - Each asset owner gets their
 * own retained earning record - Zero-balance owners don't get retained earning records
 */
@ExtendWith(MockitoExtension.class)
class RetainedEarningScenarioTest {

    @Mock
    private ReportingProcessService reportingProcessService;

    @Mock
    private DataParser dataParser;

    @Mock
    private AccountGLJournalEntryAnnualSummaryRepository retainedEarningSummaryRepository;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private RetainedEarningConfigurationService retainedEarningConfigurationService;

    @InjectMocks
    private RetainedEarningDataServiceImpl retainedEarningDataService;

    @Mock
    private LoanProduct loanProduct;

    private static final LocalDate FISCAL_YEAR_END = LocalDate.of(2023, 12, 31);
    private static final String RETAINED_EARNING_GL = "320000";

    private void setupConfigMocks() {
        when(retainedEarningConfigurationService.getIncomeExpenseGlAccounts()).thenReturn("400000-899999");
        when(retainedEarningConfigurationService.getRetainedEarningGlAccount()).thenReturn(RETAINED_EARNING_GL);
        when(loanProductRepository.findAllByNameIgnoreCase(any())).thenReturn(List.of(loanProduct));
        when(loanProduct.getId()).thenReturn(1L);
        when(loanProduct.getName()).thenReturn("GPL_DE_PI30");
        when(loanProduct.getCurrency()).thenReturn(new org.apache.fineract.organisation.monetary.domain.MonetaryCurrency("EUR", 2, null));
    }

    /**
     * Simulates the actual scenario from the Confluence sample report: - GPL_DE_PI30 product with multiple GL accounts
     * - Two asset owners: "10001" (external) and "self" - Mix of balance sheet (112601, 112603) and income/expense
     * (401001, 401002, 801001, 801002) accounts - Verifies retained earnings are calculated correctly per owner
     */
    @Test
    void shouldCalculateRetainedEarningsMatchingSampleReport() {
        List<AccountGLJournalEntryAnnualSummaryData> trialBalanceData = buildSampleTrialBalanceData();
        setupConfigMocks();

        List<AccountGLJournalEntryAnnualSummaryData> results = retainedEarningDataService.processTrialBalanceData(trialBalanceData,
                FISCAL_YEAR_END);

        assertFalse(results.isEmpty(), "Should produce results");

        List<AccountGLJournalEntryAnnualSummaryData> incomeExpenseResults = results.stream()
                .filter(r -> !r.getGlAccountCode().equals(RETAINED_EARNING_GL)).toList();
        List<AccountGLJournalEntryAnnualSummaryData> retainedEarningResults = results.stream()
                .filter(r -> r.getGlAccountCode().equals(RETAINED_EARNING_GL)).toList();

        // Balance sheet accounts (112601, 112603) are NOT in results
        assertTrue(results.stream().noneMatch(r -> r.getGlAccountCode().equals("112601")),
                "Balance sheet account 112601 should be excluded");
        assertTrue(results.stream().noneMatch(r -> r.getGlAccountCode().equals("112603")),
                "Balance sheet account 112603 should be excluded");

        assertEquals(4, incomeExpenseResults.size(), "Should have 4 income/expense records");
        assertEquals(2, retainedEarningResults.size(), "Should have 2 retained earning records (one per owner)");

        // Owner "10001": 401001(-150,000) + 801001(50,000) = -100,000
        AccountGLJournalEntryAnnualSummaryData owner10001RE = retainedEarningResults.stream()
                .filter(r -> ExternalIdFactory.produce("10001").equals(r.getOwnerExternalId())).findFirst().orElse(null);
        assertNotNull(owner10001RE, "Should have retained earning for owner 10001");
        assertEquals(RETAINED_EARNING_GL, owner10001RE.getGlAccountCode());
        assertEquals(0, new BigDecimal("-100000.00").compareTo(owner10001RE.getOpeningBalanceAmount()),
                "Owner 10001 retained earning should be -100,000.00");
        assertEquals(FISCAL_YEAR_END, owner10001RE.getYearEndDate());
        assertFalse(owner10001RE.getManualEntry());

        // Owner "self": 401002(-200,000) + 801002(75,000) = -125,000
        AccountGLJournalEntryAnnualSummaryData ownerSelfRE = retainedEarningResults.stream()
                .filter(r -> ExternalIdFactory.produce("self").equals(r.getOwnerExternalId())).findFirst().orElse(null);
        assertNotNull(ownerSelfRE, "Should have retained earning for owner self");
        assertEquals(RETAINED_EARNING_GL, ownerSelfRE.getGlAccountCode());
        assertEquals(0, new BigDecimal("-125000.00").compareTo(ownerSelfRE.getOpeningBalanceAmount()),
                "Owner self retained earning should be -125,000.00");

        // All records have product info populated
        for (AccountGLJournalEntryAnnualSummaryData result : results) {
            assertEquals(1L, result.getProductId(), "All records should have product ID set");
            assertEquals("EUR", result.getCurrencyCode(), "All records should have currency code set");
        }
    }

    @Test
    void shouldNotCreateRetainedEarningWhenNetBalanceIsZero() {
        List<AccountGLJournalEntryAnnualSummaryData> trialBalanceData = new ArrayList<>();
        trialBalanceData.add(buildRecord("401001", "10001", new BigDecimal("-500.00")));
        trialBalanceData.add(buildRecord("801001", "10001", new BigDecimal("500.00")));

        setupConfigMocks();

        List<AccountGLJournalEntryAnnualSummaryData> results = retainedEarningDataService.processTrialBalanceData(trialBalanceData,
                FISCAL_YEAR_END);

        assertEquals(2, results.size(), "Should only have income/expense records, no retained earnings");
        assertTrue(results.stream().noneMatch(rec -> rec.getGlAccountCode().equals(RETAINED_EARNING_GL)),
                "No retained earning record should be created for zero net balance");
    }

    @Test
    void shouldParseTrialBalanceReportJsonFormat() throws Exception {
        DataParser parser = new DataParser();

        String reportJson = """
                {
                  "columnHeaders": [
                    {"columnName": "postingdate", "columnType": "DATE"},
                    {"columnName": "product", "columnType": "VARCHAR"},
                    {"columnName": "glacct", "columnType": "VARCHAR"},
                    {"columnName": "description", "columnType": "VARCHAR"},
                    {"columnName": "assetowner", "columnType": "VARCHAR"},
                    {"columnName": "beginningbalance", "columnType": "DECIMAL"},
                    {"columnName": "debitmovement", "columnType": "DECIMAL"},
                    {"columnName": "creditmovement", "columnType": "DECIMAL"},
                    {"columnName": "endingbalance", "columnType": "DECIMAL"}
                  ],
                  "data": [
                    {"row": ["2023-12-31", "GPL_DE_PI30", "112601", "Loans Receivable", "10001", "467059174.32", "8006.88", "-15241427.80", "451825753.40"]},
                    {"row": ["2023-12-31", "GPL_DE_PI30", "401001", "Fee Income", "10001", "0.00", "1000.00", "-151000.00", "-150000.00"]},
                    {"row": ["2023-12-31", "GPL_DE_PI30", "801001", "Interest Expense", "10001", "0.00", "55000.00", "-5000.00", "50000.00"]},
                    {"row": ["2023-12-31", "GPL_DE_PI30", "401002", "Service Fee Income", "self", "0.00", "500.00", "-200500.00", "-200000.00"]},
                    {"row": ["2023-12-31", "GPL_DE_PI30", "801002", "Operating Expense", "self", "0.00", "80000.00", "-5000.00", "75000.00"]}
                  ]
                }
                """;

        var records = parser.parse(reportJson);

        assertEquals(5, records.size());
        assertEquals("2023-12-31", records.get(0).getPostingDate());
        assertEquals("GPL_DE_PI30", records.get(0).getProduct());
        assertEquals("112601", records.get(0).getGlAcct());
        assertEquals(ExternalIdFactory.produce("10001"), records.get(0).getAssetOwner());
        assertEquals(new BigDecimal("451825753.40"), records.get(0).getEndingBalance());
        assertEquals("401001", records.get(1).getGlAcct());
        assertEquals(new BigDecimal("-150000.00"), records.get(1).getEndingBalance());
        assertEquals("801001", records.get(2).getGlAcct());
        assertEquals(new BigDecimal("50000.00"), records.get(2).getEndingBalance());
    }

    @Test
    void shouldMapRetainedEarningToEntityCorrectly() {
        AccountGLJournalEntryAnnualSummaryData retainedEarning = AccountGLJournalEntryAnnualSummaryData.builder()
                .glAccountCode(RETAINED_EARNING_GL).productId(1L).productName("GPL_DE_PI30").officeId(1L)
                .ownerExternalId(ExternalIdFactory.produce("10001")).openingBalanceAmount(new BigDecimal("-100000.00"))
                .endingBalanceAmount(new BigDecimal("-100000.00")).yearEndDate(FISCAL_YEAR_END).currencyCode("EUR").manualEntry(false)
                .build();

        assertEquals(RETAINED_EARNING_GL, retainedEarning.getGlAccountCode());
        assertEquals(1L, retainedEarning.getProductId());
        assertEquals("GPL_DE_PI30", retainedEarning.getProductName());
        assertEquals(1L, retainedEarning.getOfficeId());
        assertEquals(ExternalIdFactory.produce("10001"), retainedEarning.getOwnerExternalId());
        assertEquals(new BigDecimal("-100000.00"), retainedEarning.getOpeningBalanceAmount());
        assertEquals(new BigDecimal("-100000.00"), retainedEarning.getEndingBalanceAmount());
        assertEquals(FISCAL_YEAR_END, retainedEarning.getYearEndDate());
        assertEquals("EUR", retainedEarning.getCurrencyCode());
        assertFalse(retainedEarning.getManualEntry());
    }

    private List<AccountGLJournalEntryAnnualSummaryData> buildSampleTrialBalanceData() {
        List<AccountGLJournalEntryAnnualSummaryData> data = new ArrayList<>();
        data.add(buildRecord("112601", "10001", new BigDecimal("451825753.40")));
        data.add(buildRecord("112601", "self", new BigDecimal("247629764.29")));
        data.add(buildRecord("112603", "10001", new BigDecimal("5000000.00")));
        data.add(buildRecord("401001", "10001", new BigDecimal("-150000.00")));
        data.add(buildRecord("401002", "self", new BigDecimal("-200000.00")));
        data.add(buildRecord("801001", "10001", new BigDecimal("50000.00")));
        data.add(buildRecord("801002", "self", new BigDecimal("75000.00")));
        return data;
    }

    private AccountGLJournalEntryAnnualSummaryData buildRecord(String glAccountId, String ownerExternalId, BigDecimal endingBalance) {
        return AccountGLJournalEntryAnnualSummaryData.builder().glAccountCode(glAccountId).productName("GPL_DE_PI30").officeId(1L)
                .ownerExternalId(ExternalIdFactory.produce(ownerExternalId)).openingBalanceAmount(BigDecimal.ZERO)
                .endingBalanceAmount(endingBalance).yearEndDate(FISCAL_YEAR_END).manualEntry(false).build();
    }
}
