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
package org.apache.fineract.infrastructure.jobs.service.aggregationjob.listener;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.time.ZoneId;
import java.util.HashMap;
import java.util.Map;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenant;
import org.apache.fineract.infrastructure.core.domain.FineractPlatformTenantConnection;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.JournalEntryAggregationJobConstant;
import org.apache.fineract.infrastructure.jobs.service.aggregationjob.domain.JournalEntryAggregationTrackingRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.batch.core.JobExecution;
import org.springframework.batch.core.scope.context.JobContext;
import org.springframework.batch.item.ExecutionContext;

@ExtendWith(MockitoExtension.class)
public class JournalEntryAggregationJobListenerTest {

    private final LocalDate businessDate = LocalDate.now(ZoneId.of("UTC"));

    @Mock
    FineractPlatformTenantConnection fineractPlatformTenantConnection;

    @Mock
    private FineractProperties fineractProperties;
    @Mock
    private FineractProperties.FineractJobProperties fineractJobProperties;
    @Mock
    private FineractProperties.FineractJournalEntryAggregationProperties fineractJournalEntryAggregationProperties;
    @Mock
    private JournalEntryAggregationTrackingRepository trackingRepository;
    @Mock
    private JobExecution jobExecution;
    @Mock
    private JobContext jobContext;
    @Mock
    private ExecutionContext executionContext;

    @InjectMocks
    private JournalEntryAggregationJobListener listener;

    @BeforeEach
    public void setUp() {
        ThreadLocalContextUtil
                .setTenant(new FineractPlatformTenant(1L, "default", "Default Tenant", "default", fineractPlatformTenantConnection));

        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.COB_DATE, businessDate.minusDays(1))));
        ThreadLocalContextUtil.setBusinessDates(new HashMap<>(Map.of(BusinessDateType.BUSINESS_DATE, businessDate)));

    }

    @Test
    public void testBeforeJobWhenLastAggregatedOnDateNull() {
        // Arrange
        int lookBackDays = 2;

        when(fineractProperties.getJob()).thenReturn(fineractJobProperties);
        when(fineractJobProperties.getJournalEntryAggregation()).thenReturn(fineractJournalEntryAggregationProperties);
        when(fineractJobProperties.getJournalEntryAggregation().getExcludeRecentNDays()).thenReturn(lookBackDays);
        when(trackingRepository.findLatestAggregatedOnDate()).thenReturn(null);

        ExecutionContext executionContext = new ExecutionContext();
        when(jobExecution.getExecutionContext()).thenReturn(executionContext);

        // Act
        listener.beforeJob(jobExecution);

        // Assert
        assertEquals(businessDate.minusDays(lookBackDays), executionContext.get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE));
        assertEquals(LocalDate.of(1970, 1, 1), executionContext.get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM));
        assertNull(executionContext.get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE));
    }

    @Test
    public void testBeforeJobWithLastAggregatedOnDate() {
        // Arrange
        LocalDate businessDate = LocalDate.now(ZoneId.of("UTC"));
        LocalDate lastAggregatedOnDate = businessDate.minusDays(5);
        int lookBackDays = 2;

        when(fineractProperties.getJob()).thenReturn(fineractJobProperties);
        when(fineractJobProperties.getJournalEntryAggregation()).thenReturn(fineractJournalEntryAggregationProperties);
        when(fineractJobProperties.getJournalEntryAggregation().getExcludeRecentNDays()).thenReturn(lookBackDays);
        when(trackingRepository.findLatestAggregatedOnDate()).thenReturn(lastAggregatedOnDate);

        JobExecution jobExecution = new JobExecution(1L);

        // Act
        listener.beforeJob(jobExecution);

        // Assert
        assertEquals(businessDate.minusDays(lookBackDays),
                jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE));
        assertEquals(businessDate.minusDays(lookBackDays),
                jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_TO));
        assertEquals(lastAggregatedOnDate,
                jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.LAST_AGGREGATED_ON_DATE));
        assertEquals(lastAggregatedOnDate,
                jobExecution.getExecutionContext().get(JournalEntryAggregationJobConstant.AGGREGATED_ON_DATE_FROM));
    }
}
