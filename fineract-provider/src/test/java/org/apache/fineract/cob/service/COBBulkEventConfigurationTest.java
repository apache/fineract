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
package org.apache.fineract.cob.service;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import edu.umd.cs.findbugs.annotations.SuppressFBWarnings;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import java.util.TreeMap;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.COBBusinessStepServiceImpl;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.event.business.service.BusinessEventNotifierService;
import org.apache.fineract.portfolio.loanaccount.domain.Loan;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.context.ApplicationContext;

@ExtendWith(MockitoExtension.class)
@SuppressFBWarnings(value = "RV_EXCEPTION_NOT_THROWN", justification = "False positive")
public class COBBulkEventConfigurationTest {

    @Mock
    private BatchBusinessStepRepository batchBusinessStepRepository;
    @Mock
    private ApplicationContext applicationContext;
    @Mock
    private ListableBeanFactory beanFactory;
    @Mock
    private BusinessEventNotifierService businessEventNotifierService;
    @Mock
    private ConfigurationDomainService configurationDomainService;
    @InjectMocks
    private COBBusinessStepServiceImpl underTest;

    @Mock
    private ReloaderService reloaderService;

    @BeforeEach
    public void setUp() throws Exception {
        ThreadLocalContextUtil.setTenant(new FineractPlatformTenant(1L, "default", "Default", "Asia/Kolkata", null));
        ThreadLocalContextUtil.setActionContext(ActionContext.DEFAULT);
        ThreadLocalContextUtil
                .setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, LocalDate.now(ZoneId.systemDefault()))));
        when(reloaderService.reload(any())).thenAnswer(invocation -> invocation.getArgument(0));
    }

    @AfterEach
    public void tearDown() {
        ThreadLocalContextUtil.reset();
    }

    @Test
    public void testGivenBulkEventEnabledWhenCOBRunThenEventsAreRecorded() {
        // given
        Loan loan = mock(Loan.class);
        TreeMap<Long, String> dummyExecutionMap = new TreeMap<>();
        dummyExecutionMap.put(1L, "dummyBusinessStep");
        COBBusinessStep businessStep = mock(COBBusinessStep.class);
        when(configurationDomainService.isCOBBulkEventEnabled()).thenReturn(true);
        when(applicationContext.getBean(anyString())).thenReturn(businessStep);
        when(businessStep.execute(any())).thenReturn(loan);

        // when
        underTest.run(dummyExecutionMap, loan);

        // then
        verify(businessEventNotifierService, times(1)).startExternalEventRecording();
        verify(businessEventNotifierService, times(1)).stopExternalEventRecording();
    }

    @Test
    public void testGivenBulkEventDisabledWhenCOBRunThenEventsAreNotRecorded() {
        // given
        Loan loan = mock(Loan.class);
        TreeMap<Long, String> dummyExecutionMap = new TreeMap<>();
        dummyExecutionMap.put(1L, "dummyBusinessStep");
        COBBusinessStep businessStep = mock(COBBusinessStep.class);
        when(configurationDomainService.isCOBBulkEventEnabled()).thenReturn(false);
        when(applicationContext.getBean(anyString())).thenReturn(businessStep);
        when(businessStep.execute(any())).thenReturn(loan);

        // when
        underTest.run(dummyExecutionMap, loan);

        // then
        verify(businessEventNotifierService, times(0)).startExternalEventRecording();
        verify(businessEventNotifierService, times(0)).stopExternalEventRecording();
    }

    @Test
    public void testGivenBulkEventEnabledWhenCOBRunExceptionThenEventRecordingReset() {
        // given
        Loan loan = mock(Loan.class);
        TreeMap<Long, String> dummyExecutionMap = new TreeMap<>();
        dummyExecutionMap.put(1L, "dummyBusinessStep");
        COBBusinessStep businessStep = mock(COBBusinessStep.class);
        when(configurationDomainService.isCOBBulkEventEnabled()).thenReturn(true);
        when(applicationContext.getBean(anyString())).thenReturn(businessStep);

        doThrow(new BusinessStepException("Test exception")).when(businessStep).execute(any());

        // when
        assertThrows(BusinessStepException.class, () -> underTest.run(dummyExecutionMap, loan));

        // then
        verify(businessEventNotifierService, times(1)).resetEventRecording();
    }

    @Test
    public void testGivenBulkEventDisabledWhenCOBRunExceptionThenEventRecordingResetNotCalled() {
        // given
        Loan loan = mock(Loan.class);
        TreeMap<Long, String> dummyExecutionMap = new TreeMap<>();
        dummyExecutionMap.put(1L, "dummyBusinessStep");
        COBBusinessStep businessStep = mock(COBBusinessStep.class);
        when(configurationDomainService.isCOBBulkEventEnabled()).thenReturn(false);
        when(applicationContext.getBean(anyString())).thenReturn(businessStep);
        doThrow(new BusinessStepException("Test exception")).when(businessStep).execute(any());

        // when
        assertThrows(BusinessStepException.class, () -> underTest.run(dummyExecutionMap, loan));

        // then
        verify(businessEventNotifierService, times(0)).resetEventRecording();
    }

}
