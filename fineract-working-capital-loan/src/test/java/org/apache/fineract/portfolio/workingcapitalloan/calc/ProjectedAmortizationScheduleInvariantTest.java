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
package org.apache.fineract.portfolio.workingcapitalloan.calc;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;

import java.math.BigDecimal;
import java.math.MathContext;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Supplier;
import java.util.stream.Stream;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.monetary.data.CurrencyData;
import org.apache.fineract.portfolio.workingcapitalloanproduct.domain.WorkingCapitalAmortizationType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

/**
 * Whatever the borrower pays, and whenever, the schedule must account for every unit of the loan:
 *
 * <ul>
 * <li>the projection ends with all outstanding principal paid and all of the discount fee amortized;</li>
 * <li>the principal actually repaid plus the principal still projected is the principal disbursed;</li>
 * <li>the discount fee actually amortized - earned on the money collected - plus the fee still projected is the
 * discount fee;</li>
 * <li>paying less than the plan by a date lengthens the schedule, paying more shortens it.</li>
 * </ul>
 *
 * <p>
 * Checked across every payment amount strategy and both amortization types, from loans repaid in a day to loans solved
 * just under {@link ProjectedAmortizationScheduleModel#MAX_CALCULABLE_ANNUAL_EIR}, for payments under, over, late and
 * in a lump.
 */
class ProjectedAmortizationScheduleInvariantTest {

    private static final MathContext MC = MathContext.DECIMAL128;
    private static final CurrencyData CURRENCY = new CurrencyData("USD", 2, null);
    private static final LocalDate DISBURSEMENT = LocalDate.of(2019, 1, 1);
    private static final BigDecimal TPV = new BigDecimal("100000");
    private static final BigDecimal RATE = new BigDecimal("18");
    private static final int DAY_COUNT = 360;

    /** {day after disbursement, amount} pairs. The TPV plan bills 50 a day. */
    private static final int[][][] PAYMENT_PATTERNS = { //
            { { 1, 30 } }, // short once
            { { 1, 120 } }, // over once
            { { 1, 30 }, { 3, 70 } }, // short, a day missed, caught up
            { { 5, 400 } }, // nothing for four days, then a lump
            { { 1, 50 }, { 2, 50 }, { 3, 10 } }, // on plan, then short
            { { 2, 333 } }, // large prepayment
            { { 1, 5 }, { 2, 5 }, { 3, 5 } }, // far short, repeatedly
    };

    /**
     * Loans written just under {@link ProjectedAmortizationScheduleModel#MAX_CALCULABLE_ANNUAL_EIR} whose projection
     * the walk re-solves above it after an off-plan payment. Capping that re-solve left their projected payments short
     * of what the borrower owes.
     */
    private static final int[][] RE_SOLVED_ABOVE_THE_CAP = { { 60, 5 }, { 100, 12 }, { 120, 17 }, { 140, 22 } };

    private record Loan(String name, Supplier<ProjectedAmortizationScheduleModel> generator) {

