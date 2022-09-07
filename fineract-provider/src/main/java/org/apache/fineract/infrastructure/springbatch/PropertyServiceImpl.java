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
import lombok.RequiredArgsConstructor;
import org.apache.fineract.infrastructure.core.config.FineractProperties;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class PropertyServiceImpl implements PropertyService {

    private final FineractProperties fineractProperties;

    @Override
    public Integer getPartitionSize(String jobName) {
        List<FineractProperties.PartitionedJobProperty> jobProperties = fineractProperties.getPartitionedJob()
                .getPartitionedJobProperties();
        return jobProperties.stream() //
                .filter(jobProperty -> jobName.equals(jobProperty.getJobName())) //
                .findFirst() //
                .map(FineractProperties.PartitionedJobProperty::getPartitionSize) //
                .orElse(1);
    }

    @Override
    public Integer getChunkSize(String jobName) {
        List<FineractProperties.PartitionedJobProperty> jobProperties = fineractProperties.getPartitionedJob()
                .getPartitionedJobProperties();
        return jobProperties.stream() //
                .filter(jobProperty -> jobName.equals(jobProperty.getJobName())) //
                .findFirst() //
                .map(FineractProperties.PartitionedJobProperty::getChunkSize) //
                .orElse(1);
    }
}
