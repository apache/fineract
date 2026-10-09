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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignReportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanTransactionSummaryReportTest {

    private static final String TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report with Asset Owner for Working Capital Loans";
    private static final String REPORT_DATE = "2026-03-02";
    private static final String COB_BUSINESS_DATE = "2026-03-03";
    private static final String TRANSACTION_DATE = "02 March 2026";
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2026";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2026, 1, 1);
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(5000);
    private static final double PENALTY_AMOUNT = 10.0;
    private static final BigDecimal PARTIAL_REPAYMENT_AMOUNT = BigDecimal.valueOf(5);

    private static final int REPORT_COLUMN_COUNT = 12;
    private static final int TRANSACTION_DATE_COLUMN = 0;
    private static final int PRODUCT_NAME_COLUMN = 1;
    private static final int TRANSACTION_TYPE_COLUMN = 2;
    private static final int PAYMENT_TYPE_COLUMN = 3;
    private static final int CHARGE_TYPE_COLUMN = 4;
    private static final int REVERSED_COLUMN = 5;
    private static final int ALLOCATION_TYPE_COLUMN = 6;
    private static final int CHARGE_OFF_REASON_COLUMN = 7;
    private static final int AMOUNT_COLUMN = 8;
    private static final int ASSET_OWNER_ID_COLUMN = 9;
    private static final int FROM_ASSET_OWNER_ID_COLUMN = 10;
    private static final int ORIGINATOR_EXTERNAL_IDS_COLUMN = 11;

    private static final String APPLY_CHARGES_TRANSACTION = "Apply Charges";
    private static final String DISBURSEMENT_TRANSACTION = "Disbursement";
    private static final String REPAYMENT_TRANSACTION = "Repayment";
    private static final String PRINCIPAL_ALLOCATION = "Principal";
    private static final String INTEREST_ALLOCATION = "Interest";
    private static final String FEES_ALLOCATION = "Fees";
    private static final String PENALTY_ALLOCATION = "Penalty";
    private static final String UNALLOCATED_CREDIT_ALLOCATION = "Unallocated Credit (UNC)";
    private static final String NO_CHARGE_TYPE = "";
    private static final double AMOUNT_TOLERANCE = 0.01;

    private static FeignOfficeHelper officeHelper;
    private static FeignClientHelper clientHelper;
    private static FeignBusinessDateHelper businessDateHelper;
    private static FeignWorkingCapitalLoanHelper wcLoanHelper;
    private static FeignReportHelper reportHelper;
    private static WorkingCapitalLoanProductHelper productHelper;

    private static Account fundSourceAccount;
    private static Account loanPortfolioAccount;
    private static Account transfersSuspenseAccount;
    private static Account incomeFromDiscountFeeAccount;
    private static Account feesReceivableAccount;
    private static Account penaltiesReceivableAccount;
    private static Account incomeFromFeeAccount;
    private static Account incomeFromPenaltyAccount;
    private static Account incomeFromRecoveryAccount;
    private static Account writeOffAccount;
    private static Account overpaymentAccount;
    private static Account deferredIncomeAccount;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final Map<Long, Long> repaymentIdByLoanId = new HashMap<>();

    @BeforeAll
    public static void setup() {
        final FineractFeignClient fineractClient = FineractFeignClientHelper.getFineractFeignClient();
        officeHelper = new FeignOfficeHelper(fineractClient);
        clientHelper = new FeignClientHelper(fineractClient);
        businessDateHelper = new FeignBusinessDateHelper(fineractClient);
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient);
        reportHelper = new FeignReportHelper(fineractClient);
        productHelper = new WorkingCapitalLoanProductHelper();

        final FeignAccountHelper accountHelper = new FeignAccountHelper(fineractClient);
        fundSourceAccount = accountHelper.createLiabilityAccount("wcTsrFundSource");
        loanPortfolioAccount = accountHelper.createAssetAccount("wcTsrLoanPortfolio");
        transfersSuspenseAccount = accountHelper.createAssetAccount("wcTsrTransfersSuspense");
        incomeFromDiscountFeeAccount = accountHelper.createIncomeAccount("wcTsrIncomeDiscountFee");
        feesReceivableAccount = accountHelper.createAssetAccount("wcTsrFeesReceivable");
        penaltiesReceivableAccount = accountHelper.createAssetAccount("wcTsrPenaltiesReceivable");
        incomeFromFeeAccount = accountHelper.createIncomeAccount("wcTsrIncomeFee");
        incomeFromPenaltyAccount = accountHelper.createIncomeAccount("wcTsrIncomePenalty");
        incomeFromRecoveryAccount = accountHelper.createIncomeAccount("wcTsrIncomeRecovery");
        writeOffAccount = accountHelper.createExpenseAccount("wcTsrWriteOff");
        overpaymentAccount = accountHelper.createLiabilityAccount("wcTsrOverpayment");
        deferredIncomeAccount = accountHelper.createLiabilityAccount("wcTsrDeferredIncome");
    }

    @AfterEach
    void cleanupLoans() {
        repaymentIdByLoanId.forEach(wcLoanHelper::undoTransaction);
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        repaymentIdByLoanId.clear();
        createdLoanIds.clear();
    }

    @Test
    public void transactionSummaryReportWithAssetOwner() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct(productName);
        final AtomicLong officeId = new AtomicLong();
        final AtomicReference<String> penaltyName = new AtomicReference<>();

        businessDateHelper.runAt(REPORT_DATE, () -> {

            final Long createdOfficeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
            assertNotNull(createdOfficeId);
            officeId.set(createdOfficeId);
            final Long clientId = createClientInOffice(officeId.get());
            final Long loanId = createApprovedAndDisbursedLoan(clientId, productId);
            penaltyName.set(addPenaltyForLoan(loanId));
            makePartialRepayment(loanId);

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", COB_BUSINESS_DATE);
            wcLoanHelper.executeInlineWCCOB(loanId);
        });

        final RunReportsResponse report = runTransactionSummaryReport(REPORT_DATE, officeId.get());

        final List<ResultsetColumnHeaderData> columnHeaders = report.getColumnHeaders();
        assertNotNull(columnHeaders);
        assertEquals(REPORT_COLUMN_COUNT, columnHeaders.size());
        columnHeaders.forEach(columnHeader -> {
            assertNotNull(columnHeader.getColumnType());
            assertNotNull(columnHeader.getColumnDisplayType());
            final Boolean columnNullable = columnHeader.getIsColumnNullable();
            assertNotNull(columnNullable);
            assertFalse(columnNullable);
        });

        final List<ResultsetRowData> reportData = report.getData();
        assertNotNull(reportData);
        final List<List<Object>> rows = reportData.stream().map(ResultsetRowData::getRow).toList();
        assertEquals(12, rows.size());
        assertReportRow(rows.get(0), productName, APPLY_CHARGES_TRANSACTION, NO_CHARGE_TYPE, INTEREST_ALLOCATION, 0.00);
        assertReportRow(rows.get(1), productName, APPLY_CHARGES_TRANSACTION, penaltyName.get(), PENALTY_ALLOCATION, 10.00);
        assertReportRow(rows.get(2), productName, DISBURSEMENT_TRANSACTION, NO_CHARGE_TYPE, FEES_ALLOCATION, 0.00);
        assertReportRow(rows.get(3), productName, DISBURSEMENT_TRANSACTION, NO_CHARGE_TYPE, INTEREST_ALLOCATION, 0.00);
        assertReportRow(rows.get(4), productName, DISBURSEMENT_TRANSACTION, NO_CHARGE_TYPE, PENALTY_ALLOCATION, 0.00);
        assertReportRow(rows.get(5), productName, DISBURSEMENT_TRANSACTION, NO_CHARGE_TYPE, PRINCIPAL_ALLOCATION, 5000.00);
        assertReportRow(rows.get(6), productName, DISBURSEMENT_TRANSACTION, NO_CHARGE_TYPE, UNALLOCATED_CREDIT_ALLOCATION, 0.00);
        assertReportRow(rows.get(7), productName, REPAYMENT_TRANSACTION, NO_CHARGE_TYPE, FEES_ALLOCATION, 0.00);
        assertReportRow(rows.get(8), productName, REPAYMENT_TRANSACTION, NO_CHARGE_TYPE, INTEREST_ALLOCATION, 0.00);
        assertReportRow(rows.get(9), productName, REPAYMENT_TRANSACTION, NO_CHARGE_TYPE, PENALTY_ALLOCATION, -5.00);
        assertReportRow(rows.get(10), productName, REPAYMENT_TRANSACTION, NO_CHARGE_TYPE, PRINCIPAL_ALLOCATION, 0.00);
        assertReportRow(rows.get(11), productName, REPAYMENT_TRANSACTION, NO_CHARGE_TYPE, UNALLOCATED_CREDIT_ALLOCATION, 0.00);
    }

    private Long createAccrualWithDeferredRevenueAmortizationProduct(final String productName) {
        return productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(productName)
                .withShortName(Utils.uniqueRandomStringGenerator("", 4)).withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
                .withFundSourceAccountId(fundSourceAccount.getAccountID().longValue())
                .withLoanPortfolioAccountId(loanPortfolioAccount.getAccountID().longValue())
                .withTransfersInSuspenseAccountId(transfersSuspenseAccount.getAccountID().longValue())
                .withIncomeFromDiscountFeeAccountId(incomeFromDiscountFeeAccount.getAccountID().longValue())
                .withReceivableFeeAccountId(feesReceivableAccount.getAccountID().longValue())
                .withReceivablePenaltyAccountId(penaltiesReceivableAccount.getAccountID().longValue())
                .withIncomeFromFeeAccountId(incomeFromFeeAccount.getAccountID().longValue())
                .withIncomeFromPenaltyAccountId(incomeFromPenaltyAccount.getAccountID().longValue())
                .withIncomeFromRecoveryAccountId(incomeFromRecoveryAccount.getAccountID().longValue())
                .withWriteOffAccountId(writeOffAccount.getAccountID().longValue())
                .withOverpaymentLiabilityAccountId(overpaymentAccount.getAccountID().longValue())
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue()).build()).getResourceId();
    }

    private Long createClientInOffice(final Long officeId) {
        return clientHelper.createClient(ClientRequestBuilders.createActivePersonClient(CLIENT_ACTIVATION_DATE).officeId(officeId))
                .getClientId();
    }

    private Long createApprovedAndDisbursedLoan(final Long clientId, final Long productId) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                PRINCIPAL, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, TRANSACTION_DATE, TRANSACTION_DATE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(TRANSACTION_DATE, PRINCIPAL, TRANSACTION_DATE));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(TRANSACTION_DATE, PRINCIPAL));
        return loanId;
    }

    private String addPenaltyForLoan(final Long loanId) {
        final ChargeRequest penalty = WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(true, PENALTY_AMOUNT);
        final Long chargeId = wcLoanHelper.createGlobalCharge(penalty);
        wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, PENALTY_AMOUNT, TRANSACTION_DATE));
        return penalty.getName();
    }

    private void makePartialRepayment(final Long loanId) {
        repaymentIdByLoanId.put(loanId, wcLoanHelper.makeRepayment(loanId,
                WorkingCapitalLoanRequestBuilders.repayment(PARTIAL_REPAYMENT_AMOUNT, TRANSACTION_DATE)));
    }

    private RunReportsResponse runTransactionSummaryReport(final String endDate, final Long officeId) {
        return reportHelper.runReport(TRANSACTION_SUMMARY_REPORT,
                Map.of("R_endDate", endDate, "R_officeId", officeId.toString(), "output-type", "CSV"));
    }

    private void assertReportRow(final List<Object> row, final String productName, final String transactionType, final String chargeType,
            final String allocationType, final double amount) {
        assertEquals(REPORT_DATE, row.get(TRANSACTION_DATE_COLUMN));
        assertEquals(productName, row.get(PRODUCT_NAME_COLUMN));
        assertEquals(transactionType, row.get(TRANSACTION_TYPE_COLUMN));
        assertNull(row.get(PAYMENT_TYPE_COLUMN));
        assertEquals(chargeType, row.get(CHARGE_TYPE_COLUMN));
        assertNotReversed(row.get(REVERSED_COLUMN));
        assertEquals(allocationType, row.get(ALLOCATION_TYPE_COLUMN));
        assertNull(row.get(CHARGE_OFF_REASON_COLUMN));
        assertEquals(amount, ((Number) row.get(AMOUNT_COLUMN)).doubleValue(), AMOUNT_TOLERANCE);
        assertNull(row.get(ASSET_OWNER_ID_COLUMN));
        assertNull(row.get(FROM_ASSET_OWNER_ID_COLUMN));
        assertNull(row.get(ORIGINATOR_EXTERNAL_IDS_COLUMN));
    }

    private void assertNotReversed(final Object reversed) {
        final boolean notReversed = reversed instanceof Number number ? number.intValue() == 0 : Boolean.FALSE.equals(reversed);
        assertTrue(notReversed, "Expected a non-reversed transaction, but the report reported reversed=" + reversed);
    }
}
