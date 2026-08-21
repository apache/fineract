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
import java.util.List;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.integrationtests.client.FeignIntegrationTest;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignBusinessDateHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignClientHelper;
import org.apache.fineract.integrationtests.client.feign.helpers.FeignWorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalLoanRequestBuilders;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

/**
 * A repayment made before the loan's first COB has ever run must still count towards delinquency.
 *
 * <p>
 * The delinquency schedule used to come into existence only when COB first ran. A loan disbursed with a backdated date
 * and repaid before that first cycle therefore had nowhere to record the money: the repayment found no period to land
 * on and was dropped, and the first COB then built period 1 from scratch reading {@code paidAmount = 0.00} — tagging a
 * borrower who had paid as delinquent. Generating the period at disbursement is what closes that window, so these tests
 * assert both halves: the schedule exists before any COB, and a repayment made in that window is still counted.
 *
 * <p>
 * The dates are chosen so the repayment falls inside the first delinquency period: disbursed 05 January, a 20 day
 * period, repaid on the 20th.
 */
public class FeignWorkingCapitalLoanRepaymentBeforeFirstCobTest extends FeignIntegrationTest {

    private static final BigDecimal PRINCIPAL = new BigDecimal("3000");
    private static final BigDecimal RATE = new BigDecimal("18");

    private static final String DISBURSEMENT_DATE = "05 January 2026";
    private static final String DISBURSEMENT_DATE_ISO = "2026-01-05";
    private static final String REPAYMENT_DATE = "20 January 2026";
    private static final LocalDate REPAYMENT_LOCAL_DATE = LocalDate.of(2026, 1, 20);
    private static final String REPAYMENT_DATE_ISO = "2026-01-20";

    /** Comfortably above the 3% of 3000 the period needs to count as met, so a dropped repayment is unmistakable. */
    private static final BigDecimal REPAYMENT = new BigDecimal("500");

    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;
    private static final BigDecimal DELINQUENCY_MIN_PAYMENT_PERCENT = new BigDecimal("3");

    private static final int BREACH_FREQUENCY = 1;
    private static final String BREACH_FREQUENCY_TYPE = "MONTHS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_MIN_PAYMENT_AMOUNT = new BigDecimal("500");

    private FeignWorkingCapitalLoanHelper wcLoanHelper;
    private FeignClientHelper clientHelper;
    private FeignBusinessDateHelper businessDateHelper;
    private WorkingCapitalLoanProductHelper productHelper;
    private WorkingCapitalBreachHelper breachHelper;

    private final List<Long> createdLoanIds = new ArrayList<>();
    private final List<Long> createdProductIds = new ArrayList<>();

    @BeforeAll
    void setupHelpers() {
        final var feignClient = fineractClient();
        wcLoanHelper = new FeignWorkingCapitalLoanHelper(feignClient);
        clientHelper = new FeignClientHelper(feignClient);
        businessDateHelper = new FeignBusinessDateHelper(feignClient);
        productHelper = new WorkingCapitalLoanProductHelper();
        breachHelper = new WorkingCapitalBreachHelper();
    }

    @AfterAll
    void cleanupEntities() {
        createdLoanIds.forEach(wcLoanHelper::cleanupLoan);
        createdLoanIds.clear();
        createdProductIds.clear();
    }

