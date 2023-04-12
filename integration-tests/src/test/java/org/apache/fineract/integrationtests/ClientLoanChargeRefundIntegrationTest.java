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
import static org.junit.jupiter.api.Assertions.assertTrue;

import com.google.gson.Gson;
import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.charges.ChargesHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
public class ClientLoanChargeRefundIntegrationTest {

    private static final Logger LOG = LoggerFactory.getLogger(ClientLoanChargeRefundIntegrationTest.class);

    private Integer disbursedLoanID;
    private Account assetAccount;
    private Account feeIncomeAccount;
    private Account penaltyIncomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;
    private static final String ZERO_INTEREST_RATE = "0";
    private static final String FOUR_INSTALLMENTS = "4";
    private static final String NONE = "1";
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private static final String MAKE_REPAYMENT_COMMAND = "repayment";
    private static final String OVERPAID = "overpaid";
    private static final String CLOSED_OBLIGATION_MET = "closedObligationsMet";
    private static final String ACTIVE = "active";

    /*
     * loan disbursed: 4 installments of 3000; zero % interest; a specified due date charge of 120 is added and the
     * amount is allocated to installment 2; allocation strategy is penalty, fees, interest, principal
     */
    private static final Float oneInstallment = 3000.00f;
    private static final Float fullLoan = 3000.00f * 4;
    private static final Float fullChargeRefundAmount = 120.00f;
    private static final Float oneThirdChargeRefundAmount = 40.00f;

