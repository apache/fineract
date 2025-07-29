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
package org.apache.fineract.organisation.monetary.service;

import static org.junit.jupiter.api.Assertions.assertEquals;

import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrencyWritePlatformServiceJpaRepositoryImplTest {

    private List<CurrencyCreateRequest> currenciesGood;

    @InjectMocks
    private CurrencyWritePlatformServiceJpaRepositoryImpl underTest;

    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Mock
    private CurrencyMapper currencyMapper;

    @BeforeEach
    void setUp() throws Exception {
        currenciesGood = List.of(
                CurrencyCreateRequest.builder().code("AAA").decimalPlaces(0).inMultiplesOf(1).displaySymbol("∑").name("Currency1")
                        .nameCode("currency.AAA").build(),

                CurrencyCreateRequest.builder().code("AAB").decimalPlaces(1).inMultiplesOf(10).displaySymbol("∏").name("Currency2")
                        .nameCode("currency.AAB").build(),

                CurrencyCreateRequest.builder().code("AAC").decimalPlaces(2).inMultiplesOf(1000).displaySymbol("∅").name("Currency3")
                        .nameCode("currency.AAC").build(),

                CurrencyCreateRequest.builder().code("AAD").decimalPlaces(3).inMultiplesOf(0).displaySymbol("∞").name("Currency4")
                        .nameCode("currency.AAD").build());
    }

    @Test
    void testHappyPathForGoodData() {
        for (CurrencyCreateRequest element : currenciesGood) {
            ApplicationCurrency currency = currencyMapper.mapToEntity(element);
            Mockito.when(applicationCurrencyRepository.save(Mockito.refEq(currency))).thenReturn(currency);
            CurrencyCreateResponse response = currencyMapper.mapToResponse(currency);
            assertEquals(underTest.createCurrency(element), response);
        }
    }
}
