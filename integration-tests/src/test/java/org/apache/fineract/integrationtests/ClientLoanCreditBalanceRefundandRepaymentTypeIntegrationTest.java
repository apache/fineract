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

import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.ACCRUAL_PERIODIC;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.CASH_BASED;
import static org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder.DEFAULT_STRATEGY;
import static org.apache.fineract.portfolio.loanaccount.domain.transactionprocessor.impl.AdvancedPaymentScheduleTransactionProcessor.ADVANCED_PAYMENT_ALLOCATION_STRATEGY;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.Objects;
import java.util.UUID;
import java.util.stream.Stream;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionEnumData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdSummary;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTransactionIdResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.accounting.Account;
import org.apache.fineract.integrationtests.common.accounting.JournalEntry;
import org.apache.fineract.integrationtests.common.loans.LoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Named;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

public class ClientLoanCreditBalanceRefundandRepaymentTypeIntegrationTest extends FeignLoanTestBase {

    private Long disbursedLoanID;

    private void runJanuary2022(Runnable action) {
        runAt("09 January 2022", action);
    }

    private void runMarch2022(Runnable action) {
        runAt("09 March 2022", action);
    }

    private static final String REPAYMENT = "repayment";
    private static final String MERCHANT_ISSUED_REFUND = "merchantIssuedRefund";
    private static final String PAYOUT_REFUND = "payoutRefund";
    private static final String GOODWILL_CREDIT = "goodwillCredit";

    private void disburseLoanOfAccountingRule(final String accountingType, LoanProductTestBuilder loanProductTestBuilder) {
        final String principal = "12000.00";
        final String submitApproveDisburseDate = "01 January 2022";
        this.disbursedLoanID = fromStartToDisburseLoan(loanProductTestBuilder, submitApproveDisburseDate, principal, accountingType);
    }

    private Long createLoanProduct(LoanProductTestBuilder loanProductTestBuilder, final String principal, final boolean multiDisburseLoan,
            final String accountingRule) {
        Account assetAccount = getAccounts().getLoansReceivableAccount();
        Account incomeAccount = getAccounts().getInterestIncomeAccount();
        Account expenseAccount = getAccounts().getGoodwillExpenseAccount();
        Account overpaymentAccount = getAccounts().getOverpaymentAccount();
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
                .withAccounting(accountingRule, new Account[] { assetAccount, incomeAccount, expenseAccount, overpaymentAccount }) //
                .withTranches(multiDisburseLoan);
        if (multiDisburseLoan) {
            loanProductTestBuilder = loanProductTestBuilder.withInterestCalculationPeriodTypeAsRepaymentPeriod(true);
            loanProductTestBuilder = loanProductTestBuilder.withMaxTrancheCount("30");
        }
        final String loanProductJSON = loanProductTestBuilder.build(null);
        return createLoanProductFromJson(loanProductJSON);
    }

    private Long applyForLoanApplication(final Long clientID, final Long loanProductID, String principal, String submitDate,
            String repaymentStrategy) {
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
        return applyForLoanFromJson(loanApplicationJSON);
    }

    private Long fromStartToDisburseLoan(LoanProductTestBuilder loanProductTestBuilder, String submitApproveDisburseDate, String principal,
            final String accountingRule) {

        final Long clientID = createClient();
        Assertions.assertNotNull(clientID);

        boolean allowMultipleDisbursals = false;
        final Long loanProductID = createLoanProduct(loanProductTestBuilder, principal, allowMultipleDisbursals, accountingRule);
        Assertions.assertNotNull(loanProductID);

        final Long loanID = applyForLoanApplication(clientID, loanProductID, principal, submitApproveDisburseDate,
                loanProductTestBuilder.getTransactionProcessingStrategyCode());
        Assertions.assertNotNull(loanID);
        assertTrue(getLoanDetails(loanID).getStatus().getPendingApproval());

        approveLoan(loanID, approveLoanRequest(Double.parseDouble(principal), submitApproveDisburseDate));
        GetLoansLoanIdStatus status = getLoanDetails(loanID).getStatus();
        assertTrue(!Boolean.TRUE.equals(status.getPendingApproval()));
        assertTrue(status.getWaitingForDisbursal());

        disburseLoanWithAmount(loanID, submitApproveDisburseDate, Double.parseDouble(principal));
        assertTrue(getLoanDetails(loanID).getStatus().getActive());
        return loanID;
    }

