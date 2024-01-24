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
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.Optional;
import org.apache.fineract.infrastructure.businessdate.data.BusinessDateData;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDate;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateRepository;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.businessdate.exception.BusinessDateActionException;
import org.apache.fineract.infrastructure.businessdate.validator.BusinessDateDataParserAndValidator;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
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
    private BusinessDateDataParserAndValidator businessDateDataParserAndValidator;

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
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE,
                LocalDate.now(ZoneId.systemDefault()));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        BusinessDateActionException exception = assertThrows(BusinessDateActionException.class,
                () -> underTest.updateBusinessDate(command));
        assertEquals("Business date functionality is not enabled", exception.getDefaultUserMessage());
    }

    @Test
    public void businessDateSetNew() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        LocalDate resultData = (LocalDate) result.getChanges().get("BUSINESS_DATE");
        assertEquals(LocalDate.of(2022, 6, 13), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BusinessDateType.BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void cobDateSetNew() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.COB_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BusinessDateType.COB_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        LocalDate resultData = (LocalDate) result.getChanges().get("COB_DATE");
        assertEquals(LocalDate.of(2022, 6, 13), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.COB_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BusinessDateType.COB_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingWhenItWasAfter() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 11));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        LocalDate resultData = (LocalDate) result.getChanges().get("BUSINESS_DATE");
        assertEquals(LocalDate.of(2022, 6, 11), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 11), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BusinessDateType.BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingWhenItWasBefore() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        LocalDate resultData = (LocalDate) result.getChanges().get("BUSINESS_DATE");
        assertEquals(LocalDate.of(2022, 6, 13), resultData);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getValue().getDate());
        assertEquals(BusinessDateType.BUSINESS_DATE, businessDateArgumentCaptor.getValue().getType());
    }

    @Test
    public void businessDateSetModifyExistingButNoChanges() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.FALSE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.of(BusinessDate.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13)));
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        assertNull(result.getChanges());
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(0)).save(businessDateArgumentCaptor.capture());
    }

    @Test
    public void cobDateSetNewAutomatically() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        LocalDate businessDate = (LocalDate) result.getChanges().get("BUSINESS_DATE");
        assertEquals(LocalDate.of(2022, 6, 13), businessDate);
        LocalDate cobDate = (LocalDate) result.getChanges().get("COB_DATE");
        assertEquals(LocalDate.of(2022, 6, 12), cobDate);
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.COB_DATE);
        verify(businessDateRepository, times(2)).save(businessDateArgumentCaptor.capture());
        assertEquals(LocalDate.of(2022, 6, 13), businessDateArgumentCaptor.getAllValues().get(0).getDate());
        assertEquals(BusinessDateType.BUSINESS_DATE, businessDateArgumentCaptor.getAllValues().get(0).getType());
        assertEquals(LocalDate.of(2022, 6, 12), businessDateArgumentCaptor.getAllValues().get(1).getDate());
        assertEquals(BusinessDateType.COB_DATE, businessDateArgumentCaptor.getAllValues().get(1).getType());
    }

    @Test
    public void businessDateAndCobDateSetModifyExistingButNoChanges() {
        JsonCommand command = JsonCommand.from("");
        BusinessDateData businessDateData = BusinessDateData.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13));
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        given(businessDateDataParserAndValidator.validateAndParseUpdate(command)).willReturn(businessDateData);
        Optional<BusinessDate> newBusinessEntity = Optional
                .of(BusinessDate.instance(BusinessDateType.BUSINESS_DATE, LocalDate.of(2022, 6, 13)));
        Optional<BusinessDate> newCOBEntity = Optional.of(BusinessDate.instance(BusinessDateType.COB_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newBusinessEntity);
        given(businessDateRepository.findByType(BusinessDateType.COB_DATE)).willReturn(newCOBEntity);
        CommandProcessingResult result = underTest.updateBusinessDate(command);
        assertNull(result.getChanges());
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.BUSINESS_DATE);
        verify(businessDateRepository, times(1)).findByType(BusinessDateType.COB_DATE);
        verify(businessDateRepository, times(0)).save(Mockito.any());
    }

    @Test
    public void businessDateIsNotEnabledTriggeredByJob() {
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.FALSE);
        assertThrows(JobExecutionException.class, () -> underTest.increaseBusinessDateByOneDay());
    }

    @Test
    public void businessDateSetNewTriggeredByJob() throws JobExecutionException {
        LocalDate localDate = DateUtils.getLocalDateOfTenant();
        LocalDate localDatePlus1 = localDate.plusDays(1);
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        Optional<BusinessDate> newEntity = Optional.empty();
        given(businessDateRepository.findByType(BusinessDateType.BUSINESS_DATE)).willReturn(newEntity);
        underTest.increaseBusinessDateByOneDay();
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(2)).save(businessDateArgumentCaptor.capture());
        assertEquals(localDatePlus1, businessDateArgumentCaptor.getAllValues().get(0).getDate());
        assertEquals(BusinessDateType.BUSINESS_DATE, businessDateArgumentCaptor.getAllValues().get(0).getType());
        assertEquals(localDate, businessDateArgumentCaptor.getAllValues().get(1).getDate());
        assertEquals(BusinessDateType.COB_DATE, businessDateArgumentCaptor.getAllValues().get(1).getType());
    }

    @Test
    public void cobDateModifyExistingTriggeredByJob() throws JobExecutionException {
        Optional<BusinessDate> newCOBEntity = Optional.of(BusinessDate.instance(BusinessDateType.COB_DATE, LocalDate.of(2022, 6, 12)));
        given(businessDateRepository.findByType(BusinessDateType.COB_DATE)).willReturn(newCOBEntity);
        LocalDate localDate = LocalDate.of(2022, 6, 12).plusDays(1);
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(Boolean.TRUE);
        given(configurationDomainService.isCOBDateAdjustmentEnabled()).willReturn(Boolean.TRUE);
        underTest.increaseCOBDateByOneDay();
        verify(configurationDomainService, times(1)).isBusinessDateEnabled();
        verify(configurationDomainService, times(1)).isCOBDateAdjustmentEnabled();
        verify(businessDateRepository, times(1)).save(businessDateArgumentCaptor.capture());
        assertEquals(localDate, businessDateArgumentCaptor.getValue().getDate());
    }
}
