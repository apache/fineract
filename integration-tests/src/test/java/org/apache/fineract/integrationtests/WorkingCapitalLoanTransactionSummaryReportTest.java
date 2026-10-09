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

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.FineractFeignClient;
import org.apache.fineract.client.models.ChargeRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoanTransactionIdResponse;
import org.apache.fineract.client.models.PaymentTypeCreateRequest;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostWorkingCapitalLoanProductsRequest.AccountingRuleEnum;
import org.apache.fineract.client.models.ResultsetColumnHeaderData;
import org.apache.fineract.client.models.ResultsetRowData;
import org.apache.fineract.client.models.RunReportsResponse;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignAccountHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignChargesHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignCodeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignLoanHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignOfficeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignPaymentTypeHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignReportHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignTransactionHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.ClientRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestAccounts;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.TransactionProcessingStrategyCode;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class WorkingCapitalLoanTransactionSummaryReportTest {

    private static final String TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report with Asset Owner for Working Capital Loans";
    private static final String REGULAR_TRANSACTION_SUMMARY_REPORT = "Transaction Summary Report with Asset Owner";
    private static final String REPORT_DATE = "2026-03-02";
    private static final String COB_BUSINESS_DATE = "2026-03-03";
    private static final String TRANSACTION_DATE = "02 March 2026";
    private static final String CLIENT_ACTIVATION_DATE = "01 January 2026";
    private static final LocalDate OFFICE_OPENING_DATE = LocalDate.of(2026, 1, 1);
    private static final DateTimeFormatter API_DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy", Locale.ENGLISH);
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(5000);
    private static final BigDecimal LOAN_PRINCIPAL = BigDecimal.valueOf(9000);
    private static final BigDecimal SMALL_LOAN_PRINCIPAL = BigDecimal.valueOf(1000);
    private static final BigDecimal DISCOUNT_FEE = BigDecimal.valueOf(1000);
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
    private static final String WRITE_OFF_TRANSACTION = "Write-Off";
    private static final String RECOVERY_REPAYMENT_TRANSACTION = "Recovery Repayment";
    private static final String WAIVE_CHARGES_TRANSACTION = "Waive Charges";
    private static final String CREDIT_BALANCE_REFUND_TRANSACTION = "Credit Balance Refund";
    private static final String PAYOUT_REFUND_TRANSACTION = "Payout Refund";
    private static final String GOODWILL_CREDIT_TRANSACTION = "Goodwill Credit";
    private static final String CHARGE_ADJUSTMENT_TRANSACTION = "Charge Adjustment";
    private static final String CHARGE_OFF_TRANSACTION = "Charge-off";
    private static final String DISCOUNT_FEE_TRANSACTION = "Discount Fee";
    private static final String DISCOUNT_FEE_AMORTIZATION_TRANSACTION = "Discount Fee Amortization";
    private static final String DISCOUNT_FEE_ADJUSTMENT_TRANSACTION = "Discount Fee Adjustment";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_TRANSACTION = "Discount Fee Amortization Adjustment";
    private static final String DISCOUNT_FEE_TYPE_CODE = "loanTransactionType.discountFee";
    private static final String DISCOUNT_FEE_AMORTIZATION_TYPE_CODE = "loanTransactionType.discountFeeAmortization";
    private static final String DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_TYPE_CODE = "loanTransactionType.discountFeeAmortizationAdjustment";
    private static final String PRINCIPAL_ALLOCATION = "Principal";
    private static final String INTEREST_ALLOCATION = "Interest";
    private static final String FEES_ALLOCATION = "Fees";
    private static final String PENALTY_ALLOCATION = "Penalty";
    private static final String UNALLOCATED_CREDIT_ALLOCATION = "Unallocated Credit (UNC)";
    private static final List<String> ALLOCATION_TYPES = List.of(FEES_ALLOCATION, INTEREST_ALLOCATION, PENALTY_ALLOCATION,
            PRINCIPAL_ALLOCATION, UNALLOCATED_CREDIT_ALLOCATION);
    private static final String NO_CHARGE_TYPE = "";
    private static final double AMOUNT_TOLERANCE = 0.01;

    private static FeignOfficeHelper officeHelper;
    private static FeignClientHelper clientHelper;
    private static FeignBusinessDateHelper businessDateHelper;
    private static FeignWorkingCapitalLoanHelper wcLoanHelper;
    private static WorkingCapitalLoanHelper wcLoanTransactionHelper;
    private static FeignReportHelper reportHelper;
    private static FeignAccountHelper accountHelper;
    private static FeignCodeHelper codeHelper;
    private static FeignPaymentTypeHelper paymentTypeHelper;
    private static FeignChargesHelper chargesHelper;
    private static FeignLoanHelper loanHelper;
    private static FeignTransactionHelper loanTransactionHelper;
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
        wcLoanTransactionHelper = new WorkingCapitalLoanHelper();
        reportHelper = new FeignReportHelper(fineractClient);
        accountHelper = new FeignAccountHelper(fineractClient);
        codeHelper = new FeignCodeHelper(fineractClient);
        paymentTypeHelper = new FeignPaymentTypeHelper(fineractClient);
        chargesHelper = new FeignChargesHelper(fineractClient);
        loanHelper = new FeignLoanHelper(fineractClient);
        loanTransactionHelper = new FeignTransactionHelper(fineractClient);
        productHelper = new WorkingCapitalLoanProductHelper();

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
        final List<List<Object>> rows = reportData.stream().map(rowData -> Objects.requireNonNull(rowData.getRow())).toList();
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

    @Test
    public void transactionSummaryReportShowsPaymentsChargeAdjustmentAndWaiverWithTheirReversals() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createProductWithoutAccounting(productName);
        final LocalDate disbursementDate = LocalDate.of(2026, 4, 1);
        final LocalDate paymentDate = disbursementDate.plusDays(1);
        final LocalDate chargeAdjustmentDate = disbursementDate.plusDays(2);
        final LocalDate chargeAdjustmentReversalDate = disbursementDate.plusDays(3);
        final LocalDate waiverDate = disbursementDate.plusDays(4);
        final LocalDate waiverReversalDate = disbursementDate.plusDays(5);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();
        final AtomicLong chargeAdjustmentId = new AtomicLong();
        final AtomicLong waiverId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoan(borrower.get().clientId(), productId, apiDate(disbursementDate), LOAN_PRINCIPAL));
        });
        final Long officeId = borrower.get().officeId();
        assertReport(disbursementDate, officeId, productName,
                allocationRows(DISBURSEMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(9000))));

        businessDateHelper.runAt(paymentDate.toString(), () -> {
            final Long repaymentId = wcLoanHelper.makeRepayment(loanId.get(),
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), apiDate(paymentDate)));
            final Long goodwillCreditId = wcLoanHelper.makeGoodwillCredit(loanId.get(),
                    WorkingCapitalLoanRequestBuilders.goodwillCredit(BigDecimal.valueOf(110), apiDate(paymentDate)));
            final Long payoutRefundId = wcLoanHelper.payoutRefund(loanId.get(),
                    WorkingCapitalLoanRequestBuilders.payoutRefund(BigDecimal.valueOf(200), apiDate(paymentDate)));
            wcLoanHelper.undoTransaction(loanId.get(), payoutRefundId);
            wcLoanHelper.undoTransaction(loanId.get(), goodwillCreditId);
            wcLoanHelper.undoTransaction(loanId.get(), repaymentId);
        });
        assertReport(paymentDate, officeId, productName,
                concat(allocationRows(GOODWILL_CREDIT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-110))),
                        allocationRows(GOODWILL_CREDIT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(110))),
                        allocationRows(PAYOUT_REFUND_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-200))),
                        allocationRows(PAYOUT_REFUND_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(200))),
                        allocationRows(REPAYMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-100))),
                        allocationRows(REPAYMENT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(100)))));

        businessDateHelper.runAt(chargeAdjustmentDate.toString(), () -> {
            final Long feeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 30.0));
            final Long loanChargeId = wcLoanHelper.addCharge(loanId.get(),
                    WorkingCapitalLoanRequestBuilders.addCharge(feeId, 30.0, apiDate(chargeAdjustmentDate)));
            chargeAdjustmentId.set(wcLoanHelper.adjustCharge(loanId.get(), loanChargeId,
                    WorkingCapitalLoanRequestBuilders.chargeAdjustment(BigDecimal.valueOf(30))));
        });
        assertReport(chargeAdjustmentDate, officeId, productName,
                allocationRows(CHARGE_ADJUSTMENT_TRANSACTION, false, Map.of(FEES_ALLOCATION, amount(-30))));

        businessDateHelper.runAt(chargeAdjustmentReversalDate.toString(),
                () -> wcLoanHelper.undoTransaction(loanId.get(), chargeAdjustmentId.get()));
        assertReport(chargeAdjustmentReversalDate, officeId, productName,
                allocationRows(CHARGE_ADJUSTMENT_TRANSACTION, true, Map.of(FEES_ALLOCATION, amount(30))));

        businessDateHelper.runAt(waiverDate.toString(), () -> {
            final Long feeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 40.0));
            final Long loanChargeId = wcLoanHelper.addCharge(loanId.get(),
                    WorkingCapitalLoanRequestBuilders.addCharge(feeId, 40.0, apiDate(waiverDate.plusDays(5))));
            waiverId.set(wcLoanHelper.waiveCharge(loanId.get(), loanChargeId, WorkingCapitalLoanRequestBuilders.chargeWaiver()));
        });
        assertReport(waiverDate, officeId, productName,
                allocationRows(WAIVE_CHARGES_TRANSACTION, false, Map.of(FEES_ALLOCATION, amount(-40))));

        businessDateHelper.runAt(waiverReversalDate.toString(), () -> wcLoanHelper.undoTransaction(loanId.get(), waiverId.get()));
        assertReport(waiverReversalDate, officeId, productName,
                allocationRows(WAIVE_CHARGES_TRANSACTION, true, Map.of(FEES_ALLOCATION, amount(40))));
    }

    @Test
    public void transactionSummaryReportShowsDiscountFeeAndItsAmortizationWithTheirReversals() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createProductWithOverridableDiscount(productName);
        final LocalDate disbursementDate = LocalDate.of(2026, 5, 1);
        final LocalDate repaymentDate = disbursementDate.plusDays(1);
        final LocalDate amortizationCobDate = disbursementDate.plusDays(2);
        final LocalDate adjustmentDate = disbursementDate.plusDays(3);
        final LocalDate adjustmentCobDate = disbursementDate.plusDays(4);
        final LocalDate adjustmentReversalDate = disbursementDate.plusDays(5);
        final LocalDate disbursementReversalDate = disbursementDate.plusDays(6);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();
        final AtomicLong repaymentId = new AtomicLong();
        final AtomicLong adjustmentId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoanWithDiscount(borrower.get().clientId(), productId, apiDate(disbursementDate)));
        });
        final Long officeId = borrower.get().officeId();
        assertReport(disbursementDate, officeId, productName,
                concat(allocationRows(DISBURSEMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(9000))),
                        allocationRows(DISCOUNT_FEE_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(1000)))));

        businessDateHelper.runAt(repaymentDate.toString(), () -> repaymentId.set(wcLoanHelper.makeRepayment(loanId.get(),
                WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), apiDate(repaymentDate)))));
        businessDateHelper.runAt(amortizationCobDate.toString(), () -> wcLoanHelper.executeInlineWCCOB(loanId.get()));
        final Long amortizationId = activeTransactionId(loanId.get(), DISCOUNT_FEE_AMORTIZATION_TYPE_CODE);
        assertReport(repaymentDate, officeId, productName,
                concat(allocationRows(DISCOUNT_FEE_AMORTIZATION_TRANSACTION, false,
                        Map.of(FEES_ALLOCATION, transactionAmount(loanId.get(), amortizationId))),
                        allocationRows(REPAYMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-50)))));

        businessDateHelper.runAt(adjustmentDate.toString(),
                () -> adjustmentId.set(wcLoanHelper.makeDiscountFeeAdjustment(loanId.get(),
                        WorkingCapitalLoanRequestBuilders.discountFeeAdjustment(activeTransactionId(loanId.get(), DISCOUNT_FEE_TYPE_CODE),
                                BigDecimal.valueOf(500), apiDate(adjustmentDate)))));
        businessDateHelper.runAt(adjustmentCobDate.toString(), () -> wcLoanHelper.executeInlineWCCOB(loanId.get()));
        final BigDecimal amortizationAdjustment = transactionAmount(loanId.get(),
                activeTransactionId(loanId.get(), DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_TYPE_CODE));
        assertReport(adjustmentDate, officeId, productName,
                concat(allocationRows(DISCOUNT_FEE_ADJUSTMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-500))),
                        allocationRows(DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_TRANSACTION, false,
                                Map.of(FEES_ALLOCATION, amortizationAdjustment.negate()))));

        businessDateHelper.runAt(adjustmentReversalDate.toString(), () -> wcLoanHelper.undoTransaction(loanId.get(), adjustmentId.get()));
        assertReport(adjustmentReversalDate, officeId, productName, concat(
                allocationRows(DISCOUNT_FEE_ADJUSTMENT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(500))),
                allocationRows(DISCOUNT_FEE_AMORTIZATION_ADJUSTMENT_TRANSACTION, true, Map.of(FEES_ALLOCATION, amortizationAdjustment))));

        businessDateHelper.runAt(disbursementReversalDate.toString(), () -> {
            wcLoanHelper.undoTransaction(loanId.get(), repaymentId.get());
            wcLoanHelper.undoDisbursal(loanId.get(), WorkingCapitalLoanRequestBuilders.undoDisbursal());
        });
        assertReport(disbursementReversalDate, officeId, productName,
                concat(allocationRows(DISBURSEMENT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(-9000))),
                        allocationRows(DISCOUNT_FEE_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(-1000))),
                        allocationRows(DISCOUNT_FEE_AMORTIZATION_TRANSACTION, true,
                                Map.of(FEES_ALLOCATION, transactionAmount(loanId.get(), amortizationId).negate())),
                        allocationRows(REPAYMENT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(50)))));
    }

    @Test
    public void transactionSummaryReportShowsChargeOffReasonFromTheChargeOffUntilItIsUndone() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createProductWithOverridableDiscount(productName);
        final String chargeOffReason = Utils.uniqueRandomStringGenerator("WC TSR Reason ", 6);
        final Long chargeOffReasonId = codeHelper.createChargeOffCodeValue(chargeOffReason, 1);
        final LocalDate disbursementDate = LocalDate.of(2026, 6, 1);
        final LocalDate repaymentDate = disbursementDate.plusDays(1);
        final LocalDate chargeOffDate = disbursementDate.plusDays(2);
        final LocalDate postChargeOffRepaymentDate = disbursementDate.plusDays(3);
        final LocalDate repaymentReversalDate = disbursementDate.plusDays(4);
        final LocalDate chargeOffReversalDate = disbursementDate.plusDays(5);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();
        final AtomicLong postChargeOffRepaymentId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoanWithDiscount(borrower.get().clientId(), productId, apiDate(disbursementDate)));
        });
        final Long officeId = borrower.get().officeId();

        businessDateHelper.runAt(repaymentDate.toString(), () -> repaymentIdByLoanId.put(loanId.get(), wcLoanHelper
                .makeRepayment(loanId.get(), WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), apiDate(repaymentDate)))));
        assertReport(repaymentDate, officeId, productName,
                allocationRows(REPAYMENT_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-50))));

        businessDateHelper.runAt(chargeOffDate.toString(), () -> wcLoanHelper.chargeOff(loanId.get(),
                WorkingCapitalLoanRequestBuilders.chargeOff(apiDate(chargeOffDate), null).chargeOffReasonId(chargeOffReasonId)));
        assertReport(chargeOffDate, officeId, productName,
                concat(allocationRows(CHARGE_OFF_TRANSACTION, null, false, chargeOffReason, Map.of(PRINCIPAL_ALLOCATION, amount(-9950))),
                        allocationRows(DISCOUNT_FEE_AMORTIZATION_TRANSACTION, null, false, chargeOffReason,
                                Map.of(FEES_ALLOCATION, amount(1000)))));

        businessDateHelper.runAt(postChargeOffRepaymentDate.toString(),
                () -> postChargeOffRepaymentId.set(wcLoanHelper.makeRepayment(loanId.get(),
                        WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(100), apiDate(postChargeOffRepaymentDate)))));
        assertReport(postChargeOffRepaymentDate, officeId, productName,
                allocationRows(REPAYMENT_TRANSACTION, null, false, chargeOffReason, Map.of(PRINCIPAL_ALLOCATION, amount(-100))));

        businessDateHelper.runAt(repaymentReversalDate.toString(),
                () -> wcLoanHelper.undoTransaction(loanId.get(), postChargeOffRepaymentId.get()));
        assertReport(repaymentReversalDate, officeId, productName,
                allocationRows(REPAYMENT_TRANSACTION, null, true, chargeOffReason, Map.of(PRINCIPAL_ALLOCATION, amount(100))));

        businessDateHelper.runAt(chargeOffReversalDate.toString(),
                () -> wcLoanHelper.undoChargeOff(loanId.get(), WorkingCapitalLoanRequestBuilders.undoChargeOff(null)));
        assertReport(chargeOffReversalDate, officeId, productName,
                concat(allocationRows(CHARGE_OFF_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(9950))),
                        allocationRows(DISCOUNT_FEE_AMORTIZATION_TRANSACTION, true, Map.of(FEES_ALLOCATION, amount(-1000)))));
    }

    @Test
    public void transactionSummaryReportShowsWriteOffAndRecoveryWithTheirReversals() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createProductWithoutAccounting(productName);
        final LocalDate disbursementDate = LocalDate.of(2026, 7, 1);
        final LocalDate writeOffDate = disbursementDate.plusDays(1);
        final LocalDate recoveryDate = disbursementDate.plusDays(2);
        final LocalDate reversalDate = disbursementDate.plusDays(3);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();
        final AtomicLong recoveryId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoan(borrower.get().clientId(), productId, apiDate(disbursementDate), SMALL_LOAN_PRINCIPAL));
        });
        final Long officeId = borrower.get().officeId();

        businessDateHelper.runAt(writeOffDate.toString(), () -> wcLoanTransactionHelper.writeOffByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(writeOffDate)));
        assertReport(writeOffDate, officeId, productName,
                allocationRows(WRITE_OFF_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-1000))));

        businessDateHelper.runAt(recoveryDate.toString(), () -> recoveryId.set(wcLoanTransactionHelper.recoveryPaymentByLoanId(loanId.get(),
                WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(recoveryDate, BigDecimal.valueOf(50)))));
        assertReport(recoveryDate, officeId, productName,
                allocationRows(RECOVERY_REPAYMENT_TRANSACTION, false, Map.of(UNALLOCATED_CREDIT_ALLOCATION, amount(-50))));

        businessDateHelper.runAt(reversalDate.toString(), () -> {
            wcLoanHelper.undoTransaction(loanId.get(), recoveryId.get());
            wcLoanTransactionHelper.undoWriteOffByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildUndoWriteOffRequest());
        });
        assertReport(reversalDate, officeId, productName,
                concat(allocationRows(RECOVERY_REPAYMENT_TRANSACTION, true, Map.of(UNALLOCATED_CREDIT_ALLOCATION, amount(50))),
                        allocationRows(WRITE_OFF_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(1000)))));
    }

    @Test
    public void transactionSummaryReportShowsOverpaymentAndCreditBalanceRefund() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createProductWithoutAccounting(productName);
        final LocalDate disbursementDate = LocalDate.of(2026, 8, 1);
        final LocalDate overpaymentDate = disbursementDate.plusDays(1);
        final LocalDate refundDate = disbursementDate.plusDays(2);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoan(borrower.get().clientId(), productId, apiDate(disbursementDate), LOAN_PRINCIPAL));
        });
        final Long officeId = borrower.get().officeId();

        businessDateHelper.runAt(overpaymentDate.toString(), () -> wcLoanHelper.makeRepayment(loanId.get(),
                WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(9050), apiDate(overpaymentDate))));
        assertReport(overpaymentDate, officeId, productName, allocationRows(REPAYMENT_TRANSACTION, false,
                Map.of(PRINCIPAL_ALLOCATION, amount(-9000), UNALLOCATED_CREDIT_ALLOCATION, amount(-50))));

        businessDateHelper.runAt(refundDate.toString(), () -> wcLoanHelper.creditBalanceRefund(loanId.get(),
                WorkingCapitalLoanRequestBuilders.creditBalanceRefund(BigDecimal.valueOf(30), apiDate(refundDate))));
        assertReport(refundDate, officeId, productName,
                allocationRows(CREDIT_BALANCE_REFUND_TRANSACTION, false, Map.of(UNALLOCATED_CREDIT_ALLOCATION, amount(30))));
    }

    @Test
    public void transactionSummaryReportShowsAccrualPerChargeAndItsReversal() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct(productName);
        final ChargeRequest fee = WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 45.0)
                .name(Utils.uniqueRandomStringGenerator("WC TSR Fee ", 6));
        final ChargeRequest penalty = WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(true, 25.0)
                .name(Utils.uniqueRandomStringGenerator("WC TSR Penalty ", 6));
        final Long feeId = wcLoanHelper.createGlobalCharge(fee);
        final Long penaltyId = wcLoanHelper.createGlobalCharge(penalty);
        final LocalDate disbursementDate = LocalDate.of(2026, 9, 1);
        final LocalDate chargeDueDate = disbursementDate.plusDays(1);
        final LocalDate reversalDate = disbursementDate.plusDays(2);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            loanId.set(createDisbursedLoan(borrower.get().clientId(), productId, apiDate(disbursementDate), LOAN_PRINCIPAL));
            wcLoanHelper.addCharge(loanId.get(), WorkingCapitalLoanRequestBuilders.addCharge(feeId, 45.0, apiDate(chargeDueDate)));
            wcLoanHelper.addCharge(loanId.get(), WorkingCapitalLoanRequestBuilders.addCharge(penaltyId, 25.0, apiDate(chargeDueDate)));
        });
        final Long officeId = borrower.get().officeId();
        final List<ReportRow> accrualRows = List.of(
                new ReportRow(APPLY_CHARGES_TRANSACTION, null, NO_CHARGE_TYPE, false, INTEREST_ALLOCATION, null, amount(0)),
                new ReportRow(APPLY_CHARGES_TRANSACTION, null, fee.getName(), false, FEES_ALLOCATION, null, amount(45)),
                new ReportRow(APPLY_CHARGES_TRANSACTION, null, penalty.getName(), false, PENALTY_ALLOCATION, null, amount(25)));

        businessDateHelper.runAt(reversalDate.toString(), () -> wcLoanHelper.executeInlineWCCOB(loanId.get()));
        assertReport(chargeDueDate, officeId, productName, accrualRows);

        businessDateHelper.runAt(reversalDate.toString(),
                () -> wcLoanHelper.undoDisbursal(loanId.get(), WorkingCapitalLoanRequestBuilders.undoDisbursal()));
        assertReport(
                reversalDate, officeId, productName, concat(
                        List.of(new ReportRow(APPLY_CHARGES_TRANSACTION, null, NO_CHARGE_TYPE, true, INTEREST_ALLOCATION, null, amount(0)),
                                new ReportRow(APPLY_CHARGES_TRANSACTION, null, fee.getName(), true, FEES_ALLOCATION, null, amount(-45)),
                                new ReportRow(APPLY_CHARGES_TRANSACTION, null, penalty.getName(), true, PENALTY_ALLOCATION, null,
                                        amount(-25))),
                        allocationRows(DISBURSEMENT_TRANSACTION, true, Map.of(PRINCIPAL_ALLOCATION, amount(-9000)))));
        assertReport(chargeDueDate, officeId, productName, accrualRows);
    }

    @Test
    public void transactionSummaryReportGivesTheSameRowsForWorkingCapitalAndRegularLoans() {
        final String productName = Utils.uniqueRandomStringGenerator("WCL TSR ", 8);
        final Long productId = createAccrualWithDeferredRevenueAmortizationProduct(productName);
        final PostLoanProductsRequest regularProduct = new LoanTestAccounts(accountHelper)
                .onePeriod30DaysPeriodicAccrualWithAdvancedAllocation();
        final Long regularProductId = Objects.requireNonNull(loanHelper.createLoanProduct(regularProduct).getResourceId());
        final String paymentTypeName = Utils.uniqueRandomStringGenerator("WC TSR Payment ", 6);
        final Long paymentTypeId = Objects.requireNonNull(paymentTypeHelper
                .createPaymentType(
                        new PaymentTypeCreateRequest().name(paymentTypeName).description(paymentTypeName).isCashPayment(false).position(1L))
                .getResourceId());
        final ChargeRequest fee = WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(false, 10.0);
        final Long feeId = wcLoanHelper.createGlobalCharge(fee);
        final ChargeRequest regularFee = ChargeRequestBuilders.loanSpecifiedDueDateFee(10.0);
        final Long regularFeeId = Objects.requireNonNull(chargesHelper.createCharge(regularFee).getResourceId());
        final LocalDate disbursementDate = LocalDate.of(2026, 10, 1);
        final LocalDate chargeDueDate = disbursementDate.plusDays(1);
        final LocalDate repaymentDate = disbursementDate.plusDays(2);
        final LocalDate writeOffDate = disbursementDate.plusDays(3);
        final LocalDate recoveryDate = disbursementDate.plusDays(4);
        final AtomicReference<Borrower> borrower = new AtomicReference<>();
        final AtomicLong loanId = new AtomicLong();
        final AtomicLong regularLoanId = new AtomicLong();

        businessDateHelper.runAt(disbursementDate.toString(), () -> {
            borrower.set(createBorrower());
            regularLoanId
                    .set(createDisbursedRegularLoan(borrower.get().clientId(), regularProductId, apiDate(disbursementDate), paymentTypeId));
            loanHelper.addLoanCharge(regularLoanId.get(), LoanRequestBuilders.addLoanCharge(regularFeeId, 10.0, apiDate(chargeDueDate)));
            loanId.set(createApprovedLoan(borrower.get().clientId(), productId, apiDate(disbursementDate), SMALL_LOAN_PRINCIPAL));
            wcLoanHelper.disburse(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(disbursementDate,
                    SMALL_LOAN_PRINCIPAL, null, null, paymentTypeId.intValue(), null, null, null, null, null));
            wcLoanHelper.addCharge(loanId.get(), WorkingCapitalLoanRequestBuilders.addCharge(feeId, 10.0, apiDate(chargeDueDate)));
        });
        final ComparedReports compared = new ComparedReports(borrower.get().officeId(), productName, regularProduct.getName(),
                regularFee.getName(), fee.getName());
        assertSameRows(compared, disbursementDate,
                allocationRows(DISBURSEMENT_TRANSACTION, paymentTypeName, false, null, Map.of(PRINCIPAL_ALLOCATION, amount(1000))));

        businessDateHelper.runAt(repaymentDate.toString(), () -> {
            loanTransactionHelper.executeInlineCOB(regularLoanId.get());
            wcLoanHelper.executeInlineWCCOB(loanId.get());
            loanTransactionHelper.addRepayment(regularLoanId.get(),
                    LoanRequestBuilders.repayLoan(100.0, apiDate(repaymentDate)).paymentTypeId(paymentTypeId));
            wcLoanHelper.makeRepayment(loanId.get(), WorkingCapitalLoanDisbursementTestBuilder.buildRepaymentRequest(repaymentDate,
                    BigDecimal.valueOf(100), null, null, paymentTypeId.intValue(), null));
        });
        assertSameRows(compared, chargeDueDate,
                List.of(new ReportRow(APPLY_CHARGES_TRANSACTION, null, NO_CHARGE_TYPE, false, INTEREST_ALLOCATION, null, amount(0)),
                        new ReportRow(APPLY_CHARGES_TRANSACTION, null, fee.getName(), false, FEES_ALLOCATION, null, amount(10))));
        assertSameRows(compared, repaymentDate, allocationRows(REPAYMENT_TRANSACTION, paymentTypeName, false, null,
                Map.of(FEES_ALLOCATION, amount(-10), PRINCIPAL_ALLOCATION, amount(-90))));

        businessDateHelper.runAt(writeOffDate.toString(), () -> {
            loanTransactionHelper.writeOff(regularLoanId.get(), LoanRequestBuilders.writeOff(apiDate(writeOffDate)));
            wcLoanTransactionHelper.writeOffByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildWriteOffRequest(writeOffDate));
        });
        assertSameRows(compared, writeOffDate, allocationRows(WRITE_OFF_TRANSACTION, false, Map.of(PRINCIPAL_ALLOCATION, amount(-910))));

        businessDateHelper.runAt(recoveryDate.toString(), () -> {
            loanTransactionHelper.makeRecoveryPayment(regularLoanId.get(), LoanRequestBuilders.repayLoan(50.0, apiDate(recoveryDate)));
            wcLoanTransactionHelper.recoveryPaymentByLoanId(loanId.get(),
                    WorkingCapitalLoanDisbursementTestBuilder.buildRecoveryPaymentRequest(recoveryDate, BigDecimal.valueOf(50)));
        });
        assertSameRows(compared, recoveryDate,
                allocationRows(RECOVERY_REPAYMENT_TRANSACTION, false, Map.of(UNALLOCATED_CREDIT_ALLOCATION, amount(-50))));
    }

    private Long createAccrualWithDeferredRevenueAmortizationProduct(final String productName) {
        return createProduct(accrualProductBuilder(productName));
    }

    private Long createProductWithoutAccounting(final String productName) {
        return createProduct(productBuilder(productName));
    }

    private Long createProductWithOverridableDiscount(final String productName) {
        return createProduct(productBuilder(productName).withAllowAttributeOverrides(Map.of("discountDefault", Boolean.TRUE)));
    }

    private Long createProduct(final WorkingCapitalLoanProductTestBuilder productBuilder) {
        return Objects.requireNonNull(productHelper.createWorkingCapitalLoanProduct(productBuilder.build()).getResourceId());
    }

    private WorkingCapitalLoanProductTestBuilder productBuilder(final String productName) {
        return new WorkingCapitalLoanProductTestBuilder().withName(productName).withShortName(Utils.uniqueRandomStringGenerator("", 4));
    }

    private WorkingCapitalLoanProductTestBuilder accrualProductBuilder(final String productName) {
        return productBuilder(productName).withAccountingRule(AccountingRuleEnum.ACC_DEF_REV_AM)
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
                .withDeferredIncomeLiabilityAccountId(deferredIncomeAccount.getAccountID().longValue());
    }

    private Borrower createBorrower() {
        final Long officeId = officeHelper.createOffice(OFFICE_OPENING_DATE).getResourceId();
        assertNotNull(officeId);
        return new Borrower(officeId, createClientInOffice(officeId));
    }

    private Long createClientInOffice(final Long officeId) {
        return Objects.requireNonNull(clientHelper
                .createClient(ClientRequestBuilders.createActivePersonClient(CLIENT_ACTIVATION_DATE).officeId(officeId)).getClientId());
    }

    private Long createApprovedAndDisbursedLoan(final Long clientId, final Long productId) {
        return createDisbursedLoan(clientId, productId, TRANSACTION_DATE, PRINCIPAL);
    }

    private Long createDisbursedLoan(final Long clientId, final Long productId, final String disbursementDate, final BigDecimal principal) {
        final Long loanId = createApprovedLoan(clientId, productId, disbursementDate, principal);
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(disbursementDate, principal));
        return loanId;
    }

    private Long createApprovedLoan(final Long clientId, final Long productId, final String disbursementDate, final BigDecimal principal) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                principal, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, disbursementDate, disbursementDate));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(disbursementDate, principal, disbursementDate));
        return loanId;
    }

    private Long createDisbursedLoanWithDiscount(final Long clientId, final Long productId, final String disbursementDate) {
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplicationWithDiscount(clientId,
                productId, LOAN_PRINCIPAL, WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT, disbursementDate,
                disbursementDate, DISCOUNT_FEE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId,
                WorkingCapitalLoanRequestBuilders.approveWithDiscount(disbursementDate, LOAN_PRINCIPAL, disbursementDate, DISCOUNT_FEE));
        wcLoanHelper.disburse(loanId,
                WorkingCapitalLoanRequestBuilders.disburseWithDiscount(disbursementDate, LOAN_PRINCIPAL, DISCOUNT_FEE));
        return loanId;
    }

    private Long createDisbursedRegularLoan(final Long clientId, final Long productId, final String disbursementDate,
            final Long paymentTypeId) {
        final Long loanId = Objects
                .requireNonNull(
                        loanHelper
                                .applyForLoan(LoanRequestBuilders
                                        .applyLoanRequest(clientId, productId, disbursementDate, SMALL_LOAN_PRINCIPAL.doubleValue(), 1,
                                                request -> request.transactionProcessingStrategyCode(
                                                        TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)))
                                .getLoanId());
        loanHelper.approveLoan(loanId, LoanRequestBuilders.approveLoan(SMALL_LOAN_PRINCIPAL.doubleValue(), disbursementDate));
        loanHelper.disburseLoan(loanId, LoanRequestBuilders.disburseLoan(SMALL_LOAN_PRINCIPAL.doubleValue(), disbursementDate)
                .paymentTypeId(paymentTypeId.intValue()));
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

    private Long activeTransactionId(final Long loanId, final String typeCode) {
        return wcLoanHelper.getTransactions(loanId).stream()
                .filter(transaction -> typeCode.equals(Objects.requireNonNull(transaction.getType()).getCode())
                        && !Boolean.TRUE.equals(transaction.getReversed()))
                .findFirst().map(GetWorkingCapitalLoanTransactionIdResponse::getId)
                .orElseThrow(() -> new AssertionError("No active " + typeCode + " transaction on Working Capital loan " + loanId));
    }

    private BigDecimal transactionAmount(final Long loanId, final Long transactionId) {
        return amount(Objects.requireNonNull(wcLoanHelper.getTransaction(loanId, transactionId).getTransactionAmount()));
    }

    private RunReportsResponse runTransactionSummaryReport(final String endDate, final Long officeId) {
        return runReport(TRANSACTION_SUMMARY_REPORT, endDate, officeId);
    }

    private RunReportsResponse runReport(final String reportName, final String endDate, final Long officeId) {
        return reportHelper.runReport(reportName, Map.of("R_endDate", endDate, "R_officeId", officeId.toString(), "output-type", "CSV"));
    }

    private void assertReport(final LocalDate reportDate, final Long officeId, final String productName,
            final List<ReportRow> expectedRows) {
        assertEquals(expectedRows, reportRows(TRANSACTION_SUMMARY_REPORT, reportDate, officeId, productName),
                "Rows of " + TRANSACTION_SUMMARY_REPORT + " for " + reportDate);
    }

    private void assertSameRows(final ComparedReports compared, final LocalDate reportDate, final List<ReportRow> expectedRows) {
        assertReport(reportDate, compared.officeId(), compared.productName(), expectedRows);
        final List<ReportRow> regularRows = reportRows(REGULAR_TRANSACTION_SUMMARY_REPORT, reportDate, compared.officeId(),
                compared.regularProductName()).stream()
                .map(row -> compared.regularChargeName().equals(row.chargeType()) ? row.withChargeType(compared.chargeName()) : row)
                .toList();
        assertEquals(expectedRows, regularRows, "Rows of " + REGULAR_TRANSACTION_SUMMARY_REPORT + " for " + reportDate);
    }

    private List<ReportRow> reportRows(final String reportName, final LocalDate reportDate, final Long officeId, final String productName) {
        final List<ResultsetRowData> reportData = runReport(reportName, reportDate.toString(), officeId).getData();
        assertNotNull(reportData);
        return reportData.stream().map(rowData -> toReportRow(Objects.requireNonNull(rowData.getRow()), reportDate, productName)).toList();
    }

    private ReportRow toReportRow(final List<Object> row, final LocalDate reportDate, final String productName) {
        assertEquals(reportDate.toString(), row.get(TRANSACTION_DATE_COLUMN));
        assertEquals(productName, row.get(PRODUCT_NAME_COLUMN));
        assertNull(row.get(ASSET_OWNER_ID_COLUMN));
        assertNull(row.get(FROM_ASSET_OWNER_ID_COLUMN));
        assertNull(row.get(ORIGINATOR_EXTERNAL_IDS_COLUMN));
        return new ReportRow((String) row.get(TRANSACTION_TYPE_COLUMN), (String) row.get(PAYMENT_TYPE_COLUMN),
                (String) row.get(CHARGE_TYPE_COLUMN), isReversed(row.get(REVERSED_COLUMN)), (String) row.get(ALLOCATION_TYPE_COLUMN),
                (String) row.get(CHARGE_OFF_REASON_COLUMN), amount(((Number) row.get(AMOUNT_COLUMN)).doubleValue()));
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
        assertFalse(isReversed(reversed), "Expected a non-reversed transaction, but the report reported reversed=" + reversed);
    }

    private static boolean isReversed(final Object reversed) {
        return reversed instanceof Number number ? number.intValue() != 0 : Boolean.TRUE.equals(reversed);
    }

    private static List<ReportRow> allocationRows(final String transactionType, final boolean reversed,
            final Map<String, BigDecimal> amounts) {
        return allocationRows(transactionType, null, reversed, null, amounts);
    }

    private static List<ReportRow> allocationRows(final String transactionType, final String paymentType, final boolean reversed,
            final String chargeOffReason, final Map<String, BigDecimal> amounts) {
        return ALLOCATION_TYPES.stream().map(allocationType -> new ReportRow(transactionType, paymentType, NO_CHARGE_TYPE, reversed,
                allocationType, chargeOffReason, amounts.getOrDefault(allocationType, amount(0)))).toList();
    }

    @SafeVarargs
    private static List<ReportRow> concat(final List<ReportRow>... groups) {
        return Stream.of(groups).flatMap(List::stream).toList();
    }

    private static BigDecimal amount(final double value) {
        return amount(BigDecimal.valueOf(value));
    }

    private static BigDecimal amount(final BigDecimal value) {
        return value.setScale(2, RoundingMode.HALF_UP);
    }

    private static String apiDate(final LocalDate date) {
        return date.format(API_DATE_FORMAT);
    }

    private record Borrower(Long officeId, Long clientId) {
    }

    private record ComparedReports(Long officeId, String productName, String regularProductName, String regularChargeName,
            String chargeName) {
    }

    private record ReportRow(String transactionType, String paymentType, String chargeType, boolean reversed, String allocationType,
            String chargeOffReason, BigDecimal amount) {

        ReportRow withChargeType(final String newChargeType) {
            return new ReportRow(transactionType, paymentType, newChargeType, reversed, allocationType, chargeOffReason, amount);
        }
    }
}
