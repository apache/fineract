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

import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.ExpectedRangeDelinquency.rangeDelinquency;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.ExpectedTag.tag;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateInstallmentLevelDelinquency;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateLoanLevelDelinquency;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateOneActiveTagPerPeriod;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validatePastDueDays;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalDelinquencyTestValidators.validateTagHistory;

import java.math.BigDecimal;
import java.util.List;
import org.apache.fineract.infrastructure.configuration.api.GlobalConfigurationConstants;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.apache.fineract.integrationtests.common.Utils;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * A Working Capital range-schedule period holds one active delinquency tag at a time (the previous one is lifted, and
 * kept in history, when the period ages into the next range), and GET loan reports {@code installmentLevelDelinquency}
 * once per range with the periods' delinquent amounts summed.
 *
 * <p>
 * Common setup: 15 DAYS bucket, 3% minimum payment, ranges R1 [1-30], R2 [31-60], R3 [61-90], principal 9000 disbursed
 * 2026-01-01, no repayments unless stated. Each period expects 270 (3% of 9000). Periods: P1 01-01..01-15, P2
 * 01-16..01-30, P3 01-31..02-14, P4 02-15..03-01, P5 03-02..03-16. The inline COB replays every skipped day, so a
 * period is tagged on the first day it enters a range (toDate + 1 / + 31 / + 61).
 *
 * <p>
 * Instant delinquency classification is pinned off for the class so a repayment is classified by the next COB, which is
 * what the repayment and cure dates assume; the previous value is restored afterwards.
 */
public class FeignWorkingCapitalLoanDelinquencyTagTest extends FeignWorkingCapitalTestBase {

