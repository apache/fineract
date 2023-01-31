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
package org.apache.fineract.infrastructure.businessdate.service;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import java.util.List;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateNotFoundException;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateMapper;
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
public class BusinessDateReadPlatformServiceTest {

    @InjectMocks
    private BusinessDateReadPlatformServiceImpl businessDateReadPlatformService;

    @Mock
    private BusinessDateRepository repository;

    @Mock
    private BusinessDateMapper mapper;

    @Test
    public void notFoundByTypeNonexistentType() {
        BusinessDateNotFoundException businessDateNotFoundException = assertThrows(BusinessDateNotFoundException.class,
                () -> businessDateReadPlatformService.findByType("invalid"));
        assertEquals("Business date with type `invalid` does not exist.", businessDateNotFoundException.getDefaultUserMessage());
    }

    @Test
    public void notFoundByTypeNotStoredInDB() {
        BusinessDateNotFoundException businessDateNotFoundException = assertThrows(BusinessDateNotFoundException.class,
                () -> businessDateReadPlatformService.findByType("BUSINESS_DATE"));
        assertEquals("Business date with type `BUSINESS_DATE` is not found.", businessDateNotFoundException.getDefaultUserMessage());
    }

    @Test
    public void findAll() {
        List<BusinessDate> resultList = Mockito.mock(List.class);
        given(repository.findAll()).willReturn(resultList);
        businessDateReadPlatformService.findAll();
        verify(repository, times(1)).findAll();
        verify(mapper, times(1)).map(resultList);
    }

    @Test
    public void findByCOBType() {
        Optional<BusinessDate> result = Optional.of(Mockito.mock(BusinessDate.class));
        given(repository.findByType(BusinessDateType.COB_DATE)).willReturn(result);
        businessDateReadPlatformService.findByType("COB_DATE");
        verify(repository, times(1)).findByType(BusinessDateType.COB_DATE);
        verify(mapper, times(1)).map(result.get());
    }

    @Test
    public void findByBusinessType() {
        Optional<BusinessDate> result = Optional.of(Mockito.mock(BusinessDate.class));
        given(repository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(result);
        businessDateReadPlatformService.findByType("BUSINESS_DATE");
        verify(repository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(mapper, times(1)).map(result.get());
    }
}
