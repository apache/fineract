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
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Objects;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.function.UnaryOperator;
import org.apache.fineract.client.models.GetLoanProductsProductIdResponse;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostHolidaysRequest;
import org.apache.fineract.client.models.PostHolidaysRequestOffices;
import org.apache.fineract.client.models.PostLoanProductsRequest;
import org.apache.fineract.client.models.PostLoansRequest;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.client.models.PutLoanProductsProductIdRequest;
import org.apache.fineract.client.models.PutLoansLoanIdRequest;
import org.apache.fineract.client.models.StringEnumOptionData;
import org.apache.fineract.client.models.WorkingDaysData;
import org.apache.fineract.client.models.WorkingDaysUpdateRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignGroupHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignRawHttpHelper;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInMonthType;
import org.apache.fineract.integrationtests.client.feign.modules.LoanTestData.DaysInYearType;
import org.apache.fineract.integrationtests.common.CalendarHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.WorkingDaysHelper;
import org.junit.jupiter.api.Test;

/**
 * A monthly due date whose anchor day (the disbursement's day of month) is missing from the target month - 30 February,
 * 31 April - is clamped back to the last day of the month by default, or rolled forward to the first day of the next
 * month when the product says so. Either way the anchor day comes back as soon as a month has it.
 */
public class MonthEndDueDateStrategyTest extends FeignLoanTestBase {

    private static final String LAST_DAY_OF_MONTH = "LAST_DAY_OF_MONTH";
    private static final String FIRST_DAY_OF_NEXT_MONTH = "FIRST_DAY_OF_NEXT_MONTH";

    private static final Integer DISBURSEMENT_DATE = 1;
    private static final Integer SUBMITTED_ON_DATE = 2;
    private static final Integer MOVE_TO_NEXT_WORKING_DAY = 2;
    private static final Integer RESCHEDULE_TO_NEXT_REPAYMENT_DATE = 1;
    private static final Integer RESCHEDULE_TO_SPECIFIED_DATE = 2;

    private static final double PRINCIPAL = 10000.0;
    private static final double INTEREST_RATE = 12.0;

    private final FeignGroupHelper groupHelper = new FeignGroupHelper(FineractFeignClientHelper.getFineractFeignClient());

    @Test
    public void testProductExposesAndUpdatesTheStrategy() {
        final Long unsetProductId = createLoanProduct(create4IProgressive());
        assertNull(retrieveLoanProduct(unsetProductId).getMonthEndDueDateStrategy(), "An unset strategy reads back as unset");

        for (final PostLoanProductsRequest template : List.of(create4IProgressive(), create4ICumulative())) {
            final Long productId = createLoanProduct(
                    template.monthEndDueDateStrategy(PostLoanProductsRequest.MonthEndDueDateStrategyEnum.FIRST_DAY_OF_NEXT_MONTH));
            assertEquals(FIRST_DAY_OF_NEXT_MONTH, strategyOf(retrieveLoanProduct(productId)), template.getLoanScheduleType());

            updateLoanProduct(productId, new PutLoanProductsProductIdRequest()
                    .monthEndDueDateStrategy(PutLoanProductsProductIdRequest.MonthEndDueDateStrategyEnum.LAST_DAY_OF_MONTH));
            assertEquals(LAST_DAY_OF_MONTH, strategyOf(retrieveLoanProduct(productId)), template.getLoanScheduleType());
        }

        final List<StringEnumOptionData> options = getLoanProductTemplate(false).getMonthEndDueDateStrategyOptions();
        assertNotNull(options, "The template offers the strategies");
        assertEquals(List.of(LAST_DAY_OF_MONTH, FIRST_DAY_OF_NEXT_MONTH), options.stream().map(StringEnumOptionData::getId).toList());
    }

    @Test
    public void testProductStrategyCanBeClearedAndRejectsUnknownValues() {
        final Long productId = createLoanProduct(
                create4IProgressive().monthEndDueDateStrategy(PostLoanProductsRequest.MonthEndDueDateStrategyEnum.FIRST_DAY_OF_NEXT_MONTH));

        final RuntimeException exception = assertThrows(RuntimeException.class,
                () -> FeignRawHttpHelper.put("/loanproducts/" + productId, """
                        {"monthEndDueDateStrategy": "SOMEWHERE_IN_BETWEEN", "locale": "en"}
                        """));
        assertTrue(exception.getMessage().startsWith("HTTP 400"), exception.getMessage());
        assertEquals(FIRST_DAY_OF_NEXT_MONTH, strategyOf(retrieveLoanProduct(productId)), "A rejected update changes nothing");

        FeignRawHttpHelper.put("/loanproducts/" + productId, """
                {"monthEndDueDateStrategy": null, "locale": "en"}
                """);
        assertNull(retrieveLoanProduct(productId).getMonthEndDueDateStrategy(), "Sending null clears the strategy");
    }

    @Test
    public void testLoanInheritsTheProductStrategy() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long progressiveLoanId = progressiveLoan(clientId, product(create4IProgressive(), FIRST_DAY_OF_NEXT_MONTH),
                    "31 January 2025", 3);
            final Long cumulativeLoanId = cumulativeLoan(clientId, product(create4ICumulative(), FIRST_DAY_OF_NEXT_MONTH),
                    "31 January 2025", 3);
            final Long unsetLoanId = progressiveLoan(clientId, product(create4IProgressive(), null), "31 January 2025", 3);

            assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(progressiveLoanId).getMonthEndDueDateStrategy().getId());
            assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(cumulativeLoanId).getMonthEndDueDateStrategy().getId());
            assertNull(getLoanDetails(unsetLoanId).getMonthEndDueDateStrategy());
        });
    }

    /**
     * The ticket's worked example: disbursed 31 January 2025, three monthly periods, Actual/Actual. February has no
     * 31st and neither has April; March does. Interest is 12% a year on the declining balance over the actual days of
     * each period - 29, 30 and 31 days when rolled against 28, 31 and 30 when clamped - so the rolled schedule bills
     * 95.34 = 10,000.00 * 12% * 29/365 in its first period against 92.05 = 10,000.00 * 12% * 28/365, and pays a
     * slightly higher EMI because its money is out for a day longer before the first repayment.
     */
    @Test
    public void testWorkedExampleOnProgressiveLoan() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final ScheduleType progressive = progressive(MonthEndDueDateStrategyTest::actualActual);
            verifyRepaymentSchedule(disbursedLoan(clientId, progressive, LAST_DAY_OF_MONTH, "31 January 2025", 3), //
                    installment(PRINCIPAL, null, "31 January 2025"), //
                    installment(3305.77, 92.05, 3397.82, false, "28 February 2025"), //
                    installment(3329.59, 68.23, 3397.82, false, "31 March 2025"), //
                    installment(3364.64, 33.19, 3397.83, false, "30 April 2025"));
            verifyRepaymentSchedule(disbursedLoan(clientId, progressive, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 3), //
                    installment(PRINCIPAL, null, "31 January 2025"), //
                    installment(3303.22, 95.34, 3398.56, false, "01 March 2025"), //
                    installment(3332.51, 66.05, 3398.56, false, "31 March 2025"), //
                    installment(3364.27, 34.29, 3398.56, false, "01 May 2025"));
        });
    }

    /**
     * The worked example on a cumulative product with no meeting calendar lands on the same dates. The cumulative EMI
     * is worked out from the nominal monthly rate, so it does not move; the interest split does, following the actual
     * days of each period, and the last instalment absorbs the difference.
     */
    @Test
    public void testWorkedExampleOnCumulativeLoan() {
        withInterestChargedFromDisbursementDate(() -> {
            final Long clientId = createClient();
            runAt("31 January 2025", () -> {
                final ScheduleType cumulative = cumulative(MonthEndDueDateStrategyTest::actualActual);
                verifyRepaymentSchedule(disbursedLoan(clientId, cumulative, LAST_DAY_OF_MONTH, "31 January 2025", 3), //
                        installment(PRINCIPAL, null, "31 January 2025"), //
                        installment(3307.25, 92.05, 3399.30, false, "28 February 2025"), //
                        installment(3331.09, 68.21, 3399.30, false, "31 March 2025"), //
                        installment(3361.66, 33.16, 3394.82, false, "30 April 2025"));
                verifyRepaymentSchedule(disbursedLoan(clientId, cumulative, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 3), //
                        installment(PRINCIPAL, null, "31 January 2025"), //
                        installment(3303.96, 95.34, 3399.30, false, "01 March 2025"), //
                        installment(3333.26, 66.04, 3399.30, false, "31 March 2025"), //
                        installment(3362.78, 34.27, 3397.05, false, "01 May 2025"));
            });
        });
    }

    /**
     * Under Actual/Actual interest follows the actual days. Disbursed 30 January 2025: the rolled first period (30
     * January - 1 March, 30 days) is a day longer than the clamped one (30 January - 28 February, 29 days), and the
     * second (1 March - 30 March, 29 days) a day shorter than its clamped counterpart (28 February - 30 March, 30
     * days). So the rolled schedule bills 98.63 = 10,000.00 * 12% * 30/365 in its first period against 95.34 =
     * 10,000.00 * 12% * 29/365, and its second period on 29 days of the remaining balance instead of 30.
     */
    @Test
    public void testRolledDueDateMovesInterestBetweenPeriodsUnderActualActual() {
        withInterestChargedFromDisbursementDate(() -> {
            final Long clientId = createClient();
            runAt("30 January 2025", () -> {
                final ScheduleType progressive = progressive(MonthEndDueDateStrategyTest::actualActual);
                verifyRepaymentSchedule(disbursedLoan(clientId, progressive, LAST_DAY_OF_MONTH, "30 January 2025", 3), //
                        installment(PRINCIPAL, null, "30 January 2025"), //
                        installment(3303.22, 95.34, 3398.56, false, "28 February 2025"), //
                        installment(3332.51, 66.05, 3398.56, false, "30 March 2025"), //
                        installment(3364.27, 34.29, 3398.56, false, "30 April 2025"));
                verifyRepaymentSchedule(disbursedLoan(clientId, progressive, FIRST_DAY_OF_NEXT_MONTH, "30 January 2025", 3), //
                        installment(PRINCIPAL, null, "30 January 2025"), //
                        installment(3300.30, 98.63, 3398.93, false, "01 March 2025"), //
                        installment(3335.05, 63.88, 3398.93, false, "30 March 2025"), //
                        installment(3364.65, 34.29, 3398.94, false, "30 April 2025"));

                final ScheduleType cumulative = cumulative(MonthEndDueDateStrategyTest::actualActual);
                verifyRepaymentSchedule(disbursedLoan(clientId, cumulative, LAST_DAY_OF_MONTH, "30 January 2025", 3), //
                        installment(PRINCIPAL, null, "30 January 2025"), //
                        installment(3303.96, 95.34, 3399.30, false, "28 February 2025"), //
                        installment(3333.26, 66.04, 3399.30, false, "30 March 2025"), //
                        installment(3362.78, 34.27, 3397.05, false, "30 April 2025"));
                verifyRepaymentSchedule(disbursedLoan(clientId, cumulative, FIRST_DAY_OF_NEXT_MONTH, "30 January 2025", 3), //
                        installment(PRINCIPAL, null, "30 January 2025"), //
                        installment(3300.67, 98.63, 3399.30, false, "01 March 2025"), //
                        installment(3335.43, 63.87, 3399.30, false, "30 March 2025"), //
                        installment(3363.90, 34.28, 3398.18, false, "30 April 2025"));
            });
        });
    }

    /**
     * Under 30/360 every whole month is a thirtieth of the year however many days it has, so rolling the due date moves
     * no interest between periods.
     */
    @Test
    public void testRolledDueDateKeepsInterestPerPeriodUnder30360() {
        withInterestChargedFromDisbursementDate(() -> {
            final Long clientId = createClient();
            runAt("30 January 2025", () -> {
                for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                    final Long clampedLoanId = disbursedLoan(clientId, type, LAST_DAY_OF_MONTH, "30 January 2025", 3);
                    final Long rolledLoanId = disbursedLoan(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "30 January 2025", 3);

                    assertNotEquals(dueDates(clampedLoanId), dueDates(rolledLoanId), type.name());
                    assertEquals(interests(clampedLoanId), interests(rolledLoanId), type.name());
                    assertEquals(principals(clampedLoanId), principals(rolledLoanId), type.name());
                }
            });
        });
    }

    /**
     * A leap-day anchor is on the 29th for eleven months and only rolls in February 2025, which has no 29th.
     */
    @Test
    public void testLeapDayAnchorRollsInCommonYearOnly() {
        final Long clientId = createClient();
        runAt("29 February 2024", () -> {
            for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                final List<LocalDate> dueDates = dueDates(disbursedLoan(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "29 February 2024", 12));

                assertEquals(12, dueDates.size());
                assertEquals(LocalDate.of(2024, 3, 29), dueDates.getFirst(), type.name());
                assertEquals(LocalDate.of(2025, 1, 29), dueDates.get(10), type.name());
                assertTrue(dueDates.subList(0, 11).stream().allMatch(dueDate -> dueDate.getDayOfMonth() == 29),
                        type.name() + ": " + dueDates);
                assertEquals(LocalDate.of(2025, 3, 1), dueDates.getLast(), type.name());
            }
        });
    }

    @Test
    public void testUnsetStrategyBehavesAsLastDayOfMonth() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(MonthEndDueDateStrategyTest::actualActual)) {
                final Long unsetLoanId = disbursedLoan(clientId, type, null, "31 January 2025", 4);
                final Long clampedLoanId = disbursedLoan(clientId, type, LAST_DAY_OF_MONTH, "31 January 2025", 4);

                assertEquals(dueDates(clampedLoanId), dueDates(unsetLoanId), type.name());
                assertEquals(interests(clampedLoanId), interests(unsetLoanId), type.name());
                assertEquals(principals(clampedLoanId), principals(unsetLoanId), type.name());
            }
        });
    }

    /**
     * A loan keeps the strategy it was submitted with: switching the product afterwards does not move its due dates,
     * not even when a backdated repayment regenerates its schedule.
     */
    @Test
    public void testChangingTheProductDoesNotMoveExistingLoans() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long productId = product(create4IProgressive(), LAST_DAY_OF_MONTH);
            final Long loanId = progressiveLoan(clientId, productId, "31 January 2025", 3);
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "31 January 2025");
            final List<LocalDate> dueDatesBefore = dueDates(loanId);

            updateLoanProduct(productId, new PutLoanProductsProductIdRequest()
                    .monthEndDueDateStrategy(PutLoanProductsProductIdRequest.MonthEndDueDateStrategyEnum.FIRST_DAY_OF_NEXT_MONTH));

            updateBusinessDate("10 March 2025");
            addRepayment(loanId, repayment(100.0, "05 March 2025"));

            assertEquals(LAST_DAY_OF_MONTH, getLoanDetails(loanId).getMonthEndDueDateStrategy().getId());
            assertEquals(dueDatesBefore, dueDates(loanId));
        });
    }

    /**
     * Regenerating the schedule - here through a backdated repayment - uses the strategy captured on the loan and lands
     * on the same rolled dates.
     */
    @Test
    public void testRegeneratedScheduleKeepsRolledDates() {
        final Long clientId = createClient();
        final List<LocalDate> rolled = List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 31));
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                final Long loanId = disbursedLoan(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 4);
                assertEquals(rolled, dueDates(loanId), type.name());

                updateBusinessDate("10 March 2025");
                addRepayment(loanId, repayment(100.0, "15 February 2025"));
                assertEquals(rolled, dueDates(loanId), type.name() + ": a backdated repayment regenerates the same dates");
                updateBusinessDate("31 January 2025");
            }
        });
    }

    /**
     * A reschedule moving a due date is a loan term variation, and on a cumulative loan it takes precedence over the
     * generated date exactly as it does for a clamped loan: the periods before it keep their dates, the rest follow the
     * new date.
     */
    @Test
    public void testDueDateVariationTakesPrecedenceOnCumulativeLoan() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long clampedLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), LAST_DAY_OF_MONTH, "31 January 2025",
                    4);
            final Long rolledLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                    "31 January 2025", 4);

            updateBusinessDate("10 March 2025");
            createAndApproveReschedule(clampedLoanId, "10 March 2025", "31 March 2025", "04 April 2025");
            createAndApproveReschedule(rolledLoanId, "10 March 2025", "31 March 2025", "04 April 2025");

            final List<LocalDate> clamped = dueDates(clampedLoanId);
            final List<LocalDate> rolled = dueDates(rolledLoanId);
            assertEquals(LocalDate.of(2025, 2, 28), clamped.getFirst());
            assertEquals(LocalDate.of(2025, 3, 1), rolled.getFirst(), "The period before the variation keeps its rolled date");
            assertEquals(LocalDate.of(2025, 4, 4), rolled.get(1), "The variation governs its own date");
            assertEquals(clamped.subList(1, clamped.size()), rolled.subList(1, rolled.size()),
                    "From the variation on both loans follow the new date");
        });
    }

    /**
     * With a fixed length the maturity date is computed separately from the generated periods and overwrites the last
     * one, so the two have to agree on the roll.
     */
    @Test
    public void testFixedLengthMaturityRollsLikeThePeriods() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            // fixed length is only allowed on interest free products
            final Long loanId = disbursedLoan(clientId, progressive(
                    template -> template.interestRatePerPeriod(0.0).minInterestRatePerPeriod(0.0).numberOfRepayments(3).fixedLength(3)),
                    FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 3, 0.0);
            assertEquals(List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1)), dueDates(loanId));
        });
    }

    /**
     * A group loan following a meeting calendar is due on the meeting dates, which the strategy does not move: not the
     * dates of a cumulative or a progressive loan, not the 30/360 interest of their periods, and not the maturity date
     * of a fixed-length loan either - when the schedule is generated, nor when transactions reprocess it. The loan
     * still reads back the strategy it inherited from its product.
     */
    @Test
    public void testMeetingCalendarDecidesDueDatesOfCalendarLoans() {
        final Long clientId = createClient();
        final Long groupId = groupHelper.createActiveGroup().getGroupId();
        groupHelper.associateClient(groupId, clientId);
        final Long calendarId = CalendarHelper.createMeetingForGroup(groupId, "31 January 2025", "3", "1", null).getResourceId();
        final List<CalendarScenario> scenarios = List.of(//
                new CalendarScenario(cumulative(UnaryOperator.identity()), INTEREST_RATE), //
                new CalendarScenario(progressive(UnaryOperator.identity()), INTEREST_RATE), //
                // fixed length is only allowed on interest free products
                new CalendarScenario(progressive(
                        template -> template.interestRatePerPeriod(0.0).minInterestRatePerPeriod(0.0).numberOfRepayments(3).fixedLength(3)),
                        0.0));

        runAt("31 January 2025", () -> {
            for (final CalendarScenario scenario : scenarios) {
                final String name = scenario.type().name() + (scenario.interestRate() == 0.0 ? " fixed length" : "");
                final Long clampedLoanId = calendarLoan(clientId, groupId, calendarId, scenario, LAST_DAY_OF_MONTH);
                final Long rolledLoanId = calendarLoan(clientId, groupId, calendarId, scenario, FIRST_DAY_OF_NEXT_MONTH);

                assertEquals(dueDates(clampedLoanId), dueDates(rolledLoanId), name);
                assertEquals(interests(clampedLoanId), interests(rolledLoanId), name);
                assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(rolledLoanId).getMonthEndDueDateStrategy().getId(), name);

                // disbursing and repaying reprocess the loan from what it stored, which must not bring the strategy
                // back
                for (final Long loanId : List.of(clampedLoanId, rolledLoanId)) {
                    disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "31 January 2025");
                    addRepayment(loanId, repayment(100.0, "31 January 2025"));
                }
                assertEquals(dueDates(clampedLoanId), dueDates(rolledLoanId), name + " after a repayment");
                assertEquals(interests(clampedLoanId), interests(rolledLoanId), name + " after a repayment");
            }
        });
    }

    private record CalendarScenario(ScheduleType type, double interestRate) {
    }

    private Long calendarLoan(final Long clientId, final Long groupId, final Long calendarId, final CalendarScenario scenario,
            final String monthEndDueDateStrategy) {
        final Long productId = product(scenario.type().template().get(), monthEndDueDateStrategy);
        final PostLoansRequest request = scenario.type().cumulative()
                ? LoanRequestBuilders.applyCumulativeLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL, scenario.interestRate(),
                        3, null)
                : LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL,
                        scenario.interestRate(), 3, null);
        final Long loanId = applyForLoan(request.groupId(groupId).loanType("jlg").calendarId(calendarId).syncDisbursementWithMeeting(false)
                // a calendar meeting on the 31st has no February meeting to start on
                .repaymentsStartingFromDate("31 March 2025"));
        approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "31 January 2025"));
        return loanId;
    }

    /**
     * Loans whose schedule does not start on the expected disbursement date. A loan disbursed later than expected is
     * re-seeded from the actual disbursement date (1 March, so nothing is ever missing and both strategies agree), and
     * repayments counted from the submitted-on date are chained from that date (31 January, the usual 31st anchor, so
     * exactly the dates the default clamps roll forward).
     */
    @Test
    public void testScheduleStartingOnFirstOfMonthOnlyRollsMissingDays() {
        final Long clientId = createClient();
        final List<LocalDate> fromDisbursement = List.of(LocalDate.of(2025, 4, 1), //
                LocalDate.of(2025, 5, 1), //
                LocalDate.of(2025, 6, 1), //
                LocalDate.of(2025, 7, 1));
        final List<LocalDate> clampedFromSubmission = List.of(LocalDate.of(2025, 2, 28), //
                LocalDate.of(2025, 3, 31), //
                LocalDate.of(2025, 4, 30), //
                LocalDate.of(2025, 5, 31));
        final List<LocalDate> rolledFromSubmission = List.of(LocalDate.of(2025, 3, 1), //
                LocalDate.of(2025, 3, 31), //
                LocalDate.of(2025, 5, 1), //
                LocalDate.of(2025, 5, 31));
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(template -> template.repaymentStartDateType(DISBURSEMENT_DATE))) {
                assertEquals(fromDisbursement,
                        dueDates(loanDisbursedLate(clientId, type, LAST_DAY_OF_MONTH, "31 January 2025", "01 March 2025", 4)),
                        type.name() + " counted from the disbursement date, clamped");
                assertEquals(fromDisbursement,
                        dueDates(loanDisbursedLate(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", "01 March 2025", 4)),
                        type.name() + " counted from the disbursement date, rolled");
            }
            for (final ScheduleType type : scheduleTypes(template -> template.repaymentStartDateType(SUBMITTED_ON_DATE))) {
                assertEquals(clampedFromSubmission,
                        dueDates(loanDisbursedLate(clientId, type, LAST_DAY_OF_MONTH, "31 January 2025", "01 March 2025", 4)),
                        type.name() + " counted from the submitted-on date, clamped");
                assertEquals(rolledFromSubmission,
                        dueDates(loanDisbursedLate(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", "01 March 2025", 4)),
                        type.name() + " counted from the submitted-on date, rolled");
            }
        });
    }

    /**
     * A due date moved to a 1st by a reschedule is the date the user chose, not a rolled one. The default stays on the
     * 1st from there on, since it only snaps dates from the 28th on back to the anchor day, and so must rolling forward
     * - also past the next short month, where a 1st would otherwise be mistaken for a rolled date.
     */
    @Test
    public void testDueDateVariationOntoFirstOfMonthIsFollowedLikeAnyOtherDate() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long clampedLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), LAST_DAY_OF_MONTH, "31 January 2025",
                    6);
            final Long rolledLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                    "31 January 2025", 6);

            updateBusinessDate("10 March 2025");
            createAndApproveReschedule(clampedLoanId, "10 March 2025", "31 March 2025", "01 May 2025");
            createAndApproveReschedule(rolledLoanId, "10 March 2025", "31 March 2025", "01 May 2025");

            assertEquals(List.of(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 7, 1),
                    LocalDate.of(2025, 8, 1), LocalDate.of(2025, 9, 1)), dueDates(clampedLoanId));
            assertEquals(
                    List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 6, 1), LocalDate.of(2025, 7, 1),
                            LocalDate.of(2025, 8, 1), LocalDate.of(2025, 9, 1)),
                    dueDates(rolledLoanId), "Only the period before the variation keeps its rolled date");
        });
    }

    /**
     * Working-day adjustment runs after the strategy has placed the date: 1 March 2025, the rolled first due date of
     * the worked example, is a Saturday, so with weekends off it moves to Monday 3 March.
     */
    @Test
    public void testRolledDateIsMovedOffNonWorkingDay() {
        final Long clientId = createClient();
        final WorkingDaysData originalWorkingDays = WorkingDaysHelper.getAllWorkingDays();
        try {
            updateWorkingDays("FREQ=WEEKLY;INTERVAL=1;BYDAY=MO,TU,WE,TH,FR", MOVE_TO_NEXT_WORKING_DAY, false);
            runAt("31 January 2025", () -> {
                assertEquals(List.of(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 30)),
                        dueDates(disbursedLoan(clientId, cumulative(UnaryOperator.identity()), LAST_DAY_OF_MONTH, "31 January 2025", 3)));
                assertEquals(List.of(LocalDate.of(2025, 3, 3), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1)), dueDates(
                        disbursedLoan(clientId, cumulative(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 3)));
            });
        } finally {
            updateWorkingDays(originalWorkingDays.getRecurrence(),
                    Math.toIntExact(originalWorkingDays.getRepaymentRescheduleType().getId()),
                    originalWorkingDays.getExtendTermForDailyRepayments());
        }
    }

    /**
     * Applying a holiday moves only the due dates inside it. The rolled dates around it are left alone rather than
     * being clamped back to the end of the month they stand for. Dates are in 2033 so no other test's loan falls into
     * the holiday, which applies to every loan of the office.
     */
    @Test
    public void testApplyingHolidayKeepsRolledDates() {
        final Long clientId = createClient();
        try {
            runAt("31 January 2033", () -> {
                final Long clampedLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), LAST_DAY_OF_MONTH,
                        "31 January 2033", 4);
                final Long rolledLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                        "31 January 2033", 4);
                assertEquals(
                        List.of(LocalDate.of(2033, 2, 28), LocalDate.of(2033, 3, 31), LocalDate.of(2033, 4, 30), LocalDate.of(2033, 5, 31)),
                        dueDates(clampedLoanId));
                assertEquals(
                        List.of(LocalDate.of(2033, 3, 1), LocalDate.of(2033, 3, 31), LocalDate.of(2033, 5, 1), LocalDate.of(2033, 5, 31)),
                        dueDates(rolledLoanId));

                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                        new PutGlobalConfigurationsRequest().enabled(true));
                HolidayHelper.activateHolidays(holiday(LocalDate.of(2033, 3, 31), LocalDate.of(2033, 4, 4)));
                schedulerHelper.executeAndAwaitJob("Apply Holidays To Loans");

                assertEquals(
                        List.of(LocalDate.of(2033, 2, 28), LocalDate.of(2033, 4, 4), LocalDate.of(2033, 4, 30), LocalDate.of(2033, 5, 31)),
                        dueDates(clampedLoanId));
                assertEquals(
                        List.of(LocalDate.of(2033, 3, 1), LocalDate.of(2033, 4, 4), LocalDate.of(2033, 5, 1), LocalDate.of(2033, 5, 31)),
                        dueDates(rolledLoanId));
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    /**
     * A holiday that reschedules its repayments to the next repayment date moves every installment from the holiday on
     * one slot forward, continuing the chain from the loan's own due dates. The rolled 1 May and 1 July stand for the
     * end of April and of June, so the rolled loan keeps its anchor exactly as the clamped one does. Dates are in 2034
     * so no other test's loan falls into the holiday, which applies to every loan of the office.
     */
    @Test
    public void testHolidayRescheduledToNextRepaymentDateKeepsRolledDates() {
        final Long clientId = createClient();
        try {
            runAt("31 January 2034", () -> {
                final Long clampedLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), LAST_DAY_OF_MONTH,
                        "31 January 2034", 4);
                final Long rolledLoanId = disbursedLoan(clientId, cumulative(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                        "31 January 2034", 4);

                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                        new PutGlobalConfigurationsRequest().enabled(true));
                HolidayHelper.activateHolidays(holiday(new PostHolidaysRequest().fromDate(LocalDate.of(2034, 3, 31))
                        .toDate(LocalDate.of(2034, 3, 31)).reschedulingType(RESCHEDULE_TO_NEXT_REPAYMENT_DATE)));
                schedulerHelper.executeAndAwaitJob("Apply Holidays To Loans");

                assertEquals(
                        List.of(LocalDate.of(2034, 2, 28), LocalDate.of(2034, 4, 30), LocalDate.of(2034, 5, 31), LocalDate.of(2034, 6, 30)),
                        dueDates(clampedLoanId));
                assertEquals(
                        List.of(LocalDate.of(2034, 3, 1), LocalDate.of(2034, 5, 1), LocalDate.of(2034, 5, 31), LocalDate.of(2034, 7, 1)),
                        dueDates(rolledLoanId));
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    /**
     * Re-aged installments are due monthly from the re-age start date, and a start date on the 31st rolls the same way
     * as a disbursement on the 31st - whichever way the re-age spreads the outstanding amounts: recalculating interest,
     * amortizing it equally, or on an interest-free loan.
     */
    @Test
    public void testReAgedInstallmentsRollForward() {
        final Long clientId = createClient();
        final List<LocalDate> rolled = List.of(LocalDate.of(2025, 1, 31), LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31));
        final List<ReAgeScenario> scenarios = List.of(//
                new ReAgeScenario("recalculated interest", INTEREST_RATE, null), //
                new ReAgeScenario("equally amortized interest", INTEREST_RATE, "EQUAL_AMORTIZATION_PAYABLE_INTEREST"), //
                new ReAgeScenario("interest free", 0.0, null));
        runAt("15 December 2024", () -> {
            for (final ReAgeScenario scenario : scenarios) {
                updateBusinessDate("15 December 2024");
                final Long loanId = disbursedLoan(clientId,
                        progressive(template -> template.minInterestRatePerPeriod(0.0).interestRatePerPeriod(scenario.interestRate())),
                        FIRST_DAY_OF_NEXT_MONTH, "15 December 2024", 3, scenario.interestRate());

                updateBusinessDate("20 January 2025");
                reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, "31 January 2025", 3, scenario.interestHandling());

                final List<LocalDate> dueDates = dueDates(loanId);
                assertEquals(rolled, dueDates.stream().filter(dueDate -> dueDate.isAfter(LocalDate.of(2025, 1, 20))).toList(),
                        scenario.name() + ": " + dueDates);
            }
        });
    }

    /**
     * Moving the due date of a re-aged installment on an interest-free loan shifts the installments after it by whole
     * months from the new date, and a new date on the 31st rolls the next one forward past April.
     */
    @Test
    public void testDueDateChangeAfterReAgeRollsForward() {
        final Long clientId = createClient();
        runAt("15 December 2024", () -> {
            final Long loanId = disbursedLoan(clientId,
                    progressive(template -> template.minInterestRatePerPeriod(0.0).interestRatePerPeriod(0.0)), FIRST_DAY_OF_NEXT_MONTH,
                    "15 December 2024", 3, 0.0);

            updateBusinessDate("20 January 2025");
            reAgeLoan(loanId, LoanTestData.RepaymentFrequencyType.MONTHS_STRING, 1, "15 February 2025", 3, null);
            createAndApproveReschedule(loanId, "20 January 2025", "15 March 2025", "31 March 2025");

            final List<LocalDate> dueDates = dueDates(loanId);
            assertEquals(List.of(LocalDate.of(2025, 2, 15), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1)),
                    dueDates.stream().filter(dueDate -> dueDate.isAfter(LocalDate.of(2025, 1, 20))).toList(), dueDates.toString());
        });
    }

    private record ReAgeScenario(String name, double interestRate, String interestHandling) {
    }

    /**
     * Rescheduling a progressive loan regenerates its schedule from the loan's own strategy: a new interest rate keeps
     * the rolled dates, and extra terms continue on the same anchor.
     */
    @Test
    public void testRescheduledProgressiveLoanKeepsRolledDates() {
        final Long clientId = createClient();
        final List<LocalDate> rolled = List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1),
                LocalDate.of(2025, 5, 31));
        runAt("31 January 2025", () -> {
            final Long newRateLoanId = disbursedLoan(clientId, progressive(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                    "31 January 2025", 4);
            final Long extraTermsLoanId = disbursedLoan(clientId, progressive(UnaryOperator.identity()), FIRST_DAY_OF_NEXT_MONTH,
                    "31 January 2025", 4);

            updateBusinessDate("10 March 2025");
            loanHelper.createAndApproveRescheduleRequest(LoanRequestBuilders
                    .rescheduleRequest(newRateLoanId, "10 March 2025", "31 March 2025", null).newInterestRate(BigDecimal.valueOf(6)),
                    LoanRequestBuilders.approveReschedule("10 March 2025"));
            assertEquals(rolled, dueDates(newRateLoanId), "A new interest rate keeps the dates");

            loanHelper.createAndApproveRescheduleRequest(
                    LoanRequestBuilders.rescheduleWithExtraTerms(extraTermsLoanId, "10 March 2025", "31 March 2025", 2),
                    LoanRequestBuilders.approveReschedule("10 March 2025"));
            assertEquals(
                    List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1), LocalDate.of(2025, 5, 31),
                            LocalDate.of(2025, 7, 1), LocalDate.of(2025, 7, 31)),
                    dueDates(extraTermsLoanId), "Extra terms keep the anchor");
        });
    }

    private Long loanDisbursedLate(final Long clientId, final ScheduleType type, final String monthEndDueDateStrategy,
            final String submittedOnDate, final String disbursementDate, final int numberOfRepayments) {
        final Long productId = product(type.template().get(), monthEndDueDateStrategy);
        final Long loanId = type.cumulative()
                ? applyAndApproveCumulativeLoan(clientId, productId, submittedOnDate, PRINCIPAL, INTEREST_RATE, numberOfRepayments, null)
                : applyAndApproveProgressiveLoan(clientId, productId, submittedOnDate, PRINCIPAL, INTEREST_RATE, numberOfRepayments, null);
        updateBusinessDate(disbursementDate);
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), disbursementDate);
        updateBusinessDate(submittedOnDate);
        return loanId;
    }

    private Long holiday(final LocalDate date, final LocalDate repaymentsRescheduledTo) {
        return holiday(new PostHolidaysRequest().fromDate(date).toDate(date).repaymentsRescheduledTo(repaymentsRescheduledTo)
                .reschedulingType(RESCHEDULE_TO_SPECIFIED_DATE));
    }

    private Long holiday(final PostHolidaysRequest request) {
        return ok(
                () -> FineractFeignClientHelper.getFineractFeignClient().holidays()
                        .createHoliday(request.offices(List.of(new PostHolidaysRequestOffices().officeId(1L))).locale("en")
                                .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("MONTH_END_HOLIDAY_", 5))))
                .getResourceId();
    }

    private void updateWorkingDays(final String recurrence, final Integer repaymentRescheduleType,
            final Boolean extendTermForDailyRepayments) {
        ok(() -> FineractFeignClientHelper.getFineractFeignClient().workingDays()
                .updateWorkingDay(new WorkingDaysUpdateRequest().recurrence(recurrence).repaymentRescheduleType(repaymentRescheduleType)
                        .extendTermForDailyRepayments(extendTermForDailyRepayments)));
    }

    /**
     * A loan can override its product's strategy at submission, in either direction and on both schedule types. The
     * loan keeps and reads back its own value, and its due dates follow it rather than the product's.
     */
    @Test
    public void testLoanOverridesTheProductStrategyAtSubmission() {
        final Long clientId = createClient();
        final List<LocalDate> clamped = List.of(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 30));
        final List<LocalDate> rolled = List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1));
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                final Long rollingLoanId = disbursedLoan(clientId, type, LAST_DAY_OF_MONTH, "31 January 2025", 3, INTEREST_RATE,
                        loanStrategy(FIRST_DAY_OF_NEXT_MONTH));
                final Long clampingLoanId = disbursedLoan(clientId, type, FIRST_DAY_OF_NEXT_MONTH, "31 January 2025", 3, INTEREST_RATE,
                        loanStrategy(LAST_DAY_OF_MONTH));

                assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(rollingLoanId).getMonthEndDueDateStrategy().getId(), type.name());
                assertEquals(rolled, dueDates(rollingLoanId), type.name() + ": the loan rolls forward on a product that clamps");
                assertEquals(LAST_DAY_OF_MONTH, getLoanDetails(clampingLoanId).getMonthEndDueDateStrategy().getId(), type.name());
                assertEquals(clamped, dueDates(clampingLoanId), type.name() + ": the loan clamps on a product that rolls forward");
            }
        });
    }

    /**
     * Modifying a loan application can change its strategy, and the schedule is regenerated with the new value.
     */
    @Test
    public void testModifyingTheApplicationChangesTheStrategy() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                final Long productId = product(type.template().get(), LAST_DAY_OF_MONTH);
                final Long loanId = applyForLoan(type.cumulative()
                        ? LoanRequestBuilders.applyCumulativeLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL, INTEREST_RATE,
                                3, null)
                        : LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL,
                                INTEREST_RATE, 3, null));
                assertEquals(LocalDate.of(2025, 2, 28), dueDates(loanId).getFirst(), type.name());

                modifyLoanApplication(loanId, "modify",
                        new PutLoansLoanIdRequest().clientId(clientId).productId(productId).loanType("individual")
                                .monthEndDueDateStrategy(PutLoansLoanIdRequest.MonthEndDueDateStrategyEnum.FIRST_DAY_OF_NEXT_MONTH)
                                .locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
                assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(loanId).getMonthEndDueDateStrategy().getId(), type.name());

                approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "31 January 2025"));
                disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "31 January 2025");
                assertEquals(List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1)), dueDates(loanId),
                        type.name());
            }
        });
    }

    /**
     * The schedule of a submitted loan is already rolled: the first due date derived from the disbursement date lands
     * on 1 March like the generated ones, not on the clamped 28 February.
     */
    @Test
    public void testScheduleOfSubmittedLoanRollsForward() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            for (final ScheduleType type : scheduleTypes(UnaryOperator.identity())) {
                final Long productId = product(type.template().get(), FIRST_DAY_OF_NEXT_MONTH);
                final Long loanId = applyForLoan(type.cumulative()
                        ? LoanRequestBuilders.applyCumulativeLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL, INTEREST_RATE,
                                3, null)
                        : LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, productId, "31 January 2025", PRINCIPAL,
                                INTEREST_RATE, 3, null));

                assertEquals(List.of(LocalDate.of(2025, 3, 1), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 5, 1)), dueDates(loanId),
                        type.name());
            }
        });
    }

    /**
     * Moving a loan application to another product takes that product's strategy, unless the request sets one.
     */
    @Test
    public void testChangingTheProductOfAnApplicationTakesItsStrategy() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long rollingProductId = product(create4IProgressive(), FIRST_DAY_OF_NEXT_MONTH);
            final Long clampingProductId = product(create4IProgressive(), LAST_DAY_OF_MONTH);
            final Long loanId = applyForLoan(LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, rollingProductId,
                    "31 January 2025", PRINCIPAL, INTEREST_RATE, 3, null));

            modifyLoanApplication(loanId, "modify", new PutLoansLoanIdRequest().clientId(clientId).productId(clampingProductId)
                    .loanType("individual").locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            assertEquals(LAST_DAY_OF_MONTH, getLoanDetails(loanId).getMonthEndDueDateStrategy().getId());

            approveLoan(loanId, LoanRequestBuilders.approveLoan(PRINCIPAL, "31 January 2025"));
            disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "31 January 2025");
            assertEquals(List.of(LocalDate.of(2025, 2, 28), LocalDate.of(2025, 3, 31), LocalDate.of(2025, 4, 30)), dueDates(loanId));

            final Long overridingLoanId = applyForLoan(LoanRequestBuilders.applyLP2ProgressiveLoanRequest(clientId, rollingProductId,
                    "31 January 2025", PRINCIPAL, INTEREST_RATE, 3, null));
            modifyLoanApplication(overridingLoanId, "modify",
                    new PutLoansLoanIdRequest().clientId(clientId).productId(clampingProductId).loanType("individual")
                            .monthEndDueDateStrategy(PutLoansLoanIdRequest.MonthEndDueDateStrategyEnum.FIRST_DAY_OF_NEXT_MONTH)
                            .locale(LoanTestData.LOCALE).dateFormat(LoanTestData.DATETIME_PATTERN));
            assertEquals(FIRST_DAY_OF_NEXT_MONTH, getLoanDetails(overridingLoanId).getMonthEndDueDateStrategy().getId());
        });
    }

    @Test
    public void testLoanRejectsUnknownStrategy() {
        final Long clientId = createClient();
        runAt("31 January 2025", () -> {
            final Long productId = product(create4ICumulative(), LAST_DAY_OF_MONTH);
            final Long loanId = applyForLoan(LoanRequestBuilders.applyCumulativeLoanRequest(clientId, productId, "31 January 2025",
                    PRINCIPAL, INTEREST_RATE, 3, null));

            final RuntimeException exception = assertThrows(RuntimeException.class, () -> FeignRawHttpHelper.put("/loans/" + loanId, """
                    {"clientId": %d, "productId": %d, "loanType": "individual", "monthEndDueDateStrategy": "SOMEWHERE_IN_BETWEEN",
                     "locale": "en", "dateFormat": "dd MMMM yyyy"}
                    """.formatted(clientId, productId)));
            assertTrue(exception.getMessage().startsWith("HTTP 400"), exception.getMessage());
            assertEquals(LAST_DAY_OF_MONTH, getLoanDetails(loanId).getMonthEndDueDateStrategy().getId(),
                    "A rejected change changes nothing");
        });
    }

    /**
     * Cumulative loans charge first-period interest from the disbursement date only when the global configuration
     * {@code interest-charged-from-date-same-as-disbursal-date} is on. The interest amounts asserted here assume it is,
     * as it is on databases initialised for the e2e tests, so it is switched on for the duration and then restored.
     */
    private void withInterestChargedFromDisbursementDate(final Runnable test) {
        final String configName = GlobalConfigurationConstants.INTEREST_CHARGED_FROM_DATE_SAME_AS_DISBURSAL_DATE;
        final boolean wasEnabled = Boolean.TRUE.equals(globalConfigurationHelper.getGlobalConfigurationByName(configName).getEnabled());
        globalConfigurationHelper.updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().enabled(true));
        try {
            test.run();
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(configName, new PutGlobalConfigurationsRequest().enabled(wasEnabled));
        }
    }

    private static PostLoanProductsRequest actualActual(final PostLoanProductsRequest template) {
        return template.daysInMonthType(DaysInMonthType.ACTUAL).daysInYearType(DaysInYearType.ACTUAL);
    }

    private Long product(final PostLoanProductsRequest template, final String monthEndDueDateStrategy) {
        return createLoanProduct(template.currencyCode("USD").monthEndDueDateStrategy(monthEndDueDateStrategy == null ? null
                : PostLoanProductsRequest.MonthEndDueDateStrategyEnum.fromValue(monthEndDueDateStrategy)));
    }

    private Long progressiveLoan(final Long clientId, final Long productId, final String date, final int numberOfRepayments) {
        return applyAndApproveProgressiveLoan(clientId, productId, date, PRINCIPAL, INTEREST_RATE, numberOfRepayments, null);
    }

    private Long cumulativeLoan(final Long clientId, final Long productId, final String date, final int numberOfRepayments) {
        return applyAndApproveCumulativeLoan(clientId, productId, date, PRINCIPAL, INTEREST_RATE, numberOfRepayments, null);
    }

    private Long disbursedLoan(final Long clientId, final ScheduleType type, final String monthEndDueDateStrategy, final String date,
            final int numberOfRepayments) {
        return disbursedLoan(clientId, type, monthEndDueDateStrategy, date, numberOfRepayments, INTEREST_RATE);
    }

    private Long disbursedLoan(final Long clientId, final ScheduleType type, final String monthEndDueDateStrategy, final String date,
            final int numberOfRepayments, final double interestRate) {
        return disbursedLoan(clientId, type, monthEndDueDateStrategy, date, numberOfRepayments, interestRate, null);
    }

    private Long disbursedLoan(final Long clientId, final ScheduleType type, final String monthEndDueDateStrategy, final String date,
            final int numberOfRepayments, final double interestRate, final Consumer<PostLoansRequest> loanCustomizer) {
        final Long productId = product(type.template().get(), monthEndDueDateStrategy);
        final Long loanId = type.cumulative()
                ? applyAndApproveCumulativeLoan(clientId, productId, date, PRINCIPAL, interestRate, numberOfRepayments, loanCustomizer)
                : applyAndApproveProgressiveLoan(clientId, productId, date, PRINCIPAL, interestRate, numberOfRepayments, loanCustomizer);
        disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), date);
        return loanId;
    }

    private static Consumer<PostLoansRequest> loanStrategy(final String monthEndDueDateStrategy) {
        return request -> request.monthEndDueDateStrategy(PostLoansRequest.MonthEndDueDateStrategyEnum.fromValue(monthEndDueDateStrategy));
    }

    /**
     * Hands out a fresh product request per product, since products must not share a name.
     */
    private record ScheduleType(String name, boolean cumulative, Supplier<PostLoanProductsRequest> template) {
    }

    private ScheduleType progressive(final UnaryOperator<PostLoanProductsRequest> customizer) {
        return new ScheduleType("PROGRESSIVE", false, () -> customizer.apply(create4IProgressive()));
    }

    private ScheduleType cumulative(final UnaryOperator<PostLoanProductsRequest> customizer) {
        return new ScheduleType("CUMULATIVE", true, () -> customizer.apply(create4ICumulative()));
    }

    private List<ScheduleType> scheduleTypes(final UnaryOperator<PostLoanProductsRequest> customizer) {
        return List.of(progressive(customizer), cumulative(customizer));
    }

    private static String strategyOf(final GetLoanProductsProductIdResponse product) {
        return product.getMonthEndDueDateStrategy() == null ? null : product.getMonthEndDueDateStrategy().getId();
    }

    private List<GetLoansLoanIdRepaymentPeriod> installments(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private List<LocalDate> dueDates(final Long loanId) {
        return installments(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).toList();
    }

    private List<BigDecimal> interests(final Long loanId) {
        return installments(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getInterestDue).map(MonthEndDueDateStrategyTest::scaled)
                .toList();
    }

    private List<BigDecimal> principals(final Long loanId) {
        return installments(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getPrincipalDue).map(MonthEndDueDateStrategyTest::scaled)
                .toList();
    }

    private static BigDecimal scaled(final BigDecimal amount) {
        return Objects.requireNonNullElse(amount, BigDecimal.ZERO).stripTrailingZeros();
    }
}