    private static final String DISBURSEMENT_DATE = "01 January 2026";
    private static final String DISBURSEMENT_DATE_ISO = "2026-01-01";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);
    private static final int DELINQUENCY_FREQUENCY_DAYS = 15;
    private static final BigDecimal MINIMUM_PAYMENT_PERCENT = BigDecimal.valueOf(3);

    private Long r1Id;
    private Long r2Id;
    private Long r3Id;
    private String r1;
    private String r2;
    private String r3;
    private Long productId;
    private boolean instantClassificationWasEnabled;

    @BeforeAll
    void setupDelinquencyProduct() {
        r1 = Utils.uniqueRandomStringGenerator("WC_DELINQ_R1_", 6);
        r2 = Utils.uniqueRandomStringGenerator("WC_DELINQ_R2_", 6);
        r3 = Utils.uniqueRandomStringGenerator("WC_DELINQ_R3_", 6);
        r1Id = createDelinquencyRange(r1, 1, 30);
        r2Id = createDelinquencyRange(r2, 31, 60);
        r3Id = createDelinquencyRange(r3, 61, 90);
        productId = createWcProductWithDelinquencyBucket(DELINQUENCY_FREQUENCY_DAYS, MINIMUM_PAYMENT_PERCENT, List.of(r1Id, r2Id, r3Id));

        instantClassificationWasEnabled = isConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION);
        setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, false);
    }

    @AfterAll
    void restoreInstantClassification() {
        setConfigurationEnabled(GlobalConfigurationConstants.ENABLE_INSTANT_DELINQUENCY_CALCULATION, instantClassificationWasEnabled);
    }

    @Test
    @DisplayName("first delinquency adds one active tag and reports its 270 delinquent amount")
    void firstDelinquency_addsSingleActiveTagWithDelinquentAmount() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-01-16");

            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, "2026-01-16", null, r1));
            validateLoanLevelDelinquency(getDelinquentData(loanId), "270", 1, "2026-01-16");
            validateInstallmentLevelDelinquency(getDelinquentData(loanId), //
                    rangeDelinquency(r1Id, r1, 1, 30, "270"));
        });
    }

    @Test
    @DisplayName("escalation lifts the previous tag on the new tag's added date, one active tag per period")
    void escalation_liftsPreviousTagOnNewTagAddedDate() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-02-16");

            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(3, "2026-02-15", null, r1), //
                    tag(1, "2026-02-15", null, r2), //
                    tag(2, "2026-01-31", null, r1), //
                    tag(1, "2026-01-16", "2026-02-15", r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId), 1, 2, 3);
        });
    }

    @Test
    @DisplayName("installmentLevelDelinquency is grouped by range, amounts summed, sorted by minimum age")
    void installmentLevelDelinquency_isAggregatedByRange() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-02-16");

            validateInstallmentLevelDelinquency(getDelinquentData(loanId), //
                    rangeDelinquency(r1Id, r1, 1, 30, "540"), //
                    rangeDelinquency(r2Id, r2, 31, 60, "270"));
        });
    }

    @Test
    @DisplayName("loan-level delinquentDays / delinquentDate keep the oldest delinquency start after escalation")
    void loanLevelDelinquencyStart_isNotMovedByEscalation() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-02-16");

            validateLoanLevelDelinquency(getDelinquentData(loanId), "810", 32, "2026-01-16");
            validatePastDueDays(getDelinquentData(loanId), 32);
        });
    }

    @Test
    @DisplayName("a partial repayment refreshes the active tag's amount without adding a tag")
    void partialRepayment_refreshesActiveTagAmountWithoutNewTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-02-16");
            makeWcRepayment(loanId, BigDecimal.valueOf(100), "16 February 2026");
            advanceBusinessDateWithCob(loanId, "2026-02-16", "2026-02-17");

            validateInstallmentLevelDelinquency(getDelinquentData(loanId), //
                    rangeDelinquency(r1Id, r1, 1, 30, "540"), //
                    rangeDelinquency(r2Id, r2, 31, 60, "170"));
            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(3, "2026-02-15", null, r1), //
                    tag(1, "2026-02-15", null, r2), //
                    tag(2, "2026-01-31", null, r1), //
                    tag(1, "2026-01-16", "2026-02-15", r1));
            validateLoanLevelDelinquency(getDelinquentData(loanId), "710", 33, "2026-01-16");
        });
    }

    @Test
    @DisplayName("curing the escalated period lifts its only active tag; the loan start moves to the next period's chain")
    void cureOfEscalatedPeriod_liftsItsOnlyActiveTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-02-16");
            makeWcRepayment(loanId, BigDecimal.valueOf(100), "16 February 2026");
            advanceBusinessDateWithCob(loanId, "2026-02-16", "2026-02-17");
            makeWcRepayment(loanId, BigDecimal.valueOf(170), "17 February 2026");
            advanceBusinessDateWithCob(loanId, "2026-02-17", "2026-02-18");

            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(3, "2026-02-15", null, r1), //
                    tag(1, "2026-02-15", "2026-02-18", r2), //
                    tag(2, "2026-01-31", null, r1), //
                    tag(1, "2026-01-16", "2026-02-15", r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId), 2, 3);
            validateInstallmentLevelDelinquency(getDelinquentData(loanId), //
                    rangeDelinquency(r1Id, r1, 1, 30, "540"));
            validateLoanLevelDelinquency(getDelinquentData(loanId), "540", 19, "2026-01-31");
        });
    }

    @Test
    @DisplayName("a two-step escalation R1 -> R2 -> R3 leaves a lifted chain and a single active R3 tag")
    void twoStepEscalation_leavesLiftedChainAndSingleActiveTag() {
        runAt(DISBURSEMENT_DATE_ISO, () -> {
            final Long loanId = disbursedLoanAdvancedTo("2026-03-17");

            validateTagHistory(getDelinquencyTagHistory(loanId), //
                    tag(1, "2026-01-16", "2026-02-15", r1), //
                    tag(1, "2026-02-15", "2026-03-17", r2), //
                    tag(1, "2026-03-17", null, r3), //
                    tag(2, "2026-01-31", "2026-03-02", r1), //
                    tag(2, "2026-03-02", null, r2), //
                    tag(3, "2026-02-15", "2026-03-17", r1), //
                    tag(3, "2026-03-17", null, r2), //
                    tag(4, "2026-03-02", null, r1), //
                    tag(5, "2026-03-17", null, r1));
            validateOneActiveTagPerPeriod(getDelinquencyTagHistory(loanId), 1, 2, 3, 4, 5);
            validateInstallmentLevelDelinquency(getDelinquentData(loanId), //
                    rangeDelinquency(r1Id, r1, 1, 30, "540"), //
                    rangeDelinquency(r2Id, r2, 31, 60, "540"), //
                    rangeDelinquency(r3Id, r3, 61, 90, "270"));
            validateLoanLevelDelinquency(getDelinquentData(loanId), "1350", 61, "2026-01-16");
        });
    }

    private Long disbursedLoanAdvancedTo(String targetIso) {
        final Long clientId = createClient(DISBURSEMENT_DATE);
        final Long loanId = createApproveAndDisburseWcLoan(clientId, productId, PRINCIPAL, DISBURSEMENT_DATE);
        runInlineWcCob(loanId);
        advanceBusinessDateWithCob(loanId, DISBURSEMENT_DATE_ISO, targetIso);
        return loanId;
    }
}
