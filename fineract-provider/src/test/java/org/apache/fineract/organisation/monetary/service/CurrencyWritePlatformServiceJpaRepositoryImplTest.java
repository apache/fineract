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

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

import java.io.InputStream;
import java.util.List;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateRequest;
import org.apache.fineract.organisation.monetary.data.CurrencyCreateResponse;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrency;
import org.apache.fineract.organisation.monetary.domain.ApplicationCurrencyRepositoryWrapper;
import org.apache.fineract.organisation.monetary.exception.InvalidCurrencyException;
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
public class CurrencyWritePlatformServiceJpaRepositoryImplTest {

     private List<CurrencyCreateRequest> currenciesGood;
     private List<CurrencyCreateRequest> currenciesCorrupted;

     @InjectMocks
     private CurrencyWritePlatformServiceJpaRepositoryImpl underTest;

     @Mock
     private ApplicationCurrencyRepositoryWrapper applicationCurrencyRepository;

     @Mock
     private CurrencyMapper currencyMapper;

     @BeforeEach
     void setUp() throws Exception {
       ObjectMapper objectMapper = new ObjectMapper();
       InputStream inputStream = getClass().getClassLoader().getResourceAsStream("test-resources/currenciesGoodData.json");
       currenciesGood = objectMapper.readValue(inputStream, new TypeReference<List<CurrencyCreateRequest>>() {});
       inputStream = getClass().getClassLoader().getResourceAsStream("test-resources/currenciesCorruptedData.json");
       currenciesCorrupted = objectMapper.readValue(inputStream, new TypeReference<List<CurrencyCreateRequest>>() {});
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

    @Test
    void testCorruptedDataShouldThrowException() {
         for (CurrencyCreateRequest element : currenciesCorrupted) {
           Mockito.when(applicationCurrencyRepository.existsByCode(element.getCode())).thenReturn(false);
           
           // Assert the validation throws exception
           InvalidCurrencyException e = assertThrows(InvalidCurrencyException.class, () -> {
               underTest.createCurrency(element);
           });
         }
    }
}
