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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.Duration;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.BusinessStep;
import org.apache.fineract.client.models.GetLoansLoanIdLoanTransactionEnumData;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PostLoansRequestChargeData;
import org.apache.fineract.client.models.TransactionType;
import org.apache.fineract.infrastructure.event.external.data.ExternalEventResponse;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessStepHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.ChargeRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.externalevents.BusinessEvent;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.awaitility.Awaitility;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

/**
 * Loan withdrawal behaves like a contract termination under its own transaction type, sub-status, events and
 * permissions.
 */
public class LoanWithdrawalTest extends FeignLoanTestBase {

    private static final String LOAN_WITHDRAWAL = "Loan Withdrawal";
    private static final String WITHDRAWAL_POST_EVENT = "LoanTransactionLoanWithdrawalPostBusinessEvent";
    private static final String WITHDRAWAL_UNDO_EVENT = "LoanUndoLoanWithdrawalBusinessEvent";
    private static final String LOAN_COB_JOB = "LOAN_CLOSE_OF_BUSINESS";
    private static final String CAPITALIZED_INCOME_AMORTIZATION_STEP = "CAPITALIZED_INCOME_AMORTIZATION";

    @Test
    public void futureDatedLoanWithdrawalChargesInterestUntilTheWithdrawalDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");

            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(84.53, LOAN_WITHDRAWAL, "31 March 2024"));

            verifyFutureDatedWithdrawalSchedule(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertEquals(1.54, Utils.getDoubleValue(loanDetails.getSummary().getInterestCharged()),
                    "Interest charged up to the withdrawal date");
            validateLoanSummaryBalances(loanDetails, 84.53, 17.01, 83.57, 16.43, null);

            assertNoAccrualAfter(loanId, LocalDate.of(2024, 3, 1));
        });
    }

    @Test
    public void loanWithdrawalOnTheBusinessDateAcceleratesTheScheduleToThatDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "01 March 2024");
            verifyBusinessDateWithdrawal(loanId);
        });
    }

    @Test
    public void loanWithdrawalWithoutTransactionDateDefaultsToTheBusinessDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId);
            verifyBusinessDateWithdrawal(loanId);
        });
    }

    @Test
    public void backdatedLoanWithdrawalBeforeTheLastTransactionIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "31 January 2024"));
            assertErrorGlobalisationCode(exception,
                    "validation.msg.loan.withdrawal.transactionDate.cannot.be.before.last.transaction.date");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void backdatedLoanWithdrawalBeforeTheSubmissionOfAChargeIsRejected() {
        final Long loanId = disburseSpecSheetLoan();
        runAt("20 February 2024", () -> addLoanCharge(loanId, createCharge(5.0, "EUR").getResourceId(), "20 February 2024", 5.0));

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "15 February 2024"));
            assertErrorGlobalisationCode(exception,
                    "validation.msg.loan.withdrawal.transactionDate.cannot.be.before.charge.submitted.on.date");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void backdatedLoanWithdrawalChargesInterestUntilTheWithdrawalDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            final GetLoansLoanIdTransactionsTemplateResponse preview = retrieveTransactionTemplate(loanId, "loanWithdrawal",
                    DATETIME_PATTERN, "15 February 2024", "en");
            applyLoanWithdrawal(loanId, "15 February 2024");

            assertEquals(83.81, preview.getAmount(), "Loan withdrawal template amount on 15 February 2024");
            assertEquals(83.57, preview.getPrincipalPortion(), "Loan withdrawal template principal on 15 February 2024");
            assertEquals(0.24, preview.getInterestPortion(), "Loan withdrawal template interest on 15 February 2024");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(0.82, "Accrual", "15 February 2024"), //
                    transaction(83.81, LOAN_WITHDRAWAL, "15 February 2024"));
            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "15 February 2024", 83.81, 83.57, 0.24, 0.0, 0.0);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertEquals(0.82, Utils.getDoubleValue(loanDetails.getSummary().getInterestCharged()),
                    "Interest charged up to the backdated withdrawal date");
            validateLoanSummaryBalances(loanDetails, 83.81, 17.01, 83.57, 16.43, null);
        });
    }

    @Test
    public void futureDatedLoanWithdrawalOnTheMaturityDateIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            assertEquals(LocalDate.of(2024, 7, 1), getLoanDetails(loanId).getTimeline().getActualMaturityDate(), "Loan maturity date");

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "01 July 2024"));
            assertErrorGlobalisationCode(exception, "validation.msg.loan.withdrawal.transactionDate.must.be.before.maturity.date");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void futureDatedLoanWithdrawalAfterTheMaturityDateIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "15 July 2024"));
            assertErrorGlobalisationCode(exception, "validation.msg.loan.withdrawal.transactionDate.must.be.before.maturity.date");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void unsupportedLoanWithdrawalParameterIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> moveLoanState(loanId, new PostLoansLoanIdRequest().paymentTypeId(1), "loanWithdrawal"));
            assertErrorGlobalisationCode(exception, "error.msg.parameter.unsupported");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @ParameterizedTest
    @ValueSource(strings = { "null", "\"\"", "\"   \"", "[\"31 March 2024\"]", "{\"date\":\"31 March 2024\"}" })
    public void loanWithdrawalWithAnUnparseableTransactionDateIsRejected(String unparseableValue) {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            String body = "{\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"note\":\"Loan Withdrawal\",\"transactionDate\":"
                    + unparseableValue + "}";

            RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                    () -> FeignRawHttpHelper.post("/loans/" + loanId + "?command=loanWithdrawal", body));

            assertTrue(exception.getMessage().startsWith("HTTP 400 "), "Expected HTTP 400 for " + body + " but got " + exception);
            assertTrue(exception.getMessage().contains("validation.msg.loan.withdrawal.transactionDate.invalid.date.format"),
                    "Expected the unparseable date error code for " + body + " but got " + exception);
            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void futureDatedLoanWithdrawalKeepsAccruingDailyUntilTheWithdrawalDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));

        runAt("16 March 2024", () -> {
            executeInlineCOB(loanId);

            assertEquals(1.29, accruedInterest(loanId), "Interest accrued up to 15 March 2024");
            assertNoAccrualAfter(loanId, LocalDate.of(2024, 3, 15));
        });
    }

    @Test
    public void futureDatedLoanWithdrawalAccruesNoInterestBeyondTheWithdrawalDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));

        runAt("31 March 2024", () -> executeInlineCOB(loanId));
        runAt("15 April 2024", () -> {
            executeInlineCOB(loanId);

            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);

            assertEquals(1.54, accruedInterest(loanId), "Interest accrued up to the withdrawal date");
            assertNoAccrualAfter(loanId, LocalDate.of(2024, 3, 31));

            verifyFutureDatedWithdrawalSchedule(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertEquals(1.54, Utils.getDoubleValue(loanDetails.getSummary().getInterestCharged()),
                    "Interest charged up to the withdrawal date");
            validateLoanSummaryBalances(loanDetails, 84.53, 17.01, 83.57, 16.43, null);
        });
    }

    @Test
    public void undoOfAFutureDatedLoanWithdrawalRestoresTheOriginalSchedule() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");
            undoLoanWithdrawal(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    reversedTransaction(84.53, LOAN_WITHDRAWAL, "31 March 2024"));

            verifyRepaymentSchedule(loanId, //
                    installment(100.0, null, "01 January 2024"), //
                    installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                    installment(16.52, 0.49, 0.0, 0.0, 17.01, false, "01 March 2024"), //
                    installment(16.62, 0.39, 0.0, 0.0, 17.01, false, "01 April 2024"), //
                    installment(16.72, 0.29, 0.0, 0.0, 17.01, false, "01 May 2024"), //
                    installment(16.81, 0.2, 0.0, 0.0, 17.01, false, "01 June 2024"), //
                    installment(16.9, 0.1, 0.0, 0.0, 17.0, false, "01 July 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertNull(loanDetails.getSubStatus());
            validateLoanSummaryBalances(loanDetails, 85.04, 17.01, 83.57, 16.43, null);
        });
    }

    @Test
    public void undoOfALoanWithdrawalFollowedByAUserTransactionIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId);
            addRepaymentForLoan(loanId, 17.01, "01 March 2024");

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> undoLoanWithdrawal(loanId));
            assertErrorGlobalisationCode(exception, "error.msg.loan.withdrawal.is.not.the.last.user.transaction");

            assertEquals(LOAN_WITHDRAWAL, getLoanDetails(loanId).getSubStatus().getValue(), "Loan sub-status");
        });
    }

    @Test
    public void payoffBeforeAFutureDatedLoanWithdrawalChargesInterestUntilThePayoffDate() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));

        runAt("15 March 2024", () -> {
            GetLoansLoanIdTransactionsTemplateResponse prepayAmount = getPrepayAmount(loanId, "15 March 2024");
            assertEquals(84.28, prepayAmount.getAmount(), "Payoff amount on 15 March 2024");
            assertEquals(83.57, prepayAmount.getPrincipalPortion(), "Payoff principal on 15 March 2024");
            assertEquals(0.71, prepayAmount.getInterestPortion(), "Payoff interest on 15 March 2024");

            GetLoansLoanIdTransactionsTemplateResponse withdrawalPreview = retrieveTransactionTemplate(loanId, "loanWithdrawal",
                    DATETIME_PATTERN, "15 March 2024", "en");
            assertEquals(84.28, withdrawalPreview.getAmount(), "Loan withdrawal template amount on 15 March 2024");
            assertEquals(83.57, withdrawalPreview.getPrincipalPortion(), "Loan withdrawal template principal on 15 March 2024");
            assertEquals(0.71, withdrawalPreview.getInterestPortion(), "Loan withdrawal template interest on 15 March 2024");

            addRepaymentForLoan(loanId, 84.28, "15 March 2024");

            verifyTransactionPortions(loanId, "Repayment", "15 March 2024", 84.28, 83.57, 0.71, 0.0, 0.0);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertNoAccrualAfter(loanId, LocalDate.of(2024, 3, 15));
        });
    }

    @Test
    public void undoOfAPayoffBeforeAFutureDatedLoanWithdrawalRestoresTheWithdrawal() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));

        runAt("15 March 2024", () -> {
            Long payoffId = addRepaymentForLoan(loanId, 84.28, "15 March 2024");
            undoRepayment(loanId, payoffId, "15 March 2024");

            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);

            verifyFutureDatedWithdrawalSchedule(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            validateLoanSummaryBalances(loanDetails, 84.53, 17.01, 83.57, 16.43, null);
            assertNoAccrualAfter(loanId, LocalDate.of(2024, 3, 15));
        });
    }

    @Test
    public void chargeOffOnTheFutureDatedWithdrawalDateAccruesTheInterestOnce() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));

        runAt("31 March 2024", () -> {
            chargeOffLoan(loanId, "31 March 2024");

            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);
            verifyTransactionPortions(loanId, "Charge-off", "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);
            assertEquals(1.54, accruedInterest(loanId), "Interest accrued up to the withdrawal date");
        });
    }

    @Test
    public void loanWithdrawalOnANotActiveLoanIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 500.0, 7.0, 3, null);

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId));
            assertErrorGlobalisationCode(exception, "error.msg.loan.account.is.not.active.state");
        });
    }

    @Test
    public void loanWithdrawalOnANonProgressiveLoanIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(
                createOnePeriod30DaysPeriodicAccrualProduct(12.4).transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)
                        .loanScheduleType(LoanScheduleType.CUMULATIVE.toString()));

        runAt("1 January 2024", () -> {
            final Long loanId = applyAndApproveLoan(clientId, loanProductId, "1 January 2024", 100.0, 6);
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId));
            assertErrorGlobalisationCode(exception, "error.msg.loan.withdrawal.is.only.supported.for.progressive.loan.schedule.type");
        });
    }

    @Test
    public void loanWithdrawalOnAChargedOffLoanIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            chargeOffLoan(loanId, "01 March 2024");

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId));
            assertErrorGlobalisationCode(exception, "error.msg.loan.account.is.charge-off");

            assertNoLoanWithdrawal(loanId);
        });
    }

    @Test
    public void secondLoanWithdrawalOnAWithdrawnLoanIsRejected() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "15 March 2024"));
            assertErrorGlobalisationCode(exception, "error.msg.loan.account.is.already.loan.withdrawal.substate");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(84.53, LOAN_WITHDRAWAL, "31 March 2024"));
        });
    }

    @Test
    public void contractTerminationIsRejectedOnAWithdrawnLoan() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");

            CallFailedRuntimeException terminate = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyContractTermination(loanId, "15 March 2024"));
            assertErrorGlobalisationCode(terminate, "error.msg.loan.account.is.already.loan.withdrawal.substate");

            CallFailedRuntimeException undoTermination = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> undoContractTermination(loanId));
            assertErrorGlobalisationCode(undoTermination, "error.msg.loan.is.not.contract.terminated");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertTrue(loanDetails.getTransactions().stream().noneMatch(tr -> "Contract Termination".equals(tr.getType().getValue())),
                    "A withdrawn loan must not get a Contract Termination transaction");
            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);
        });
    }

    @Test
    public void loanWithdrawalIsRejectedOnAContractTerminatedLoan() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyContractTermination(loanId, "31 March 2024");

            CallFailedRuntimeException withdraw = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyLoanWithdrawal(loanId, "15 March 2024"));
            assertErrorGlobalisationCode(withdraw, "error.msg.loan.account.is.already.contract.termination.substate");

            CallFailedRuntimeException undoWithdrawal = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> undoLoanWithdrawal(loanId));
            assertErrorGlobalisationCode(undoWithdrawal, "error.msg.loan.is.not.loan.withdrawal");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals("Contract Termination", loanDetails.getSubStatus().getValue(), "Loan sub-status");
            verifyTransactionPortions(loanId, "Contract Termination", "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);
        });
    }

    @Test
    public void reAgeAndReAmortizeAreRejectedOnAWithdrawnLoan() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");

            CallFailedRuntimeException reAge = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> reAgeLoan(loanId, "MONTHS", 1, "01 April 2024", 3, null));
            assertErrorGlobalisationCode(reAge, "error.msg.loan.reage.not.allowed.on.loan.withdrawal");

            CallFailedRuntimeException reAmortize = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> reAmortizeLoan(loanId, null));
            assertErrorGlobalisationCode(reAmortize, "error.msg.loan.reamortize.not.allowed.on.loan.withdrawal");
        });
    }

    @Test
    public void loanWithdrawalPostsNoJournalEntriesOfItsOwn() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> applyLoanWithdrawal(loanId, "31 March 2024"));
        runAt("31 March 2024", () -> executeInlineCOB(loanId));

        runAt("15 April 2024", () -> {
            executeInlineCOB(loanId);

            verifyTRJournalEntries(getTransactionId(loanId, LOAN_WITHDRAWAL, "31 March 2024"));

            assertEquals(1.54, interimJournalBalance(loanId, getIncomeAccountId("interestIncome"), LocalDate.of(2024, 4, 15), false),
                    "Interest income booked up to 15 April 2024");
        });
    }

    @Test
    public void loanWithdrawalTransactionIsFlaggedAndCanBeExcludedFromTheTransactionList() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            applyLoanWithdrawal(loanId, "31 March 2024");

            GetLoansLoanIdLoanTransactionEnumData withdrawalType = transactionType(loanId, LOAN_WITHDRAWAL);
            assertEquals(Boolean.TRUE, withdrawalType.getLoanWithdrawal(), "Loan Withdrawal transaction has the loanWithdrawal flag");
            assertEquals(Boolean.FALSE, withdrawalType.getContractTermination(),
                    "Loan Withdrawal transaction has no contractTermination flag");
            assertEquals(Boolean.FALSE, transactionType(loanId, "Repayment").getLoanWithdrawal(),
                    "Repayment transaction has no loanWithdrawal flag");

            assertEquals(3L, getLoanTransactions(loanId).getTotalElements(), "Disbursement, Repayment and Loan Withdrawal");
            GetLoansLoanIdTransactionsResponse filtered = getLoanTransactions(loanId, List.of(TransactionType.LOAN_WITHDRAWAL));
            assertEquals(2L, filtered.getTotalElements(), "Disbursement and Repayment remain after excluding loanWithdrawal");
            assertTrue(filtered.getContent().stream().noneMatch(tr -> "loanTransactionType.loanWithdrawal".equals(tr.getType().getCode())),
                    "excludedTypes=loanWithdrawal must drop the Loan Withdrawal transaction");
        });
    }

    @Test
    public void loanWithdrawalAndItsUndoRaiseTheirOwnBusinessEvents() {
        Map<String, Boolean> enabledByEventType = externalEventHelper.getEnabledByEventType();
        assertEquals(Boolean.FALSE, enabledByEventType.get(WITHDRAWAL_POST_EVENT),
                WITHDRAWAL_POST_EVENT + " is registered, disabled by default");
        assertEquals(Boolean.FALSE, enabledByEventType.get(WITHDRAWAL_UNDO_EVENT),
                WITHDRAWAL_UNDO_EVENT + " is registered, disabled by default");

        final Long loanId = disburseSpecSheetLoan();

        try {
            externalEventHelper.enableBusinessEvent(WITHDRAWAL_POST_EVENT);
            externalEventHelper.enableBusinessEvent(WITHDRAWAL_UNDO_EVENT);

            runAt("1 March 2024", () -> {
                deleteAllExternalEvents();

                applyLoanWithdrawal(loanId, "31 March 2024");

                verifyBusinessEvents(new BusinessEvent(WITHDRAWAL_POST_EVENT, "01 March 2024"));
                Map<String, Object> payload = singleEvent(WITHDRAWAL_POST_EVENT).getPayLoad();
                assertEquals(84.53, ((Number) payload.get("amount")).doubleValue(), "Event payload amount");
                assertEquals(83.57, ((Number) payload.get("principalPortion")).doubleValue(), "Event payload principal portion");
                assertEquals(0.96, ((Number) payload.get("interestPortion")).doubleValue(), "Event payload interest portion");

                undoLoanWithdrawal(loanId);

                verifyBusinessEvents(new BusinessEvent(WITHDRAWAL_UNDO_EVENT, "01 March 2024"));
            });
        } finally {
            externalEventHelper.disableBusinessEvent(WITHDRAWAL_POST_EVENT);
            externalEventHelper.disableBusinessEvent(WITHDRAWAL_UNDO_EVENT);
        }
    }

    @ParameterizedTest
    @CsvSource({ "HORIZONTAL, loanWithdrawal", "VERTICAL, loanWithdrawal", "HORIZONTAL, contractTermination",
            "VERTICAL, contractTermination" })
    public void repaymentOnTheEarlyTerminationDatePaysTheAcceleratedInterestFirst(final String processingType, final String command) {
        // Past due interest before principal, but due principal before interest: the early termination makes what it
        // accelerates past due on its own date, so the repayment pays the interest first
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(createClient(),
                createLoanProduct(create4IProgressive().loanScheduleProcessingType(processingType)
                        .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "NEXT_INSTALLMENT", "PAST_DUE_PENALTY",
                                "PAST_DUE_FEE", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL", "DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL",
                                "DUE_INTEREST", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_INTEREST")))));
        final boolean withdrawal = "loanWithdrawal".equals(command);

        runAt("1 March 2024", () -> {
            if (withdrawal) {
                applyLoanWithdrawal(loanId, "01 March 2024");
            } else {
                applyContractTermination(loanId, "01 March 2024");
            }
            addRepaymentForLoan(loanId, 17.01, "01 March 2024");

            verifyTransactionPortions(loanId, "Repayment", "01 March 2024", 17.01, 16.52, 0.49, 0.0, 0.0);
            verifyTransactionPortions(loanId, withdrawal ? LOAN_WITHDRAWAL : "Contract Termination", "01 March 2024", 84.06, 83.57, 0.49,
                    0.0, 0.0);
            validateLoanSummaryBalances(getLoanDetails(loanId), 67.05, 34.02, 67.05, 32.95, null);
        });
    }

    @Test
    public void withdrawalPreviewWithoutInterestRecalculationMatchesThePostedWithdrawal() {
        final Long loanId = disburseSpecSheetLoanWithoutInterestRecalculation();

        runAt("1 March 2024", () -> {
            verifyEarlyTerminationPreview(loanId, "loanWithdrawal", "01 March 2024", 84.06, 0.49);
            verifyEarlyTerminationPreview(loanId, "loanWithdrawal", "31 March 2024", 84.44, 0.87);
            verifyEarlyTerminationPreview(loanId, "contractTermination", "31 March 2024", 84.44, 0.87);

            applyLoanWithdrawal(loanId, "31 March 2024");

            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 84.44, 83.57, 0.87, 0.0, 0.0);
        });
    }

    @Test
    public void repaymentMadeBeforeTheWithdrawalOnItsDateKeepsItsAllocationWhenReprocessed() {
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(createClient(),
                createLoanProduct(create4IProgressive()
                        .paymentAllocation(List.of(LoanRequestBuilders.paymentAllocation("DEFAULT", "NEXT_INSTALLMENT", "PAST_DUE_PENALTY",
                                "PAST_DUE_FEE", "PAST_DUE_INTEREST", "PAST_DUE_PRINCIPAL", "DUE_PENALTY", "DUE_FEE", "DUE_PRINCIPAL",
                                "DUE_INTEREST", "IN_ADVANCE_PENALTY", "IN_ADVANCE_FEE", "IN_ADVANCE_PRINCIPAL", "IN_ADVANCE_INTEREST")))));

        runAt("1 March 2024", () -> {
            addRepaymentForLoan(loanId, 5.0, "01 March 2024");
            applyLoanWithdrawal(loanId, "01 March 2024");
            verifyTransactionPortions(loanId, "Repayment", "01 March 2024", 5.0, 5.0, 0.0, 0.0, 0.0);

            addRepaymentForLoan(loanId, 1.0, "15 February 2024");

            verifyTransactionPortions(loanId, "Repayment", "01 March 2024", 5.0, 5.0, 0.0, 0.0, 0.0);
        });
    }

    @Test
    public void withdrawalAfterInterestPaidInAdvanceDoesNotChargeThePaidInterestTwice() {
        final Long loanId = disburseSpecSheetLoanWithoutInterestRecalculation();
        runAt("1 March 2024", () -> {
            addRepaymentForLoan(loanId, 17.01, "01 March 2024");
            addRepaymentForLoan(loanId, 17.00, "01 March 2024");
        });

        runAt("15 March 2024", () -> {
            final GetLoansLoanIdTransactionsTemplateResponse preview = retrieveTransactionTemplate(loanId, "loanWithdrawal",
                    DATETIME_PATTERN, "15 March 2024", "en");
            applyLoanWithdrawal(loanId, "15 March 2024");

            // 0.38 of the March interest is paid in advance, more than the 0.18 earned until 15 March
            assertEquals(50.43, preview.getAmount(), "Loan withdrawal template amount on 15 March 2024");
            assertEquals(0.0, preview.getInterestPortion(), "Loan withdrawal template interest on 15 March 2024");
            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "15 March 2024", 50.43, 50.43, 0.0, 0.0, 0.0);
            final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNull(loanDetails.getTotalOverpaid(), "Interest paid in advance is not refunded");
            assertEquals(0.0, Utils.getDoubleValue(loanDetails.getSummary().getInterestOutstanding()), "Interest outstanding");
        });
    }

    @Test
    public void withdrawalPreviewWithInstallmentFeesMatchesThePostedWithdrawal() {
        final Long chargeId = chargesHelper.createCharge(ChargeRequestBuilders.loanInstallmentFee(1.0).currencyCode("EUR")).getResourceId();
        final Long productId = createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false));
        final Long clientId = createClient();
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();
        runAt("1 January 2024", () -> {
            final Long loanId = applyAndApproveProgressiveLoan(clientId, productId, "1 January 2024", 500.0, 7.0, 6,
                    request -> request.charges(List.of(new PostLoansRequestChargeData().chargeId(chargeId).amount(BigDecimal.ONE))));
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
            loanIdRef.set(loanId);
        });
        final Long loanId = loanIdRef.get();

        runAt("1 March 2024", () -> {
            final GetLoansLoanIdTransactionsTemplateResponse preview = retrieveTransactionTemplate(loanId, "loanWithdrawal",
                    DATETIME_PATTERN, "31 March 2024", "en");
            applyLoanWithdrawal(loanId, "31 March 2024");

            // The installment fees of April, May and June are dropped with their installments
            assertEquals(104.45, preview.getAmount(), "Loan withdrawal template amount on 31 March 2024");
            assertEquals(3.0, preview.getFeeChargesPortion(), "Loan withdrawal template fees on 31 March 2024");
            verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "31 March 2024", 104.45, 100.0, 1.45, 3.0, 0.0);
        });
    }

    @Test
    public void backdatedLoanWithdrawalIsNotBlockedByCapitalizedIncomeAmortization() {
        final FeignBusinessStepHelper businessStepHelper = new FeignBusinessStepHelper(FineractFeignClientHelper.getFineractFeignClient());
        final List<BusinessStep> configuredSteps = businessStepHelper.getConfiguredBusinessStepsByJobName(LOAN_COB_JOB).getBusinessSteps();
        final List<BusinessStep> stepsWithAmortization = new ArrayList<>(configuredSteps);
        if (configuredSteps.stream().noneMatch(step -> CAPITALIZED_INCOME_AMORTIZATION_STEP.equals(step.getStepName()))) {
            stepsWithAmortization.add(new BusinessStep().stepName(CAPITALIZED_INCOME_AMORTIZATION_STEP)
                    .order(configuredSteps.stream().mapToLong(BusinessStep::getOrder).max().orElse(0) + 1));
        }
        businessStepHelper.updateBusinessSteps(LOAN_COB_JOB, stepsWithAmortization);
        try {
            verifyBackdatedWithdrawalAfterCapitalizedIncomeAmortization();
        } finally {
            businessStepHelper.updateBusinessSteps(LOAN_COB_JOB, configuredSteps);
        }
    }

    private void verifyBackdatedWithdrawalAfterCapitalizedIncomeAmortization() {
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(createClient(),
                createLoanProduct(create4IProgressiveWithCapitalizedIncome()));
        runAt("1 February 2024", () -> addCapitalizedIncome(loanId, "01 February 2024", 10.0));

        runAt("1 March 2024", () -> {
            executeInlineCOB(loanId);
            assertTrue(
                    getLoanDetails(loanId).getTransactions().stream()
                            .anyMatch(tr -> "Capitalized Income Amortization".equals(tr.getType().getValue())
                                    && LocalDate.of(2024, 2, 29).equals(tr.getDate())),
                    "COB posts a capitalized income amortization after the withdrawal date");

            applyLoanWithdrawal(loanId, "15 February 2024");

            final GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
            assertTrue(
                    loanDetails.getTransactions().stream().anyMatch(
                            tr -> LOAN_WITHDRAWAL.equals(tr.getType().getValue()) && LocalDate.of(2024, 2, 15).equals(tr.getDate())),
                    "Loan withdrawal dated 15 February 2024");
        });
    }

    @Test
    public void backdatedLoanWithdrawalHasTheInterestAccruedBeyondItsDateAdjustedByTheNextCob() {
        final Long loanId = disburseSpecSheetLoan();

        runAt("1 March 2024", () -> {
            executeInlineCOB(loanId);
            applyLoanWithdrawal(loanId, "15 February 2024");
        });
        runAt("2 March 2024", () -> {
            executeInlineCOB(loanId);
            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(0.82, "Accrual", "15 February 2024"), //
                    transaction(83.81, LOAN_WITHDRAWAL, "15 February 2024"), //
                    transaction(1.05, "Accrual", "29 February 2024"), //
                    transaction(1.05, "Accrual Adjustment", "29 February 2024"));
            assertEquals(0.82, Utils.getDoubleValue(getLoanDetails(loanId).getSummary().getInterestCharged()),
                    "Interest charged up to the backdated withdrawal date");
        });
    }

    private void verifyEarlyTerminationPreview(final Long loanId, final String command, final String date, final double amount,
            final double interest) {
        final GetLoansLoanIdTransactionsTemplateResponse preview = retrieveTransactionTemplate(loanId, command, DATETIME_PATTERN, date,
                "en");
        assertEquals(amount, preview.getAmount(), command + " template amount on " + date);
        assertEquals(83.57, preview.getPrincipalPortion(), command + " template principal on " + date);
        assertEquals(interest, preview.getInterestPortion(), command + " template interest on " + date);
    }

    private Long disburseSpecSheetLoanWithoutInterestRecalculation() {
        return disburseSpecSheetLoanAndRepayFirstInstallment(createClient(),
                createLoanProduct(create4IProgressive().isInterestRecalculationEnabled(false)));
    }

    private Long disburseSpecSheetLoan() {
        return disburseSpecSheetLoanAndRepayFirstInstallment(createClient(), createLoanProduct(create4IProgressive()));
    }

    private void verifyBusinessDateWithdrawal(Long loanId) {
        verifyTransactionPortions(loanId, LOAN_WITHDRAWAL, "01 March 2024", 84.06, 83.57, 0.49, 0.0, 0.0);
        verifyTransactionPortions(loanId, "Accrual", "01 March 2024", 1.07, 0.0, 1.07, 0.0, 0.0);

        verifyTransactions(loanId, //
                transaction(100.0, "Disbursement", "01 January 2024"), //
                transaction(17.01, "Repayment", "01 February 2024"), //
                transaction(1.07, "Accrual", "01 March 2024"), //
                transaction(84.06, LOAN_WITHDRAWAL, "01 March 2024"));

        verifyRepaymentSchedule(loanId, //
                installment(100.0, null, "01 January 2024"), //
                installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                installment(83.57, 0.49, 0.0, 0.0, 84.06, false, "01 March 2024"));

        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
        assertEquals(LOAN_WITHDRAWAL, loanDetails.getSubStatus().getValue(), "Loan sub-status");
        validateLoanSummaryBalances(loanDetails, 84.06, 17.01, 83.57, 16.43, null);
    }

    private void verifyFutureDatedWithdrawalSchedule(Long loanId) {
        verifyRepaymentSchedule(loanId, //
                installment(100.0, null, "01 January 2024"), //
                installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                installment(16.52, 0.49, 0.0, 0.0, 17.01, false, "01 March 2024"), //
                installment(67.05, 0.47, 0.0, 0.0, 67.52, false, "31 March 2024"));
    }

    private ExternalEventResponse singleEvent(String type) {
        AtomicReference<ExternalEventResponse> event = new AtomicReference<>();
        Awaitility.await().atMost(Duration.ofSeconds(30)).pollInterval(Duration.ofMillis(500)).untilAsserted(() -> {
            List<ExternalEventResponse> events = externalEventHelper.getExternalEventsByType(type);
            assertEquals(1, events.size(), "Expected exactly one " + type);
            event.set(events.get(0));
        });
        return event.get();
    }

    private GetLoansLoanIdLoanTransactionEnumData transactionType(Long loanId, String type) {
        return getLoanDetails(loanId).getTransactions().stream().filter(tr -> type.equals(tr.getType().getValue())).findFirst()
                .orElseThrow().getType();
    }

    private void assertNoLoanWithdrawal(Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNull(loanDetails.getSubStatus(), "Loan withdrawal sub status must not be set by a rejected withdrawal");
        assertTrue(loanDetails.getTransactions().stream().noneMatch(tr -> LOAN_WITHDRAWAL.equals(tr.getType().getValue())),
                "A rejected loan withdrawal must not create a Loan Withdrawal transaction");
    }
}
