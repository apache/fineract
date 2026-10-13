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
import java.util.List;
import org.apache.fineract.client.models.GetLoansLoanIdRepaymentPeriod;
import org.apache.fineract.client.models.PostHolidaysRequest;
import org.apache.fineract.client.models.PostHolidaysRequestOffices;
import org.apache.fineract.client.models.PutGlobalConfigurationsRequest;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignLoanTestBase;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.HolidayHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.Test;

/**
 * Applying a holiday that reschedules repayments to a date the user picked must move the instalments inside the holiday
 * to exactly that date.
 * <p>
 * Dates are in 2037 because the Apply Holidays To Loans job applies a holiday to every loan of the office, and no other
 * test's loan should fall into it.
 * </p>
 */
public class ApplyHolidaysToLoansRescheduleDateTest extends FeignLoanTestBase {

    private static final Integer RESCHEDULE_TO_SPECIFIED_DATE = 2;
    private static final double PRINCIPAL = 10000.0;

    /**
     * A loan disbursed on 31 January is anchored on the 31st. A holiday on 31 March reschedules repayments to the
     * picked 28 March, which must be kept as picked rather than snapped back onto the anchor day - that would land the
     * instalment on 31 March, the holiday itself. A second holiday rescheduling to 27 May shows the job did process the
     * loan.
     */
    @Test
    public void testInstalmentMovesToThePickedRescheduleDate() {
        final Long clientId = createClient();
        try {
            runAt("31 January 2037", () -> {
                final Long productId = createLoanProduct(create4ICumulative().currencyCode("USD"));
                final Long loanId = applyAndApproveCumulativeLoan(clientId, productId, "31 January 2037", PRINCIPAL, 12.0, 4, null);
                disburseLoan(loanId, BigDecimal.valueOf(PRINCIPAL), "31 January 2037");
                assertEquals(List.of(LocalDate.of(2037, 2, 28), //
                        LocalDate.of(2037, 3, 31), //
                        LocalDate.of(2037, 4, 30), //
                        LocalDate.of(2037, 5, 31) //
                ), dueDates(loanId));

                globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                        new PutGlobalConfigurationsRequest().enabled(true));
                HolidayHelper.activateHolidays(holiday(LocalDate.of(2037, 3, 31), LocalDate.of(2037, 3, 28)));
                HolidayHelper.activateHolidays(holiday(LocalDate.of(2037, 5, 31), LocalDate.of(2037, 5, 27)));
                schedulerHelper.executeAndAwaitJob("Apply Holidays To Loans");

                final List<LocalDate> dueDates = dueDates(loanId);
                assertEquals(LocalDate.of(2037, 3, 28), dueDates.get(1), "The instalment in the March holiday moves to the picked date");
                assertEquals(LocalDate.of(2037, 5, 27), dueDates.get(3), "The job moved the instalment in the May holiday");
            });
        } finally {
            globalConfigurationHelper.updateGlobalConfiguration(GlobalConfigurationConstants.RESCHEDULE_REPAYMENTS_ON_HOLIDAYS,
                    new PutGlobalConfigurationsRequest().enabled(false));
        }
    }

    private Long holiday(final LocalDate date, final LocalDate repaymentsRescheduledTo) {
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().holidays()
                .createHoliday(new PostHolidaysRequest().offices(List.of(new PostHolidaysRequestOffices().officeId(1L))).locale("en")
                        .dateFormat("yyyy-MM-dd").name(Utils.uniqueRandomStringGenerator("RESCHEDULE_TO_HOLIDAY_", 5)).fromDate(date)
                        .toDate(date).repaymentsRescheduledTo(repaymentsRescheduledTo).reschedulingType(RESCHEDULE_TO_SPECIFIED_DATE)))
                .getResourceId();
    }

    private List<LocalDate> dueDates(final Long loanId) {
        return getLoanDetails(loanId).getRepaymentSchedule().getPeriods().stream().filter(period -> period.getPeriod() != null)
                .map(GetLoansLoanIdRepaymentPeriod::getDueDate).toList();
    }
}
