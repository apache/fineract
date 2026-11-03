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
package org.apache.fineract.integrationtests.savings;

import static org.apache.fineract.client.feign.util.FeignCalls.ok;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.client.models.GetStandingInstructionHistoryPageItemsResponse;
import org.apache.fineract.client.models.JobDetailHistoryDataSwagger;
import org.apache.fineract.client.models.SavingsAccountTransactionData;
import org.apache.fineract.client.models.StandingInstructionCreationRequest;
import org.apache.fineract.integrationtests.client.feign.FeignSavingsTestBase;
import org.apache.fineract.integrationtests.client.feign.modules.SavingsRequestBuilders;
import org.apache.fineract.integrationtests.common.FineractFeignClientHelper;
import org.apache.fineract.integrationtests.common.StandingInstructionsHelper;
import org.junit.jupiter.api.Test;

/**
 * Three standing instructions due on the same business date, run by one scheduled run of the Execute Standing
 * Instruction job: one funded, one that cannot be covered with partial transfers off, and one partly covered with
 * partial transfers on. The failure must not stop or roll back the other two, and the run must still be reported as
 * failed in job_run_history.
 */
class StandingInstructionMixedOutcomeTest extends FeignSavingsTestBase {

    private static final String JOB_NAME = "Execute Standing Instruction";
    private static final String SAVINGS_ACCOUNT_TYPE = "2";
    private static final int SAVINGS_ACCOUNT_TYPE_ID = 2;
    private static final int TRANSFER_TYPE_ACCOUNT_TRANSFER = 1;
    private static final String RECURRENCE_FREQUENCY_MONTHS = "2";
    // the due-instruction query orders by priority descending, so LOW runs first and HIGH last
    private static final String PRIORITY_HIGH = "2";
    private static final String PRIORITY_MEDIUM = "3";
    private static final String PRIORITY_LOW = "4";

    private static final String OPENING_DATE = "01 January 2026";
    private static final String VALID_FROM = "01 February 2026";
    private static final String VALID_TILL = "31 December 2026";
    // monthly on the 15th from 1 February: due on 15 February and on 15 March 2026
    private static final String RECURRENCE_ON = "15 March";
    private static final String JOB_DATE = "15 March 2026";
    private static final LocalDate JOB_BUSINESS_DATE = LocalDate.of(2026, 3, 15);
    private static final String AMOUNT = "40.00";

    private final StandingInstructionsHelper standingInstructionsHelper = new StandingInstructionsHelper();

