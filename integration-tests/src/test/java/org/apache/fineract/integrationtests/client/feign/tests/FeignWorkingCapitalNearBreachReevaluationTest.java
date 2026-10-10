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

import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.ExpectedNearBreachState.nearBreachState;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.validateNearBreachStates;
import static org.junit.jupiter.api.Assertions.assertEquals;

import java.math.BigDecimal;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Advancing the business date to X with COB evaluates as of X - 1, so a near breach checkpoint elapses once its day has
 * closed.
 */
public class FeignWorkingCapitalNearBreachReevaluationTest extends FeignWorkingCapitalTestBase {

    private static final String NEAR_BREACH_EVENT = "WorkingCapitalLoanNearBreachChangeBusinessEvent";

    private static final int BREACH_FREQUENCY = 6;
    private static final String BREACH_FREQUENCY_TYPE = "DAYS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "FLAT";
    private static final BigDecimal BREACH_AMOUNT = BigDecimal.valueOf(90);
    private static final int BREACH_GRACE_DAYS = 0;
    private static final BigDecimal NEAR_BREACH_THRESHOLD = BigDecimal.valueOf(40);
    private static final int NEAR_BREACH_FREQUENCY = 2;
    private static final String NEAR_BREACH_FREQUENCY_TYPE = "DAYS";
    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(9000);

    @BeforeAll
    void enableNearBreachEvent() {
        enableExternalEvent(NEAR_BREACH_EVENT);
    }

    @AfterAll
    void disableNearBreachEvent() {
        disableExternalEvent(NEAR_BREACH_EVENT);
    }

    @Test
    @DisplayName("a repayment that catches up with the elapsed checkpoint clears near breach immediately and emits one event")
    void repaymentCatchingUpClearsNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("COB re-raises near breach at the next checkpoint after a repayment cleared it")
    void cobReraisesNearBreachAfterRepaymentCleared() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");

            deleteAllExternalEvents();
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-05");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", true, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("undoing the catching-up repayment re-raises near breach immediately and emits one event")
    void undoOfRepaymentReraisesNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            final Long repaymentId = makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", false, null));

