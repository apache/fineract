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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.event.business.domain.client.ClientUndoWithdrawalBusinessEvent;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.infrastructure.security.service.PlatformSecurityContext;
import org.apache.fineract.portfolio.client.data.ClientDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.useradministration.domain.AppUser;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private ClientRepositoryWrapper clientRepository;

    @Mock
    private BusinessEventNotifierService businessEventNotifierService;

    @Mock
    private PlatformSecurityContext context;

    @Mock
    private ClientDataValidator fromApiJsonDeserializer;

    @InjectMocks
    private ClientWritePlatformServiceJpaRepositoryImpl underTest;

    @Test
    public void testUndoWithdrawalFiresBusinessEvent() {
        Long clientId = 1L;
        Client client = mock(Client.class);
        AppUser currentUser = mock(AppUser.class);
        JsonCommand command = mock(JsonCommand.class);
        LocalDate dummyDate = LocalDate.now();
        when(clientRepository.findOneWithNotFoundDetection(clientId)).thenReturn(client);
        when(context.authenticatedUser()).thenReturn(currentUser);
        when(command.localDateValueOfParameterNamed(any(String.class))).thenReturn(dummyDate);
        when(client.isWithdrawn()).thenReturn(true);
        this.underTest.undoWithdrawal(clientId, command);
        verify(businessEventNotifierService).notifyPostBusinessEvent(any(ClientUndoWithdrawalBusinessEvent.class));
        verify(clientRepository).saveAndFlush(client);
    }
}