    @Test
    void failedInstructionDoesNotStopOrRollBackTheOthersAndTheRunIsReportedAsFailed() {
        final Instruction[] instructions = new Instruction[3];
        runAt(OPENING_DATE, () -> {
            final Long clientId = createClient(OPENING_DATE);
            final Long productId = createSavingsProduct(SavingsRequestBuilders.defaultSavingsProduct().digitsAfterDecimal(2)
                    .nominalAnnualInterestRate(0.0).allowOverdraft(false).enforceMinRequiredBalance(false).withHoldTax(false))
                    .getResourceId();
            instructions[0] = new Instruction(clientId, createFundedAccount(clientId, productId, "100.00"),
                    createApproveActivateSavings(clientId, productId, OPENING_DATE));
            instructions[1] = new Instruction(clientId, createFundedAccount(clientId, productId, "10.00"),
                    createApproveActivateSavings(clientId, productId, OPENING_DATE));
            instructions[2] = new Instruction(clientId, createFundedAccount(clientId, productId, "12.34"),
                    createApproveActivateSavings(clientId, productId, OPENING_DATE));
        });
        final Instruction a = instructions[0];
        final Instruction b = instructions[1];
        final Instruction c = instructions[2];

        runAt(VALID_FROM, () -> {
            a.id = createInstruction(a, PRIORITY_LOW, false);
            b.id = createInstruction(b, PRIORITY_MEDIUM, false);
            c.id = createInstruction(c, PRIORITY_HIGH, true);
        });

        assertBalance(a.source, "100.00");
        assertBalance(b.source, "10.00");
        assertBalance(c.source, "12.34");
        for (Instruction instruction : instructions) {
            assertBalance(instruction.destination, "0.00");
            instruction.historyBefore = history(instruction).size();
            instruction.lastRunDateBefore = lastRunDate(instruction);
            instruction.sourceTransactionsBefore = transactions(instruction.source).size();
            instruction.destinationTransactionsBefore = transactions(instruction.destination).size();
        }
        assertEquals(0, b.destinationTransactionsBefore);

        final JobDetailHistoryDataSwagger[] run = new JobDetailHistoryDataSwagger[1];
        runAt(JOB_DATE, () -> run[0] = schedulerHelper.executeAndAwaitJobRun(JOB_NAME));

        assertEquals("failed", run[0].getStatus(), () -> "job_run_history entry of the run: " + run[0]);

        // A: funded, transfers the full amount
        final GetStandingInstructionHistoryPageItemsResponse aRow = singleNewHistoryRow(a);
        assertEquals("success", aRow.getStatus());
        assertAmount("40.00", aRow.getAmount());
        assertEquals(JOB_BUSINESS_DATE, lastRunDate(a));
        assertBalance(a.source, "60.00");
        assertBalance(a.destination, "40.00");
        assertSingleNewTransfer(a, "40.00");

        // B: cannot cover the amount, partial transfers off: nothing moves, the full amount is recorded as attempted
        final GetStandingInstructionHistoryPageItemsResponse bRow = singleNewHistoryRow(b);
        assertEquals("failed", bRow.getStatus());
        assertAmount("40.00", bRow.getAmount());
        assertNotNull(bRow.getErrorLog());
        assertTrue(bRow.getErrorLog().contains("InsufficientAccountBalance"), () -> "error log of B: " + bRow.getErrorLog());
        assertEquals(b.lastRunDateBefore, lastRunDate(b));
        assertBalance(b.source, "10.00");
        assertBalance(b.destination, "0.00");
        assertEquals(b.sourceTransactionsBefore, transactions(b.source).size(), "no debit on B's source");
        assertEquals(b.destinationTransactionsBefore, transactions(b.destination).size(), "no credit on B's destination");

        // C: partial transfers on: transfers what is available and records the amount posted
        final GetStandingInstructionHistoryPageItemsResponse cRow = singleNewHistoryRow(c);
        assertEquals("partial", cRow.getStatus());
        assertAmount("12.34", cRow.getAmount());
        assertEquals(JOB_BUSINESS_DATE, lastRunDate(c));
        assertBalance(c.source, "0.00");
        assertBalance(c.destination, "12.34");
        assertSingleNewTransfer(c, "12.34");
    }

    private Long createFundedAccount(final Long clientId, final Long productId, final String openingBalance) {
        final Long savingsId = createApproveActivateSavings(clientId, productId, OPENING_DATE);
        deposit(savingsId, openingBalance, OPENING_DATE);
        return savingsId;
    }

    private Long createInstruction(final Instruction instruction, final String priority, final boolean allowPartialTransfer) {
        final StandingInstructionCreationRequest request = standingInstructionsHelper
                .build(instruction.clientId.toString(), instruction.source.toString(), instruction.destination.toString(),
                        SAVINGS_ACCOUNT_TYPE, SAVINGS_ACCOUNT_TYPE, VALID_FROM, VALID_TILL, RECURRENCE_ON)
                .recurrenceFrequency(RECURRENCE_FREQUENCY_MONTHS).recurrenceInterval("1").priority(priority).amount(AMOUNT)
                .allowPartialTransfer(allowPartialTransfer);
        return ok(() -> FineractFeignClientHelper.getFineractFeignClient().standingInstructions().createStandingInstruction(request))
                .getResourceId().longValue();
    }

