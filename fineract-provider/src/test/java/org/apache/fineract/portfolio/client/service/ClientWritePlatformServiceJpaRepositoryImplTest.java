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
package org.apache.fineract.portfolio.client.service;

import static org.mockito.Mockito.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.Collections;
import org.apache.fineract.infrastructure.codes.domain.CodeValue;
import org.apache.fineract.infrastructure.codes.domain.CodeValueRepositoryWrapper;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.dataqueries.service.EntityDatatableChecksWritePlatformService;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientCloseBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientReactivateBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientUndoRejectionBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientUndoWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientWithdrawBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.api.ClientApiConstants;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientStatus;
import org.apache.fineract.portfolio.client.domain.LegalForm;
import org.apache.fineract.portfolio.loanaccount.domain.LoanRepositoryWrapper;
import org.apache.fineract.portfolio.savings.domain.SavingsAccountRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private ClientDataValidator fromApiJsonDeserializer;
    @Mock
    private PlatformSecurityContext context;
    @Mock
    private CodeValueRepositoryWrapper codeValueRepository;
    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;
    @Mock
    private LoanRepositoryWrapper loanRepositoryWrapper;
    @Mock
    private SavingsAccountRepositoryWrapper savingsRepositoryWrapper;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private EntityDatatableChecksWritePlatformService entityDatatableChecksWritePlatformService;
    @InjectMocks
    private ClientWritePlatformServiceJpaRepositoryImpl clientWritePlatformService;

    @Test
    void shouldPublishClientCloseBusinessEventWhenClientIsClosed() {
        Long clientId = 1L;
        Long closureReasonId = 2L;
        LocalDate closureDate = LocalDate.of(2026, 7, 30);
        JsonCommand command = mock(JsonCommand.class);
        AppUser currentUser = mock(AppUser.class);
        Client currentClient = mock(Client.class);
        CodeValue closureReason = mock(CodeValue.class);
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(clientId)).thenReturn(currentClient);
        when(command.localDateValueOfParameterNamed(ClientApiConstants.closureDateParamName)).thenReturn(closureDate);
        when(command.longValueOfParameterNamed(ClientApiConstants.closureReasonIdParamName)).thenReturn(closureReasonId);
        when(codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_CLOSURE_REASON, closureReasonId))
                .thenReturn(closureReason);
        // client can be closed
        when(currentClient.getStatus()).thenReturn(ClientStatus.ACTIVE.getValue());
        when(currentClient.isNotPending()).thenReturn(Boolean.FALSE);
        when(currentClient.getLegalForm()).thenReturn(LegalForm.PERSON.getValue());
        // no opening loan and savings
        when(loanRepositoryWrapper.findLoanByClientId(clientId)).thenReturn(Collections.emptyList());
        when(savingsRepositoryWrapper.findSavingAccountByClientId(clientId)).thenReturn(Collections.emptyList());
        clientWritePlatformService.closeClient(clientId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientCloseBusinessEvent.class));
    }

    @Test
    void shouldPublishClientReactivateBusinessEventWhenClientReactivated() {
        Long entityId = 1L;
        JsonCommand command = mock(JsonCommand.class);
        Client currentClient = mock(Client.class);
        LocalDate reactivateDate = LocalDate.of(2026, 7, 30);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(entityId)).thenReturn(currentClient);
        when(command.localDateValueOfParameterNamed(ClientApiConstants.reactivationDateParamName)).thenReturn(reactivateDate);
        when(currentClient.isClosed()).thenReturn(Boolean.TRUE);
        when(currentClient.getClosureDate()).thenReturn(reactivateDate);
        clientWritePlatformService.reActivateClient(entityId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientReactivateBusinessEvent.class));
    }

    @Test
    void shouldPublishClientUndoRejectionBusinessEventWhenClientUndoRejection() {
        Long entityId = 1L;
        JsonCommand command = mock(JsonCommand.class);
        Client currentClient = mock(Client.class);
        LocalDate undoRejectDate = LocalDate.of(2026, 7, 30);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(entityId)).thenReturn(currentClient);
        when(command.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName)).thenReturn(undoRejectDate);
        when(currentClient.isRejected()).thenReturn(Boolean.TRUE);
        when(currentClient.getRejectedDate()).thenReturn(undoRejectDate);
        clientWritePlatformService.undoRejection(entityId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientUndoRejectionBusinessEvent.class));
    }

    @Test
    void shouldPublishClientWithdrawBusinessEventWhenClientWithdrawn() {
        Long entityId = 1L;
        Long withdrawalReasonId = 2L;
        LocalDate withdrawalDate = LocalDate.of(2026, 7, 30);
        JsonCommand command = mock(JsonCommand.class);
        Client currentClient = mock(Client.class);
        CodeValue withdrawalReason = mock(CodeValue.class);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(entityId)).thenReturn(currentClient);
        when(command.localDateValueOfParameterNamed(ClientApiConstants.withdrawalDateParamName)).thenReturn(withdrawalDate);
        when(command.longValueOfParameterNamed(ClientApiConstants.withdrawalReasonIdParamName)).thenReturn(withdrawalReasonId);
        when(codeValueRepository.findOneByCodeNameAndIdWithNotFoundDetection(ClientApiConstants.CLIENT_WITHDRAW_REASON, withdrawalReasonId))
                .thenReturn(withdrawalReason);
        when(currentClient.isNotPending()).thenReturn(Boolean.FALSE);
        when(currentClient.getSubmittedOnDate()).thenReturn(withdrawalDate);
        clientWritePlatformService.withdrawClient(entityId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientWithdrawBusinessEvent.class));
    }

    @Test
    void shouldPublishClientUndoWithdrawalBusinessEventWhenClientWithdrawalUndone() {
        Long entityId = 1L;
        LocalDate undoWithdrawalDate = LocalDate.of(2026, 7, 30);
        JsonCommand command = mock(JsonCommand.class);
        Client currentClient = mock(Client.class);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(entityId)).thenReturn(currentClient);
        when(command.localDateValueOfParameterNamed(ClientApiConstants.reopenedDateParamName)).thenReturn(undoWithdrawalDate);
        when(currentClient.isWithdrawn()).thenReturn(Boolean.TRUE);
        when(currentClient.getWithdrawalDate()).thenReturn(undoWithdrawalDate);
        clientWritePlatformService.undoWithdrawal(entityId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientUndoWithdrawalBusinessEvent.class));
    }
}
