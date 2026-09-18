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
import org.apache.fineract.integrationtests.common.BusinessDateHelper;
import org.apache.fineract.integrationtests.common.ClientHelper;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.Utils;
import org.apache.fineract.integrationtests.common.loans.LoanTestLifecycleExtension;
import org.apache.fineract.integrationtests.common.products.DelinquencyRangesHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanApplicationTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDelinquencyRangeScheduleHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanDisbursementTestBuilder;
import org.apache.fineract.integrationtests.common.workingcapitalloan.WorkingCapitalLoanHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalBreachHelper;
import org.apache.fineract.integrationtests.common.workingcapitalloanbreach.WorkingCapitalLoanBreachActionHelper;
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
 * <li>{@code breachStartDate} = fromDate of the earliest breached breach-schedule period. The breach schedule bakes
 * {@code breachGraceDays} into the toDate of its first period, so the fromDate is the raw anchor date.</li>
 * <li>{@code breachEffectiveStartDate} = {@code breachStartDate} shifted by {@code breachGraceDays}, set only when the
 * earliest breached period is the first one, grace days are configured, and the resulting date still falls inside that
 * period (an operation that cuts the period short, such as a reset, leaves no cool off period to report).</li>
 * <li>{@code delinquencyStartDate} = fromDate of the earliest delinquent range-schedule period (minPaymentCriteriaMet =
 * false). The range schedule bakes {@code delinquencyGraceDays} into the toDate of its first period, so the fromDate is
 * the raw anchor date.</li>
 * </ul>
 */
@Slf4j
@ExtendWith(LoanTestLifecycleExtension.class)
public class WorkingCapitalLoanStartDatesTest {

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(10000);
    private static final BigDecimal TOTAL_PAYMENT_VOLUME = BigDecimal.valueOf(100000);
    private static final BigDecimal BREACH_AMOUNT = new BigDecimal("500");
    private static final BigDecimal DELINQUENCY_MIN_PAYMENT_PERCENT = new BigDecimal("3");

