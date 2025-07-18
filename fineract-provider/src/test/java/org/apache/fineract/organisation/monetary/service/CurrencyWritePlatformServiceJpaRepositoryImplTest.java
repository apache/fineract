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

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
public class CurrencyWritePlatformServiceJpaRepositoryImplTest {

    // private List<CurrencyData> currenciesGood;
    // private List<CurrencyData> currenciesCorrupted;

    // @InjectMocks
    // private CurrencyWritePlatformServiceJpaRepositoryImpl underTest;

    // @Mock
    // private CreateCurrencyRepository createCurrencyRepository;

    // @BeforeEach
    // void setUp() throws Exception {
    // ObjectMapper objectMapper = new ObjectMapper();
    // InputStream inputStream =
    // getClass().getClassLoader().getResourceAsStream("test-resources/currenciesGoodData.json");
    // currenciesGood = objectMapper.readValue(inputStream, new TypeReference<List<CurrencyData>>() {});
    // inputStream = getClass().getClassLoader().getResourceAsStream("test-resources/currenciesCorruptedData.json");
    // currenciesCorrupted = objectMapper.readValue(inputStream, new TypeReference<List<CurrencyData>>() {});
    // }

    @Disabled
    @Test
    void testHappyPathForGoodData() {
        // for (CurrencyData element : currenciesGood) {
        // CreateCurrency currency = CreateCurrency.fromCurrencyData(element);
        // Mockito.when(createCurrencyRepository.save(Mockito.refEq(currency))).thenReturn(currency);
        // assertEquals(underTest.createCurrency(element), element);
        // }
    }

    @Disabled
    @Test
    void testCorruptedDataShouldThrowException() {
        // for (CurrencyData element : currenciesCorrupted) {
        // Throwable thrown = assertThrows(Throwable.class, () -> {
        // underTest.createCurrency(element);
        // });
        // }
    }
}
