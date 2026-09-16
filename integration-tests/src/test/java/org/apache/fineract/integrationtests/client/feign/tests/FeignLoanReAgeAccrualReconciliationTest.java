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
package org.apache.fineract.integrationtests.client.feign.tests;

import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.RepaymentFrequencyType;
import org.junit.jupiter.api.Test;

@Slf4j
public class FeignLoanReAgeAccrualReconciliationTest extends FeignLoanTestBase {

    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd MMMM yyyy");
    /** Rounding between the per-day accrual postings and the recomputed balance can differ by a cent. */
    private static final BigDecimal RECONCILIATION_TOLERANCE = new BigDecimal("0.01");

    /** Interest accrued in the current period must stay in step with the accrual transactions posted for it. */
    @Test
    void notDueInterestTracksDailyAccrualAfterReAge() {
        AtomicLong loanIdHolder = new AtomicLong();
        LocalDate disbursementDate = LocalDate.of(2026, 1, 1);

        runAt(DATE_FORMAT.format(disbursementDate), () -> {
            Long clientId = createClient();

            int numberOfRepayments = 6;
            PostLoanProductsRequest product = create4IProgressive() //
                    .numberOfRepayments(numberOfRepayments) //
                    .repaymentEvery(1) //
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS_L);
            Long loanProductId = createLoanProduct(product);

            double amount = 6000.0;
            PostLoansRequest applicationRequest = applyLoanRequest(clientId, loanProductId, DATE_FORMAT.format(disbursementDate), amount,
                    numberOfRepayments)//
                    .transactionProcessingStrategyCode(LoanTestData.TransactionProcessingStrategyCode.ADVANCED_PAYMENT_ALLOCATION_STRATEGY)//
                    .repaymentEvery(1)//
                    .loanTermFrequency(numberOfRepayments)//
                    .repaymentFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .loanTermFrequencyType(RepaymentFrequencyType.MONTHS)//
                    .interestRatePerPeriod(BigDecimal.valueOf(10.0))//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.DAILY);

            Long loanId = applyForLoan(applicationRequest);
            approveLoan(loanId, approveLoanRequest(amount, DATE_FORMAT.format(disbursementDate)));
            disburseLoan(loanId, BigDecimal.valueOf(amount), DATE_FORMAT.format(disbursementDate));
            loanIdHolder.set(loanId);
        });

        Long loanId = loanIdHolder.get();

        LocalDate reAgeDate = LocalDate.of(2026, 3, 15);
        runAt(DATE_FORMAT.format(disbursementDate.plusDays(1)), () -> executeInlineCOB(loanId));
        runAt(DATE_FORMAT.format(reAgeDate), () -> {
            executeInlineCOB(loanId);
            reAgeLoan(loanId, RepaymentFrequencyType.MONTHS_STRING, 1, "01 April 2026", 5, "EQUAL_AMORTIZATION_PAYABLE_INTEREST");
        });

        BigDecimal[] settledNotDue = new BigDecimal[1];
        Long[] lastSeenAccrualTxId = new Long[1];
        runAt(DATE_FORMAT.format(reAgeDate.plusDays(1)), () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            settledNotDue[0] = loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest();
            lastSeenAccrualTxId[0] = latestAccrualTransactionId(loanDetails);
        });
        assertNotNull(settledNotDue[0], "summary.totalUnpaidPayableNotDueInterest was not reported after the re-age settled");
        log.info("totalUnpaidPayableNotDueInterest after re-age settles to {}", settledNotDue[0]);

        BigDecimal[] totalAccruedSinceSettled = new BigDecimal[1];
        BigDecimal[] latestNotDue = new BigDecimal[1];
        LocalDate finalDate = reAgeDate.plusDays(5);
        runAt(DATE_FORMAT.format(finalDate), () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            latestNotDue[0] = loanDetails.getSummary().getTotalUnpaidPayableNotDueInterest();

            List<GetLoansLoanIdTransactions> newAccruals = nonReversedAccrualsAfter(loanDetails, lastSeenAccrualTxId[0]);
            totalAccruedSinceSettled[0] = newAccruals.stream()//
                    .map(tx -> tx.getInterestPortion() == null ? BigDecimal.ZERO : tx.getInterestPortion())//
                    .reduce(BigDecimal.ZERO, BigDecimal::add);

            log.info("New accrual interestPortion={}, totalUnpaidPayableNotDueInterest={}", totalAccruedSinceSettled[0], latestNotDue[0]);
        });

        assertNotNull(latestNotDue[0], "summary.totalUnpaidPayableNotDueInterest was not reported on the final date");

        BigDecimal notDueGrowthSinceSettled = latestNotDue[0].subtract(settledNotDue[0]);
        log.info("Since re-age settled: accrual activity totaled {} and totalUnpaidPayableNotDueInterest grew by {}",
                totalAccruedSinceSettled[0], notDueGrowthSinceSettled);

        assertTrue(totalAccruedSinceSettled[0].compareTo(BigDecimal.ZERO) > 0,
                "Test setup issue: no further loanTransactionType.accrual transactions were posted after the re-age settled, "
                        + "so this scenario does not exercise the behaviour under test");

        // The two must reconcile: an external process that sums transaction activity and expects it to equal the
        // balance delta fails by the difference, every day, for as long as they disagree.
        BigDecimal reconciliationGap = notDueGrowthSinceSettled.subtract(totalAccruedSinceSettled[0]).abs();
        assertTrue(reconciliationGap.compareTo(RECONCILIATION_TOLERANCE) <= 0,
                "totalUnpaidPayableNotDueInterest should have grown by " + totalAccruedSinceSettled[0]
                        + " to track the daily INTEREST_ACCRUAL transactions posted after the re-age, but it grew by "
                        + notDueGrowthSinceSettled + " (" + settledNotDue[0] + " -> " + latestNotDue[0] + "), leaving a gap of "
                        + reconciliationGap);
    }

    private Long latestAccrualTransactionId(GetLoansLoanIdResponse loanDetails) {
        return nonReversedAccrualsAfter(loanDetails, -1L).stream().map(GetLoansLoanIdTransactions::getId).max(Long::compareTo).orElse(-1L);
    }

    private List<GetLoansLoanIdTransactions> nonReversedAccrualsAfter(GetLoansLoanIdResponse loanDetails, Long afterId) {
        List<GetLoansLoanIdTransactions> transactions = loanDetails.getTransactions();
        if (transactions == null) {
            return List.of();
        }
        return transactions.stream()//
                .filter(tx -> tx.getType() != null && "loanTransactionType.accrual".equals(tx.getType().getCode()))//
                .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())//
                .filter(tx -> tx.getId() != null && tx.getId() > afterId)//
                .toList();
    }
}