    // Breach: 15-day frequency with a 5-day grace baked into the end of the first period -> [D .. D+14+grace].
    private static final int BREACH_FREQUENCY_DAYS = 15;
    private static final int BREACH_GRACE_DAYS = 5;
    // Delinquency: 20-day frequency (no grace baked into the schedule) -> first period [D .. D+19].
    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);
    // First breach period is [D .. D+14+grace] = [2026-01-01 .. 2026-01-20]; the effective start is D + grace.
    private static final LocalDate BREACH_EFFECTIVE_START_DATE = LocalDate.of(2026, 1, 6);
    // Second breach period starts the day after the first one ends and carries no grace days.
    private static final LocalDate SECOND_BREACH_PERIOD_FROM_DATE = LocalDate.of(2026, 1, 21);
    // Submitted-on date intentionally earlier than the disbursement date so the two anchors can be told apart.
    private static final LocalDate SUBMITTED_ON_DATE = LocalDate.of(2025, 12, 20);
    // Same shift applied to the submitted-on anchor: 2025-12-20 + 5 breach grace days.
    private static final LocalDate BREACH_EFFECTIVE_START_DATE_FROM_CREATION = LocalDate.of(2025, 12, 25);
    // Reset date deliberately inside the grace window [2026-01-01 .. 2026-01-06).
    private static final String RESET_DATE = "2026-01-03";
    // The reset cuts the first period the day before it: [2026-01-01 .. 2026-01-02].
    private static final LocalDate RESET_FIRST_PERIOD_TO_DATE = LocalDate.of(2026, 1, 2);
    // Natural end of the first period, restored when the reset is undone.
    private static final LocalDate NATURAL_FIRST_PERIOD_TO_DATE = LocalDate.of(2026, 1, 20);
    // Frequency the reschedule scenario shortens the first period to, deliberately shorter than the grace days.
    private static final int RESCHEDULED_FREQUENCY_DAYS = 2;
    // First period re-dated by that reschedule: [D .. D+1+grace] = [2026-01-01 .. 2026-01-07].
    private static final LocalDate RESCHEDULED_FIRST_PERIOD_TO_DATE = LocalDate.of(2026, 1, 7);

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

            // breachEffectiveStartDate = breachStartDate + breachGraceDays, since the breached period is the first one.
            assertEquals(BREACH_EFFECTIVE_START_DATE, response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate should be the fromDate of the first breached period plus breachGraceDays");

            // delinquencyStartDate = fromDate of the first delinquent period (= disbursement)
            assertEquals(DISBURSEMENT_DATE, response.getDelinquencyStartDate(),
                    "delinquencyStartDate should be the fromDate of the first delinquent period");
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
                    "delinquencyStartDate should anchor on submittedOnDate + delinquencyGraceDays when delinquencyStartType = LOAN_CREATION");
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
            assertEquals(BREACH_EFFECTIVE_START_DATE_FROM_CREATION, response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate should shift the submitted-on anchor by breachGraceDays");
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
            assertEquals(BREACH_EFFECTIVE_START_DATE, response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate should shift the disbursement anchor by breachGraceDays");
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
            assertNull(response.getBreachEffectiveStartDate(), "breachEffectiveStartDate must be null when the loan is not in breach");
            assertNull(response.getDelinquencyStartDate(), "delinquencyStartDate must be null when the loan is not delinquent");
        });
    }

    @Test
    public void testBreachEffectiveStartDateIsNullWhenNoGraceDaysConfigured() {
        AtomicLong loanIdRef = new AtomicLong();

        // given - the same setup but with breachGraceDays = 0, so there is no cool off period to expose
        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoanWithBreachStartType(null, "DISBURSEMENT", 0));
        });

        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(),
                    "breachStartDate should be the fromDate of the first breached period");
            assertNull(response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate must be null when no breach grace days are configured");
        });
    }

    @Test
    public void testBreachEffectiveStartDateIsNullWhenTheBreachedPeriodIsNotTheFirstOne() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        // Cover the minimum payment of the first breach period [2026-01-01 .. 2026-01-20] so it never breaches.
        BusinessDateHelper.runAt("05 January 2026", () -> {
            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            loanHelper.makeRepaymentByLoanId(loanIdRef.get(), WorkingCapitalLoanDisbursementTestBuilder
                    .buildRepaymentRequest(LocalDate.of(2026, 1, 5), BREACH_AMOUNT, null, "repayment", 1, null));
        });

        // The second period [2026-01-21 .. 2026-02-04] goes unpaid and is the earliest breached one.
        BusinessDateHelper.runAt("05 February 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            assertEquals(SECOND_BREACH_PERIOD_FROM_DATE, response.getBreachStartDate(),
                    "breachStartDate should be the fromDate of the second period once the first one is covered");
            assertNull(response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate must be null when the breached period is not the first one, "
                            + "which is the only one the grace days shift");
        });
    }

    /**
     * A reset that restarts the schedule cuts the first period short at the reset date, and the cut can land before the
     * grace days are over. The effective start date then describes a cool off period that never took place, so there is
     * none to report.
     */
    @Test
    public void testBreachEffectiveStartDateIsNullWhenAResetCutsTheFirstPeriodInsideTheGraceWindow() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        // The reset lands inside the grace window, so the first period ends before the cool off period would.
        BusinessDateHelper.runAt("03 January 2026", () -> {
            new WorkingCapitalLoanBreachActionHelper().resetRestartingFromResetDate(loanIdRef.get(), RESET_DATE);
        });

        BusinessDateHelper.runAt("04 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanBreachActionHelper actionHelper = new WorkingCapitalLoanBreachActionHelper();
            assertEquals(RESET_FIRST_PERIOD_TO_DATE, firstBreachPeriodToDate(actionHelper, loanId),
                    "the reset should have cut the first period the day before the reset date");

            final GetWorkingCapitalLoansLoanIdResponse response = new WorkingCapitalLoanHelper().retrieveLoan(loanId);
            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(),
                    "breachStartDate should still be the fromDate of the cut first period");
            assertNull(response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate must be null when the grace days outlast the period they belong to");
        });
    }

    @Test
    public void testBreachEffectiveStartDateComesBackWhenTheResetIsUndone() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        BusinessDateHelper.runAt("03 January 2026", () -> {
            final WorkingCapitalLoanBreachActionHelper actionHelper = new WorkingCapitalLoanBreachActionHelper();
            actionHelper.resetRestartingFromResetDate(loanIdRef.get(), RESET_DATE);
            // The undo rebuilds the first period from its natural length, which carries the grace days again.
            actionHelper.undoReset(loanIdRef.get(), RESET_DATE);
        });

        // The day after the restored period ends, it is breached again and the cool off period is reportable.
        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final WorkingCapitalLoanBreachActionHelper actionHelper = new WorkingCapitalLoanBreachActionHelper();
            assertEquals(NATURAL_FIRST_PERIOD_TO_DATE, firstBreachPeriodToDate(actionHelper, loanId),
                    "the undo should have restored the natural end date of the first period");

            final GetWorkingCapitalLoansLoanIdResponse response = new WorkingCapitalLoanHelper().retrieveLoan(loanId);
            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(), "breachStartDate should be the fromDate of the first period");
            assertEquals(BREACH_EFFECTIVE_START_DATE, response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate should be reportable again once the period carries its grace days");
        });
    }

    /**
     * A reschedule re-dates the first period from its own fromDate, and the grace days belong to that period rather
     * than to the frequency it was created with, so they survive the new frequency.
     */
    @Test
    public void testFirstPeriodKeepsItsGraceDaysAfterAFrequencyReschedule() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        BusinessDateHelper.runAt("02 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            final WorkingCapitalLoanBreachActionHelper actionHelper = new WorkingCapitalLoanBreachActionHelper();
            actionHelper.rescheduleFrequency(loanId, "2026-01-02", RESCHEDULED_FREQUENCY_DAYS, "DAYS");

            assertEquals(RESCHEDULED_FIRST_PERIOD_TO_DATE, firstBreachPeriodToDate(actionHelper, loanId),
                    "the rescheduled first period must still carry the breach grace days");
        });

        BusinessDateHelper.runAt("08 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            final GetWorkingCapitalLoansLoanIdResponse response = new WorkingCapitalLoanHelper().retrieveLoan(loanId);
            assertEquals(DISBURSEMENT_DATE, response.getBreachStartDate(),
                    "breachStartDate should still be the fromDate of the rescheduled first period");
            assertEquals(BREACH_EFFECTIVE_START_DATE, response.getBreachEffectiveStartDate(),
                    "breachEffectiveStartDate should be unchanged by the reschedule and fall inside the period");
        });
    }

    private LocalDate firstBreachPeriodToDate(final WorkingCapitalLoanBreachActionHelper actionHelper, final Long loanId) {
        return actionHelper.retrieveBreachSchedule(loanId).stream().filter(period -> Integer.valueOf(1).equals(period.getPeriodNumber()))
                .findFirst().orElseThrow().getToDate();
    }

    private Long createDisbursedLoan() {
        // Default: submitted-on date left unset (defaults to the disbursement date) and no explicit
        // delinquencyStartType.
        return createDisbursedLoan(null, null);
    }

    private Long createDisbursedLoan(final LocalDate submittedOnDate, final String delinquencyStartType) {
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
                .withDelinquencyGraceDays(DELINQUENCY_GRACE_DAYS) //
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
        return createDisbursedLoanWithBreachStartType(submittedOnDate, breachStartType, BREACH_GRACE_DAYS);
    }

    private Long createDisbursedLoanWithBreachStartType(final LocalDate submittedOnDate, final String breachStartType,
            final int breachGraceDays) {
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
                .withBreachGraceDays(breachGraceDays) //
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
