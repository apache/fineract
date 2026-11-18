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
package org.apache.fineract.portfolio.charge.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.Optional;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.domain.ChargeRepositoryWrapper;
import org.apache.fineract.portfolio.charge.exception.ChargeIsNotActiveException;
import org.apache.fineract.portfolio.charge.exception.ChargeNotFoundException;
import org.apache.fineract.portfolio.client.contract.ClientChargeDefinitionData;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientChargeReadServiceImplTest {

    private static final Long CHARGE_ID = 7L;

    @Mock
    private ChargeRepository chargeRepository;

    private ClientChargeReadServiceImpl underTest;

    @BeforeEach
    void setUp() {
        underTest = new ClientChargeReadServiceImpl(chargeRepository, new ChargeRepositoryWrapper(chargeRepository));
    }

    @Test
    void existingClientChargeResolvesInactiveChargeDefinition() {
        Charge charge = charge(false, false);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));

        ClientChargeDefinitionData result = underTest.retrieveClientChargeDefinition(CHARGE_ID);

        assertEquals(CHARGE_ID, result.chargeId());
        assertEquals("USD", result.currencyCode());
        assertEquals(42L, result.incomeAccountId());
    }

    @Test
    void existingClientChargeResolvesDeletedChargeDefinition() {
        Charge charge = charge(true, true);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));

        ClientChargeDefinitionData result = underTest.retrieveClientChargeDefinition(CHARGE_ID);

        assertEquals(CHARGE_ID, result.chargeId());
        assertEquals("USD", result.currencyCode());
    }

    @Test
    void existingClientChargeFailsWhenChargeDefinitionIsMissing() {
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.empty());

        assertThrows(ChargeNotFoundException.class, () -> underTest.retrieveClientChargeDefinition(CHARGE_ID));
    }

    @Test
    void newClientChargeRejectsInactiveChargeDefinition() {
        Charge charge = mock(Charge.class);
        when(charge.isActive()).thenReturn(false);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));

        assertThrows(ChargeIsNotActiveException.class, () -> underTest.retrieveApplicableClientChargeDefinition(CHARGE_ID));
    }

    @Test
    void newClientChargeRejectsDeletedChargeDefinition() {
        Charge charge = mock(Charge.class);
        when(charge.isDeleted()).thenReturn(true);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));

        assertThrows(ChargeNotFoundException.class, () -> underTest.retrieveApplicableClientChargeDefinition(CHARGE_ID));
    }

    @Test
    void newClientChargeResolvesActiveChargeDefinition() {
        Charge charge = charge(false, true);
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));

        ClientChargeDefinitionData result = underTest.retrieveApplicableClientChargeDefinition(CHARGE_ID);

        assertEquals(CHARGE_ID, result.chargeId());
        assertEquals(BigDecimal.TEN, result.amount());
    }

    private static Charge charge(boolean deleted, boolean active) {
        Charge charge = mock(Charge.class);
        when(charge.getId()).thenReturn(CHARGE_ID);
        when(charge.getCurrencyCode()).thenReturn("USD");
        when(charge.getAmount()).thenReturn(BigDecimal.TEN);
        when(charge.getIncomeAccountId()).thenReturn(42L);
        lenient().when(charge.isDeleted()).thenReturn(deleted);
        lenient().when(charge.isActive()).thenReturn(active);
        return charge;
    }
}
