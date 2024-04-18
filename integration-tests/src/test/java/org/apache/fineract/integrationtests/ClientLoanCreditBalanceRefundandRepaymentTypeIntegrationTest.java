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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

import io.restassured.builder.RequestSpecBuilder;
import io.restassured.builder.ResponseSpecBuilder;
import io.restassured.http.ContentType;
import io.restassured.specification.RequestSpecification;
import io.restassured.specification.ResponseSpecification;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.UUID;
import java.util.stream.Stream;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.AdvancedPaymentData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.PaymentAllocationOrder;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoanProductsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PostLoansResponse;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.CommonConstants;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.AccountHelper;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.accounting.JournalEntryHelper;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanStatusChecker;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.loans.LoanTransactionHelper;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.apache.fineract.portfolio.loanproduct.domain.PaymentAllocationType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

@SuppressWarnings({ "rawtypes", "unchecked" })
@ExtendWith(LoanTestLifecycleExtension.class)
@Slf4j
public class ClientLoanCreditBalanceRefundandRepaymentTypeIntegrationTest extends BaseLoanIntegrationTest {

    private ResponseSpecification responseSpec;
    private ResponseSpecification responseSpec403;
    private RequestSpecification requestSpec;
    private LoanTransactionHelper loanTransactionHelper;
    private LoanTransactionHelper loanTransactionHelperValidationError;
    private JournalEntryHelper journalEntryHelper;
    private AccountHelper accountHelper;
    private Integer disbursedLoanID;
    private static final String CASH_BASED = "2";
    private static final String ACCRUAL_PERIODIC = "3";
    private Account assetAccount;
    private Account incomeAccount;
    private Account expenseAccount;
    private Account overpaymentAccount;
    private static final String REPAYMENT = "repayment";
    private static final String MERCHANT_ISSUED_REFUND = "merchantIssuedRefund";
    private static final String PAYOUT_REFUND = "payoutRefund";
    private static final String GOODWILL_CREDIT = "goodwillCredit";

    @BeforeEach
    public void setup() {
        Utils.initializeRESTAssured();
        this.requestSpec = new RequestSpecBuilder().setContentType(ContentType.JSON).build();
        this.requestSpec.header("Authorization", "Basic " + Utils.loginIntoServerAndGetBase64EncodedAuthenticationKey());
        this.responseSpec = new ResponseSpecBuilder().expectStatusCode(200).build();
        this.responseSpec403 = new ResponseSpecBuilder().expectStatusCode(403).build();
        this.loanTransactionHelper = new LoanTransactionHelper(this.requestSpec, this.responseSpec);
        this.loanTransactionHelperValidationError = new LoanTransactionHelper(this.requestSpec, new ResponseSpecBuilder().build());
        this.accountHelper = new AccountHelper(this.requestSpec, this.responseSpec);
        this.assetAccount = this.accountHelper.createAssetAccount();
        this.incomeAccount = this.accountHelper.createIncomeAccount();
        this.expenseAccount = this.accountHelper.createExpenseAccount();
        this.overpaymentAccount = this.accountHelper.createLiabilityAccount();
        this.journalEntryHelper = new JournalEntryHelper(this.requestSpec, this.responseSpec);
    }

    private void disburseLoanOfAccountingRule(final String accountingType, LoanProductTestBuilder loanProductTestBuilder) {
        final String principal = "12000.00";
        final String submitApproveDisburseDate = "01 January 2022";
        this.disbursedLoanID = fromStartToDisburseLoan(loanProductTestBuilder, submitApproveDisburseDate, principal, accountingType,
                assetAccount, incomeAccount, expenseAccount, overpaymentAccount);
    }

