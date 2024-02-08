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
import org.apache.fineract.cob.exceptions.BusinessStepException;
import org.apache.fineract.cob.exceptions.BusinessStepNotBelongsToJobException;
import org.apache.fineract.infrastructure.core.api.JsonCommand;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResult;
import org.apache.fineract.infrastructure.core.data.CommandProcessingResultBuilder;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class ConfigJobParameterServiceImpl implements ConfigJobParameterService, InitializingBean {

    private final BatchBusinessStepRepository batchBusinessStepRepository;
    private final BusinessStepConfigDataParser dataParser;
    private final BusinessStepCategoryService businessStepCategoryService;
    private final ApplicationContext applicationContext;
    private final BusinessStepMapper mapper;
    private JobBusinessStepDetail availableBusinessStepsForLoan;

    @Override
    public void afterPropertiesSet() throws Exception {
        availableBusinessStepsForLoan = getAvailableBusinessStepsByJobName(BusinessStepCategory.LOAN.name());
    }

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
        if (businessSteps.isEmpty()) {
            throw new BusinessStepException("A job needs to have 1 business step at least.");
        }
        List<String> availableBusinessStepNames = availableBusinessStepsForLoan.getAvailableBusinessSteps().stream()
                .map(BusinessStepDetail::getStepName).toList();
        List<String> notValidBusinessStepNames = businessSteps.stream().map(BusinessStep::getStepName)
                .filter(businessStepName -> !availableBusinessStepNames.contains(businessStepName)).toList();
        if (notValidBusinessStepNames.isEmpty()) {
            batchBusinessStepRepository.deleteAllByJobName(jobName);
            businessSteps.forEach(newBusinessStepConfig -> {
                BatchBusinessStep batchBusinessStep = new BatchBusinessStep();
                batchBusinessStep.setJobName(jobName);
                batchBusinessStep.setStepName(newBusinessStepConfig.getStepName());
                batchBusinessStep.setStepOrder(newBusinessStepConfig.getOrder());
                batchBusinessStepRepository.save(batchBusinessStep);
            });
        } else {
            throw new BusinessStepException(notValidBusinessStepNames + " Business steps are not configurable for this job.");
        }
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

    @Override
    public List<String> getAllConfiguredJobNames() {
        return batchBusinessStepRepository.findConfiguredJobNames();
    }
}
