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
    private static final String LOAN_START_DATE = "01 January 2026";
    private static final double PRINCIPAL = 100000.0;
    private static final double ANNUAL_INTEREST_RATE = 12.0;

    private Long createCumulativeLoan(final int numberOfRepayments) {
        Long clientId = createClient(LOAN_START_DATE);

        PostLoanProductsRequest productRequest = fourInstallmentsCumulativeWithInterestRecalculation()//
                .currencyCode("USD")//
                .principal(PRINCIPAL)//
                .minPrincipal(1000.0)//
                .maxPrincipal(200000.0)//
                .numberOfRepayments(numberOfRepayments)//
                .amortizationType(LoanTestData.AmortizationType.EQUAL_PRINCIPAL)//
                .interestRatePerPeriod(ANNUAL_INTEREST_RATE)//
                .interestRateFrequencyType(LoanTestData.InterestRateFrequencyType.YEARS)//
                .interestCalculationPeriodType(LoanTestData.InterestCalculationPeriodType.SAME_AS_REPAYMENT_PERIOD)//
                .allowPartialPeriodInterestCalculation(true);

        Long productId = createLoanProduct(productRequest);
        Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoan(clientId, productId, LOAN_START_DATE, PRINCIPAL,
                numberOfRepayments, ANNUAL_INTEREST_RATE));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, LOAN_START_DATE));
        disburseLoan(loanId, LoanRequestBuilders.disburseLoan(PRINCIPAL, LOAN_START_DATE));
        return loanId;
    }

    private List<GetLoansLoanIdTransactions> getNonReversedTransactions(final GetLoansLoanIdResponse loanDetails,
            final String transactionTypeCode) {
        return loanDetails.getTransactions().stream().filter(tx -> transactionTypeCode.equals(tx.getType().getCode()))
                .filter(tx -> tx.getManuallyReversed() == null || !tx.getManuallyReversed()).toList();
    }

    private List<GetLoansLoanIdTransactions> getPostClosureAccruals(final GetLoansLoanIdResponse loanDetails, final LocalDate closureDate) {
        return getNonReversedTransactions(loanDetails, "loanTransactionType.accrual").stream()
                .filter(tx -> tx.getDate().isAfter(closureDate)).toList();
    }

    private double sumInterestPortion(final List<GetLoansLoanIdTransactions> transactions) {
        return transactions.stream().mapToDouble(tx -> Utils.getDoubleValue(tx.getInterestPortion())).sum();
    }

    @Test
    void testPrepayLoanShouldNotReceiveAccrualAfterClosure() {
        final Long[] loanIdHolder = new Long[1];

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(12));

        Long loanId = loanIdHolder[0];

        runAt("2026-01-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 January 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);

            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            log.info("Prepay template amount: {}", prepayAmount);
            log.info("Prepay template interest portion: {}", prepayTemplate.getInterestPortion());
            log.info("Prepay template principal portion: {}", prepayTemplate.getPrincipalPortion());

            addRepayment(loanId, repayment(prepayAmount, "15 January 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            log.info("Loan status after prepay: {}", loanAfterPrepay.getStatus().getCode());
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterPrepay.getSummary().getTotalOutstanding()),
                    "Total outstanding should be 0 after prepay");
        });

        runAt("2026-01-16", () -> {
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

            LocalDate prepayDate = LocalDate.of(2026, 1, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = getPostClosureAccruals(loanAfterCOB, prepayDate);

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

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(12));

        Long loanId = loanIdHolder[0];

        runAt("2026-01-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            double totalAccruedInterest = sumInterestPortion(getNonReversedTransactions(loanBeforePrepay, "loanTransactionType.accrual"));
            double totalInterestPaid = sumInterestPortion(getNonReversedTransactions(loanBeforePrepay, "loanTransactionType.repayment"));
            double unpaidAccruedInterest = totalAccruedInterest - totalInterestPaid;
            log.info("Before prepay - totalAccrued: {}, totalPaid: {}, unpaidAccrued: {}", totalAccruedInterest, totalInterestPaid,
                    unpaidAccruedInterest);

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 January 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion().doubleValue() : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest,
                    prepayTemplate.getPrincipalPortion());

            assertTrue(templateInterest >= unpaidAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all unpaid accrued interest (" + unpaidAccruedInterest
                            + "). Shortfall: " + (unpaidAccruedInterest - templateInterest));

            addRepayment(loanId, repayment(prepayAmount, "15 January 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });

        runAt("2026-01-16", () -> {
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

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(1));

        Long loanId = loanIdHolder[0];

        runAt("2026-02-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            log.info("Loan status before prepay: {}", loanBeforePrepay.getStatus().getCode());

            double totalAccruedInterest = sumInterestPortion(getNonReversedTransactions(loanBeforePrepay, "loanTransactionType.accrual"));
            double totalInterestPaid = sumInterestPortion(getNonReversedTransactions(loanBeforePrepay, "loanTransactionType.repayment"));
            double unpaidAccruedInterest = totalAccruedInterest - totalInterestPaid;
            log.info("Before prepay - totalAccrued: {}, totalPaid: {}, unpaidAccrued: {}", totalAccruedInterest, totalInterestPaid,
                    unpaidAccruedInterest);

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion().doubleValue() : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest,
                    prepayTemplate.getPrincipalPortion());

            assertTrue(templateInterest >= unpaidAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all unpaid accrued interest (" + unpaidAccruedInterest
                            + ") including post-maturity period. Shortfall: " + (unpaidAccruedInterest - templateInterest));

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

            LocalDate prepayDate = LocalDate.of(2026, 2, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = getPostClosureAccruals(loanAfterCOB, prepayDate);

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

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(1));

        Long loanId = loanIdHolder[0];

        runAt("2026-02-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            List<GetLoansLoanIdRepaymentPeriod> periodsBeforePrepay = loanBeforePrepay.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null).toList();
            log.info("Schedule periods before prepay: {}", periodsBeforePrepay.size());
            for (GetLoansLoanIdRepaymentPeriod p : periodsBeforePrepay) {
                log.info("  Period {}: fromDate={}, dueDate={}, interestDue={}", p.getPeriod(), p.getFromDate(), p.getDueDate(),
                        p.getInterestDue());
            }

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            log.info("Prepay amount: {}", prepayAmount);
            addRepayment(loanId, repayment(prepayAmount, "15 February 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);

            List<GetLoansLoanIdRepaymentPeriod> periodsAfterPrepay = loanAfterPrepay.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null).toList();
            log.info("Schedule periods after prepay: {}", periodsAfterPrepay.size());
            for (GetLoansLoanIdRepaymentPeriod p : periodsAfterPrepay) {
                log.info("  Period {}: fromDate={}, dueDate={}, interestDue={}, principalDue={}", p.getPeriod(), p.getFromDate(),
                        p.getDueDate(), p.getInterestDue(), p.getPrincipalDue());
            }

            assertTrue(periodsAfterPrepay.size() > 1,
                    "After post-maturity prepay, schedule should have more than 1 period (N+1 installment). Found: "
                            + periodsAfterPrepay.size());

            GetLoansLoanIdRepaymentPeriod n1Period = periodsAfterPrepay.stream().filter(p -> p.getPeriod() == 2).findFirst()
                    .orElse(periodsAfterPrepay.get(periodsAfterPrepay.size() - 1));

            assertNotNull(n1Period.getFromDate(), "N+1 period should have a fromDate");
            assertNotNull(n1Period.getDueDate(), "N+1 period should have a dueDate");
            assertEquals(LocalDate.of(2026, 2, 1), n1Period.getFromDate(),
                    "N+1 period should start from last installment due date (Feb 1)");

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

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(1));

        Long loanId = loanIdHolder[0];

        runAt("2026-02-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanDetails = getLoanDetails(loanId);

            var installment = loanDetails.getRepaymentSchedule().getPeriods().stream()
                    .filter(p -> p.getPeriod() != null && p.getPeriod() == 1).findFirst().orElseThrow();
            double installmentTotalDue = Utils.getDoubleValue(installment.getTotalDueForPeriod());
            log.info("Installment total due: {}", installmentTotalDue);

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            log.info("Prepay amount on Feb 15: {}", prepayAmount);

            assertTrue(prepayAmount > installmentTotalDue,
                    "Prepay amount should include post-maturity interest beyond installment total due. " + "Prepay amount: " + prepayAmount
                            + ", Installment total due: " + installmentTotalDue);

            addRepayment(loanId, repayment(prepayAmount, "15 February 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });
    }

    @Test
    void testPostMaturityPrepayWithNoInstallmentsPaid() {
        final Long[] loanIdHolder = new Long[1];

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(1));

        Long loanId = loanIdHolder[0];

        runAt("2026-02-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdResponse loanBeforePrepay = getLoanDetails(loanId);
            log.info("Loan status before prepay (no installments paid): {}", loanBeforePrepay.getStatus().getCode());

            double totalAccruedInterest = sumInterestPortion(getNonReversedTransactions(loanBeforePrepay, "loanTransactionType.accrual"));
            log.info("Total accrued interest (all on full principal, no payments): {}", totalAccruedInterest);

            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            assertNotNull(prepayTemplate);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            double templateInterest = prepayTemplate.getInterestPortion() != null ? prepayTemplate.getInterestPortion().doubleValue() : 0.0;
            double templatePrincipal = prepayTemplate.getPrincipalPortion() != null ? prepayTemplate.getPrincipalPortion().doubleValue()
                    : 0.0;
            log.info("Prepay template - amount: {}, interest: {}, principal: {}", prepayAmount, templateInterest, templatePrincipal);

            // Prepay template must include full principal + all interest (including post-maturity)
            assertTrue(templateInterest >= totalAccruedInterest,
                    "Prepay template interest (" + templateInterest + ") should cover all accrued interest (" + totalAccruedInterest
                            + ") including post-maturity period on full 100k principal. Shortfall: "
                            + (totalAccruedInterest - templateInterest));

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
            verifyLoanStatus(loanAfterCOB, GetLoansLoanIdStatus::getClosedObligationsMet);
            assertEquals(0.0, Utils.getDoubleValue(loanAfterCOB.getSummary().getTotalOutstanding()),
                    "Total outstanding should remain 0 after COB on closed loan with no prior payments");
            assertEquals(0.0, Utils.getDoubleValue(loanAfterCOB.getSummary().getInterestOutstanding()),
                    "Interest outstanding should be 0 after COB");

            LocalDate prepayDate = LocalDate.of(2026, 2, 15);
            List<GetLoansLoanIdTransactions> postClosureAccruals = getPostClosureAccruals(loanAfterCOB, prepayDate);

            log.info("Post-closure accrual transactions found: {}", postClosureAccruals.size());
            assertTrue(postClosureAccruals.isEmpty(),
                    "No accrual transactions should be posted after closure (all installments unpaid scenario). Found "
                            + postClosureAccruals.size() + " accrual(s)");
        });
    }

    @Test
    void testMultipleCOBCyclesAfterClosureProduceNoAccruals() {
        final Long[] loanIdHolder = new Long[1];

        runAt("2026-01-01", () -> loanIdHolder[0] = createCumulativeLoan(1));

        Long loanId = loanIdHolder[0];

        runAt("2026-02-15", () -> {
            executeInlineCOB(loanId);
            GetLoansLoanIdTransactionsTemplateResponse prepayTemplate = getPrepaymentAmount(loanId, "15 February 2026", DATETIME_PATTERN);
            double prepayAmount = prepayTemplate.getAmount().doubleValue();
            log.info("Prepay amount: {}", prepayAmount);
            addRepayment(loanId, repayment(prepayAmount, "15 February 2026"));

            GetLoansLoanIdResponse loanAfterPrepay = getLoanDetails(loanId);
            verifyLoanStatus(loanAfterPrepay, GetLoansLoanIdStatus::getClosedObligationsMet);
        });

        LocalDate closureDate = LocalDate.of(2026, 2, 15);

        for (LocalDate date = LocalDate.of(2026, 2, 16); !date.isAfter(LocalDate.of(2026, 2, 18)); date = date.plusDays(1)) {
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
                List<GetLoansLoanIdTransactions> postClosureAccruals = getPostClosureAccruals(loanAfterCOB, closureDate);

                log.info("COB on {} - postClosureAccruals: {}, totalOutstanding: {}, interestOutstanding: {}", currentDate,
                        postClosureAccruals.size(), totalOutstanding, interestOutstanding);

                assertTrue(postClosureAccruals.isEmpty(), "No accrual transactions should be posted after closure. On " + currentDate
                        + " found " + postClosureAccruals.size() + " accrual(s)");
            });
        }
    }
}
