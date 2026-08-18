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
package org.apache.fineract.commands.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Collections;
import java.util.List;
import org.apache.fineract.commands.data.PendingMakerCheckerData;
import org.apache.fineract.commands.domain.CommandProcessingResultType;
import org.apache.fineract.commands.domain.CommandSource;
import org.apache.fineract.commands.domain.CommandSourceRepository;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class MakerCheckerReadServiceImplTest {

    @Mock
    private CommandSourceRepository commandSourceRepository;

    @InjectMocks
    private MakerCheckerReadServiceImpl service;

    private static final Integer AWAITING_STATUS = CommandProcessingResultType.AWAITING_APPROVAL.getValue();
    private static final OffsetDateTime NOW = OffsetDateTime.now(ZoneOffset.UTC);

    private CommandSource buildCommandSource(String action, String entity, String username) {
        final AppUser maker = mock(AppUser.class);
        when(maker.getUsername()).thenReturn(username);

        return CommandSource.builder()
                .actionName(action)
                .entityName(entity)
                .maker(maker)
                .madeOnDate(NOW)
                .status(CommandProcessingResultType.AWAITING_APPROVAL.getValue())
                .sanitized(false)
                .build();
    }

    @Test
    void retrievePendingByLoanId_withPendingCommand_returnsMappedData() {
        final Long loanId = 101L;
        final CommandSource cs = buildCommandSource("APPROVE", "LOAN", "maker01");
        when(commandSourceRepository.findPendingByLoanId(loanId, AWAITING_STATUS)).thenReturn(List.of(cs));

        final List<PendingMakerCheckerData> result = service.retrievePendingByLoanId(loanId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActionName()).isEqualTo("APPROVE");
        assertThat(result.get(0).getEntityName()).isEqualTo("LOAN");
        assertThat(result.get(0).getPermissionCode()).isEqualTo("APPROVE_LOAN"); // computed: action + "_" + entity
        assertThat(result.get(0).getMakerUsername()).isEqualTo("maker01");
        assertThat(result.get(0).getMadeOnDate()).isEqualTo(NOW);
        verify(commandSourceRepository).findPendingByLoanId(loanId, AWAITING_STATUS);
    }

    @Test
    void retrievePendingByLoanId_withNoPendingCommands_returnsEmptyList() {
        final Long loanId = 102L;
        when(commandSourceRepository.findPendingByLoanId(loanId, AWAITING_STATUS)).thenReturn(Collections.emptyList());

        final List<PendingMakerCheckerData> result = service.retrievePendingByLoanId(loanId);

        assertThat(result).isEmpty();
    }

    @Test
    void retrievePendingByLoanId_withMultiplePendingCommands_returnsAllMapped() {
        final Long loanId = 103L;
        final CommandSource cs1 = buildCommandSource("APPROVE", "LOAN", "maker01");
        final CommandSource cs2 = buildCommandSource("DISBURSE", "LOAN", "maker02");
        when(commandSourceRepository.findPendingByLoanId(loanId, AWAITING_STATUS)).thenReturn(List.of(cs1, cs2));

        final List<PendingMakerCheckerData> result = service.retrievePendingByLoanId(loanId);

        assertThat(result).hasSize(2);
        assertThat(result.get(0).getActionName()).isEqualTo("APPROVE");
        assertThat(result.get(1).getActionName()).isEqualTo("DISBURSE");
    }

    @Test
    void retrievePendingByLoanId_withNullMaker_returnsMakerUsernameNull() {
        final Long loanId = 104L;
        final CommandSource cs = CommandSource.builder()
                .actionName("APPROVE")
                .entityName("LOAN")
                .maker(null)
                .madeOnDate(NOW)
                .status(CommandProcessingResultType.AWAITING_APPROVAL.getValue())
                .sanitized(false)
                .build();
        when(commandSourceRepository.findPendingByLoanId(loanId, AWAITING_STATUS)).thenReturn(List.of(cs));

        final List<PendingMakerCheckerData> result = service.retrievePendingByLoanId(loanId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getMakerUsername()).isNull();
    }

    @Test
    void retrievePendingByClientId_withPendingCommand_returnsMappedData() {
        final Long clientId = 201L;
        final CommandSource cs = buildCommandSource("ACTIVATE", "CLIENT", "maker03");
        when(commandSourceRepository.findPendingByClientId(clientId, AWAITING_STATUS)).thenReturn(List.of(cs));

        final List<PendingMakerCheckerData> result = service.retrievePendingByClientId(clientId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActionName()).isEqualTo("ACTIVATE");
        assertThat(result.get(0).getEntityName()).isEqualTo("CLIENT");
        assertThat(result.get(0).getPermissionCode()).isEqualTo("ACTIVATE_CLIENT");
        assertThat(result.get(0).getMakerUsername()).isEqualTo("maker03");
        verify(commandSourceRepository).findPendingByClientId(clientId, AWAITING_STATUS);
    }

    @Test
    void retrievePendingByClientId_withNoPendingCommands_returnsEmptyList() {
        final Long clientId = 202L;
        when(commandSourceRepository.findPendingByClientId(clientId, AWAITING_STATUS)).thenReturn(Collections.emptyList());

        final List<PendingMakerCheckerData> result = service.retrievePendingByClientId(clientId);

        assertThat(result).isEmpty();
    }

    @Test
    void retrievePendingBySavingsId_withPendingCommand_returnsMappedData() {
        final Long savingsId = 301L;
        final CommandSource cs = buildCommandSource("APPROVE", "SAVINGSACCOUNT", "maker04");
        when(commandSourceRepository.findPendingBySavingsId(savingsId, AWAITING_STATUS)).thenReturn(List.of(cs));

        final List<PendingMakerCheckerData> result = service.retrievePendingBySavingsId(savingsId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getActionName()).isEqualTo("APPROVE");
        assertThat(result.get(0).getEntityName()).isEqualTo("SAVINGSACCOUNT");
        assertThat(result.get(0).getPermissionCode()).isEqualTo("APPROVE_SAVINGSACCOUNT");
        assertThat(result.get(0).getMakerUsername()).isEqualTo("maker04");
        verify(commandSourceRepository).findPendingBySavingsId(savingsId, AWAITING_STATUS);
    }

    @Test
    void retrievePendingBySavingsId_withNoPendingCommands_returnsEmptyList() {
        final Long savingsId = 302L;
        when(commandSourceRepository.findPendingBySavingsId(savingsId, AWAITING_STATUS)).thenReturn(Collections.emptyList());

        final List<PendingMakerCheckerData> result = service.retrievePendingBySavingsId(savingsId);

        assertThat(result).isEmpty();
    }

    @Test
    void retrievePendingBySavingsId_coversFixedDeposit_returnsMappedData() {
        final Long fdId = 401L;
        final CommandSource cs = buildCommandSource("ACTIVATE", "FIXEDDEPOSITACCOUNT", "maker05");
        when(commandSourceRepository.findPendingBySavingsId(fdId, AWAITING_STATUS)).thenReturn(List.of(cs));

        final List<PendingMakerCheckerData> result = service.retrievePendingBySavingsId(fdId);

        assertThat(result).hasSize(1);
        assertThat(result.get(0).getEntityName()).isEqualTo("FIXEDDEPOSITACCOUNT");
        assertThat(result.get(0).getPermissionCode()).isEqualTo("ACTIVATE_FIXEDDEPOSITACCOUNT");
        assertThat(result.get(0).getMakerUsername()).isEqualTo("maker05");
    }
}
