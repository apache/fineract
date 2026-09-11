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
package org.apache.fineract.cob.savings;

import com.google.common.reflect.TypeToken;
import com.google.gson.Gson;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.fineract.cob.COBBusinessStepService;
import org.apache.fineract.cob.COBConstant;
import org.apache.fineract.cob.common.CustomJobParameterResolver;
import org.apache.fineract.cob.data.BusinessStepNameAndOrder;
import org.apache.fineract.cob.exceptions.CustomJobParameterNotFoundException;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.domain.ActionContext;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.springframework.batch.core.StepContribution;
import org.springframework.batch.core.scope.context.ChunkContext;
import org.springframework.batch.core.step.tasklet.Tasklet;
import org.springframework.batch.repeat.RepeatStatus;

@Slf4j
@RequiredArgsConstructor
public class InlineSavingsCOBBuildExecutionContextTasklet implements Tasklet {

    private final COBBusinessStepService cobBusinessStepService;
    private final CustomJobParameterRepository customJobParameterRepository;
    private final CustomJobParameterResolver customJobParameterResolver;

    private final Gson gson = GoogleGsonSerializerHelper.createSimpleGson();

    public Set<BusinessStepNameAndOrder> resolveBusinessSteps() {
        return cobBusinessStepService.getCOBBusinessSteps(SavingsCOBBusinessStep.class, SavingsCOBConstant.SAVINGS_COB_JOB_NAME);
    }

    @Override
    public RepeatStatus execute(StepContribution contribution, ChunkContext chunkContext) throws Exception {
        HashMap<BusinessDateType, LocalDate> businessDates = ThreadLocalContextUtil.getBusinessDates();
        ThreadLocalContextUtil.setActionContext(ActionContext.COB);
        Set<BusinessStepNameAndOrder> cobBusinessSteps = resolveBusinessSteps();
        contribution.getStepExecution().getExecutionContext().put(COBConstant.COB_PARAMETER, getSavingsIdsFromJobParameters(chunkContext));
        contribution.getStepExecution().getExecutionContext().put(COBConstant.BUSINESS_STEPS, cobBusinessSteps);
        String businessDateString = getBusinessDateFromJobParameters(chunkContext);
        contribution.getStepExecution().getExecutionContext().put(COBConstant.BUSINESS_DATE_PARAMETER_NAME, businessDateString);
        LocalDate businessDate = LocalDate.parse(businessDateString, DateTimeFormatter.ISO_DATE);
        businessDates.put(BusinessDateType.COB_DATE, businessDate);
        businessDates.put(BusinessDateType.BUSINESS_DATE, businessDate.plusDays(1));
        ThreadLocalContextUtil.setBusinessDates(businessDates);
        return RepeatStatus.FINISHED;
    }

    private String getBusinessDateFromJobParameters(ChunkContext chunkContext) {
        Long customJobParameterId = (Long) chunkContext.getStepContext().getJobParameters().get(COBConstant.BUSINESS_DATE_PARAMETER_NAME);
        CustomJobParameter customJobParameter = customJobParameterRepository.findById(customJobParameterId)
                .orElseThrow(() -> new CustomJobParameterNotFoundException(String.valueOf(customJobParameterId)));
        String parameterJson = customJobParameter.getParameterJson();
        Set<JobParameterDTO> jobParameters = gson.fromJson(parameterJson, new TypeToken<HashSet<JobParameterDTO>>() {}.getType());
        JobParameterDTO businessDateParameter = jobParameters.stream()
                .filter(jobParameterDTO -> jobParameterDTO.getParameterName().equals(COBConstant.BUSINESS_DATE_PARAMETER_NAME)).findFirst()
                .orElseThrow(() -> new CustomJobParameterNotFoundException(COBConstant.BUSINESS_DATE_PARAMETER_NAME));
        return businessDateParameter.getParameterValue();
    }

    private List<Long> getSavingsIdsFromJobParameters(ChunkContext chunkContext) {
        Set<JobParameterDTO> jobParameters = customJobParameterResolver
                .getCustomJobParameterSet(chunkContext.getStepContext().getStepExecution())
                .orElseThrow(() -> new CustomJobParameterNotFoundException(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY));
        JobParameterDTO savingsIdsParameter = jobParameters.stream()
                .filter(jobParameterDTO -> jobParameterDTO.getParameterName().equals(SavingsCOBConstant.SAVINGS_IDS_PARAMETER_NAME))
                .findFirst().orElseThrow(() -> new CustomJobParameterNotFoundException(SavingsCOBConstant.SAVINGS_IDS_PARAMETER_NAME));
        return gson.fromJson(savingsIdsParameter.getParameterValue(), new TypeToken<ArrayList<Long>>() {}.getType());
    }
}
