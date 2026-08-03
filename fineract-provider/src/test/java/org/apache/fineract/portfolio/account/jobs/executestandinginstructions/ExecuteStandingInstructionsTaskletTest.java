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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doAnswer;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.account.PortfolioAccountType;
import org.apache.fineract.portfolio.account.data.PortfolioAccountData;
import org.apache.fineract.portfolio.account.data.StandingInstructionData;
import org.apache.fineract.portfolio.account.domain.AccountTransferRecurrenceType;
import org.apache.fineract.portfolio.account.domain.AccountTransferType;
import org.apache.fineract.portfolio.account.domain.StandingInstructionStatus;
import org.apache.fineract.portfolio.account.domain.StandingInstructionType;
import org.apache.fineract.portfolio.account.service.StandingInstructionExecutionService;
import org.apache.fineract.portfolio.account.service.StandingInstructionReadPlatformService;
import org.apache.fineract.portfolio.common.domain.PeriodFrequencyType;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
class ExecuteStandingInstructionsTaskletTest {

    @Mock
    private StandingInstructionReadPlatformService readService;
    @Mock
    private StandingInstructionExecutionService executionService;

    private ExecuteStandingInstructionsTasklet tasklet;

    @BeforeEach
    void setUp() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(
                Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.of(2026, 5, 1), BusinessDateType.COB_DATE, LocalDate.of(2026, 4, 30))));
        tasklet = new ExecuteStandingInstructionsTasklet(readService, executionService);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    /**
     * Regression for the pre-fix bug where the whole batch ran in one transaction: a single failing instruction marked
     * it rollback-only, reverting every successful sibling and persisting no history. With per-instruction isolation
     * the run must complete, siblings must be recorded as success, and the failing one must be recorded as failed —
     * each via its own (REQUIRES_NEW) execution-service call.
     */
    @Test
    void oneFailingInstructionDoesNotAbortRunAndEveryOutcomeIsRecorded() throws Exception {
        final StandingInstructionData si1 = dueFixedSavingsToSavings(1L);
        final StandingInstructionData si2 = dueFixedSavingsToSavings(2L);
        final StandingInstructionData si3 = dueFixedSavingsToSavings(3L);
        when(readService.retrieveAll(StandingInstructionStatus.ACTIVE.getValue())).thenReturn(List.of(si1, si2, si3));
        // the middle instruction (id 2) fails atomically on execute(); the other two succeed
        doAnswer(invocation -> {
            final Long standingInstructionId = invocation.getArgument(1);
            if (standingInstructionId == 2L) {
                throw new RuntimeException("insufficient funds");
            }
            return null;
        }).when(executionService).execute(any(), any(), any(), any());

        final RepeatStatus status = assertDoesNotThrow(() -> tasklet.execute(null, null));

        assertEquals(RepeatStatus.FINISHED, status);
        // siblings executed atomically (transfer + success history) — proving one failure no longer reverts them
        verify(executionService).execute(any(), eq(1L), any(), any());
        verify(executionService).execute(any(), eq(3L), any(), any());
        // the failure produced a durable failed record (not swallowed, not lost to a rollback)...
        verify(executionService).recordFailure(eq(2L), anyString());
        // ...and the successful siblings were NOT misrecorded as failures
        verify(executionService, never()).recordFailure(eq(1L), anyString());
        verify(executionService, never()).recordFailure(eq(3L), anyString());
    }

    private StandingInstructionData dueFixedSavingsToSavings(final Long id) {
        final StandingInstructionData data = mock(StandingInstructionData.class);
        final PortfolioAccountData fromAccount = mock(PortfolioAccountData.class);
        final PortfolioAccountData toAccount = mock(PortfolioAccountData.class);
        lenient().when(fromAccount.getId()).thenReturn(id * 10);
        lenient().when(toAccount.getId()).thenReturn(id * 10 + 1);
        when(data.getId()).thenReturn(id);
        when(data.getRecurrenceType()).thenReturn(AccountTransferRecurrenceType.PERIODIC);
        when(data.getRecurrenceFrequency()).thenReturn(PeriodFrequencyType.DAYS);
        when(data.getValidFrom()).thenReturn(LocalDate.of(2020, 1, 1));
        when(data.getRecurrenceInterval()).thenReturn(1);
        when(data.getInstructionType()).thenReturn(StandingInstructionType.FIXED);
        when(data.getAmount()).thenReturn(new BigDecimal("100"));
        when(data.getFromAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(data.getToAccountType()).thenReturn(PortfolioAccountType.SAVINGS);
        when(data.getFromAccount()).thenReturn(fromAccount);
        when(data.getToAccount()).thenReturn(toAccount);
        when(data.getName()).thenReturn("SI-" + id);
        when(data.getTransferType()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER);
        when(data.toTransferType()).thenReturn(AccountTransferType.ACCOUNT_TRANSFER.getValue());
        return data;
    }
}
