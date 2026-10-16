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
package org.apache.fineract.portfolio.account.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.portfolio.account.data.AccountTransferDTO;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistory;
import org.apache.fineract.portfolio.account.domain.StandingInstructionHistoryRepository;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.jdbc.core.JdbcTemplate;

@ExtendWith(MockitoExtension.class)
class StandingInstructionExecutionServiceImplTest {

    private static final LocalDate BUSINESS_DATE = LocalDate.of(2026, 5, 1);

    @Mock
    private AccountTransfersWritePlatformService accountTransfersWritePlatformService;
    @Mock
    private StandingInstructionHistoryRepository historyRepository;
    @Mock
    private JdbcTemplate jdbcTemplate;

    @BeforeEach
    void setUp() {
        // StandingInstructionHistory stamps its execution time from the tenant's clock.
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
    }

    @AfterEach
    void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    private StandingInstructionExecutionServiceImpl service() {
        return new StandingInstructionExecutionServiceImpl(accountTransfersWritePlatformService, historyRepository, jdbcTemplate);
    }

    @Test
    void aClaimedInstructionTransfersAndIsRecordedAsSuccess() {
        final AccountTransferDTO transfer = mock(AccountTransferDTO.class);
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(1);

        final boolean transferred = service().execute(transfer, 7L, BUSINESS_DATE, new BigDecimal("100"));

        assertThat(transferred).isTrue();
        verify(accountTransfersWritePlatformService).transferFunds(transfer);
        final ArgumentCaptor<StandingInstructionHistory> history = ArgumentCaptor.forClass(StandingInstructionHistory.class);
        verify(historyRepository).saveAndFlush(history.capture());
        assertThat(history.getValue().getStatus()).isEqualTo("success");
        assertThat(history.getValue().getAmount()).isEqualByComparingTo("100");
    }

    /**
     * A rolled-back chunk is replayed one instruction at a time, so an instruction can be presented twice in a single
     * run — and a job can be triggered twice in a day. The claim is what makes the second presentation a no-op instead
     * of a second transfer.
     */
    @Test
    void anInstructionThatHasAlreadyRunTodayTransfersNothing() {
        final AccountTransferDTO transfer = mock(AccountTransferDTO.class);
        when(jdbcTemplate.update(anyString(), any(), any(), any())).thenReturn(0);

        final boolean transferred = service().execute(transfer, 7L, BUSINESS_DATE, new BigDecimal("100"));

        assertThat(transferred).isFalse();
        verifyNoInteractions(accountTransfersWritePlatformService);
        verifyNoInteractions(historyRepository);
    }

    @Test
    void aFailureIsRecordedAgainstTheMandate() {
        service().recordFailure(7L, "InsufficientAccountBalance Exception ");

        final ArgumentCaptor<StandingInstructionHistory> history = ArgumentCaptor.forClass(StandingInstructionHistory.class);
        verify(historyRepository).saveAndFlush(history.capture());
        assertThat(history.getValue().getStatus()).isEqualTo("failed");
        assertThat(history.getValue().getErrorLog()).contains("InsufficientAccountBalance");
    }
}
