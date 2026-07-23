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
 * false) plus {@code delinquencyGraceDays} (the range schedule does not apply the grace days when generating
 * periods).</li>
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
    // Delinquency: 20-day frequency (no grace baked into the schedule) -> first period [D .. D+19].
    private static final int DELINQUENCY_FREQUENCY_DAYS = 20;
    private static final int DELINQUENCY_GRACE_DAYS = 3;

    private static final LocalDate DISBURSEMENT_DATE = LocalDate.of(2026, 1, 1);
    // Submitted-on date intentionally earlier than the disbursement date so the two anchors can be told apart.
    private static final LocalDate SUBMITTED_ON_DATE = LocalDate.of(2025, 12, 20);

    @Test
    public void testStartDatesArePopulatedWhenLoanBreachesAndBecomesDelinquent() {
        AtomicLong loanIdRef = new AtomicLong();

        BusinessDateHelper.runAt("01 January 2026", () -> {
            loanIdRef.set(createDisbursedLoan());
        });

        BusinessDateHelper.runAt("21 January 2026", () -> {
            final Long loanId = loanIdRef.get();
            ok(() -> FineractFeignClientHelper.getFineractFeignClient().inlineJob().executeInlineJob("WC_LOAN_COB",
                    new InlineJobRequest().addLoanIdsItem(loanId)));

            // then - both start dates are populated on the retrieveOne response
            final WorkingCapitalLoanHelper loanHelper = new WorkingCapitalLoanHelper();
            final GetWorkingCapitalLoansLoanIdResponse response = loanHelper.retrieveLoan(loanId);

            // breachStartDate = fromDate of the first breached period = disbursement + breachGraceDays (grace already
            // in schedule)
            assertEquals(DISBURSEMENT_DATE.plusDays(BREACH_GRACE_DAYS), response.getBreachStartDate(),
                    "breachStartDate should be the fromDate of the first breached period (disbursement + breachGraceDays)");

            // delinquencyStartDate = fromDate of the first delinquent period (= disbursement) + delinquencyGraceDays
            assertEquals(DISBURSEMENT_DATE.plusDays(DELINQUENCY_GRACE_DAYS), response.getDelinquencyStartDate(),
                    "delinquencyStartDate should be the fromDate of the first delinquent period plus delinquencyGraceDays");
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

            // delinquencyStartDate must anchor on the loan submitted-on date (creation), not the disbursement date,
            // plus the delinquencyGraceDays.
            assertEquals(SUBMITTED_ON_DATE.plusDays(DELINQUENCY_GRACE_DAYS), response.getDelinquencyStartDate(),
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

            // breachStartDate must anchor on the loan submitted-on (creation) date, not the disbursement date, plus the
            // breachGraceDays.
            assertEquals(SUBMITTED_ON_DATE.plusDays(BREACH_GRACE_DAYS), response.getBreachStartDate(),
                    "breachStartDate should anchor on submittedOnDate + breachGraceDays when breachStartType = LOAN_CREATION");
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

            assertEquals(DISBURSEMENT_DATE.plusDays(BREACH_GRACE_DAYS), response.getBreachStartDate(),
                    "breachStartDate should anchor on disbursementDate + breachGraceDays when breachStartType = DISBURSEMENT");
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
        });
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
