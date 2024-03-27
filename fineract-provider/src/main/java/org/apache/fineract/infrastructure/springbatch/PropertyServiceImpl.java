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
package org.apache.fineract.infrastructure.springbatch;

import java.util.List;
import java.util.function.Function;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final FineractProperties fineractProperties;

    @Override
    public Integer getPartitionSize(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getPartitionSize);
    }

    @Override
    public Integer getChunkSize(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getChunkSize);
    }

    @Override
    public Integer getRetryLimit(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getRetryLimit);
    }

    @Override
    public Integer getThreadPoolCorePoolSize(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getThreadPoolCorePoolSize);
    }

    @Override
    public Integer getThreadPoolMaxPoolSize(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getThreadPoolMaxPoolSize);
    }

    @Override
    public Integer getThreadPoolQueueCapacity(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getThreadPoolQueueCapacity);
    }

    @Override
    public Integer getPollInterval(String jobName) {
        return getProperty(jobName, FineractProperties.PartitionedJobProperty::getPollInterval);
    }

    private Integer getProperty(String jobName, Function<? super FineractProperties.PartitionedJobProperty, Integer> function) {
        List<FineractProperties.PartitionedJobProperty> jobProperties = fineractProperties.getPartitionedJob()
                .getPartitionedJobProperties();
        return jobProperties.stream() //
                .filter(jobProperty -> jobName.equals(jobProperty.getJobName())) //
                .findFirst() //
                .map(function) //
                .orElse(1);
    }
}