    private Integer createLoanProduct(LoanProductTestBuilder loanProductTestBuilder, final String principal,
            final boolean multiDisburseLoan, final String accountingRule, final Account... accounts) {
        log.info("------------------------------CREATING NEW LOAN PRODUCT ---------------------------------------");
        loanProductTestBuilder = loanProductTestBuilder //
                .withPrincipal(principal) //
                .withShortName(Utils.uniqueRandomStringGenerator("", 4)) //
                .withNumberOfRepayments("4") //
                .withRepaymentAfterEvery("1") //
                .withRepaymentTypeAsMonth() //
                .withinterestRatePerPeriod("1") //
                .withInterestRateFrequencyTypeAsMonths() //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withAccounting(accountingRule, accounts) //
                .withTranches(multiDisburseLoan);
        if (multiDisburseLoan) {
            loanProductTestBuilder = loanProductTestBuilder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
            loanProductTestBuilder = loanProductTestBuilder.withMaxTrancheCount("30");
        }
        final String loanProductJSON = loanProductTestBuilder.build(null);
        return this.loanTransactionHelper.getLoanProductId(loanProductJSON);
    }

    private Integer applyForLoanApplication(final Integer clientID, final Integer loanProductID, String principal, String submitDate,
            String repaymentStrategy) {
        log.info("--------------------------------APPLYING FOR LOAN APPLICATION--------------------------------");
        final String loanApplicationJSON = new LoanApplicationTestBuilder() //
                .withPrincipal(principal) //
                .withLoanTermFrequency("4") //
                .withLoanTermFrequencyAsMonths() //
                .withNumberOfRepayments("4") //
                .withRepaymentEveryAfter("1") //
                .withRepaymentFrequencyTypeAsMonths() //
                .withInterestRatePerPeriod("2") //
                .withAmortizationTypeAsEqualInstallments() //
                .withInterestTypeAsDecliningBalance() //
                .withInterestCalculationPeriodTypeSameAsRepaymentPeriod() //
                .withExpectedDisbursementDate(submitDate) //
                .withSubmittedOnDate(submitDate) //
                .withRepaymentStrategy(repaymentStrategy) //
                .build(clientID.toString(), loanProductID.toString(), null);
        return this.loanTransactionHelper.getLoanId(loanApplicationJSON);
    }

