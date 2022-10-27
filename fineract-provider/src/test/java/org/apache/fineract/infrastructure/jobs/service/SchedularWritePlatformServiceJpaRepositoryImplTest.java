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
package org.apache.fineract.infrastructure.jobs.service;

import org.apache.fineract.infrastructure.jobs.data.JobDetailDataValidator;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobDetailRepository;
import org.apache.fineract.infrastructure.jobs.domain.ScheduledJobRunHistoryRepository;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetailRepository;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;

import java.util.Collections;
import java.util.List;

import static org.mockito.BDDMockito.given;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedularWritePlatformServiceJpaRepositoryImplTest {

    @Mock
    private ScheduledJobDetailRepository scheduledJobDetailsRepository;

    @Mock
    private ScheduledJobRunHistoryRepository scheduledJobRunHistoryRepository;

    @Mock
    private SchedulerDetailRepository schedulerDetailRepository;

    @Mock
    private JobDetailDataValidator dataValidator;

    @InjectMocks
    private  SchedularWritePlatformServiceJpaRepositoryImpl schedularWritePlatformService;

    @Test
    void testRetrieveSchedulerDetail_handle_null_response() {
        // given
        given(schedulerDetailRepository.findAll()).willReturn(null);

        // when
        final var result = schedularWritePlatformService.retrieveSchedulerDetail();

        // then
        Assertions.assertNull(result);
    }

    @Test
    void testRetrieveSchedulerDetail_handle_empty_response() {
        // given
        given(schedulerDetailRepository.findAll()).willReturn(Collections.emptyList());

        // when
        final var result = schedularWritePlatformService.retrieveSchedulerDetail();

        // then
        Assertions.assertNull(result);
    }

    @Test
    void testRetrieveSchedulerDetail_handle_not_empty_response() {
        // given
        SchedulerDetail schedulerDetail = new SchedulerDetail();
        given(schedulerDetailRepository.findAll()).willReturn(List.of(schedulerDetail));

        // when
        final var result = schedularWritePlatformService.retrieveSchedulerDetail();

        // then
        Assertions.assertEquals(schedulerDetail, result);
    }
}
