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
package org.apache.fineract.portfolio.loanaccount.jobs.generateloanlossprovisioning;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.springframework.batch.repeat.RepeatStatus.FINISHED;

import java.time.LocalDate;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.apache.fineract.accounting.provisioning.exception.ProvisioningEntryAlreadyCreatedException;
import org.apache.fineract.accounting.provisioning.service.ProvisioningEntriesWritePlatformService;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.organisation.provisioning.data.ProvisioningCriteriaData;
import org.apache.fineract.organisation.provisioning.service.ProvisioningCriteriaReadPlatformService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.repeat.RepeatStatus;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class GenerateLoanlossProvisioningTaskletTest {

    public static final LocalDate BUSINESS_DATE = LocalDate.of(2022, 6, 12);

    @Mock
    private StepContribution stepContribution;

    @Mock
    private ChunkContext chunkContext;

    @Mock
    private ProvisioningCriteriaReadPlatformService provisioningCriteriaReadPlatformService;
    @Mock
    private ProvisioningEntriesWritePlatformService provisioningEntriesWritePlatformService;

    @InjectMocks
    private GenerateLoanlossProvisioningTasklet underTest;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, BUSINESS_DATE)));
    }

    @Test
    public void testExecuteShouldCreateProvisioningEntry() throws Exception {
        // given
        Collection<ProvisioningCriteriaData> provisioningCriterias = List.of(new ProvisioningCriteriaData());
        given(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias()).willReturn(provisioningCriterias);
        // when
        RepeatStatus result = underTest.execute(stepContribution, chunkContext);
        // then
        verify(provisioningEntriesWritePlatformService).createProvisioningEntry(BUSINESS_DATE, true);
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    public void testExecuteShouldNotCreateProvisioningEntryWhenNoProvisioningCriteriasArePresent() throws Exception {
        // given
        Collection<ProvisioningCriteriaData> provisioningCriterias = List.of();
        given(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias()).willReturn(provisioningCriterias);
        // when
        RepeatStatus result = underTest.execute(stepContribution, chunkContext);
        // then
        verifyNoInteractions(provisioningEntriesWritePlatformService);
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    public void testExecuteShouldNotCreateProvisioningEntryWhenNullProvisioningCriteriasArePresent() throws Exception {
        // given
        given(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias()).willReturn(null);
        // when
        RepeatStatus result = underTest.execute(stepContribution, chunkContext);
        // then
        verifyNoInteractions(provisioningEntriesWritePlatformService);
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    public void testExecuteShouldNotFailWhenProvisioningEntryIsAlreadyCreated() throws Exception {
        // given
        Collection<ProvisioningCriteriaData> provisioningCriterias = List.of(new ProvisioningCriteriaData());
        given(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias()).willReturn(provisioningCriterias);
        given(provisioningEntriesWritePlatformService.createProvisioningEntry(BUSINESS_DATE, true))
                .willThrow(new ProvisioningEntryAlreadyCreatedException(1L, BUSINESS_DATE));
        // when
        RepeatStatus result = underTest.execute(stepContribution, chunkContext);
        // then
        verify(provisioningEntriesWritePlatformService).createProvisioningEntry(BUSINESS_DATE, true);
        assertThat(result).isEqualTo(FINISHED);
    }

    @Test
    public void testExecuteShouldNotFailWhenExceptionIsThrownInProvisioningEntryCreation() throws Exception {
        // given
        Collection<ProvisioningCriteriaData> provisioningCriterias = List.of(new ProvisioningCriteriaData());
        given(provisioningCriteriaReadPlatformService.retrieveAllProvisioningCriterias()).willReturn(provisioningCriterias);
        given(provisioningEntriesWritePlatformService.createProvisioningEntry(BUSINESS_DATE, true)).willThrow(new RuntimeException("Test"));
        // when
        RepeatStatus result = underTest.execute(stepContribution, chunkContext);
        // then
        verify(provisioningEntriesWritePlatformService).createProvisioningEntry(BUSINESS_DATE, true);
        assertThat(result).isEqualTo(FINISHED);
    }

}
