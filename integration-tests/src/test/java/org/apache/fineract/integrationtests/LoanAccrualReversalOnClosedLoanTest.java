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
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.atomic.AtomicReference;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsRequest;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsResponse;
import org.apache.fineract.client.models.PostLoansLoanIdTransactionsTransactionIdRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Verifies that accrual transactions are never reversed (is_reversed=TRUE) on closed/overpaid loans.
 *
 * When income recognition needs correction, Fineract should create ACCRUAL_ADJUSTMENT (type 34) transactions instead of
 * reversing existing ACCRUAL transactions. Reversing accruals breaks downstream event reconciliation.
 */
@Slf4j
public class LoanAccrualReversalOnClosedLoanTest extends FeignLoanTestBase {

    private static final String LOCALE = "en";

    /**
     * Reproduces the production scenario from 1. Loan disbursed, repayments made, loan closed 2. Goodwill credit
     * (backdated) makes loan overpaid 3. Merchant issued refund with interest refund calculation further overpays 4.
     * CBR to handle overpayment 5. CBR reversed and re-created 6. During reprocessing, accruals should NOT be reversed
     */
    @Test
    public void testNoAccrualReversalAfterCBRAdjustmentOnOverpaidLoan() {
        AtomicLong loanIdRef = new AtomicLong();
        AtomicReference<Long> clientIdRef = new AtomicReference<>();

        // Step 1: Create loan product with interest refund support and accrual activity posting
        runAt("01 November 2024", () -> {
            final Long clientId = createClient();
            clientIdRef.set(clientId);

            final Long loanProductId = createLoanProduct(create4IProgressive()//
                    .currencyCode("USD")//
                    .principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1)//
                    .interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true)//
                    .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND")//
                    .paymentAllocation(List.of(//
                            createDefaultPaymentAllocation("NEXT_INSTALLMENT"), //
                            createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                            createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT")//
            ))//
            );

            Long loanId = applyAndApproveProgressiveLoan(clientIdRef.get(), loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
            log.info("Loan disbursed: id={}, amount=220.0", loanId);
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        // Step 2: Run COB through November and make monthly repayments to close the loan
        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));

        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));

        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));

        payInstallment(loanId, 3, "01 February 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 28));

        // Step 3: Final repayment closes the loan
        runAt("01 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            double amount = Utils.getDoubleValue(installment.getTotalDueForPeriod());
            addRepaymentForLoan(loanId, amount, "01 March 2025");

            GetLoansLoanIdResponse loanAfterClose = getLoanDetails(loanId);
            assertTrue(loanAfterClose.getStatus().getClosedObligationsMet(),
                    "Loan should be CLOSED but is: " + loanAfterClose.getStatus().getCode());
            log.info("Loan closed on 01 March 2025");
        });

        runCobRange(loanId, fmt, LocalDate.of(2025, 3, 2), LocalDate.of(2025, 3, 14));

        // Step 4: Goodwill credit (backdated) makes loan overpaid — mirrors production 10/29 goodwill credit
        runAt("15 March 2025", () -> {
            PostLoansLoanIdTransactionsResponse gwcResponse = makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 March 2025").locale(LOCALE).transactionAmount(0.5));
            assertNotNull(gwcResponse.getResourceId());

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid(),
                    "Loan should be OVERPAID after goodwill credit but is: " + loanDetails.getStatus().getCode());
            log.info("Goodwill credit of 0.50 applied → loan OVERPAID");
        });

        // Step 5: Merchant issued refund with automatic interest refund — mirrors production merchant refunds
        runAt("16 March 2025", () -> {
            PostLoansLoanIdTransactionsResponse mirResponse = makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025").locale(LOCALE).transactionAmount(100.0));
            assertNotNull(mirResponse.getResourceId());

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertTrue(loanDetails.getStatus().getOverpaid(), "Loan should still be OVERPAID but is: " + loanDetails.getStatus().getCode());
            log.info("Merchant issued refund of 100.0 → loan still OVERPAID");

            // Log all transactions after the refunds
            logAllTransactions(loanId, "After merchant refund");
        });

        // Step 6: Run COB, then create first CBR
        AtomicReference<Long> cbrTxnIdRef = new AtomicReference<>();
        runAt("17 March 2025", () -> {
            executeInlineCOB(loanId);

            // Get the overpayment amount
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            log.info("Total overpaid: {}", overpayment);
            assertTrue(overpayment > 0, "Should have overpayment");

            PostLoansLoanIdTransactionsResponse cbrResponse = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("17 March 2025").locale(LOCALE).transactionAmount(overpayment));
            assertNotNull(cbrResponse.getResourceId());
            cbrTxnIdRef.set(cbrResponse.getResourceId());
            log.info("CBR created for {} → txn id: {}", overpayment, cbrResponse.getResourceId());

            logAllTransactions(loanId, "After first CBR");
        });

        // Step 7: Snapshot accrual state before the problematic reversal
        runAt("18 March 2025", () -> {
            executeInlineCOB(loanId);
            logAccrualState(loanId, "Before CBR reversal");
        });

        // Step 8: Reverse CBR and re-create — this is the trigger for the bug
        AtomicReference<Long> newCbrTxnIdRef = new AtomicReference<>();
        runAt("20 March 2025", () -> {
            Long cbrTxnId = cbrTxnIdRef.get();
            log.info("=== Reversing CBR transaction: {} ===", cbrTxnId);

            reverseLoanTransaction(loanId, cbrTxnId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("20 March 2025").transactionAmount(0.0).locale(LOCALE));

            GetLoansLoanIdResponse afterReversal = getLoanDetails(loanId);
            log.info("Status after CBR reversal: {}", afterReversal.getStatus().getCode());
            logAccrualState(loanId, "After CBR reversal (before re-create)");

            // Re-create CBR with same amount
            double overpayment = Utils.getDoubleValue(afterReversal.getTotalOverpaid());
            log.info("Overpayment after CBR reversal: {}", overpayment);
            if (overpayment > 0) {
                PostLoansLoanIdTransactionsResponse newCbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                        .dateFormat(DATETIME_PATTERN).transactionDate("20 March 2025").locale(LOCALE).transactionAmount(overpayment));
                assertNotNull(newCbr.getResourceId());
                newCbrTxnIdRef.set(newCbr.getResourceId());
                log.info("New CBR created: {}", newCbr.getResourceId());
            }

            logAccrualState(loanId, "After CBR reversal + re-create");
        });

        // Step 9: Second reversal cycle — mirrors the multi-cycle pattern from production
        runAt("25 March 2025", () -> {
            Long newCbrId = newCbrTxnIdRef.get();
            if (newCbrId != null) {
                log.info("=== Second CBR reversal cycle: {} ===", newCbrId);

                reverseLoanTransaction(loanId, newCbrId, new PostLoansLoanIdTransactionsTransactionIdRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("25 March 2025").transactionAmount(0.0).locale(LOCALE));

                GetLoansLoanIdResponse afterReversal = getLoanDetails(loanId);
                double overpayment = Utils.getDoubleValue(afterReversal.getTotalOverpaid());
                if (overpayment > 0) {
                    makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                            .transactionDate("25 March 2025").locale(LOCALE).transactionAmount(overpayment));
                }
                logAccrualState(loanId, "After second CBR reversal cycle");
            }
        });

        // Step 10: Run COB and perform the critical assertion
        runAt("26 March 2025", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getTransactions());

            logAllTransactions(loanId, "Final state");

            // CRITICAL ASSERTION: No accrual transaction should be reversed
            assertNoReversedAccruals(loanDetails);

            log.info("Test completed. Loan status: {}", loanDetails.getStatus().getCode());
        });
    }

    /**
     * Simpler variant: just merchant refund + CBR reversal without goodwill credit and without interest refund support.
     * This serves as a baseline to confirm the simpler path doesn't trigger reversals.
     */
    @Test
    public void testNoAccrualReversalOnSimpleCBRReversalWithoutInterestRefund() {
        AtomicLong loanIdRef = new AtomicLong();

        runAt("01 November 2024", () -> {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive()//
                    .currencyCode("USD").principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1).interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true));

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        // Pay off the loan
        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));
        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));
        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));
        payInstallment(loanId, 3, "01 February 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 28));

        runAt("01 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            addRepaymentForLoan(loanId, Utils.getDoubleValue(installment.getTotalDueForPeriod()), "01 March 2025");
            assertTrue(getLoanDetails(loanId).getStatus().getClosedObligationsMet());
        });

        // Merchant refund → CBR → reverse CBR → new CBR
        AtomicReference<Long> cbrIdRef = new AtomicReference<>();
        runAt("15 March 2025", () -> {
            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 March 2025").locale(LOCALE).transactionAmount(50.0));
        });

        runAt("16 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            PostLoansLoanIdTransactionsResponse cbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025").locale(LOCALE).transactionAmount(overpayment));
            cbrIdRef.set(cbr.getResourceId());
        });

        runAt("20 March 2025", () -> {
            reverseLoanTransaction(loanId, cbrIdRef.get(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("20 March 2025").transactionAmount(0.0).locale(LOCALE));

            GetLoansLoanIdResponse afterReversal = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(afterReversal.getTotalOverpaid());
            if (overpayment > 0) {
                makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                        .transactionDate("20 March 2025").locale(LOCALE).transactionAmount(overpayment));
            }
        });

        runAt("21 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNoReversedAccruals(loanDetails);
        });
    }

    /**
     * Progressive loan with backdated CBR re-creation — mirrors the production scenario where CBR was reversed on
     * 12/16/25 but re-created backdated to 10/31/25.
     */
    @Test
    public void testNoAccrualReversalWithBackdatedCBRRecreation() {
        AtomicLong loanIdRef = new AtomicLong();

        runAt("01 November 2024", () -> {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4IProgressive()//
                    .currencyCode("USD").principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1).interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true)//
                    .addSupportedInterestRefundTypesItem("MERCHANT_ISSUED_REFUND")//
                    .paymentAllocation(List.of(//
                            createDefaultPaymentAllocation("NEXT_INSTALLMENT"), //
                            createPaymentAllocation("MERCHANT_ISSUED_REFUND", "NEXT_INSTALLMENT"), //
                            createPaymentAllocation("GOODWILL_CREDIT", "LAST_INSTALLMENT")//
            ))//
            );

            Long loanId = applyAndApproveProgressiveLoan(clientId, loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
            log.info("[Backdated CBR] Loan disbursed: id={}", loanId);
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        // Pay off the loan
        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));
        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));
        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));
        payInstallment(loanId, 3, "01 February 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 28));

        runAt("01 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            addRepaymentForLoan(loanId, Utils.getDoubleValue(installment.getTotalDueForPeriod()), "01 March 2025");
            assertTrue(getLoanDetails(loanId).getStatus().getClosedObligationsMet());
            log.info("[Backdated CBR] Loan closed on 01 March 2025");
        });

        runCobRange(loanId, fmt, LocalDate.of(2025, 3, 2), LocalDate.of(2025, 3, 14));

        // Goodwill credit + merchant refund → overpaid
        runAt("15 March 2025", () -> {
            makeGoodwillCredit(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 March 2025").locale(LOCALE).transactionAmount(0.5));
            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 March 2025").locale(LOCALE).transactionAmount(100.0));
            assertTrue(getLoanDetails(loanId).getStatus().getOverpaid());
        });

        // CBR on March 16
        AtomicReference<Long> cbrIdRef = new AtomicReference<>();
        runAt("16 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            PostLoansLoanIdTransactionsResponse cbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025").locale(LOCALE).transactionAmount(overpayment));
            cbrIdRef.set(cbr.getResourceId());
            logAccrualState(loanId, "After first CBR on March 16");
        });

        runCobRange(loanId, fmt, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 31));

        // Advance to April — reverse CBR and re-create it BACKDATED to March 16
        runAt("15 April 2025", () -> {
            log.info("[Backdated CBR] Reversing CBR {} and re-creating backdated to 16 March 2025", cbrIdRef.get());

            reverseLoanTransaction(loanId, cbrIdRef.get(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 April 2025").transactionAmount(0.0).locale(LOCALE));

            logAccrualState(loanId, "After CBR reversal on April 15");

            GetLoansLoanIdResponse afterReversal = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(afterReversal.getTotalOverpaid());
            if (overpayment > 0) {
                // Re-create CBR backdated to original date — this is the production pattern
                makeCreditBalanceRefund(loanId,
                        new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025") // BACKDATED
                                .locale(LOCALE).transactionAmount(overpayment));
                log.info("[Backdated CBR] New CBR created backdated to 16 March 2025");
            }

            logAccrualState(loanId, "After backdated CBR re-creation");
        });

        runAt("16 April 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            logAllTransactions(loanId, "Final backdated CBR");
            assertNoReversedAccruals(loanDetails);
        });
    }

    /**
     * Cumulative loan WITHOUT compounding but with backdated CBR — tests the reprocessExistingAccruals path
     * specifically for cumulative loans during CBR reversal.
     */
    @Test
    public void testNoAccrualReversalOnCumulativeLoanWithBackdatedCBR() {
        AtomicLong loanIdRef = new AtomicLong();

        runAt("01 November 2024", () -> {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4ICumulative()//
                    .currencyCode("USD").principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1).interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true)//
            );

            Long loanId = applyAndApproveCumulativeLoan(clientId, loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
            log.info("[Cumulative+Backdated] Loan disbursed: id={}", loanId);
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));
        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));
        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));
        payInstallment(loanId, 3, "01 February 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 2, 2), LocalDate.of(2025, 2, 28));

        runAt("01 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            addRepaymentForLoan(loanId, Utils.getDoubleValue(installment.getTotalDueForPeriod()), "01 March 2025");
            assertTrue(getLoanDetails(loanId).getStatus().getClosedObligationsMet());
            log.info("[Cumulative+Backdated] Loan closed");
        });

        runCobRange(loanId, fmt, LocalDate.of(2025, 3, 2), LocalDate.of(2025, 3, 14));

        // Merchant refund → overpaid
        AtomicReference<Long> cbrIdRef = new AtomicReference<>();
        runAt("15 March 2025", () -> {
            makeMerchantIssuedRefund(loanId, new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN)
                    .transactionDate("15 March 2025").locale(LOCALE).transactionAmount(50.0));
            assertTrue(getLoanDetails(loanId).getStatus().getOverpaid());
        });

        // CBR
        runAt("16 March 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            PostLoansLoanIdTransactionsResponse cbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025").locale(LOCALE).transactionAmount(overpayment));
            cbrIdRef.set(cbr.getResourceId());
            logAccrualState(loanId, "After CBR");
        });

        runCobRange(loanId, fmt, LocalDate.of(2025, 3, 17), LocalDate.of(2025, 3, 31));

        // Reverse and re-create backdated
        runAt("15 April 2025", () -> {
            log.info("[Cumulative+Backdated] Reversing CBR and re-creating backdated");
            reverseLoanTransaction(loanId, cbrIdRef.get(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 April 2025").transactionAmount(0.0).locale(LOCALE));

            logAccrualState(loanId, "After CBR reversal");

            GetLoansLoanIdResponse afterReversal = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(afterReversal.getTotalOverpaid());
            if (overpayment > 0) {
                makeCreditBalanceRefund(loanId,
                        new PostLoansLoanIdTransactionsRequest().dateFormat(DATETIME_PATTERN).transactionDate("16 March 2025") // BACKDATED
                                .locale(LOCALE).transactionAmount(overpayment));
            }
            logAccrualState(loanId, "After backdated CBR re-creation");
        });

        runAt("16 April 2025", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            logAllTransactions(loanId, "Final cumulative+backdated");
            assertNoReversedAccruals(loanDetails);
        });
    }

    /**
     * REPRODUCTION: Cumulative loan where last installment interest is NOT accrued by COB, then loan becomes overpaid
     * after lastDueDate. The final accrual is placed at overpaidOnDate > lastDueDate. A subsequent CBR undo triggers
     * reprocessExistingAccruals → reverseTransactionsAfter(lastDueDate) which reverses the post-due-date accrual.
     *
     * Key mechanism: 1. Skip COB for last installment period → interest unaccrued 2. Overpay after lastDueDate →
     * processAccrualsOnLoanClosure creates ACCRUAL at overpaidOnDate > lastDueDate 3. CBR + CBR undo →
     * reprocessExistingAccruals → reverseTransactionsAfter(lastDueDate) → REVERSES the accrual
     */
    @Test
    public void testNoAccrualReversalOnCumulativeLoanClosedAfterLastDueDate() {
        AtomicLong loanIdRef = new AtomicLong();

        runAt("01 November 2024", () -> {
            final Long clientId = createClient();

            final Long loanProductId = createLoanProduct(create4ICumulative()//
                    .currencyCode("USD").principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1).interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true)//
            );

            Long loanId = applyAndApproveCumulativeLoan(clientId, loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
            log.info("[AccrualFix] Loan disbursed: id={}", loanId);
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        // Pay installments 1-3 on time with COB — these get fully accrued
        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));
        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));
        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));
        payInstallment(loanId, 3, "01 February 2025");

        // CRITICAL: Do NOT run COB for the last installment period (Feb 2 - Feb 28)
        // This leaves installment 4's interest unaccrued, so processAccrualsOnLoanClosure
        // will need to create a final accrual with non-zero interest amount
        log.info("[AccrualFix] Skipping COB for last period — installment 4 interest stays unaccrued");

        // Overpay AFTER lastDueDate (01 March) → overpaidOnDate > lastDueDate
        // For cumulative loans: accrualDate = overpaidOnDate (via getFinalAccrualTransactionDate)
        // Since installment 4 interest is unaccrued, the final accrual will have non-zero amount
        // and will be placed at overpaidOnDate (after lastDueDate) → POST-DUE-DATE ACCRUAL
        runAt("05 March 2025", () -> {
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            double installmentAmount = Utils.getDoubleValue(installment.getTotalDueForPeriod());
            // Overpay by 50 to ensure OVERPAID status (extra covers any late interest)
            double overpayAmount = installmentAmount + 50.0;
            log.info("[AccrualFix] Installment 4 due: {}, paying: {} (overpaying by 50) on 05 March (due: 01 March)", installmentAmount,
                    overpayAmount);

            addRepaymentForLoan(loanId, overpayAmount, "05 March 2025");

            GetLoansLoanIdResponse afterPay = getLoanDetails(loanId);
            log.info("[AccrualFix] Status after overpayment: {}", afterPay.getStatus().getCode());
            assertTrue(afterPay.getStatus().getOverpaid(), "Loan should be OVERPAID but is: " + afterPay.getStatus().getCode());

            logAccrualState(loanId, "After overpayment — should have post-due-date accrual");
        });

        // CBR to refund overpayment
        AtomicReference<Long> cbrIdRef = new AtomicReference<>();
        runAt("15 March 2025", () -> {
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            log.info("[AccrualFix] Overpayment: {}, creating CBR", overpayment);
            assertTrue(overpayment > 0, "Should have overpayment");
            PostLoansLoanIdTransactionsResponse cbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 March 2025").locale(LOCALE).transactionAmount(overpayment));
            cbrIdRef.set(cbr.getResourceId());
            logAccrualState(loanId, "After CBR");
        });

        // Undo CBR → triggers reprocessExistingAccruals → reverseTransactionsAfter(lastDueDate)
        // The post-due-date accrual from the overpayment closure should get REVERSED here
        runAt("20 March 2025", () -> {
            log.info("[AccrualFix] === Undoing CBR {} ===", cbrIdRef.get());

            reverseLoanTransaction(loanId, cbrIdRef.get(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("20 March 2025").transactionAmount(0.0).locale(LOCALE));

            logAccrualState(loanId, "After CBR undo — checking for accrual reversal");
            logAllTransactions(loanId, "After CBR undo");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // This assertion should FAIL if the bug exists — accruals should NOT be reversed
            assertNoReversedAccruals(loanDetails);
        });
    }

    /**
     * Cumulative loan WITH interest recalculation — matches the production scenario. The post-due-date accrual is
     * created when COB is skipped for the last period and the loan is overpaid after lastDueDate. CBR undo triggers
     * reprocessExistingAccruals → reprocessPeriodicAccruals → adjustAccrualsAfter(lastDueDate) which should create an
     * ACCRUAL_ADJUSTMENT instead of reversing the post-due-date accrual.
     */
    @Test
    public void testNoAccrualReversalOnCumulativeLoanWithCBRUndo() {
        AtomicLong loanIdRef = new AtomicLong();

        runAt("01 November 2024", () -> {
            final Long clientId = createClient();

            // Cumulative loan WITH interest recalculation — matches production scenario
            final Long loanProductId = createLoanProduct(create4ICumulative()//
                    .currencyCode("USD").principal(220.0).minPrincipal(100.0).maxPrincipal(1000.0)//
                    .numberOfRepayments(4).repaymentEvery(1).interestRatePerPeriod(12.0).interestRateFrequencyType(3)//
                    .enableAccrualActivityPosting(true)//
            );

            Long loanId = applyAndApproveCumulativeLoan(clientId, loanProductId, "01 November 2024", 220.0, 12.0, 4, null);
            loanIdRef.set(loanId);
            disburseLoan(loanId, BigDecimal.valueOf(220.0), "01 November 2024");
            log.info("[AccrualFix] Loan disbursed: id={}", loanId);
        });

        Long loanId = loanIdRef.get();
        DateTimeFormatter fmt = DateTimeFormatter.ofPattern(DATETIME_PATTERN);

        // Pay installments 1-3 on time with COB
        runCobRange(loanId, fmt, LocalDate.of(2024, 11, 2), LocalDate.of(2024, 11, 30));
        payInstallment(loanId, 1, "01 December 2024");
        runCobRange(loanId, fmt, LocalDate.of(2024, 12, 2), LocalDate.of(2024, 12, 31));
        payInstallment(loanId, 2, "01 January 2025");
        runCobRange(loanId, fmt, LocalDate.of(2025, 1, 2), LocalDate.of(2025, 1, 31));
        payInstallment(loanId, 3, "01 February 2025");

        // Skip COB for last period — interest stays unaccrued
        log.info("[AccrualFix] Skipping COB for last period — installment 4 interest stays unaccrued");

        // Overpay to create post-due-date accrual and move loan to OVERPAID
        runAt("05 March 2025", () -> {
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null && p.getPeriod() == 4)
                    .findFirst().orElseThrow();
            double installmentAmount = Utils.getDoubleValue(installment.getTotalDueForPeriod());
            double overpayAmount = installmentAmount + 50.0;
            log.info("[AccrualFix] Installment 4 due: {}, overpaying with {} on 05 March", installmentAmount, overpayAmount);

            addRepaymentForLoan(loanId, overpayAmount, "05 March 2025");

            GetLoansLoanIdResponse afterPay = getLoanDetails(loanId);
            log.info("[AccrualFix] Status after overpayment: {}", afterPay.getStatus().getCode());
            assertTrue(afterPay.getStatus().getOverpaid(), "Loan should be OVERPAID but is: " + afterPay.getStatus().getCode());
            logAccrualState(loanId, "After overpayment");
        });

        // CBR to refund overpayment
        AtomicReference<Long> cbrIdRef = new AtomicReference<>();
        runAt("15 March 2025", () -> {
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            double overpayment = Utils.getDoubleValue(details.getTotalOverpaid());
            log.info("[AccrualFix] Overpayment: {}, creating CBR", overpayment);
            assertTrue(overpayment > 0, "Should have overpayment");
            PostLoansLoanIdTransactionsResponse cbr = makeCreditBalanceRefund(loanId, new PostLoansLoanIdTransactionsRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("15 March 2025").locale(LOCALE).transactionAmount(overpayment));
            cbrIdRef.set(cbr.getResourceId());
            logAccrualState(loanId, "After CBR");
        });

        // Undo CBR → triggers adjustLoanTransaction → reprocessExistingAccruals → reprocessPeriodicAccruals
        // → adjustAccrualsAfter(lastDueDate) → should create ACCRUAL_ADJUSTMENT (not reverse the accrual)
        runAt("20 March 2025", () -> {
            log.info("[AccrualFix] === Undoing CBR {} ===", cbrIdRef.get());

            reverseLoanTransaction(loanId, cbrIdRef.get(), new PostLoansLoanIdTransactionsTransactionIdRequest()
                    .dateFormat(DATETIME_PATTERN).transactionDate("20 March 2025").transactionAmount(0.0).locale(LOCALE));

            logAccrualState(loanId, "After CBR undo");
            logAllTransactions(loanId, "After CBR undo");

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // Accruals must never be reversed — check via assertNoReversedAccruals
            assertNoReversedAccruals(loanDetails);
        });
    }

    // --- Helper methods ---

    private void runCobRange(Long loanId, DateTimeFormatter fmt, LocalDate from, LocalDate to) {
        for (LocalDate date = from; !date.isAfter(to); date = date.plusDays(1)) {
            final String dateStr = fmt.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }
    }

    private void payInstallment(Long loanId, int installmentNumber, String date) {
        runAt(date, () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse details = getLoanDetails(loanId);
            var installment = details.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == installmentNumber).findFirst().orElseThrow();
            double amount = Utils.getDoubleValue(installment.getTotalDueForPeriod());
            log.info("Paying installment #{}: {}", installmentNumber, amount);
            addRepaymentForLoan(loanId, amount, date);
        });
    }

    private void assertNoReversedAccruals(GetLoansLoanIdResponse loanDetails) {
        List<GetLoansLoanIdTransactions> allAccruals = loanDetails.getTransactions().stream()
                .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode())).toList();

        List<GetLoansLoanIdTransactions> reversedAccruals = allAccruals.stream().filter(tx -> Boolean.TRUE.equals(tx.getManuallyReversed()))
                .toList();

        // Log detailed breakdown: count by date and reversed status
        LocalDate lastDueDate = loanDetails.getRepaymentSchedule().getPeriods().stream().filter(p -> p.getPeriod() != null)
                .map(p -> p.getDueDate()).max(LocalDate::compareTo).orElse(null);
        long postDueDateCount = allAccruals.stream()
                .filter(tx -> tx.getDate() != null && lastDueDate != null && tx.getDate().isAfter(lastDueDate)).count();
        long postDueDateReversed = allAccruals.stream().filter(tx -> tx.getDate() != null && lastDueDate != null
                && tx.getDate().isAfter(lastDueDate) && Boolean.TRUE.equals(tx.getManuallyReversed())).count();
        log.info("Accrual analysis: total={}, reversed={}, lastDueDate={}, postDueDate={}, postDueDateReversed={}", allAccruals.size(),
                reversedAccruals.size(), lastDueDate, postDueDateCount, postDueDateReversed);

        // Log each post-due-date accrual
        allAccruals.stream().filter(tx -> tx.getDate() != null && lastDueDate != null && tx.getDate().isAfter(lastDueDate)).forEach(tx -> {
            log.info("  POST-DUE-DATE accrual: id={}, date={}, amount={}, interest={}, reversed={}", tx.getId(), tx.getDate(),
                    tx.getAmount(), tx.getInterestPortion(), tx.getManuallyReversed());
        });

        if (!reversedAccruals.isEmpty()) {
            StringBuilder msg = new StringBuilder("Found reversed accrual transactions:\n");
            for (GetLoansLoanIdTransactions reversed : reversedAccruals) {
                msg.append(String.format("  id=%d, date=%s, amount=%s%n", reversed.getId(), reversed.getDate(), reversed.getAmount()));
            }
            msg.append("Accruals should NEVER be reversed. Use ACCRUAL_ADJUSTMENT (type 34) instead.");
            assertEquals(0, reversedAccruals.size(), msg.toString());
        }

        log.info("Assertion passed: {} accrual transactions, {} reversed", allAccruals.size(), reversedAccruals.size());
    }

    private void logAccrualState(Long loanId, String label) {
        GetLoansLoanIdResponse details = getLoanDetails(loanId);
        List<GetLoansLoanIdTransactions> accruals = details.getTransactions().stream()
                .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode())
                        || "loanTransactionType.accrualAdjustment".equals(tx.getType().getCode()))
                .toList();
        log.info("=== Accrual state [{}]: {} transactions ===", label, accruals.size());
        for (GetLoansLoanIdTransactions a : accruals) {
            log.info("  {}: id={}, date={}, amount={}, interest={}, reversed={}", a.getType().getValue(), a.getId(), a.getDate(),
                    a.getAmount(), a.getInterestPortion(), a.getManuallyReversed());
        }
    }

    private void logAllTransactions(Long loanId, String label) {
        GetLoansLoanIdResponse details = getLoanDetails(loanId);
        log.info("=== All transactions [{}] ===", label);
        for (GetLoansLoanIdTransactions tx : details.getTransactions()) {
            log.info("  {}: id={}, date={}, amount={}, reversed={}", tx.getType().getValue(), tx.getId(), tx.getDate(), tx.getAmount(),
                    tx.getManuallyReversed());
        }
    }
}
