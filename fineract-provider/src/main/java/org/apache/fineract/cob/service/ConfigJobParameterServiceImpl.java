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
package org.apache.fineract.cob.service;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.apache.fineract.cob.COBBusinessStep;
import org.apache.fineract.cob.data.BusinessStep;
import org.apache.fineract.cob.data.BusinessStepDetail;
import org.apache.fineract.cob.data.JobBusinessStepConfigData;
import org.apache.fineract.cob.data.JobBusinessStepDetail;
import org.apache.fineract.cob.domain.BatchBusinessStep;
import org.apache.fineract.cob.domain.BatchBusinessStepRepository;
import org.apache.fineract.cob.exceptions.BusinessStepNotBelongsToJobException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigJobParameterServiceImpl implements ConfigJobParameterService {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final BusinessStepConfigDataParser dataParser;
    private final BusinessStepCategoryService businessStepCategoryService;
    private final ApplicationContext applicationContext;
    private final BusinessStepMapper mapper;

    @Override
    public JobBusinessStepConfigData getBusinessStepConfigByJobName(String jobName) {
        List<BatchBusinessStep> batchBusinessSteps = batchBusinessStepRepository.findAllByJobName(jobName);
        JobBusinessStepConfigData jobBusinessStepConfigData = new JobBusinessStepConfigData();
        jobBusinessStepConfigData.setJobName(jobName);
        jobBusinessStepConfigData.setBusinessSteps(mapper.map(batchBusinessSteps));
        return jobBusinessStepConfigData;
    }

    @Override
    public CommandProcessingResult updateStepConfigByJobName(JsonCommand command, String jobName)
            throws BusinessStepNotBelongsToJobException {
        List<BusinessStep> businessSteps = dataParser.parseUpdate(command);
        List<BatchBusinessStep> batchBusinessSteps = batchBusinessStepRepository.findAllByJobName(jobName);
        businessSteps.forEach(newBusinessStepConfig -> {
            BatchBusinessStep batchBusinessStep = batchBusinessSteps.stream()
                    .filter(oldBusinessStepConfig -> oldBusinessStepConfig.getStepName().equals(newBusinessStepConfig.getStepName()))
                    .findFirst().orElseThrow(BusinessStepNotBelongsToJobException::new);
            batchBusinessStep.setStepName(newBusinessStepConfig.getStepName());
            batchBusinessStep.setStepOrder(newBusinessStepConfig.getOrder());
            batchBusinessStepRepository.save(batchBusinessStep);
        });
        return new CommandProcessingResultBuilder().withCommandId(command.commandId()).build();
    }

    @Override
    public JobBusinessStepDetail getAvailableBusinessStepsByJobName(String jobName) {
        Class<? extends COBBusinessStep> businessStepClass = businessStepCategoryService.getBusinessStepByCategory(jobName);
        if (businessStepClass == null) {
            return null;
        }
        List<String> businessStepBeanNames = Arrays.stream(applicationContext.getBeanNamesForType(businessStepClass)).toList();
        JobBusinessStepDetail jobBusinessStepDetail = new JobBusinessStepDetail();
        List<BusinessStepDetail> availableBusinessSteps = new ArrayList<>();
        for (String businessStepBean : businessStepBeanNames) {
            COBBusinessStep businessStep = (COBBusinessStep) applicationContext.getBean(businessStepBean);
            BusinessStepDetail businessStepDetail = new BusinessStepDetail();
            businessStepDetail.setStepName(businessStep.getEnumStyledName());
            businessStepDetail.setStepDescription(businessStep.getHumanReadableName());
            availableBusinessSteps.add(businessStepDetail);
        }
        jobBusinessStepDetail.setJobName(jobName);
        jobBusinessStepDetail.setAvailableBusinessSteps(availableBusinessSteps);
        return jobBusinessStepDetail;
    }
}
