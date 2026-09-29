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

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicLong;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.client.models.DelinquencyRangeRequest;
import org.apache.fineract.client.models.GetWorkingCapitalLoansLoanIdResponse;
import org.apache.fineract.client.models.InlineJobRequest;
import org.apache.fineract.client.models.PostDelinquencyBucketResponse;
import org.apache.fineract.client.models.PostDelinquencyRangeResponse;
import org.apache.fineract.client.models.PostWorkingCapitalLoansDelinquencyActionRequest;
import org.apache.fineract.client.models.WorkingCapitalLoanDelinquencyRangeScheduleData;
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyActionHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanproduct.WorkingCapitalLoanProductTestBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;

/**
 * Validates the {@code breachStartDate} and {@code delinquencyStartDate} fields populated by
 * {@code WorkingCapitalLoanApplicationReadPlatformServiceImpl.enrichWithStartDates} on the {@code GET
 * /workingcapitalloans/{loanId}} response.
 *
 * <ul>
 * <li>{@code breachStartDate} = fromDate of the earliest breached breach-schedule period. The breach schedule already
 * offsets its first period by {@code breachGraceDays}, so the grace is reflected in the fromDate.</li>
 * <li>{@code delinquencyStartDate} = fromDate of the earliest delinquent range-schedule period (minPaymentCriteriaMet =
 * false). The range schedule bakes {@code delinquencyGraceDays} into the toDate of its first period, so the fromDate is
 * the raw anchor date.</li>
 * <li>{@code delinquencyEffectiveStartDate} = {@code delinquencyStartDate} shifted by {@code delinquencyGraceDays}, set
 * only when the earliest delinquent period is the first one and grace days are configured.</li>
 * </ul>
 */
