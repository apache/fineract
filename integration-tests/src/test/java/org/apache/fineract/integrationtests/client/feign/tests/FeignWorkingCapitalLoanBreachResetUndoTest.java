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

import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.ExpectedBreachAction.action;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.ExpectedBreachPeriod.period;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.validateBreachActions;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.validateBreachPastDueAmount;
import static org.apache.fineract.integrationtests.client.feign.modules.WorkingCapitalBreachTestValidators.validateBreachSchedule;

import java.math.BigDecimal;
import org.apache.fineract.integrationtests.client.feign.FeignWorkingCapitalTestBase;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

/**
 * Common setup: breach config 60 DAYS / PERCENTAGE 50 / grace 0, principal 800 disbursed 01 Jan 2026 (minimum payment
 * 400), repayments 200 on 15 Jan and 100 on 15 Feb, business date left at 15 Feb. After the COB of 03 Mar: period 1
 * [2026-01-01..2026-03-01] breach=true outstanding=100, period 2 [2026-03-02..2026-04-30].
 */
public class FeignWorkingCapitalLoanBreachResetUndoTest extends FeignWorkingCapitalTestBase {

    private static final int BREACH_FREQUENCY = 60;
    private static final String BREACH_FREQUENCY_TYPE = "DAYS";
    private static final String BREACH_AMOUNT_CALCULATION_TYPE = "PERCENTAGE";
    private static final BigDecimal BREACH_AMOUNT_PERCENT = BigDecimal.valueOf(50);
    private static final int BREACH_GRACE_DAYS = 0;

    private static final BigDecimal PRINCIPAL = BigDecimal.valueOf(800);