        @Override
        public String toString() {
            return name;
        }
    }

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DISBURSEMENT)));
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    static Stream<Arguments> loansAndPayments() {
        final List<Loan> loans = new ArrayList<>();
        for (final int[] loan : RE_SOLVED_ABOVE_THE_CAP) {
            loans.add(tpv(WorkingCapitalAmortizationType.EIR, loan[0], loan[1]));
        }
        for (final WorkingCapitalAmortizationType type : WorkingCapitalAmortizationType.values()) {
            for (final int net : new int[] { 300, 1000, 3000 }) {
                for (final int fee : new int[] { 5, net / 10, net / 4 }) {
                    loans.add(tpv(type, net, fee));
                    for (final String annualEir : new String[] { "50", "5000", "500000" }) {
                        loans.add(new Loan(type + " annual EIR " + annualEir + "% net " + net + " fee " + fee,
                                () -> ProjectedAmortizationScheduleModel.generateFromAnnualEir(type, BigDecimal.valueOf(fee),
                                        BigDecimal.valueOf(net), new BigDecimal(annualEir), DAY_COUNT, DISBURSEMENT, MC, CURRENCY,
                                        DISBURSEMENT)));
                    }
                    for (final String paymentAmount : new String[] { "50", "17.35" }) {
                        loans.add(new Loan(type + " payment amount " + paymentAmount + " net " + net + " fee " + fee,
                                () -> ProjectedAmortizationScheduleModel.generateFromPaymentAmount(type, BigDecimal.valueOf(fee),
                                        BigDecimal.valueOf(net), new BigDecimal(paymentAmount), DAY_COUNT, DISBURSEMENT, MC, CURRENCY,
                                        DISBURSEMENT)));
                    }
                }
            }
        }
        return loans.stream().filter(ProjectedAmortizationScheduleInvariantTest::isCalculable)
                .flatMap(loan -> Arrays.stream(PAYMENT_PATTERNS).map(pattern -> Arguments.of(loan, pattern)));
    }

    @ParameterizedTest(name = "{0}, paid {1}")
    @MethodSource("loansAndPayments")
    void theScheduleAccountsForEveryUnitOfTheLoan(final Loan loan, final int[][] payments) {
        final ProjectedAmortizationScheduleModel plan = loan.generator().get();
        final List<ProjectedPayment> planRows = plan.projectedPayments();
        final LocalDate planEnd = planRows.getLast().date();
        final BigDecimal net = plan.netDisbursementAmount().getAmount();
        final BigDecimal fee = plan.discountFeeAmount().getAmount();

        final ProjectedAmortizationScheduleModel model = loan.generator().get();
        BigDecimal collected = BigDecimal.ZERO;
        LocalDate lastPaymentDate = DISBURSEMENT;
        for (final int[] payment : payments) {
            lastPaymentDate = DISBURSEMENT.plusDays(payment[0]);
            model.applyPayment(lastPaymentDate, BigDecimal.valueOf(payment[1]));
            collected = collected.add(BigDecimal.valueOf(payment[1]));
        }

        final List<ProjectedPayment> rows = model.projectedPayments();
        ProjectedPayment lastPaidRow = null;
        BigDecimal projectedPayments = BigDecimal.ZERO;
        BigDecimal projectedAmortization = BigDecimal.ZERO;
        for (final ProjectedPayment row : rows.subList(1, rows.size())) {
            if (row.date().isAfter(lastPaymentDate)) {
                projectedPayments = projectedPayments.add(row.expectedPaymentAmount().getAmount());
                projectedAmortization = projectedAmortization.add(row.expectedAmortizationAmount().getAmount());
            } else {
                lastPaidRow = row;
            }
        }
        final BigDecimal actualAmortization = fee.subtract(lastPaidRow.actualDiscountFeeBalance().getAmount());
        final BigDecimal actualPrincipal = collected.min(net.add(fee)).subtract(actualAmortization);
        final BigDecimal projectedPrincipal = projectedPayments.subtract(projectedAmortization);

        if (collected.compareTo(net.add(fee)) < 0) {
            final ProjectedPayment last = rows.getLast();
            assertEquals(0, last.expectedBalance().getAmount().signum(), "the projection ends with the principal repaid");
            assertEquals(0, last.expectedDiscountFeeBalance().getAmount().signum(), "the projection ends with the fee amortized");
        }
        assertEquals(0, actualPrincipal.add(projectedPrincipal).compareTo(net),
                "principal repaid " + actualPrincipal + " plus projected " + projectedPrincipal + " is the principal " + net);
        assertEquals(0, actualAmortization.add(projectedAmortization).compareTo(fee),
                "fee amortized " + actualAmortization + " plus projected " + projectedAmortization + " is the fee " + fee);

        // A payment after the plan's last day is late whatever its size, so only payments within the plan compare.
        if (!lastPaymentDate.isAfter(planEnd)) {
            final LocalDate paidBy = lastPaymentDate;
            final BigDecimal planBilledByThen = planRows.subList(1, planRows.size()).stream().filter(row -> !row.date().isAfter(paidBy))
                    .map(row -> row.expectedPaymentAmount().getAmount()).reduce(BigDecimal.ZERO, BigDecimal::add);
            final LocalDate end = rows.getLast().date();
            final int againstPlan = collected.compareTo(planBilledByThen);
            if (againstPlan < 0) {
                assertFalse(end.isBefore(planEnd), "paid less than the plan, so the schedule must not end before " + planEnd);
            } else if (againstPlan > 0) {
                assertFalse(end.isAfter(planEnd), "paid more than the plan, so the schedule must not end after " + planEnd);
            }
        }
    }

    private static Loan tpv(final WorkingCapitalAmortizationType type, final int net, final int fee) {
        return new Loan(type + " TPV net " + net + " fee " + fee, () -> ProjectedAmortizationScheduleModel.generate(type,
                BigDecimal.valueOf(fee), BigDecimal.valueOf(net), TPV, RATE, DAY_COUNT, DISBURSEMENT, MC, CURRENCY, DISBURSEMENT));
    }

    /** A loan that is not calculable is refused when it is created, so there is no schedule to hold to account. */
    private static boolean isCalculable(final Loan loan) {
        try {
            ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "UTC", null));
            ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
            ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, DISBURSEMENT)));
            loan.generator().get();
            return true;
        } catch (final IllegalArgumentException | IllegalStateException | ArithmeticException e) {
            return false;
        } finally {
            ThreadLocalContextUtil.reset();
        }
    }
}
