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
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.exceptions.CustomJobParameterNotFoundException;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.springframework.batch.core.JobParameter;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.StepExecution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.item.ExecutionContext;
import org.springframework.stereotype.Component;

@Slf4j
@RequiredArgsConstructor
@Component
public class CustomJobParameterResolver {

    private final CustomJobParameterRepository customJobParameterRepository;

    protected Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    public void resolveToJobExecutionContext(final StepContribution contribution, final ChunkContext chunkContext,
            final String[] requiredParameterNames, final String[] optionalParameterNames) {
        final Set<JobParameterDTO> jobParameterDTOList = getCustomJobParameterSet(chunkContext.getStepContext().getStepExecution())
                .orElseThrow(() -> new CustomJobParameterNotFoundException(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY));
        final ExecutionContext jobExecutionContext = contribution.getStepExecution().getJobExecution().getExecutionContext();
        for (String parameterName : requiredParameterNames) {
            final JobParameterDTO dto = jobParameterDTOList.stream().filter(p -> parameterName.equals(p.getParameterName())).findFirst()
                    .orElseThrow(() -> new CustomJobParameterNotFoundException(parameterName));
            jobExecutionContext.put(parameterName, dto.getParameterValue());
        }
        for (String parameterName : optionalParameterNames) {
            jobParameterDTOList.stream().filter(p -> parameterName.equals(p.getParameterName())).findFirst().ifPresentOrElse(
                    dto -> jobExecutionContext.put(parameterName, dto.getParameterValue()),
                    () -> log.warn("Optional custom job parameter '{}' not found in custom parameter table.", parameterName));
        }
    }

    /**
     * Get parameter set from custom job parameter table
     *
     * @param stepExecution
     * @return
     */
    public Optional<Set<JobParameterDTO>> getCustomJobParameterSet(StepExecution stepExecution) {
        Long customJobParameterId = (Long) getJobParameters(stepExecution).get(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY);
        return customJobParameterRepository.findById(customJobParameterId).map(CustomJobParameter::getParameterJson)
                .map(json -> gson.fromJson(json, new TypeToken<HashSet<JobParameterDTO>>() {}.getType()));
    }

    /**
     * Resolve job parameters from step execution context,
     * like @org.springframework.batch.core.scope.context.StepContext#getJobParameters()
     *
     * @param stepExecution
     *            StepExecution context
     * @return
     */
    private Map<String, Object> getJobParameters(StepExecution stepExecution) {
        Map<String, Object> result = new HashMap<>();
        for (Map.Entry<String, JobParameter<?>> entry : stepExecution.getJobParameters().getParameters().entrySet()) {
            result.put(entry.getKey(), entry.getValue().getValue());
        }
        return Collections.unmodifiableMap(result);
    }

}
