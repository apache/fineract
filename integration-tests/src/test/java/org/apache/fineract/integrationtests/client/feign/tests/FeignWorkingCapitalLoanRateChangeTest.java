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
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import org.apache.fineract.client.feign.util.CallFailedRuntimeException;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
import org.apache.fineract.client.models.WorkingCapitalLoanPeriodPaymentRateChangeData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

public class FeignWorkingCapitalLoanRateChangeTest extends FeignIntegrationTest {

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private FeignBusinessDateHelper businessDateHelper;

    private Long clientId;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        clientId = clientHelper.createClient();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    void testUpdateRateOnActiveLoan() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17),
                Utils.dateFormatter.format(Utils.getLocalDateOfTenant())));

        GetWorkingCapitalLoansLoanIdResponse loan = wcLoanHelper.getLoanDetails(loanId);
        assertNotNull(loan);
        assertEquals(0, BigDecimal.valueOf(18).compareTo(loan.getPaymentRate()),
                "The loan reports the rate it was created with, which a rate change does not move");
        assertEquals(0, BigDecimal.valueOf(17).compareTo(rateInEffectOn(loanId, Utils.getLocalDateOfTenant())),
                "The rate in force is the one just booked");
    }

    @Test
    void testRateChangeHistoryIsRecorded() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17),
                Utils.dateFormatter.format(Utils.getLocalDateOfTenant())));

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
        assertFalse(history.isEmpty(), "Rate change history should not be empty");

        WorkingCapitalLoanPeriodPaymentRateChangeData change = history.getFirst();
        assertEquals(0, BigDecimal.valueOf(18).compareTo(change.getPreviousRate()));
        assertEquals(0, BigDecimal.valueOf(17).compareTo(change.getNewRate()));
        assertFalse(change.getReversed());
    }

    @Test
    void testRateChangeNotAllowedOnNonActiveLoan() {
        Long productId = createProduct();
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = submitAndTrack(clientId, productId, BigDecimal.valueOf(5000), BigDecimal.valueOf(18), today);

        CallFailedRuntimeException exception = wcLoanHelper.updateRateExpectingError(loanId,
                WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17), today));
        assertTrue(exception.getStatus() >= 400,
                "Rate change on non-active loan should fail with 4xx status, got: " + exception.getStatus());
    }

    @Test
    void testMultipleRateChangesAutoReversesPrevious() {
        Long loanId = createAndDisburseLoan(BigDecimal.valueOf(5000), BigDecimal.valueOf(18));

        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(17), today));
        wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15), today));

        List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
        assertEquals(2, history.size(), "Should have 2 rate change records");

        // Most recent (15%) should be active, previous (17%) should be auto-reversed
        WorkingCapitalLoanPeriodPaymentRateChangeData latestChange = history.get(0);
        WorkingCapitalLoanPeriodPaymentRateChangeData firstChange = history.get(1);
        assertFalse(latestChange.getReversed(), "Latest rate change should be active");
        assertTrue(firstChange.getReversed(), "Previous rate change should be auto-reversed");

        assertEquals(0, BigDecimal.valueOf(15).compareTo(rateInEffectOn(loanId, Utils.getLocalDateOfTenant())),
                "The surviving change of the two booked for today is the one in force");
    }

    @Test
    void testMultipleRateChangesOnDifferentBusinessDates() {
        businessDateHelper.runAt("2026-01-01", () -> {
            Long clientForTest = clientHelper.createClient("01 January 2026");
            Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(50000), BigDecimal.valueOf(18), "01 January 2026");

            // First rate change: 18 → 15 on Jan 1
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15), "01 January 2026"));

            // Advance business date by 8 days
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-09");

            // Second rate change: 15 → 11 on Jan 9
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(11), "09 January 2026"));

            assertEquals(0, BigDecimal.valueOf(11).compareTo(rateInEffectOn(loanId, LocalDate.of(2026, 1, 9))),
                    "Rate in force on 09 January should be 11 after the second rate change");

            List<WorkingCapitalLoanPeriodPaymentRateChangeData> history = wcLoanHelper.getRateChangeHistory(loanId);
            assertEquals(2, history.size(), "Should have 2 rate change records");

            // Both stay active: only a change sharing an effective date overwrites, and these take effect on different
            // dates, so each governs its own segment of the schedule.
            assertFalse(history.get(0).getReversed(), "Rate change effective 09 January should be active");
            assertFalse(history.get(1).getReversed(), "Rate change effective 01 January should stay active on its own date");
        });
    }

    /**
     * Two changes booked a day apart can end up governing the same day of the schedule, and the one booked later must
     * win.
     *
     * <p>
     * Which calendar day the schedule starts on depends on whether anything was repaid on the disbursement date: with a
     * repayment there the first instalment falls on the disbursement date itself, without one it falls the day after.
     * So undoing such a repayment shifts every day of the schedule back by one, and a change effective on the
     * disbursement date no longer has a day of its own - it lands on the first instalment, alongside the change
     * effective the day after it.
     *
     * <p>
     * The day must bill the rate in force on it, which is the later of the two. Taking the earlier one instead would
     * have a superseded rate govern the first day and the live one start a day late, and since the schedule ahead is
     * solved from the first day's balance, that re-prices every day after it rather than just the one.
     */
    @Test
    void testRateChangesOnConsecutiveDaysCollapsingOntoOneKeepTheRateBookedLater() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long clientForTest = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(clientForTest, BigDecimal.valueOf(9000), BigDecimal.valueOf(18),
                    "01 January 2026");

            // A repayment on the disbursement date is what puts the first instalment on that date.
            final Long disbursementDateRepayment = wcLoanHelper.makeRepayment(loanId,
                    WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(50), "01 January 2026"));
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(15), "01 January 2026"));

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-02");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(13), "02 January 2026"));

            // While the repayment stands the two changes have a day each: rate 15 bills 41.67, rate 13 bills 36.11.
            final List<ProjectedAmortizationSchedulePaymentData> before = instalments(wcLoanHelper.getAmortizationSchedule(loanId));
            assertEquals(LocalDate.of(2026, 1, 1), before.getFirst().getPaymentDate(),
                    "A repayment on the disbursement date puts the first instalment on that date");
            assertEquals(0, new BigDecimal("41.67").compareTo(before.getFirst().getExpectedPaymentAmount()),
                    "The first day bills the rate effective on the disbursement date");
            assertEquals(0, new BigDecimal("36.11").compareTo(before.get(1).getExpectedPaymentAmount()),
                    "The second day bills the rate effective the day after it");

            wcLoanHelper.undoTransaction(loanId, disbursementDateRepayment, WorkingCapitalLoanRequestBuilders.undoTransaction());

            assertEquals(2, wcLoanHelper.getRateChangeHistory(loanId).size(), "Undoing a repayment must not discard either change");
            assertEquals(0, BigDecimal.valueOf(13).compareTo(rateInEffectOn(loanId, LocalDate.of(2026, 1, 2))),
                    "The later change is still the one in force");

            final List<ProjectedAmortizationSchedulePaymentData> after = instalments(wcLoanHelper.getAmortizationSchedule(loanId));
            assertEquals(LocalDate.of(2026, 1, 2), after.getFirst().getPaymentDate(),
                    "With nothing repaid on the disbursement date the first instalment falls the day after it");
            assertEquals(0, new BigDecimal("36.11").compareTo(after.getFirst().getExpectedPaymentAmount()),
                    "The first day must bill the rate booked later, not the one it superseded");
            assertEquals(0, new BigDecimal("36.11").compareTo(after.get(1).getExpectedPaymentAmount()),
                    "And it must go on billing it, rather than starting a day late");
        });
    }

    private static List<ProjectedAmortizationSchedulePaymentData> instalments(final ProjectedAmortizationScheduleData schedule) {
        assertNotNull(schedule.getPayments(), "The schedule has no payments");
        return schedule.getPayments().stream().filter(row -> row.getPaymentNo() != null && row.getPaymentNo() > 0).toList();
    }

    /**
     * The rate the loan is actually billed at on {@code asOf}, taken from the rate-change history.
     *
     * <p>
     * The loan resource reports the rate it was created with and does not move when a change is booked, so the two
     * answers differ for any loan with a change in force. Ranked by effective date rather than by id, because a
     * backdated change is created after the changes it precedes; id only breaks ties within one effective date, where
     * the later-created change is the correction.
     */
    private BigDecimal rateInEffectOn(Long loanId, LocalDate asOf) {
        return wcLoanHelper.getRateChangeHistory(loanId).stream() //
                .filter(change -> !Boolean.TRUE.equals(change.getReversed())) //
                .filter(change -> change.getEffectiveDate() != null && !change.getEffectiveDate().isAfter(asOf)) //
                .max(Comparator.comparing(WorkingCapitalLoanPeriodPaymentRateChangeData::getEffectiveDate)
                        .thenComparing(WorkingCapitalLoanPeriodPaymentRateChangeData::getId)) //
                .map(WorkingCapitalLoanPeriodPaymentRateChangeData::getNewRate) //
                .orElseGet(() -> wcLoanHelper.getLoanDetails(loanId).getPaymentRate());
    }

    private Long createAndDisburseLoanOnDate(Long clientIdParam, BigDecimal principal, BigDecimal rate, String date) {
        Long productId = createProduct();
        Long loanId = submitAndTrack(clientIdParam, productId, principal, rate, date);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createAndDisburseLoan(BigDecimal principal, BigDecimal rate) {
        Long productId = createProduct();
        String today = Utils.dateFormatter.format(Utils.getLocalDateOfTenant());
        Long loanId = submitAndTrack(clientId, productId, principal, rate, today);

        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(today, principal, today));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(today, principal));

        return loanId;
    }

    private Long submitAndTrack(Long clientIdParam, Long productId, BigDecimal principal, BigDecimal rate, String date) {
        Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientIdParam, productId, principal, rate, date, date));
        createdLoanIds.add(loanId);
        return loanId;
    }

    private Long createProduct() {
        String uniqueName = "WCL Rate " + Utils.uniqueRandomStringGenerator("", 8);
        String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
