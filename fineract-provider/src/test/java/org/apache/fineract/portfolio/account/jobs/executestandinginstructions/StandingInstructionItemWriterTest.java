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
package org.apache.fineract.portfolio.account.jobs.executestandinginstructions;

import static org.apache.fineract.portfolio.account.jobs.executestandinginstructions.StandingInstructionTestData.dueFixedSavingsToSavings;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.argThat;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.organisation.monetary.domain.MonetaryCurrency;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistoryStatus;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.savings.domain.SavingsAccount;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountAssembler;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.infrastructure.item.Chunk;

@ExtendWith(MockitoExtension.class)
class StandingInstructionItemWriterTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 5, 1);

    @Mock
    private StandingInstructionExecutionService executionService;

    @Mock
    private SavingsAccountAssembler savingsAccountAssembler;

    private StandingInstructionItemWriter writer() {
        return new StandingInstructionItemWriter(executionService, savingsAccountAssembler);
    }

    private DueStandingInstruction due(final Long id) {
        return new DueStandingInstruction(dueFixedSavingsToSavings(id), new BigDecimal("100"), BUSINESS_DATE);
    }

    @Test
    void everyInstructionOfTheChunkIsExecuted() {
        when(executionService.execute(any(), any(), any(), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = writer();

        writer.write(new Chunk<>(List.of(due(1L), due(2L))));

        verify(executionService).execute(any(), eq(1L), eq(BUSINESS_DATE), any(), any());
        verify(executionService).execute(any(), eq(2L), eq(BUSINESS_DATE), any(), any());
    }

    @Test
    void theTransferIsBuiltFromTheMandate() {
        when(executionService.execute(any(), any(), any(), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = writer();

        writer.write(new Chunk<>(List.of(due(1L))));

        final ArgumentCaptor<AccountTransferDTO> transfer = ArgumentCaptor.forClass(AccountTransferDTO.class);
        verify(executionService).execute(transfer.capture(), eq(1L), eq(BUSINESS_DATE), any(), any());
        assertThat(transfer.getValue().getFromAccountId()).isEqualTo(10L);
        assertThat(transfer.getValue().getToAccountId()).isEqualTo(11L);
        assertThat(transfer.getValue().getTransactionAmount()).isEqualByComparingTo("100");
    }

    /**
     * The writer must let a failure out rather than swallow it. That is what rolls the chunk back and makes the step
     * replay it one instruction per transaction; a swallowed failure would leave the run reporting success for an
     * instruction that never paid.
     */
    @Test
    void aFailingInstructionStopsTheChunk() {
        when(executionService.execute(any(), eq(1L), any(), any(), any())).thenThrow(new IllegalStateException("boom"));
        final StandingInstructionItemWriter writer = writer();

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(due(1L), due(2L))))).isInstanceOf(IllegalStateException.class);

        verify(executionService).execute(any(), eq(1L), any(), any(), any());
        verifyNoMoreInteractions(executionService);
    }

    /**
     * A replay can re-present an instruction that has already run; the execution service declines it and the writer
     * carries on rather than treating the refusal as a failure.
     */
    @Test
    void anInstructionThatHasAlreadyRunIsNotAFailure() {
        when(executionService.execute(any(), eq(1L), any(), any(), any())).thenReturn(false);
        when(executionService.execute(any(), eq(2L), any(), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = writer();

        writer.write(new Chunk<>(List.of(due(1L), due(2L))));

        verify(executionService).execute(any(), eq(2L), any(), any(), any());
    }

    /**
     * Without the opt-in, an instruction is attempted for its whole amount and the savings account is not consulted
     * (FINERACT-2400 leaves the default untouched).
     */
    @Test
    void withoutPartialTransferTheWholeAmountIsAttempted() {
        when(executionService.execute(any(), any(), any(), any(), any())).thenReturn(true);

        writer().write(new Chunk<>(List.of(due(1L))));

        verify(executionService).execute(any(), eq(1L), eq(BUSINESS_DATE), argThat(amount -> amount.compareTo(new BigDecimal("100")) == 0),
                eq(StandingInstructionHistoryStatus.SUCCESS));
        verifyNoInteractions(savingsAccountAssembler);
    }

    @Test
    void partialTransferIsLimitedToTheAvailableBalance() {
        givenAWithdrawableBalanceOf(new BigDecimal("60"), BigDecimal.ZERO);

        assertThat(writePartialTransfer()).isEqualByComparingTo("60");
        verify(executionService).execute(any(), eq(1L), any(), argThat(amount -> amount.compareTo(new BigDecimal("60")) == 0),
                eq(StandingInstructionHistoryStatus.PARTIAL));
    }

    @Test
    void partialTransferLeavesRoomForTheWithdrawalFee() {
        // the amount fits in the withdrawable balance, but not together with its fee
        givenAWithdrawableBalanceOf(new BigDecimal("105"), new BigDecimal("10"));

        assertThat(writePartialTransfer()).isEqualByComparingTo("95");
        verify(executionService).execute(any(), any(), any(), any(), eq(StandingInstructionHistoryStatus.PARTIAL));
    }

    /**
     * With nothing to cover, the opt-in changes nothing: the full amount is attempted, so the run fails and is recorded
     * as failed.
     */
    @Test
    void nothingCoverableAttemptsTheFullAmount() {
        givenAWithdrawableBalanceOf(BigDecimal.ZERO, BigDecimal.ZERO);

        assertThat(writePartialTransfer()).isEqualByComparingTo("100");
        verify(executionService).execute(any(), any(), any(), any(), eq(StandingInstructionHistoryStatus.SUCCESS));
    }

    @Test
    void fullAmountIsTransferredWhenTheBalanceCoversIt() {
        givenAWithdrawableBalanceOf(new BigDecimal("500"), BigDecimal.ZERO);

        assertThat(writePartialTransfer()).isEqualByComparingTo("100");
        verify(executionService).execute(any(), any(), any(), any(), eq(StandingInstructionHistoryStatus.SUCCESS));
    }

    @Test
    void coverableAmountReservesAPercentageWithdrawalFee() {
        final SavingsAccount account = savingsAccount(new BigDecimal("1000"), null);
        when(account.calculateWithdrawalFee(any(BigDecimal.class)))
                .thenAnswer(invocation -> invocation.<BigDecimal>getArgument(0).movePointLeft(2));

        assertThat(StandingInstructionItemWriter.coverableAmount(account, new BigDecimal("2000"))).isEqualByComparingTo("990");
        assertThat(StandingInstructionItemWriter.coverableAmount(account, new BigDecimal("990"))).isEqualByComparingTo("990")
                .describedAs("an amount that fits together with its fee is not reduced");
    }

    /** Runs a partial-transfer instruction due for 100 and returns the amount the transfer was built for. */
    private BigDecimal writePartialTransfer() {
        when(executionService.execute(any(), any(), any(), any(), any())).thenReturn(true);
        final DueStandingInstruction due = due(1L);
        when(due.instruction().isAllowPartialTransfer()).thenReturn(true);

        writer().write(new Chunk<>(List.of(due)));

        final ArgumentCaptor<AccountTransferDTO> transfer = ArgumentCaptor.forClass(AccountTransferDTO.class);
        verify(executionService).execute(transfer.capture(), any(), any(), any(), any());
        assertThat(transfer.getValue().getFromSavingsAccount()).isNotNull()
                .describedAs("the transfer reuses the savings account the coverable amount was read from");
        return transfer.getValue().getTransactionAmount();
    }

    private void givenAWithdrawableBalanceOf(final BigDecimal withdrawableBalance, final BigDecimal flatWithdrawalFee) {
        final SavingsAccount account = savingsAccount(withdrawableBalance, flatWithdrawalFee);
        when(savingsAccountAssembler.assembleFrom(10L, false)).thenReturn(account);
    }

    private static SavingsAccount savingsAccount(final BigDecimal withdrawableBalance, final BigDecimal flatWithdrawalFee) {
        final SavingsAccount account = mock(SavingsAccount.class);
        when(account.getWithdrawableBalance()).thenReturn(withdrawableBalance);
        lenient().when(account.calculateWithdrawalFee(any(BigDecimal.class))).thenReturn(flatWithdrawalFee);
        lenient().when(account.getCurrency()).thenReturn(new MonetaryCurrency("USD", 2, null));
        return account;
    }
}