@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class WorkingCapitalLoanStartDatesTest {

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(10000);
    private static final BigDecimal TOTAL_PAYMENT_VOLUME = BigDecimal.valueOf(100000);
    private static final BigDecimal BREACH_AMOUNT = new BigDecimal("500");
    private static final BigDecimal DELINQUENCY_MIN_PAYMENT_PERCENT = new BigDecimal("3");

    // Breach: 15-day frequency with a 5-day grace -> first period [D+5 .. D+19].
    private static final int BREACH_FREQUENCY_DAYS = 15;
    private static final int BREACH_GRACE_DAYS = 5;
    // Delinquency: 20-day frequency, with the grace days baked into the first period -> first period [D .. D+19+grace].
    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);
    // First delinquency period is [D .. D+19+grace] = [2026-01-01 .. 2026-01-23]; the effective start is D + grace.
    private static final LocalDate DELINQUENCY_EFFECTIVE_START_DATE = LocalDate.of(2026, 1, 4);
    // Second delinquency period starts the day after the first one ends and carries no grace days.
    private static final LocalDate SECOND_PERIOD_FROM_DATE = LocalDate.of(2026, 1, 24);
    // 3% of the 10000 principal: the minimum payment that makes a delinquency period meet its criteria.
    private static final BigDecimal MINIMUM_PAYMENT = new BigDecimal("300");
    // Frequency the reschedule scenario shortens the first period to, deliberately shorter than the grace days.
    private static final int RESCHEDULED_FREQUENCY_DAYS = 2;
    // First period re-dated by that reschedule: [D .. D+1+grace] = [2026-01-01 .. 2026-01-05].
    private static final LocalDate RESCHEDULED_FIRST_PERIOD_TO_DATE = LocalDate.of(2026, 1, 5);
    // Submitted-on date intentionally earlier than the disbursement date so the two anchors can be told apart.
    private static final LocalDate SUBMITTED_ON_DATE = LocalDate.of(2025, 12, 20);
    // Effective start under the LOAN_CREATION anchor: submittedOnDate + delinquencyGraceDays.
    private static final LocalDate SUBMITTED_ON_EFFECTIVE_START_DATE = LocalDate.of(2025, 12, 23);

    @Test
    public void testStartDatesArePopulatedWhenLoanBreachesAndBecomesDelinquent() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        BusinessDateHelper.runAt("26 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            // then - both start dates are populated on the retrieveOne response
            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            // breachStartDate = fromDate of the first breached period = disbursement
            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(),
                    "breachStartDate should be the fromDate of the first breached period (disbursement)");

            // delinquencyStartDate = fromDate of the first delinquent period (= disbursement)
            assertEquals(DISBURSEMENT_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should be the fromDate of the first delinquent period");

            // delinquencyEffectiveStartDate = delinquencyStartDate + delinquencyGraceDays, since the delinquent
            // period is the first one.
            assertEquals(DELINQUENCY_EFFECTIVE_START_DATE, response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate should be the fromDate of the first delinquent period plus delinquencyGraceDays");
        });
    }

    @Test
    public void testDelinquencyStartDateUsesLoanCreationDateWhenConfigured() {
        AtomicLong loanIdRef = new AtomicLong();

        // given - a WC loan submitted on 2025-12-20 but disbursed on 2026-01-01, with delinquencyStartType =
        // LOAN_CREATION
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan(SUBMITTED_ON_DATE, "LOAN_CREATION"));
        });

        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            // delinquencyStartDate must anchor on the loan submitted-on date (creation), not the disbursement date.
            assertEquals(SUBMITTED_ON_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should anchor on submittedOnDate when delinquencyStartType = LOAN_CREATION");

            // The effective start date follows the same anchor, shifted by the delinquency grace days.
            assertEquals(SUBMITTED_ON_EFFECTIVE_START_DATE, response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate should be submittedOnDate + delinquencyGraceDays when delinquencyStartType = LOAN_CREATION");
        });
    }

    // Breach anchor = LOAN_CREATION -> breachStartDate must be submittedOnDate (2025-12-20) + breachGraceDays (5) =
    // 2025-12-25 (rather than anchoring on the disbursement date 2026-01-01).
    @Test
    public void testBreachStartDateUsesLoanCreationDateWhenConfigured() {
        AtomicLong loanIdRef = new AtomicLong();

        // given - a WC loan submitted on 2025-12-20 but disbursed on 2026-01-01, with breachStartType = LOAN_CREATION
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoanWithBreachStartType(SUBMITTED_ON_DATE, "LOAN_CREATION"));
        });

        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            // breachStartDate must anchor on the loan submitted-on (creation) date, not the disbursement date.
            assertEquals(SUBMITTED_ON_DATE, response.getBreachStartDate(),
                    "breachStartDate should anchor on submittedOnDate when breachStartType = LOAN_CREATION");
        });
    }

    // Breach anchor = DISBURSEMENT (explicit) -> breachStartDate must be disbursementDate (2026-01-01) +
    // breachGraceDays
    // (5) = 2026-01-06.
    @Test
    public void testBreachStartDateUsesDisbursementDateWhenConfigured() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoanWithBreachStartType(null, "DISBURSEMENT"));
        });

        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(),
                    "breachStartDate should anchor on disbursementDate when breachStartType = DISBURSEMENT");
        });
    }

    @Test
    public void testStartDatesAreNullForHealthyLoan() {
        BusinessDateHelper.runAt("01 January 2026", () -> {
            // given - a disbursed WC loan with breach + delinquency configuration
            final Long loanId = createDisbursedLoan();

            // when - run the WC COB on the disbursement date, before any period has expired
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            // then - neither start date is set while the loan is healthy
            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertNull(response.getBreachStartDate(), "breachStartDate must be null when the loan is not in breach");
            assertNull(response.getDelinquencyStartDate(), "delinquencyStartDate must be null when the loan is not delinquent");
            assertNull(response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate must be null when the loan is not delinquent");
        });
    }

    @Test
    public void testDelinquencyEffectiveStartDateIsNullWhenNoGraceDaysConfigured() {
        AtomicLong loanIdRef = new AtomicLong();

        // given - the same setup but with delinquencyGraceDays = 0, so there is no cool off period to expose
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan(null, null, 0));
        });

        BusinessDateHelper.runAt("26 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(DISBURSEMENT_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should be the fromDate of the first delinquent period");
            assertNull(response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate must be null when no delinquency grace days are configured");
        });
    }

    @Test
    public void testDelinquencyEffectiveStartDateIsNullWhenTheDelinquentPeriodIsNotTheFirstOne() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        // Meet the minimum payment of the first period [2026-01-01 .. 2026-01-23] so it is never delinquent.
        BusinessDateHelper.runAt("05 January 2026", () -> {
            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            loanHelper.makeRepaymentByLoanId(loanIdRef.get(), WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), MINIMUM_PAYMENT, null, "repayment", 1, null));
        });

        // The second period [2026-01-24 .. 2026-02-12] goes unpaid and is the earliest delinquent one.
        BusinessDateHelper.runAt("13 February 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(SECOND_PERIOD_FROM_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should be the fromDate of the second period once the first one is met");
            assertNull(response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate must be null when the delinquent period is not the first one, "
                            + "which is the only one the grace days shift");
        });
    }

    /**
     * A reschedule re-dates the first period from its own fromDate, and the grace days have to be re-applied when it
     * does: they belong to the first period, not to the frequency it was created with. Rescheduling to a frequency
     * shorter than the grace days is what makes the difference observable - without the grace days the period would end
     * on 2026-01-02, before the effective start date the read model reports.
     */
    @Test
    public void testFirstPeriodKeepsItsGraceDaysAfterAFrequencyReschedule() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        // The first period is still open on this date, so the reschedule re-dates it instead of a later one.
        BusinessDateHelper.runAt("02 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            final PostWorkingCapitalLoansDelinquencyActionRequest request = WorkingCapitalLoanDelinquencyActionHelper
                    .buildActionRequest("reschedule", LocalDate.of(2026, 1, 2), null);
            request.setFrequency(RESCHEDULED_FREQUENCY_DAYS);
            request.setFrequencyType("DAYS");
            WorkingCapitalLoanDelinquencyActionHelper.createDelinquencyAction(loanId, request);

            final List<WorkingCapitalLoanDelinquencyRangeScheduleData> schedule = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                    .getDelinquencyRangeSchedule(loanId);
            final WorkingCapitalLoanDelinquencyRangeScheduleData firstPeriod = schedule.stream()
                    .filter(period -> Integer.valueOf(1).equals(period.getPeriodNumber())).findFirst().orElseThrow();

            assertEquals(DISBURSEMENT_DATE, firstPeriod.getFromDate(), "the reschedule must not move the first period start");
            assertEquals(RESCHEDULED_FIRST_PERIOD_TO_DATE, firstPeriod.getToDate(),
                    "the rescheduled first period must still carry the delinquency grace days");
        });

        // The day after the rescheduled period ends, COB evaluates it and the loan turns delinquent on it.
        BusinessDateHelper.runAt("06 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(DISBURSEMENT_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should still be the fromDate of the rescheduled first period");
            // The read model derives the effective start from the same grace days the rescheduled period kept, so it
            // stays inside that period instead of falling after its end date.
            assertEquals(DELINQUENCY_EFFECTIVE_START_DATE, response.getDelinquencyEffectiveStartDate(),
                    "delinquencyEffectiveStartDate should be unchanged by the reschedule and fall inside the period");
        });
    }

    private Long createDisbursedLoan() {
        // Default: submitted-on date left unset (defaults to the disbursement date) and no explicit
        // delinquencyStartType.
        return createDisbursedLoan(null, null);
    }

    private Long createDisbursedLoan(final LocalDate submittedOnDate, final String delinquencyStartType) {
        return createDisbursedLoan(submittedOnDate, delinquencyStartType, DELINQUENCY_GRACE_DAYS);
    }

    private Long createDisbursedLoan(final LocalDate submittedOnDate, final String delinquencyStartType, final int delinquencyGraceDays) {
        // Delinquency bucket with a percentage minimum payment and a 20-day frequency.
        final List<Long> rangeIds = createDelinquencyRanges();
        final PostDelinquencyBucketResponse bucketResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds, DELINQUENCY_FREQUENCY_DAYS, 0, DELINQUENCY_MIN_PAYMENT_PERCENT, 1);
        assertNotNull(bucketResponse);

        // Breach with a flat amount and a 15-day frequency.
        final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.uniqueRandomStringGenerator("WCL_Breach_", 6),
                BREACH_FREQUENCY_DAYS, "DAYS", "FLAT", BREACH_AMOUNT));
        assertNotNull(breachId);

        // Product wiring breach + delinquency, with distinct grace days for each.
        final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withDelinquencyBucketId(bucketResponse.getResourceId()) //
                .withDelinquencyGraceDays(delinquencyGraceDays) //
                .withDelinquencyStartType(delinquencyStartType) //
                .withBreachId(breachId) //
                .withBreachGraceDays(BREACH_GRACE_DAYS) //
                .build()).getResourceId();
        assertNotNull(productId);

        // Client + loan application.
        final Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
        final Long loanId = loanHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(PRINCIPAL) //
                .withSubmittedOnDate(submittedOnDate) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(TOTAL_PAYMENT_VOLUME) //
                .buildSubmitRequest());
        assertNotNull(loanId);

        // Approve and disburse on the same date so the schedules anchor on DISBURSEMENT_DATE.
        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(DISBURSEMENT_DATE, PRINCIPAL, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(DISBURSEMENT_DATE, PRINCIPAL));
        log.info("Created disbursed WC loan {} for start-date validation", loanId);
        return loanId;
    }

    /**
     * Mirrors {@link #createDisbursedLoan(LocalDate, String)} but configures the product with an explicit breach
     * start-date-type anchor.
     */
    private Long createDisbursedLoanWithBreachStartType(final LocalDate submittedOnDate, final String breachStartType) {
        final List<Long> rangeIds = createDelinquencyRanges();
        final PostDelinquencyBucketResponse bucketResponse = WorkingCapitalLoanDelinquencyRangeScheduleHelper
                .createWorkingCapitalLoanDelinquencyBucket(rangeIds, DELINQUENCY_FREQUENCY_DAYS, 0, DELINQUENCY_MIN_PAYMENT_PERCENT, 1);
        assertNotNull(bucketResponse);

        final WorkingCapitalBreachHelper breachHelper = new WorkingCapitalBreachHelper();
        final Long breachId = breachHelper.create(breachHelper.createBreachRequest(Utils.uniqueRandomStringGenerator("WCL_Breach_", 6),
                BREACH_FREQUENCY_DAYS, "DAYS", "FLAT", BREACH_AMOUNT));
        assertNotNull(breachId);

        final WorkingCapitalLoanProductHelper productHelper = new WorkingCapitalLoanProductHelper();
        final String uniqueName = "WCL Product " + UUID.randomUUID().toString().substring(0, 8);
        final String uniqueShortName = Utils.uniqueRandomStringGenerator("", 4);
        final Long productId = productHelper.createWorkingCapitalLoanProduct(new WorkingCapitalLoanProductTestBuilder() //
                .withName(uniqueName) //
                .withShortName(uniqueShortName) //
                .withDelinquencyBucketId(bucketResponse.getResourceId()) //
                .withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS) //
                .withBreachId(breachId) //
                .withBreachGraceDays(BREACH_GRACE_DAYS) //
                .withBreachStartType(breachStartType) //
                .build()).getResourceId();
        assertNotNull(productId);

        final Long clientId = ClientHelper.createClient(ClientHelper.defaultClientCreationRequest()).getClientId();
        final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
        final Long loanId = loanHelper.submit(new WorkingCapitalLoanApplicationTestBuilder() //
                .withClientId(clientId) //
                .withProductId(productId) //
                .withPrincipal(PRINCIPAL) //
                .withSubmittedOnDate(submittedOnDate) //
                .withPeriodPaymentRate(WorkingCapitalLoanProductTestBuilder.DEFAULT_PERIOD_PAYMENT_RATE_PERCENT) //
                .withTotalPaymentVolume(TOTAL_PAYMENT_VOLUME) //
                .buildSubmitRequest());
        assertNotNull(loanId);

        loanHelper.approveById(loanId, WorkingCapitalLoanApplicationTestBuilder.buildApproveRequest(DISBURSEMENT_DATE, PRINCIPAL, null));
        loanHelper.disburseById(loanId, WorkingCapitalLoanDisbursementTestBuilder.buildDisburseRequest(DISBURSEMENT_DATE, PRINCIPAL));
        log.info("Created disbursed WC loan {} with breachStartType={} for start-date validation", loanId, breachStartType);
        return loanId;
    }

    private List<Long> createDelinquencyRanges() {
        final PostDelinquencyRangeResponse range1 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(1).maximumAgeDays(30).locale("en"));
        final PostDelinquencyRangeResponse range2 = DelinquencyRangesHelper.createRange(new DelinquencyRangeRequest()
                .classification(Utils.randomStringGenerator("DLQ_R_", 10)).minimumAgeDays(31).maximumAgeDays(60).locale("en"));
        return List.of(range1.getResourceId(), range2.getResourceId());
    }
}
