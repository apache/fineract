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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.List;
import java.util.Locale;
import java.util.Set;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdLoanChargePaidByData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdChargesRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.portfolio.charge.domain.ChargeCalculationType;
import org.apache.fineract.portfolio.loanaccount.domain.LoanStatus;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@ExtendWith(LoanTestLifecycleExtension.class)
public class ClientLoanChargeRefundIntegrationTest extends FeignLoanTestBase {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanChargeRefundIntegrationTest.class);

    private static final String ZERO_INTEREST_RATE = "0";
    private static final String FOUR_INSTALLMENTS = "4";
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";

    /*
     * loan disbursed: 4 installments of 3000; zero % interest; a specified due date charge of 120 is added and the
     * amount is allocated to installment 2; allocation strategy is penalty, fees, interest, principal
     */
    private static final Double oneInstallment = 3000.00d;
    private static final Double fullLoan = 3000.00d * 4;
    private static final Double fullChargeRefundAmount = 120.00d;
    private static final Double oneThirdChargeRefundAmount = 40.00d;

    private Long disbursedLoanID;
    private Account assetAccount;
    private Account feeIncomeAccount;
    private Account penaltyIncomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;
    private Long createdRepaymentTypeResourceId;

    @BeforeEach
    public void setupAccounts() {
        this.assetAccount = accountHelper.createAssetAccount();
        this.feeIncomeAccount = accountHelper.createIncomeAccount();
        this.penaltyIncomeAccount = accountHelper.createIncomeAccount();
        this.expenseAccount = accountHelper.createExpenseAccount();
        this.overpaymentAccount = accountHelper.createLiabilityAccount();
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Active() {
        testRefundAndReverseOfPaidChargeSucceeds(oneInstallment + fullChargeRefundAmount, fullChargeRefundAmount, LoanStatus.ACTIVE,
                LoanStatus.ACTIVE);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Com() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan, fullChargeRefundAmount, LoanStatus.ACTIVE, LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + 50.00d, fullChargeRefundAmount, LoanStatus.ACTIVE, LoanStatus.OVERPAID);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Com_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + fullChargeRefundAmount, fullChargeRefundAmount,
                LoanStatus.CLOSED_OBLIGATIONS_MET, LoanStatus.OVERPAID);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Overpaid_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + fullChargeRefundAmount + 50.00d, fullChargeRefundAmount, LoanStatus.OVERPAID,
                LoanStatus.OVERPAID);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Active() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan, oneThirdChargeRefundAmount, LoanStatus.ACTIVE, LoanStatus.ACTIVE);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Com() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + (oneThirdChargeRefundAmount * 2), oneThirdChargeRefundAmount, LoanStatus.ACTIVE,
                LoanStatus.CLOSED_OBLIGATIONS_MET);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + (oneThirdChargeRefundAmount * 2) + 1.0d, oneThirdChargeRefundAmount,
                LoanStatus.ACTIVE, LoanStatus.OVERPAID);
    }

    private void testRefundAndReverseOfPaidChargeSucceeds(final Double repaymentAmount, final Double refundAmount,
            final LoanStatus expectedPostRepaymentStatus, final LoanStatus expectedPostRefundStatus) {
        // disburse, repay, add charge, charge refund and reverse charge refund
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, expectedPostRepaymentStatus, NONE, true);

        Double totalOutstandingPreRefund = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Double overpaidPreRefund = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        Double expectedTotalOutstandingPostRefund = null;
        Double expectedOverpaidPostRefund = null;
        if (totalOutstandingPreRefund.compareTo(refundAmount) >= 0) {
            expectedTotalOutstandingPostRefund = totalOutstandingPreRefund - refundAmount;
            expectedOverpaidPostRefund = 0.0d;
        } else {
            expectedTotalOutstandingPostRefund = 0.0d;
            if (totalOutstandingPreRefund == 0.0d) {
                expectedOverpaidPostRefund = overpaidPreRefund + refundAmount;
            } else {
                expectedOverpaidPostRefund = refundAmount - totalOutstandingPreRefund;
            }
        }

        LOG.info("-------------Loancharge Refund -----------");
        Long chargeRefundTxnId = loanChargeRefund(loanChargeId, refundAmount);
        verifyLoanStatus(disbursedLoanID, expectedPostRefundStatus);

        Double totalOutstandingPostRefund = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Double overpaidPostRefund = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        assertEquals(expectedTotalOutstandingPostRefund, totalOutstandingPostRefund, "Incorrect totalOutstanding Post Refund");
        assertEquals(expectedOverpaidPostRefund, overpaidPostRefund, "Incorrect overpaid Post Refund");

        verifyPaidByEntry(disbursedLoanID, chargeRefundTxnId, refundAmount);

        LOG.info("-------------Reverse Loancharge Refund -----------");
        adjustLoanTransaction(disbursedLoanID, chargeRefundTxnId, getTodaysDate());
        verifyLoanStatus(disbursedLoanID, expectedPostRepaymentStatus);

        Double totalOutstandingPostReverse = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Double overpaidPostReverse = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        assertEquals(totalOutstandingPreRefund, totalOutstandingPostReverse, "Incorrect totalOutstanding Post Reverse");
        assertEquals(overpaidPreRefund, overpaidPostReverse, "Incorrect overpaid Post Reverse");
    }

    @Test
    public void refundOfUnpaidChargeFailsTest() {

        final Double repaymentAmount = 3000.00d; // pays installment one but none of added charge
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund -----------");
        final Double refundAmount = 60.00d;
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanChargeRefund(loanChargeId, refundAmount));
        assertErrorGlobalisationCode(exception, "error.msg.loan.charge.transaction.amount.is.more.than.is.refundable");
    }

    @Test
    public void refundingMoreThanPaidFailsTest() {

        final Double repaymentAmount = 3090.00d; // pays installment one and 90 (not all) of added charge
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund -----------");
        final Double refundAmount = 90.01d; // 0.01 more than paid.
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> loanChargeRefund(loanChargeId, refundAmount));
        assertErrorGlobalisationCode(exception, "error.msg.loan.charge.transaction.amount.is.more.than.is.refundable");
    }

    @Test
    public void onyRefundElementNotRepaymentElementUsedToCalculateRefundableAmountTest() {
        final Double chargeAmountPaid = 60.00d;
        final Double repaymentAmount = 3000.00d + chargeAmountPaid;
        // covers Installment 1 plus half of 120 charge added to installment 2
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund 1 -----------");
        // refund 60 pays off remainder of charge leaving an amount 60 that could be refunded
        loanChargeRefund(loanChargeId, chargeAmountPaid);

        LOG.info("-------------Loancharge Refund 2 -----------");
        final Double smallRefund = 0.01d;
        loanChargeRefund(loanChargeId, smallRefund);
    }

    @Test
    public void refundOfPartiallyPaidChargeCanRepayMoreOfSameChargeTest() {
        final Double chargeAmountPaid = 80.00d;
        final Double chargeAmountFull = 120.00d;
        final Double chargeAmountOutstanding = chargeAmountFull - chargeAmountPaid;
        final Double repaymentAmount = 3000.00d + chargeAmountPaid;
        // covers Installment 1 plus two thirds of 120 charge added to installment 2
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, NONE, false);
        Double feeChargesPaid = getLoanDetailsSummaryfeeChargesPaid(disbursedLoanID);
        assertEquals(chargeAmountPaid, feeChargesPaid, "Incorrect Partial feeChargesPaid");

        LOG.info("-------------Loancharge Refund -----------");
        Long chargeRefundTxnId = loanChargeRefund(loanChargeId, chargeAmountPaid);
        feeChargesPaid = getLoanDetailsSummaryfeeChargesPaid(disbursedLoanID);
        assertEquals(chargeAmountFull, feeChargesPaid, "Incorrect Full feeChargesPaid");

        // expecting 2 entries: the -80 refund and the 40 repayment. loanChargePaidByList is a Set, so the server
        // promises no order; match on sign rather than position.
        List<Double> paidByAmounts = getLoanTransactionDetails(disbursedLoanID, chargeRefundTxnId).getLoanChargePaidByList().stream()
                .map(GetLoansLoanIdLoanChargePaidByData::getAmount).map(Utils::getDoubleValue).toList();
        assertEquals(2, paidByAmounts.size());
        paidByAmounts.forEach(amount -> assertNotEquals(0.0d, amount));

        Double refundElement = paidByAmounts.stream().filter(amount -> amount < 0.0d).findFirst().orElse(null);
        Double repaymentElement = paidByAmounts.stream().filter(amount -> amount > 0.0d).findFirst().orElse(null);
        assertNotNull(refundElement, "Refund Element missing");
        assertNotNull(repaymentElement, "Repayment Element missing");
        assertEquals(chargeAmountPaid * -1, refundElement, "Refund Element Incorrect");
        assertEquals(chargeAmountOutstanding, repaymentElement, "Repayment Element Incorrect");
    }

    @Test
    public void chargeRefundCreatesCorrectJournalEntriesForPeriodicAccruals_Fee_Test() {
        chargeRefundCreatesCorrectJournalEntries(ACCRUAL_PERIODIC, false);
    }

    @Test
    public void chargeRefundCreatesCorrectJournalEntriesForCashAccounting_Fee_Test() {
        chargeRefundCreatesCorrectJournalEntries(CASH_BASED, false);
    }

    @Test
    public void chargeRefundCreatesCorrectJournalEntriesForPeriodicAccruals_Penalty_Test() {
        chargeRefundCreatesCorrectJournalEntries(ACCRUAL_PERIODIC, true);
    }

    @Test
    public void chargeRefundCreatesCorrectJournalEntriesForCashAccounting_Penalty_Test() {
        chargeRefundCreatesCorrectJournalEntries(CASH_BASED, true);
    }

    private void chargeRefundCreatesCorrectJournalEntries(final String accountingType, final boolean penalty) {

        final Double repaymentAmount = fullLoan + fullChargeRefundAmount;
        Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.CLOSED_OBLIGATIONS_MET, accountingType, penalty);

        LOG.info("-------------Loancharge Refund -----------");
        Long chargeRefundTxnId = loanChargeRefund(loanChargeId, oneThirdChargeRefundAmount);
        final String txnDate = getTodaysDate();

        Account incomeAccount = penalty ? penaltyIncomeAccount : feeIncomeAccount;

        checkJournalEntryForAssetAccount(assetAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, assetAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, overpaymentAccount, JournalEntry.TransactionType.CREDIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, incomeAccount, JournalEntry.TransactionType.DEBIT.name()));

        LOG.info("-------------Reverse Loancharge Refund -----------");
        adjustLoanTransaction(disbursedLoanID, chargeRefundTxnId, getTodaysDate());

        checkJournalEntryForAssetAccount(assetAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, assetAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForLiabilityAccount(overpaymentAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, overpaymentAccount, JournalEntry.TransactionType.DEBIT.name()));
        checkJournalEntryForIncomeAccount(incomeAccount, txnDate,
                journalEntry(oneThirdChargeRefundAmount, incomeAccount, JournalEntry.TransactionType.CREDIT.name()));
    }

    @Test
    public void repaymentReversalDisallowedIfLaterChargeRefundTest() {

        // repayment covers 2 installments plus charge
        final Double repaymentAmount = oneInstallment + oneInstallment + fullChargeRefundAmount;
        final Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, ACCRUAL_PERIODIC, false);

        final String repayment2Date = "20 January 2022";
        makeRepaymentType(MAKE_REPAYMENT_COMMAND, repayment2Date, oneInstallment); // installment 3
        Long repayment2Id = createdRepaymentTypeResourceId;

        LOG.info("-------------Loancharge Refund -----------");
        loanChargeRefund(loanChargeId, oneThirdChargeRefundAmount);

        LOG.info("-------------Reverse Repayment 2  -----------");
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> adjustLoanTransaction(disbursedLoanID, repayment2Id, getTodaysDate()));
        assertErrorGlobalisationCode(exception, "error.msg.loan.transaction.cant.be.reversed.because.later.charge.refund.exists");
    }

    @Test
    public void repaymentNotAllowedIfLaterChargeRefundTest() {

        // repayment covers 2 installments plus charge
        final Double repaymentAmount = oneInstallment + oneInstallment + fullChargeRefundAmount;
        final Long loanChargeId = disburseAddChargeAndRepay(repaymentAmount, LoanStatus.ACTIVE, ACCRUAL_PERIODIC, false);

        LOG.info("-------------Loancharge Refund -----------");
        loanChargeRefund(loanChargeId, oneThirdChargeRefundAmount);

        final String repayment2Date = "20 January 2022";
        CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                () -> makeRepaymentType(MAKE_REPAYMENT_COMMAND, repayment2Date, oneInstallment)); // installment 3
        assertErrorGlobalisationCode(exception, "error.msg.loan.transaction.cant.be.created.because.later.charge.refund.exists");
    }

    private Long loanChargeRefund(final Long loanChargeId, final Double refundAmount) {
        return transactionHelper
                .makeChargeRefund(disbursedLoanID,
                        new PostLoansLoanIdTransactionsRequest().locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN)
                                .loanChargeId(loanChargeId).transactionAmount(refundAmount).note("Loancharge Refund Made!!!"))
                .getResourceId();
    }

    private void disburseLoanOfAccountingRule(final String accountingType, final Double loanAmount, final String loanDate,
            final boolean penalty) {
        this.disbursedLoanID = fromStartToDisburseLoan(loanDate, loanAmount, penalty, accountingType, assetAccount, feeIncomeAccount,
                expenseAccount, overpaymentAccount);
    }

    private Long fromStartToDisburseLoan(String submitApproveDisburseDate, Double principal, final boolean penalty,
            final String accountingRule, final Account... accounts) {

        final Long clientId = createClient();

        boolean allowMultipleDisbursals = false;
        final Long loanProductId = createLoanProduct(principal, allowMultipleDisbursals, accountingRule, accounts);
        assertNotNull(loanProductId);

        LOG.info("-----------------------------------Setting Specific Penalty Income Account-----------------------------------------");
        updateLoanProduct(loanProductId,
                new PutLoanProductsProductIdRequest().incomeFromPenaltyAccountId(penaltyIncomeAccount.getAccountID().longValue()));

        final Long loanId = applyForLoanApplication(clientId, loanProductId, principal, submitApproveDisburseDate);
        assertNotNull(loanId);
        verifyLoanStatus(loanId, LoanStatus.SUBMITTED_AND_PENDING_APPROVAL);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        approveLoan(loanId, LoanRequestBuilders.approveLoan(principal, submitApproveDisburseDate));
        verifyLoanStatus(loanId, LoanStatus.APPROVED);

        LOG.info("-------------------------------DISBURSE LOAN -------------------------------------------");
        disburseLoan(loanId, submitApproveDisburseDate, principal);
        verifyLoanStatus(loanId, LoanStatus.ACTIVE);
        return loanId;
    }

    private void makeRepaymentType(final String repaymentTypeCommand, final String repaymentDate, final Double repayment) {
        LOG.info("-------------Make repayment Type -----------");
        createdRepaymentTypeResourceId = makeLoanRepayment(disbursedLoanID, repaymentTypeCommand, repaymentDate, repayment).getResourceId();
    }

    private Long createLoanProduct(final Double principal, final boolean multiDisburseLoan, final String accountingRule,
            final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal(principal.toString()) //
                .withNumberOfRepayments(FOUR_INSTALLMENTS) //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod(ZERO_INTEREST_RATE) //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        return createLoanProduct(builder.buildRequest(null));
    }

    private Long applyForLoanApplication(final Long clientId, final Long loanProductId, Double principal, String loanDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final PostLoansRequest application = LoanRequestBuilders.applyLoan(clientId, loanProductId, loanDate, principal,
                Integer.valueOf(FOUR_INSTALLMENTS));
        return applyForLoan(application);
    }

    private Long disburseAddChargeAndRepay(final Double repaymentAmount, final LoanStatus expectedPostRepaymentStatus,
            final String accountingType, final boolean penalty) {
        final String loanDate = "01 January 2022";
        disburseLoanOfAccountingRule(accountingType, fullLoan, loanDate, penalty);

        final Long charge = chargesHelper
                .createLoanSpecifiedDueDateCharge(ChargeCalculationType.PERCENT_OF_AMOUNT_AND_INTEREST, 1.0d, penalty).getResourceId();

        final String chargeDueDate = "15 February 2022"; // will be added to the 2nd installment (March)
        final Long loanChargeId = addChargesForLoan(disbursedLoanID, new PostLoansLoanIdChargesRequest().locale(LoanTestData.LOCALE)
                .dateFormat(LoanTestData.DATETIME_PATTERN).chargeId(charge).amount(1.0d).dueDate(chargeDueDate)).getResourceId();
        assertNotNull(loanChargeId);

        final String repaymentDate = "10 January 2022";
        makeRepaymentType(MAKE_REPAYMENT_COMMAND, repaymentDate, repaymentAmount);
        verifyLoanStatus(disbursedLoanID, expectedPostRepaymentStatus);
        return loanChargeId;
    }

    private Double getLoanDetailsSummaryTotalOutstanding(final Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        Double amount = Utils.getDoubleValue(loanDetails.getSummary().getTotalOutstanding());
        return amount == null ? 0.0d : amount;
    }

    private Double getLoanDetailsSummaryfeeChargesPaid(final Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        Double amount = Utils.getDoubleValue(loanDetails.getSummary().getFeeChargesPaid());
        return amount == null ? 0.0d : amount;
    }

    private Double getLoanDetailsTotalOverpaidAmount(final Long loanId) {
        Double amount = Utils.getDoubleValue(getLoanDetails(loanId).getTotalOverpaid());
        return amount == null ? 0.0d : amount;
    }

    private void verifyPaidByEntry(final Long loanId, final Long chargeRefundTxnId, final Double refundAmount) {
        Set<GetLoansLoanIdLoanChargePaidByData> loanChargePaidByList = getLoanTransactionDetails(loanId, chargeRefundTxnId)
                .getLoanChargePaidByList();
        assertNotNull(loanChargePaidByList);
        assertEquals(1, loanChargePaidByList.size());

        Double paidByAmount = Utils.getDoubleValue(loanChargePaidByList.iterator().next().getAmount()) * -1;
        assertEquals(refundAmount, paidByAmount, "Incorrect Paid By Amount");
    }

    private String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat(LoanTestData.DATETIME_PATTERN, Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        return dateFormat.format(todaysDate.getTime());
    }
}
