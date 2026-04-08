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
package org.apache.fineract.infrastructure.core.config;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.mockito.Mockito.mock;

import java.util.ArrayList;
import java.util.List;
import org.apache.fineract.infrastructure.core.condition.FineractPartitionJobConfigValidationCondition;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.mockito.junit.jupiter.MockitoExtension;
import org.mockito.junit.jupiter.MockitoSettings;
import org.mockito.quality.Strictness;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@ExtendWith(MockitoExtension.class)
@MockitoSettings(strictness = Strictness.LENIENT)
class FineractPartitionJobConfigValidationConditionTest {

    @InjectMocks
    private FineractPartitionJobConfigValidationCondition testObj;

    @Mock
    private AnnotatedTypeMetadata metadata;

    private ConditionContext context;

    @BeforeEach
    void setUp() {
        context = mock(ConditionContext.class);
    }

    @Test
    public void testApplicationStartup_ShouldApplicationStartupFails_WhenConfigurationNumbersAreBelowOne() {
        try (MockedStatic<ExplicitConfigurationPropertiesFactory> propertyFactory = Mockito
                .mockStatic(ExplicitConfigurationPropertiesFactory.class)) {
            propertyFactory.when(() -> ExplicitConfigurationPropertiesFactory.getProperty(context, "fineract.partitioned-job",
                    FineractProperties.FineractPartitionedJob.class)).thenReturn(getBelowOneConfig());
            assertTrue(testObj.matches(context, metadata));
        }
    }

    @Test
    public void testApplicationStartup_ShouldApplicationStart_WhenConfigValid() {
        try (MockedStatic<ExplicitConfigurationPropertiesFactory> propertyFactory = Mockito
                .mockStatic(ExplicitConfigurationPropertiesFactory.class)) {
            propertyFactory.when(() -> ExplicitConfigurationPropertiesFactory.getProperty(context, "fineract.partitioned-job",
                    FineractProperties.FineractPartitionedJob.class)).thenReturn(getValidConfig());
            assertFalse(testObj.matches(context, metadata));
        }
    }

    private FineractProperties.FineractPartitionedJob getValidConfig() {
        FineractProperties.FineractPartitionedJob partitionedJob = new FineractProperties.FineractPartitionedJob();
        List<FineractProperties.PartitionedJobProperty> jobProperties = new ArrayList<>();
        FineractProperties.PartitionedJobProperty partitionedJobProperty = new FineractProperties.PartitionedJobProperty();
        partitionedJobProperty.setJobName("LOAN_COB");
        partitionedJobProperty.setPartitionSize(100);
        partitionedJobProperty.setChunkSize(10);
        partitionedJobProperty.setThreadPoolCorePoolSize(10);
        partitionedJobProperty.setThreadPoolMaxPoolSize(20);
        partitionedJobProperty.setThreadPoolQueueCapacity(10);
        jobProperties.add(partitionedJobProperty);
        partitionedJob.setPartitionedJobProperties(jobProperties);
        return partitionedJob;
    }

    private FineractProperties.FineractPartitionedJob getBelowOneConfig() {
        FineractProperties.FineractPartitionedJob partitionedJob = new FineractProperties.FineractPartitionedJob();
        List<FineractProperties.PartitionedJobProperty> jobProperties = new ArrayList<>();
        FineractProperties.PartitionedJobProperty partitionedJobProperty = new FineractProperties.PartitionedJobProperty();
        partitionedJobProperty.setJobName("LOAN_COB");
        partitionedJobProperty.setPartitionSize(0);
        partitionedJobProperty.setChunkSize(1);
        partitionedJobProperty.setThreadPoolCorePoolSize(1);
        partitionedJobProperty.setThreadPoolMaxPoolSize(1);
        partitionedJobProperty.setThreadPoolQueueCapacity(1);
        jobProperties.add(partitionedJobProperty);
        partitionedJob.setPartitionedJobProperties(jobProperties);
        return partitionedJob;
    }

}
