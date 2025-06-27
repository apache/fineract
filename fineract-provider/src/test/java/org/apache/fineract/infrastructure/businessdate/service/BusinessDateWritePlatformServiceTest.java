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

import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.BUSINESS_DATE;
import static org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType.COB_DATE;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateResponse;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateUpdateRequest;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateActionException;
import org.apache.fineract.infrastructure.businessdate.mapper.BusinessDateUpdateRequestMapper;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.DateUtils;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.exception.JobExecutionException;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Captor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class BusinessDateWritePlatformServiceTest {

    @InjectMocks
    private BusinessDateWritePlatformServiceImpl underTest;

    @Mock
    private BusinessDateUpdateRequestMapper updateBusinessDateMapper;

    @Mock
    private BusinessDateRepository businessDateRepository;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Captor
    private ArgumentCaptor<BusinessDate> businessDateArgumentCaptor;

    @BeforeEach
    public void init() {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void businessDateIsNotEnabled() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.FALSE);

        BusinessDateActionException exception = assertThrows(BusinessDateActionException.class,
                () -> underTest.updateBusinessDate(businessDateUpdateRequest));
        assertEquals("Business date functionality is not enabled", exception.getDefaultUserMessage());
    }

    @Test
    public void businessDateSetNew() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 13);
        final Map<BusinessDateType, LocalDate> changes = new HashMap<>(Map.of(BUSINESS_DATE, localDate));
        BusinessDateResponse businessDateRes = BusinessDateResponse.builder().type(BUSINESS_DATE)
                .description(BUSINESS_DATE.getDescription()).date(localDate).changes(changes).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(updateBusinessDateMapper.map(businessDateUpdateRequest)).willReturn(businessDateRes);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(businessDateUpdateRequest);
        final LocalDate resultDate = result.getChanges().get(BUSINESS_DATE);
        assertEquals(LocalDate.of(2022, 6, 13), resultDate);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void cobDateSetNew() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 13);
        final Map<BusinessDateType, LocalDate> changes = new HashMap<>(Map.of(COB_DATE, localDate));
        BusinessDateResponse response = BusinessDateResponse.builder().type(COB_DATE).description(COB_DATE.getDescription()).date(localDate)
                .changes(changes).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(updateBusinessDateMapper.map(businessDateUpdateRequest)).willReturn(response);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(COB_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(businessDateUpdateRequest);
        LocalDate resultData = result.getChanges().get(COB_DATE);
        assertEquals(LocalDate.of(2022, 6, 13), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(COB_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(COB_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingWhenItWasAfter() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 11);
        final Map<BusinessDateType, LocalDate> changes = new HashMap<>(Map.of(COB_DATE, localDate));
        BusinessDateResponse response = BusinessDateResponse.builder().type(BUSINESS_DATE).description(BUSINESS_DATE.getDescription())
                .date(localDate).changes(changes).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(updateBusinessDateMapper.map(businessDateUpdateRequest)).willReturn(response);
        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BUSINESS_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(businessDateUpdateRequest);
        LocalDate resultData = result.getChanges().get(BUSINESS_DATE);
        assertEquals(LocalDate.of(2022, 6, 11), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 11), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingWhenItWasBefore() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 13);
        final Map<BusinessDateType, LocalDate> changes = new HashMap<>(Map.of(BUSINESS_DATE, localDate));
        BusinessDateResponse response = BusinessDateResponse.builder().type(BUSINESS_DATE).description(BUSINESS_DATE.getDescription())
                .date(localDate).changes(changes).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(updateBusinessDateMapper.map(businessDateUpdateRequest)).willReturn(response);
        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BUSINESS_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(businessDateUpdateRequest);
        LocalDate resultData = result.getChanges().get(BUSINESS_DATE);
        assertEquals(LocalDate.of(2022, 6, 13), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingButNoChanges() {
        BusinessDateUpdateRequest businessDateUpdateRequest = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 13);
        BusinessDateResponse response = BusinessDateResponse.builder().type(BUSINESS_DATE).description(BUSINESS_DATE.getDescription())
                .date(localDate).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(updateBusinessDateMapper.map(businessDateUpdateRequest)).willReturn(response);

        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BUSINESS_DATE, LocalDate.of(2022, 6, 13)));
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(businessDateUpdateRequest);
        assertNull(result.getChanges());
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(0)).save(businessDateArgumentCaptor.capture());
    }

    @Test
    public void cobDateSetNewAutomatically() {
        BusinessDateUpdateRequest request = new BusinessDateUpdateRequest();
        final LocalDate localDate = LocalDate.of(2022, 6, 13);
        final Map<BusinessDateType, LocalDate> changes = new HashMap<>(Map.of(BUSINESS_DATE, localDate));
        BusinessDateResponse response = BusinessDateResponse.builder().type(BUSINESS_DATE).description(BUSINESS_DATE.getDescription())
                .date(localDate).changes(changes).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        given(updateBusinessDateMapper.map(request)).willReturn(response);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(request);
        LocalDate businessDate = result.getChanges().get(BUSINESS_DATE);
        assertEquals(LocalDate.of(2022, 6, 13), businessDate);
        LocalDate cobDate = result.getChanges().get(COB_DATE);
        assertEquals(LocalDate.of(2022, 6, 12), cobDate);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(1)).findByType(COB_DATE);
        verify(businessDateRepository, times(2)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getAllValues().get(0).getDate());
        assertEquals(BUSINESS_DATE, businessDateArgumentCaptor.getAllValues().get(0).getType());
        assertEquals(LocalDate.of(2022, 6, 12), businessDateArgumentCaptor.getAllValues().get(1).getDate());
        assertEquals(COB_DATE, businessDateArgumentCaptor.getAllValues().get(1).getType());
    }

    @Test
    public void businessDateAndCobDateSetModifyExistingButNoChanges() {
        BusinessDateUpdateRequest request = new BusinessDateUpdateRequest();
        BusinessDateResponse response = BusinessDateResponse.builder().type(BUSINESS_DATE).description(BUSINESS_DATE.getDescription())
                .date(LocalDate.of(2022, 6, 13)).build();
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        given(updateBusinessDateMapper.map(request)).willReturn(response);

        Optional<BusinessDate> newBusinessEntity = Optional.of(BusinessDate.instance(BUSINESS_DATE, LocalDate.of(2022, 6, 13)));
        Optional<BusinessDate> newCOBEntity = Optional.of(BusinessDate.instance(COB_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newBusinessEntity);
        given(businessDateRepository.findByType(COB_DATE)).willReturn(newCOBEntity);
        BusinessDateResponse result = underTest.updateBusinessDate(request);
        assertNull(result.getChanges());
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BUSINESS_DATE);
        verify(businessDateRepository, times(1)).findByType(COB_DATE);
        verify(businessDateRepository, times(0)).save(Mockito.any());
    }

    @Test
    public void businessDateIsNotEnabledTriggeredByJob() {
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.FALSE);
        assertThrows(JobExecutionException.class, () -> underTest.increaseDateByTypeByOneDay(BUSINESS_DATE));
    }

    @Test
    public void businessDateSetNewTriggeredByJob() throws JobExecutionException {
        LocalDate localDate = DateUtils.getLocalDateOfTenant();
        LocalDate localDatePlus1 = localDate.plusDays(1);
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BUSINESS_DATE)).willReturn(newEntity);
        underTest.increaseDateByTypeByOneDay(BUSINESS_DATE);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(2)).save(businessDateArgumentCaptor.capture());
        assertEquals(localDatePlus1, businessDateArgumentCaptor.getAllValues().get(0).getDate());
        assertEquals(BUSINESS_DATE, businessDateArgumentCaptor.getAllValues().get(0).getType());
        assertEquals(localDate, businessDateArgumentCaptor.getAllValues().get(1).getDate());
        assertEquals(COB_DATE, businessDateArgumentCaptor.getAllValues().get(1).getType());
    }

    @Test
    public void cobDateModifyExistingTriggeredByJob() throws JobExecutionException {
        Optional<BusinessDate> newCOBEntity = Optional.of(BusinessDate.instance(COB_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(COB_DATE)).willReturn(newCOBEntity);
        LocalDate localDate = LocalDate.of(2022, 6, 12).plusDays(1);
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        underTest.increaseDateByTypeByOneDay(COB_DATE);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(localDate, businessDateArgumentCaptor.getValue().getDate());
    }
}
