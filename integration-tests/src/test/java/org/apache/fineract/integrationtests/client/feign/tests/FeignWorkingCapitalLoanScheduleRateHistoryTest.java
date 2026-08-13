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
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import org.apache.fineract.client.models.ProjectedAmortizationScheduleData;
import org.apache.fineract.client.models.ProjectedAmortizationSchedulePaymentData;
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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Verifies that when reprocessing rebuilds the projected amortization schedule from scratch, it replays the loan's
 * rate-change history rather than flattening the whole schedule to the current rate.
 *
 * <p>
 * The projected schedule is a forward projection from the running balance, so actual repayments legitimately reshape
 * the periods on or after their date (more principal paid -> the payoff lands sooner). What must NOT change is the
 * segment <em>before</em> the first repayment: those periods are projected from the untouched opening balance, so they
 * are a pure function of the rate history. If reconstruction flattened the schedule to the current rate, the
 * original-rate segment would move; this test pins it to the rate-18 schedule captured at disbursement.
 */
public class FeignWorkingCapitalLoanScheduleRateHistoryTest extends FeignIntegrationTest {

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(fineractClient());
        clientHelper = new FeignClientHelper(fineractClient());
        businessDateHelper = new FeignBusinessDateHelper(fineractClient());
        productHelper = new WorkingCapitalLoanProductHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    // The rate change is applied on this date, so periods strictly before it are the original-rate (18%) segment, and
    // the first repayment is later still, so those periods are also untouched by any actual payment.
    private static final LocalDate RATE_CHANGE_DATE = LocalDate.of(2026, 1, 10);

    @Test
    @DisplayName("Reconstruction triggered by a backdated repayment preserves the original-rate segment instead of flattening to the current rate")
    void reconstructionAfterBackdatedRepayment_preservesOriginalRateSegment() {
        businessDateHelper.runAt("2026-01-01", () -> {
            final Long client = clientHelper.createClient("01 January 2026");
            final Long loanId = createAndDisburseLoanOnDate(client, BigDecimal.valueOf(9000), "01 January 2026");
            // A charge makes a later backdated repayment route through the full reset+replay reprocessing that rebuilds
            // the schedule from scratch (the path that must respect the rate history).
            addCharge(loanId, false, 100, "01 January 2026");

            // The schedule at disbursement is a single 18% projection: capture the original-rate segment as the
            // baseline.
            final Map<LocalDate, BigDecimal> baselineRate18Segment = originalRateSegment(wcLoanHelper.getAmortizationSchedule(loanId));
            assertFalse(baselineRate18Segment.isEmpty(), "There must be projected periods before the rate change to pin the segment on");

            // Change the rate on day 10, splitting the schedule into an 18% segment then an 11% segment.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-10");
            wcLoanHelper.updateRate(loanId, WorkingCapitalLoanRequestBuilders.updateRate(BigDecimal.valueOf(11), "10 January 2026"));
            assertTrue(distinctPositiveExpectedAmounts(expectedByDate(wcLoanHelper.getAmortizationSchedule(loanId))) >= 2,
                    "The rate change must produce at least two distinct projected payment amounts (two segments)");

            // A repayment on day 25 - the later transaction that a subsequent backdated one will predate.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-25");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(500), "25 January 2026"));

            // Backdated repayment on day 15 (before the day-25 one) triggers the full reprocess + schedule rebuild.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", "2026-01-30");
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(BigDecimal.valueOf(200), "15 January 2026"));

            final ProjectedAmortizationScheduleData rebuilt = wcLoanHelper.getAmortizationSchedule(loanId);
            // The original-rate segment precedes every repayment, so reconstruction must reproduce it exactly - a
            // flatten
            // to the current 11% rate would shift these amounts.
            final Map<LocalDate, BigDecimal> rebuiltRate18Segment = originalRateSegment(rebuilt);
            assertEquals(baselineRate18Segment.keySet(), rebuiltRate18Segment.keySet(),
                    "The original-rate segment must keep the same period dates through reconstruction");
            baselineRate18Segment
                    .forEach((date, expected) -> assertEquals(0, expected.compareTo(rebuiltRate18Segment.get(date)), "Projected payment on "
                            + date + " (original-rate segment) must survive reconstruction unchanged, not flatten to the current rate"));
            // And the rebuilt schedule still carries the rate segmentation rather than collapsing to a single rate.
            assertTrue(distinctPositiveExpectedAmounts(expectedByDate(rebuilt)) >= 2,
                    "The reconstructed schedule must still show the rate-change segmentation (not flattened to one rate)");
        });
    }

    /** The projected periods strictly before the rate change - the original-rate segment, ahead of every repayment. */
    private static Map<LocalDate, BigDecimal> originalRateSegment(final ProjectedAmortizationScheduleData schedule) {
        final Map<LocalDate, BigDecimal> segment = new LinkedHashMap<>();
        expectedByDate(schedule).forEach((date, expected) -> {
            if (expected != null && expected.signum() > 0 && date.isAfter(LocalDate.of(2026, 1, 1)) && date.isBefore(RATE_CHANGE_DATE)) {
                segment.put(date, expected);
            }
        });
        return segment;
    }

    private static Map<LocalDate, BigDecimal> expectedByDate(final ProjectedAmortizationScheduleData schedule) {
        assertNotNull(schedule.getPayments(), "Schedule must have payment rows");
        final Map<LocalDate, BigDecimal> byDate = new LinkedHashMap<>();
        for (final ProjectedAmortizationSchedulePaymentData payment : schedule.getPayments()) {
            byDate.put(payment.getPaymentDate(), payment.getExpectedPaymentAmount());
        }
        return byDate;
    }

    private static int distinctPositiveExpectedAmounts(final Map<LocalDate, BigDecimal> expectedByDate) {
        final Set<String> distinct = new HashSet<>();
        for (final BigDecimal amount : expectedByDate.values()) {
            if (amount != null && amount.signum() > 0) {
                distinct.add(amount.stripTrailingZeros().toPlainString());
            }
        }
        return distinct.size();
    }

    private Long addCharge(final Long loanId, final boolean penalty, final double amount, final String dueDate) {
        final Long chargeId = wcLoanHelper.createGlobalCharge(WorkingCapitalLoanRequestBuilders.specifiedDueDateCharge(penalty, amount));
        return wcLoanHelper.addCharge(loanId, WorkingCapitalLoanRequestBuilders.addCharge(chargeId, amount, dueDate));
    }

    private Long createAndDisburseLoanOnDate(final Long clientId, final BigDecimal principal, final String date) {
        final Long productId = createProduct();
        final Long loanId = wcLoanHelper.submitApplication(
                WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId, principal, BigDecimal.valueOf(18), date, date));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(date, principal, date));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(date, principal));
        return loanId;
    }

    private Long createProduct() {
        final String uniqueName = "WCL RateHist " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper
                .createWorkingCapitalLoanProduct(
                        new WorkingCapitalLoanProductTestBuilder().withName(uniqueName).withShortName(uniqueShortName).build())
                .getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
