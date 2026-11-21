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
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostHolidaysRequest;
import org.apache.fineract.client.models.PostHolidaysRequestOffices;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.LoanRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * A loan reschedule regenerates the schedule from the reschedule date on. When an earlier instalment was moved by a
 * "reschedule to next repayment date" holiday, the regenerated instalments must keep following on from the moved date,
 * as the original schedule does.
 * <p>
 * Dates are in 2038 so that no other test's loan has a due date inside the holiday.
 * </p>
 */
public class LoanRescheduleAfterNextRepaymentDateHolidayTest extends FeignLoanTestBase {

    private static final Integer RESCHEDULE_TO_NEXT_REPAYMENT_DATE = 1;
    private static final double PRINCIPAL = 10000.0;

    /**
     * The holiday moves instalment 2 from 30 December 2038 to the next repayment date, 30 January 2039. A reschedule
     * from instalment 3 (28 February 2039) with one period of principal grace must keep every due date and add one
     * instalment at the end; it used to pull every regenerated due date back one period instead.
     */
    @Test
    public void testRescheduleKeepsTheHolidayMoveOfAnEarlierInstalment() {
        final Long clientId = createClient();
        try {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(true));
            HolidayHelper.activateHolidays(holiday(LocalDate.of(2038, 12, 30), LocalDate.of(2039, 1, 5)));

            runAt("30 October 2038", () -> {
                final Long productId = createLoanProduct(create4ICumulative().currencyCode("USD"));
                final Long loanId = applyAndApproveCumulativeLoan(clientId, productId, "30 October 2038", PRINCIPAL, 1.0, 12, null);
                disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "30 October 2038");

                final List<LocalDate> originalDueDates = List.of(LocalDate.of(2038, 11, 30), //
                        LocalDate.of(2039, 1, 30), // moved from 30 December 2038 by the holiday
                        LocalDate.of(2039, 2, 28), //
                        LocalDate.of(2039, 3, 30), //
                        LocalDate.of(2039, 4, 30), //
                        LocalDate.of(2039, 5, 30), //
                        LocalDate.of(2039, 6, 30), //
                        LocalDate.of(2039, 7, 30), //
                        LocalDate.of(2039, 8, 30), //
                        LocalDate.of(2039, 9, 30), //
                        LocalDate.of(2039, 10, 30), //
                        LocalDate.of(2039, 11, 30));
                assertEquals(originalDueDates, dueDates(loanId), "The holiday moves instalment 2 and the later ones follow on");

                final Long rescheduleId = createRescheduleRequest(
                        LoanRequestBuilders.rescheduleRequest(loanId, "30 October 2038", "28 February 2039", null).graceOnPrincipal(1));
                approveRescheduleRequest(rescheduleId, LoanRequestBuilders.approveReschedule("30 October 2038"));

                final List<GetLoansLoanIdRepaymentPeriod> periods = periods(loanId);
                final List<LocalDate> expectedDueDates = new ArrayList<>(originalDueDates);
                expectedDueDates.add(LocalDate.of(2039, 12, 30));
                assertEquals(expectedDueDates, periods.stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).toList(),
                        "The reschedule keeps every due date and adds one instalment at the end");
                assertEquals(LocalDate.of(2039, 1, 30), periods.get(2).getFromDate(), "Instalment 3 starts on the moved date");
                assertEquals(0, BigDecimal.ZERO.compareTo(periods.get(2).getPrincipalDue()),
                        "The principal grace applies to instalment 3, the first rescheduled one");
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long holiday(final LocalDate fromDate, final LocalDate toDate) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays()
                .createHoliday(new PostHolidaysRequest().offices(List.of(new PostHolidaysRequestOffices().officeId(1L))).locale("en")
                        .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("NEXT_REPAYMENT_HOLIDAY_", 5)).fromDate(fromDate)
                        .toDate(toDate).reschedulingType(RESCHEDULE_TO_NEXT_REPAYMENT_DATE)))
                .getResourceId();
    }

    private List<GetLoansLoanIdRepaymentPeriod> periods(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null).toList();
    }

    private List<LocalDate> dueDates(final Long loanId) {
        return periods(loanId).stream().map(GetLoansLoanIdRepaymentPeriod::getDueDate).toList();
    }
}