    private ResponseSpecification responseSpec;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanTransactionHelper loanTransactionHelperValidationError;
    private AccountHelper accountHelper;
    private JournalEntryHelper journalEntryHelper;
    private Integer createdRepaymentTypeResourceId;

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.loanTransactionHelperValidationError = new LoanTransactionHelper(this.requestSpec, new ResponseSpecBuilder().build());
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.assetAccount = this.accountHelper.createAssetAccount();
        this.feeIncomeAccount = this.accountHelper.createIncomeAccount();
        this.penaltyIncomeAccount = this.accountHelper.createIncomeAccount();
        this.expenseAccount = this.accountHelper.createExpenseAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Active() {
        testRefundAndReverseOfPaidChargeSucceeds(oneInstallment + fullChargeRefundAmount, fullChargeRefundAmount, ACTIVE, ACTIVE);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Com() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan, fullChargeRefundAmount, ACTIVE, CLOSED_OBLIGATION_MET);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Active_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + 50.00f, fullChargeRefundAmount, ACTIVE, OVERPAID);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Com_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + fullChargeRefundAmount, fullChargeRefundAmount, CLOSED_OBLIGATION_MET,
                OVERPAID);
    }

    @Test
    public void fullRefundAndReverseOfPaidChargeSucceedsTest_Overpaid_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + fullChargeRefundAmount + 50.00f, fullChargeRefundAmount, OVERPAID, OVERPAID);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Active() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan, oneThirdChargeRefundAmount, ACTIVE, ACTIVE);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Com() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + (oneThirdChargeRefundAmount * 2), oneThirdChargeRefundAmount, ACTIVE,
                CLOSED_OBLIGATION_MET);
    }

    @Test
    public void partialRefundAndReverseOfPaidChargeSucceedsTest_Active_Overpaid() {
        testRefundAndReverseOfPaidChargeSucceeds(fullLoan + (oneThirdChargeRefundAmount * 2) + 1.0f, oneThirdChargeRefundAmount, ACTIVE,
                OVERPAID);
    }

    private void testRefundAndReverseOfPaidChargeSucceeds(final Float repaymentAmount, final Float refundAmount,
            final String expectedPostRepaymentStatus, final String expectedPostRefundStatus) {
        // disburse, repay, add charge, charge refund and reverse charge refund
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, expectedPostRepaymentStatus, NONE, true);

        Float totalOutstandingPreRefund = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Float overpaidPreRefund = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        Float expectedTotalOutstandingPostRefund = null;
        Float expectedOverpaidPostRefund = null;
        if (totalOutstandingPreRefund.compareTo(refundAmount) >= 0) {
            expectedTotalOutstandingPostRefund = totalOutstandingPreRefund - refundAmount;
            expectedOverpaidPostRefund = 0.0f;
        } else {
            expectedTotalOutstandingPostRefund = 0.0f;
            if (totalOutstandingPreRefund == 0.0f) {
                expectedOverpaidPostRefund = overpaidPreRefund + refundAmount;
            } else {
                expectedOverpaidPostRefund = refundAmount - totalOutstandingPreRefund;
            }
        }

        LOG.info("-------------Loancharge Refund -----------");
        final Integer installmentNumber = null;
        final String externalId = null;
        Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber, refundAmount,
                externalId, this.disbursedLoanID, "resourceId");
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        assertTrue((Boolean) loanStatusHashMap.get(expectedPostRefundStatus), "Invalid Post Refund Status");

        Float totalOutstandingPostRefund = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Float overpaidPostRefund = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        Assertions.assertEquals(expectedTotalOutstandingPostRefund, totalOutstandingPostRefund, "Incorrect totalOutstanding Post Refund");
        Assertions.assertEquals(expectedOverpaidPostRefund, overpaidPostRefund, "Incorrect overpaid Post Refund");

        verifyPaidByEntry(disbursedLoanID, chargeRefundTxnId, refundAmount);

        LOG.info("-------------Reverse Loancharge Refund -----------");
        final String reverseDate = getTodaysDate();
        final Float adjustmentAmount = 0.0f;
        HashMap reverseHashMap = (HashMap) this.loanTransactionHelper.adjustLoanTransaction(disbursedLoanID, chargeRefundTxnId, reverseDate,
                adjustmentAmount.toString(), "");
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        assertTrue((Boolean) loanStatusHashMap.get(expectedPostRepaymentStatus), "Invalid Post Reversed Status");

        Float totalOutstandingPostReverse = getLoanDetailsSummaryTotalOutstanding(disbursedLoanID);
        Float overpaidPostReverse = getLoanDetailsTotalOverpaidAmount(disbursedLoanID);

        Assertions.assertEquals(totalOutstandingPreRefund, totalOutstandingPostReverse, "Incorrect totalOutstanding Post Reverse");
        Assertions.assertEquals(overpaidPreRefund, overpaidPostReverse, "Incorrect overpaid Post Reverse");

    }

    @Test
    public void refundOfUnpaidChargeFailsTest() {

        final Float repaymentAmount = 3000.00f; // pays installment one but none of added charge
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund -----------");
        final Float refundAmount = 60.00f;
        final Integer installmentNumber = null;
        final String externalId = null;
        ArrayList<HashMap> errors = (ArrayList<HashMap>) this.loanTransactionHelperValidationError.loanChargeRefund(loanChargeId,
                installmentNumber, refundAmount, externalId, this.disbursedLoanID, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.loan.charge.transaction.amount.is.more.than.is.refundable",
                errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    @Test
    public void refundingMoreThanPaidFailsTest() {

        final Float repaymentAmount = 3090.00f; // pays installment one and 90 (not all) of added charge
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund -----------");
        final Float refundAmount = 90.01f; // 0.01 more than paid.
        final Integer installmentNumber = null;
        final String externalId = null;
        ArrayList<HashMap> errors = (ArrayList<HashMap>) this.loanTransactionHelperValidationError.loanChargeRefund(loanChargeId,
                installmentNumber, refundAmount, externalId, this.disbursedLoanID, CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.loan.charge.transaction.amount.is.more.than.is.refundable",
                errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @Test
    public void onyRefundElementNotRepaymentElementUsedToCalculateRefundableAmountTest() {
        final Float chargeAmountPaid = 60.00f;
        final Float repaymentAmount = 3000.00f + chargeAmountPaid;
        // covers Installment 1 plus half of 120 charge added to installment 2
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, NONE, false);

        LOG.info("-------------Loancharge Refund 1 -----------");
        final Float refundAmount = chargeAmountPaid; // refund charge paid
        final Integer installmentNumber = null;
        final String externalId = null;
        Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber, refundAmount,
                externalId, this.disbursedLoanID, "resourceId");
        HashMap loanDetailsHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec,
                disbursedLoanID, "");
        // refund 60 pays off remainder of charge leaving an amount 60 that could be refunded

        LOG.info("-------------Loancharge Refund 2 -----------");
        final Float smallRefund = 0.01f;
        chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber, smallRefund, externalId,
                this.disbursedLoanID, "resourceId");
        loanDetailsHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID, "");

    }

    @Test
    public void refundOfPartiallyPaidChargeCanRepayMoreOfSameChargeTest() {
        final Float chargeAmountPaid = 80.00f;
        final Float chargeAmountFull = 120.00f;
        final Float chargeAmountOutstanding = chargeAmountFull - chargeAmountPaid;
        final Float repaymentAmount = 3000.00f + chargeAmountPaid;
        // covers Installment 1 plus two thirds of 120 charge added to installment 2
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, NONE, false);
        Float feeChargesPaid = getLoanDetailsSummaryfeeChargesPaid(disbursedLoanID);
        Assertions.assertEquals(feeChargesPaid, chargeAmountPaid, "Incorrect Partial feeChargesPaid");

        LOG.info("-------------Loancharge Refund -----------");
        final Float refundAmount = chargeAmountPaid; // refund charge paid
        final Integer installmentNumber = null;
        final String externalId = null;
        Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber, refundAmount,
                externalId, this.disbursedLoanID, "resourceId");
        feeChargesPaid = getLoanDetailsSummaryfeeChargesPaid(disbursedLoanID);
        Assertions.assertEquals(feeChargesPaid, chargeAmountFull, "Incorrect Full feeChargesPaid");

        ArrayList<HashMap> loanChargePaidByList = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanTransactionDetails(disbursedLoanID,
                chargeRefundTxnId, "loanChargePaidByList");
        Assertions.assertNotNull(loanChargePaidByList);
        Assertions.assertEquals(loanChargePaidByList.size(), 2);
        // expecting 2 entries 1)-80 refund 2) 40 repayment
        Float paidByAmount1 = (Float) loanChargePaidByList.get(0).get("amount");
        Assertions.assertNotNull(paidByAmount1);
        Assertions.assertNotEquals(paidByAmount1, 0.0f);
        Float paidByAmount2 = (Float) loanChargePaidByList.get(1).get("amount");
        Assertions.assertNotNull(paidByAmount2);
        Assertions.assertNotEquals(paidByAmount2, 0.0f);

        if (paidByAmount1 < 0.0f) {
            Assertions.assertEquals(paidByAmount1, chargeAmountPaid * -1, "Refund Element Incorrect");
        } else {
            Assertions.assertEquals(paidByAmount1, chargeAmountOutstanding, "Repayment Element Incorrect");
        }
        if (paidByAmount2 < 0.0f) {
            Assertions.assertEquals(paidByAmount2, chargeAmountPaid * -1, "Refund Element Incorrect");
        } else {
            Assertions.assertEquals(paidByAmount2, chargeAmountOutstanding, "Repayment Element Incorrect");
        }

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

        final Float repaymentAmount = fullLoan + fullChargeRefundAmount;
        Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, CLOSED_OBLIGATION_MET, accountingType, penalty);

        LOG.info("-------------Loancharge Refund -----------");
        final Integer installmentNumber = null;
        final String externalId = null;
        Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber,
                oneThirdChargeRefundAmount, externalId, this.disbursedLoanID, "resourceId");
        final String txnDate = getTodaysDate();

        Account incomeAccount = feeIncomeAccount;
        if (penalty) {
            incomeAccount = penaltyIncomeAccount;
        }

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.DEBIT));

        LOG.info("-------------Reverse Loancharge Refund -----------");
        final String reverseDate = getTodaysDate();
        final Float adjustmentAmount = 0.0f;
        HashMap reverseHashMap = (HashMap) this.loanTransactionHelper.adjustLoanTransaction(disbursedLoanID, chargeRefundTxnId, reverseDate,
                adjustmentAmount.toString(), "");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.DEBIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, txnDate,
                new JournalEntry(oneThirdChargeRefundAmount, JournalEntry.TransactionType.CREDIT));

    }

    @Test
    public void repaymentReversalDisallowedIfLaterChargeRefundTest() {

        // repayment covers 2 installments plus charge
        final Float repaymentAmount = oneInstallment + oneInstallment + fullChargeRefundAmount;
        final Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, ACCRUAL_PERIODIC, false);
        final Integer repayment1Id = createdRepaymentTypeResourceId;

        final String repayment2Date = "20 January 2022";
        final Float repayment2Amount = oneInstallment; // installment 3
        makeRepaymentType(MAKE_REPAYMENT_COMMAND, repayment2Date, repayment2Amount);
        Integer repayment2Id = createdRepaymentTypeResourceId;

        LOG.info("-------------Loancharge Refund -----------");
        final Integer installmentNumber = null;
        final String externalId = null;
        final Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber,
                oneThirdChargeRefundAmount, externalId, this.disbursedLoanID, "resourceId");

        final String reverseDate = getTodaysDate();
        final Float adjustmentAmount = 0.0f;
        LOG.info("-------------Reverse Repayment 2  -----------");
        ArrayList<HashMap> errors = (ArrayList<HashMap>) this.loanTransactionHelperValidationError.adjustLoanTransaction(disbursedLoanID,
                repayment2Id, reverseDate, adjustmentAmount.toString(), CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.loan.transaction.cant.be.reversed.because.later.charge.refund.exists",
                errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
    }

    @Test
    public void repaymentNotAllowedIfLaterChargeRefundTest() {

        // repayment covers 2 installments plus charge
        final Float repaymentAmount = oneInstallment + oneInstallment + fullChargeRefundAmount;
        final Integer loanChargeId = disburseAddChargeAndRepay(repaymentAmount, ACTIVE, ACCRUAL_PERIODIC, false);

        LOG.info("-------------Loancharge Refund -----------");
        final Integer installmentNumber = null;
        final String externalId = null;
        final Integer chargeRefundTxnId = (Integer) this.loanTransactionHelper.loanChargeRefund(loanChargeId, installmentNumber,
                oneThirdChargeRefundAmount, externalId, this.disbursedLoanID, "resourceId");

        final String repayment2Date = "20 January 2022";
        final Float repayment2Amount = oneInstallment; // installment 3
        ArrayList<HashMap> errors = (ArrayList<HashMap>) this.loanTransactionHelperValidationError.makeRepaymentTypePayment(
                MAKE_REPAYMENT_COMMAND, repayment2Date, repayment2Amount, this.disbursedLoanID, CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.loan.transaction.cant.be.created.because.later.charge.refund.exists",
                errors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    private void disburseLoanOfAccountingRule(final String accountingType, final String loanAmount, final String loanDate,
            final boolean penalty) {
        this.disbursedLoanID = fromStartToDisburseLoan(loanDate, loanAmount, penalty, accountingType, assetAccount, feeIncomeAccount,
                expenseAccount, overpaymentAccount);
    }

    private Integer fromStartToDisburseLoan(String submitApproveDisburseDate, String principal, final boolean penalty,
            final String accountingRule, final Account... accounts) {

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        boolean allowMultipleDisbursals = false;
        final Integer loanProductID = createLoanProduct(principal, allowMultipleDisbursals, accountingRule, accounts);
        Assertions.assertNotNull(loanProductID);
        // if (penalty) {
        LOG.info("-----------------------------------Setting Specific Penalty Income Account-----------------------------------------");
        final String accountId = penaltyIncomeAccount.getAccountID().toString();
        final String putURL = "/fineract-provider/api/v1/loanproducts/" + loanProductID + "?" + Utils.TENANT_IDENTIFIER;

        final HashMap<String, Object> map = new HashMap<>();
        map.put("incomeFromPenaltyAccountId", accountId);
        final String jsonBodyToSend = new Gson().toJson(map);
        Utils.performServerPut(requestSpec, responseSpec, putURL, jsonBodyToSend);
        // }

        final List<HashMap> charges = null;
        final Integer loanID = applyForLoanApplication(clientID, loanProductID, charges, principal, submitApproveDisburseDate);
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        LOG.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(submitApproveDisburseDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        LOG.info("-------------------------------DISBURSE LOAN -------------------------------------------"); //
        // String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(submitApproveDisburseDate, loanID, principal);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        return loanID;
    }

    private HashMap makeRepaymentType(final String repaymentTypeCommand, final String repaymentDate, final Float repayment) {
        LOG.info("-------------Make repayment Type -----------");
        createdRepaymentTypeResourceId = (Integer) this.loanTransactionHelper.makeRepaymentTypePayment(repaymentTypeCommand, repaymentDate,
                repayment, this.disbursedLoanID, "resourceId");
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        return loanStatusHashMap;
    }

    private static String getLoanChargeAsJSON(final String chargeId, final String dueDate, final String amount, final String externalId) {
        final HashMap<String, String> map = new HashMap<>();
        map.put("locale", "en_GB");
        map.put("dateFormat", "dd MMMM yyyy");
        map.put("amount", amount);
        map.put("dueDate", dueDate);
        map.put("chargeId", chargeId);
        map.put("externalId", externalId);
        String json = new Gson().toJson(map);
        LOG.info("{}", json);
        return json;
    }

    private Integer createLoanProduct(final String principal, final boolean multiDisburseLoan, final String accountingRule,
            final Account... accounts) {
        LOG.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        LoanProductTestBuilder builder = new LoanProductTestBuilder() //
                .withPrincipal(principal) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("0") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withTranches(multiDisburseLoan) //
                .withAccounting(accountingRule, accounts);
        if (multiDisburseLoan) {
            builder = builder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
        }
        final String loanProductJSON = builder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, List<HashMap> charges, String principal,
            String loanDate) {
        LOG.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String savingsId = null;
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments(FOUR_INSTALLMENTS) //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod(ZERO_INTEREST_RATE) //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(loanDate) //
                .withSubmittedOnDate(loanDate) //
                .withCharges(charges).build(clientID.toString(), loanProductID.toString(), savingsId);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer disburseAddChargeAndRepay(final Float repaymentAmount, final String expectedPostRepaymentStatus,
            final String accountingType, final boolean penalty) {
        final String loanDate = "01 January 2022";
        final String loanAmount = "12,000.00";
        disburseLoanOfAccountingRule(accountingType, loanAmount, loanDate, penalty);

        final Integer charge = ChargesHelper.createCharges(requestSpec, responseSpec, ChargesHelper
                .getLoanSpecifiedDueDateJSON(ChargesHelper.CHARGE_CALCULATION_TYPE_PERCENTAGE_AMOUNT_AND_INTEREST, "1", penalty));

        final String externalId = null;
        final float amount = 1.0f;
        final String chargeDueDate = "15 February 2022"; // will be added to the 2nd installment (March)
        final Integer loanChargeId = this.loanTransactionHelper.addChargesForLoan(this.disbursedLoanID,
                getLoanChargeAsJSON(String.valueOf(charge), chargeDueDate, String.valueOf(amount), externalId));
        Assertions.assertNotNull(loanChargeId);

        final String repaymentDate = "10 January 2022";
        HashMap loanStatusHashMap = makeRepaymentType(MAKE_REPAYMENT_COMMAND, repaymentDate, repaymentAmount);
        assertTrue((Boolean) loanStatusHashMap.get(expectedPostRepaymentStatus));
        return loanChargeId;
    }

    private Float getLoanDetailsSummaryTotalOutstanding(final Integer loanId) {

        HashMap loanDetailsHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanId,
                "summary");
        Float amount = (Float) loanDetailsHashMap.get("totalOutstanding");
        if (amount == null) {
            amount = 0.0f;
        }
        return amount;
    }

    private Float getLoanDetailsSummaryfeeChargesPaid(final Integer loanId) {

        HashMap loanDetailsHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanId,
                "summary");
        Float amount = (Float) loanDetailsHashMap.get("feeChargesPaid");
        if (amount == null) {
            amount = 0.0f;
        }
        return amount;
    }

    private Float getLoanDetailsTotalOverpaidAmount(final Integer loanId) {

        Float amount = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, loanId, "totalOverpaid");
        if (amount == null) {
            amount = 0.0f;
        }
        return amount;
    }

    private void verifyPaidByEntry(final Integer loanId, final Integer chargeRefundTxnId, final Float refundAmount) {
        ArrayList<HashMap> loanChargePaidByList = (ArrayList<HashMap>) this.loanTransactionHelper.getLoanTransactionDetails(loanId,
                chargeRefundTxnId, "loanChargePaidByList");
        Assertions.assertNotNull(loanChargePaidByList);
        Assertions.assertEquals(loanChargePaidByList.size(), 1);

        Float paidByAmount = (Float) loanChargePaidByList.get(0).get("amount") * -1;
        Assertions.assertEquals(refundAmount, paidByAmount, "Incorrect Paid By Amount");
    }

    private String getTodaysDate() {
        DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", Locale.US);
        dateFormat.setTimeZone(Utils.getTimeZoneOfTenant());
        Calendar todaysDate = Calendar.getInstance(Utils.getTimeZoneOfTenant());
        return dateFormat.format(todaysDate.getTime());
    }

}
