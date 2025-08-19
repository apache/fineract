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
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.util.stream.Stream;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.domain.OrganisationCurrencyRepository;
import org.apache.fineract.organisation.monetary.mapper.CurrencyMapper;
import org.apache.fineract.portfolio.charge.service.ChargeReadPlatformService;
import org.apache.fineract.portfolio.loanproduct.service.LoanProductReadPlatformService;
import org.apache.fineract.portfolio.savings.service.SavingsProductReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mapstruct.factory.Mappers;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class CurrencyWritePlatformServiceJpaRepositoryImplTest {

    @InjectMocks
    private CurrencyWritePlatformServiceJpaRepositoryImpl underTest;

    @Mock
    private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

    @Mock
    private OrganisationCurrencyRepository organisationCurrencyRepository;

    @Mock
    private LoanProductReadPlatformService loanProductService;

    @Mock
    private SavingsProductReadPlatformService savingsProductService;

    @Mock
    private ChargeReadPlatformService chargeService;

    private final CurrencyMapper currencyMapper = Mappers.getMapper(CurrencyMapper.class);

    @BeforeEach
    void setUp() {
        underTest = new CurrencyWritePlatformServiceJpaRepositoryImpl(applicationCurrencyRepository, organisationCurrencyRepository,
                loanProductService, savingsProductService, chargeService, currencyMapper);
    }

    @ParameterizedTest
    @MethodSource("validCurrencyProvider")
    void createCurrency_HappyPath(CurrencyCreateRequest element) {
        ApplicationCurrency currency = currencyMapper.mapToEntity(element);
        currency.setNameCode("currency." + currency.getCode());

        Mockito.when(applicationCurrencyRepository.saveCurrency(Mockito.refEq(currency))).thenReturn(currency);

        CurrencyCreateResponse expected = currencyMapper.mapToResponse(currency);
        expected.setDisplayLabel(computeDisplayLabel(currency));
        CurrencyCreateResponse actual = underTest.createCurrency(element);
        assertEquals(expected, actual);
    }

    @ParameterizedTest
    @MethodSource("invalidCurrencyProvider")
    void createCurrency_InvalidCurrencies(CurrencyCreateRequest element) {
        assertThrows(Exception.class, () -> underTest.createCurrency(element),
                "Expected InvalidCurrencyException for invalid currency request: " + element);
    }

    static Stream<CurrencyCreateRequest> validCurrencyProvider() {
        return Stream.of(
                CurrencyCreateRequest.builder().code("AAA").decimalPlaces(0).inMultiplesOf(1).displaySymbol("∑").name("Currency1").build(),
                CurrencyCreateRequest.builder().code("AAB").decimalPlaces(1).inMultiplesOf(10).displaySymbol("∏").name("Currency2").build(),
                CurrencyCreateRequest.builder().code("AAC").decimalPlaces(2).inMultiplesOf(1000).displaySymbol("∅").name("Currency3")
                        .build(),
                CurrencyCreateRequest.builder().code("AAD").decimalPlaces(3).inMultiplesOf(0).displaySymbol("∞").name("Currency4").build());
    }

    static Stream<CurrencyCreateRequest> invalidCurrencyProvider() {
        return Stream.of(
                CurrencyCreateRequest.builder().code("USD").decimalPlaces(null).inMultiplesOf(null).displaySymbol(null).name("CurrencyUSD")
                        .build(),

                CurrencyCreateRequest.builder().code("AAA").decimalPlaces(null).inMultiplesOf(null).displaySymbol(null).name("Currencyaaa")
                        .build(),

                CurrencyCreateRequest.builder().code("XYZ").decimalPlaces(null).inMultiplesOf(null).displaySymbol(null).name("CurrencyXYZ")
                        .build(),

                CurrencyCreateRequest.builder().code("aaa").decimalPlaces(-1).inMultiplesOf(null).displaySymbol(null).name("Currency1")
                        .build(),

                CurrencyCreateRequest.builder().code("aaa").decimalPlaces(null).inMultiplesOf(-1).displaySymbol(null).name("Currency1")
                        .build(),

                CurrencyCreateRequest.builder().code("AAAA").decimalPlaces(1).inMultiplesOf(10).displaySymbol("∏").name("Currency2")
                        .build(),

                CurrencyCreateRequest.builder().code("AAC").decimalPlaces(null).inMultiplesOf(null).displaySymbol(null).name(
                        "Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3Currency3")
                        .build(),

                CurrencyCreateRequest.builder().code("1234").decimalPlaces(-1).inMultiplesOf(-1).displaySymbol(null).name("Currency")
                        .build(),

                CurrencyCreateRequest.builder().code("AAa").decimalPlaces(-1).inMultiplesOf(-1).displaySymbol(null).name("Currency")
                        .build());
    }

    private String computeDisplayLabel(ApplicationCurrency entity) {
        StringBuilder builder = new StringBuilder(20);

        if (entity.getName() != null) {
            builder.append(entity.getName()).append(' ');
        }

        if (entity.getDisplaySymbol() != null && !entity.getDisplaySymbol().trim().isEmpty()) {
            builder.append('(').append(entity.getDisplaySymbol()).append(')');
        } else {
            builder.append('[').append(entity.getCode()).append(']');
        }
        return builder.toString();
    }
}