    private List<GetStandingInstructionHistoryPageItemsResponse> history(final Instruction instruction) {
        return standingInstructionsHelper
                .getStandingInstructionHistory(instruction.source.intValue(), SAVINGS_ACCOUNT_TYPE_ID, instruction.clientId.intValue(),
                        TRANSFER_TYPE_ACCOUNT_TRANSFER)
                .stream().filter(row -> instruction.id.equals(row.getStandingInstructionId())).toList();
    }

    private GetStandingInstructionHistoryPageItemsResponse singleNewHistoryRow(final Instruction instruction) {
        final List<GetStandingInstructionHistoryPageItemsResponse> rows = history(instruction);
        assertEquals(instruction.historyBefore + 1, rows.size(), () -> "history of standing instruction " + instruction.id + ": " + rows);
        assertEquals(0, instruction.historyBefore, "the instruction had no history before the run");
        return rows.get(0);
    }

    private LocalDate lastRunDate(final Instruction instruction) {
        return standingInstructionsHelper.getStandingInstructionById(instruction.id).getLastRunDate();
    }

    private List<SavingsAccountTransactionData> transactions(final Long savingsId) {
        return Optional.ofNullable(savingsTransactionHelper.getTransactions(savingsId)).orElse(List.of()).stream()
                .filter(transaction -> !Boolean.TRUE.equals(transaction.getReversed())).toList();
    }

    /** Exactly one new transaction on each side, for the given amount. */
    private void assertSingleNewTransfer(final Instruction instruction, final String amount) {
        final List<SavingsAccountTransactionData> sourceTransactions = transactions(instruction.source);
        final List<SavingsAccountTransactionData> destinationTransactions = transactions(instruction.destination);
        assertEquals(instruction.sourceTransactionsBefore + 1, sourceTransactions.size(), "one debit on the source");
        assertEquals(instruction.destinationTransactionsBefore + 1, destinationTransactions.size(), "one credit on the destination");
        assertTrue(
                sourceTransactions.stream()
                        .anyMatch(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getWithdrawal())
                                && new BigDecimal(amount).compareTo(transaction.getAmount()) == 0
                                && JOB_BUSINESS_DATE.equals(transaction.getDate())),
                () -> "debit of " + amount + " on " + JOB_BUSINESS_DATE + " in " + sourceTransactions);
        assertTrue(
                destinationTransactions.stream()
                        .anyMatch(transaction -> Boolean.TRUE.equals(transaction.getTransactionType().getDeposit())
                                && new BigDecimal(amount).compareTo(transaction.getAmount()) == 0
                                && JOB_BUSINESS_DATE.equals(transaction.getDate())),
                () -> "credit of " + amount + " on " + JOB_BUSINESS_DATE + " in " + destinationTransactions);
    }

    private void assertBalance(final Long savingsId, final String expected) {
        final BigDecimal balance = Optional.ofNullable(savingsHelper.getSavingsSummary(savingsId).getAccountBalance())
                .orElse(BigDecimal.ZERO);
        assertEquals(0, new BigDecimal(expected).compareTo(balance), () -> "balance of savings account " + savingsId + " was " + balance);
    }

    private static void assertAmount(final String expected, final Number actual) {
        assertNotNull(actual);
        assertEquals(0, new BigDecimal(expected).compareTo(new BigDecimal(actual.toString())),
                () -> "history amount was " + actual + ", expected " + expected);
    }

    private static final class Instruction {

        private final Long clientId;
        private final Long source;
        private final Long destination;
        private Long id;
        private int historyBefore;
        private LocalDate lastRunDateBefore;
        private int sourceTransactionsBefore;
        private int destinationTransactionsBefore;

        private Instruction(final Long clientId, final Long source, final Long destination) {
            this.clientId = clientId;
            this.source = source;
            this.destination = destination;
        }
    }
}