    @Test
    @DisplayName("Undo of a period-splitting reset on the same day restores the pre-split period and past due 100")
    void undoSameDay_revertsSplitPeriod_restoresTwoRowSchedule() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-06-13", 60, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-04-15"), //
                    action("UNDO_RESET", "2026-04-15"));
        });
    }

    @Test
    @DisplayName("Undo after later periods restores the split, deletes chained periods and reprocesses the 150 payment")
    void undoAfterLaterPeriods_restoresSplitAndReprocessesPayments() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            advanceBusinessDateWithCob(loanId, "2026-04-15", "2026-06-14");
            makeWcRepayment(loanId, BigDecimal.valueOf(150), "20 May 2026");

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-06-13", 60, "400.00", "250.00", true, true), //
                    period(4, "2026-06-14", "2026-08-12", 60, "350.00", "350.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "250");

            advanceBusinessDateWithCob(loanId, "2026-06-14", "2026-06-20");
            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", true, false), //
                    period(3, "2026-05-01", "2026-06-29", 60, "400.00", "250.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "500");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-04-15"), //
                    action("UNDO_RESET", "2026-06-20"));
        });
    }

    @Test
    @DisplayName("Undo reassigns a 20 Apr payment from the split-created period into the restored period 2")
    void undoReassignsPaymentAcrossRestoredBoundary_intoRestoredPeriodTwo() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            advanceBusinessDateWithCob(loanId, "2026-04-15", "2026-04-20");
            makeWcRepayment(loanId, BigDecimal.valueOf(150), "20 April 2026");

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-06-13", 60, "400.00", "250.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "250.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-04-15"), //
                    action("UNDO_RESET", "2026-04-20"));
        });
    }

    @Test
    @DisplayName("Undo restores a pause-extended split period to 70 days with the pause re-applied")
    void undoRestoresPauseExtendedPeriod_withPauseReapplied() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");

            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-03-20");
            createBreachPause(loanId, "20 March 2026", "29 March 2026");

            advanceBusinessDateWithCob(loanId, "2026-03-20", "2026-04-11");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-05-10", 70, "400.00", "400.00", null, false));

            advanceBusinessDateWithCob(loanId, "2026-04-11", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-06-13", 60, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-05-10", 70, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("PAUSE", "2026-03-20"), //
                    action("RESET", "2026-04-15"), //
                    action("UNDO_RESET", "2026-04-15"));
        });
    }

    @Test
    @DisplayName("Undo of a split reset pops only the latest reset and preserves the earlier active reset flag")
    void undoOfStackedResets_popsOnlyLatest_preservesEarlierReset() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-02-20");
            createBreachReset(loanId);

            advanceBusinessDateWithCob(loanId, "2026-02-20", "2026-03-03");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, true), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");

            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, true), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-06-13", 60, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, true), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-02-20"), //
                    action("RESET", "2026-04-15"), //
                    action("UNDO_RESET", "2026-04-15"));
        });
    }

    @Test
    @DisplayName("Undo of a no-split restart reset is flag-only: schedule structure unchanged, past due 100")
    void undoOfNoSplitRestartReset_leavesScheduleStructureUnchanged() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-02");

            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-03-02"), //
                    action("UNDO_RESET", "2026-03-02"));
        });
    }

    @Test
    @DisplayName("Undo keeps a reschedule made before the reset on the restored period (30 days, not the product's 60)")
    void undoKeepsAPreResetRescheduleOnTheRestoredPeriod() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();
            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-03-10");
            createBreachReschedule(loanId, 30, "DAYS");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-03-31", 30, "400.00", "400.00", null, false));
            advanceBusinessDateWithCob(loanId, "2026-03-10", "2026-03-20");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-03-19", 18, "400.00", "400.00", true, false), //
                    period(3, "2026-03-20", "2026-04-18", 30, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");
            createBreachUndoReset(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-03-31", 30, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESCHEDULE", "2026-03-10"), //
                    action("RESET", "2026-03-20"), //
                    action("UNDO_RESET", "2026-03-20"));
            advanceBusinessDateWithCob(loanId, "2026-03-20", "2026-04-01");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-03-31", 30, "400.00", "400.00", true, false), //
                    period(3, "2026-04-01", "2026-04-30", 30, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "500");
        });
    }

    @Test
    @DisplayName("Undo restores the period on the cadence in force at the reset; a later reschedule applies from the next period")
    void undoIgnoresARescheduleMadeAfterTheReset_forTheRestoredPeriod() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();
            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");
            advanceBusinessDateWithCob(loanId, "2026-04-15", "2026-04-20");
            createBreachReschedule(loanId, 30, "DAYS");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-14", 44, "400.00", "400.00", true, false), //
                    period(3, "2026-04-15", "2026-05-14", 30, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");
            createBreachUndoReset(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "100");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-04-15"), //
                    action("RESCHEDULE", "2026-04-20"), //
                    action("UNDO_RESET", "2026-04-20"));
            advanceBusinessDateWithCob(loanId, "2026-04-20", "2026-05-01");
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", true, false), //
                    period(3, "2026-05-01", "2026-05-30", 30, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "500");
        });
    }

    private Long setupCommonBreachLoan() {
        final Long clientId = createClient("01 January 2026");
        final Long productId = createWcProductWithBreachConfig(BREACH_FREQUENCY, BREACH_FREQUENCY_TYPE, BREACH_AMOUNT_CALCULATION_TYPE,
                BREACH_AMOUNT_PERCENT, BREACH_GRACE_DAYS);
        final Long loanId = createApproveAndDisburseWcLoan(clientId, productId, PRINCIPAL, "01 January 2026");
        runInlineWcCob(loanId);

        advanceBusinessDateWithCob(loanId, "2026-01-01", "2026-01-15");
        makeWcRepayment(loanId, BigDecimal.valueOf(200), "15 January 2026");

        advanceBusinessDateWithCob(loanId, "2026-01-15", "2026-02-15");
        makeWcRepayment(loanId, BigDecimal.valueOf(100), "15 February 2026");
        return loanId;
    }

    @Test
    @DisplayName("A pause recorded after a restart reset keeps the reset flag on the period holding the reset date")
    void pauseAfterRestartReset_keepsTheFlagOnThePeriodHoldingTheResetDate() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-04-15");
            createBreachResetWithRestartPeriod(loanId);

            advanceBusinessDateWithCob(loanId, "2026-04-15", "2026-04-20");
            createBreachPause(loanId, "20 April 2026", "29 April 2026");

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-05-10", 70, "400.00", "400.00", null, true), //
                    period(3, "2026-05-11", "2026-07-09", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");

            advanceBusinessDateWithCob(loanId, "2026-04-20", "2026-06-01");

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-05-10", 70, "400.00", "400.00", true, true), //
                    period(3, "2026-05-11", "2026-07-09", 60, "400.00", "400.00", null, false));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "400");
        });
    }

    @Test
    @DisplayName("Undo after a backwards business date move flags the period regenerated for the still active reset")
    void undoAfterBackwardsBusinessDateMove_flagsTheRegeneratedPeriodOfTheStillActiveReset() {
        runAt("2026-01-01", () -> {
            final Long loanId = setupCommonBreachLoan();

            advanceBusinessDateWithCob(loanId, "2026-02-15", "2026-03-03");
            advanceBusinessDateWithCob(loanId, "2026-03-03", "2026-05-10");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", true, false), //
                    period(3, "2026-05-01", "2026-05-09", 9, "400.00", "400.00", true, false), //
                    period(4, "2026-05-10", "2026-07-08", 60, "400.00", "400.00", null, true));

            setBusinessDate("2026-03-20");
            createBreachResetWithRestartPeriod(loanId);
            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-03-19", 18, "400.00", "400.00", true, false), //
                    period(3, "2026-03-20", "2026-05-18", 60, "400.00", "400.00", null, true));

            advanceBusinessDateWithCob(loanId, "2026-03-20", "2026-06-20");
            createBreachUndoReset(loanId);

            validateBreachSchedule(getBreachSchedule(loanId), //
                    period(1, "2026-01-01", "2026-03-01", 60, "400.00", "100.00", true, false), //
                    period(2, "2026-03-02", "2026-04-30", 60, "400.00", "400.00", true, false), //
                    period(3, "2026-05-01", "2026-06-29", 60, "400.00", "400.00", null, true));
            validateBreachPastDueAmount(getBreachPastDueAmount(loanId), "0");
            validateBreachActions(getBreachActions(loanId), //
                    action("RESET", "2026-05-10"), //
                    action("RESET", "2026-03-20"), //
                    action("UNDO_RESET", "2026-06-20"));
        });
    }
}
