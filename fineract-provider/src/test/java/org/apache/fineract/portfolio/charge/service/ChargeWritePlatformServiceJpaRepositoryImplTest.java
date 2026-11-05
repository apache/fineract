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

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.portfolio.charge.domain.Charge;
import org.apache.fineract.portfolio.charge.domain.ChargeRepository;
import org.apache.fineract.portfolio.charge.exception.ChargeCannotBeUpdatedException;
import org.apache.fineract.portfolio.charge.serialization.ChargeDefinitionCommandFromApiJsonDeserializer;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ChargeWritePlatformServiceJpaRepositoryImplTest {

    private static final Long CHARGE_ID = 1L;

    @Mock
    private ChargeDefinitionCommandFromApiJsonDeserializer fromApiJsonDeserializer;
    @Mock
    private ChargeRepository chargeRepository;
    @Mock
    private LoanProductRepository loanProductRepository;
    @Mock
    private JsonCommand command;
    @Mock
    private Charge charge;

    @InjectMocks
    private ChargeWritePlatformServiceJpaRepositoryImpl underTest;

    @BeforeEach
    void setUp() {
        when(chargeRepository.findById(CHARGE_ID)).thenReturn(Optional.of(charge));
    }

    @Test
    void testChangingChargeToDownPaymentIsRejectedWhenAttachedToProductWithoutDownPaymentSupport() {
        givenChargeTimeTypeChangedTo(true);
        final List<LoanProduct> products = List.of(product(true), product(false));
        when(loanProductRepository.retrieveLoanProductsByChargeId(CHARGE_ID)).thenReturn(products);

        assertThrows(ChargeCannotBeUpdatedException.class, () -> underTest.updateCharge(CHARGE_ID, command));
        verify(chargeRepository, never()).save(any());
    }

    @Test
    void testChangingChargeToDownPaymentIsAllowedWhenAllProductsSupportDownPayment() {
        givenChargeTimeTypeChangedTo(true);
        final List<LoanProduct> products = List.of(product(true));
        when(loanProductRepository.retrieveLoanProductsByChargeId(CHARGE_ID)).thenReturn(products);

        assertDoesNotThrow(() -> underTest.updateCharge(CHARGE_ID, command));
        verify(chargeRepository).save(charge);
    }

    @Test
    void testProductsAreNotCheckedWhenChargeTimeTypeIsNotChangedToDownPayment() {
        givenChargeTimeTypeChangedTo(false);

        assertDoesNotThrow(() -> underTest.updateCharge(CHARGE_ID, command));
        verify(loanProductRepository, never()).retrieveLoanProductsByChargeId(any());
    }

    private void givenChargeTimeTypeChangedTo(final boolean downPayment) {
        when(charge.update(command)).thenReturn(Map.of(Charge.CHARGE_TIME_PARAM_NAME, 2));
        when(charge.isDownPaymentCharge()).thenReturn(downPayment);
    }

    private LoanProduct product(final boolean supportsDownPaymentCharges) {
        final LoanProduct product = mock(LoanProduct.class);
        when(product.supportsDownPaymentCharges()).thenReturn(supportsDownPaymentCharges);
        return product;
    }
}
