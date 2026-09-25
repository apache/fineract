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
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.List;
import java.util.concurrent.atomic.AtomicReference;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.JournalEntryTransactionItem;
import org.apache.fineract.client.models.PostLoansLoanIdRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanProductTestBuilder;
import org.apache.fineract.portfolio.loanaccount.loanschedule.domain.LoanScheduleType;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;

public class LoanContractTerminationTest extends FeignLoanTestBase {

    @Test
    public void testLoanContractTermination() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final Long clientId = createClient();

        final Long loanProductId = createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 500.0, 7.0, 6, null);
            loanIdRef.set(loanId);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
        });

        runAt("2 February 2024", () -> {
            Long loanId = loanIdRef.get();
            executeInlineCOB(loanId);
        });

        runAt("3 February 2024", () -> {
            Long loanId = loanIdRef.get();

            moveLoanState(loanId,
                    new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                    "contractTermination");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(0.58, "Accrual", "01 February 2024"), //
                    transaction(100.62, "Contract Termination", "03 February 2024"), //
                    transaction(0.04, "Accrual", "03 February 2024") //
            );
        });
    }

    @Test
    public void testNegativeLoanContractTerminationInNoActiveLoan() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final Long clientId = createClient();

        final Long loanProductId = createLoanProduct(create4IProgressive());

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 500.0, 7.0, 3, null);
            loanIdRef.set(loanId);

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> moveLoanState(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                            "contractTermination"));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Contract termination can not be applied, Loan Account is not Active"));
        });
    }

    @Test
    public void testNegativeLoanContractTerminationInNoProgressiveLoan() {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        final Long clientId = createClient();

        final Long loanProductId = createLoanProduct(
                createOnePeriod30DaysPeriodicAccrualProduct(12.4).transactionProcessingStrategyCode(LoanProductTestBuilder.DEFAULT_STRATEGY)
                        .loanScheduleType(LoanScheduleType.CUMULATIVE.toString()));

        runAt("1 January 2024", () -> {
            final Long loanId = applyAndApproveLoan(clientId, loanProductId, "1 January 2024", 100.0, 6);

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            CallFailedRuntimeException callFailedRuntimeException = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> moveLoanState(loanId,
                            new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                            "contractTermination"));

            Assertions.assertTrue(callFailedRuntimeException.getMessage()
                    .contains("Contract termination can not be applied, Loan product schedule type is not Progressive"));
        });
    }

    @Test
    public void testLoanContractTerminationSameDisbursementDate() {
        final Long clientId = createClient();

        runAt("1 January 2024", () -> {

            Long loanProductId = createLoanProduct(create4IProgressive().interestRecognitionOnDisbursementDate(false));
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 500.0, 7.0, 6,
                    (request) -> request.interestRecognitionOnDisbursementDate(false));

            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");

            moveLoanState(loanId,
                    new PostLoansLoanIdRequest().note("Contract Termination Test").externalId(Utils.randomStringGenerator("", 20)),
                    "contractTermination");

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(100.0, "Contract Termination", "01 January 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertEquals(BigDecimal.ZERO.stripTrailingZeros(), loanDetails.getSummary().getInterestCharged().stripTrailingZeros());
        });
    }

    @Test
    public void futureDatedContractTerminationChargesInterestUntilTheTransactionDate() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            applyContractTermination(loanId, "31 March 2024");

            verifyTransactionPortions(loanId, "Contract Termination", "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);

            verifyTransactionPortions(loanId, "Accrual", "31 March 2024", 1.54, 0.0, 1.54, 0.0, 0.0);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(1.54, "Accrual", "31 March 2024"), //
                    transaction(84.53, "Contract Termination", "31 March 2024"));

            verifyRepaymentSchedule(loanId, //
                    installment(100.0, null, "01 January 2024"), //
                    installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                    installment(16.52, 0.49, 0.0, 0.0, 17.01, false, "01 March 2024"), //
                    installment(67.05, 0.47, 0.0, 0.0, 67.52, false, "31 March 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals("Contract Termination", loanDetails.getSubStatus().getValue());
            assertEquals(1.54, Utils.getDoubleValue(loanDetails.getSummary().getInterestCharged()));
            validateLoanSummaryBalances(loanDetails, 84.53, 17.01, 83.57, 16.43, null);

            assertEquals(List.of(LocalDate.of(2024, 3, 31)), journalEntryDatesOf(loanId, "Accrual", "31 March 2024"),
                    "Journal entry dates of the future dated accrual");
        });
    }

    @Test
    public void futureDatedContractTerminationAccruesNoInterestBeyondTheTerminationDate() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> applyContractTermination(loanId, "31 March 2024"));

        runAt("31 March 2024", () -> executeInlineCOB(loanId));
        runAt("15 April 2024", () -> {
            executeInlineCOB(loanId);

            verifyTransactionPortions(loanId, "Contract Termination", "31 March 2024", 84.53, 83.57, 0.96, 0.0, 0.0);
            verifyTransactionPortions(loanId, "Accrual", "31 March 2024", 1.54, 0.0, 1.54, 0.0, 0.0);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(1.54, "Accrual", "31 March 2024"), //
                    transaction(84.53, "Contract Termination", "31 March 2024"));

            verifyRepaymentSchedule(loanId, //
                    installment(100.0, null, "01 January 2024"), //
                    installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                    installment(16.52, 0.49, 0.0, 0.0, 17.01, false, "01 March 2024"), //
                    installment(67.05, 0.47, 0.0, 0.0, 67.52, false, "31 March 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals("Contract Termination", loanDetails.getSubStatus().getValue());
            assertEquals(1.54, Utils.getDoubleValue(loanDetails.getSummary().getInterestCharged()));
            validateLoanSummaryBalances(loanDetails, 84.53, 17.01, 83.57, 16.43, null);
        });
    }

    @Test
    public void unsupportedContractTerminationParameterIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> moveLoanState(loanId, new PostLoansLoanIdRequest().paymentTypeId(1), "contractTermination"));
            assertErrorGlobalisationCode(exception, "error.msg.parameter.unsupported");

            assertNoContractTermination(loanId);
        });
    }

    @Test
    public void contractTerminationWithTransactionDateOnTheBusinessDateIsUnchanged() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            applyContractTermination(loanId, "01 March 2024");

            verifyTransactionPortions(loanId, "Contract Termination", "01 March 2024", 84.06, 83.57, 0.49, 0.0, 0.0);
            verifyTransactionPortions(loanId, "Accrual", "01 March 2024", 1.07, 0.0, 1.07, 0.0, 0.0);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    transaction(1.07, "Accrual", "01 March 2024"), //
                    transaction(84.06, "Contract Termination", "01 March 2024"));

            verifyRepaymentSchedule(loanId, //
                    installment(100.0, null, "01 January 2024"), //
                    installment(16.43, 0.58, 0.0, 0.0, 0.0, true, "01 February 2024"), //
                    installment(83.57, 0.49, 0.0, 0.0, 84.06, false, "01 March 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertEquals("Contract Termination", loanDetails.getSubStatus().getValue());
            validateLoanSummaryBalances(loanDetails, 84.06, 17.01, 83.57, 16.43, null);
        });
    }

    @Test
    public void backdatedContractTerminationIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyContractTermination(loanId, "28 February 2024"));
            assertErrorGlobalisationCode(exception,
                    "validation.msg.loan.contract.termination.transactionDate.cannot.be.before.business.date");

            assertNoContractTermination(loanId);
        });
    }

    @Test
    public void futureDatedContractTerminationAfterTheMaturityDateIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            assertEquals(LocalDate.of(2024, 7, 1), getLoanDetails(loanId).getTimeline().getActualMaturityDate());

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyContractTermination(loanId, "15 July 2024"));
            assertErrorGlobalisationCode(exception,
                    "validation.msg.loan.contract.termination.transactionDate.must.be.before.maturity.date");

            assertNoContractTermination(loanId);
        });
    }

    @Test
    public void futureDatedContractTerminationOnTheMaturityDateIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            assertEquals(LocalDate.of(2024, 7, 1), getLoanDetails(loanId).getTimeline().getActualMaturityDate());

            CallFailedRuntimeException exception = Assertions.assertThrows(CallFailedRuntimeException.class,
                    () -> applyContractTermination(loanId, "01 July 2024"));
            assertErrorGlobalisationCode(exception,
                    "validation.msg.loan.contract.termination.transactionDate.must.be.before.maturity.date");

            assertNoContractTermination(loanId);
        });
    }

    @Test
    public void contractTerminationOnTheBusinessDateAfterMaturityStillSucceeds() {
        final Long clientId = createClient();
        updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                new PutGlobalConfigurationsRequest().enabled(true));
        try {
            final Long loanProductId = createLoanProduct(create4IProgressive());
            final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

            runAt("1 March 2024", () -> makeLoanRepayment(loanId, "repayment", "01 March 2024", 17.01));
            runAt("1 April 2024", () -> makeLoanRepayment(loanId, "repayment", "01 April 2024", 17.01));
            runAt("1 May 2024", () -> makeLoanRepayment(loanId, "repayment", "01 May 2024", 17.01));

            runAt("15 July 2024", () -> {
                applyContractTermination(loanId, "15 July 2024");

                verifyTransactionPortions(loanId, "Contract Termination", "15 July 2024", 34.11, 33.71, 0.4, 0.0, 0.0);
                verifyTransactionPortions(loanId, "Accrual", "15 July 2024", 2.15, 0.0, 2.15, 0.0, 0.0);

                GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
                verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
                assertEquals("Contract Termination", loanDetails.getSubStatus().getValue());
                validateLoanSummaryBalances(loanDetails, 34.11, 68.04, 33.71, 66.29, null);
            });
        } finally {
            updateGlobalConfiguration(GlobalConfigurationConstants.IS_PRINCIPAL_COMPOUNDING_DISABLED_FOR_OVERDUE_LOANS,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    @Test
    public void undoOfAFutureDatedContractTerminationRestoresTheOriginalSchedule() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            applyContractTermination(loanId, "31 March 2024");
            undoContractTermination(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    reversedTransaction(1.54, "Accrual", "31 March 2024"), //
                    reversedTransaction(84.53, "Contract Termination", "31 March 2024"));

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
    public void undoOfAFutureDatedContractTerminationAfterItsTerminationDateStillReversesTheAccrual() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> applyContractTermination(loanId, "31 March 2024"));

        runAt("31 March 2024", () -> executeInlineCOB(loanId));
        runAt("15 April 2024", () -> executeInlineCOB(loanId));

        runAt("20 April 2024", () -> {
            undoContractTermination(loanId);

            verifyTransactions(loanId, //
                    transaction(100.0, "Disbursement", "01 January 2024"), //
                    transaction(17.01, "Repayment", "01 February 2024"), //
                    reversedTransaction(1.54, "Accrual", "31 March 2024"), //
                    reversedTransaction(84.53, "Contract Termination", "31 March 2024"));

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            verifyLoanStatus(loanDetails, GetLoansLoanIdStatus::getActive);
            assertNull(loanDetails.getSubStatus());

            // Principal and interest split is left unasserted: two instalments are overdue, so it is the overdue
            // recalculation, pinned by the undo test above instead.
            List<GetLoansLoanIdRepaymentPeriod> periods = loanDetails.getRepaymentSchedule().getPeriods();
            assertEquals(LocalDate.of(2024, 7, 1), periods.get(periods.size() - 1).getDueDate(),
                    "The original maturity date of the restored schedule");

            LocalDate periodEnd = LocalDate.of(2024, 4, 15);
            assertEquals(0.0, interimJournalBalance(loanId, getIncomeAccountId("interestIncome"), periodEnd, false),
                    "Interest income booked up to 15 April 2024");
            assertEquals(-0.58, interimJournalBalance(loanId, getAssetAccountId("interestReceivable"), periodEnd, true),
                    "Interest receivable booked up to 15 April 2024");
        });
    }

    @Test
    public void contractTerminationWithAnUnparseableTransactionDateIsRejected() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());

        for (String unparseableValue : List.of("null", "\"\"", "\"   \"", "[\"31 March 2024\"]", "{\"date\":\"31 March 2024\"}")) {
            final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

            runAt("1 March 2024", () -> {
                String body = "{\"dateFormat\":\"dd MMMM yyyy\",\"locale\":\"en\",\"note\":\"Contract Termination\",\"transactionDate\":"
                        + unparseableValue + "}";

                RuntimeException exception = Assertions.assertThrows(RuntimeException.class,
                        () -> FeignRawHttpHelper.post("/loans/" + loanId + "?command=contractTermination", body));

                assertTrue(exception.getMessage().startsWith("HTTP 400 "), "Expected HTTP 400 for " + body + " but got " + exception);
                assertTrue(exception.getMessage().contains("validation.msg.loan.contract.termination.transactionDate.invalid.date.format"),
                        "Expected the unparseable date error code for " + body + " but got " + exception);
                assertNoContractTermination(loanId);
            });
        }
    }

    @Test
    public void undoOfAFutureDatedContractTerminationLeavesAConsistentInterimLedger() {
        final Long clientId = createClient();
        final Long loanProductId = createLoanProduct(create4IProgressive());
        final Long loanId = disburseSpecSheetLoanAndRepayFirstInstallment(clientId, loanProductId);

        runAt("1 March 2024", () -> {
            applyContractTermination(loanId, "31 March 2024");
            undoContractTermination(loanId);
        });

        for (String businessDate : List.of("2 March 2024", "15 March 2024", "1 April 2024")) {
            runAt(businessDate, () -> executeInlineCOB(loanId));
        }

        runAt("15 April 2024", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            assertTrue(
                    loanDetails.getTransactions().stream().anyMatch(tr -> "Accrual".equals(tr.getType().getValue())
                            && LocalDate.of(2024, 3, 1).equals(tr.getDate()) && Utils.getDoubleValue(tr.getAmount()) == 1.07),
                    "The 1.07 accrual of 01 March 2024 is missing");

            assertTrue(loanDetails.getTransactions().stream().noneMatch(tr -> "Accrual Adjustment".equals(tr.getType().getValue())),
                    "No accrual adjustment may be posted after an undone future dated termination");

            LocalDate periodEnd = LocalDate.of(2024, 3, 15);
            assertEquals(1.29, interimJournalBalance(loanId, getIncomeAccountId("interestIncome"), periodEnd, false),
                    "Interest income booked up to 15 March 2024");
            assertEquals(0.71, interimJournalBalance(loanId, getAssetAccountId("interestReceivable"), periodEnd, true),
                    "Interest receivable booked up to 15 March 2024");
        });
    }

    private double interimJournalBalance(Long loanId, Long glAccountId, LocalDate cutoff, boolean debitNormal) {
        double debitBalance = journalHelper.getJournalEntriesForLoan(loanId).getPageItems().stream()
                .filter(entry -> glAccountId.equals(entry.getGlAccountId())).filter(entry -> !entry.getTransactionDate().isAfter(cutoff))
                .mapToDouble(entry -> "DEBIT".equals(entry.getEntryType().getValue()) ? entry.getAmount() : -entry.getAmount()).sum();
        return BigDecimal.valueOf(debitNormal ? debitBalance : -debitBalance).setScale(2, RoundingMode.HALF_UP).doubleValue();
    }

    // Expected values come from the "Contract Termination Examples" sheet 1vFvry9rVEIvHuHvXeQ3zhEIevtLsGWAGUmRxI4cIjpE
    private Long disburseSpecSheetLoanAndRepayFirstInstallment(Long clientId, Long loanProductId) {
        final AtomicReference<Long> loanIdRef = new AtomicReference<>();

        runAt("1 January 2024", () -> {
            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "1 January 2024", 500.0, 7.0, 6, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(100), "1 January 2024");
        });

        runAt("1 February 2024", () -> makeLoanRepayment(loanIdRef.get(), "repayment", "01 February 2024", 17.01));

        return loanIdRef.get();
    }

    private List<LocalDate> journalEntryDatesOf(Long loanId, String type, String date) {
        List<JournalEntryTransactionItem> entries = getJournalEntries("L" + getTransactionId(loanId, type, date)).getPageItems();
        assertTrue(entries != null && !entries.isEmpty(), "No journal entry was created for the " + type + " of " + date);
        return entries.stream().map(JournalEntryTransactionItem::getTransactionDate).distinct().sorted().toList();
    }

    private void assertNoContractTermination(Long loanId) {
        GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
        assertNull(loanDetails.getSubStatus(), "Contract termination sub status must not be set by a rejected termination");
        assertTrue(loanDetails.getTransactions().stream().noneMatch(tr -> "Contract Termination".equals(tr.getType().getValue())),
                "A rejected contract termination must not create a Contract Termination transaction");
    }

}