            deleteAllExternalEvents();
            undoWcTransaction(loanId, repaymentId);

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a backdated repayment into the open period counts towards cumulative paid and clears near breach")
    void backdatedRepaymentIntoOpenPeriodClearsNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-05");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(80), "02 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "80", "10", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a payment before the first checkpoint leaves near breach null and emits no event")
    void paymentBeforeFirstCheckpointLeavesNearBreachNull() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(10), "01 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "10", "80", null, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("a partial payment on the checkpoint day, before that day's COB, leaves near breach null")
    void paymentOnCheckpointDayBeforeItClosedLeavesNearBreachNull() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-02");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", null, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(10), "02 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "10", "80", null, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("a backdated payment into a closed period leaves that period's near breach frozen")
    void backdatedPaymentIntoClosedPeriodLeavesNearBreachFrozen() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-08");
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-06", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-07", "2026-01-12", "0", "90", null, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(80), "05 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "80", "10", true, true), //
                    nearBreachState(2, "0", "90", null, null));
            assertNearBreachEvents(loanId, 0);

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(90), "08 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "80", "10", true, true), //
                    nearBreachState(2, "90", "0", null, false));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("a reset with restart re-derives the re-dated period's stale near breach from the replayed paid amount")
    void resetWithRestartRederivesStaleNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-05");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", true, null));

            deleteAllExternalEvents();
            createBreachResetWithRestartPeriod(loanId);

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-04", "40", "50", false, true), //
                    nearBreachState(2, "2026-01-05", "2026-01-10", "0", "90", null, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("no near breach change while breach evaluation is disabled, then ENABLE reprocessing re-derives it")
    void disabledBreachEvaluationThenEnableReprocessRederives() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));
            createBreachDisable(loanId, "03 January 2026");

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));
            assertNearBreachEvents(loanId, 0);

            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-04");
            deleteAllExternalEvents();
            createBreachEnable(loanId, "04 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a repayment on the ENABLE day that catches up with the elapsed checkpoint clears near breach immediately")
    void repaymentOnTheEnableDayClearsNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            createBreachDisable(loanId, "03 January 2026");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-04");
            createBreachEnable(loanId, "04 January 2026");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "04 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("undoing a restart reset re-derives the reopened period and emits one event for the changed open value")
    void undoOfRestartResetRederivesTheReopenedPeriod() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-05");
            createBreachResetWithRestartPeriod(loanId);
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-04", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-05", "2026-01-10", "0", "90", null, null));

            deleteAllExternalEvents();
            createBreachUndoReset(loanId);

            assertEquals(1, getBreachSchedule(loanId).size(), "the undo must delete the restarted period 2");
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-06", "0", "90", true, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("re-evaluation uses the loan-level RESCHEDULE threshold over the product configuration")
    void loanLevelRescheduleOverridesProductConfiguration() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            createNearBreachReschedule(loanId, BigDecimal.valueOf(80), NEAR_BREACH_FREQUENCY, NEAR_BREACH_FREQUENCY_TYPE);
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "03 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", true, null));
            assertNearBreachEvents(loanId, 0);

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(35), "03 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "75", "15", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("COB without monetary activity raises near breach once and closes the period out unchanged")
    void cobWithoutMonetaryActivityIsUnchanged() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();

            deleteAllExternalEvents();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));
            assertNearBreachEvents(loanId, 1);

            deleteAllExternalEvents();
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-08");

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-06", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-07", "2026-01-12", "0", "90", null, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("COB resolves a satisfied checkpoint to false, then raises at the missed next checkpoint")
    void cobResolvesSatisfiedCheckpointToFalse() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "01 January 2026");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", null, null));

            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", false, null));

            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-05");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "40", "50", true, null));
        });
    }

    @Test
    @DisplayName("a repayment that leaves the resolved value unchanged emits no near breach event")
    void noOpReevaluationEmitsNoEvent() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");

            deleteAllExternalEvents();
            makeWcRepayment(loanId, BigDecimal.valueOf(10), "03 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "10", "80", true, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("undoing a loan-closing repayment re-opens the loan and re-raises near breach")
    void undoOfLoanClosingRepaymentReraisesNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");

            deleteAllExternalEvents();
            final Long repaymentId = makeWcRepayment(loanId, PRINCIPAL, "03 January 2026");

            assertEquals(Boolean.TRUE, getWcLoanDetails(loanId).getStatus().getClosedObligationsMet(),
                    "the 9000 repayment must close the loan");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "9000", "0", false, false));
            assertNearBreachEvents(loanId, 1);

            deleteAllExternalEvents();
            undoWcTransaction(loanId, repaymentId);

            assertEquals(Boolean.TRUE, getWcLoanDetails(loanId).getStatus().getActive(), "the undo must re-open the loan");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a pause replay that moves the checkpoints re-derives near breach and emits one event")
    void pauseReplayRederivesNearBreach() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-07");
            makeWcRepayment(loanId, BigDecimal.valueOf(40), "07 January 2026");
            advanceBusinessDateWithCob(loanId, "2026-01-07", "2026-01-09");
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-06", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-07", "2026-01-12", "40", "50", false, null));
            advanceBusinessDateWithCob(loanId, "2026-01-09", "2026-01-11");
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "0", "90", true, true), //
                    nearBreachState(2, "40", "50", true, null));

            deleteAllExternalEvents();
            createBreachPause(loanId, "05 January 2026", "06 January 2026");

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-08", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-09", "2026-01-14", "40", "50", false, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a reset with restart that leaves the near breach unchanged emits no event")
    void resetWithRestartLeavingNearBreachUnchangedEmitsNoEvent() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-05");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "90", true, null));

            deleteAllExternalEvents();
            createBreachResetWithRestartPeriod(loanId);

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-04", "0", "90", true, true), //
                    nearBreachState(2, "2026-01-05", "2026-01-10", "0", "90", null, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    @Test
    @DisplayName("a reset with restart re-derives a closed period's stale near breach from its paid amount")
    void resetWithRestartRederivesStaleClosedPeriod() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan();
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            advanceBusinessDateWithCob(loanId, "2026-01-03", "2026-01-08");
            makeWcRepayment(loanId, BigDecimal.valueOf(80), "05 January 2026");
            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "80", "10", true, true), //
                    nearBreachState(2, "0", "90", null, null));

            deleteAllExternalEvents();
            createBreachResetWithRestartPeriod(loanId);

            validateNearBreachStates(getBreachSchedule(loanId), //
                    nearBreachState(1, "2026-01-01", "2026-01-06", "80", "10", false, true), //
                    nearBreachState(2, "2026-01-07", "2026-01-07", "0", "90", null, true), //
                    nearBreachState(3, "2026-01-08", "2026-01-13", "0", "90", null, null));
            assertNearBreachEvents(loanId, 1);
        });
    }

    @Test
    @DisplayName("a minimum payment reschedule on a balance-capped period judges near breach against the capped minimum payment")
    void minimumPaymentRescheduleOnCappedPeriodUsesCappedMinimumPayment() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupLoan(BigDecimal.valueOf(50));
            advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-03");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "0", "50", true, null));
            makeWcRepayment(loanId, BigDecimal.valueOf(20), "03 January 2026");
            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "20", "30", false, null));

            deleteAllExternalEvents();
            createBreachMinimumPaymentReschedule(loanId, BigDecimal.valueOf(100), BREACH_AMOUNT_CALCULATION_TYPE);

            validateNearBreachStates(getBreachSchedule(loanId), nearBreachState(1, "20", "30", false, null));
            assertNearBreachEvents(loanId, 0);
        });
    }

    private Long setupLoan() {
        return setupLoan(PRINCIPAL);
    }

    private Long setupLoan(final BigDecimal principal) {
        final Long clientId = createClient("01 January 2026");
        final Long productId = createWcProductWithBreachAndNearBreachConfig(BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE,
                BREACH_AMOUNT_CALCULATION_TYPE, BREACH_AMOUNT, BREACH_GRACE_DAYS, NEAR_BREACH_THRESHOLD, NEAR_BREACH_FREQUENCY,
                NEAR_BREACH_FREQUENCY_TYPE);
        return createApproveAndDisburseWcLoan(clientId, productId, principal, "01 January 2026");
    }

    private void assertNearBreachEvents(final Long loanId, final long expected) {
        assertEquals(expected, countExternalEvents(NEAR_BREACH_EVENT, loanId),
                "WorkingCapitalLoanNearBreachChangeBusinessEvent count for loan " + loanId);
    }
}