    private Integer fromStartToDisburseLoan(LoanProductTestBuilder loanProductTestBuilder, String submitApproveDisburseDate,
            String principal, final String accountingRule, final Account... accounts) {

        final Integer clientID = ClientHelper.createClient(this.requestSpec, this.responseSpec);
        ClientHelper.verifyClientCreatedOnServer(this.requestSpec, this.responseSpec, clientID);

        boolean allowMultipleDisbursals = false;
        final Integer loanProductID = createLoanProduct(loanProductTestBuilder, principal, allowMultipleDisbursals, accountingRule,
                accounts);
        Assertions.assertNotNull(loanProductID);

        final Integer loanID = applyForLoanApplication(clientID, loanProductID, principal, submitApproveDisburseDate,
                loanProductTestBuilder.getTransactionProcessingStrategyCode());
        Assertions.assertNotNull(loanID);
        HashMap loanStatusHashMap = LoanStatusChecker.getStatusOfLoan(this.requestSpec, this.responseSpec, loanID);
        LoanStatusChecker.verifyLoanIsPending(loanStatusHashMap);

        log.info("-----------------------------------APPROVE LOAN-----------------------------------------");
        loanStatusHashMap = this.loanTransactionHelper.approveLoan(submitApproveDisburseDate, loanID);
        LoanStatusChecker.verifyLoanIsApproved(loanStatusHashMap);
        LoanStatusChecker.verifyLoanIsWaitingForDisbursal(loanStatusHashMap);

        log.info("-------------------------------DISBURSE LOAN -------------------------------------------"); //
        // String loanDetails = this.loanTransactionHelper.getLoanDetails(this.requestSpec, this.responseSpec, loanID);
        loanStatusHashMap = this.loanTransactionHelper.disburseLoanWithNetDisbursalAmount(submitApproveDisburseDate, loanID, principal);
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);
        return loanID;
    }

    private HashMap makeRepayment(final String repaymentDate, final Float repayment) {
        log.info("-------------Make repayment -----------");
        this.loanTransactionHelper.makeRepayment(repaymentDate, repayment, disbursedLoanID);
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        return loanStatusHashMap;
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void creditBalanceRefundCanOnlyBeAppliedWhereLoanStatusIsOverpaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 2000.00f); // not full payment
        LoanStatusChecker.verifyLoanIsActive(loanStatusHashMap);

        final String creditBalanceRefundDate = "09 January 2022";
        final Float refund = 1000.00f;
        final String externalId = null;
        ArrayList<HashMap> cbrErrors = (ArrayList<HashMap>) loanTransactionHelperValidationError
                .creditBalanceRefund(creditBalanceRefundDate, refund, externalId, disbursedLoanID, CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.loan.credit.balance.refund.account.is.not.overpaid",
                cbrErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void cantRefundMoreThanOverpaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final String creditBalanceRefundDate = "09 January 2022";
        Float refund = 10000.00f;
        final String externalId = null;
        ArrayList<HashMap> cbrErrors = (ArrayList<HashMap>) loanTransactionHelperValidationError
                .creditBalanceRefund(creditBalanceRefundDate, refund, externalId, disbursedLoanID, CommonConstants.RESPONSE_ERROR);

        assertEquals("error.msg.transactionAmount.invalid.must.be.>zero.and<=overpaidamount",
                cbrErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

        refund = (float) -1.00;
        cbrErrors = (ArrayList<HashMap>) loanTransactionHelperValidationError.creditBalanceRefund(creditBalanceRefundDate, refund,
                externalId, disbursedLoanID, CommonConstants.RESPONSE_ERROR);
        assertEquals("validation.msg.loan.transaction.transactionAmount.not.greater.than.zero",
                cbrErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void fullRefundChangesStatusToClosedObligationMetTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float totalOverpaid = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "totalOverpaid");

        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = null;
        loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, totalOverpaid, externalId, disbursedLoanID, null);
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

        final Float floatZero = 0.0f;
        Float totalOverpaidAtEnd = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "totalOverpaid");
        if (totalOverpaidAtEnd == null) {
            totalOverpaidAtEnd = floatZero;
        }
        assertEquals(totalOverpaidAtEnd, floatZero);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void refundAcceptedOnTheCurrentBusinessDate(LoanProductTestBuilder loanProductTestBuilder) {
        runAt("09 January 2022", () -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
            LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

            final Float totalOverpaid = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec,
                    disbursedLoanID, "totalOverpaid");

            final String creditBalanceRefundDate = "09 January 2022";
            final String externalId = null;
            loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, totalOverpaid, externalId, disbursedLoanID, null);
            loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                    "status");
            LoanStatusChecker.verifyLoanAccountIsClosed(loanStatusHashMap);

            final Float floatZero = 0.0f;
            Float totalOverpaidAtEnd = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec,
                    disbursedLoanID, "totalOverpaid");
            if (totalOverpaidAtEnd == null) {
                totalOverpaidAtEnd = floatZero;
            }
            assertEquals(totalOverpaidAtEnd, floatZero);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void refundCannotBeDuneForFutureDate(LoanProductTestBuilder loanProductTestBuilder) {
        runAt("06 January 2022", () -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
            LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

            final Float totalOverpaid = (Float) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec,
                    disbursedLoanID, "totalOverpaid");

            final String creditBalanceRefundDate = "09 January 2022";
            final String externalId = null;

            ArrayList<HashMap> cbrErrors = (ArrayList<HashMap>) loanTransactionHelperValidationError.creditBalanceRefund(
                    creditBalanceRefundDate, totalOverpaid, externalId, disbursedLoanID, CommonConstants.RESPONSE_ERROR);

            assertEquals("error.msg.transaction.date.cannot.be.in.the.future",
                    cbrErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void partialRefundKeepsOverpaidStatusTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float refund = 5000.00f; // partial refund

        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = null;
        loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, refund, externalId, disbursedLoanID, null);
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanDetail(this.requestSpec, this.responseSpec, disbursedLoanID,
                "status");
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundSavesExternalIdTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float refund = 1000.00f; // partial refund
        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = "cbrextID" + disbursedLoanID.toString();
        Integer resourceId = (Integer) loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, refund, externalId,
                disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);

        HashMap creditBalanceRefundMap = (HashMap) this.loanTransactionHelper.getLoanTransactionDetails(disbursedLoanID, resourceId, "");
        Assertions.assertNotNull(creditBalanceRefundMap.get("externalId"));
        Assertions.assertEquals(creditBalanceRefundMap.get("externalId"), externalId, "Incorrect External Id Saved");

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundFindsDuplicateExternalIdTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float refund = 1000.00f; // partial refund
        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = "cbrextID" + disbursedLoanID.toString();
        final Integer resourceId = (Integer) loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, refund, externalId,
                disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);

        final Float refund2 = 10.00f; // partial refund
        final String creditBalanceRefundDate2 = "10 January 2022";
        ArrayList<HashMap> cbrErrors = (ArrayList<HashMap>) loanTransactionHelperValidationError
                .creditBalanceRefund(creditBalanceRefundDate2, refund2, externalId, disbursedLoanID, CommonConstants.RESPONSE_ERROR);
        assertEquals("error.msg.loan.creditBalanceRefund.duplicate.externalId",
                cbrErrors.get(0).get(CommonConstants.RESPONSE_ERROR_MESSAGE_CODE));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundCreatesCorrectJournalEntriesForPeriodicAccrualsTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("06 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float refund = 1000.00f; // partial refund
        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = null;
        final Integer resourceId = (Integer) loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, refund, externalId,
                disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, creditBalanceRefundDate,
                new JournalEntry(refund, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, creditBalanceRefundDate,
                new JournalEntry(refund, JournalEntry.TransactionType.DEBIT));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundCreatesCorrectJournalEntriesForCashAccountingTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanStatusHashMap = makeRepayment("08 January 2022", 20000.00f); // overpayment
        LoanStatusChecker.verifyLoanAccountIsOverPaid(loanStatusHashMap);

        final Float refund = 1000.00f; // partial refund
        final String creditBalanceRefundDate = "09 January 2022";
        final String externalId = null;
        final Integer resourceId = (Integer) loanTransactionHelper.creditBalanceRefund(creditBalanceRefundDate, refund, externalId,
                disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, creditBalanceRefundDate,
                new JournalEntry(refund, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, creditBalanceRefundDate,
                new JournalEntry(refund, JournalEntry.TransactionType.DEBIT));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void repaymentTransactionTypeMatchesTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        verifyRepaymentTransactionTypeMatches(MERCHANT_ISSUED_REFUND);
        verifyRepaymentTransactionTypeMatches(PAYOUT_REFUND);
        verifyRepaymentTransactionTypeMatches(GOODWILL_CREDIT);

    }

    private void verifyRepaymentTransactionTypeMatches(final String repaymentTransactionType) {
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.makeRepaymentTypePayment(repaymentTransactionType,
                "06 January 2022", 200.00f, this.disbursedLoanID, "");
        Integer newTransactionId = (Integer) loanStatusHashMap.get("resourceId");
        loanStatusHashMap = (HashMap) this.loanTransactionHelper.getLoanTransactionDetails(this.disbursedLoanID, newTransactionId, "");

        HashMap typeMap = (HashMap) loanStatusHashMap.get("type");
        Boolean isTypeCorrect = (Boolean) typeMap.get(repaymentTransactionType);
        Assertions.assertTrue(Boolean.TRUE.equals(isTypeCorrect), "Not " + repaymentTransactionType);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void repaymentTransactionTypeWhenPaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        verifyRepaymentTransactionTypeWhenPaid(MERCHANT_ISSUED_REFUND);
        verifyRepaymentTransactionTypeWhenPaid(PAYOUT_REFUND);
        verifyRepaymentTransactionTypeWhenPaid(GOODWILL_CREDIT);
        verifyRepaymentTransactionTypeWhenPaid(REPAYMENT);

    }

    private void verifyRepaymentTransactionTypeWhenPaid(final String repaymentTransactionType) {

        // Overpay loan
        Integer resourceId = (Integer) this.loanTransactionHelper.makeRepaymentTypePayment(REPAYMENT, "06 January 2022", 13000.00f,
                this.disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);
        resourceId = (Integer) this.loanTransactionHelper.makeRepaymentTypePayment(repaymentTransactionType, "06 January 2022", 1.00f,
                this.disbursedLoanID, "resourceId");
        Assertions.assertNotNull(resourceId);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void goodWillCreditWillCloseTheLoanCorrectly(LoanProductTestBuilder loanProductTestBuilder) {

        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float goodwillAmount = totalOutstanding;
        final String goodwillDate = "09 March 2022";
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate,
                goodwillAmount, this.disbursedLoanID, "");

        GetLoansLoanIdResponse details = this.loanTransactionHelper.getLoan(this.requestSpec, this.responseSpec, disbursedLoanID);

        Assertions.assertNull(details.getSummary().getInArrears());
        Assertions.assertTrue(details.getStatus().getClosedObligationsMet());
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void paymentRefundWillCloseTheLoanCorrectly(LoanProductTestBuilder loanProductTestBuilder) {

        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float goodwillAmount = totalOutstanding;
        final String goodwillDate = "09 March 2022";
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.makeRepaymentTypePayment(PAYOUT_REFUND, goodwillDate,
                goodwillAmount, this.disbursedLoanID, "");

        GetLoansLoanIdResponse details = this.loanTransactionHelper.getLoan(this.requestSpec, this.responseSpec, disbursedLoanID);

        Assertions.assertNull(details.getSummary().getInArrears());
        Assertions.assertTrue(details.getStatus().getClosedObligationsMet());
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newGoodwillCreditCreatesCorrectJournalEntriesForPeriodicAccrualsTest(LoanProductTestBuilder loanProductTestBuilder) {

        disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float goodwillAmount = totalOutstanding + overpaidAmount;
        final Float goodwillAmountInExpense = principalOutstanding + overpaidAmount;
        final String goodwillDate = "09 January 2022";
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate,
                goodwillAmount, this.disbursedLoanID, "");

        // only a single credit for principal and interest as test sets up same GL account for both (summed up)
        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, goodwillDate,
                new JournalEntry(totalOutstanding, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, goodwillDate,
                new JournalEntry(overpaidAmount, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, goodwillDate,
                new JournalEntry(goodwillAmountInExpense, JournalEntry.TransactionType.DEBIT));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newGoodwillCreditCreatesCorrectJournalEntriesForCashAccountingTest(LoanProductTestBuilder loanProductTestBuilder) {

        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float goodwillAmount = totalOutstanding + overpaidAmount;
        final Float goodwillAmountInExpense = principalOutstanding + overpaidAmount;
        final String goodwillDate = "09 January 2022";
        HashMap loanStatusHashMap = (HashMap) this.loanTransactionHelper.makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate,
                goodwillAmount, this.disbursedLoanID, "");

        this.journalEntryHelper.checkJournalEntryForAssetAccount(assetAccount, goodwillDate,
                new JournalEntry(principalOutstanding, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForIncomeAccount(incomeAccount, goodwillDate,
                new JournalEntry(interestOutstanding, JournalEntry.TransactionType.CREDIT));

        this.journalEntryHelper.checkJournalEntryForLiabilityAccount(overpaymentAccount, goodwillDate,
                new JournalEntry(overpaidAmount, JournalEntry.TransactionType.CREDIT));
        this.journalEntryHelper.checkJournalEntryForExpenseAccount(expenseAccount, goodwillDate,
                new JournalEntry(goodwillAmountInExpense, JournalEntry.TransactionType.DEBIT));

    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoGoodWillCreditTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(GOODWILL_CREDIT,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.reverseLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoPayoutRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(PAYOUT_REFUND,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.reverseLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoMerchantIssuedRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(MERCHANT_ISSUED_REFUND,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.reverseLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustGoodWillCreditTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(GOODWILL_CREDIT,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.adjustLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec403);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustPayoutRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(PAYOUT_REFUND,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.adjustLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec403);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustMerchantIssuedRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        // Given
        disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
        HashMap loanSummaryMap = this.loanTransactionHelper.getLoanSummary(this.requestSpec, this.responseSpec, disbursedLoanID);

        // pay off all of principal, interest (no fees or penalties)
        final Float principalOutstanding = (Float) loanSummaryMap.get("principalOutstanding");
        final Float interestOutstanding = (Float) loanSummaryMap.get("interestOutstanding");
        final Float totalOutstanding = (Float) loanSummaryMap.get("totalOutstanding");
        final Float overpaidAmount = 159.00f;
        final Float transactionAmount = totalOutstanding + overpaidAmount;
        final String transactionDate = "09 January 2022";
        PostLoansLoanIdTransactionsResponse loanTransactionResponse = loanTransactionHelper.makeLoanRepayment(MERCHANT_ISSUED_REFUND,
                transactionDate, transactionAmount, this.disbursedLoanID);
        Assertions.assertNotNull(loanTransactionResponse);
        Assertions.assertNotNull(loanTransactionResponse.getResourceId());

        // Then
        loanTransactionHelper.adjustLoanTransaction(this.disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate,
                responseSpec403);
    }

    @Test
    public void cbrReverseReplayTest() {
        runAt("06 March 2024", () -> {
            Long clientId = clientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
            PostLoanProductsRequest product = createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(1)
                    .repaymentEvery(30).enableDownPayment(false);
            PostLoanProductsResponse loanProductResponse = loanProductHelper.createLoanProduct(product);
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductResponse.getResourceId(), "25 January 2024", 1000.0,
                    4);

            applicationRequest = applicationRequest.numberOfRepayments(1).loanTermFrequency(30)
                    .transactionProcessingStrategyCode(
                            LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                    .repaymentEvery(30);

            PostLoansResponse loanResponse = loanTransactionHelper.applyLoan(applicationRequest);

            loanTransactionHelper.approveLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                            .approvedOnDate("25 January 2024").locale("en"));

            loanTransactionHelper.disburseLoan(loanResponse.getLoanId(),
                    new PostLoansLoanIdRequest().actualDisbursementDate("25 January 2024").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(BigDecimal.valueOf(100.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 100.0, 0.0, 100.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 0.0, 100.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            String repaymentExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeLoanRepayment(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("24 February 2024").locale("en")
                            .transactionAmount(100.0).externalId(repaymentExternalId));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            String mir1ExternalId = UUID.randomUUID().toString();
            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(),
                    new PostLoansLoanIdTransactionsRequest().transactionDate("28 February 2024").dateFormat(DATETIME_PATTERN)
                            .transactionAmount(36.99).locale("en").externalId(mir1ExternalId));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 36.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("28 February 2024").dateFormat(DATETIME_PATTERN).transactionAmount(18.94).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 55.93);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("28 February 2024").dateFormat(DATETIME_PATTERN).transactionAmount(36.99).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 92.92);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("28 February 2024").dateFormat(DATETIME_PATTERN).transactionAmount(31.99).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 124.91);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeCreditBalanceRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("01 March 2024").dateFormat(DATETIME_PATTERN).transactionAmount(124.91).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("02 March 2024").dateFormat(DATETIME_PATTERN).transactionAmount(19.99).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 19.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            loanTransactionHelper.makeMerchantIssuedRefund(loanResponse.getLoanId(), new PostLoansLoanIdTransactionsRequest()
                    .transactionDate("02 March 2024").dateFormat(DATETIME_PATTERN).transactionAmount(19.99).locale("en"));
            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 39.98);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(100, "Disbursement", "25 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100, "Repayment", "24 February 2024", 0.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(18.94, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.94), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99), //
                    transaction(31.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 31.99), //
                    transaction(124.91, "Credit Balance Refund", "01 March 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 124.91), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 19.99), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 19.99) //
            );

            loanTransactionHelper.reverseLoanTransaction(loanResponse.getLoanId(), mir1ExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN).transactionDate("02 March 2024")
                            .transactionAmount(0.0).locale("en"));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 224.91, 0.0, 224.91, 2.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 124.91, 124.91, 0.0, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(100, "Disbursement", "25 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100, "Repayment", "24 February 2024", 0.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99, true), //
                    transaction(18.94, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.94), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99), //
                    transaction(31.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 31.99), //
                    transaction(124.91, "Credit Balance Refund", "01 March 2024", 36.99, 36.99, 0.0, 0.0, 0.0, 0.0, 87.92), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 17.0, 19.99, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 0.0, 17.0, 0.0, 0.0, 0.0, 0.0, 2.99) //
            );

            loanTransactionHelper.chargebackLoanTransaction(loanResponse.getLoanId(), repaymentExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(2.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.0, 224.91, 0.0, 224.91, 0.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 124.91, 124.91, 0.0, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(100, "Disbursement", "25 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100, "Repayment", "24 February 2024", 0.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99, true), //
                    transaction(18.94, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.94), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99), //
                    transaction(31.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 31.99), //
                    transaction(124.91, "Credit Balance Refund", "01 March 2024", 36.99, 36.99, 0.0, 0.0, 0.0, 0.0, 87.92), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 17.0, 19.99, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 0.0, 17.0, 0.0, 0.0, 0.0, 0.0, 2.99), //
                    transaction(2.0, "Chargeback", "06 March 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0) //
            );

            loanTransactionHelper.chargebackLoanTransaction(loanResponse.getLoanId(), repaymentExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(1.0));

            loanDetails = loanTransactionHelper.getLoanDetails(loanResponse.getLoanId());
            validateLoanSummaryBalances(loanDetails, 0.01, 225.90, 0.01, 225.90, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 6), 125.91, 125.90, 0.01, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTransactions(loanResponse.getLoanId(), //
                    transaction(100, "Disbursement", "25 January 2024", 100.0, 0.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(100, "Repayment", "24 February 2024", 0.0, 100.0, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99, true), //
                    transaction(18.94, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 18.94), //
                    transaction(36.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 36.99), //
                    transaction(31.99, "Merchant Issued Refund", "28 February 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 31.99), //
                    transaction(124.91, "Credit Balance Refund", "01 March 2024", 36.99, 36.99, 0.0, 0.0, 0.0, 0.0, 87.92), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 17.0, 19.99, 0.0, 0.0, 0.0, 0.0, 0.0), //
                    transaction(19.99, "Merchant Issued Refund", "02 March 2024", 0.0, 17.0, 0.0, 0.0, 0.0, 0.0, 2.99), //
                    transaction(2.0, "Chargeback", "06 March 2024", 0.0, 0.0, 0.0, 0.0, 0.0, 0.0, 2.0), //
                    transaction(1.0, "Chargeback", "06 March 2024", 0.01, 0.01, 0.0, 0.0, 0.0, 0.0, 0.99) //
            );

        });
    }

    private static AdvancedPaymentData createRepaymentPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("REPAYMENT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_INTEREST, PaymentAllocationType.PAST_DUE_PRINCIPAL,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_INTEREST,
                PaymentAllocationType.DUE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static AdvancedPaymentData createDefaultPaymentAllocation() {
        AdvancedPaymentData advancedPaymentData = new AdvancedPaymentData();
        advancedPaymentData.setTransactionType("DEFAULT");
        advancedPaymentData.setFutureInstallmentAllocationRule("NEXT_INSTALLMENT");

        List<PaymentAllocationOrder> paymentAllocationOrders = getPaymentAllocationOrder(PaymentAllocationType.PAST_DUE_PENALTY,
                PaymentAllocationType.PAST_DUE_FEE, PaymentAllocationType.PAST_DUE_PRINCIPAL, PaymentAllocationType.PAST_DUE_INTEREST,
                PaymentAllocationType.DUE_PENALTY, PaymentAllocationType.DUE_FEE, PaymentAllocationType.DUE_PRINCIPAL,
                PaymentAllocationType.DUE_INTEREST, PaymentAllocationType.IN_ADVANCE_PENALTY, PaymentAllocationType.IN_ADVANCE_FEE,
                PaymentAllocationType.IN_ADVANCE_PRINCIPAL, PaymentAllocationType.IN_ADVANCE_INTEREST);

        advancedPaymentData.setPaymentAllocationOrder(paymentAllocationOrders);
        return advancedPaymentData;
    }

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(createDefaultPaymentAllocation(), createRepaymentPaymentAllocation()))));
    }

}
