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
package org.apache.fineract.portfolio.collateralmanagement.domain;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.RETURNS_DEEP_STUBS;
import static org.mockito.Mockito.lenient;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

import java.math.BigDecimal;
import java.util.List;
import java.util.Optional;
import org.apache.fineract.portfolio.client.domain.Client;
import org.apache.fineract.portfolio.client.domain.ClientRepositoryWrapper;
import org.apache.fineract.portfolio.collateralmanagement.data.ClientCollateralManagementData;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProduct;
import org.apache.fineract.portfolio.loanproduct.domain.LoanProductRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class ClientCollateralManagementRepositoryWrapperTest {

    private static final Long CLIENT_ID = 2L;
    private static final Long PRODUCT_ID = 1L;

    @Mock
    private ClientCollateralManagementRepository clientCollateralManagementRepository;

    @Mock
    private ClientRepositoryWrapper clientRepositoryWrapper;

    @Mock
    private LoanProductRepository loanProductRepository;

    @Mock
    private Client client;

    private ClientCollateralManagementRepositoryWrapper wrapper;

    @BeforeEach
    void setUp() {
        wrapper = new ClientCollateralManagementRepositoryWrapper(clientCollateralManagementRepository, clientRepositoryWrapper,
                loanProductRepository);
        when(clientRepositoryWrapper.findOneWithNotFoundDetection(CLIENT_ID)).thenReturn(client);
        final List<ClientCollateralManagement> collaterals = List.of(collateral(10L, "Gold", "USD"), collateral(11L, "Car", "EUR"));
        when(clientCollateralManagementRepository.findByClientId(client)).thenReturn(collaterals);
    }

    @Test
    void listsEveryCollateralWhenNoLoanProductIsGiven() {
        final List<ClientCollateralManagementData> collaterals = wrapper.getClientCollateralData(CLIENT_ID, null);

        assertEquals(List.of(10L, 11L), collaterals.stream().map(ClientCollateralManagementData::getId).toList());
    }

    @Test
    void listsOnlyCollateralsInTheLoanProductCurrency() {
        final LoanProduct loanProduct = mock(LoanProduct.class, RETURNS_DEEP_STUBS);
        when(loanProduct.getCurrency().getCode()).thenReturn("USD");
        when(loanProductRepository.findById(PRODUCT_ID)).thenReturn(Optional.of(loanProduct));

        final List<ClientCollateralManagementData> collaterals = wrapper.getClientCollateralData(CLIENT_ID, PRODUCT_ID);

        assertEquals(List.of(10L), collaterals.stream().map(ClientCollateralManagementData::getId).toList());
    }

    private static ClientCollateralManagement collateral(final Long id, final String name, final String currencyCode) {
        final ClientCollateralManagement collateral = mock(ClientCollateralManagement.class, RETURNS_DEEP_STUBS);
        when(collateral.getQuantity()).thenReturn(BigDecimal.ONE);
        when(collateral.getTotal()).thenReturn(BigDecimal.TEN);
        // Lenient: the currency is only read when a loan product is given, and the id and name only for listed
        // collaterals.
        lenient().when(collateral.getId()).thenReturn(id);
        lenient().when(collateral.getCollaterals().getName()).thenReturn(name);
        lenient().when(collateral.getCollaterals().getCurrency().getCode()).thenReturn(currencyCode);
        return collateral;
    }
}