    private GetLoansLoanIdStatus makeRepayment(final String repaymentDate, final Float repayment) {
        makeLoanRepayment(disbursedLoanID, "Repayment", repaymentDate, repayment.doubleValue());
        return getLoanDetails(disbursedLoanID).getStatus();
    }

    private PostLoansLoanIdTransactionsRequest creditBalanceRefundRequest(String date, Float amount, String externalId) {
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest().transactionDate(date)
                .dateFormat(DATETIME_PATTERN).transactionAmount(amount.doubleValue()).locale("en");
        if (externalId != null) {
            request.externalId(externalId);
        }
        return request;
    }

    private PostLoansLoanIdTransactionsResponse makeRepaymentTypePayment(final String repaymentTypeCommand, final String date,
            final Double amount, final Long loanId) {
        final double roundedAmount = BigDecimal.valueOf(amount).setScale(2, RoundingMode.HALF_UP).doubleValue();
        PostLoansLoanIdTransactionsRequest request = new PostLoansLoanIdTransactionsRequest().transactionAmount(roundedAmount)
                .transactionDate(date).dateFormat("dd MMMM yyyy").locale("en");

        return switch (repaymentTypeCommand) {
            case GOODWILL_CREDIT -> makeGoodwillCredit(loanId, request.note("Repayment Made!!!"));
            case PAYOUT_REFUND -> makePayoutRefund(loanId, request.note("Repayment Made!!!"));
            case MERCHANT_ISSUED_REFUND -> makeMerchantIssuedRefund(loanId, request.note("Repayment Made!!!"));
            default -> makeLoanRepayment(loanId, repaymentTypeCommand, date, roundedAmount);
        };
    }

    private Float requireTotalOverpaid(Long loanId) {
        Double value = Utils.getDoubleValue(getLoanDetails(loanId).getTotalOverpaid());
        Assertions.assertNotNull(value, "totalOverpaid should not be null");
        return value.floatValue();
    }

    private Float totalOverpaidOrZero(Long loanId) {
        Double value = Utils.getDoubleValue(getLoanDetails(loanId).getTotalOverpaid());
        return value == null ? 0.0f : value.floatValue();
    }

    private boolean isTransactionType(GetLoansLoanIdLoanTransactionEnumData type, String repaymentTransactionType) {
        return switch (repaymentTransactionType) {
            case MERCHANT_ISSUED_REFUND -> Boolean.TRUE.equals(type.getMerchantIssuedRefund());
            case PAYOUT_REFUND -> Boolean.TRUE.equals(type.getPayoutRefund());
            case GOODWILL_CREDIT -> Boolean.TRUE.equals(type.getGoodwillCredit());
            case REPAYMENT -> Boolean.TRUE.equals(type.getRepayment());
            default -> false;
        };
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void creditBalanceRefundCanOnlyBeAppliedWhereLoanStatusIsOverpaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 2000.00f);
            assertTrue(loanStatus.getActive());