    @Test
    void testDisbursementCreatesTheDelinquencyScheduleBeforeAnyCobRuns() {
        businessDateHelper.runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disburseLoan();

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = wcLoanHelper.getDelinquencyRangeSchedule(loanId);
            assertFalse(periods.isEmpty(), "disbursement must generate the first delinquency period, so a repayment made before the "
                    + "first COB has a period to be recorded against — but the schedule is empty");
        });
    }

    @Test
    void testRepaymentBeforeFirstCobIsCountedByTheDelinquencyPeriod() {
        businessDateHelper.runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disburseLoan();

            // Paid well after disbursement but still before this loan has ever been through COB.
            businessDateHelper.updateBusinessDate("BUSINESS_DATE", REPAYMENT_DATE_ISO);
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, REPAYMENT_DATE));

            // The loan's first-ever COB cycle.
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = wcLoanHelper.getDelinquencyRangeSchedule(loanId);
            assertFalse(periods.isEmpty(), "the first COB must leave a delinquency schedule behind");

            final WorkingCapitalLoanDelinquencyRangeScheduleData covering = periodCovering(periods, REPAYMENT_LOCAL_DATE);
            assertNotNull(covering.getPaidAmount(),
                    "the period covering " + REPAYMENT_LOCAL_DATE + " reports no paid amount at all" + render(periods));
            assertEquals(0, REPAYMENT.compareTo(covering.getPaidAmount()),
                    "the repayment was made before the first COB and must still be counted against the period covering it, but that "
                            + "period reports " + covering.getPaidAmount() + render(periods));

            // Nothing may go missing elsewhere either: the money is counted once, on the period that owns its date.
            BigDecimal paidAcrossSchedule = BigDecimal.ZERO;
            for (final WorkingCapitalLoanDelinquencyRangeScheduleData period : periods) {
                if (period.getPaidAmount() != null) {
                    paidAcrossSchedule = paidAcrossSchedule.add(period.getPaidAmount());
                }
            }
            assertEquals(0, REPAYMENT.compareTo(paidAcrossSchedule),
                    "the schedule must account for the repayment exactly once, but its periods total " + paidAcrossSchedule
                            + render(periods));
        });
    }

    @Test
    void testRepaymentBeforeFirstCobSatisfiesTheMinimumPaymentCriteria() {
        businessDateHelper.runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disburseLoan();

            businessDateHelper.updateBusinessDate("BUSINESS_DATE", REPAYMENT_DATE_ISO);
            wcLoanHelper.makeRepayment(loanId, WorkingCapitalLoanRequestBuilders.repayment(REPAYMENT, REPAYMENT_DATE));
            wcLoanHelper.executeInlineWCCOB(loanId);

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods = wcLoanHelper.getDelinquencyRangeSchedule(loanId);
            final WorkingCapitalLoanDelinquencyRangeScheduleData covering = periodCovering(periods, REPAYMENT_LOCAL_DATE);
            assertNotNull(covering.getMinPaymentCriteriaMet(),
                    "the period covering the repayment reports nothing about the minimum " + "payment criteria" + render(periods));
            assertTrue(covering.getMinPaymentCriteriaMet(),
                    "a repayment of " + REPAYMENT + " is well above the " + DELINQUENCY_MIN_PAYMENT_PERCENT + "% of " + PRINCIPAL
                            + " the period asks for, so it must satisfy the minimum payment criteria" + render(periods));
        });
    }

    private static WorkingCapitalLoanDelinquencyRangeScheduleData periodCovering(
            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods, final LocalDate date) {
        return periods.stream()
                .filter(period -> period.getFromDate() != null && period.getToDate() != null && !date.isBefore(period.getFromDate())
                        && !date.isAfter(period.getToDate()))
                .findFirst().orElseThrow(() -> new AssertionError("no delinquency period covers " + date + render(periods)));
    }

    private static String render(final List<WorkingCapitalLoanDelinquencyRangeScheduleData> periods) {
        final StringBuilder out = new StringBuilder("\ndelinquency schedule:\n");
        for (final WorkingCapitalLoanDelinquencyRangeScheduleData period : periods) {
            out.append("  #").append(period.getPeriodNumber()).append(' ').append(period.getFromDate()).append("..")
                    .append(period.getToDate()).append(" expected=").append(period.getExpectedAmount()).append(" paid=")
                    .append(period.getPaidAmount()).append(" outstanding=").append(period.getOutstandingAmount())
                    .append(" minPaymentCriteriaMet=").append(period.getMinPaymentCriteriaMet()).append('\n');
        }
        return out.toString();
    }

    private Long disburseLoan() {
        final Long clientId = clientHelper.createClient(DISBURSEMENT_DATE);
        final Long productId = createProductWithDelinquency();
        final Long loanId = wcLoanHelper.submitApplication(WorkingCapitalLoanRequestBuilders.submitApplication(clientId, productId,
                PRINCIPAL, RATE, DISBURSEMENT_DATE, DISBURSEMENT_DATE));
        createdLoanIds.add(loanId);
        wcLoanHelper.approve(loanId, WorkingCapitalLoanRequestBuilders.approve(DISBURSEMENT_DATE, PRINCIPAL, DISBURSEMENT_DATE));
        wcLoanHelper.disburse(loanId, WorkingCapitalLoanRequestBuilders.disburse(DISBURSEMENT_DATE, PRINCIPAL));
        return loanId;
    }

    private Long createProductWithDelinquency() {
        final String uniqueName = "WCL PreCobRepay " + Utils.uniqueRandomStringGenerator("", 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final PostDelinquencyRangeResponse range = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(60).locale("en"));
        final PostDelinquencyBucketResponse bucket = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(List.of(range.getResourceId()), DELINQUENCY_FREQUENCY_DAYS, 0,
                        DELINQUENCY_MIN_PAYMENT_PERCENT, 1);
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.randomStringGenerator("WC_BREACH_", 8),
                BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE, BREACH_MIN_PAYMENT_AMOUNT));
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder().withName(uniqueName)
                .withShortName(uniqueShortName).withDelinquencyBucketId(bucket.getResourceId())
                .withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS).withBreachId(breachId).build()).getResourceId();
        createdProductIds.add(productId);
        return productId;
    }
}
