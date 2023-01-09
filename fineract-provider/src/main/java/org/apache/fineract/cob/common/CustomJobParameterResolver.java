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
package org.apache.fineract.cob.common;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.util.HashSet;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.exceptions.CustomJobParameterNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;

@RequiredArgsConstructor
@Component
public class CustomJobParameterResolver implements InitializingBean {

    private final GoogleGsonSerializerHelper gsonFactory;
    private final CustomJobParameterRepository customJobParameterRepository;

    protected Gson gson;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.gson = gsonFactory.createSimpleGson();
    }

    public void resolve(StepContribution contribution, ChunkContext chunkContext, String customJobParameterKey,
            String parameterNameInExecutionContext) {
        Set<JobParameterDTO> jobParameterDTOList = getCustomJobParameterSet(chunkContext);
        JobParameterDTO businessDateParameter = jobParameterDTOList.stream()
                .filter(jobParameterDTO -> customJobParameterKey.equals(jobParameterDTO.getParameterName())) //
                .findFirst().orElseThrow(() -> new CustomJobParameterNotFoundException(customJobParameterKey));
        contribution.getStepExecution().getExecutionContext().put(parameterNameInExecutionContext,
                businessDateParameter.getParameterValue());
    }

    private Set<JobParameterDTO> getCustomJobParameterSet(ChunkContext chunkContext) {
        Long customJobParameterId = (Long) chunkContext.getStepContext().getJobParameters()
                .get(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY);
        CustomJobParameter customJobParameter = customJobParameterRepository.findById(customJobParameterId)
                .orElseThrow(() -> new CustomJobParameterNotFoundException(customJobParameterId));
        String parameterJson = customJobParameter.getParameterJson();
        return gson.fromJson(parameterJson, new TypeToken<HashSet<JobParameterDTO>>() {}.getType());
    }
}
