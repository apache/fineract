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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.GetLoansLoanIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdStatus;
import org.apache.fineract.client.models.GetLoansLoanIdTransactions;
import org.apache.fineract.client.models.GetLoansLoanIdTransactionsTemplateResponse;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

@Slf4j
public class FeignLoanAccrualOnClosedLoanTest extends FeignLoanTestBase {

    private static final DateTimeFormatter ISO_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final String DATETIME_PATTERN = LoanTestData.DATETIME_PATTERN;

    @Test
    void testPrepayLoanShouldNotReceiveAccrualAfterClosure() {
        final Long[] loanIdHolder = new Long[1];

        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(12)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 12, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            assertNotNull(loanDetails.getRepaymentSchedule());

            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();

            double firstInstallmentAmount = Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod());
            log.info("First installment amount: {}", firstInstallmentAmount);

            addRepayment(loanId, repayment(firstInstallmentAmount, "01 February 2026"));
        });

        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 2, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-15", () -> {
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);

            double prepayAmount = prepayTemplate.getAmount();
            log.info("Prepay template amount: {}", prepayAmount);
            log.info("Prepay template interest portion: {}", prepayTemplate.getInterestPortion());
            log.info("Prepay template principal portion: {}", prepayTemplate.getPrincipalPortion());

            addRepayment(loanId, repayment(prepayAmount, "15 February 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            log.info("Loan status after prepay: {}", loanAfterPrepay.getStatus().getCode());
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterPrepay.getSummary().getTotalOutstanding()),
                    "Total outstanding should be 0 after prepay");
        });

        runAt("2026-02-16", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanAfterCOB = getLoanDetails(loanId);
            log.info("Loan status after COB: {}", loanAfterCOB.getStatus().getCode());
            log.info("Outstanding after COB: principal={}, interest={}, total={}", loanAfterCOB.getSummary().getPrincipalOutstanding(),
                    loanAfterCOB.getSummary().getInterestOutstanding(), loanAfterCOB.getSummary().getTotalOutstanding());

            verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);

            assertEquals(0.0, Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding()),
                    "Total outstanding should still be 0 after COB");

            List<GetLoansLoanIdTransactions> transactions = loanAfterCOB.getTransactions();
            assertNotNull(transactions);

            LocalDate prepayDate = LocalDate.of(2026, 2, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = transactions.stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getDate().isAfter(prepayDate))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed()).toList();

            log.info("Post-closure accrual transactions found: {}", postClosureAccruals.size());
            for (GetLoansLoanIdTransactions accrual : postClosureAccruals) {
                log.info("  Accrual: date={}, amount={}, interest={}", accrual.getDate(), accrual.getAmount(),
                        accrual.getInterestPortion());
            }

            assertTrue(postClosureAccruals.isEmpty(),
                    "No accrual transactions should be posted after the loan is closed. Found " + postClosureAccruals.size()
                            + " accrual(s) with total interest: "
                            + postClosureAccruals.stream().mapToDouble(a -> Utils.getDoubleValue(a.getInterestPortion())).sum());
        });
    }

    @Test
    void testPrepayAmountShouldIncludeFullAccruedInterest() {
        final Long[] loanIdHolder = new Long[1];

        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(12)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 12, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()), "01 February 2026"));
        });

        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 2, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-15", () -> {
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            double totalAccruedInterest = loanBeforePrepay.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                    .mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
            double totalInterestPaid = loanBeforePrepay.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.repayment".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                    .mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
            double unpaidAccruedInterest = totalAccruedInterest - totalInterestPaid;
            log.info("Before prepay - totalAccrued: {}, totalPaid: {}, unpaidAccrued: {}", totalAccruedInterest, totalInterestPaid,
                    unpaidAccruedInterest);

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion() : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest,
                    prepayTemplate.getPrincipalPortion());

            assertTrue(templateInterest >= unpaidAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all unpaid accrued interest (" + unpaidAccruedInterest
                            + "). Shortfall: " + (unpaidAccruedInterest - templateInterest));

            addRepayment(loanId, repayment(prepayAmount, "15 February 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });

        runAt("2026-02-16", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanAfterCOB = getLoanDetails(loanId);

            double totalOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding());
            double interestOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getInterestOutstanding());

            log.info("After COB - status: {}, totalOutstanding: {}, interestOutstanding: {}", loanAfterCOB.getStatus().getCode(),
                    totalOutstanding, interestOutstanding);

            assertEquals(0.0, interestOutstanding,
                    "Interest outstanding should be 0 after prepay + COB. "
                            + "The prepay template should have included all accrued interest. " + "Found interest outstanding: "
                            + interestOutstanding);

            assertEquals(0.0, totalOutstanding, "Total outstanding should be 0 after prepay + COB. Found: " + totalOutstanding);

            verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);
        });
    }

    @Test
    void testMaturedLoanPrepayAmountShouldIncludePostMaturityInterest() {
        final Long[] loanIdHolder = new Long[1];

        // Step 1: Create and disburse a 3-installment loan on Jan 1
        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(3)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 3, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        // Step 2: Run daily COB from Jan 2 to Feb 1
        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 3: Pay 1st installment on Feb 1
        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            double firstAmount = Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod());
            log.info("Paying 1st installment: {}", firstAmount);
            addRepayment(loanId, repayment(firstAmount, "01 February 2026"));
        });

        // Step 4: Run daily COB from Feb 2 to Mar 1
        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 3, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 5: Pay 2nd installment on Mar 1
        runAt("2026-03-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var secondInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 2).findFirst().orElseThrow();
            double secondAmount = Utils.getDoubleValue(secondInstallment.getTotalDueForPeriod());
            log.info("Paying 2nd installment: {}", secondAmount);
            addRepayment(loanId, repayment(secondAmount, "01 March 2026"));
        });

        // Step 6: Run daily COB from Mar 2 through Apr 15
        // 3rd installment due date (Apr 1) passes WITHOUT payment — loan matures
        for (LocalDate date = LocalDate.of(2026, 3, 2); !date.isAfter(LocalDate.of(2026, 4, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 7: On Apr 15 — all installments past due, loan matured
        // Query prepay template and verify it includes post-maturity interest
        runAt("2026-04-15", () -> {
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            log.info("Loan status before prepay: {}", loanBeforePrepay.getStatus().getCode());

            // Calculate total accrued interest and total interest already paid
            double totalAccruedInterest = loanBeforePrepay.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                    .mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
            double totalInterestPaid = loanBeforePrepay.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.repayment".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                    .mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
            double unpaidAccruedInterest = totalAccruedInterest - totalInterestPaid;
            log.info("Before prepay - totalAccrued: {}, totalPaid: {}, unpaidAccrued: {}", totalAccruedInterest, totalInterestPaid,
                    unpaidAccruedInterest);

            // Query prepay template
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 April 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);
            double prepayAmount = prepayTemplate.getAmount();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion() : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest,
                    prepayTemplate.getPrincipalPortion());

            // KEY ASSERTION: prepay template interest must cover all unpaid accrued interest
            // This includes post-maturity interest for Apr 1 → Apr 15
            assertTrue(templateInterest >= unpaidAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all unpaid accrued interest (" + unpaidAccruedInterest
                            + ") including post-maturity period. Shortfall: " + (unpaidAccruedInterest - templateInterest));

            // Pay the prepay amount
            addRepayment(loanId, repayment(prepayAmount, "15 April 2026"));

            // Verify loan closes
            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            log.info("Loan status after prepay: {}", loanAfterPrepay.getStatus().getCode());
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterPrepay.getSummary().getTotalOutstanding()),
                    "Total outstanding should be 0 after prepay");
        });

        // Step 8: Run COB the next day — verify no spurious accruals
        runAt("2026-04-16", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanAfterCOB = getLoanDetails(loanId);
            double totalOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding());
            double interestOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getInterestOutstanding());

            log.info("After COB - status: {}, totalOutstanding: {}, interestOutstanding: {}", loanAfterCOB.getStatus().getCode(),
                    totalOutstanding, interestOutstanding);

            verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);

            assertEquals(0.0, interestOutstanding,
                    "Interest outstanding should be 0 after prepay + COB on matured loan. Found: " + interestOutstanding);

            assertEquals(0.0, totalOutstanding,
                    "Total outstanding should be 0 after prepay + COB on matured loan. Found: " + totalOutstanding);

            // Verify no accrual transactions posted after closure
            List<GetLoansLoanIdTransactions> transactions = loanAfterCOB.getTransactions();
            assertNotNull(transactions);

            LocalDate prepayDate = LocalDate.of(2026, 4, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = transactions.stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getDate().isAfter(prepayDate))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed()).toList();

            log.info("Post-closure accrual transactions found: {}", postClosureAccruals.size());
            for (GetLoansLoanIdTransactions accrual : postClosureAccruals) {
                log.info("  Accrual: date={}, amount={}, interest={}", accrual.getDate(), accrual.getAmount(),
                        accrual.getInterestPortion());
            }

            assertTrue(postClosureAccruals.isEmpty(),
                    "No accrual transactions should be posted after the matured loan is closed. Found " + postClosureAccruals.size()
                            + " accrual(s) with total interest: "
                            + postClosureAccruals.stream().mapToDouble(a -> Utils.getDoubleValue(a.getInterestPortion())).sum());
        });
    }

    @Test
    void testN1InstallmentExistsAfterPostMaturityPrepay() {
        final Long[] loanIdHolder = new Long[1];

        // Step 1: Create and disburse a 3-installment loan on Jan 1
        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(3)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 3, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        // Step 2: Run daily COB and pay installments 1 and 2
        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()), "01 February 2026"));
        });

        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 3, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-03-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var secondInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 2).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(secondInstallment.getTotalDueForPeriod()), "01 March 2026"));
        });

        // Step 3: Let installment 3 (Apr 1) pass unpaid, COB runs through Apr 15
        for (LocalDate date = LocalDate.of(2026, 3, 2); !date.isAfter(LocalDate.of(2026, 4, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 4: Before prepay — verify schedule has exactly 3 installment periods
        runAt("2026-04-15", () -> {
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            List<GetLoansLoanIdRepaymentPeriod> periodsBeforePrepay = loanBeforePrepay.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null).toList();
            log.info("Schedule periods before prepay: {}", periodsBeforePrepay.size());
            for (GetLoansLoanIdRepaymentPeriod p : periodsBeforePrepay) {
                log.info("  Period {}: fromDate={}, dueDate={}, interestDue={}", p.getPeriod(), p.getFromDate(), p.getDueDate(),
                        p.getInterestDue());
            }

            // Prepay on Apr 15
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 April 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount();
            log.info("Prepay amount: {}", prepayAmount);
            addRepayment(loanId, repayment(prepayAmount, "15 April 2026"));

            // After prepay — verify N+1 installment exists
            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);

            List<GetLoansLoanIdRepaymentPeriod> periodsAfterPrepay = loanAfterPrepay.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null).toList();
            log.info("Schedule periods after prepay: {}", periodsAfterPrepay.size());
            for (GetLoansLoanIdRepaymentPeriod p : periodsAfterPrepay) {
                log.info("  Period {}: fromDate={}, dueDate={}, interestDue={}, principalDue={}", p.getPeriod(), p.getFromDate(),
                        p.getDueDate(), p.getInterestDue(), p.getPrincipalDue());
            }

            assertTrue(periodsAfterPrepay.size() > 3,
                    "After post-maturity prepay, schedule should have more than 3 periods (N+1 installment). Found: "
                            + periodsAfterPrepay.size());

            // Find the N+1 period (period 4)
            GetLoansLoanIdRepaymentPeriod n1Period = periodsAfterPrepay.stream().filter(p -> p.getPeriod() == 4).findFirst()
                    .orElse(periodsAfterPrepay.get(periodsAfterPrepay.size() - 1));

            // N+1 period should cover from Apr 1 onwards
            assertNotNull(n1Period.getFromDate(), "N+1 period should have a fromDate");
            assertNotNull(n1Period.getDueDate(), "N+1 period should have a dueDate");
            assertEquals(LocalDate.of(2026, 4, 1), n1Period.getFromDate(),
                    "N+1 period should start from last installment due date (Apr 1)");

            // N+1 period should have interest > 0
            double n1Interest = Utils.getDoubleValue(n1Period.getInterestDue());
            log.info("N+1 period interest: {}", n1Interest);
            assertTrue(n1Interest > 0, "N+1 period should carry post-maturity interest. Found: " + n1Interest);

            assertEquals(0.0, Utils.getDoubleValue(loanAfterPrepay.getSummary().getTotalOutstanding()),
                    "Total outstanding should be 0 after prepay");
        });
    }

    @Test
    void testPostMaturityInterestAccruesDaily() {
        final Long[] loanIdHolder = new Long[1];

        // Step 1: Create and disburse a 3-installment loan on Jan 1
        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(3)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 3, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        // Step 2: Pay installments 1 and 2 on time
        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()), "01 February 2026"));
        });

        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 3, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-03-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var secondInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 2).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(secondInstallment.getTotalDueForPeriod()), "01 March 2026"));
        });

        // Step 3: Let installment 3 (Apr 1) pass unpaid, COB runs through Apr 10
        for (LocalDate date = LocalDate.of(2026, 3, 2); !date.isAfter(LocalDate.of(2026, 4, 10)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 4: Verify prepay amount on Apr 15 includes post-maturity interest
        runAt("2026-04-15", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            // Get installment 3's total due (principal + scheduled interest for the period)
            var installment3 = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 3).findFirst().orElseThrow();
            double installment3TotalDue = Utils.getDoubleValue(installment3.getTotalDueForPeriod());
            log.info("Installment 3 total due: {}", installment3TotalDue);

            // Get prepay amount on Apr 15 (14 days after last installment due date)
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 April 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount();
            log.info("Prepay amount on Apr 15: {}", prepayAmount);

            // Prepay amount must be greater than installment 3's total due because it includes
            // post-maturity interest (Apr 1 → Apr 15). This proves the schedule generator correctly
            // computes additional interest during prepayment calculation.
            assertTrue(prepayAmount > installment3TotalDue,
                    "Prepay amount should include post-maturity interest beyond installment 3 total due. " + "Prepay amount: "
                            + prepayAmount + ", Installment 3 total due: " + installment3TotalDue);

            // Prepay and verify loan closes successfully
            addRepayment(loanId, repayment(prepayAmount, "15 April 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });
    }

    @Test
    void testPostMaturityPrepayWithNoInstallmentsPaid() {
        final Long[] loanIdHolder = new Long[1];

        // Step 1: Create and disburse a 3-installment loan on Jan 1
        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(3)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 3, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        // Step 2: NO payments at all — run COB from Jan 2 through Apr 15
        // All 3 installments (Feb 1, Mar 1, Apr 1) pass unpaid
        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 4, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 3: Prepay on Apr 15 for the full amount
        runAt("2026-04-15", () -> {
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            log.info("Loan status before prepay (no installments paid): {}", loanBeforePrepay.getStatus().getCode());

            // Calculate total accrued interest (should include post-maturity accruals on full 100k principal)
            double totalAccruedInterest = loanBeforePrepay.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed())
                    .mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
            log.info("Total accrued interest (all on full principal, no payments): {}", totalAccruedInterest);

            // Query prepay template
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 April 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);
            double prepayAmount = prepayTemplate.getAmount();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion() : 0.0;
            double templatePrincipal = prepayTemplate.getPrincipalPortion() != null ? prepayTemplate.getPrincipalPortion() : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest, templatePrincipal);

            // Prepay template must include full principal + all interest (including post-maturity)
            assertTrue(templateInterest >= totalAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all accrued interest (" + totalAccruedInterest
                            + ") including post-maturity period on full 100k principal. Shortfall: "
                            + (totalAccruedInterest - templateInterest));

            // Pay the prepay amount
            addRepayment(loanId, repayment(prepayAmount, "15 April 2026"));

            // Verify loan closes
            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            log.info("Loan status after prepay: {}", loanAfterPrepay.getStatus().getCode());
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterPrepay.getSummary().getTotalOutstanding()),
                    "Total outstanding should be 0 after prepay");
        });

        // Step 4: Run COB the next day — verify no spurious accruals
        runAt("2026-04-16", () -> {
            executeInlineCOB(loanId);

            GetLoansLoanIdResponse loanAfterCOB = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding()),
                    "Total outstanding should remain 0 after COB on closed loan with no prior payments");
            assertEquals(0.0, Utils.getDoubleValue(loanAfterCOB.getSummary().getInterestOutstanding()),
                    "Interest outstanding should be 0 after COB");

            // Verify no accrual transactions posted after closure
            LocalDate prepayDate = LocalDate.of(2026, 4, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = loanAfterCOB.getTransactions().stream()
                    .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                    .filter(tx -> tx.getDate().isAfter(prepayDate))
                    .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed()).toList();

            log.info("Post-closure accrual transactions found: {}", postClosureAccruals.size());
            assertTrue(postClosureAccruals.isEmpty(),
                    "No accrual transactions should be posted after closure (all installments unpaid scenario). Found "
                            + postClosureAccruals.size() + " accrual(s)");
        });
    }

    @Test
    void testMultipleCOBCyclesAfterClosureProduceNoAccruals() {
        final Long[] loanIdHolder = new Long[1];

        // Step 1: Create and disburse a 3-installment loan on Jan 1
        runAt("2026-01-01", () -> {
            Long clientId = createClient("01 January 2026");

            PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                    .currencyCode("USD")//
                    .principal(100000.0)//
                    .minPrincipal(1000.0)//
                    .maxPrincipal(200000.0)//
                    .numberOfRepayments(3)//
                    .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                    .interestRatePerPeriod(12.0)//
                    .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                    .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                    .allowPartialPeriodInterestCalculation(true);

            Long productId = createLoanProduct(productRequest);

            Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, "01 January 2026", 100000.0, 3, 12.0));
            approveLoan(loanId, LoanRequestBuilders.approveLoan(100000.0, "01 January 2026"));
            disburseLoan(loanId, LoanRequestBuilders.disburseLoan(100000.0, "01 January 2026"));
            loanIdHolder[0] = loanId;
        });

        Long loanId = loanIdHolder[0];

        // Step 2: Pay installments 1 and 2 on time
        for (LocalDate date = LocalDate.of(2026, 1, 2); !date.isAfter(LocalDate.of(2026, 2, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-02-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var firstInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(firstInstallment.getTotalDueForPeriod()), "01 February 2026"));
        });

        for (LocalDate date = LocalDate.of(2026, 2, 2); !date.isAfter(LocalDate.of(2026, 3, 1)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        runAt("2026-03-01", () -> {
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);
            var secondInstallment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 2).findFirst().orElseThrow();
            addRepayment(loanId, repayment(Utils.getDoubleValue(secondInstallment.getTotalDueForPeriod()), "01 March 2026"));
        });

        // Step 3: Let installment 3 (Apr 1) pass unpaid, COB through Apr 15
        for (LocalDate date = LocalDate.of(2026, 3, 2); !date.isAfter(LocalDate.of(2026, 4, 15)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            runAt(dateStr, () -> executeInlineCOB(loanId));
        }

        // Step 4: Prepay on Apr 15
        runAt("2026-04-15", () -> {
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 April 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount();
            log.info("Prepay amount: {}", prepayAmount);
            addRepayment(loanId, repayment(prepayAmount, "15 April 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });

        // Step 5: Run COB for 5 consecutive days after closure (Apr 16 → Apr 20)
        // Verify durability of accrual cap on each day
        LocalDate closureDate = LocalDate.of(2026, 4, 15);

        for (LocalDate date = LocalDate.of(2026, 4, 16); !date.isAfter(LocalDate.of(2026, 4, 20)); date = date.plusDays(1)) {
            final String dateStr = ISO_FORMAT.format(date);
            final LocalDate currentDate = date;
            runAt(dateStr, () -> {
                executeInlineCOB(loanId);

                GetLoansLoanIdResponse loanAfterCOB = getLoanDetails(loanId);

                // Status must remain closed
                verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);

                // Total outstanding must remain 0
                double totalOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding());
                assertEquals(0.0, totalOutstanding,
                        "Total outstanding should be 0 after COB on " + currentDate + ". Found: " + totalOutstanding);

                // Interest outstanding must remain 0
                double interestOutstanding = Utils.getDoubleValue(loanAfterCOB.getSummary().getInterestOutstanding());
                assertEquals(0.0, interestOutstanding,
                        "Interest outstanding should be 0 after COB on " + currentDate + ". Found: " + interestOutstanding);

                // No new non-reversed accrual transactions after closure
                List<GetLoansLoanIdTransactions> postClosureAccruals = loanAfterCOB.getTransactions().stream()
                        .filter(tx -> "loanTransactionType.accrual".equals(tx.getType().getCode()))
                        .filter(tx -> tx.getDate().isAfter(closureDate))
                        .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed()).toList();

                log.info("COB on {} - postClosureAccruals: {}, totalOutstanding: {}, interestOutstanding: {}", currentDate,
                        postClosureAccruals.size(), totalOutstanding, interestOutstanding);

                assertTrue(postClosureAccruals.isEmpty(), "No accrual transactions should be posted after closure. On " + currentDate
                        + " found " + postClosureAccruals.size() + " accrual(s)");
            });
        }
    }
}
