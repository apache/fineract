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

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostHolidaysRequest;
import org.apache.fineract.client.models.PostHolidaysRequestOffices;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignDelinquencyHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Loan COB and the Apply Holidays To Loans job read the repayment schedules of all the loans they load in one query.
 * <p>
 * Both tests pay into the first installment before the loans are loaded that way. The payment rewrites the
 * installment's row, and PostgreSQL can then return it after the rows of the later installments unless the query orders
 * them. The loan reads its schedule by position, so a schedule read out of order shows up in what the job computes from
 * it.
 * </p>
 */
public class LoanRepaymentScheduleFetchTest extends FeignLoanTestBase {

    private static final Integer RESCHEDULE_TO_SPECIFIED_DATE = 2;
    private static final String LOAN_RESCHEDULED_DUE_HOLIDAY_EVENT = "LoanRescheduledDueHolidayBusinessEvent";

    /**
     * Installment 1 stays partly unpaid, so on 15 April 2024 each loan is overdue since 1 February 2024, 74 days. COB
     * takes the overdue date from the first overdue installment of the schedule, and from installment 2 or 3 it would
     * classify the loan into a younger range. Where the rewritten rows end up differs from run to run, so three loans
     * are checked.
     */
    @Test
    public void testCobClassifiesDelinquencyFromTheOldestOverdueInstallment() {
        final Long clientId = createClient();
        final Long bucketId = new FeignDelinquencyHelper(FineractFeignClientHelper.getFineractFeignClient())
                .createBucket(List.of(Pair.of(1, 30), Pair.of(31, 60), Pair.of(61, null)));
        final Long productId = createLoanProduct(
                create4IProgressive().currencyCode("USD").isInterestRecalculationEnabled(false).delinquencyBucketId(bucketId));
        final List<Long> loanIds = new ArrayList<>();

        runAt("1 January 2024", () -> {
            for (int i = 0; i < 3; i++) {
                final Long loanId = applyAndApproveProgressiveLoan(clientId, productId, "1 January 2024", 1000.0, 12.0, 4, null);
                disburseLoan(loanId, BigDecimal.valueOf(1000), "1 January 2024");
                loanIds.add(loanId);
            }
        });
        runAt("2 February 2024", () -> loanIds.forEach(loanId -> makeLoanRepayment(loanId, "repayment", "2 February 2024", 10.0)));
        runAt("15 April 2024", () -> {
            executeInlineCOB(loanIds);

            for (final Long loanId : loanIds) {
                assertEquals(61, getLoanDetails(loanId).getDelinquencyRange().getMinimumAgeDays(),
                        "COB classified loan " + loanId + " by the overdue date of installment 1");
            }
        });
    }

    /**
     * The job sets each installment's from date to the due date of the installment before it in the list, so after it
     * ran the from dates chain the due dates only if each loan's schedule came in installment order. It raises one
     * rescheduled event per loan it was given, so each loan must come back from the office query exactly once.
     */
    @Test
    public void testApplyHolidaysProcessesEachLoanOnceWithItsScheduleInInstallmentOrder() {
        final Long firstClientId = createClient();
        final Long secondClientId = createClient();
        final Map<String, Boolean> previousEventStates = externalEventHelper
                .enableBusinessEvents(List.of(LOAN_RESCHEDULED_DUE_HOLIDAY_EVENT));
        try {
            runAt("15 January 2038", () -> {
                final Long productId = createLoanProduct(create4ICumulative().currencyCode("USD"));
                final Long repaidLoanId = applyAndApproveCumulativeLoan(firstClientId, productId, "15 January 2038", 1000.0, 12.0, 4, null);
                disburseLoan(repaidLoanId, BigDecimal.valueOf(1000), "15 January 2038");
                final Long unpaidLoanId = applyAndApproveCumulativeLoan(secondClientId, productId, "15 January 2038", 1000.0, 12.0, 4,
                        null);
                disburseLoan(unpaidLoanId, BigDecimal.valueOf(1000), "15 January 2038");
                makeLoanRepayment(repaidLoanId, "repayment", "15 January 2038",
                        installments(repaidLoanId).getFirst().getTotalDueForPeriod().doubleValue());

                final List<LocalDate> expectedDueDates = new ArrayList<>(dueDates(repaidLoanId));
                assertEquals(expectedDueDates, dueDates(unpaidLoanId));
                final LocalDate holiday = expectedDueDates.get(2);
                final LocalDate rescheduledTo = holiday.minusDays(3);
                expectedDueDates.set(2, rescheduledTo);

                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                        new PutGlobalConfigurationsRequest().enabled(true));
                HolidayHelper.activateHolidays(holiday(holiday, rescheduledTo));
                schedulerHelper.executeAndAwaitJob("Apply Holidays To Loans");

                for (final Long loanId : List.of(repaidLoanId, unpaidLoanId)) {
                    assertEquals(expectedDueDates, dueDates(loanId), "Due dates of loan " + loanId);
                    final List<LocalDate> expectedFromDates = new ArrayList<>();
                    expectedFromDates.add(LocalDate.of(2038, 1, 15));
                    expectedFromDates.addAll(expectedDueDates.subList(0, expectedDueDates.size() - 1));
                    assertEquals(expectedFromDates, installments(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getFromDate).toList(),
                            "From dates of loan " + loanId);
                    assertEquals(1, externalEventHelper.getExternalEvents(LOAN_RESCHEDULED_DUE_HOLIDAY_EVENT, loanId).size(),
                            "Rescheduled events of loan " + loanId);
                }
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(false));
            externalEventHelper.restoreBusinessEvents(previousEventStates);
        }
    }

    private Long holiday(final LocalDate date, final LocalDate repaymentsRescheduledTo) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays()
                .createHoliday(new PostHolidaysRequest().offices(List.of(new PostHolidaysRequestOffices().officeId(1L))).locale("en")
                        .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("SCHEDULE_FETCH_HOLIDAY_", 5)).fromDate(date)
                        .toDate(date).repaymentsRescheduledTo(repaymentsRescheduledTo).reschedulingType(RESCHEDULE_TO_SPECIFIED_DATE)))
                .getResourceId();
    }

    private List<GetLoansLoanIdRepaymentPeriod> installments(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private List<LocalDate> dueDates(final Long loanId) {
        return installments(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).toList();
    }
}
