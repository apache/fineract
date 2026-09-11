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
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.data.DueStandingInstruction;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.item.Chunk;

@ExtendWith(MockitoExtension.class)
class StandingInstructionItemWriterTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 5, 1);

    @Mock
    private StandingInstructionExecutionService executionService;

    private DueStandingInstruction due(final Long id) {
        return new DueStandingInstruction(dueFixedSavingsToSavings(id), new BigDecimal("100"), BUSINESS_DATE);
    }

    @Test
    void everyInstructionOfTheChunkIsExecuted() {
        when(executionService.execute(any(), any(), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = new StandingInstructionItemWriter(executionService);

        writer.write(new Chunk<>(List.of(due(1L), due(2L))));

        verify(executionService).execute(any(), eq(1L), eq(BUSINESS_DATE), any());
        verify(executionService).execute(any(), eq(2L), eq(BUSINESS_DATE), any());
    }

    @Test
    void theTransferIsBuiltFromTheMandate() {
        when(executionService.execute(any(), any(), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = new StandingInstructionItemWriter(executionService);

        writer.write(new Chunk<>(List.of(due(1L))));

        final ArgumentCaptor<AccountTransferDTO> transfer = ArgumentCaptor.forClass(AccountTransferDTO.class);
        verify(executionService).execute(transfer.capture(), eq(1L), eq(BUSINESS_DATE), any());
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
        when(executionService.execute(any(), eq(1L), any(), any())).thenThrow(new IllegalStateException("boom"));
        final StandingInstructionItemWriter writer = new StandingInstructionItemWriter(executionService);

        assertThatThrownBy(() -> writer.write(new Chunk<>(List.of(due(1L), due(2L))))).isInstanceOf(IllegalStateException.class);

        verify(executionService).execute(any(), eq(1L), any(), any());
        verifyNoMoreInteractions(executionService);
    }

    /**
     * A replay can re-present an instruction that has already run; the execution service declines it and the writer
     * carries on rather than treating the refusal as a failure.
     */
    @Test
    void anInstructionThatHasAlreadyRunIsNotAFailure() {
        when(executionService.execute(any(), eq(1L), any(), any())).thenReturn(false);
        when(executionService.execute(any(), eq(2L), any(), any())).thenReturn(true);
        final StandingInstructionItemWriter writer = new StandingInstructionItemWriter(executionService);

        writer.write(new Chunk<>(List.of(due(1L), due(2L))));

        verify(executionService).execute(any(), eq(2L), any(), any());
    }
}
