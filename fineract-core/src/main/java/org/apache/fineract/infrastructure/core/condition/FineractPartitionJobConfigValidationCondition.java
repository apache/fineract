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
package org.apache.fineract.infrastructure.core.condition;

import java.util.List;
import java.util.function.Predicate;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.infrastructure.core.config.ExplicitConfigurationPropertiesFactory;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.context.annotation.Condition;
import org.springframework.context.annotation.ConditionContext;
import org.springframework.core.type.AnnotatedTypeMetadata;

@Slf4j
public class FineractPartitionJobConfigValidationCondition implements Condition {

    @Override
    public boolean matches(ConditionContext context, AnnotatedTypeMetadata metadata) {
        FineractProperties.FineractPartitionedJob partitionedJobProperties = ExplicitConfigurationPropertiesFactory.getProperty(context,
                "fineract.partitioned-job", FineractProperties.FineractPartitionedJob.class);
        if (partitionedJobProperties != null) {
            List<FineractProperties.PartitionedJobProperty> invalidConfigs = partitionedJobProperties.getPartitionedJobProperties().stream()
                    .filter(isAnyConfigBelowOne().or(FineractPartitionJobConfigValidationCondition::invalidMaxPoolSize)).toList();
            if (!invalidConfigs.isEmpty()) {
                for (FineractProperties.PartitionedJobProperty invalidConfig : invalidConfigs) {
                    log.error(
                            "{} partitioned job is not configured properly. The partition size, chunk size and thread count must be more than 0, and partition size must be less then chunk size * thread count",
                            invalidConfig.getJobName());
                }
            }
            return !invalidConfigs.isEmpty();
        } else {
            return false;
        }
    }

    private static Predicate<FineractProperties.PartitionedJobProperty> isAnyConfigBelowOne() {
        return partitionedJobProperty -> !(partitionedJobProperty.getPartitionSize() > 0 && partitionedJobProperty.getChunkSize() > 0
                && partitionedJobProperty.getThreadPoolCorePoolSize() > 0 && partitionedJobProperty.getThreadPoolMaxPoolSize() > 0
                && partitionedJobProperty.getThreadPoolQueueCapacity() > 0);
    }

    private static boolean invalidMaxPoolSize(FineractProperties.PartitionedJobProperty partitionedJobProperty) {
        return partitionedJobProperty.getThreadPoolMaxPoolSize() < partitionedJobProperty.getThreadPoolCorePoolSize();
    }
}