            final String creditBalanceRefundDate = "09 January 2022";
            final Float refund = 1000.00f;
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeCreditBalanceRefund(disbursedLoanID, creditBalanceRefundRequest(creditBalanceRefundDate, refund, null)));

            assertErrorGlobalisationCode(exception, "error.msg.loan.credit.balance.refund.account.is.not.overpaid");
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void cantRefundMoreThanOverpaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final String creditBalanceRefundDate = "09 January 2022";
            final Float excessiveRefund = 10000.00f;
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeCreditBalanceRefund(disbursedLoanID,
                            creditBalanceRefundRequest(creditBalanceRefundDate, excessiveRefund, null)));

            assertErrorGlobalisationCode(exception, "error.msg.transactionAmount.invalid.must.be.>zero.and<=overpaidamount");

            final Float negativeRefund = (float) -1.00;
            exception = assertThrows(CallFailedRuntimeException.class, () -> makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, negativeRefund, null)));
            assertErrorGlobalisationCode(exception, "validation.msg.loan.transaction.transactionAmount.not.greater.than.zero");
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void fullRefundChangesStatusToClosedObligationMetAndSetBackToOverpayAfterReverseTest(
            LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float totalOverpaid = requireTotalOverpaid(disbursedLoanID);

            final String creditBalanceRefundDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse response = makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, totalOverpaid, null));
            assertTrue(getLoanDetails(disbursedLoanID).getStatus().getClosed());

            final Float floatZero = 0.0f;
            Float totalOverpaidAtEnd = totalOverpaidOrZero(disbursedLoanID);
            assertEquals(totalOverpaidAtEnd, floatZero);

            reverseLoanTransaction(disbursedLoanID, response.getResourceId(), creditBalanceRefundDate);

            assertTrue(getLoanDetails(disbursedLoanID).getStatus().getOverpaid());

            Float totalOverpaidAfterReverse = requireTotalOverpaid(disbursedLoanID);

            assertEquals(totalOverpaidAfterReverse, totalOverpaid);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void refundAcceptedOnTheCurrentBusinessDate(LoanProductTestBuilder loanProductTestBuilder) {
        runAt("09 January 2022", () -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float totalOverpaid = requireTotalOverpaid(disbursedLoanID);

            final String creditBalanceRefundDate = "09 January 2022";
            makeCreditBalanceRefund(disbursedLoanID, creditBalanceRefundRequest(creditBalanceRefundDate, totalOverpaid, null));
            assertTrue(getLoanDetails(disbursedLoanID).getStatus().getClosed());

            assertEquals(totalOverpaidOrZero(disbursedLoanID), 0.0f);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void refundCannotBeDuneForFutureDate(LoanProductTestBuilder loanProductTestBuilder) {
        runAt("06 January 2022", () -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float totalOverpaid = requireTotalOverpaid(disbursedLoanID);

            final String creditBalanceRefundDate = "09 January 2022";

            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeCreditBalanceRefund(disbursedLoanID,
                            creditBalanceRefundRequest(creditBalanceRefundDate, totalOverpaid, null)));

            assertErrorGlobalisationCode(exception, "error.msg.transaction.date.cannot.be.in.the.future");
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void partialRefundKeepsOverpaidStatusTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float refund = 5000.00f;

            final String creditBalanceRefundDate = "09 January 2022";
            makeCreditBalanceRefund(disbursedLoanID, creditBalanceRefundRequest(creditBalanceRefundDate, refund, null));
            assertTrue(getLoanDetails(disbursedLoanID).getStatus().getOverpaid());
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundSavesExternalIdTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float refund = 1000.00f;
            final String creditBalanceRefundDate = "09 January 2022";
            final String externalId = "cbrextID" + disbursedLoanID.toString();
            PostLoansLoanIdTransactionsResponse response = makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, refund, externalId));
            Assertions.assertNotNull(response.getResourceId());

            GetLoansLoanIdTransactionsTransactionIdResponse creditBalanceRefundDetails = getLoanTransactionDetails(disbursedLoanID,
                    response.getResourceId());
            Assertions.assertNotNull(creditBalanceRefundDetails.getExternalId());
            Assertions.assertEquals(creditBalanceRefundDetails.getExternalId(), externalId, "Incorrect External Id Saved");
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundFindsDuplicateExternalIdTest(LoanProductTestBuilder loanProductTestBuilder) {
        runAt("10 January 2022", () -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float refund = 1000.00f;
            final String creditBalanceRefundDate = "09 January 2022";
            final String externalId = "cbrextID" + disbursedLoanID.toString();
            PostLoansLoanIdTransactionsResponse response = makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, refund, externalId));
            Assertions.assertNotNull(response.getResourceId());

            final Float refund2 = 10.00f;
            final String creditBalanceRefundDate2 = "10 January 2022";
            CallFailedRuntimeException exception = assertThrows(CallFailedRuntimeException.class,
                    () -> makeCreditBalanceRefund(disbursedLoanID,
                            creditBalanceRefundRequest(creditBalanceRefundDate2, refund2, externalId)));
            assertErrorGlobalisationCode(exception, "error.msg.loan.creditBalanceRefund.duplicate.externalId");
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundCreatesCorrectJournalEntriesForPeriodicAccrualsTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("06 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float refund = 1000.00f;
            final String creditBalanceRefundDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse response = makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, refund, null));
            Assertions.assertNotNull(response.getResourceId());

            checkJournalEntryForAssetAccount(getAccounts().getLoansReceivableAccount(), creditBalanceRefundDate,
                    journalEntry(refund, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForLiabilityAccount(getAccounts().getOverpaymentAccount(), creditBalanceRefundDate,
                    journalEntry(refund, getAccounts().getOverpaymentAccount(), JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newCreditBalanceRefundCreatesCorrectJournalEntriesForCashAccountingTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdStatus loanStatus = makeRepayment("08 January 2022", 20000.00f);
            assertTrue(loanStatus.getOverpaid());

            final Float refund = 1000.00f;
            final String creditBalanceRefundDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse response = makeCreditBalanceRefund(disbursedLoanID,
                    creditBalanceRefundRequest(creditBalanceRefundDate, refund, null));
            Assertions.assertNotNull(response.getResourceId());

            checkJournalEntryForAssetAccount(getAccounts().getLoansReceivableAccount(), creditBalanceRefundDate,
                    journalEntry(refund, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForLiabilityAccount(getAccounts().getOverpaymentAccount(), creditBalanceRefundDate,
                    journalEntry(refund, getAccounts().getOverpaymentAccount(), JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void repaymentTransactionTypeMatchesTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            verifyRepaymentTransactionTypeMatches(MERCHANT_ISSUED_REFUND);
            verifyRepaymentTransactionTypeMatches(PAYOUT_REFUND);
            verifyRepaymentTransactionTypeMatches(GOODWILL_CREDIT);
        });
    }

    private void verifyRepaymentTransactionTypeMatches(final String repaymentTransactionType) {
        PostLoansLoanIdTransactionsResponse response = makeRepaymentTypePayment(repaymentTransactionType, "06 January 2022", 200.00,
                disbursedLoanID);
        GetLoansLoanIdTransactions tx = getLoanDetails(disbursedLoanID).getTransactions().stream()
                .filter(t -> Objects.equals(t.getId(), response.getResourceId())).findFirst().orElseThrow();
        Assertions.assertTrue(isTransactionType(tx.getType(), repaymentTransactionType), "Not " + repaymentTransactionType);
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void repaymentTransactionTypeWhenPaidTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            verifyRepaymentTransactionTypeWhenPaid(MERCHANT_ISSUED_REFUND);
            verifyRepaymentTransactionTypeWhenPaid(PAYOUT_REFUND);
            verifyRepaymentTransactionTypeWhenPaid(GOODWILL_CREDIT);
            verifyRepaymentTransactionTypeWhenPaid(REPAYMENT);
        });
    }

    private void verifyRepaymentTransactionTypeWhenPaid(final String repaymentTransactionType) {
        PostLoansLoanIdTransactionsResponse resourceId = makeLoanRepayment(disbursedLoanID, "Repayment", "06 January 2022", 13000.00);
        Assertions.assertNotNull(resourceId);
        Assertions.assertNotNull(resourceId.getResourceId());
        resourceId = makeRepaymentTypePayment(repaymentTransactionType, "06 January 2022", 1.00, disbursedLoanID);
        Assertions.assertNotNull(resourceId);
        Assertions.assertNotNull(resourceId.getResourceId());
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void goodWillCreditWillCloseTheLoanCorrectly(LoanProductTestBuilder loanProductTestBuilder) {
        runMarch2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float goodwillAmount = totalOutstanding;
            final String goodwillDate = "09 March 2022";
            makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate, goodwillAmount.doubleValue(), disbursedLoanID);

            GetLoansLoanIdResponse details = getLoanDetails(disbursedLoanID);

            Assertions.assertNull(details.getSummary().getInArrears());
            Assertions.assertTrue(details.getStatus().getClosedObligationsMet());
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void paymentRefundWillCloseTheLoanCorrectly(LoanProductTestBuilder loanProductTestBuilder) {
        runMarch2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float refundAmount = totalOutstanding;
            final String refundDate = "09 March 2022";
            makeRepaymentTypePayment(PAYOUT_REFUND, refundDate, refundAmount.doubleValue(), disbursedLoanID);

            GetLoansLoanIdResponse details = getLoanDetails(disbursedLoanID);

            Assertions.assertNull(details.getSummary().getInArrears());
            Assertions.assertTrue(details.getStatus().getClosedObligationsMet());
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newGoodwillCreditCreatesCorrectJournalEntriesForPeriodicAccrualsTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(ACCRUAL_PERIODIC, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float principalOutstanding = Utils.getDoubleValue(loanSummary.getPrincipalOutstanding()).floatValue();
            final Float interestOutstanding = Utils.getDoubleValue(loanSummary.getInterestOutstanding()).floatValue();
            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float goodwillAmount = totalOutstanding + overpaidAmount;
            final Float goodwillAmountInExpense = principalOutstanding + overpaidAmount;
            final String goodwillDate = "09 January 2022";
            makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate, goodwillAmount.doubleValue(), disbursedLoanID);

            checkJournalEntryForAssetAccount(getAccounts().getLoansReceivableAccount(), goodwillDate,
                    journalEntry(totalOutstanding, getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForLiabilityAccount(getAccounts().getOverpaymentAccount(), goodwillDate,
                    journalEntry(overpaidAmount, getAccounts().getOverpaymentAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(getAccounts().getGoodwillExpenseAccount(), goodwillDate, journalEntry(
                    goodwillAmountInExpense, getAccounts().getGoodwillExpenseAccount(), JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void newGoodwillCreditCreatesCorrectJournalEntriesForCashAccountingTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float principalOutstanding = Utils.getDoubleValue(loanSummary.getPrincipalOutstanding()).floatValue();
            final Float interestOutstanding = Utils.getDoubleValue(loanSummary.getInterestOutstanding()).floatValue();
            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float goodwillAmount = totalOutstanding + overpaidAmount;
            final Float goodwillAmountInExpense = principalOutstanding + overpaidAmount;
            final String goodwillDate = "09 January 2022";
            makeRepaymentTypePayment(GOODWILL_CREDIT, goodwillDate, goodwillAmount.doubleValue(), disbursedLoanID);

            checkJournalEntryForAssetAccount(getAccounts().getLoansReceivableAccount(), goodwillDate, journalEntry(principalOutstanding,
                    getAccounts().getLoansReceivableAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForIncomeAccount(getAccounts().getInterestIncomeAccount(), goodwillDate, journalEntry(interestOutstanding,
                    getAccounts().getInterestIncomeAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForLiabilityAccount(getAccounts().getOverpaymentAccount(), goodwillDate,
                    journalEntry(overpaidAmount, getAccounts().getOverpaymentAccount(), JournalEntry.TransactionType.CREDIT.name()));
            checkJournalEntryForExpenseAccount(getAccounts().getGoodwillExpenseAccount(), goodwillDate, journalEntry(
                    goodwillAmountInExpense, getAccounts().getGoodwillExpenseAccount(), JournalEntry.TransactionType.DEBIT.name()));
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoGoodWillCreditTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(GOODWILL_CREDIT, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            reverseLoanTransaction(disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoPayoutRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(PAYOUT_REFUND, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            reverseLoanTransaction(disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void undoMerchantIssuedRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(MERCHANT_ISSUED_REFUND, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            reverseLoanTransaction(disbursedLoanID, loanTransactionResponse.getResourceId(), transactionDate);
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustGoodWillCreditTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(GOODWILL_CREDIT, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            CallFailedRuntimeException exception = adjustLoanTransactionExpectingError(disbursedLoanID,
                    loanTransactionResponse.getResourceId(), transactionDate, 10.0);
            assertEquals(403, exception.getStatus());
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustPayoutRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(PAYOUT_REFUND, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            CallFailedRuntimeException exception = adjustLoanTransactionExpectingError(disbursedLoanID,
                    loanTransactionResponse.getResourceId(), transactionDate, 10.0);
            assertEquals(403, exception.getStatus());
        });
    }

    @ParameterizedTest
    @MethodSource("loanProductFactory")
    public void adjustMerchantIssuedRefundTransactionTest(LoanProductTestBuilder loanProductTestBuilder) {
        runJanuary2022(() -> {
            disburseLoanOfAccountingRule(CASH_BASED, loanProductTestBuilder);
            GetLoansLoanIdSummary loanSummary = getLoanDetails(disbursedLoanID).getSummary();

            final Float totalOutstanding = Utils.getDoubleValue(loanSummary.getTotalOutstanding()).floatValue();
            final Float overpaidAmount = 159.00f;
            final Float transactionAmount = totalOutstanding + overpaidAmount;
            final String transactionDate = "09 January 2022";
            PostLoansLoanIdTransactionsResponse loanTransactionResponse = makeRepaymentTypePayment(MERCHANT_ISSUED_REFUND, transactionDate,
                    transactionAmount.doubleValue(), disbursedLoanID);
            Assertions.assertNotNull(loanTransactionResponse);
            Assertions.assertNotNull(loanTransactionResponse.getResourceId());

            CallFailedRuntimeException exception = adjustLoanTransactionExpectingError(disbursedLoanID,
                    loanTransactionResponse.getResourceId(), transactionDate, 10.0);
            assertEquals(403, exception.getStatus());
        });
    }

    @Test
    public void cbrReverseReplayTest() {
        runAt("06 March 2024", () -> {
            Long clientId = createClient();
            Long loanProductId = createLoanProduct(createOnePeriod30DaysLongNoInterestPeriodicAccrualProduct().numberOfRepayments(1)
                    .repaymentEvery(30).enableDownPayment(false));
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, "25 January 2024", 1000.0, 4);

            applicationRequest = applicationRequest.numberOfRepayments(1).loanTermFrequency(30)
                    .transactionProcessingStrategyCode(
                            LoanProductTestBuilder.DUE_PENALTY_FEE_INTEREST_PRINCIPAL_IN_ADVANCE_PRINCIPAL_PENALTY_FEE_INTEREST_STRATEGY)
                    .repaymentEvery(30);

            Long loanId = applyForLoan(applicationRequest);

            approveLoan(loanId, new PostLoansLoanIdRequest().approvedLoanAmount(BigDecimal.valueOf(1000)).dateFormat(DATETIME_PATTERN)
                    .approvedOnDate("25 January 2024").locale("en"));

            disburseLoan(loanId, new PostLoansLoanIdRequest().actualDisbursementDate("25 January 2024").dateFormat(DATETIME_PATTERN)
                    .transactionAmount(BigDecimal.valueOf(100.0)).locale("en"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 100.0, 0.0, 100.0, 0.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 0.0, 100.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getActive());

            String repaymentExternalId = UUID.randomUUID().toString();
            makeLoanRepayment(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("24 February 2024").locale("en").transactionAmount(100.0).externalId(repaymentExternalId));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            String mir1ExternalId = UUID.randomUUID().toString();
            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 February 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(36.99).locale("en").externalId(mir1ExternalId));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 36.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 February 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(18.94).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 55.93);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 February 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(36.99).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 92.92);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("28 February 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(31.99).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 124.91);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("01 March 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(124.91).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getClosedObligationsMet());

            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("02 March 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(19.99).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 19.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().transactionDate("02 March 2024")
                    .dateFormat(DATETIME_PATTERN).transactionAmount(19.99).locale("en"));
            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 100.0, 0.0, 100.0, 39.98);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanId, //
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

            reverseLoanTransaction(loanId, mir1ExternalId, new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("02 March 2024").transactionAmount(0.0).locale("en"));

            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 224.91, 0.0, 224.91, 2.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 124.91, 124.91, 0.0, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanId, //
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

            chargebackLoanTransaction(loanId, repaymentExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(2.0));

            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.0, 224.91, 0.0, 224.91, 0.99);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 1), 124.91, 124.91, 0.0, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getOverpaid());

            verifyTransactions(loanId, //
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

            chargebackLoanTransaction(loanId, repaymentExternalId,
                    new PostLoansLoanIdTransactionsTransactionIdRequest().locale("en").transactionAmount(1.0));

            loanDetails = getLoanDetails(loanId);
            validateLoanSummaryBalances(loanDetails, 0.01, 225.90, 0.01, 225.90, null);
            validateRepaymentPeriod(loanDetails, 1, LocalDate.of(2024, 2, 24), 100.0, 100.0, 0.0, 0.0, 0.0);
            validateRepaymentPeriod(loanDetails, 2, LocalDate.of(2024, 3, 6), 125.91, 125.90, 0.01, 0.0, 36.99);
            assertTrue(loanDetails.getStatus().getActive());

            verifyTransactions(loanId, //
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

    private static Stream<Arguments> loanProductFactory() {
        return Stream.of(Arguments.of(Named.of("DEFAULT_STRATEGY", new LoanProductTestBuilder().withRepaymentStrategy(DEFAULT_STRATEGY))),
                Arguments.of(Named.of("ADVANCED_PAYMENT_ALLOCATION_STRATEGY",
                        new LoanProductTestBuilder().withRepaymentStrategy(ADVANCED_PAYMENT_ALLOCATION_STRATEGY)
                                .withLoanScheduleType(LoanScheduleType.PROGRESSIVE)
                                .addAdvancedPaymentAllocation(LoanRequestBuilders.defaultPaymentAllocation(),
                                        LoanRequestBuilders.repaymentPaymentAllocation()))));
    }

}
