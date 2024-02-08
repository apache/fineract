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
package org.apache.fineract.infrastructure.jobs.service.increasedateby1day.increasebusinessdateby1day;

import static org.junit.Assert.assertEquals;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.configuration.domain.ConfigurationDomainService;
import org.apache.fineract.infrastructure.jobs.service.increasedateby1day.IncreaseDateBy1DayService;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.ExitStatus;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class IncreaseBusinessDateBy1DayTaskletTest {

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private ConfigurationDomainService configurationDomainService;

    @Mock
    private IncreaseDateBy1DayService increaseDateBy1DayService;

    @InjectMocks
    private IncreaseBusinessDateBy1DayTasklet underTest;

    @Test
    public void shouldBusinessDateJobBeSkippedWhenBusinessDateIsNotEnabled() throws Exception {
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(false);

        RepeatStatus repeatStatus = underTest.execute(stepContribution, chunkContext);

        verify(stepContribution, times(1)).setExitStatus(ExitStatus.NOOP);
        assertEquals(RepeatStatus.FINISHED, repeatStatus);
    }

    @Test
    public void shouldBusinessDateJobBeProcessedWhenBusinessDateIsEnabled() throws Exception {
        given(configurationDomainService.isBusinessDateEnabled()).willReturn(true);

        RepeatStatus repeatStatus = underTest.execute(stepContribution, chunkContext);

        verify(increaseDateBy1DayService, times(1)).increaseDateByTypeByOneDay(BusinessDateType.BUSINESS_DATE);
        assertEquals(RepeatStatus.FINISHED, repeatStatus);
    }

}
