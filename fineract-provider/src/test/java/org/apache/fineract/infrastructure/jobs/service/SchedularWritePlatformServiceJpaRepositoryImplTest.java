/**
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements. See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership. The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License. You may obtain a copy of the License at
 * <p>
 * http://www.apache.org/licenses/LICENSE-2.0
 * <p>
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied. See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.fineract.infrastructure.jobs.service;

import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetail;
import org.apache.fineract.infrastructure.jobs.domain.SchedulerDetailRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;

import java.util.Collections;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link SchedularWritePlatformServiceJpaRepositoryImpl}
 */
@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class SchedularWritePlatformServiceJpaRepositoryImplTest {

    @InjectMocks
    private SchedularWritePlatformServiceJpaRepositoryImpl underTest;

    @Mock
    private SchedulerDetailRepository schedulerDetailRepository;

    private SchedulerDetail schedulerDetail;

    @BeforeEach
    void setUp() {
        // Create a sample SchedulerDetail for testing
        schedulerDetail = new SchedulerDetail();
        schedulerDetail.setExecuteInstructionForMisfiredJobs(true);
        schedulerDetail.setSuspended(false);
        schedulerDetail.setResetSchedulerOnBootup(false);
    }

    @Test
    void testRetrieveSchedulerDetail_WhenRecordExists_ShouldReturnSchedulerDetail() {
        // Arrange
        Page<SchedulerDetail> page = new PageImpl<>(Collections.singletonList(schedulerDetail));
        when(schedulerDetailRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        SchedulerDetail result = underTest.retrieveSchedulerDetail();

        // Assert
        assertNotNull(result, "SchedulerDetail should not be null");
        assertEquals(schedulerDetail.isExecuteInstructionForMisfiredJobs(), result.isExecuteInstructionForMisfiredJobs(),
                "Execute instruction for misfired jobs should match");
        assertEquals(schedulerDetail.isSuspended(), result.isSuspended(), "Suspended status should match");
        assertEquals(schedulerDetail.isResetSchedulerOnBootup(), result.isResetSchedulerOnBootup(),
                "Reset scheduler on bootup should match");

        // Verify that findAll was called with PageRequest.of(0, 1)
        verify(schedulerDetailRepository, times(1)).findAll(PageRequest.of(0, 1));
    }

    @Test
    void testRetrieveSchedulerDetail_WhenNoRecordExists_ShouldReturnNull() {
        // Arrange
        Page<SchedulerDetail> emptyPage = new PageImpl<>(Collections.emptyList());
        when(schedulerDetailRepository.findAll(any(PageRequest.class))).thenReturn(emptyPage);

        // Act
        SchedulerDetail result = underTest.retrieveSchedulerDetail();

        // Assert
        assertNull(result, "SchedulerDetail should be null when no records exist");

        // Verify that findAll was called with PageRequest.of(0, 1)
        verify(schedulerDetailRepository, times(1)).findAll(PageRequest.of(0, 1));
    }

    @Test
    void testRetrieveSchedulerDetail_ShouldFetchOnlyOneRecord() {
        // Arrange
        Page<SchedulerDetail> page = new PageImpl<>(Collections.singletonList(schedulerDetail));
        when(schedulerDetailRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        underTest.retrieveSchedulerDetail();

        // Assert - Verify that PageRequest was created with page=0 and size=1
        verify(schedulerDetailRepository, times(1)).findAll(PageRequest.of(0, 1));
    }

    @Test
    void testRetrieveSchedulerDetail_WithMultipleRecords_ShouldReturnFirstRecord() {
        // Arrange
        SchedulerDetail firstDetail = new SchedulerDetail();
        firstDetail.setExecuteInstructionForMisfiredJobs(true);
        firstDetail.setSuspended(false);

        // Even though we're testing with PageRequest.of(0, 1), let's verify it returns the first one
        Page<SchedulerDetail> page = new PageImpl<>(Collections.singletonList(firstDetail));
        when(schedulerDetailRepository.findAll(any(PageRequest.class))).thenReturn(page);

        // Act
        SchedulerDetail result = underTest.retrieveSchedulerDetail();

        // Assert
        assertNotNull(result, "SchedulerDetail should not be null");
        assertEquals(firstDetail.isExecuteInstructionForMisfiredJobs(), result.isExecuteInstructionForMisfiredJobs(),
                "Should return the first record");
        verify(schedulerDetailRepository, times(1)).findAll(PageRequest.of(0, 1));
    }
}

