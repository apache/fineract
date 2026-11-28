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

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.client.data.ClientChargeDataValidator;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientCharge;
import org.apache.fineract.portfolio.client.domain.ClientChargeRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.client.domain.ClientTransactionRepository;
import org.apache.fineract.portfolio.client.exception.ClientChargeNotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class ClientChargeWritePlatformServiceImplTest {

    private static final Long CLIENT_ID = 7L;
    private static final Long OTHER_CLIENT_ID = 8L;
    private static final Long CHARGE_ID = 99L;

    @Mock
    private ClientRepositoryWrapper clientRepository;
    @Mock
    private ClientChargeRepositoryWrapper clientChargeRepository;
    @Mock
    private ClientChargeDataValidator clientChargeDataValidator;
    @Mock
    private ClientTransactionRepository clientTransactionRepository;

    @InjectMocks
    private ClientChargeWritePlatformServiceImpl underTest;

    @BeforeEach
    void setUp() {
        Client client = mock(Client.class);
        when(clientRepository.getActiveClientInUserScope(CLIENT_ID)).thenReturn(client);
        Client otherClient = mock(Client.class);
        when(otherClient.getId()).thenReturn(OTHER_CLIENT_ID);
        ClientCharge charge = mock(ClientCharge.class);
        when(charge.getClient()).thenReturn(otherClient);
        when(clientChargeRepository.findOneWithNotFoundDetection(CHARGE_ID)).thenReturn(charge);
    }

    @Test
    void payChargeRejectsChargeOfAnotherClient() {
        JsonCommand command = mock(JsonCommand.class);
        assertThrows(ClientChargeNotFoundException.class, () -> underTest.payCharge(CLIENT_ID, CHARGE_ID, command));
        verify(clientTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void waiveChargeRejectsChargeOfAnotherClient() {
        assertThrows(ClientChargeNotFoundException.class, () -> underTest.waiveCharge(CLIENT_ID, CHARGE_ID));
        verify(clientTransactionRepository, never()).saveAndFlush(any());
    }

    @Test
    void deleteChargeRejectsChargeOfAnotherClient() {
        assertThrows(ClientChargeNotFoundException.class, () -> underTest.deleteCharge(CLIENT_ID, CHARGE_ID));
        verify(clientChargeRepository, never()).delete(any());
    }
}
