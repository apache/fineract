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
package org.apache.fineract.infrastructure.jobs.service.jobparameterprovider;

import com.google.gson.Gson;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Map;
import java.util.Optional;
import java.util.Set;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.loan.LoanCOBConstant;
import org.apache.fineract.infrastructure.businessdate.domain.BusinessDateType;
import org.apache.fineract.infrastructure.core.serialization.GoogleGsonSerializerHelper;
import org.apache.fineract.infrastructure.core.service.ThreadLocalContextUtil;
import org.apache.fineract.infrastructure.jobs.data.JobParameterDTO;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameter;
import org.apache.fineract.infrastructure.jobs.domain.CustomJobParameterRepository;
import org.apache.fineract.infrastructure.jobs.service.JobName;
import org.apache.fineract.infrastructure.springbatch.SpringBatchJobConstants;
import org.springframework.batch.core.JobParameter;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
@RequiredArgsConstructor
public class LoanCOBJobParameterProvider extends AbstractJobParameterProvider implements InitializingBean {

    private final CustomJobParameterRepository customJobParameterRepository;
    private final GoogleGsonSerializerHelper gsonFactory;

    private Gson gson;

    @Override
    public void afterPropertiesSet() throws Exception {
        this.gson = gsonFactory.createSimpleGson();
    }

    @Override
    @Transactional
    public Map<String, JobParameter> provide(Set<JobParameterDTO> jobParameterDTOSet) {
        Map<String, JobParameter> jobParameterMap = new HashMap<>();
        CustomJobParameter customJobParameter = new CustomJobParameter();
        customJobParameter.setParameterJson(gson.toJson(getJobParameterDTOListWithCorrectBusinessDate(jobParameterDTOSet)));
        CustomJobParameter savedCustomJobParameter = customJobParameterRepository.saveAndFlush(customJobParameter);
        jobParameterMap.put(SpringBatchJobConstants.CUSTOM_JOB_PARAMETER_ID_KEY, new JobParameter(savedCustomJobParameter.getId()));
        return jobParameterMap;
    }

    @Override
    public String getJobName() {
        return JobName.LOAN_COB.name();
    }

    private Set<JobParameterDTO> getJobParameterDTOListWithCorrectBusinessDate(Set<JobParameterDTO> jobParameterDTOset) {
        Set<JobParameterDTO> jobParameterDTOListWithCorrectBusinessDate = jobParameterDTOset.isEmpty() ? new HashSet<>()
                : new HashSet<>(jobParameterDTOset);
        Optional<JobParameterDTO> optionalBusinessDateJobParameter = jobParameterDTOListWithCorrectBusinessDate.stream()
                .filter(jobParameterDTO -> LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME.equals(jobParameterDTO.getParameterName()))
                .findFirst();
        if (optionalBusinessDateJobParameter.isEmpty()) {
            jobParameterDTOListWithCorrectBusinessDate.add(new JobParameterDTO(LoanCOBConstant.BUSINESS_DATE_PARAMETER_NAME,
                    ThreadLocalContextUtil.getBusinessDateByType(BusinessDateType.COB_DATE).format(DateTimeFormatter.ISO_DATE)));
        }
        return jobParameterDTOListWithCorrectBusinessDate;
    }
}
